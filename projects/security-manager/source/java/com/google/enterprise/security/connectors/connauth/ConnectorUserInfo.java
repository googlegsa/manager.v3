// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.enterprise.security.connectors.connauth;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple container class for holding information about a user, the connector,
 * and the connector manager that information applies to. This class is
 * immutable.
 * 
 */
public final class ConnectorUserInfo {
  // this can be null or empty string
  private final String connectorManagerName;
  private final String connectorName;
  // this will be null in the case that a user failed to authenticate to the
  // connector named by connectorName.
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

  /**
   * Create a ConnectorUserInfo that represents the fact that a particular user
   * failed to authenticate to a particular connector.
   * 
   * @param connectorManagerName The name of the connector manager hosting the
   *        connector.
   * @param connectorName The name of the connector which the user failed to
   *        authenticate against.
   */
  public ConnectorUserInfo(String connectorManagerName, String connectorName) {
    checkNotNull(connectorName);
    this.connectorManagerName = connectorManagerName;
    this.connectorName = connectorName;
    this.identity = null;
  }

  public String getConnectorManagerName() {
    return connectorManagerName;
  }

  public String getConnectorName() {
    return connectorName;
  }

  /**
   * Get the identity of the search user.
   * 
   * @return null if the user failed to authenticate to the associated
   *         connector.
   */
  public String getIdentity() {
    return identity;
  }

}
