// Copyright 2008 Google Inc.
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
package com.google.enterprise.connector.persist;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Provide some basic context that might be useful for the various
 * persistent store implementations.  For example, most make use
 * of the connectorName, and the FileStore needs access to the
 * connector work directory.
 */
public class StoreContext implements Comparable<StoreContext> {
  private final String connectorName;
  private final String typeName;

  public StoreContext(String connectorName, String typeName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(connectorName),
        "StoreContext.connectorName may not be null or empty.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(typeName),
        "StoreContext.typeName may not be null or empty.");
    this.connectorName = connectorName;
    this.typeName = typeName;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public String getTypeName() {
    return typeName;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof StoreContext))
      return false;
    StoreContext context = (StoreContext) other;
    return (connectorName.equals(context.connectorName) &&
            typeName.equals(context.typeName));
  }

  @Override
  public int hashCode() {
    // See Effective Java by Joshua Bloch, Item 8.
    int result = 131;
    result = 17 * result + connectorName.hashCode();
    result = 17 * result + typeName.hashCode();
    return result;
  }

  /* @Override */
  public int compareTo(StoreContext other) {
    int diff = connectorName.compareTo(other.connectorName);
    if (diff != 0) {
      return diff;
    } else {
      return typeName.compareTo(other.typeName);
    }
  }

  @Override
  public String toString() {
    return "{ " + connectorName + ", " + typeName + " }";
  }
}
