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
 * Checkpoint for the {@link DiffingConnector}.
 *
 * @since 2.8
 */
public class DiffingConnectorCheckpoint implements
    Comparable<DiffingConnectorCheckpoint> {
  private static enum JsonFields {
    MAJOR_NUMBER, MINOR_NUMBER;
  }

  private final long majorNumber;
  private final long minorNumber;

  /**
   * Returns a {@link DiffingConnectorCheckpoint} that is less than or equal to
   * all others.
   */
  public static DiffingConnectorCheckpoint newFirst() {
    return new DiffingConnectorCheckpoint(0, 0);
  }

  /**
   * Returns a {@link DiffingConnectorCheckpoint} from a {@link String} that was
   * produced by calling {link #toString}.
   *
   * @throws IllegalArgumentException if checkpoint is not a valid value
   *         that was created by calling {link #toString()}
   */
  public static DiffingConnectorCheckpoint fromJsonString(String jsonObjectString) {
    try {
      JSONObject jsonObject = new JSONObject(jsonObjectString);
      return fromJson(jsonObject);
    } catch (JSONException je) {
      throw new IllegalArgumentException("Invalid checkpoint " + jsonObjectString, je);
    }
  }

  public static DiffingConnectorCheckpoint fromJson(JSONObject jsonObject) {
    try {
      return new DiffingConnectorCheckpoint(jsonObject.getLong(JsonFields.MAJOR_NUMBER.name()),
          jsonObject.getLong(JsonFields.MINOR_NUMBER.name()));
    } catch (JSONException je) {
      throw new IllegalArgumentException("Invalid checkpoint " + jsonObject, je);
    }
  }

  /**
   * Returns the {@link DiffingConnectorCheckpoint} after this one.
   */
  public DiffingConnectorCheckpoint next() {
    return new DiffingConnectorCheckpoint(majorNumber, minorNumber + 1);
  }

  /**
   * Returns the first {@link DiffingConnectorCheckpoint} with {@link
   * #getMajorNumber()} greater than the value for this {@link
   * DiffingConnectorCheckpoint}.
   */
  public DiffingConnectorCheckpoint nextMajor() {
    return new DiffingConnectorCheckpoint(majorNumber + 1, 0);
  }

  /**
   * Returns a {@link String} representation of this
   * {@link DiffingConnectorCheckpoint}.
   */
  @Override
  public String toString() {
    return getJson().toString();
  }

  /* @Override */
  public int compareTo(DiffingConnectorCheckpoint o) {
    long result = majorNumber - o.majorNumber;
    if (result == 0) {
      result = minorNumber - o.minorNumber;
    }
    return Long.signum(result);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (majorNumber ^ (majorNumber >>> 32));
    result = prime * result + (int) (minorNumber ^ (minorNumber >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DiffingConnectorCheckpoint)) {
      return false;
    }
    DiffingConnectorCheckpoint other = (DiffingConnectorCheckpoint) obj;
    if (majorNumber != other.majorNumber) {
      return false;
    }
    if (minorNumber != other.minorNumber) {
      return false;
    }
    return true;
  }

  public final long getMajorNumber() {
    return majorNumber;
  }

  public final long getMinorNumber() {
    return minorNumber;
  }

  private DiffingConnectorCheckpoint(long major, long minor) {
    this.majorNumber = major;
    this.minorNumber = minor;
  }

  JSONObject getJson() {
    try {
      JSONObject result = new JSONObject();
      result.put(JsonFields.MAJOR_NUMBER.name(), majorNumber);
      result.put(JsonFields.MINOR_NUMBER.name(), minorNumber);
      return result;
    } catch (JSONException je) {
      throw new RuntimeException("Unexpected JSON Exception ", je);
    }
  }
}
