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
public class StoreContext {

  private String connectorName;
  private File connectorDir;

  public StoreContext(String connectorName) {
    this(connectorName, null);
  }

  public StoreContext(String connectorName, File connectorDir) {
    this.connectorName = connectorName;
    this.connectorDir = connectorDir;
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
