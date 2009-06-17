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
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.persist.ConnectorConfigStore;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorScheduleStore;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.GenerationalStateStore;
import com.google.enterprise.connector.persist.MockConnectorConfigStore;
import com.google.enterprise.connector.persist.MockConnectorScheduleStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.scheduler.Scheduler;
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
  private final ConnectorConfigStore configStore;
  private final ConnectorScheduleStore scheduleStore;
  private final ConnectorStateStore stateStore;

  private Scheduler scheduler;

  public MockInstantiator() {
    this(new MockConnectorConfigStore(), new MockConnectorScheduleStore(),
         new MockConnectorStateStore());
  }

  public MockInstantiator(ConnectorConfigStore configStore,
      ConnectorScheduleStore schedStore, ConnectorStateStore stateStore) {
    this.configStore = configStore;
    this.scheduleStore = schedStore;
    this.stateStore = stateStore;
    this.connectorMap = new HashMap<String, ConnectorCoordinator>();
  }

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public synchronized void shutdown() {
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
        new ConnectorInterfaces(traverserName, traverser,
            nullAuthenticationManager, nullAuthorizationManager);
    GenerationalStateStore gStateStore =
        new GenerationalStateStore(stateStore, storeContext);
    ConnectorCoordinator cc =
        new MockConnectorCoordinator(traverserName, interfaces, gStateStore,
            configStore, scheduleStore, storeContext);
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
    Pusher pusher = new MockPusher(System.out);
    QueryTraverser queryTraverser =
        new QueryTraverser(pusher, traversalManager,
                           new MockTraversalStateStore(connectorName),
                           connectorName);

    setupTraverser(connectorName, queryTraverser);
  }

  public ConnectorType getConnectorType(String connectorTypeName) {
    throw new UnsupportedOperationException();
  }

  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getTraverser();
  }

  /**
   * Creates a TraversalStateStore for the connector instance.
   *
   * @return a new TraversalStateStore
   */
  public TraversalStateStore getTraversalStateStore(String connectorName) {
    return new MockTraversalStateStore(connectorName);
  }

  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    ConnectorCoordinator cc = connectorMap.get(connectorName);
    if (scheduler != null) {
      scheduler.removeConnector(connectorName);
    }
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
      if (scheduler != null) {
        scheduler.removeConnector(connectorName);
      }
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

    getConnectorCoordinator(connectorName).storeTraversalState(state);
  }

  public String getConnectorState(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getTraversalState();
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
  /**
   * TraversalStateStore implementation used by the Traverser to
   * maintain state between batches.
   */
  private class MockTraversalStateStore implements TraversalStateStore {
    private final GenerationalStateStore store;
    private final StoreContext storeContext;

    public MockTraversalStateStore(String connectorName) {
      this.storeContext = new StoreContext(connectorName);
      this.store = new GenerationalStateStore(stateStore, storeContext);
    }

     public void storeTraversalState(String state) {
      if (state == null) {
        store.removeConnectorState(storeContext);
      } else {
        store.storeConnectorState(storeContext, state);
      }
    }

    public String getTraversalState() {
      return store.getConnectorState(storeContext);
    }
  }
}
