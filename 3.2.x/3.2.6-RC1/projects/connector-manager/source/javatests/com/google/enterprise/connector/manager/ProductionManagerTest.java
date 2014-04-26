// Copyright 2011 Google Inc.
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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.ThreadPool;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.MockFeedConnection;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.MockConnector;
import com.google.enterprise.connector.spi.MockRetriever;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.traversal.TraversalStateStore;
import com.google.enterprise.connector.util.SystemClock;

import junit.framework.TestCase;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tests for {@link ProductionManager}. */
public class ProductionManagerTest extends TestCase {

  ProductionManager manager;
  MockInstantiator instantiator;
  AuthenticationIdentity identity;
  String connectorName;

  protected void setUp() throws Exception {
    ThreadPool threadPool = new ThreadPool(5, new SystemClock());
    instantiator = new MockInstantiator(threadPool);
    instantiator.setupTestTraversers();
    manager = new ProductionManager();
    manager.setInstantiator(instantiator);
    manager.setFeedConnection(new MockFeedConnection());
    connectorName = MockInstantiator.TRAVERSER_NAME1;
    identity = new SimpleAuthenticationIdentity("bar");
  }

  /** Test authenticate() with no AuthenticationManager. */
  public void testAuthenticateNoAuthenticationManger() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, null, null));
    AuthenticationResponse response =
        manager.authenticate(connectorName, identity);
    assertNotNull(response);
    assertFalse(response.isValid());
    assertNull(response.getData());
    assertNull(response.getGroups());
  }

  /** Test authenticate() with success response. */
  public void testAuthenticateSuccess() throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null,
        new AuthenticatingAuthenticationManager(), null, null, null));
    AuthenticationResponse response =
        manager.authenticate(connectorName, identity);
    assertNotNull(response);
    assertTrue(response.isValid());
    assertEquals("bar", response.getData());
  }

  /** Test authenticate() ConnectorNotFoundException. */
  public void testAuthenticateConnectorNotFoundException() throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null,
        new AuthenticatingAuthenticationManager(), null, null, null));
    AuthenticationResponse response =
        manager.authenticate("nonexistent", identity);
    assertNotNull(response);
    assertFalse(response.isValid());
    assertNull(response.getData());
    assertNull(response.getGroups());
  }

  /** Test authenticate() throws RepositoryException. */
  public void testAuthenticateRepositoryException() throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null,
        new ExceptionalAuthenticationManager(new RepositoryException()),
        null, null, null));
    AuthenticationResponse response =
        manager.authenticate(connectorName, identity);
    assertNotNull(response);
    assertFalse(response.isValid());
    assertNull(response.getData());
    assertNull(response.getGroups());
  }

  /** Test authenticate() throws RepositoryLoginException. */
  public void testAuthenticateRepositoryLoginException() throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null,
        new ExceptionalAuthenticationManager(new RepositoryLoginException()),
        null, null, null));
    AuthenticationResponse response =
        manager.authenticate(connectorName, identity);
    assertNotNull(response);
    assertFalse(response.isValid());
    assertNull(response.getData());
    assertNull(response.getGroups());
  }

  /** Test authenticate() throws RuntimeException. */
  public void testAuthenticateRuntimeException() throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null,
        new ExceptionalAuthenticationManager(null), null, null, null));
    AuthenticationResponse response = manager.authenticate(
        connectorName, identity);
    assertNotNull(response);
    assertFalse(response.isValid());
    assertNull(response.getData());
    assertNull(response.getGroups());
  }

  /** AuthenticationManager that authenticates anybody. */
  private static class AuthenticatingAuthenticationManager
      implements AuthenticationManager {
    public AuthenticationResponse authenticate(AuthenticationIdentity id) {
      return new AuthenticationResponse(true, id.getUsername());
    }
  }

  /** AuthenticationManager that throws the given exception. */
  private static class ExceptionalAuthenticationManager
      implements AuthenticationManager {
    RepositoryException re;
    public ExceptionalAuthenticationManager(RepositoryException re) {
      this.re = re;
    }
    public AuthenticationResponse authenticate(AuthenticationIdentity id)
        throws RepositoryException {
      if (re == null) {
        throw new RuntimeException("testing");
      }
      throw re;
    }
  }

  /** Test authorizeDocids with no AuthorizationManager. */
  public void testAuthorizeDocidsNoAuthorizationManager() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, null, null));
    List<String> docids = Arrays.asList(new String[] { "foo", "bar", "baz" });
    assertNull(manager.authorizeDocids(connectorName, docids, identity));
  }

  /** Test authorizeDocids with Exception throwing AuthorizationManager. */
  public void testAuthorizeDocidsExceptionalAuthorizationManager()
      throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null, null,
        new ExceptionalAuthorizationManager(), null, null));
    List<String> docids = Arrays.asList(new String[] { "foo", "bar", "baz" });
    assertNull(manager.authorizeDocids(connectorName, docids, identity));
  }

  /** Test authorizeDocids with ConnectorNotFound. */
  public void testAuthorizeDocidsConnectorNotFound() throws Exception {
    List<String> docids = Arrays.asList(new String[] { "foo", "bar", "baz" });
    assertNull(manager.authorizeDocids("nonexistent", docids, identity));
  }

  /** Test authorizeDocids. */
  public void testAuthorizeDocids() throws Exception {
    instantiator.addConnector(connectorName, new MockConnector(null, null,
        new AuthorizeAllAuthorizationManager(), null, null));
    List<String> docids = Arrays.asList(new String[] { "foo", "bar", "baz" });
    Collection<AuthorizationResponse> authorized =
        manager.authorizeDocids(connectorName, docids, identity);

    assertNotNull(authorized);
    assertFalse(authorized.isEmpty());
    for (String docid : docids) {
      checkContainsDocid(authorized, docid);
    }
  }

  /** Check if the Collection of AuthorizationResponses contains a response
   *  for docid.
   */
  private boolean checkContainsDocid(
      Collection<AuthorizationResponse> authorized, String docid) {
    for (AuthorizationResponse response : authorized) {
      if (response.getDocid().equals(docid)) {
        return true;
      }
    }
    return false;
  }

  /** AuthorizationManager that authorizes all docs. */
  private static class AuthorizeAllAuthorizationManager
      implements AuthorizationManager {
    public Collection<AuthorizationResponse> authorizeDocids(
        Collection<String> docids, AuthenticationIdentity identity) {
      HashSet<AuthorizationResponse> response =
          new HashSet<AuthorizationResponse>();
      for (String docid : docids) {
        response.add(new AuthorizationResponse(true, docid));
      }
      return response;
    }
  }

  /** AuthorizationManager that throws RepositoryException. */
  private static class ExceptionalAuthorizationManager
      implements AuthorizationManager {
    public Collection<AuthorizationResponse> authorizeDocids(
        Collection<String> docids, AuthenticationIdentity identity)
        throws RepositoryException {
      throw new RepositoryException();
    }
  }

  /** Test getDocumentContent with ConnectorNotFound. */
  public void testGetDocumentContentConnectorNotFound() throws Exception {
    try {
      manager.getDocumentContent("nonexistent", "docid");
      fail("Expected ConnectorNotFoundException");
    } catch (ConnectorNotFoundException expected) {
      // Expected.
    }
  }

  /** Test getDocumentContent with no Retriever. */
  public void testGetDocumentContentNoRetriever() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, null, null));
    assertNull(manager.getDocumentContent(connectorName, "docid"));
  }

  /** Test getDocumentContent with Document NotFound. */
  public void testGetDocumentContentDocumentNotFound() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    try {
      manager.getDocumentContent(connectorName, MockRetriever.DOCID_NOT_FOUND);
      fail("Expected RepositoryDocumentException");
    } catch (RepositoryDocumentException expected) {
      // Expected.
    }
  }

  /** Test getDocumentContent with RepositoryException. */
  public void testGetDocumentContentRepositoryException() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    try {
      manager.getDocumentContent(connectorName,
                                 MockRetriever.DOCID_REPOSITORY_EXCEPTION);
      fail("Expected RepositoryException");
    } catch (RepositoryException expected) {
      // Expected.
    }
  }

  /** Test getDocumentContent where document has no content. */
  public void testGetDocumentContentNoContent() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    InputStream in = manager.getDocumentContent(connectorName,
                                                MockRetriever.DOCID_NO_CONTENT);
    // GSA still doesn't handle docs with no content, so the
    // Production Manager substitutes a single space.
    assertNotNull(in);
    assertEquals(" ", StringUtils.streamToString(in));
  }

  /** Test getDocumentContent where document has empty content. */
  public void testGetDocumentContentEmptyContent() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    InputStream in = manager.getDocumentContent(connectorName,
                                                MockRetriever.DOCID_NO_CONTENT);
    // GSA still doesn't handle docs with no content, so the
    // Production Manager substitutes a single space.
    assertNotNull(in);
    assertEquals(" ", StringUtils.streamToString(in));
  }

  /** Test getDocumentContent. */
  public void testGetDocumentContent() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    InputStream in = manager.getDocumentContent(connectorName, "docid");
    assertNotNull(in);
    assertEquals("docid", StringUtils.streamToString(in));
  }

  /** Test getDocumentMetaData with ConnectorNotFound. */
  public void testGetDocumentMetaDataConnectorNotFound() throws Exception {
    try {
      manager.getDocumentMetaData("nonexistent", "docid");
      fail("Expected ConnectorNotFoundException");
    } catch (ConnectorNotFoundException expected) {
      // Expected.
    }
  }

  /** Test getDocumentMetaData with no Retriever. */
  public void testGetDocumentMetaDataNoRetriever() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, null, null));
    assertNull(manager.getDocumentMetaData(connectorName, "docid"));
  }

  /** Test getDocumentMetaData with Document NotFound. */
  public void testGetDocumentMetaDataDocumentNotFound() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    try {
      manager.getDocumentMetaData(connectorName, MockRetriever.DOCID_NOT_FOUND);
      fail("Expected RepositoryDocumentException");
    } catch (RepositoryDocumentException expected) {
      // Expected.
    }
  }

  /** Test getDocumentMetaData with RepositoryException. */
  public void testGetDocumentMetaDataRepositoryException() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    try {
      manager.getDocumentMetaData(connectorName,
                                  MockRetriever.DOCID_REPOSITORY_EXCEPTION);
      fail("Expected RepositoryException");
    } catch (RepositoryException expected) {
      // Expected.
    }
  }

  /** Test getDocumentMetaData. */
  public void testGetDocumentMetaData() throws Exception {
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever(), null));
    Document document = manager.getDocumentMetaData(connectorName, "docid");
    assertNotNull(document);
    assertEquals("docid",
        Value.getSingleValueString(document, SpiConstants.PROPNAME_DOCID));
  }

  /** Test getConnectorStatus. */
  public void testGetConnectorStatus() throws Exception {
    ConnectorStatus status = manager.getConnectorStatus(connectorName);
    assertNotNull(status);
    assertEquals(connectorName, status.getName());
    assertEquals(0, status.getStatus());
    assertEquals(instantiator.getConnectorTypeName(connectorName),
                 status.getType());
    assertNotNull(status.getSchedule());
    assertTrue(status.getSchedule().startsWith("#"));

    Schedule sched = new Schedule(connectorName, false, 200, 1000, "1-2");
    String schedStr = sched.toString();
    manager.setSchedule(connectorName, schedStr);
    status = manager.getConnectorStatus(connectorName);
    assertNotNull(status);
    assertEquals(schedStr, status.getSchedule());

    try {
      status = manager.getConnectorStatus("nonexistent");
      fail("Expected ConnectorNotFoundException");
    } catch (ConnectorNotFoundException expected) {
      // Expected
    }
  }

  /** Test getConnectorStatuses. */
  public void testGetConnectorStatuses() throws Exception {
    List<ConnectorStatus> statuses = manager.getConnectorStatuses();
    assertNotNull(statuses);
    assertFalse(statuses.isEmpty());
  }

  /** Test getConnectorTypeNames. */
  public void testGetConnectorTypeNames() throws Exception {
    Set<String> typeNames = manager.getConnectorTypeNames();
    assertNotNull(typeNames);
    assertFalse(typeNames.isEmpty());
    assertEquals(instantiator.getConnectorTypeNames(), typeNames);
  }

  /** Test getConnectorType. */
  public void testGetConnectorType() throws Exception {
    String typeName = instantiator.getConnectorTypeName(connectorName);
    ConnectorType type = manager.getConnectorType(typeName);
    assertNotNull(type);
    assertEquals(instantiator.getConnectorType(connectorName), type);
  }

  /** Test getConfigForm. */
  public void testGetConfigForm() throws Exception {
    String typeName = instantiator.getConnectorTypeName(connectorName);
    ConfigureResponse response = manager.getConfigForm(typeName, "en");
    assertNotNull(response);
    assertTrue(response.getMessage().contains(typeName));
  }

  /** Test getConfigFormForConnector. */
  public void testGetConfigFormForConnector() throws Exception {
    ConfigureResponse response =
        manager.getConfigFormForConnector(connectorName, "en");
    assertNotNull(response);
  }

  /** Test setConnectorConfiguration and getConnctorConfiguration. */
  public void testSetAndGetConnectorConfiguration() throws Exception {
    String typeName = instantiator.getConnectorTypeName(connectorName);
    Map<String, String> config = new HashMap<String, String>();
    config.put(PropertiesUtils.GOOGLE_CONNECTOR_NAME, connectorName);

    Configuration configuration = new Configuration(typeName, config, null);
    ConfigureResponse response = manager.setConnectorConfiguration(
        connectorName, configuration, "en", true);
    assertNull(response);

    assertEquals(instantiator.getConnectorConfiguration(connectorName),
                 manager.getConnectorConfiguration(connectorName));
  }

  /** Test setConnectorSchedule and getConnectorSchedule. */
  public void testSetAndGetConnectorSchedule() throws Exception {
    Schedule sched = new Schedule(connectorName, false, 200, 1000, "1-2");
    assertFalse(sched.equals(instantiator.getConnectorSchedule(connectorName)));
    manager.setSchedule(connectorName, sched.toString());
    assertEquals(sched, instantiator.getConnectorSchedule(connectorName));
    manager.setSchedule(connectorName, null);
    assertNull(instantiator.getConnectorSchedule(connectorName));
  }

  /** Test restartConnectorTraversal. */
  public void testRestartTraversal() throws Exception {
    TraversalStateStore store =
        instantiator.getTraversalStateStore(connectorName);
    store.storeTraversalState("checkpoint");
    assertEquals("checkpoint", store.getTraversalState());
    manager.restartConnectorTraversal(connectorName);
    assertNull(store.getTraversalState());
  }

  /** Test removeConnector. */
  public void testRemoveConnector() throws Exception {
    String typeName = instantiator.getConnectorTypeName(connectorName);
    Map<String, String> config = new HashMap<String, String>();
    config.put(PropertiesUtils.GOOGLE_CONNECTOR_NAME, connectorName);

    Configuration configuration = new Configuration(typeName, config, null);
    ConfigureResponse response = manager.setConnectorConfiguration(
        connectorName, configuration, "en", true);
    assertNull(response);

    manager.getConnectorConfiguration(connectorName);
    assertEquals(instantiator.getConnectorConfiguration(connectorName),
                 configuration);

    manager.removeConnector(connectorName);
    try {
      manager.getConnectorConfiguration(connectorName);
      fail("Expected ConnectorNotFoundException");
    } catch (ConnectorNotFoundException expected) {
      // Expected.
    }
  }
}


