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

import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description of a change to be sent to the Search Appliance.
 *
 * @since 2.8
 */
public class Change {
  /**
   * Indication of the {@link DocumentHandleFactory} needed to
   * un-serialize a {@link Change} from its JSON representation.
   */
  public static enum FactoryType {
    /** Indicates the client provided {@link DocumentHandleFactory} */
    CLIENT,
    /**
     * Indicates the internal {@link DocumentHandleFactory}. Currently
     * this is used for system generated deletes.
     */
    INTERNAL
  }

  /** Enumeration of fields in the JSON representation of a {@link Change}. */
  static enum Field {
    FACTORY_TYPE, DOCUMENT_HANDLE, MONITOR_CHECKPOINT,
        mcp //Monitor checkpoint was represented as 'mcp' in old format.
  }

  private final FactoryType factoryType;
  private final DocumentHandle documentHandle;
  private final MonitorCheckpoint monitorCheckpoint;
  /**
   * Create a new {@code Change}.
   *
   * @param factoryType
   * @param documentHandle
   * @param monitorCheckpoint
   */
  public Change(FactoryType factoryType, DocumentHandle documentHandle,
      MonitorCheckpoint monitorCheckpoint) {
    Preconditions.checkNotNull(factoryType);
    Preconditions.checkNotNull(documentHandle);
    Preconditions.checkNotNull(monitorCheckpoint);
    this.factoryType = factoryType;
    this.documentHandle = documentHandle;
    this.monitorCheckpoint = monitorCheckpoint;
  }

  /**
   * Create a new {@code Change} based on a JSON-encoded object.
   *
   * @param json a JSON-encoded object
   * @param internalFactory a DocumentHandleFactory
   * @param clientFactory a DocumentHandleFactory
   * @throws JSONException
   */
  Change(JSONObject json, DocumentHandleFactory internalFactory,
      DocumentHandleFactory clientFactory) throws JSONException {
    FactoryType tempFactoryType;
    DocumentHandle docHandle;
    if (isNewVersionOfChangeRepresentation(json)) {
        tempFactoryType = FactoryType.valueOf(json.getString(
          Field.FACTORY_TYPE.name()));
      if (tempFactoryType.equals(FactoryType.INTERNAL)) {
          docHandle = internalFactory.fromString(
            json.getString(Field.DOCUMENT_HANDLE.name()));
      } else {
          docHandle = clientFactory.fromString(
            json.getString(Field.DOCUMENT_HANDLE.name()));
      }
      this.monitorCheckpoint = new MonitorCheckpoint(
          json.getJSONObject(Field.MONITOR_CHECKPOINT.name()));
    } else {
      // This else condition is required for previous version of diffing
      // connector (FS connector) which used a completely different String
      // representation of Change.There is no way to know which factory to
      // use because the old json tags are not available so approach here
      // is to ask question to each factory and one that give non-null handle
      // will be used. This way the change will be in FS connector code since
      // it knows what it used previously. Implementation classes of
      // DocumentHandleFactory should take care of checking the backward
      // compatibility of the change representations.
      tempFactoryType = FactoryType.INTERNAL;
      docHandle = internalFactory.fromString(json.toString());
      if (this.documentHandle == null) {
          tempFactoryType = FactoryType.CLIENT;
          docHandle = clientFactory.fromString(json.toString());
      }
      if (docHandle == null) {
        throw new IllegalArgumentException("Could not "
            + "constitute a document handle with given Json object.");
      }
      this.monitorCheckpoint = new MonitorCheckpoint(
          json.getJSONObject(Field.mcp.name()));
    }
    this.factoryType = tempFactoryType;
    this.documentHandle = docHandle;
  }

  /**
   * Returns true if the change represented by Json object is of new version
   * or not.
   * @param json json representation of the change
   * @return true / false depending on the version of Change representation
   */
  private boolean isNewVersionOfChangeRepresentation(JSONObject json) {
    return json.has(Field.FACTORY_TYPE.name());
  }

  /**
   * @return the monitor checkpoint associated with this change
   */
  MonitorCheckpoint getMonitorCheckpoint() {
    return monitorCheckpoint;
  }

  JSONObject getJson() {
    JSONObject result = new JSONObject();
    try {
      result.put(Field.FACTORY_TYPE.name(),
          factoryType.name());
      result.put(Field.DOCUMENT_HANDLE.name(),
          documentHandle.toString());
      result.put(Field.MONITOR_CHECKPOINT.name(),
          monitorCheckpoint.getJson());
      return result;
    } catch (JSONException e) {
      // Only thrown if a key is null or a value is a non-finite number, neither
      // of which should ever happen.
      throw new RuntimeException("internal error: failed to create JSON", e);
    }
  }

  DocumentHandle getDocumentHandle() {
    return documentHandle;
  }

  /**
   * Converts this instance into a JSON object and
   * returns that object's string representation.
   */
  @Override
  public String toString() {
    return "" + getJson();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((documentHandle == null) ? 0 : documentHandle.hashCode());
    result = prime * result
        + ((factoryType == null) ? 0 : factoryType.hashCode());
    result = prime * result
        + ((monitorCheckpoint == null) ? 0 : monitorCheckpoint.hashCode());
    return result;
  }

  @Override
  // TODO: Upgrade CheckpointAndChangeQueueTest to not depend on
  //       Change implementing values equality which is not
  //       reliable given DocumentHandle may fall back to
  //       object instance equality.
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Change other = (Change) obj;
    if (documentHandle == null) {
      if (other.documentHandle != null) {
        return false;
      }
    } else if (!documentHandle.equals(other.documentHandle)) {
      return false;
    }
    if (factoryType == null) {
      if (other.factoryType != null) {
        return false;
      }
    } else if (!factoryType.equals(other.factoryType)) {
      return false;
    }
    if (monitorCheckpoint == null) {
      if (other.monitorCheckpoint != null) {
        return false;
      }
    } else if (!monitorCheckpoint.equals(other.monitorCheckpoint)) {
      return false;
    }
    return true;
  }
}
