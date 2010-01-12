// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;
import com.google.enterprise.connector.pusher.MockFeedConnection;

import junit.framework.TestCase;

/**
 * Test HostLoadManager class.
 */
public class HostLoadManagerTest extends TestCase {
  private final MockInstantiator instantiator = new MockInstantiator(null);

  private void addLoad(String connectorName, int load) {
    Schedule schedule = new Schedule(connectorName + ":" + load + ":100:0-0");
    String connectorSchedule = schedule.toString();
    try {
      instantiator.setConnectorSchedule(connectorName, connectorSchedule);
    } catch (ConnectorNotFoundException cnfe) {
      // This test attempts to add schedules to connectors that do not exist.
      // If there is not yet a connector with this name, create a dummy one.
      try {
        instantiator.setupTraverser(connectorName, null);
        instantiator.setConnectorSchedule(connectorName, connectorSchedule);
      } catch (ConnectorNotFoundException e) {
        fail("Unexpected ConnectorNotFoundException : " + e.toString());
      }
    }
  }

  public void testMaxFeedRateLimit() {
    final String connectorName = "cn1";
    addLoad(connectorName, 60);
    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null);
    assertEquals(60, hostLoadManager.determineBatchSize(connectorName).getHint());
    hostLoadManager.updateNumDocsTraversed(connectorName, 60);
    assertEquals(0, hostLoadManager.determineBatchSize(connectorName).getHint());
  }

  public void testMultipleUpdates() {
    final String connectorName = "cn1";
    addLoad(connectorName, 60);
    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    assertEquals(30, hostLoadManager.determineBatchSize(connectorName).getHint());
  }

  public void testMultipleConnectors() {
    final String connectorName1 = "cn1";
    final String connectorName2 = "cn2";
    addLoad(connectorName1, 60);
    addLoad(connectorName2, 60);
    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null);
    hostLoadManager.updateNumDocsTraversed(connectorName1, 60);
    assertEquals(0, hostLoadManager.determineBatchSize(connectorName1).getHint());

    hostLoadManager.updateNumDocsTraversed(connectorName2, 50);
    assertEquals(10, hostLoadManager.determineBatchSize(connectorName2).getHint());
  }

  public void testPeriod() {
    final String connectorName = "cn1";

    // NOTE: HostLoadManager.determineBatchSize() and shouldDelay() make the
    // assumption that periodInMillis is a minute.  We skew these values here
    // in such a way that their calculations come out the same, but we don't
    // have multi-minute waits in the unit tests.
    final long periodInMillis = 1000;
    addLoad(connectorName, 3600);

    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null, periodInMillis);
    hostLoadManager.updateNumDocsTraversed(connectorName, 55);
    assertEquals(5, hostLoadManager.determineBatchSize(connectorName).getHint());
    // sleep a period (and then some) so that batchHint is reset
    try {
      // extra time in ms in case sleeping the period is not long enough
      Thread.sleep(periodInMillis + 200);
    } catch (InterruptedException e) {
      // Ignore.
    }
    assertEquals(60, hostLoadManager.determineBatchSize(connectorName).getHint());
    hostLoadManager.updateNumDocsTraversed(connectorName, 15);
    assertEquals(45, hostLoadManager.determineBatchSize(connectorName).getHint());
  }

  public void testRetryDelay() {
    final String connectorName = "cn1";

    // NOTE: HostLoadManager.determineBatchSize() and shouldDelay() make the
    // assumption that periodInMillis is a minute.  We skew these values here
    // in such a way that their calculations come out the same, but we don't
    // have multi-minute waits in the unit tests.
    final long periodInMillis = 1000;
    addLoad(connectorName, 3600);

    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null, periodInMillis);
    assertFalse(hostLoadManager.shouldDelay(connectorName));
    hostLoadManager.connectorFinishedTraversal(connectorName, 100);
    assertTrue(hostLoadManager.shouldDelay(connectorName));
    // sleep more than 100ms the time set in MockConnectorSchedule
    // so that this connector can be allowed to run again without delay
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      // Ignore.
    }
    assertFalse(hostLoadManager.shouldDelay(connectorName));
  }

  /**
   * Test that if we meet or exceed the host load limit, further
   * traversal should be delayed until the next traversal period.
   */
  public void testLoadDelay() {
    final String connectorName = "cn1";

    // NOTE: HostLoadManager.determineBatchSize() and shouldDelay() make the
    // assumption that periodInMillis is a minute.  We skew these values here
    // in such a way that their calculations come out the same, but we don't
    // have multi-minute waits in the unit tests.
    final long periodInMillis = 1000;
    addLoad(connectorName, 3600);

    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null, periodInMillis);
    assertFalse(hostLoadManager.shouldDelay(connectorName));
    hostLoadManager.updateNumDocsTraversed(connectorName, 60);
    assertTrue(hostLoadManager.shouldDelay(connectorName));

    // Sleep more than 1000ms the time set in MockConnectorSchedule
    // so that this connector can be allowed to run again without delay.
    try {
      Thread.sleep(1250);
    } catch (InterruptedException e) {
      // Ignore.
    }
    assertFalse(hostLoadManager.shouldDelay(connectorName));
  }

  /**
   * Test that if we have nearly reached the load level, we
   * won't OK a traversal requesting a tiny number of documents.
   */
  public void testLoadDelay2() {
    final String connectorName = "cn1";

    // NOTE: HostLoadManager.determineBatchSize() and shouldDelay() make the
    // assumption that periodInMillis is a minute.  We skew these values here
    // in such a way that their calculations come out the same, but we don't
    // have multi-minute waits in the unit tests.
    final long periodInMillis = 1000;
    addLoad(connectorName, 3600);

    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null, periodInMillis);
    assertFalse(hostLoadManager.shouldDelay(connectorName));
    hostLoadManager.updateNumDocsTraversed(connectorName, 59);

    // We should delay rather than ask for a batch of 1.
    BatchSize batchSize = hostLoadManager.determineBatchSize(connectorName);
    assertEquals(1, batchSize.getHint());
    assertTrue(hostLoadManager.shouldDelay(connectorName));

    // Sleep more than 1000ms the time set in MockConnectorSchedule
    // so that this connector can be allowed to run again without delay.
    try {
      Thread.sleep(1250);
    } catch (InterruptedException e) {
      // Ignore.
    }
    assertFalse(hostLoadManager.shouldDelay(connectorName));
  }

  /**
   * Test that the optimal batch size may be configured in the Spring Context.
   */
  public void testBatchSize() {
    // Need to test through Spring to make sure the property works.
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext("testdata/mocktestdata/applicationContext.xml",
        null);
    // Get the HostLoadManager bean.
    HostLoadManager hostLoadManager = (HostLoadManager) context.getRequiredBean(
        "HostLoadManager", HostLoadManager.class);
    assertEquals(500, hostLoadManager.getBatchSize());

    // Now get one from a different context.
    Context.refresh();
    context = Context.getInstance();
    context.setStandaloneContext(
        "testdata/schedulerTests/applicationContext.xml", null);
    hostLoadManager = (HostLoadManager) context.getRequiredBean(
        "HostLoadManager", HostLoadManager.class);
    assertEquals(200, hostLoadManager.getBatchSize());
  }

  /**
   * The new determineBatchSize logic treats both the hint and the load
   * as "suggestions", or "desired targets".  The traversal might
   * return more than the batchHint, and the number of docs processed in
   * a period might exceed the load.  This flexibility allows the connectors
   * to work more efficiently without expending a great deal of effort
   * trying to hit the batch size exactly.  However, poorly behaved
   * connectors could attempt to vastly exceed the recommended batch size,
   * so the BatchSize.maximum constraint puts a ceiling on the number of
   * documents that will be processed from the DocumentList.
   */
  public void testDetermineBatchSize() {
    final String connectorName = "cn1";
    addLoad(connectorName, 60);
    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, null);
    BatchSize batchSize = hostLoadManager.determineBatchSize(connectorName);
    assertEquals(60, batchSize.getHint());
    assertEquals(120, batchSize.getMaximum());
    hostLoadManager.setBatchSize(40);
    batchSize = hostLoadManager.determineBatchSize(connectorName);
    assertEquals(40, batchSize.getHint());
    assertEquals(80, batchSize.getMaximum());
    hostLoadManager.setBatchSize(10);
    batchSize = hostLoadManager.determineBatchSize(connectorName);
    assertEquals(10, batchSize.getHint());
    assertEquals(60, batchSize.getMaximum());
  }

  /**
   * Test shouldDelay(void) with a low memory condition.
   */
  public void testShouldDelayLowMemory() {
    Runtime rt = Runtime.getRuntime();
    FileSizeLimitInfo fsli = new FileSizeLimitInfo();
    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, null, fsli);

    // OK to start a traversal if there is plenty of memory for a new feed.
    rt.gc();
    fsli.setMaxFeedSize(rt.freeMemory()/100);
    assertFalse(hostLoadManager.shouldDelay());

    // Not OK to start a traversal if there is not enough memory for feeds.
    rt.gc();
    fsli.setMaxFeedSize(rt.maxMemory() - (rt.totalMemory() - rt.freeMemory()));
    assertTrue(hostLoadManager.shouldDelay());
  }

  /**
   * Test shouldDelay(void) with backlogged FeedConnection.
   */
  public void testShouldDelayFeedBacklogged() {
    BacklogFeedConnection feedConnection = new BacklogFeedConnection();
    HostLoadManager hostLoadManager =
        new HostLoadManager(instantiator, feedConnection, null);

    // OK to start a traversal if feedConnection is not backlogged.
    feedConnection.setBacklogged(false);
    assertFalse(hostLoadManager.shouldDelay());

    // Not OK to start a traversal if feedConnection is backlogged.
    feedConnection.setBacklogged(true);
    assertTrue(hostLoadManager.shouldDelay());
  }

  /**
   * A FeedConnection that can be backlogged.
   */
  private class BacklogFeedConnection extends MockFeedConnection {
    private boolean backlogged = false;

    public void setBacklogged(boolean backlogged) {
      this.backlogged = backlogged;
    }

    @Override
    public boolean isBacklogged() {
      return backlogged;
    }
  }
}
