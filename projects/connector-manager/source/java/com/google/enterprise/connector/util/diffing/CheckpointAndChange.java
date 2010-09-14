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
 * Holder Object for a {@link Change} with its associated {@link
 * DiffingConnectorCheckpoint}.
 */
class CheckpointAndChange {
  private static final String CHECKPOINT_LABEL = "cp";
  private static final String CHANGE_LABEL = "chg";

  private final DiffingConnectorCheckpoint checkpoint;
  private final Change change;

  CheckpointAndChange(DiffingConnectorCheckpoint checkpoint, Change change) {
    if (null == checkpoint) {
      throw new IllegalArgumentException("Checkpoint cannot be null.");
    }
    if (null == change) {
      throw new IllegalArgumentException("Change cannot be null.");
    }
    this.checkpoint = checkpoint;
    this.change = change;
  }

  CheckpointAndChange(JSONObject json, DocumentHandleFactory internalFactory,
      DocumentHandleFactory clientFactory) throws JSONException {
    this.checkpoint = DiffingConnectorCheckpoint.fromJson(
        json.getJSONObject(CHECKPOINT_LABEL));
    this.change = new Change(json.getJSONObject(CHANGE_LABEL), internalFactory,
        clientFactory);
  }

  DiffingConnectorCheckpoint getCheckpoint() {
    return checkpoint;
  }

  Change getChange() {
    return change;
  }

  /**
   * Converts this instance into a JSON object
   * and return that object's string representation.
   */
  @Override
  public String toString() {
    return "" + getJson();
  }

  public JSONObject getJson() {
    JSONObject result = new JSONObject();
    try {
      result.put(CHECKPOINT_LABEL, checkpoint.getJson());
      result.put(CHANGE_LABEL, change.getJson());
      return result;
    } catch (JSONException e) {
      // Expected to never occur.
      throw new RuntimeException("internal error: failed to create JSON", e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (null == o) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof CheckpointAndChange)) {
      return false;
    }
    CheckpointAndChange other = (CheckpointAndChange) o;
    return this.checkpoint.equals(other.checkpoint)
        && this.change.equals(other.change);
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.hashCode(new Object[]{checkpoint, change});
  }
}
