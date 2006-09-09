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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test HostLoadManager class.
 */
public class HostLoadManagerTest extends TestCase {
  public void testMaxFeedRateLimit() {
    final int maxFeedRate = 1;  // 1 dps == 60 dpm
    final String connectorName = "cn1";
    HostLoadManager hostLoadManager = new HostLoadManager(maxFeedRate);
    hostLoadManager.updateNumDocsTraversed(connectorName, 60);
    Assert.assertEquals(0, hostLoadManager.determineBatchHint(connectorName));
  }
  
  public void testMultipleUpdates() {
    final int maxFeedRate = 1;  // 1 dps == 60 dpm
    final String connectorName = "cn1";
    HostLoadManager hostLoadManager = new HostLoadManager(maxFeedRate);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    Assert.assertEquals(30, hostLoadManager.determineBatchHint(connectorName));    
  }
  
  public void testMultipleConnectors() {
    final int maxFeedRate = 1;  // 1 dps == 60 dpm
    final String connectorName1 = "cn1";
    final String connectorName2 = "cn2";
    HostLoadManager hostLoadManager = new HostLoadManager(maxFeedRate);
    hostLoadManager.updateNumDocsTraversed(connectorName1, 60);
    Assert.assertEquals(0, hostLoadManager.determineBatchHint(connectorName1));

    hostLoadManager.updateNumDocsTraversed(connectorName2, 50);
    Assert.assertEquals(10, hostLoadManager.determineBatchHint(connectorName2));
  }
  
  public void testMinuteWait() {
    final int maxFeedRate = 1;  // 1 dps == 60 dpm
    final String connectorName = "cn1";
    HostLoadManager hostLoadManager = new HostLoadManager(maxFeedRate);
    hostLoadManager.updateNumDocsTraversed(connectorName, 55);
    Assert.assertEquals(5, hostLoadManager.determineBatchHint(connectorName));
    // sleep a minute so that batchHint is reset 
    try {
      Thread.sleep(60 * 1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Assert.assertEquals(60, hostLoadManager.determineBatchHint(connectorName));
    hostLoadManager.updateNumDocsTraversed(connectorName, 15);
    Assert.assertEquals(45, hostLoadManager.determineBatchHint(connectorName));
    
  }
}
