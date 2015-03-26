// Copyright 2007-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Session;

/**
 * Wraps an existing Connector, adding the ability
 * to add custom properties.
 */
public final class CustomProtoTestConnector
    implements Connector, ConnectorShutdownAware {

  private Connector delegateConnector;
  private String customProperty = "default";
  private int customIntProperty = 0;

  public void setDelegateConnector(final Connector delegateConnector) {
    this.delegateConnector = delegateConnector;
  }

  public void setCustomProperty(final String customProperty) {
    this.customProperty = customProperty;
  }

  public String getCustomProperty() {
    return customProperty;
  }

  public int getCustomIntProperty() {
    return customIntProperty;
  }

  public void setCustomIntProperty(int customIntProperty) {
    this.customIntProperty = customIntProperty;
  }

  public Session login() throws RepositoryLoginException, RepositoryException {
     return delegateConnector.login();
  }

  public void shutdown() throws RepositoryException {
    if (delegateConnector instanceof ConnectorShutdownAware) {
      ((ConnectorShutdownAware)delegateConnector).shutdown();
    }
  }

  public void delete() throws RepositoryException {
    if (delegateConnector instanceof ConnectorShutdownAware) {
      ((ConnectorShutdownAware)delegateConnector).delete();
    }
  }
}
