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

package com.google.enterprise.connector.common;

import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * The WorkQueue provides a way for callers to issue non-blocking work
 * requests.
 */
public class WorkQueue {
  private static final Logger logger = 
    Logger.getLogger(WorkQueue.class.getName());
  
  // Queue used for added work.
  private LinkedList workQueue;
  
  private int numThreads;
  private Thread[] threads;
  
  /**
   * Creates a WorkQueue with a given number of worker threads.
   * @param numThreads the number of threads to execute work on the WorkQueue.
   * This number should be at least 1.
   */
  public WorkQueue(int numThreads) {
    if (numThreads <= 0) {
      throw new IllegalArgumentException("numThreads must be > 0");
    }
    this.workQueue = new LinkedList();
    this.numThreads = numThreads;
    this.threads = new Thread[this.numThreads];
    for (int i = 0 ; i < this.numThreads; i++) {
      threads[i] = new WorkQueueThread();
      threads[i].start();
    }
  }
  
  /**
   * Add a piece of work that will be executed.
   * @param work one piece of work
   */
  public void addWork(Runnable work) {
    synchronized (workQueue) {
      workQueue.addLast(work);
      workQueue.notifyAll();
    }
  }
  
  /**
   * Determine the number of pieces of work that are in the queue.
   */
  public int getWorkCount() {
    synchronized (workQueue) {
      return workQueue.size();
    }
  }
  
  /**
   * A WorkQueueThread executes work in the WorkQueue and calls the associated
   * callback method when the work is complete.
   */
  private class WorkQueueThread extends Thread {
    public void run() {
      while (true) {
        synchronized (workQueue) {
          while (workQueue.isEmpty()) {
            try {
              workQueue.wait();
            } catch (InterruptedException ie) {
              logger.warning("WorkQueueThread was interrupted: " 
                + ie.getMessage());
              continue;
            }
          }
          
          Runnable work = (Runnable) workQueue.removeFirst();
          try {
            work.run();
          } catch (RuntimeException e) {
            logger.warning("WorkQueueThread work had problems: " 
              + e.getMessage());
            continue;
          }         
        }
      }
    }
  }
}
