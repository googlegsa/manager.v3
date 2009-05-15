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

import com.google.enterprise.connector.logging.NDC;

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

  // Timeout in milliseconds to let a work item run before interrupting it.
  private long workItemTimeout = 20 * 60 * 1000;

  // How long to wait after interrupting an overdue work item thread
  // before killing it off.
  private long killThreadTimeout = 10 * 60 * 1000;

  /*
   * Variables that are protected by instance lock
   */
  private boolean isInitialized;  // true after one-time init done
  private boolean shutdown;       // true when shutdown is initiated

  private final LinkedList<WorkQueueItem> workQueue; // Queue used for added
                                                     // work.
  private final Map<WorkQueueItem, Long> absTimeoutMap; // Map of expected
                                                        // timeouts.

  private final int numThreads;
  // Access is protected by "threads" instance lock.
  private final Set<WorkQueueThread> threads;

  private InterrupterThread interrupterThread; // Looks for long running threads.
  private LifeThread lifeThread;               // Looks for hung threads.

  /**
   * Creates a WorkQueue with 10 service threads, a default 20 minute timeout
   * before interrupting a thread, and a default 10 minute timeout before
   * a thread is killed after it is interrupted.
   */
  public WorkQueue() {
    this(10, 10L * 60 * 1000, 20L * 60 * 1000);
  }

  /**
   * Creates a WorkQueue with a default 20 minute timeout before
   * interrupting a thread, and a default 10 minute timeout before
   * a thread is killed after it is interrupted.
   *
   * @param numThreads the number of threads to execute work on the WorkQueue.
   *        This number should be at least 1.
   */
  public WorkQueue(int numThreads) {
    this(numThreads, 10L * 60 * 1000, 20L * 60 * 1000);
  }

  /**
   * Creates a WorkQueue with a given number of worker threads and a
   * given timeout for the worker threads.
   *
   * @param numThreads the number of threads to execute work on the WorkQueue.
   *        This number should be at least 1.
   * @param killThreadTimeout the additional time in milliseconds given over the
   *        {@code workItemTimeout} before the {@code WorkQueueThread} is
   *        killed rather than just interrupted.
   */
  public WorkQueue(int numThreads, long killThreadTimeout) {
    this(numThreads, killThreadTimeout, 20L * 60 * 1000);
  }

  /**
   * Creates a WorkQueue with the given specification and also sets the timeout
   * of the WorkQueueItem.
   *
   * @param numThreads the number of threads to execute work on the WorkQueue.
   *        This number should be at least 1.
   * @param killThreadTimeout the additional time in milliseconds given over the
   *        {@code workItemTimeout} before the {@code WorkQueueThread} is
   *        killed rather than just interrupted.
   * @param workItemTimeout time in milliseconds that each {@code WorkQueueItem}
   *        is given before it is interrupted.  A value of 0 means it never
   *        times out.
   */
  public WorkQueue(int numThreads, long killThreadTimeout, long workItemTimeout)
  {
    if (numThreads <= 0) {
      throw new IllegalArgumentException("numThreads must be > 0");
    }
    this.killThreadTimeout = killThreadTimeout;
    this.workQueue = new LinkedList<WorkQueueItem>();
    this.absTimeoutMap = new HashMap<WorkQueueItem, Long>();
    this.isInitialized = false;
    this.shutdown = false;
    this.numThreads = numThreads;
    this.threads = new HashSet<WorkQueueThread>();
    this.workItemTimeout = workItemTimeout;
  }

  /**
   * Initialize work queue by starting worker threads.
   */
  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    shutdown = false;

    // If we will timeout overdue work items, start a thread that tracks them
    // and interrupts (and maybe kills off) those that are running too long.
    if (workItemTimeout > 0) {
      interrupterThread = new InterrupterThread("InterrupterThread");
      interrupterThread.start();
    }

    // Start WorkQueueThreads after initialization since they depend on the
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
   *
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
  public void shutdown(boolean interrupt) {
    shutdown(interrupt, DEFAULT_SHUTDOWN_TIMEOUT);
  }

  /**
   * Shutdown the work queue.
   *
   * @param interrupt if true, interrupt threads
   * @param timeoutInMillis wait at least this timeout for threads to complete
   *        before just returning
   */
  public void shutdown(boolean interrupt, long timeoutInMillis) {
    synchronized (this) {
      if (!isInitialized || shutdown) {
        return;
      }
      shutdown = true;
      workQueue.clear();

      try {
        lifeThread.shutdown();
        lifeThread.join(timeoutInMillis);
      } catch (InterruptedException e) {
        LOGGER.log(Level.WARNING,
            "Interrupted Exception while waiting for lifeThread: ", e);
      }
      if (interrupterThread != null) {
        interrupterThread.shutdown();
      }

      if (interrupt) {
        synchronized (threads) {
          for (WorkQueueThread thread : threads) {
            thread.interruptAndKill();
          }
        }
      }
    }

    // Don't hold lock while sleeping.
    long endTime = System.currentTimeMillis() + timeoutInMillis;
    while (isAnyThreadWorking() && System.currentTimeMillis() < endTime) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        LOGGER.log(Level.WARNING,
            "Interrupted Exception while waiting for worker threads: ", e);
      }
    }
    synchronized (this) {
      isInitialized = false;
    }
  }

  /**
   * Determine whether any WorkQueue thread is working.
   *
   * @return true if any thread in the WorkQueue is doing work.
   */
  private boolean isAnyThreadWorking() {
    synchronized (threads) {
      for (WorkQueueThread thread : threads) {
        if (thread.isWorking()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Interrupts all WorkQueueItems that have timed out.
   *
   * @return number of milliseconds until next soonest timeout.
   */
  private synchronized long interruptAllTimedOutItems() {
    // Determine which work threads should be interrupted as well as
    // set nextAbsTimeout.
    long now = System.currentTimeMillis();
    long nextAbsTimeout = now + killThreadTimeout;
    Iterator<Map.Entry<WorkQueueItem, Long>> iter;
    for (iter = absTimeoutMap.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<WorkQueueItem, Long> entry = iter.next();
      long absTimeout = entry.getValue();
      if (absTimeout <= now) {  // We have a timeout.
        LOGGER.finest("A WorkItem timeout has occured...");
        WorkQueueItem item = entry.getKey();
        // If the wait is too long, then we want to replace the thread.
        if (absTimeout + killThreadTimeout <= now) {
          LOGGER.finest("...replacing thread");
          iter.remove();
          item.cancelWork();
          replaceHangingThread(item);
          item.getWorkQueueThread().interruptAndKill();
        } else {
          LOGGER.finest("...interrupting thread");
          item.getWorkQueueThread().interrupt();
        }
      } else {
        // We have a thread that will timeout later.
        // LOGGER.finest("...waiting until later");
        if (absTimeout < nextAbsTimeout) {
          nextAbsTimeout = absTimeout;
        }
      }
    }
    return nextAbsTimeout - now;
  }

  /**
   * Given that a particular work item is hanging and isn't responding to
   * an interrupt, we want to be able to reclaim the thread that is hung
   * executing that work.  We do so by spawning a new thread.  Caller must
   * hold instance lock.
   *
   * @param item the item that is causing the hang
   */
  private void replaceHangingThread(WorkQueueItem item) {
    // Replace hanging thread with new thread if timeout is too long.
    WorkQueueThread hungThread = item.getWorkQueueThread();
    WorkQueueThread thread = new WorkQueueThread(hungThread.getName(), this);
    LOGGER.warning("Replacing work queue thread: " + hungThread.getName());
    synchronized (threads) {
      threads.remove(hungThread);
      threads.add(thread);
    }
    thread.start();
  }

  /**
   * Work to do right before executing the item.
   *
   * @param item
   */
  void preWork(WorkQueueItem item) {
    synchronized(this) {
      absTimeoutMap.put(item, workItemTimeout + System.currentTimeMillis());
    }
    synchronized(interrupterThread) {
      interrupterThread.notifyAll();
    }
  }

  /**
   * Work to do right after executing an item.
   *
   * @param item
   */
  void postWork(WorkQueueItem item) {
    synchronized(this) {
      absTimeoutMap.remove(item);
    }
  }

  /**
   * Add a piece of work that will be executed at a later time.
   *
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
    LOGGER.finest("Adding work: " + work);
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
   *
   * @param work the piece of work to be updated.
   */
  public synchronized void cancelWork(WorkQueueItem work) {
    if (!isInitialized) {
      throw new IllegalStateException(
        "Must init() WorkQueue object before canceling work.");
    }

    if (workQueue.remove(work)) {
      // If the work is in the queue, it means no thread is operating on it
      // so we simply finish.
      LOGGER.info("Cancelling work by removing unstarted work from work"
        + " queue.");
    } else {
      work.cancelWork();
      // See if a thread is working on this item.
      LOGGER.info("Cancelling work by interrupting the worker thread.");
      Thread thread = work.getWorkQueueThread();
      thread.interrupt();
    }
  }

  /**
   * Remove a piece of work.
   *
   * @return the work item
   */
  WorkQueueItem removeWork() {
    WorkQueueItem item = workQueue.removeFirst();
    LOGGER.finest("Removing work: " + item);
    return item;
  }

  /**
   * Determine the number of pieces of work that are in the queue.
   *
   * @return the number of pieces of work in the queue.
   */
  public synchronized int getWorkCount() {
    return (isInitialized) ? workQueue.size() : 0;
  }


  /**
   * Interrupts WorkQueueItemThread if it takes too long.
   */
  private class InterrupterThread extends Thread {

    private boolean shutdownNow = false;

    public InterrupterThread(String name) {
      super(name);
    }

    public void shutdown() {
      synchronized (this) {
        shutdownNow = true;
        notifyAll();
      }
    }

    @Override
    public void run() {
      NDC.push("Traverse");
      long nextCheckTimeout = killThreadTimeout;
      while (true) {
        try {
          synchronized (this) {
            if (shutdownNow) {
              break;
            } else {
              wait(nextCheckTimeout);
            }
          }
        } catch (InterruptedException e) {
          // Thread was signaled to determine whether there are new work
          // items to be interrupted (this is done under normal operation --
          // e.g. when we add a work item).
        }
        nextCheckTimeout = interruptAllTimedOutItems();
      }
      NDC.remove();
    }
  }

  /**
   * Ensures that WorkQueue does not have any dead WorkQueueThreads.
   */
  private class LifeThread extends Thread {
    // Timeout in milliseconds.
    private static final int LIFE_THREAD_WAIT_TIMEOUT = 60 * 1000;
    private boolean shutdownNow = false;

    public LifeThread(String name) {
      super(name);
    }

    public void shutdown() {
      synchronized (this) {
        shutdownNow = true;
        notifyAll();
      }
    }

    @Override
    public void run() {
      NDC.push("Traverse");
      while (true) {
        synchronized (this) {
          if (shutdownNow) {
            break;
          }
        }
        try {
          Set<WorkQueueThread> newThreads = new HashSet<WorkQueueThread>();
          Set<WorkQueueThread> deadThreads = new HashSet<WorkQueueThread>();
          synchronized (threads) {
            for (WorkQueueThread thread : threads) {
              if (!thread.isAlive()) {
                LOGGER.warning("WorkQueueThread was dead and is "
                    + "restarted by LifeThread: " + thread.getName());
                deadThreads.add(thread);
                newThreads.add(createAndStartWorkQueueThread(thread.getName()));
              }
            }
            threads.removeAll(deadThreads);
            threads.addAll(newThreads);
          }
          synchronized (this) {
            while (!shutdownNow) {
              wait(LIFE_THREAD_WAIT_TIMEOUT);
            }
          }
        } catch (InterruptedException e) {
          LOGGER.log(Level.WARNING, "Lifethread interrupted: ", e);
        }
      }
      NDC.remove();
    }
  }
}