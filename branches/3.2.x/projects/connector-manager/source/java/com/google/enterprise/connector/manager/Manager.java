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

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The main interface to the Connector Manager. Front ends such as servlets or
 * main() programs may be built using this.
 */
public interface Manager {

  /**
   * Gets the current configuration of the Connector Manager itself.
   *
   * @return the current configuration settings in a Properties object.  Keys
   *         to the Properties are the same as the keys in the property file.
   * @throws PersistentStoreException if there was a problem retrieving the
   *         configuration.
   */
  public Properties getConnectorManagerConfig() throws PersistentStoreException;

  /**
   * Stores configuration changes to the Connector Manager itself.
   *
   * @param feederGateProtocol The GSA feeder gate protocol; one of
   * {@code "http"}, {@code "https"}, {@code ""}, or {@code null}
   * @param feederGateHost The GSA host expressed as a String
   * @param feederGatePort The GSA feeder gate HTTP port number
   * @param feederGateSecurePort The GSA feeder gate HTTP port number
   * @param connectorManagerUrl URL string for the Connector Manager servlet
   * @throws PersistentStoreException If there was a problem storing the
   *         configuration
   */
  public void setConnectorManagerConfig(String feederGateProtocol,
      String feederGateHost, int feederGatePort, int feederGateSecurePort,
      String connectorManagerUrl) throws PersistentStoreException;

  /**
   * Returns a list of connector types that this manager knows about.
   *
   * @return A Set of Strings - the name of each connector implementation.
   */
  public Set<String> getConnectorTypeNames();

  /**
   * Returns the ConnectorType that is associated with the supplied name.
   *
   * @param typeName a ConnectorType name.
   * @return an instance of ConnectorType associated with the typeName.
   * @throws ConnectorTypeNotFoundException if the connector type is not found.
   */
  public ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException;

  /**
   * Returns a list of ConnectorStatus objects for each connector that this
   * manager knows about.
   *
   * @return A list of ConnectorStatus objects.
   */
  public List<ConnectorStatus> getConnectorStatuses();

  /**
   * Returns the status of a particular connector.
   *
   * @param connectorName the name of the connector instance
   * @throws ConnectorNotFoundException If the named connector is not known to
   *         this manager.
   * @return Document containing XML configuration - DTD TBD.
   */
  public ConnectorStatus getConnectorStatus(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Get initial configuration form snippet for a connector type.
   *
   * @param connectorTypeName The name of a connector implementation - it should
   *        be one that this manager knows about (one that would be returned by
   *        a call to getConnectorTypes()).
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object, which may be null. If the return object
   *         is null or the form is null or empty, then the caller will use a
   *         default form.
   * @throws ConnectorTypeNotFoundException If the named connector type is not
   *         known to this manager.
   * @throws InstantiatorException
   */
  public ConfigureResponse getConfigForm(String connectorTypeName,
      String language) throws ConnectorTypeNotFoundException,
      InstantiatorException;

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
   * @throws InstantiatorException
   */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String language) throws ConnectorNotFoundException, InstantiatorException;

  /**
   * Set config data for a new Connector or update config data for a running
   * Connector instance
   *
   * @param connectorName The connector to update
   * @param configuration A {@link Configuration} containing the connector's
   *        connectorType-specific configuration data
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @param update A boolean true to update the giving existing connector,
   *        false to create a new connector for the given connector name
   * @return a ConfigureResponse object. If the return object is null, then this
   *         means that the configuration was valid and has been successfully
   *         stored. If the object is non-null, then the caller should try
   *         again.
   * @throws ConnectorNotFoundException If the named connector is not known to
   *         this manager.
   * @throws PersistentStoreException If there was a problem storing the
   *         configuration
   * @throws InstantiatorException If the instantiator cannot store the
   *         configuration
   */
  public ConfigureResponse setConnectorConfiguration(String connectorName,
      Configuration configuration, String language, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      PersistentStoreException, InstantiatorException;

  /**
   * Authenticates an Identity against a named connector.
   *
   * @param connectorName
   * @param identity An AuthenticationIdentity object that encapsulates the
   *        user's identity
   * @return an AuthenticationResponse
   */
  public AuthenticationResponse authenticate(String connectorName,
      AuthenticationIdentity identity);

  /**
   * Gets authorization from a named connector for a set of documents by ID.
   *
   * @param connectorName
   * @param docidList The document set represented as a list of Strings: the
   *        docid for each document
   * @param identity An AuthenticationIdentity object that encapsulates the
   *        user's identity
   * @return A {@code Collection} of {@link AuthorizationResponse} objects.
   *         The collection of responses need not be in the same order as the
   *         collection of docids. The returned collection of responses may
   *         contain only those docids for which the user has positive access,
   *         although inclusion of items with deny access is recommended.
   *         A return value of {@code null} indicates that the documents could
   *         not be authorized for some reason, and no access rights (positive
   *         or negative) should be inferred.
   */
  public Collection<AuthorizationResponse> authorizeDocids(String connectorName,
      List<String> docidList, AuthenticationIdentity identity);

  /**
   * Return an {@code InputStream} that may be used to access content for the
   * document identified by {@code docid}.
   *
   * @param connectorName
   * @param docid the document identifier
   * @return an InputStream for the document content, or {@code null} if
   *         document content is not available.
   */
  public InputStream getDocumentContent(String connectorName, String docid)
      throws ConnectorNotFoundException, InstantiatorException,
             RepositoryException;

  /**
   * Return a {@link Document} that contains meta-data for the
   * document identified by {@code docid}, but not the actual content.
   *
   * @param connectorName
   * @param docid the document identifier
   * @return a {@link Document} containing the document meta-data,
   *         or {@code null} if document content is not available.
   */
  public Document getDocumentMetaData(String connectorName, String docid)
      throws ConnectorNotFoundException, InstantiatorException,
             RepositoryException;

  /**
   * Set schedule for a given Connector.
   *
   * @param connectorName
   * @param schedule stringified Schedule
   * @throws ConnectorNotFoundException If the named connector is not known to
   *         this manager.
   * @throws PersistentStoreException If there was a problem storing the
   *         configuration
   */
  public void setSchedule(String connectorName, String schedule)
      throws ConnectorNotFoundException, PersistentStoreException;

  /**
   * Remove the named connector.
   *
   * @param connectorName
   * @throws ConnectorNotFoundException If the named connector is not known to
   *         this manager.
   * @throws PersistentStoreException If there was a problem storing the
   *         configuration
   */
  public void removeConnector(String connectorName)
      throws ConnectorNotFoundException, PersistentStoreException,
      InstantiatorException;

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
   * Get a connector's ConnectorType-specific configuration data
   *
   * @param connectorName the connector to look up
   * @return a {@link Configuration} of its ConnectorType-specific
   * configuration data
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public Configuration getConnectorConfiguration(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * @return true if the manager is currently locked, false otherwise.
   */
  public boolean isLocked();
}
