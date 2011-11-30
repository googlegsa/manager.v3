// Copyright 2006 Google Inc.
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

import com.google.common.collect.Maps;
import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.ExtendedConfigureResponse;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ProductionManager implements Manager {
  private static final Logger LOGGER =
      Logger.getLogger(ProductionManager.class.getName());

  Instantiator instantiator;

  public ProductionManager() {
  }

  /**
   * @param instantiator the instantiator to set
   */
  public void setInstantiator(Instantiator instantiator) {
    this.instantiator = instantiator;
  }

  /* @Override */
  public AuthenticationResponse authenticate(String connectorName,
      AuthenticationIdentity identity) {
    try {
      AuthenticationManager authnManager =
          instantiator.getAuthenticationManager(connectorName);
      // Some connectors don't implement the AuthenticationManager interface so
      // we need to check.
      if (authnManager != null) {
        return authnManager.authenticate(identity);
      }
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Connector " + connectorName + " Not Found: ",
          e);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Instantiator: ", e);
    } catch (RepositoryLoginException e) {
      LOGGER.log(Level.WARNING, "Login: ", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Repository: ", e);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Exception: ", e);
    }
    return new AuthenticationResponse(false, null);
  }

  /* @Override */
  public Collection<AuthorizationResponse> authorizeDocids(String connectorName,
      List<String> docidList, AuthenticationIdentity identity) {
    try {
      AuthorizationManager authzManager =
          instantiator.getAuthorizationManager(connectorName);
      if (authzManager == null) {
        // This is a bad situation.  This means the Connector has feed the
        // content in such a way that it is being asked to authorize access to
        // that content and yet it doesn't implement the AuthorizationManager
        // interface.  Log the situation and return the empty result.
        LOGGER.warning("Connector:" + connectorName
            + " is being asked to authorize documents but has not implemented"
            + " the AuthorizationManager interface.");
        return null;
      }
      Collection<AuthorizationResponse> results =
          authzManager.authorizeDocids(docidList, identity);
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("AUTHORIZED: connector = " + connectorName + ", "
                    + identity + ": authorized " + results.size()
                    + " of " + docidList.size() + " documents.");
      }
      if (LOGGER.isLoggable(Level.FINEST)) {
        for (AuthorizationResponse response : results) {
          LOGGER.finest("AUTHORIZED " + response.getDocid() + ": "
                        + response.getStatus());
        }
      }
      return results;
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Connector " + connectorName + " Not Found: ",
          e);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Instantiator: ", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Repository: ", e);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Exception: ", e);
    }
    return null;
  }

  /* @Override */
  public ConfigureResponse getConfigForm(String connectorTypeName,
      String language)
      throws ConnectorTypeNotFoundException, InstantiatorException {
    ConnectorType connectorType =
        instantiator.getConnectorType(connectorTypeName);
    Locale locale = I18NUtil.getLocaleFromStandardLocaleString(language);
    ConfigureResponse response;
    try {
      response = connectorType.getConfigForm(locale);
    } catch (Exception e) {
      throw new InstantiatorException("Failed to get configuration form.", e);
    }

    // Include the connectorInstance.xml in the response.
    if (response != null) {
      return new ExtendedConfigureResponse(response,
          instantiator.getConnectorInstancePrototype(connectorTypeName));
    }
    return response;
  }

  /* @Override */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String language)
      throws ConnectorNotFoundException, InstantiatorException {
    String connectorTypeName = instantiator.getConnectorTypeName(connectorName);
    Locale locale = I18NUtil.getLocaleFromStandardLocaleString(language);
    ConfigureResponse response =
        instantiator.getConfigFormForConnector(connectorName,
            connectorTypeName, locale);
    return response;
  }

  /* @Override */
  public ConnectorStatus getConnectorStatus(String connectorName)
      throws ConnectorNotFoundException {
    String connectorTypeName = instantiator.getConnectorTypeName(connectorName);
    Schedule schedule = instantiator.getConnectorSchedule(connectorName);
    // TODO: resolve the third parameter - we need to give status a meaning
    return new ConnectorStatus(connectorName, connectorTypeName, 0,
        ((schedule == null) ? null : schedule.toString()));
  }

  /* @Override */
  public List<ConnectorStatus> getConnectorStatuses() {
    List<ConnectorStatus> result = new ArrayList<ConnectorStatus>();
    for (String connectorName : instantiator.getConnectorNames()) {
      try {
        result.add(getConnectorStatus(connectorName));
      } catch (ConnectorNotFoundException e) {
        // This is unlikely to happen, but skip this one anyway.
        LOGGER.finest("Connector not found: " + connectorName);
      }
    }
    return result;
  }

  /* @Override */
  public Set<String> getConnectorTypeNames() {
    return instantiator.getConnectorTypeNames();
  }

  /* @Override */
  public ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    return instantiator.getConnectorType(typeName);
  }

  /* @Override */
  public ConfigureResponse setConnectorConfiguration(String connectorName,
      Configuration configuration, String language, boolean update)
      throws ConnectorNotFoundException, PersistentStoreException,
      InstantiatorException {
    return instantiator.setConnectorConfiguration(connectorName, configuration,
        I18NUtil.getLocaleFromStandardLocaleString(language), update);
  }

  /* @Override */
  public Properties getConnectorManagerConfig() {
    return Context.getInstance().getConnectorManagerConfig();
  }

  /* @Override */
  public void setConnectorManagerConfig(String feederGateProtocol,
      String feederGateHost, int feederGatePort, int feederGateSecurePort)
      throws PersistentStoreException {
    try {
      Context.getInstance().setConnectorManagerConfig(feederGateProtocol,
          feederGateHost, feederGatePort, feederGateSecurePort);
    } catch (InstantiatorException e) {
      throw new PersistentStoreException(e);
    }
  }

  /* @Override */
  public void setSchedule(String connectorName, String schedule)
      throws ConnectorNotFoundException, PersistentStoreException {
    instantiator.setConnectorSchedule(connectorName,
        ((schedule == null) ? null : new Schedule(schedule)));
  }

  /* @Override */
  public void removeConnector(String connectorName)
      throws InstantiatorException {
    instantiator.removeConnector(connectorName);
  }

  /* @Override */
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    instantiator.restartConnectorTraversal(connectorName);
  }

  /* @Override */
  public Configuration getConnectorConfiguration(String connectorName)
      throws ConnectorNotFoundException {
    return instantiator.getConnectorConfiguration(connectorName);
  }

  /* @Override */
  public boolean isLocked() {
    return Context.getInstance().getIsManagerLocked();
  }
}
