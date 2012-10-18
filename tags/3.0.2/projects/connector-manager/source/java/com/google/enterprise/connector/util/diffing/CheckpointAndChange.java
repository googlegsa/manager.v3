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
 * Holder Object for a {@link Change} with its associated
 * {@link DiffingConnectorCheckpoint}.
 *
 * @since 2.8
 */
class CheckpointAndChange {
  private static final String CHECKPOINT_LABEL = "cp";
  private static final String CHANGE_LABEL = "chg";

  private final DiffingConnectorCheckpoint checkpoint;
  private final Change change;

  /**
   * Construct a holder object for a {@link Change} with its associated
   * {@link DiffingConnectorCheckpoint}.
   *
   * @param checkpoint
   * @param change
   */
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

  /**
   * Construct a CheckpointAndChange object.
   *
   * @param json the JSON representation of a DiffingConnectorCheckpoint
   * @param internalFactory a DocumentHandleFactory
   * @param clientFactory a DocumentHandleFactory
   */
  CheckpointAndChange(JSONObject json, DocumentHandleFactory internalFactory,
      DocumentHandleFactory clientFactory) throws JSONException {
    this.checkpoint = DiffingConnectorCheckpoint.fromJson(
        json.getJSONObject(CHECKPOINT_LABEL));
    this.change = new Change(json.getJSONObject(CHANGE_LABEL), internalFactory,
        clientFactory);
  }

  /** @return the  {@link DiffingConnectorCheckpoint} */
  DiffingConnectorCheckpoint getCheckpoint() {
    return checkpoint;
  }

  /** @return the {@link Change} */
  Change getChange() {
    return change;
  }

  /** @return the string representation of the JSON object of this instance */
  @Override
  public String toString() {
    return "" + getJson();
  }

  /** @return this instance as a JSON object */
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
