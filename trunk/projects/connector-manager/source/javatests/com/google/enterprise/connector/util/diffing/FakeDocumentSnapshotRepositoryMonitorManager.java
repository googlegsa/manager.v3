// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util.diffing;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@Link FileSystemMonitorManager} for testing.
 */
public class FakeDocumentSnapshotRepositoryMonitorManager
    implements DocumentSnapshotRepositoryMonitorManager {
  private final AtomicInteger startCount = new AtomicInteger();
  private final AtomicInteger cleanCount = new AtomicInteger();
  private final AtomicInteger stopCount = new AtomicInteger();
  private final AtomicInteger guaranteeCount = new AtomicInteger();
  private final CheckpointAndChangeQueue checkpointAndChangeQueue;
  private boolean isRunning = false;

  public FakeDocumentSnapshotRepositoryMonitorManager(ChangeSource changeSource,
      TestCase testCase,
      DocumentHandleFactory internalFactory,
      DocumentHandleFactory clientFactory)
      throws IOException {
    File persistDir = new TestDirectoryManager(testCase).makeDirectory("queue");
    checkpointAndChangeQueue = (changeSource == null) ? null :
        new CheckpointAndChangeQueue(changeSource, persistDir, internalFactory,
            clientFactory);
    try {
      this.checkpointAndChangeQueue.start(null);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Unexpectedatly cannot start CheckpointAndChangeQueue.", e);
    }
  }

  /* @Override */
  public void clean() {
    cleanCount.incrementAndGet();
  }

  /* @Override */
  public int getThreadCount() {
    return 0;
  }

  /* @Override */
  public void start(String checkpoint) {
    startCount.incrementAndGet();
    isRunning = true;
  }

  /* @Override */
  public synchronized void stop() {
    stopCount.incrementAndGet();
    isRunning = false;
  }

  /**
   * Returns the number of times {@link #start(String)}
   * has been called.
   */
  public int getStartCount() {
    return startCount.get();
  }

  /**
   * Returns the number of times {@link #stop} has been called.
   */
  public int getStopCount() {
    return stopCount.get();
  }

  /**
   * Returns the number of times {@link #clean} has been called.
   */
  public int getCleanCount() {
    return cleanCount.get();
  }

  /**
   * Returns the number of times {@link #acceptGuarantees} has been called.
   */
  public int getGuaranteeCount() {
    return guaranteeCount.get();
  }

  /* @Override */
  public CheckpointAndChangeQueue getCheckpointAndChangeQueue() {
    if (checkpointAndChangeQueue == null) {
      throw new IllegalStateException(
          "getCheckpointAndChangeQueue not supported with null checkpointAndChangeQueue");
    }
    return checkpointAndChangeQueue;
  }

  public synchronized boolean isRunning() {
    return isRunning;
  }

  /* @Override */
  public void acceptGuarantees(Map<String, MonitorCheckpoint> guarantees) {
    guaranteeCount.incrementAndGet();
  }
}
