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
import com.google.enterprise.connector.common.AlternateContentFilterInputStream;
import com.google.enterprise.connector.common.BigEmptyDocumentFilterInputStream;
import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.DocumentFilterFactoryFactory;
import com.google.enterprise.connector.instantiator.ExtendedConfigureResponse;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.pusher.AclTransformFilter;
import com.google.enterprise.connector.pusher.DocUtils;
import com.google.enterprise.connector.pusher.FeedConnection;
import com.google.enterprise.connector.pusher.InheritFromExtractedAclDocumentFilter;
import com.google.enterprise.connector.pusher.UrlConstructor;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.util.EofFilterInputStream;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import java.io.InputStream;
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
  private DocumentFilterFactoryFactory documentFilterFactoryFactory = null;
  private FeedConnection feedConnection;

  public ProductionManager() {
  }

  /**
   * @param instantiator the instantiator to set
   */
  public void setInstantiator(Instantiator instantiator) {
    this.instantiator = instantiator;
  }

  /**
   * Specify the document filter factory that should be applied to documents.
   *
   * @param documentFilterFactoryFactory document filter factory to use
   */
  public void setDocumentFilterFactoryFactory(
      DocumentFilterFactoryFactory documentFilterFactoryFactory) {
    this.documentFilterFactoryFactory = documentFilterFactoryFactory;
  }

  /**
   * Sets the feed connection to use to discover if the security header is
   * supported. This must be set during startup to take effect.
   */
  public void setFeedConnection(FeedConnection feedConnection) {
    this.feedConnection = feedConnection;
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
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("AUTHENTICATE: " + identity);
        }
        AuthenticationResponse response = authnManager.authenticate(identity);
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("AUTHENTICATION "
              + (response.isValid() ? "SUCCEEDED" : "FAILED") + ": "
              + identity + ": " + response);
        }
        return response;
      }
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Connector " + connectorName + " not found", e);
    } catch (RepositoryLoginException e) {
      LOGGER.log(Level.WARNING, "Authentication failed for connector "
                 + connectorName + ": " + identity , e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Authentication failed for connector "
                 + connectorName + ": " + identity, e);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Authentication failed for connector "
                 + connectorName + ": " + identity, e);
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
        LOGGER.warning("Connector " + connectorName
            + " is being asked to authorize documents but has not implemented"
            + " the AuthorizationManager interface.");
        return null;
      }
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("AUTHORIZE: " + identity + ": docids = " + docidList);
      }
      Collection<AuthorizationResponse> results =
          authzManager.authorizeDocids(docidList, identity);
      if (LOGGER.isLoggable(Level.FINE)) {
         LOGGER.fine("AUTHORIZATION: " + identity + ": authorized for "
            + results.size() + " of " + docidList.size() + " documents.");
      }
      if (LOGGER.isLoggable(Level.FINEST)) {
        for (AuthorizationResponse response : results) {
          LOGGER.finest("AUTHORIZATION: " + response.getDocid() + ": "
                        + response.getStatus());
        }
      }
      return results;
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Connector " + connectorName + " not found", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Authorization failed for connector "
                 + connectorName + ": " + identity, e);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Authorization failed for connector "
                 + connectorName + ": " + identity, e);
    }
    return null;
  }

  /* @Override */
  public InputStream getDocumentContent(String connectorName, String docid)
      throws ConnectorNotFoundException, InstantiatorException,
             RepositoryException {
    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("RETRIEVER: Retrieving content from connector "
                   + connectorName + " for document " + docid);
    }
    Retriever retriever = instantiator.getRetriever(connectorName);
    if (retriever == null) {
      // We are borked here.  This should not happen.
      LOGGER.warning("GetDocumentContent request for connector " + connectorName
                     + " that does not support the Retriever interface.");
      return null;
    }
    InputStream in = retriever.getContent(docid);
    if (in == null) {
      LOGGER.finer("RETRIEVER: Document has no content.");
    }
    // The GSA can't handle meta-and-url feeds with no content, so we
    // provide some minimal content of a single space, if none is available.
    // We are only detecting empty content here, not large documents.
    // TODO: Figure out how to handle CONTENT morphing document filters.
    return
        new AlternateContentFilterInputStream(
            new BigEmptyDocumentFilterInputStream(
                (in == null) ? in : new EofFilterInputStream(in),
                Long.MAX_VALUE),
            null);
  }

  /* @Override */
  public Document getDocumentMetaData(String connectorName, String docid)
      throws ConnectorNotFoundException, InstantiatorException,
             RepositoryException {
    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("RETRIEVER: Retrieving metadata from connector "
                   + connectorName + " for document " + docid);
    }
    Retriever retriever = instantiator.getRetriever(connectorName);
    if (retriever == null) {
      // We are borked here.  This should not happen.
      LOGGER.warning("GetDocumentMetaData request for connector "
                     + connectorName
                     + " that does not support the Retriever interface.");
      return null;
    }
    Document metaDoc = retriever.getMetaData(docid);
    if (metaDoc == null) {
      LOGGER.finer("RETRIEVER: Document has no metadata.");
      // TODO: Create empty Document?
    } else {
      if (documentFilterFactoryFactory != null) {
        DocumentFilterFactory documentFilterFactory = 
          documentFilterFactoryFactory.getDocumentFilterFactory(connectorName);
        metaDoc = documentFilterFactory.newDocumentFilter(metaDoc);
      }

      // GSA 7.0 does not support case-sensitivity or namespaces in ACLs
      // during crawl-time. So we have to send the ACLs at feed-time.
      // But the crawl-time metadata overwrites the feed-time ACLs.
      // The proposed escape is to send a named resource ACL in the feed for
      // each document, and at crawl-time return an empty ACL that inherits
      // from the corresponding named resource ACL.
      if (feedConnection.supportsInheritedAcls()
          && DocUtils.hasAclProperties(metaDoc)) {
        metaDoc = new InheritFromExtractedAclDocumentFilter()
            .newDocumentFilter(metaDoc);
      }

      // Configure the dynamic ACL transformation filters for the documents.
      // TODO(bmj): Is FeedType.CONTENTURL a reasonable assumption here?
      AclTransformFilter aclTransformFilter = new AclTransformFilter(
          feedConnection,
          new UrlConstructor(connectorName, FeedType.CONTENTURL));
      metaDoc = aclTransformFilter.newDocumentFilter(metaDoc);
    }
    return metaDoc;
  }

  /* @Override */
  public ConfigureResponse getConfigForm(String connectorTypeName,
      String language)
      throws ConnectorTypeNotFoundException, InstantiatorException {
    ConnectorType connectorType =
        instantiator.getConnectorType(connectorTypeName);
    Locale locale = I18NUtil.getLocaleFromStandardLocaleString(language);
    if (LOGGER.isLoggable(Level.CONFIG)) {
      LOGGER.config("GET CONFIG FORM: Fetching configuration form for connector"
                  + " type " + connectorTypeName + ", locale = " + locale);
    }
    ConfigureResponse response;
    try {
      response = connectorType.getConfigForm(locale);
    } catch (Exception e) {
      throw new InstantiatorException("Failed to get configuration form.", e);
    }

    // Include the connectorInstance.xml in the response.
    if (response != null) {
      response = new ExtendedConfigureResponse(response,
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
    if (LOGGER.isLoggable(Level.CONFIG)) {
      LOGGER.config("GET CONFIG FORM: Fetching configuration form for "
                    + "connector " + connectorName + ", locale = " + locale);
    }
    ConfigureResponse response = instantiator.getConfigFormForConnector(
         connectorName, connectorTypeName, locale);
    return response;
  }

  /* @Override */
  public ConnectorStatus getConnectorStatus(String connectorName)
      throws ConnectorNotFoundException {
    String connectorTypeName = instantiator.getConnectorTypeName(connectorName);
    Schedule schedule = instantiator.getConnectorSchedule(connectorName);
    Configuration config = getConnectorConfiguration(connectorName);
    String globalNamespace = null;
    String localNamespace = null;
    if (config != null) {
      Map<String, String> configData = config.getMap();
      globalNamespace = configData.get(PropertiesUtils.GOOGLE_GLOBAL_NAMESPACE);
      localNamespace = configData.get(PropertiesUtils.GOOGLE_LOCAL_NAMESPACE);
    }
    // Note: We do not return a null or empty Schedule, as most GSAs cannot
    // handle it.
    // TODO: resolve the third parameter - we need to give status a meaning
    return new ConnectorStatus(connectorName, connectorTypeName, 0,
        Schedule.toString(schedule), globalNamespace, localNamespace);
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
      String feederGateHost, int feederGatePort, int feederGateSecurePort,
      String connectorManagerUrl) throws PersistentStoreException {
    try {
      Context.getInstance().setConnectorManagerConfig(feederGateProtocol,
          feederGateHost, feederGatePort, feederGateSecurePort,
          connectorManagerUrl);
    } catch (InstantiatorException e) {
      throw new PersistentStoreException(e);
    }
  }

  /* @Override */
  public void setSchedule(String connectorName, String schedule)
      throws ConnectorNotFoundException, PersistentStoreException {
    instantiator.setConnectorSchedule(connectorName, Schedule.of(schedule));
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
