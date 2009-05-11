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

import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.common.WorkQueueItem;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.monitor.Monitor;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
  private HostLoadManager hostLoadManager;

  private boolean isInitialized;  // Protected by instance lock.
  private boolean isShutdown;     // Protected by instance lock.

  // Map of runnables that are currently scheduled.  Protected by instance lock.
  private Map<String, WorkQueueItem> runnables;

  // Set of connectors removed.  Needed so that we only schedule connectors
  // that haven't been removed.  Protected by instance lock.
  private Set<String> removedConnectors;

  /**
   * Create a scheduler object.
   *
   * @param instantiator
   * @param monitor
   * @param workQueue
   */
  public TraversalScheduler(Instantiator instantiator, Monitor monitor,
      WorkQueue workQueue) {
    this.instantiator = instantiator;
    this.monitor = monitor;
    this.workQueue = workQueue;
    this.isInitialized = false;
    this.isShutdown = false;
    this.hostLoadManager = new HostLoadManager(instantiator);
    this.runnables = new HashMap<String, WorkQueueItem>();
    this.removedConnectors = new HashSet<String>();
  }

  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    workQueue.init();
    isInitialized = true;
    isShutdown = false;
    new Thread(this, "TraversalScheduler").start();
  }

  public synchronized void shutdown(boolean interrupt, long timeoutInMillis) {
    LOGGER.info("Shutdown initiated...");
    if (isShutdown) {
      return;
    }
    workQueue.shutdown(interrupt, timeoutInMillis);
    instantiator.shutdown();
    isInitialized = false;
    isShutdown = true;
  }

  /**
   * Return a traversal schedule as store in the configuration and schedule
   * stores.  As a side effect, clear the removedConnectors Set.
   *
   * @return a schedule of traversal work
   */
  private synchronized List<Schedule> getSchedules() {
    List<Schedule> schedules = new ArrayList<Schedule>();
    for (String connectorName : instantiator.getConnectorNames()) {
      if (removedConnectors.contains(connectorName)) {
        continue;
      }
      String scheduleStr = null;
      try {
        scheduleStr = instantiator.getConnectorSchedule(connectorName);
      } catch (ConnectorNotFoundException e) {
        // Looks like the connector just got deleted.  Don't schedule it.
        continue;
      }
      if (scheduleStr != null) {
        Schedule schedule = new Schedule(scheduleStr);
        if (!schedule.isDisabled()) {
          schedules.add(schedule);
        }
      }
    }
    removedConnectors.clear();
    return schedules;
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
   * Determines whether scheduler should run.  Assumes caller holds instance
   * lock.
   *
   * @return true if we are in a running state and scheduler should run or
   *         continue running.
   */
  private synchronized boolean isRunningState() {
    return isInitialized && !isShutdown;
  }

  /**
   * Call this method when a connector is removed.  Assumes ScheduleStore has
   * already been updated to reflect the schedule change.  This causes the
   * scheduler to gracefully interrupt any work that is done on this connector.
   *
   * @param connectorName name of the connector instance
   */
  public void removeConnector(String connectorName) {
    synchronized (this) {
      // Let scheduler know not to schedule more work for this connector.
      removedConnectors.add(connectorName);

      // Interrupt any work that is already getting done.
      TraversalWorkQueueItem runnable =
        (TraversalWorkQueueItem) runnables.remove(connectorName);
      if (null != runnable) {
        workQueue.cancelWork(runnable);
      }

      // Tell the load manager to forget about this connector.
      hostLoadManager.removeConnector(connectorName);
    }
  }

  public void run() {
    NDC.push("Traverse Scheduler");
    try {
      while (isRunningState()) {
        try {
          for (Schedule schedule : getSchedules()) {
            if (!isRunningState()) {
              break;
            }
            NDC.push("Traverse " + schedule.getConnectorName());
            try {
              runSchedule(schedule);
            } finally {
              NDC.pop();
            }
          }
          if (!isRunningState()) {
            break;
          }

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
      LOGGER.info("TraversalScheduler thread is stopping due to "
                  + "shutdown or not being initialized.");
    } finally {
      NDC.remove();
    }
  }

  private void runSchedule(Schedule schedule) {
    if (shouldRun(schedule)) {
      String connectorName = schedule.getConnectorName();
      TraversalWorkQueueItem runnable;
      synchronized (this) {
        runnable = (TraversalWorkQueueItem) runnables.get(connectorName);
        if (null == runnable) {
          runnable = new TraversalWorkQueueItem(connectorName);
          runnables.put(connectorName, runnable);
        }
      }
      if (!isRunningState()) {
        return;
      }
      // Check if the item is already running here to avoid deadlock.
      if (!runnable.isFinished()) {
        // LOGGER.finer("Traversal work for connector "
        //              + connectorName + " is still running.");
        return;
      }
      synchronized (this) {
        if (removedConnectors.contains(connectorName)) {
          LOGGER.info("Connector was removed so no work for it will "
                      + "be done: " + connectorName);
          return;
        }

        // In the case where the work queue item is being reused need
        // to reset the finished status so the number of documents is
        // reported after it finishes.
        runnable.setFinished(false);
        workQueue.addWork(runnable);
      }
    }
  }

  private class TraversalWorkQueueItem extends WorkQueueItem {
    private String connectorName;
    private int numDocsTraversed;
    private boolean finished;
    private Traverser traverser;

    public TraversalWorkQueueItem(String connectorName) {
      this.connectorName = connectorName;
      this.numDocsTraversed = 0;
      this.finished = true;
      this.traverser = null;
    }

    public synchronized void setFinished(boolean isFinished) {
      this.finished = isFinished;
    }

    public synchronized boolean isFinished() {
      return this.finished;
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

    @Override
    public void doWork() {
      NDC.push("Traverse " + connectorName);
      int batchHint = hostLoadManager.determineBatchHint(connectorName);
      int batchDone = Traverser.ERROR_WAIT;
      try {
        traverser = getTraverser();
        if (null != traverser) {
          int numDocs = 0;
          if (batchHint > 0) {
            LOGGER.finest("Begin runBatch; batchHint = " + batchHint);
            batchDone = traverser.runBatch(batchHint);
            numDocs = (batchDone < 0) ? 0 : batchDone;
            if (isFinished()) {
              LOGGER.finest("End runBatch; Batch was canceled.");
              return;      // If we got canceled, bail out.
            }
            LOGGER.finest("End runBatch; batchDone = " + batchDone);
          }
          if (numDocs > 0) {
            hostLoadManager.updateNumDocsTraversed(connectorName, numDocs);
          }
        }
        if (batchDone < 0 && batchHint > 0) {
          try {
            Schedule schedule =
                new Schedule(instantiator.getConnectorSchedule(connectorName));
            if (batchDone == Traverser.POLLING_WAIT) {
              // If we have reached the end of the ECM content, and we are
              // not polling for new content, then disable this schedule.
              hostLoadManager.connectorFinishedTraversal(connectorName,
                  schedule.getRetryDelayMillis());
              if (schedule.getRetryDelayMillis() == Schedule.POLLING_DISABLED) {
                schedule.setDisabled(true);
                instantiator.setConnectorSchedule(connectorName,
                    schedule.toString());
                LOGGER.info("Traversal complete. Automatically pausing "
                  + "traversal for connector " + connectorName);
              }
            } else if (batchDone == Traverser.ERROR_WAIT) {
              hostLoadManager.connectorFinishedTraversal(connectorName,
                  Traverser.ERROR_WAIT_MILLIS);
            } else {
              hostLoadManager.connectorFinishedTraversal(connectorName,
                  schedule.getRetryDelayMillis());
            }
          } catch (ConnectorNotFoundException cnfe) {
            // Connector was apparently deleted.  Not a problem.
          }
        }
      } finally {
        synchronized(this) {
          traverser = null;
          finished = true;
          NDC.pop();
          notifyAll();
        }
      }
    }

    @Override
    public synchronized void cancelWork() {
      if (traverser != null) {
        traverser.cancelBatch();
        traverser = null;
      }
      setFinished(true);
      notifyAll();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("TraversalWorkQueueItem[");
      sb.append("this=" + hashCode());
      sb.append(";connectorName=" + connectorName);
      sb.append(";traverser=" + traverser);
      sb.append(";numDocsTraversed=" + numDocsTraversed);
      sb.append(";isFinished=" + finished);
      sb.append("]");
      return sb.toString();
    }
  }
}
