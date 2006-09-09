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
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.monitor.HashMapMonitor;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests the Scheduler.
 *
 */
public class MockSchedulerTest extends TestCase {
  public void testRun() {
    WorkQueue workQueue = new WorkQueue(2);
    MockScheduler scheduler = 
      new MockScheduler(new MockInstantiator(), new HashMapMonitor(),
        workQueue);
    scheduler.init();
    Thread thread = new Thread(scheduler);
    thread.start();
    try {  
      Thread.sleep(5 * 1000);  // sleep 5 seconds
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      Assert.fail(ie.toString());
    }
    scheduler.shutdown();
  }
}
