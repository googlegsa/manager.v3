// Copyright 2009 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.jcr.JcrConnector;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.MockPersistentStore;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.CancellableQueryTraverser;
import com.google.enterprise.connector.traversal.InterruptibleQueryTraverser;
import com.google.enterprise.connector.traversal.LongRunningQueryTraverser;
import com.google.enterprise.connector.traversal.NeverEndingQueryTraverser;
import com.google.enterprise.connector.traversal.NoopQueryTraverser;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.TraversalStateStore;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Repository;

/**
 * Mock implementation of {@link Instantiator} that comes with support for
 * adding predefined connectors {@link #setupTestTraversers()} and support
 * for clients to add there own using {@link #setupConnector(String, String)}
 * or {@link #setupTraverser(String, Traverser)};
 */
public class MockInstantiator implements Instantiator {
  public static final String TRAVERSER_NAME1 = "foo";
  public static final String TRAVERSER_NAME2 = "bar";
  public static final String TRAVERSER_NAME_NOOP = "noop";
  public static final String TRAVERSER_NAME_LONG_RUNNING = "longrunning";
  public static final String TRAVERSER_NAME_NEVER_ENDING = "neverending";
  public static final String TRAVERSER_NAME_INTERRUPTIBLE = "interruptible";
  public static final String TRAVERSER_NAME_CANCELLABLE = "cancellable";

  private static final AuthenticationManager nullAuthenticationManager =
      new AuthenticationManager() {
        public AuthenticationResponse authenticate(
            AuthenticationIdentity identity) {
          throw new UnsupportedOperationException();
        }
      };

  private static final AuthorizationManager nullAuthorizationManager =
      new AuthorizationManager() {
        public Collection<AuthorizationResponse> authorizeDocids(
            Collection<String> docids, AuthenticationIdentity identity) {
          throw new UnsupportedOperationException();
        }
      };

  private final Map<String, ConnectorCoordinator> connectorMap;
  private final PersistentStore persistentStore;
  private final ThreadPool threadPool;

  public MockInstantiator(ThreadPool threadPool) {
    this(new MockPersistentStore(), threadPool);
  }

  public MockInstantiator(PersistentStore persistentStore,
      ThreadPool threadPool) {
    this.persistentStore = persistentStore;
    this.connectorMap = new HashMap<String, ConnectorCoordinator>();
    this.threadPool = threadPool;
  }

  public synchronized void shutdown(boolean interrupt, long timeoutMillis) {
    for (Map.Entry<String, ConnectorCoordinator> e : connectorMap.entrySet()) {
      e.getValue().shutdown();
    }
    connectorMap.clear();
  }

  /**
   * Creates and registers a suite of test connectors with this
   * {@link Instantiator}.
   */
  public void setupTestTraversers() {
    setupConnector(TRAVERSER_NAME1, "MockRepositoryEventLog1.txt");
    setupConnector(TRAVERSER_NAME2, "MockRepositoryEventLog1.txt");
    setupTraverser(TRAVERSER_NAME_NOOP, new NoopQueryTraverser());
    setupTraverser(TRAVERSER_NAME_LONG_RUNNING,
        new LongRunningQueryTraverser());
    setupTraverser(TRAVERSER_NAME_NEVER_ENDING,
        new NeverEndingQueryTraverser());
    setupTraverser(TRAVERSER_NAME_INTERRUPTIBLE,
        new InterruptibleQueryTraverser());
    setupTraverser(TRAVERSER_NAME_CANCELLABLE,
        new CancellableQueryTraverser());
  }

  /**
   * Creates and registers a {@link Connector} for the provided
   * {@link Traverser} with this {@link Instantiator}.
   */
  public void setupTraverser(String traverserName, Traverser traverser) {
    StoreContext storeContext = new StoreContext(traverserName);
    ConnectorInterfaces interfaces =
        new ConnectorInterfaces(traverserName, null, nullAuthenticationManager,
            nullAuthorizationManager);
    ConnectorCoordinator cc =
        new MockConnectorCoordinator(traverserName, interfaces, traverser,
            persistentStore, storeContext, threadPool);
    connectorMap.put(traverserName, cc);
  }

  /**
   * Creates a {@link JcrConnector} with a backing {@link MockRepository} and
   * registers the created connector with this {@link Instantiator}.
   */
  public void setupConnector(String connectorName, String resourceName) {
    MockRepositoryEventList mrel = new MockRepositoryEventList(resourceName);
    MockRepository mockRepository = new MockRepository(mrel);
    Repository repository = new MockJcrRepository(mockRepository);
    Connector connector = new JcrConnector(repository);

    TraversalManager traversalManager;
    try {
      Session session = connector.login();
      traversalManager = session.getTraversalManager();
    } catch (Exception e) {
      // won't happen
      e.printStackTrace();
      throw new RuntimeException();
    }
    QueryTraverser queryTraverser =
        new QueryTraverser(new MockPusher(), traversalManager,
            new MockTraversalStateStore(persistentStore, connectorName),
            connectorName, Context.getInstance().getTraversalContext());

    setupTraverser(connectorName, queryTraverser);
  }

  public ConnectorType getConnectorType(String connectorTypeName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a TraversalStateStore for the connector instance.
   *
   * @return a new TraversalStateStore
   */
  public TraversalStateStore getTraversalStateStore(String connectorName) {
    return new MockTraversalStateStore(persistentStore, connectorName);
  }

  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    ConnectorCoordinator cc = connectorMap.get(connectorName);
    cc.restartConnectorTraversal();
  }

  public String getConnectorInstancePrototype(String connectorTypeName) {
    return "";
  }

  public Set<String> getConnectorTypeNames() {
    return connectorMap.keySet();
  }

  public void removeConnector(String connectorName) {
    ConnectorCoordinator cc = connectorMap.remove(connectorName);
    if (cc != null) {
      cc.removeConnector();
    }
  }

  /**
   * Returns an {@Link AuthenticationManager} that throws
   * {@link UnsupportedOperationException} for all
   * {@link AuthenticationManager#authenticate(AuthenticationIdentity)}
   * calls.
   */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthenticationManager();
  }

  /**
   * Returns an {@Link AuthorizationManager} that
   * {@link UnsupportedOperationException} for all
   * {@link AuthorizationManager#authorizeDocids(Collection,
   * AuthenticationIdentity)} calls.
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthorizationManager();
  }

  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale) {
    throw new UnsupportedOperationException();
  }

  public Set<String> getConnectorNames() {
    return connectorMap.keySet();
  }

  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorTypeName();
  }

  public ConfigureResponse setConnectorConfig(String connectorName,
      String typeName, Map<String, String> configKeys, Locale locale,
      boolean update) throws ConnectorNotFoundException,
      ConnectorExistsException, InstantiatorException {
    ConnectorCoordinator cc = getConnectorCoordinator(connectorName);
    if (!cc.getConnectorTypeName().equals(typeName)) {
      throw new UnsupportedOperationException(
          "MockInstantiator does not support changing a connectors type");
    }
    return cc.setConnectorConfig(null, configKeys, locale, update);
  }

  public Map<String, String> getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorConfig();
  }

  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).setConnectorSchedule(
        connectorSchedule);
  }

  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorSchedule();
  }

  public void setConnectorState(String connectorName, String state)
      throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).setConnectorState(state);
  }

  public String getConnectorState(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorState();
  }

  public ConnectorCoordinator getConnectorCoordinator(String connectorName)
      throws ConnectorNotFoundException {
    if (connectorMap.containsKey(connectorName)) {
      return connectorMap.get(connectorName);
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }
}
