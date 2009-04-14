// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.security.connectors.connauth;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple container class for holding information about a user, the connector,
 * and the connector manager that information applies to. This class is
 * immutable.
 */
public final class ConnectorUserInfo {
  private final String connectorManagerName;  // may be null or empty string
  private final String connectorName;
  private final String identity;

  /**
   * Create a ConnectorUserInfo with a particular connector name and user
   * identity.
   *
   * @param connectorManagerName The name of the connector manager hosting the
   *        connector.
   * @param connectorName The name of the connector associated with this
   *        identity.
   * @param identity The user identity this object represents.
   */
  public ConnectorUserInfo(String connectorManagerName, String connectorName,
      String identity) {
    checkNotNull(connectorName);
    checkNotNull(identity);
    this.connectorManagerName = connectorManagerName;
    this.connectorName = connectorName;
    // TODO check that identity does not contain invalid characters.
    // The connectorMangerName and connectorName are checked by the admin.
    this.identity = identity;
  }

  public String getConnectorManagerName() {
    return connectorManagerName;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public String getIdentity() {
    return identity;
  }
}
