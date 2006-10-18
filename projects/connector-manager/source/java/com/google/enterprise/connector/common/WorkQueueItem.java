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

/**
 * Item that will be run within the WorkQueue.
 */
public abstract class WorkQueueItem {
  private static final long timeout = 5 * 1000;
  // the thread that is executing the item (this is used for interrupting work)
  private WorkQueueThread workQueueThread;
  
  public WorkQueueThread getWorkQueueThread() {
    return workQueueThread;
  }

  /**
   * Set the WorkQueue thread.  This must be done before doWork() is called.
   * This thread will be used to interrupt the work if timeout occurs.
   * @param workQueueThread
   */
  public void setWorkQueueThread(WorkQueueThread workQueueThread) {
    this.workQueueThread = workQueueThread;
  }

  /**
   * Return the number of milliseconds before the WorkQueueRunnable times out
   * and may be interrupted.
   * @return timeout in milliseconds
   */
  public long getTimeout() {
    return timeout;
  }
  
  public abstract void doWork();
}
