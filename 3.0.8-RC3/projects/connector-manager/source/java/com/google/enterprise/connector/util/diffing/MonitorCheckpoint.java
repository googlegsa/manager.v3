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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A checkpoint for a single {@link DocumentSnapshotRepositoryMonitor}.
 *
 * @since 2.8
 */
public class MonitorCheckpoint {
  private static final String SNAPSHOT_NUMBER_TAG = "ssn";
  private static final String MONITOR_NAME_TAG = "name";
  private static final String OFFSET_1_TAG = "offset1";
  private static final String OFFSET_2_TAG = "offset2";

  private final String monitorName;
  private final long snapshotNumber;
  private final long offset1;
  private final long offset2;

  /**
   * Constructs {@link DocumentSnapshotRepositoryMonitor} checkpoint.
   *
   * @param monitorName the {@link DocumentSnapshotRepositoryMonitor} name
   * @param snapshotNumber the snapshot number
   * @param offset1 the offset into the snapshot repository for snapshot
   *        {@code snapshotNumber}
   * @param offset2 the offset into the snapshot repository for snapshot
   *        {@code snapshotNumber + 1}
   */
  public MonitorCheckpoint(String monitorName, long snapshotNumber,
                           long offset1, long offset2) {
    this.monitorName = monitorName;
    this.snapshotNumber = snapshotNumber;
    this.offset1 = offset1;
    this.offset2 = offset2;
  }

  /**
   * Reconstructs {@link DocumentSnapshotRepositoryMonitor} checkpoint from
   * a JSON object.
   * @see #getJson
   * @param json a JSON encoded {@link DocumentSnapshotRepositoryMonitor}
   */
  public MonitorCheckpoint(JSONObject json) throws JSONException {
    this.monitorName = json.getString(MONITOR_NAME_TAG);
    this.snapshotNumber = json.getLong(SNAPSHOT_NUMBER_TAG);
    this.offset1 = json.getLong(OFFSET_1_TAG);
    this.offset2 = json.getLong(OFFSET_2_TAG);
  }

  /**
   * @return a JSON object that can be used to reconstruct this checkpoint.
   */
  public JSONObject getJson() {
    try {
      JSONObject result = new JSONObject();
      result.put(MONITOR_NAME_TAG, monitorName);
      result.put(SNAPSHOT_NUMBER_TAG, snapshotNumber);
      result.put(OFFSET_1_TAG, offset1);
      result.put(OFFSET_2_TAG, offset2);
      return result;
    } catch (JSONException e) {
      // Only thrown if a key is null or a value is a non-finite number, neither
      // of which should ever happen.
      throw new RuntimeException("internal error: unexpected JSON exception", e);
    }
  }

  /**
   * @return the name of the monitor this checkpoint applies to.
   */
  public String getMonitorName() {
    return monitorName;
  }

  /**
   * @return the lower of the two snapshot numbers that belong to this
   *         checkpoint.
   */
  public long getSnapshotNumber() {
    return snapshotNumber;
  }

  /**
   * @return the offset into the snapshot repository for snapshot number
   *         {@code snapshotNumber}
   */
  public long getOffset1() {
    return offset1;
  }

  /**
   * @return the offset into the snapshot repository for snapshot number
   *         {@code snapshotNumber + 1}
   */
  public long getOffset2() {
    return offset2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((monitorName == null) ? 0 : monitorName.hashCode());
    result = prime * result + (int) (offset1 ^ (offset1 >>> 32));
    result = prime * result + (int) (offset2 ^ (offset2 >>> 32));
    result = prime * result + (int) (snapshotNumber ^ (snapshotNumber >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MonitorCheckpoint)) {
      return false;
    }
    MonitorCheckpoint other = (MonitorCheckpoint) obj;
    if (monitorName == null) {
      if (other.monitorName != null) {
        return false;
      }
    } else if (!monitorName.equals(other.monitorName)) {
      return false;
    }
    if (offset1 != other.offset1) {
      return false;
    }
    if (offset2 != other.offset2) {
      return false;
    }
    if (snapshotNumber != other.snapshotNumber) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getJson().toString();
  }
}
