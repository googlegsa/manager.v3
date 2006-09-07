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

import junit.framework.TestCase;

/**
 * Test for WorkQueue class.
 */
public class WorkQueueTest extends TestCase {
  private static class PrintRunnable implements Runnable {
    private String str;
    public PrintRunnable(String str) {
      this.str = str;
    }
    public void run() {
      System.out.println(str);
    }
  }
  
  /**
   * Wait until the queue is empty (i.e. all work done).
   * @param queue the WorkQueue
   */
  private static void waitForEmptyQueue(WorkQueue queue) {
    while (queue.getWorkCount() > 0);
  }
  
  public void testAddWork() {
    WorkQueue queue = new WorkQueue(2);
    queue.init();
    for (int i = 0; i < 10; i++) {
      queue.addWork(new PrintRunnable("work started: " + i));
    }
    
    waitForEmptyQueue(queue);
    queue.shutdown();
  }
  
  public void testInitShutdown() {
    WorkQueue queue = new WorkQueue(2);
    queue.init();
    for (int i = 0; i < 10; i++) {
      queue.addWork(new PrintRunnable("work started: " + i));
    }
    queue.shutdown();
    queue.init();
    for (int i = 0; i < 10; i++) {
      queue.addWork(new PrintRunnable("work started again: " + i));
    }
    
    waitForEmptyQueue(queue);
    queue.shutdown();
  }
}
