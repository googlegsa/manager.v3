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
import com.google.enterprise.connector.persist.ConnectorScheduleStore;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler that schedules connector traversal.  This class is thread safe.  
 * Must initialize TraversalScheduler before running it.
 */
public class TraversalScheduler implements Scheduler {
  public static final String SCHEDULER_CURRENT_TIME = "/Scheduler/currentTime";
  
  private static final Logger LOGGER = 
    Logger.getLogger(TraversalScheduler.class.getName());
  
  private Instantiator instantiator;
  private Monitor monitor;
  private WorkQueue workQueue;
  private ConnectorScheduleStore scheduleStore;
  
  private HostLoadManager hostLoadManager;
  
  private boolean isInitialized;  // Protected by instance lock.
  private boolean isShutdown;  // Protected by instance lock.
  
  // Map of runnables that are currently scheduled.  Protected by instance lock.
  private Map runnables = new HashMap();  // <connectorName, TraverserRunnable>
  // Set of connectors removed.  Needed so that we only schedule connectors
  // that haven't been removed.  Protected by instance lock.
  private Set removedConnectors;
  
  /**
   * Create a scheduler object.
   * @param instantiator
   * @param monitor
   * @param workQueue
   * @param scheduleStore
   */
  public TraversalScheduler(Instantiator instantiator, Monitor monitor, 
      WorkQueue workQueue, ConnectorScheduleStore scheduleStore) {
    this.instantiator = instantiator;
    this.monitor = monitor;
    this.workQueue = workQueue;
    this.scheduleStore = scheduleStore;
    this.hostLoadManager = new HostLoadManager(scheduleStore);
    this.isInitialized = false;
    this.isShutdown = false;
    this.removedConnectors = new HashSet();
  }
  
  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    workQueue.init();
    isInitialized = true;
    isShutdown = false;
  }
  
  public synchronized void shutdown(boolean interrupt, long timeoutInMillis) {
    LOGGER.info("Shutdown initiated...");
    if (isShutdown) {
      return;
    }
    workQueue.shutdown(interrupt, timeoutInMillis);
    isInitialized = false;
    isShutdown = true;
  }
  
  /**
   * Return a traversal schedule as store in the configuration and schedule
   * stores.  As a side effect, clear the removedConnectors Set.
   * @return a schedule of traversal work
   */
  private synchronized List getSchedules() {
    List schedules = new ArrayList();
    Iterator iter = instantiator.getConnectorNames();
    while (iter.hasNext()) {
      String connectorName = (String) iter.next();
      if (removedConnectors.contains(connectorName)) {
        continue;
      }
      String scheduleStr = scheduleStore.getConnectorSchedule(connectorName);
      if (null == scheduleStr) {
        LOGGER.log(Level.WARNING, "Could not find schedule for connector: " +
          connectorName);
        continue;
      }
      Schedule schedule = new Schedule(scheduleStr);
      schedules.add(schedule);
    }
    removedConnectors.clear();
    return schedules;
  }

  private void updateMonitor() {
    // TODO: change this when we figure out what we really want to monitor
    Map vars = new HashMap();
    vars.put(SCHEDULER_CURRENT_TIME, new Date());
    monitor.setVariables(vars);
  }

  private boolean shouldRun(Schedule schedule) {
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    List timeIntervals = schedule.getTimeIntervals();
    for (Iterator iter = timeIntervals.iterator(); iter.hasNext(); ) {
      ScheduleTimeInterval interval = (ScheduleTimeInterval) iter.next();
      int startHour = interval.getStartTime().getHour();
      int endHour = interval.getEndTime().getHour();
      if (0 == endHour) {
        endHour = 24;
      }
      if ((hour >= startHour) && (hour < endHour)) {
        if (hostLoadManager.shouldDelay(schedule.getConnectorName())) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Determines whether scheduler should run.  Assumes caller holds instance 
   * lock.
   * @return true if we are in a running state and scheduler should run or
   * continue running
   */
  private boolean isRunningState() {
    return isInitialized && !isShutdown;
  }

  /**
   * Call this method when a connector is removed.  Assumes ScheduleStore has 
   * already been updated to reflect the schedule change.  This causes the 
   * scheduler to gracefully interrupt any work that is done on this connector.
   * @param connectorName name of the connector instance
   */
  public void removeConnector(String connectorName) {
    synchronized (this) {
      // let scheduler know not to schedule more work for this connector
      removedConnectors.add(connectorName);
      
      // interrupt any work that is already getting done
      TraversalWorkQueueItem runnable = 
        (TraversalWorkQueueItem) runnables.remove(connectorName);
      if (null != runnable) {
        workQueue.cancelWork(runnable);
      }
    }    
  }
  
  public void run() {
    while (true) {
      try {
        synchronized (this) {
          if (!isRunningState()) {
            LOGGER.info("TraversalScheduler thread is stopping due to " 
              + "shutdown or not being initialized.");
            return;
          }
        }
        List schedules = getSchedules();
        for (Iterator iter = schedules.iterator(); iter.hasNext(); ){
          Schedule schedule = (Schedule) iter.next();
          if (shouldRun(schedule)) {
            String connectorName = schedule.getConnectorName();
            TraversalWorkQueueItem runnable;
            synchronized (this) {
              runnable = (TraversalWorkQueueItem) runnables.get(connectorName);
              if (null == runnable) {
                runnable = 
                  new TraversalWorkQueueItem(connectorName);
                runnables.put(connectorName, runnable);
              }
            }
            // we back off if we have received previous failures (e.g. when trying
            // to get a Traverser object)
            if (runnable.getNumConsecutiveFailures() >= 2) {
              long backoff = 
                1000 * (long) Math.pow(2, runnable.getNumConsecutiveFailures());
              long now = System.currentTimeMillis();
              if (runnable.getTimeOfFirstFailure() + backoff > now) {
                continue;
              }
            }
            LOGGER.finer("Trying to add traversal work to workQueue: "
              + connectorName);
            synchronized (this) {
              if (!isRunningState()) {
                  LOGGER.info("TraversalScheduler thread is stopping due to " 
                    + "shutdown or not being initialized.");
                  return;
              }
              if (removedConnectors.contains(connectorName)) {
                LOGGER.info("Connector was removed so no work for it will be " 
                  + "done: " + connectorName);
                continue;
              }
              // In the case where the work queue item is being reused need to
              // reset the finished status so the number of documents is reported
              // after it finishes.
              runnable.setIsFinished(false);
              workQueue.addWork(runnable);
            }
            int numDocsTraversed = runnable.getNumDocsTraversed();
            if (numDocsTraversed > 0) {
              hostLoadManager.updateNumDocsTraversed(connectorName, 
                numDocsTraversed);
            } else if (numDocsTraversed == 0 && runnable.getBatchHint() > 0) {
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
          synchronized (this) {
            wait(1000);
          }
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } catch (Throwable t) {
        t.printStackTrace();
        LOGGER.log(Level.SEVERE, 
          "TraversalScheduler caught unexpected Throwable: ", t);
      }
    }
  }
  
  private class TraversalWorkQueueItem extends WorkQueueItem {
    private String connectorName;
    private int numDocsTraversed;
    private boolean isFinished;
    private int batchHint = 0;
    
    private int numConsecutiveFailures;
    // the time in millis of the first consecutive failure given the 
    // numConsecutiveFailures is > 0.
    private long timeOfFirstFailure;
        
    public TraversalWorkQueueItem(String connectorName) {
      this.connectorName = connectorName;
      this.numDocsTraversed = 0;
      this.isFinished = false;
      this.numConsecutiveFailures = 0;
      this.timeOfFirstFailure = 0;
    }

    public void setIsFinished(boolean isFinished) {
      this.isFinished = isFinished;
    }

    public int getBatchHint() {
      return batchHint;
    }

    public int getNumDocsTraversed() {
      waitTillFinishedOrTimeout();
      return numDocsTraversed;
    }

    private Traverser getTraverser() {
      Traverser traverser = null;
      try {
        traverser = instantiator.getTraverser(connectorName); 
      } catch (ConnectorNotFoundException cnfe) {
        cnfe.printStackTrace();
      } catch (InstantiatorException ie) {
        ie.printStackTrace();
      }
      return traverser;
    }

    private long getTraversalTimeout(Traverser traverser) {
      return (null == traverser) ? 0 : traverser.getTimeoutMillis();
    }

    /**
     * Wait until the run is finished.  Object lock must be held to call this 
     * method.
     */
    private void waitTillFinishedOrTimeout() {
      if (!isFinished) {
        try {
          synchronized(this) {
            long timeout = getTraversalTimeout(getTraverser());
            LOGGER.log(Level.FINEST, "Beginning wait (timeout=" + timeout + ")...");
            wait(timeout);
            LOGGER.log(Level.FINEST, "...ending wait");
          }
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          LOGGER.log(Level.FINEST, "Interrupted");
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
      batchHint = hostLoadManager.determineBatchHint(connectorName);
      Traverser traverser = getTraverser();
      if (null != traverser) {
        if (batchHint > 0) {
          LOGGER.log(Level.FINEST, "Begin runBatch; batchHint=" + batchHint);
          numDocsTraversed = traverser.runBatch(batchHint);
          LOGGER.log(Level.FINEST, "End runBatch");
        } else {
          numDocsTraversed = 0;
        }
        numConsecutiveFailures = 0;
        timeOfFirstFailure = 0;
      }
      isFinished = true;
      if (numDocsTraversed == 0 && batchHint > 0) {
        hostLoadManager.connectorFinishedTraversal(connectorName);
      }
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

    public String toString() {
      StringBuffer sb = new StringBuffer();
      Traverser traverser = getTraverser();
      long traversalTimeout = getTraversalTimeout(traverser);
      sb.append("TraversalWorkQueueItem[");
      sb.append("this=" + hashCode());
      sb.append(";connectorName=" + connectorName);
      sb.append(";traverser=" + traverser);
      sb.append(";numDocsTraversed=" + numDocsTraversed);
      sb.append(";isFinished=" + isFinished);
      sb.append(";numConsecutiveFailures=" + numConsecutiveFailures);
      sb.append(";timeOfFirstFailure=" + timeOfFirstFailure);
      sb.append(";traversalTimeout=" + traversalTimeout);
      sb.append("]");
      return (new String(sb));
    }
  }
}
