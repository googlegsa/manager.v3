// Copyright 2009 Google Inc.
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
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.MockPersistentStore;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.MockConnectorType;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.CancellableQueryTraverser;
import com.google.enterprise.connector.traversal.InterruptibleQueryTraverser;
import com.google.enterprise.connector.traversal.LongRunningQueryTraverser;
import com.google.enterprise.connector.traversal.NeverEndingQueryTraverser;
import com.google.enterprise.connector.traversal.NoopQueryTraverser;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.TraversalStateStore;
import com.google.enterprise.connector.traversal.Traverser;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;
import com.google.enterprise.connector.util.SystemClock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Repository;

/**
 * Mock implementation of {@link Instantiator} that comes with support for
 * adding predefined connectors {@link #setupTestTraversers()} and support
 * for clients to add there own using {@link #setupConnector(String, String)}
 * or {@link #setupTraverser(String, Traverser)};
 */
public class MockInstantiator implements Instantiator {
  private static final Logger LOGGER =
      Logger.getLogger(MockInstantiator.class.getName());

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
  private final TraversalContext traversalContext;

  public MockInstantiator(ThreadPool threadPool) {
    this(new MockPersistentStore(), threadPool);
  }

  public MockInstantiator(PersistentStore persistentStore,
      ThreadPool threadPool) {
    this.persistentStore = persistentStore;
    this.connectorMap = new HashMap<String, ConnectorCoordinator>();
    this.threadPool = threadPool;
    this.traversalContext = new SimpleTraversalContext();
  }

  @Override
  public synchronized void shutdown(boolean interrupt, long timeoutMillis) {
    for (Map.Entry<String, ConnectorCoordinator> e : connectorMap.entrySet()) {
      e.getValue().shutdown();
    }
    connectorMap.clear();
  }

  private StoreContext getStoreContext(String name) {
    return new StoreContext(name, "testType");
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
    StoreContext storeContext = getStoreContext(traverserName);
    ConnectorInterfaces interfaces =  new ConnectorInterfaces(traverserName,
        null, nullAuthenticationManager, nullAuthorizationManager);
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
    addConnector(connectorName, connector);
  }

  /**
   * Registers the supplied {@link Connector} with this {@link Instantiator}.
   */
  public void addConnector(String connectorName, Connector connector) {
    TraversalManager traversalManager;
    try {
      Session session = connector.login();
      traversalManager = session.getTraversalManager();
    } catch (Exception e) {
      // won't happen
      e.printStackTrace();
      throw new RuntimeException();
    }
    StoreContext storeContext = getStoreContext(connectorName);
    QueryTraverser queryTraverser =
        new QueryTraverser(new MockPusher(), traversalManager,
            new MockTraversalStateStore(persistentStore, storeContext),
            connectorName, traversalContext,
            new SystemClock() /* TODO: use a mock clock */, null);

    ConnectorInterfaces interfaces =
        new ConnectorInterfaces(connectorName, connector);
    ConnectorCoordinator cc =
        new MockConnectorCoordinator(connectorName, interfaces, queryTraverser,
            persistentStore, storeContext, threadPool);
    connectorMap.put(connectorName, cc);
  }

  @Override
  public ConnectorType getConnectorType(String connectorTypeName) {
    return new MockConnectorType(connectorTypeName);
  }

  /**
   * Creates a TraversalStateStore for the connector instance.
   *
   * @return a new TraversalStateStore
   */
  public TraversalStateStore getTraversalStateStore(String connectorName) {
    return new MockTraversalStateStore(persistentStore,
                                       getStoreContext(connectorName));
  }

  @Override
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    ConnectorCoordinator cc = connectorMap.get(connectorName);
    cc.restartConnectorTraversal();
  }

  @Override
  public String getConnectorInstancePrototype(String connectorTypeName) {
    return "";
  }

  @Override
  public Set<String> getConnectorTypeNames() {
    return connectorMap.keySet();
  }

  @Override
  public void removeConnector(String connectorName) {
    ConnectorCoordinator cc = connectorMap.remove(connectorName);
    if (cc != null) {
      cc.removeConnector();
    }
  }

  /**
   * Returns an {@Link AuthenticationManager}.
   */
  @Override
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthenticationManager();
  }

  /**
   * Returns an {@Link AuthorizationManager}.
   */
  @Override
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthorizationManager();
  }

  /** Returns a {@Link Retreiver}. */
  @Override
  public Retriever getRetriever(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getRetriever();
  }

  @Override
  public void startBatch(String connectorName)
      throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).startBatch();
  }

  @Override
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale) throws ConnectorNotFoundException
  {
    Configuration config = getConnectorConfiguration(connectorName);
    return new MockConnectorType(connectorTypeName)
        .getPopulatedConfigForm((config == null) ? null : config.getMap(),
                                locale);
  }

  @Override
  public Set<String> getConnectorNames() {
    return connectorMap.keySet();
  }

  @Override
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorTypeName();
  }

  @Override
  public ConfigureResponse setConnectorConfiguration(String connectorName,
      Configuration configuration, Locale locale, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      InstantiatorException {
    ConnectorCoordinator cc = getConnectorCoordinator(connectorName);
    if (update) {
      if (!cc.getConnectorTypeName().equals(configuration.getTypeName())) {
        throw new UnsupportedOperationException(
            "MockInstantiator does not support changing a connectors type");
      }
    }
    return cc.setConnectorConfiguration(null, configuration, locale, update);
  }

  @Override
  public Configuration getConnectorConfiguration(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorConfiguration();
  }

  @Override
  public void setConnectorSchedule(String connectorName,
      Schedule schedule) throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).setConnectorSchedule(schedule);
  }

  @Override
  public Schedule getConnectorSchedule(String connectorName)
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

  @Override
  public DocumentFilterFactory getDocumentFilterFactory(String connectorName)
      throws ConnectorNotFoundException {
    return null;
  }

  public void setGDataConfig() {
    for (String name : getConnectorNames()) {
      try {
        getConnectorCoordinator(name).setGDataConfig();
      } catch (ConnectorNotFoundException cnfe) {
        // Shouldn't happen, but if it does, skip it.
      } catch (InstantiatorException ie) {
        LOGGER.log(Level.WARNING, "", ie);
      }
    }
  }

  private ConnectorCoordinator getConnectorCoordinator(String connectorName)
      throws ConnectorNotFoundException {
    if (connectorMap.containsKey(connectorName)) {
      return connectorMap.get(connectorName);
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }
}
