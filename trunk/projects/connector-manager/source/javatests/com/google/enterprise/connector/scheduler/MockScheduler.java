// Copyright 2006 Google Inc.  All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.common.WorkQueueItem;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.monitor.Monitor;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple MockScheduler class.  This class is thread safe.  Must initialize
 * object before running it.
 */
public class MockScheduler implements Scheduler {
  public static final String SCHEDULER_CURRENT_TIME = "scheduler/currentTime";
  
  private Instantiator instantiator;
  private Monitor monitor;
  private WorkQueue workQueue;
  private List schedules;
  
  private HostLoadManager hostLoadManager;
  
  private boolean isInitialized;
  private boolean isShutdown;
  
  public MockScheduler(Instantiator instantiator, Monitor monitor, 
      WorkQueue workQueue, List schedules) {
    this.instantiator = instantiator;
    this.monitor = monitor;
    this.workQueue = workQueue;
    this.schedules = schedules;
    this.hostLoadManager = new HostLoadManager(100);
    this.isInitialized = false;
    this.isShutdown = false;
  }
  
  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    workQueue.init();
    isInitialized = true;
    isShutdown = false;
  }
  
  public synchronized void shutdown(boolean force) {
    if (isShutdown) {
      return;
    }
    workQueue.shutdown(force);
    isInitialized = false;
    isShutdown = true;
  }
  
  /**
   * Return a hardcoded schedule.
   * @return a hardcoded schedule.
   */
  private List getSchedules() {
    return schedules;
  }

  private Traverser getTraverser(String connectorName) {
    Traverser traverser = null;
    try {
      traverser = instantiator.getTraverser(connectorName); 
    } catch (ConnectorNotFoundException cnfe) {
      cnfe.printStackTrace();
    } catch (InstantiatorException ie) {
      // TODO(danny): please evaluate whether this is the right thing to do
      ie.printStackTrace();
    }
    return traverser;
  }

  private void updateMonitor() {
    Map vars = new HashMap();
    vars.put(SCHEDULER_CURRENT_TIME, new Date());
    monitor.setVariables(vars);
  }

  private boolean shouldRun(Schedule schedule) {
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int minute = now.get(Calendar.MINUTE);
    List timeIntervals = schedule.getTimeIntervals();
    for (Iterator iter = timeIntervals.iterator(); iter.hasNext(); ) {
      ScheduleTimeInterval interval = (ScheduleTimeInterval) iter.next();
      int startHour = interval.getStartTime().getHour();
      int endHour = interval.getEndTime().getHour();
      if (0 == endHour) {
        endHour = 24;
      }
      if ((hour >= startHour) && (hour < endHour)) {
        return true;
      }
    }
    return false;
  }
  
  public synchronized void run() {
    Map runnables = new HashMap();  // <connectorName, Runnable>
    while (isInitialized && !isShutdown) {
      List schedules = getSchedules();
      for (Iterator iter = schedules.iterator(); iter.hasNext(); ){
        Schedule schedule = (Schedule) iter.next();
        if (shouldRun(schedule)) {
          String connectorName = schedule.getConnectorName();
          TraverserRunnable runnable = 
            (TraverserRunnable) runnables.get(connectorName);
          if (null == runnable) {
            runnable = new TraverserRunnable(connectorName, 5000);
            runnables.put(connectorName, runnable);
          }
          // we back off if we have received previous failures (e.g. when trying
          // to get a Traverser object)
          if (runnable.getNumConsecutiveFailures() >= 2) {
            long backoff = 
              1000 * (long) Math.pow(2, runnable.getNumConsecutiveFailures());
            long now = System.currentTimeMillis();
            if (runnable.getTimeOfFirstFailure() + backoff > now) {
              System.out.println("Backing off due to previous failures: "
                + connectorName);
              System.out.println("Backoff (ms): " + backoff);
              continue;
            }
          }
          System.out.println("Adding work to workQueue.");
          workQueue.addWork(runnable);
          int numDocsTraversed = runnable.getNumDocsTraversed();
          if (numDocsTraversed > 0) {
            hostLoadManager.updateNumDocsTraversed(connectorName, 
              numDocsTraversed);
          } else {
            // Traversal of 0 documents indicates some type of problem.
            // It may be that runnable is not returning or able to be 
            // interrupted.
            runnable.failure();
          }
        }
      }
      updateMonitor();
      
      // Give someone else a chance to run.
      try {
        wait(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private class TraverserRunnable extends WorkQueueItem {
    private String connectorName;
    private int numDocsTraversed;
    private boolean isFinished;
    
    private int numConsecutiveFailures;
    // the time in millis of the first consecutive failure given the 
    // numConsecutiveFailures is > 0.
    private long timeOfFirstFailure;
    
    // time allowed for doing a traversal before we give up
    private long timeout;
    
    public TraverserRunnable(String connectorName, long timeout) {
      this.connectorName = connectorName;
      this.numDocsTraversed = 0;
      this.isFinished = false;
      this.numConsecutiveFailures = 0;
      this.timeOfFirstFailure = 0;
      this.timeout = timeout;
    }
    
    public int getNumDocsTraversed() {
      waitTillFinishedOrTimeout();
      return numDocsTraversed;
    }

    /**
     * Wait until the run is finished.  Object lock must be held to call this 
     * method.
     */
    private void waitTillFinishedOrTimeout() {
      if (!isFinished) {
        try {
          synchronized(this) {
            wait(timeout);
          }
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    /**
     * Retrieve the number of consecutive failures.  A successful run,  resets
     * this count.
     * @return the number of consecutive failures
     */
    public int getNumConsecutiveFailures() {
      return numConsecutiveFailures;
    }
    
    /**
     * Time of the first consecutive failure given that 
     * getNumConsecutiveFailures() is > 0.
     * @return time in millis
     */
    public long getTimeOfFirstFailure() {
      return timeOfFirstFailure;
    }
    
    public void doWork() {
      Traverser traverser = getTraverser(connectorName);
      if (null != traverser) {
        try {
          System.out.println("Begin runBatch");
          numDocsTraversed = traverser.runBatch(
            hostLoadManager.determineBatchHint(connectorName));
          System.out.println("End runBatch");
          numConsecutiveFailures = 0;
          timeOfFirstFailure = 0;
        } catch (InterruptedException e) {
          // timeout causes interruption of execution
        }
      } else {
        failure();
      }
      isFinished = true;
      synchronized(this) {
        notifyAll();
      }
    }

    /**
     * Called when a failure occurs.
     */
    public void failure() {
      numConsecutiveFailures++;
      if (1 == numConsecutiveFailures) {
        timeOfFirstFailure = System.currentTimeMillis();
      }
    }
  }
}
