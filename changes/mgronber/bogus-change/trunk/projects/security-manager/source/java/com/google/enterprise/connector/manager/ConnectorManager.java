// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.connector.manager;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.server.BackEnd;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is temporary -- while the connector manager and the security
 * manager are different builds. This gives the security manager a connector
 * manager with an implementation for the overloading of authenticate() that
 * includes a security context.
 *
 * This functionality will eventually be merged with the connector manager's
 * {@link ProductionManager}.
 */
public class ConnectorManager extends ProductionManager {

  private static final Logger LOGGER = Logger.getLogger(ConnectorManager.class.getName());

  private BackEnd backEnd;
  private boolean securityManagerConnectorsInitialized;

  public ConnectorManager() {
    super();
    securityManagerConnectorsInitialized = false;
  }

  public BackEnd getBackEnd() {
    return backEnd;
  }

  public void setBackEnd(BackEnd backEnd) {
    backEnd.setConnectorManager(this);
    this.backEnd = backEnd;
  }

  @Override
  public boolean authenticate(String connectorName, 
      AuthenticationIdentity identity) {
    throw new UnsupportedOperationException();
  }

  /**
   * This method will become part of the {@link Manager} interface
   */
  public AuthenticationResponse authenticate(String connectorName, AuthenticationIdentity id,
      SecAuthnContext securityContext) {
    AuthenticationManager authnManager = null;
    try {
      authnManager = instantiator.getAuthenticationManager(connectorName);
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Connector " + connectorName + " Not Found: ", e);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Instantiator: ", e);
    }

    // Some connectors don't implement the AuthenticationManager interface, or
    // there may been an instantiation problem.
    if (authnManager == null) {
      return null;
    }

    AuthnCaller authnCaller = new AuthnCaller(authnManager, id, securityContext);

    return authnCaller.authenticate();
  }

  @Override
  public List<ConnectorStatus> getConnectorStatuses() {
    if (!securityManagerConnectorsInitialized) {
      try {
        this.setConnectorConfig("BasicAuth", "BasicAuthConnector",
                                ImmutableMap.of("ServerUrl", ""),  // dummy parameter
                                "en", false);
        this.setConnectorConfig("FormAuth", "FormAuthConnector",
                                ImmutableMap.of("CookieName", ""),  // dummy parameter
                                "en", false);
        this.setConnectorConfig("ConnAuth", "ConnAuthConnector",
                                ImmutableMap.of("SpiVersion", "0"),
                                "en", false);
        securityManagerConnectorsInitialized = true;
      } catch (ConnectorNotFoundException e) {
        LOGGER.info("ConnectorNotFound: " + e.toString());
      } catch (InstantiatorException e) {
        LOGGER.info("Instantiator: " + e.toString());
      } catch (PersistentStoreException e) {
        LOGGER.info("PersistentStore: " + e.toString());
      }
    }
    @SuppressWarnings("unchecked")
        List<ConnectorStatus> result = super.getConnectorStatuses();
    return result;
  }
}
