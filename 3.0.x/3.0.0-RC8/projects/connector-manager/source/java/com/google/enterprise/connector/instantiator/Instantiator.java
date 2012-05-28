// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Facade interface for instantiator package.
 */
public interface Instantiator {
  /**
   * Gets an AuthenticationManager for a named connector.
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
   * Gets an AuthorizationManager for a named connector.
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
   * Return a {@link Retriever} that may be used to access content for the
   * document identified by {@code docid}.  If the connector does not support
   * the {@link Retriever} interface, {@code null} is returned.
   *
   * @param connectorName the String name of the connector from which to get the
   *        Retriever
   * @return a {@link Retriever}, or {@code null} if none is available
   * @throws ConnectorNotFoundException to indicate that no connector of the
   *         specified name is found
   * @throws InstantiatorException if something bad, probably unrecoverable,
   *         happens
   */
  public Retriever getRetriever(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Restart the Traverser for the named connector.
   * This resets the Traverser, re-indexing the repository from scratch.
   *
   * @param connectorName
   * @throws ConnectorNotFoundException
   * @throws InstantiatorException
   */
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Removes a named connector.
   *
   * @param connectorName
   * @throws InstantiatorException
   */
  public void removeConnector(String connectorName) throws InstantiatorException;

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
   * Gets all the known connector type names.
   *
   * @return a Set of String names
   */
  public Set<String> getConnectorTypeNames();

  /**
   * Gets the prototype definition for instances of this type
   *
   * @param connectorTypeName The connector type for which to get the prototype
   * @return prototype String
   * @throws ConnectorTypeNotFoundException if the connector type is not found
   * @see ConnectorType#getConfigForm(Locale)
   */
  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException;

  /**
   * Get configuration form snippet populated with values representing the
   * configuration of the supplied connector.
   *
   * @param connectorName the connector whose configuration should be used to
   *        populate the form snippet.
   * @param connectorTypeName The connector type for which to get the prototype
   * @param locale A java.util.Locale which the implementation may use to
   *        produce appropriate descriptions and messages.
   * @return a ConfigureResponse object. The form must be prepopulated with the
   *         data from the supplied connector instance's configuration.
   * @see ConnectorType#getPopulatedConfigForm(Map, Locale)
   */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale)
      throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Get the names of all known connector instances.
   *
   * @return a Set of String names
   */
  public Set<String> getConnectorNames();

  /**
   * Get the type for a known connector
   *
   * @param connectorName the connector to look up
   * @return its type, as a String
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Sets the {@link Configuration} for a new connector.
   *
   * @param connectorName The connector's name
   * @param configuration A Configuration for this connector
   * @param locale A Java Locale string
   * @param update A boolean true if updating the existing connector
   * @return null if config is valid and accepted, a ConfigureResponse object
   *         if config is invalid.
   * @throws ConnectorNotFoundException
   * @throws ConnectorExistsException
   * @throws ConnectorTypeNotFoundException
   * @throws InstantiatorException
   */
  public ConfigureResponse setConnectorConfiguration(String connectorName,
      Configuration configuration, Locale locale, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      ConnectorTypeNotFoundException, InstantiatorException;

  /**
   * Get a connector's ConnectorType-specific {@link Configuration} data
   *
   * @param connectorName the connector to look up
   * @return a {@link Configuration} of its ConnectorType-specific
   *         configuration data
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public Configuration getConnectorConfiguration(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Sets the {@link Schedule} of a named connector.
   *
   * @param connectorName
   * @param connectorSchedule Schedule to store or null unset any existing
   *        Schedule.
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public void setConnectorSchedule(String connectorName,
      Schedule connectorSchedule) throws ConnectorNotFoundException;

  /**
   * Gets the {@link Schedule} of a named connector.
   *
   * @param connectorName
   * @return the Schedule, or null if there is no stored Schedule
   *         for this connector.
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public Schedule getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Returns a connector's {@link DocumentFilterFactory}. Connectors may define
   * a document filter specific to that connector instance.  This filter will
   * be used in conjuction with the Connector Manager's document filter, and
   * will act as the source for the Connector Manager's document filter.
   *
   * @return {@link DocumentFilterFactory} for the connector, or {@code null}
   *         if the connector does not define a DocumentFilterFactory.
   */
  public DocumentFilterFactory getDocumentFilterFactory(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Sets {@code GData} configuration for GData aware Connectors.
   */
  public void setGDataConfig();

  /**
   * Starts running a batch for a named connector if a batch is not
   * already running.
   *
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public void startBatch(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Shutdown all the Connector instances.
   */
  public void shutdown(boolean interrupt, long timeoutMillis);
}
