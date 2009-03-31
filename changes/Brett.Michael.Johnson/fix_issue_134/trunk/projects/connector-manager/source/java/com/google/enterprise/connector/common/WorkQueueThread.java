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

package com.google.enterprise.connector.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A WorkQueueThread executes work in the WorkQueue and calls the associated
 * callback method when the work is complete.
 */
public class WorkQueueThread extends Thread {
  private static final Logger LOGGER =
    Logger.getLogger(WorkQueueThread.class.getName());

  private WorkQueue workQueue;
  private boolean isWorking;
  private boolean exit; // true if thread should exit

  public WorkQueueThread(String name, WorkQueue workQueue) {
    super(name);
    this.workQueue = workQueue;
    isWorking = false;
    exit = false;
  }

  public boolean isWorking() {
    return isWorking;
  }

  public boolean isKilled() {
    return exit;
  }

  /**
   * Interrupt this Thread and have it exit.
   */
  public void interruptAndKill() {
    exit = true;
    interrupt();
  }

  public void run() {
    while (!exit) {
      try {
        WorkQueueItem item;
        synchronized (workQueue) {
          while (0 == workQueue.getWorkCount()) {
            try {
              workQueue.wait();
            } catch (InterruptedException ie) {
              if (exit) {
                // thread exits, for example, when shutdown of WorkQueue occurs
                LOGGER.log(Level.INFO,
                  "Interrupted WorkQueueThread is exiting due to interrupt " +
                  "and kill.");
                return;
              } else {
                // if we aren't killing this thread, we go back to wait for more
                // work
                LOGGER.log(Level.INFO, "Interrupted WorkQueueThread is fine so" +
                  " will continue to wait for work.");
                continue;
              }
            }
          }

          item = workQueue.removeWork();
        }

        item.setWorkQueueThread(this);
        try {
          workQueue.preWork(item);
          isWorking = true;
          item.doWork();
        } catch (RuntimeException e) {
          LOGGER.log(Level.WARNING, "WorkQueueThread work had problems: ", e);
          continue;
        } finally {
          isWorking = false;
          workQueue.postWork(item);
        }

      } catch (Throwable t) {
        LOGGER.log(Level.WARNING, "WorkQueueThread has problems: ", t);
      }
    }
  }
}
