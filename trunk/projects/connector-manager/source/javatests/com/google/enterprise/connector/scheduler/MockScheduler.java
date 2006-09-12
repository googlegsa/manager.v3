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
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.monitor.Monitor;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.ArrayList;
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
  
  private HostLoadManager hostLoadManager;
  
  private boolean isInitialized;
  private boolean isShutdown;
  
  public MockScheduler(Instantiator instantiator, Monitor monitor, 
      WorkQueue workQueue) {
    this.instantiator = instantiator;
    this.monitor = monitor;
    this.workQueue = workQueue;
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
  
  public synchronized void shutdown() {
    if (isShutdown) {
      return;
    }
    workQueue.shutdown();
    isInitialized = false;
    isShutdown = true;
  }
  
  /**
   * Return a hardcoded schedule.
   * @return a hardcoded schedule.
   */
  private List getSchedules() {
    List schedules = new ArrayList();
    List intervals = new ArrayList();
    intervals.add(new ScheduleTimeInterval(
      new ScheduleTime(0),
      new ScheduleTime(0)));
    Schedule schedule1 = 
      new Schedule(MockInstantiator.TRAVERSER_NAME1, intervals);
    Schedule schedule2 =
      new Schedule(MockInstantiator.TRAVERSER_NAME2, intervals);
    schedules.add(schedule1);
    schedules.add(schedule2);
    return schedules;
  }

  private Traverser getTraverser(String connectorName) {
    Traverser traverser = null;
    try {
      traverser = instantiator.getTraverser(connectorName); 
    } catch (ConnectorNotFoundException cnfe) {
      cnfe.printStackTrace();
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
  
  private class TraverserRunnable implements Runnable {
    private String connectorName;
    private int numDocsTraversed;
    private boolean isFinished;
    
    public TraverserRunnable(String connectorName) {
      this.connectorName = connectorName;
      this.numDocsTraversed = 0;
      this.isFinished = false;
    }
    
    public synchronized int getNumDocsTraversed() {
      while (!isFinished) {
        try {
          wait();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      return numDocsTraversed;
    }

    public synchronized void run() {
      Traverser traverser = getTraverser(connectorName);
      try {
        System.out.println("Begin runBatch");
        numDocsTraversed = 
          traverser.runBatch(hostLoadManager.determineBatchHint(connectorName));
        System.out.println("End runBatch");
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      isFinished = true;
      notifyAll();
    }
  }
  
  public synchronized void run() {
    while (isInitialized && !isShutdown) {
      List schedules = getSchedules();
      for (Iterator iter = schedules.iterator(); iter.hasNext(); ){
        Schedule schedule = (Schedule) iter.next();
        if (shouldRun(schedule)) {
          String connectorName = schedule.getConnectorName();
          TraverserRunnable runnable = new TraverserRunnable(connectorName);
          System.out.println("Adding work to workQueue.");
          workQueue.addWork(runnable);
          hostLoadManager.updateNumDocsTraversed(connectorName, 
            runnable.getNumDocsTraversed());
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
}
