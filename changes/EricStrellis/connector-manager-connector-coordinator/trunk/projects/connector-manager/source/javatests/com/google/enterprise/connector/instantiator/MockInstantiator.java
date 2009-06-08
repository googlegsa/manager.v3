// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.jcr.JcrConnector;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorConfigStore;
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
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.CancellableQueryTraverser;
import com.google.enterprise.connector.traversal.InterruptibleQueryTraverser;
import com.google.enterprise.connector.traversal.LongRunningQueryTraverser;
import com.google.enterprise.connector.traversal.NeverEndingQueryTraverser;
import com.google.enterprise.connector.traversal.NoopQueryTraverser;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;
import com.google.enterprise.connector.traversal.TraversalStateStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Repository;
import javax.naming.OperationNotSupportedException;

/**
 *
 */
public class MockInstantiator implements Instantiator {
  public static final String TRAVERSER_NAME1 = "foo";
  public static final String TRAVERSER_NAME2 = "bar";
  public static final String TRAVERSER_NAME_NOOP = "noop";
  public static final String TRAVERSER_NAME_LONG_RUNNING = "longrunning";
  public static final String TRAVERSER_NAME_NEVER_ENDING = "neverending";
  public static final String TRAVERSER_NAME_INTERRUPTIBLE = "interruptible";
  public static final String TRAVERSER_NAME_CANCELLABLE = "cancellable";

  private static final ConnectorType CONNECTOR_TYPE = null;

  private static final Logger LOGGER =
      Logger.getLogger(MockInstantiator.class.getName());

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

  private final Map<String, ConnectorHolder> connectorMap;
  private final ConnectorConfigStore connectorConfigStore;
  private final ConnectorScheduleStore connectorScheduleStore;
  private final ConnectorStateStore connectorStateStore;

  private Scheduler scheduler;

  public MockInstantiator() {
    this(new MockConnectorConfigStore(), new MockConnectorScheduleStore(),
         new MockConnectorStateStore());
  }

  public MockInstantiator(ConnectorConfigStore configStore,
      ConnectorScheduleStore schedStore, ConnectorStateStore stateStore) {
    this.connectorConfigStore = configStore;
    this.connectorScheduleStore = schedStore;
    this.connectorStateStore = stateStore;
    this.connectorMap = new HashMap<String, ConnectorHolder>();
  }

  /* Setter Injectors */
  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public synchronized void shutdown() {
    for (String name : connectorMap.keySet()) {
      ConnectorHolder instance = connectorMap.get(name);
      Connector connector = instance.getConnectorInterfaces().getConnector();
      if (connector != null && (connector instanceof ConnectorShutdownAware)) {
        try {
          ((ConnectorShutdownAware)connector).shutdown();
        } catch (RepositoryException e) {
          LOGGER.log(Level.WARNING, "Problem shutting down connector "
                     + name, e);
        }
      }
    }
    connectorMap.clear();
  }

  public void setupTestTraversers() {
    setupConnector(TRAVERSER_NAME1, "MockRepositoryEventLog1.txt");
    setupConnector(TRAVERSER_NAME2, "MockRepositoryEventLog1.txt");
    setupTraverser(TRAVERSER_NAME_NOOP, new NoopQueryTraverser());
    setupTraverser(TRAVERSER_NAME_LONG_RUNNING, new LongRunningQueryTraverser());
    setupTraverser(TRAVERSER_NAME_NEVER_ENDING, new NeverEndingQueryTraverser());
    setupTraverser(TRAVERSER_NAME_INTERRUPTIBLE, new InterruptibleQueryTraverser());
    setupTraverser(TRAVERSER_NAME_CANCELLABLE, new CancellableQueryTraverser());
  }

  public void setupTraverser(String traverserName, Traverser traverser) {
    connectorMap.put(traverserName, new ConnectorHolder(
        new ConnectorInterfaces(traverserName, traverser,
            nullAuthenticationManager, nullAuthorizationManager),
        connectorStateStore));
  }

  public void setupConnector(String connectorName, String resourceName) {
    MockRepositoryEventList mrel = new MockRepositoryEventList(resourceName);
    MockRepository mockRepository = new MockRepository(mrel);
    Repository repository = new MockJcrRepository(mockRepository);
    Connector connector = new JcrConnector(repository);

    TraversalManager traversalManager;
    AuthenticationManager authenticationManager;
    AuthorizationManager authorizationManager;
    try {
      Session session = connector.login();
      traversalManager = session.getTraversalManager();
      authenticationManager = session.getAuthenticationManager();
      authorizationManager = session.getAuthorizationManager();
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

    connectorMap.put(connectorName, new ConnectorHolder(
        new ConnectorInterfaces(connectorName, queryTraverser,
            authenticationManager, authorizationManager),
        connectorStateStore));
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConfigurer(java.lang.String)
   */
  public ConnectorType getConnectorType(String connectorTypeName) {
    return CONNECTOR_TYPE;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getTraverser(java.lang.String)
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorHolder(connectorName)
        .getConnectorInterfaces().getTraverser();
  }

  /**
   * Creates a TraversalStateStore for the connector instance.
   *
   * @return a new TraversalStateStore
   */
  public TraversalStateStore getTraversalStateStore(String connectorName) {
    return new MockTraversalStateStore(connectorName);
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
      this.store = new GenerationalStateStore(connectorStateStore,
                                              storeContext);
    }

    /**
     * Store traversal state.
     *
     * @param state a String representation of the state to store.
     *        If null, any previous stored state is discarded.
     * @throws IllegalStateException if the store is no longer valid.
     */
    public void storeTraversalState(String state) {
      if (state == null) {
        store.removeConnectorState(storeContext);
      } else {
        store.storeConnectorState(storeContext, state);
      }
    }

    /**
     * Return a stored traversal state.
     *
     * @returns String representation of the stored state, or
     *          null if no state is stored.
     * @throws IllegalStateException if the store is no longer valid.
     */
    public String getTraversalState() {
      return store.getConnectorState(storeContext);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #restartConnectorTraversal(java.lang.String)
   */
  public void restartConnectorTraversal(String connectorName) {
    ConnectorHolder inst = connectorMap.get(connectorName);
    if (scheduler != null) {
      scheduler.removeConnector(connectorName);
    }
    inst.getStateStore().removeConnectorState(inst.getStoreContext());
  }

  public String getConnectorInstancePrototype(String connectorTypeName) {
    return "";
  }

  public Set<String> getConnectorTypeNames() {
    return connectorMap.keySet();
  }

  public void removeConnector(String connectorName) {
    ConnectorHolder inst = connectorMap.remove(connectorName);
    if (inst != null) {
      StoreContext context = inst.getStoreContext();
      inst.getStateStore().removeConnectorState(context);
      connectorScheduleStore.removeConnectorSchedule(context);
      connectorConfigStore.removeConnectorConfiguration(context);
      if (scheduler != null) {
        scheduler.removeConnector(connectorName);
      }
    }
  }

  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces(connectorName).getAuthenticationManager();
  }

  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces(connectorName).getAuthorizationManager();
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
    return getConnectorHolder(connectorName).getTypeName();
  }

  public ConfigureResponse setConnectorConfig(String connectorName,
      String typeName, Map<String, String> configKeys, Locale locale,
      boolean update) throws ConnectorNotFoundException {
    getConnectorHolder(connectorName).setTypeName(typeName);
    connectorConfigStore.storeConnectorConfiguration(
        getConnectorHolder(connectorName).getStoreContext(),
        PropertiesUtils.fromMap(configKeys));
    return null;
  }

  public Map<String, String> getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException {
    Properties props = connectorConfigStore.getConnectorConfiguration(
        getConnectorHolder(connectorName).getStoreContext());
    if (props == null) {
      return new HashMap<String, String>();
    } else {
      return PropertiesUtils.toMap(props);
    }
  }

  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    connectorScheduleStore.storeConnectorSchedule(
        getConnectorHolder(connectorName).getStoreContext(),
        connectorSchedule);
  }

  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return connectorScheduleStore.getConnectorSchedule(
        getConnectorHolder(connectorName).getStoreContext());
  }

  public void setConnectorState(String connectorName, String connectorState)
      throws ConnectorNotFoundException {
    ConnectorHolder inst = getConnectorHolder(connectorName);
    inst.getStateStore().storeConnectorState(inst.getStoreContext(),
                                             connectorState);
  }

  public String getConnectorState(String connectorName)
      throws ConnectorNotFoundException {
    ConnectorHolder inst = getConnectorHolder(connectorName);
    return inst.getStateStore().getConnectorState(inst.getStoreContext());

  }

  public ConnectorCoordinatorImpl getConnectorCoordinator(String connectorName)
      throws ConnectorNotFoundException {
    // TODO(strellis): Implement this.
    throw new UnsupportedOperationException();
  }

  private ConnectorHolder getConnectorHolder(String connectorName)
      throws ConnectorNotFoundException {
    if (connectorMap.containsKey(connectorName)) {
      return connectorMap.get(connectorName);
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }

  private ConnectorInterfaces getConnectorInterfaces(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorHolder(connectorName).getConnectorInterfaces();
  }

  static class ConnectorHolder {
    private final ConnectorInterfaces connectorInterfaces;
    private final GenerationalStateStore stateStore;
    private final StoreContext storeContext;
    private String typeName;

    public ConnectorHolder(ConnectorInterfaces connectorInterfaces,
                             ConnectorStateStore baseStore) {
      this.connectorInterfaces = connectorInterfaces;
      storeContext = new StoreContext(connectorInterfaces.getConnectorName());
      stateStore = new GenerationalStateStore(baseStore, storeContext);
      typeName = "";
    }

    public GenerationalStateStore getStateStore() {
      return stateStore;
    }

    public StoreContext getStoreContext() {
      return storeContext;
    }

    public ConnectorInterfaces getConnectorInterfaces() {
      return connectorInterfaces;
    }

    public void setTypeName(String typeName) {
      this.typeName = typeName;
    }

    public String getTypeName() {
      return typeName;
    }
  }
}