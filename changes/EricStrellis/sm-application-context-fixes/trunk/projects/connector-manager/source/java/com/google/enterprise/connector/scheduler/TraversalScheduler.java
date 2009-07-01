// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.instantiator.BatchResultRecorder;
import com.google.enterprise.connector.instantiator.ConnectorCoordinator;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.monitor.Monitor;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler that schedules connector traversal.  This class is thread safe.
 * Must initialize TraversalScheduler before running it.
 *
 * <p> This facility includes a schedule thread that runs a loop.
 * Each iteration it asks the instantiator for the schedule
 * for each Connector Instance and runs batches for those that
 * are
 * <OL>
 * <LI> scheduled to run.
 * <LI> have not exhausted their quota for the current time interval.
 * <LI> are not currently running.
 * </OL>
 * The implementation must handle the situation that a Connector
 * Instance is running.
 */
public class TraversalScheduler implements Runnable {
  public static final String SCHEDULER_CURRENT_TIME = "/Scheduler/currentTime";

  private static final Logger LOGGER =
    Logger.getLogger(TraversalScheduler.class.getName());

  private final Instantiator instantiator;
  private final Monitor monitor;
  private final HostLoadManager hostLoadManager;

  private boolean isInitialized; // Protected by instance lock.
  private boolean isShutdown; // Protected by instance lock.

  /**
   * Create a scheduler object.
   *
   * @param instantiator
   * @param monitor
   */
  public TraversalScheduler(Instantiator instantiator, Monitor monitor) {
    this.instantiator = instantiator;
    this.monitor = monitor;
    this.isInitialized = false;
    this.isShutdown = false;
    this.hostLoadManager = new HostLoadManager(instantiator);
  }

  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    isShutdown = false;
    new Thread(this, "TraversalScheduler").start();
  }

  public synchronized void shutdown(boolean interrupt, long timeoutInMillis) {
    LOGGER.info("Shutdown initiated...");
    if (isShutdown) {
      return;
    }
    instantiator.shutdown(interrupt, timeoutInMillis);
    isInitialized = false;
    isShutdown = true;
  }

  private void updateMonitor() {
    // TODO: Change this when we figure out what we really want to monitor.
    Map<String, Date> vars = new HashMap<String, Date>();
    vars.put(SCHEDULER_CURRENT_TIME, new Date());
    monitor.setVariables(vars);
  }

  private boolean shouldRun(Schedule schedule) {
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    for (ScheduleTimeInterval interval : schedule.getTimeIntervals()) {
      int startHour = interval.getStartTime().getHour();
      int endHour = interval.getEndTime().getHour();
      if (0 == endHour) {
        endHour = 24;
      }
      if ((hour >= startHour) && (hour < endHour)) {
        return !hostLoadManager.shouldDelay(schedule.getConnectorName());
      }
    }
    return false;
  }

  /**
   * Determines whether scheduler should run.
   *
   * @return true if we are in a running state and scheduler should run or
   *         continue running.
   */
  private synchronized boolean isRunningState() {
    return isInitialized && !isShutdown;
  }

  private void scheduleBatches() {
    for (String connectorName : instantiator.getConnectorNames()) {
      NDC.pushAppend(connectorName);
      try {
        scheduleABatch(connectorName);
      } finally {
        NDC.pop();
      }
    }
  }

  private void scheduleABatch(String connectorName) {
    String scheduleStr = null;
    try {
      scheduleStr = instantiator.getConnectorSchedule(connectorName);
    } catch (ConnectorNotFoundException e) {
      // Looks like the connector just got deleted.  Don't schedule it.
      return;
    }
    Schedule schedule = new Schedule(scheduleStr);
    if (schedule.isDisabled()) {
      return;
    }
    if (shouldRun(schedule)) {
      int batchHint = hostLoadManager.determineBatchHint(connectorName);
      if (batchHint <= 0) {
        return;
      }
      try {
        ConnectorCoordinator coordinator =
          instantiator.getConnectorCoordinator(connectorName);
        BatchResultRecorder resultRecorder =
          new TraversalBatchResultRecorder(schedule, hostLoadManager,
              coordinator);
        coordinator.startBatch(resultRecorder, batchHint);
      } catch (ConnectorNotFoundException cnfe) {
        LOGGER.log(Level.WARNING, "Connector not found - this is normal if you "
            + " recently reconfigured your connector instance.", cnfe);
      }
    }
  }

  public void run() {
    NDC.push("Traverse");
    try {
      while (true) {
        try {
          if (!isRunningState()) {
            LOGGER.info("TraversalScheduler thread is stopping due to "
                + "shutdown or not being initialized.");
            return;
          }
          scheduleBatches();
          updateMonitor();
          // Give someone else a chance to run.
          try {
            synchronized (this) {
              wait(1000);
            }
          } catch (InterruptedException e) {
            // May have been interrupted for shutdown.
          }
        } catch (Throwable t) {
          LOGGER.log(Level.SEVERE,
              "TraversalScheduler caught unexpected Throwable: ", t);
        }
      }
    } finally {
      NDC.remove();
    }
  }
}
