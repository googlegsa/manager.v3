// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;

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
/* TODO (bmj): This should be removed and its functionality moved to
 * ConnectorCoordinator.  Connectors should be in charge of their own
 * scheduling.  In particular, the ConnectorCoordinator could leverage
 * Schedule.nextScheduledInterval(), sleeping when unscheduled rather
 * than checking all schedules once per second.
 */
public class TraversalScheduler implements Runnable {
  public static final String SCHEDULER_CURRENT_TIME = "/Scheduler/currentTime";

  private static final Logger LOGGER =
    Logger.getLogger(TraversalScheduler.class.getName());

  private final Instantiator instantiator;

  private boolean isInitialized; // Protected by instance lock.
  private boolean isShutdown; // Protected by instance lock.

  /**
   * Create a scheduler object.
   *
   * @param instantiator used to get schedule for connector instances
   */
  public TraversalScheduler(Instantiator instantiator) {
    this.instantiator = instantiator;
    this.isInitialized = false;
    this.isShutdown = false;
  }

  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    isShutdown = false;
    new Thread(this, "TraversalScheduler").start();
  }

  public synchronized void shutdown() {
    if (isShutdown) {
      return;
    }
    isInitialized = false;
    isShutdown = true;
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
        instantiator.startBatch(connectorName);
      } catch (ConnectorNotFoundException e) {
        // Looks like the connector just got deleted.  Don't schedule it.
      } finally {
        NDC.pop();
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
