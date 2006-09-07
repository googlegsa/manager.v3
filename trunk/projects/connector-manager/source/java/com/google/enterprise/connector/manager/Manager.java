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

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.util.List;
import java.util.Map;

/**
 * The main interface to the Connector Manager. Front ends such as servlets or
 * main() programs may be built using this.
 */
public interface Manager {

  /**
   * Stores configuration changes to the Connector Manager itself.
   * 
   * @param certAuth Boolean indicating whether certificate authentication
   *        should be used
   * @param feederGateHost The GSA host expressed as a String
   * @param feederGatePort The GSA feeder port number
   * @param maxFeedRate The maximum feed rate expressed in documents/second
   * @throws PersistentStoreException If there was a problem storing the
   *         configuration
   */
  public void setConnectorManagerConfig(boolean certAuth, String feederGateHost,
      int feederGatePort, int maxFeedRate) throws PersistentStoreException;

  /**
   * Returns a list of connector types that this manager knows about.
   * 
   * @return A list of Strings - the name of each connector implementation.
   */
  public List getConnectorTypes();

  /**
   * Returns a list of ConnectorStatus objects for each connector that this
   * manager knows about.
   * 
   * @return A list of ConnectorStatus objects.
   */
  public List getConnectorStatuses();

  /**
   * Returns the status of a particular connector.
   * 
   * @param connectorInstanceName
   * @return Document containing XML configuration - DTD TBD.
   */
  public ConnectorStatus getConnectorStatus(String connectorInstanceName);

  /**
   * Get initial configuration form snippet for a connector type.
   * 
   * @param ConnectorType The name of a connector implementation - it should be
   *        one that this manager knows about (one that would be returned by a
   *        call to getConnectorTypes()).
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object, which may be null. If the return object
   *         is null or the form is null or empty, then the caller will use a
   *         default form.
   * @throws ConnectorTypeNotFoundException If the named connector type is not
   *         known to this manager.
   */
  public ConfigureResponse getConfigForm(String ConnectorType, String language)
      throws ConnectorTypeNotFoundException;

  /**
   * Get configuration data as a form snippet for an existing connnector. This
   * is different from getConfigForm because this is used to change the
   * configuration of a saved, configured Connector instance, not to configure a
   * new Connector instance.
   * 
   * @param connectorName The connector for which to fetch configuration
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object. As above, if the return object is null
   *         or the message and form are null or empty, then the caller will use
   *         a default form.
   * @throws ConnectorNotFoundException If the named connector is not known to
   *         this manager.
   */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String language) throws ConnectorNotFoundException;

  /**
   * Set config data for a new Connector or update config data for a running
   * Connector instance
   * 
   * @param connectorName The connector to update
   * @param configData A map of name, value pairs (String, String) of
   *        configuration data to submit
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object. If the return object is null, then this
   *         means that the configuration was valid and has been successfully
   *         stored. If the object is non-null, then the caller should try
   *         again.
   * @throws ConnectorNotFoundException If the named connector is not known to
   *         this manager.
   * @throws PersistentStoreException If there was a problem storing the
   *         configuration
   */
  public ConfigureResponse setConnectorConfig(String connectorName, 
      Map configData, String language) throws ConnectorNotFoundException,
      PersistentStoreException;

  /**
   * Authenticates a user against a named connector.
   * 
   * @param connectorInstanceName
   * @param username
   * @param password
   * @return true for success.
   */
  public boolean authenticate(String connectorInstanceName, String username,
      String password);

  /**
   * Gets authorization from a named connector for a set of documents by ID.
   * 
   * @param connectorInstanceName
   * @param docidList The document set represented as a list of Strings: the
   *        docid for each document
   * @param username The username as a string
   * @return A List of booleans parallel to the input list of IDs: the boolean
   *         in the corresponding position indicates whether that user can see
   *         that document.
   */
  public List authorizeDocids(String connectorInstanceName, List docidList,
      String username);

  /**
   * Gets authorization from a named connector for a set of documents by token.
   * 
   * @param connectorInstanceName
   * @param tokenList The document set represented as a list of Strings: the
   *        security token for a class of documents
   * @param username The username as a string
   * @return A List of booleans parallel to the input list of IDs: the boolean
   *         in the corresponding position indicates whether that user can see
   *         that document.
   */
  public List authorizeTokens(String connectorInstanceName, List tokenList,
      String username);

}
