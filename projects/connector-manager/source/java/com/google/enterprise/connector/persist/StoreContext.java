// Copyright 2008 Google Inc.  All Rights Reserved.
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

import java.io.File;

/**
 * Provide some basic context that might be useful for the various
 * persistent store implementations.  For example, most make use
 * of the connectorName, and the FileStore needs access to the
 * connector work directory.
 */
public class StoreContext implements Comparable<StoreContext> {
  private String connectorName;
  private File connectorDir;

  public StoreContext(String connectorName) {
    this(connectorName, null);
  }

  public StoreContext(String connectorName, File connectorDir) {
    this.connectorName = connectorName;
    this.connectorDir = connectorDir;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof StoreContext))
      return false;
    StoreContext context = (StoreContext) other;
    if (!connectorName.equals(context.connectorName)) {
      return false;
    } else {
      return (connectorDir == null)
          ? context.connectorDir == null
          : connectorDir.equals(context.connectorDir);
    }
  }

  @Override
  public int hashCode() {
    // See Effective Java by Joshua Bloch, Item 8.
    int result = 131;
    result = 17 * result + connectorName.hashCode();
    result = 17 * result
        + (connectorDir == null ? 0 : connectorDir.hashCode());
    return result;
  }

  //@Override
  public int compareTo(StoreContext other) {
    int diff = connectorName.compareTo(other.connectorName);
    if (diff != 0) {
      return diff;
    } else {
      if (connectorDir == null && other.connectorDir == null) {
        return 0;
      } else if (connectorDir == null) {
        return -1;
      } else if (other.connectorDir == null) {
        return 1;
      } else 
        return connectorDir.compareTo(other.connectorDir);
    }
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  public void setConnectorDir(String connectorDir) {
    setConnectorDir(new File(connectorDir));
  }

  public void setConnectorDir(File connectorDir) {
    this.connectorDir = connectorDir;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public File getConnectorDir() {
    return connectorDir;
  }
}
