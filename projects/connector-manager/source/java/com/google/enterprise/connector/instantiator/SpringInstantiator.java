// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 
 */
public class SpringInstantiator implements Instantiator {
  
  private static final Logger LOGGER =
    Logger.getLogger(SpringInstantiator.class.getName());

  TypeMap typeMap = null;
  InstanceMap instanceMap = null;
  Pusher pusher;
  ConnectorStateStore connectorStateStore;
  Map connectorCache;

  public SpringInstantiator(Pusher pusher, 
      ConnectorStateStore connectorStateStore) {
    this.pusher = pusher;
    this.connectorStateStore = connectorStateStore;
    connectorCache = new HashMap();
    // NOTE: we can't call initialize() here because then there would be a
    // circular dependency on the Context, which hasn't been constructed yet
  }

  private void initialize() {
    if (typeMap != null) {
      return;
    }
    LOGGER.info("Initializing instantiator");
    typeMap = new TypeMap();
    instanceMap = new InstanceMap(typeMap);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #dropConnector(java.lang.String)
   */
  public void dropConnector(String connectorName) throws InstantiatorException {
    initialize();
    instanceMap.dropConnector(connectorName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getAuthenticationManager(java.lang.String)
   */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        getConnectorInterfaces(connectorName);
    return connectorInterfaces.getAuthenticationManager();
  }

  private ConnectorInterfaces getConnectorInterfaces(String connectorName)
      throws ConnectorNotFoundException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        (ConnectorInterfaces) connectorCache.get(connectorName);
    if (connectorInterfaces == null) {
      InstanceInfo instanceInfo = (InstanceInfo) instanceMap.get(connectorName);
      if (instanceInfo == null) {
        throw new ConnectorNotFoundException();
      }
      connectorInterfaces =
          new ConnectorInterfaces(connectorName, instanceInfo.getConnector(),
              pusher, connectorStateStore, instanceInfo.getProperties());
      connectorCache.put(connectorName, connectorInterfaces);
    }
    return connectorInterfaces;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getAuthorizationManager(java.lang.String)
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        getConnectorInterfaces(connectorName);
    return connectorInterfaces.getAuthorizationManager();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getConfigFormForConnector(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, String language)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    InstanceInfo instanceInfo = (InstanceInfo) instanceMap.get(connectorName);
    if (instanceInfo == null) {
      throw new ConnectorNotFoundException();
    }
    TypeInfo typeInfo = instanceInfo.getTypeInfo();
    ConnectorType connectorType = typeInfo.getConnectorType();
    Map configMap = instanceInfo.getProperties();
    ConfigureResponse configureResponse =
        connectorType.getPopulatedConfigForm(configMap, language);
    return configureResponse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getConnectorInstancePrototype(java.lang.String)
   */
  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getConnectorType(java.lang.String)
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    initialize();
    TypeInfo typeInfo = typeMap.getTypeInfo(connectorTypeName);
    if (typeInfo == null) {
      throw new ConnectorTypeNotFoundException();
    }
    return typeInfo.getConnectorType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getConnectorTypeNames()
   */
  public Iterator getConnectorTypeNames() {
    initialize();
    return Collections.unmodifiableSet(typeMap.keySet()).iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getTraverser(java.lang.String)
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        getConnectorInterfaces(connectorName);
    return connectorInterfaces.getTraverser();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#setConnectorConfig(java.lang.String,
   *      java.lang.String, java.util.Map)
   */
  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      ConnectorTypeNotFoundException, InstantiatorException {
    initialize();
    instanceMap.updateConnector(
        connectorName, connectorTypeName, configKeys, update);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore#getConnectorNames()
   */
  public Iterator getConnectorNames() {
    initialize();
    return Collections.unmodifiableSet(instanceMap.keySet()).iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore#getConnectorTypeName(java.lang.String)
   */
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    initialize();
    InstanceInfo instanceInfo = (InstanceInfo) instanceMap.get(connectorName);
    if (instanceInfo == null) {
      throw new ConnectorNotFoundException("Connector not found: " 
        + connectorName);
    }
    return instanceInfo.getTypeInfo().getConnectorTypeName();
  }

}
