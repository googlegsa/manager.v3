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

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.persist.ConnectorConfigStore;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 */
public class ProductionManager implements Manager {

  Instantiator instantiator;
  ConnectorConfigStore connectorConfigStore;

  public ProductionManager() {
  }


  /**
   * @param connectorConfigStore the connectorConfigStore to set
   */
  public void setConnectorConfigStore(ConnectorConfigStore connectorConfigStore) {
    this.connectorConfigStore = connectorConfigStore;
  }


  /**
   * @param instantiator the instantiator to set
   */
  public void setInstantiator(Instantiator instantiator) {
    this.instantiator = instantiator;
  }


  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager
   *      #authenticate(java.lang.String, java.lang.String, java.lang.String)
   */
  public boolean authenticate(String connectorName, String username,
      String password) {
    // TODO need a real implementation
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager
   *      #authorizeDocids(java.lang.String, java.util.List, java.lang.String)
   */
  public List authorizeDocids(String connectorName, List docidList,
      String username) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager
   *      #authorizeTokens(java.lang.String, java.util.List, java.lang.String)
   */
  public List authorizeTokens(String connectorName, List tokenList,
      String username) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager
   *      #getConfigForm(java.lang.String, java.lang.String)
   */
  public ConfigureResponse getConfigForm(String connectorTypeName,
      String language) throws ConnectorTypeNotFoundException {
    ConnectorType connectorType =
        instantiator.getConnectorType(connectorTypeName);
    return connectorType.getConfigForm(language);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager
   *      #getConfigFormForConnector(java.lang.String, java.lang.String)
   */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String language) throws ConnectorNotFoundException {
    String connectorTypeName =
        connectorConfigStore.getConnectorType(connectorName);
    // TODO: this will return the form - but without the existing config
    // pre-filled in
    ConfigureResponse response = null;
    try {
      response = getConfigForm(connectorTypeName, language);
    } catch (ConnectorTypeNotFoundException e) {
      // shouldn't happen, because we just checked and established the known
      // type
      throw new IllegalArgumentException();
    }
    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager
   *      #getConnectorStatus(java.lang.String)
   */
  public ConnectorStatus getConnectorStatus(String connectorName) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager#getConnectorStatuses()
   */
  public List getConnectorStatuses() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager#getConnectorTypes()
   */
  public List getConnectorTypes() {
    // TODO this interface should really return a Set
    Set result = new TreeSet();
    for (Iterator i = instantiator.getConnectorTypeNames(); i.hasNext();) {
      result.add(i.next());
    }
    return new ArrayList(result);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager#setConnectorConfig(java.lang.String,
   *      java.util.Map, java.lang.String)
   */
  public ConfigureResponse setConnectorConfig(String connectorName,
      Map configData, String language) throws ConnectorNotFoundException,
      PersistentStoreException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.manager.Manager#setConnectorManagerConfig(boolean,
   *      java.lang.String, int, int)
   */
  public void setConnectorManagerConfig(boolean certAuth,
      String feederGateHost, int feederGatePort, int maxFeedRate)
      throws PersistentStoreException {
    // TODO - need a real implementation here
   }

}
