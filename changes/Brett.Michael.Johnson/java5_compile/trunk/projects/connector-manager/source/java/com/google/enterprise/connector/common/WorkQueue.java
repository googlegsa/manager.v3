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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The WorkQueue provides a way for callers to issue non-blocking work
 * requests.  This class is multi-thread safe.
 */
public class WorkQueue {
  /**
   *  The default amount of time in ms to wait before shutting down system.
   */
  public static final int DEFAULT_SHUTDOWN_TIMEOUT = 10 * 1000;

  private static final Logger LOGGER =
    Logger.getLogger(WorkQueue.class.getName());

  // possible value for nextAbsTimeout
  private static final long NO_TIMEOUT = 0;

  // relative timeout that tells us when we should just kill a WorkQueueThread
  // instead of just trying to interrupt it
  private long killThreadTimeout = 5 * 60 * 1000;

  /*
   * Variables that are protected by instance lock
   */
  private LinkedList <WorkQueueItem> workQueue;    // Queue used for added work.
  private Map <WorkQueueItem, Long> absTimeoutMap; // Map of expected timeouts.
  private long nextAbsTimeout;  // next earliest absolute timeout
  private boolean isInitialized;
  private boolean shutdown;  // true when shutdown is initiated

  private int numThreads;
  // access is protected by "threads" instance lock
  private Set <WorkQueueThread> threads;

  private InterrupterThread interrupterThread;
  private LifeThread lifeThread;

  /**
   * Creates a WorkQueue with a default 60 second timeout before a thread is
   * killed after it is interrupted.
   * @param numThreads number of threads servicing work queue
   */
  public WorkQueue(int numThreads) {
    this(numThreads, 60 * 1000);
  }

  /**
   * Creates a WorkQueue with a given number of worker threads.
   * @param numThreads the number of threads to execute work on the WorkQueue.
   * This number should be at least 1.
   * @param killThreadTimeout
   */
  public WorkQueue(int numThreads, long killThreadTimeout) {
    if (numThreads <= 0) {
      throw new IllegalArgumentException("numThreads must be > 0");
    }
    this.killThreadTimeout = killThreadTimeout;
    this.workQueue = new LinkedList<WorkQueueItem>();
    this.absTimeoutMap = new HashMap<WorkQueueItem, Long>();
    this.nextAbsTimeout = NO_TIMEOUT;
    this.isInitialized = false;
    this.shutdown = false;
    this.numThreads = numThreads;
    this.threads = new HashSet<WorkQueueThread>();
  }

  /**
   * Creates a WorkQueue with the given specification and also sets the timeout
   * of the WorkQueueItem.
   *
   * @param numThreads the number of threads to execute work on the WorkQueue.
   * This number should be at least 1.
   * @param killThreadTimeout the additional time in milliseconds given over the
   * {@code workItemTimeout} before the {@code WorkQueueThread} is killed rather
   * than just interrupted.
   * @param workItemTimeout time in milliseconds that each {@code WorkQueueItem}
   * is given before it is interrupted.
   */
  public WorkQueue(int numThreads, long killThreadTimeout, long workItemTimeout)
  {
    this(numThreads, killThreadTimeout);
    WorkQueueItem.timeout = workItemTimeout;
  }

  /**
   * Initialize work queue by starting worker threads.
   */
  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    interrupterThread = new InterrupterThread("InterrupterThread");
    interrupterThread.start();
    isInitialized = true;
    shutdown = false;

    // start WorkQueueThreads after initialization since they depend on the
    // WorkQueue
    synchronized (threads) {
      for (int i = 0; i < numThreads; i++) {
        threads.add(createAndStartWorkQueueThread("WorkQueueThread-" + i));
      }
    }
    lifeThread = new LifeThread("LifeThread");
    lifeThread.start();
  }

  /**
   * Create WorkQueueThread and start it.
   * @param name name of the thread
   */
  private WorkQueueThread createAndStartWorkQueueThread(String name) {
    WorkQueueThread thread = new WorkQueueThread(name, this);
    thread.start();
    return thread;
  }

  /**
   * Shutdown the work queue after waiting for 10 seconds.
   *
   * @param interrupt if true, interrupt threads.
   */
  public synchronized void shutdown(boolean interrupt) {
    shutdown(interrupt, DEFAULT_SHUTDOWN_TIMEOUT);
  }

  /**
   * Shutdown the work queue.
   *
   * @param interrupt if true, interrupt threads
   * @param timeoutInMillis wait at least this timeout for threads to complete
   * before just returning
   */
  public synchronized void shutdown(boolean interrupt, long timeoutInMillis) {
    shutdown = true;
    try {
      lifeThread.shutdown();
      lifeThread.join();
    } catch (InterruptedException e) {
      LOGGER.log(Level.WARNING,
          "Interrupted Exception while waiting for lifeThread: ", e);
    }
    if (isInitialized) {
      if (interrupt) {
        synchronized (threads) {
          Iterator <WorkQueueThread> iter = threads.iterator();
          while (iter.hasNext()) {
            iter.next().interruptAndKill();
          }
        }
        try {
          Thread.sleep(timeoutInMillis);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          LOGGER.log(Level.WARNING, "Interrupted Exception: ", e);
        }
      } else {
        long baseTime = System.currentTimeMillis();
        while (baseTime + timeoutInMillis < System.currentTimeMillis()) {
          if (isAnyThreadWorking()) {
            try {
              Thread.sleep(200);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              LOGGER.log(Level.WARNING, "Interrupted Exception: ", e);
            }
          } else {
            // no threads are still working so break out of loop
            break;
          }
        }
      }
      interrupterThread.shutdown();
      workQueue.clear();
      isInitialized = false;
    }
  }

  /**
   * Determine whether any WorkQueue thread is working.
   * @return true if any thread in the WorkQueue is doing work.
   */
  private boolean isAnyThreadWorking() {
    synchronized (threads) {
      Iterator <WorkQueueThread> iter = threads.iterator();
      while (iter.hasNext()) {
        if (iter.next().isWorking()) {
          return true;
        }
      }
      return false;
    }
  }

  private synchronized long getNextAbsoluteTimeout() {
    return nextAbsTimeout;
  }

  /**
   * Interrupts all WorkQueueItems that have timed out.
   */
  private synchronized void interruptAllTimedOutItems() {
    long now;
    // determine which work threads should be interrupted as well as
    // set nextAbsTimeout
    now = System.currentTimeMillis();
    nextAbsTimeout = NO_TIMEOUT;
    Iterator <Map.Entry <WorkQueueItem, Long>> iter;
    for (iter = absTimeoutMap.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry <WorkQueueItem, Long> entry = iter.next();
      Long absTimeout = entry.getValue();
      if (absTimeout.longValue() <= now) {  // we have a timeout
        LOGGER.log(Level.FINEST, "A WorkItem timeout has occured...");
        WorkQueueItem item = entry.getKey();
        // if the wait is too long, then we want to replace the thread
        if (absTimeout.longValue() + killThreadTimeout <= now) {
          LOGGER.log(Level.FINEST, "...replacing thread");
          iter.remove();
          replaceHangingThread(item);
          item.getWorkQueueThread().interruptAndKill();
        } else {
          LOGGER.log(Level.FINEST, "...interrupting thread");
          item.getWorkQueueThread().interrupt();
        }
      } else {  // we have a thread that will timeout later
        LOGGER.log(Level.FINEST, "...waiting until later");
        if ((NO_TIMEOUT == getNextAbsoluteTimeout())
            || (getNextAbsoluteTimeout() > absTimeout.longValue())) {
          nextAbsTimeout = absTimeout.longValue();
        }
      }
    }
  }

  /**
   * Given that a particular work item is hanging and isn't responding to
   * an interrupt, we want to be able to reclaim the thread that is hung
   * executing that work.  We do so by spawning a new thread.  Caller must
   * hold instance lock.
   * @param item the item that is causing the hang
   */
  private void replaceHangingThread(WorkQueueItem item) {
    // replace hanging thread with new thread if timeout is too long
    WorkQueueThread thread =
      new WorkQueueThread(item.getWorkQueueThread().getName(), this);
    synchronized (threads) {
      threads.remove(item.getWorkQueueThread());
      LOGGER.log(Level.WARNING, "Replacing work queue thread: "
        + item.getWorkQueueThread().getName());
      threads.add(thread);
    }
    thread.start();
  }

  /**
   * Work to do right before executing the item.
   * @param item
   */
  void preWork(WorkQueueItem item) {
    Long absTimeout =
      new Long(item.getTimeout() + System.currentTimeMillis());
    synchronized(this) {
      absTimeoutMap.put(item, absTimeout);
    }
    synchronized(interrupterThread) {
      interrupterThread.notifyAll();
    }
  }

  /**
   * Work to do right after executing an item.
   * @param item
   */
  void postWork(WorkQueueItem item) {
    synchronized(this) {
      absTimeoutMap.remove(item);
    }
  }

  /**
   * Add a piece of work that will be executed at a later time.
   * @param work one piece of work
   */
  public synchronized void addWork(WorkQueueItem work) {
    if (!isInitialized) {
      throw new IllegalStateException(
        "Must init() WorkQueue object before adding work.");
    }
    if (shutdown) {
      // don't add more work if we are shutting down
      return;
    }
    LOGGER.log(Level.FINEST, "Adding work: " + work);
    workQueue.addLast(work);
    notifyAll();

    // wake up interrupter thread.  this code is not strictly necessary but
    // ensures that work item properly gets timed out
    synchronized (interrupterThread) {
      interrupterThread.interrupt();
    }
  }

  /**
   * Cancel a piece of work by interrupting it.
   * @param work the piece of work to be updated.
   */
  public synchronized void cancelWork(WorkQueueItem work) {
    if (!isInitialized) {
      throw new IllegalStateException(
        "Must init() WorkQueue object before canceling work.");
    }
    if (shutdown) {
      // if we're shutting down it doesn't matter since work will finish
      return;
    }

    if (workQueue.remove(work)) {
      // If the work is in the queue, it means no thread is operating on it
      // so we simply finish.
      LOGGER.info("Cancelling work by removing unstarted work from work"
        + " queue.");
    } else {
      // See if a thread is working on this item.
      LOGGER.info("Cancelling work by interrupting the worker thread.");
      Thread thread = work.getWorkQueueThread();
      thread.interrupt();
    }
  }

  /**
   * Remove a piece of work.
   * @return the work item
   */
  WorkQueueItem removeWork() {
    WorkQueueItem item = (WorkQueueItem) workQueue.removeFirst();
    LOGGER.log(Level.FINEST, "Removing work: " + item);
    return item;
  }

  /**
   * Determine the number of pieces of work that are in the queue.
   * @return the number of pieces of work in the queue.
   */
  public synchronized int getWorkCount() {
    if (!isInitialized) {
      return 0;
    }
    return workQueue.size();
  }


  /**
   * Interrupts WorkQueueItemThread if it takes too long.
   */
  private class InterrupterThread extends Thread {

    public InterrupterThread(String name) {
      super(name);
    }

    public void shutdown() {
      synchronized (this) {
        notifyAll();
      }
    }

    public void run() {
      while (!shutdown) {
        long now = System.currentTimeMillis();
        try {
          if (getNextAbsoluteTimeout() == NO_TIMEOUT) {
            synchronized (this) {
              wait(killThreadTimeout);
            }
          } else {
            long timeout = getNextAbsoluteTimeout() - now;
            if (timeout > 0) {
              synchronized (this) {
                wait(timeout);
              }
            }
          }
        } catch (InterruptedException e) {
          // thread was signaled to determine whether there are new work items
          // to be interrupted (this is done under normal operation--e.g. when
          // we add a work item)
        }

        interruptAllTimedOutItems();
      }
    }
  }

  /**
   * Ensures that WorkQueue does not have any dead WorkQueueThreads.
   */
  private class LifeThread extends Thread {
    // Timeout in milliseconds.
    private static final int LIFE_THREAD_WAIT_TIMEOUT = 60 * 1000;

    public LifeThread(String name) {
      super(name);
    }

    public void shutdown() {
      synchronized (this) {
        notifyAll();
      }
    }

    public void run() {
      while (!shutdown) {
        try {
          Set <WorkQueueThread> replacementThreads =
              new HashSet<WorkQueueThread>();
          synchronized (threads) {
            Iterator <WorkQueueThread> iter = threads.iterator();
            while (iter.hasNext()) {
              WorkQueueThread thread = iter.next();
              if (!thread.isAlive()) {
                LOGGER.log(Level.WARNING, "WorkQueueThread was dead and is "
                    + "restarted by LifeThread: " + thread.getName());
                iter.remove();
                replacementThreads.add(
                    createAndStartWorkQueueThread(thread.getName()));
              }
            }
            threads.addAll(replacementThreads);
          }
          synchronized (this) {
            if (!shutdown) {
              wait(LIFE_THREAD_WAIT_TIMEOUT);
            }
          }
        } catch (InterruptedException e) {
          LOGGER.log(Level.WARNING, "", e);
        }
      }
    }
  }
}
