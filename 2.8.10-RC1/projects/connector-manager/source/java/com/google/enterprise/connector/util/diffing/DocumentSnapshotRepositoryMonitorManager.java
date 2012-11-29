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

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalSchedule;

import java.util.Map;

/**
 * Management interface to {@link DocumentSnapshotRepositoryMonitor} threads.
 *
 * @since 2.8
 */
public interface DocumentSnapshotRepositoryMonitorManager {
  /**
   * Ensures all monitor threads are running.
   *
   * @param checkpoint for the last completed document or null if none have
   *        been completed.
   * @throws RepositoryException
   */
  void start(String checkpoint) throws RepositoryException;

  /**
   * Stops all the configured {@link DocumentSnapshotRepositoryMonitor} threads.
   */
  void stop();

  /**
   * Removes persisted state for {@link DocumentSnapshotRepositoryMonitor}
   * threads. After calling this {@link DocumentSnapshotRepositoryMonitor}
   * threads will no longer be able to resume from where they left off last
   * time.
   */
  void clean();

  /**
   * Returns the number of {@link DocumentSnapshotRepositoryMonitor} threads
   * that are alive. This method is for testing purposes.
   */
  int getThreadCount();

  /**
   * Returns the {@link CheckpointAndChangeQueue} for this
   * {@link DocumentSnapshotRepositoryMonitorManager}
   */
  CheckpointAndChangeQueue getCheckpointAndChangeQueue();

  /** Returns whether we are after a start() call and before a stop(). */
  boolean isRunning();

  /**
   * Receives information specifying what is guaranteed to be delivered to GSA.
   * Every entry in passed in Map is a monitor name and MonitorCheckpoint.
   * The monitor of that name can expect that all documents before and including
   * document related with MonitorCheckpoint will be delivered to GSA.
   * This information is for the convenience and efficiency of the Monitor so
   * that it knows how many changes it has to resend.  It's valid for a monitor
   * to ignore these updates if it feels like it for some good reason.
   * FileConnectorSystemMonitor instances use this information to trim their
   * file system snapshots.
   */
  void acceptGuarantees(Map<String, MonitorCheckpoint> guarantees);

  /**
   * Receives {@link TraversalSchedule} from TraversalManager which is
   * {@link TraversalScheduleAware}.  
   */
  void setTraversalSchedule(TraversalSchedule traversalSchedule);
}
