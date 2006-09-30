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

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Iterator;
import java.util.Map;

/**
 * Interface for instantiator component.
 */
public interface Instantiator {

  /**
   * gets an AuthenticationManager for a named connector.
   * 
   * @param connectorName the String name of the connector for which to get the
   *        Traverser
   * @return the AuthenticationManager, fully instantiated
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * gets an AuthorizationManager for a named connector.
   * 
   * @param connectorName the String name of the connector for which to get the
   *        Traverser
   * @return the AuthorizationManager, fully instantiated
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Finds a named connector.
   * 
   * @param connectorName
   * @return the Connector, fully instantiated
   * @throws ConnectorNotFoundException
   * @throws InstantiatorException
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Drops a named connector.
   * 
   * @param connectorName
   * @throws InstantiatorException
   */
  public void dropConnector(String connectorName) throws InstantiatorException;

  /**
   * Finds a named connector type.
   * 
   * @param connectorTypeName The connector type to find
   * @return the ConnectorType, fully instantiated
   * @throws ConnectorTypeNotFoundException if the connector type is not found
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException;

  /**
   * Gets all the known connector type names
   * 
   * @return an iterator of String names
   */
  public Iterator getConnectorTypeNames();

  /**
   * Gets the prototype definition for instances of this type
   * 
   * @param connectorTypeName The connector type for which to get the prototype
   * @return prototype String
   * @throws ConnectorTypeNotFoundException if the connector type is not found
   */
  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException;

  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, String language)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Sets the configuration for a new connector. This connector should not
   * exist.
   * 
   * @param connectorName The connector to create
   * @param connectorTypeName The type for this connector
   * @param configKeys A configuration map for this connector
   * @throws ConnectorNotFoundException
   * @throws ConnectorTypeNotFoundException
   * @throws InstantiatorException
   */
  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException;
}
