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


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Bounded buffer of {@link Change} objects for buffering between the
 * {@link DocumentSnapshotRepositoryMonitor} and the
 * {@link CheckpointAndChangeQueue}.
 * <p>
 * Public for testing.
 */
public class ChangeQueue implements ChangeSource {
  private final BlockingQueue<Change> pendingChanges;

  /** Milliseconds to sleep after a scan that finds no changes. */
  private final long sleepInterval;

  /**
   * Adds changes to this queue.
   */
  private class Callback implements DocumentSnapshotRepositoryMonitor.Callback {
    private int changeCount = 0;

    public void passBegin() {
      changeCount = 0;
    }

    /* @Override */
    public void changedDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {
      ++changeCount;
      pendingChanges.put(new Change(Change.FactoryType.CLIENT, dh, mcp));
    }

     /* @Override */
    public void deletedDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {
      ++changeCount;
      pendingChanges.put(new Change(Change.FactoryType.INTERNAL, dh, mcp));
    }

    /* @Override */
    public void newDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {
      ++changeCount;
      pendingChanges.put(new Change(Change.FactoryType.CLIENT, dh, mcp));
    }

    /* @Override */
    public void passComplete(MonitorCheckpoint mcp) throws InterruptedException {
      if (changeCount == 0) {
        Thread.sleep(sleepInterval);
      }
    }

    public boolean hasEnqueuedAtLeastOneChangeThisPass() {
      return changeCount > 0;
    }
  }

  public ChangeQueue(int size, long sleepInterval) {
    pendingChanges = new ArrayBlockingQueue<Change>(size);
    this.sleepInterval = sleepInterval;
  }

  /**
   * @return the monitor callback. This is a factory method for use by Spring,
   *         which needs a Callback to create a FileSystemMonitor.
   */
  public DocumentSnapshotRepositoryMonitor.Callback newCallback() {
    return new Callback();
  }

  /**
   * @return the next available change, or null if no changes are available.
   */
  public Change getNextChange() {
    return pendingChanges.poll();
  }

  /** Makes empty by removing all references from data structure. */
  void clear() {
    pendingChanges.clear();
  }
}
