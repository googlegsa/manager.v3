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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.PropertiesException;
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
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.InterruptibleQueryTraverser;
import com.google.enterprise.connector.traversal.LongRunningQueryTraverser;
import com.google.enterprise.connector.traversal.NeverEndingQueryTraverser;
import com.google.enterprise.connector.traversal.NoopQueryTraverser;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Repository;

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
  public static final String TRAVERSER_NAME_REQUESTS_MORE_TIME = "requestsMore";

  private static final ConnectorType CONNECTOR_TYPE = null;
  private static final ConnectorConfigStore connectorConfigStore =
      new MockConnectorConfigStore();
  private static final ConnectorScheduleStore connectorScheduleStore =
      new MockConnectorScheduleStore();
  private static final ConnectorStateStore connectorStateStore =
      new MockConnectorStateStore();

  private static final AuthenticationManager nullAuthenticationManager =
      new AuthenticationManager() {
        public AuthenticationResponse authenticate(
            AuthenticationIdentity identity) {
          throw new UnsupportedOperationException();
        }
      };

  private static final AuthorizationManager nullAuthorizationManager =
      new AuthorizationManager() {
        public Collection authorizeDocids(Collection docids,
            AuthenticationIdentity identity) {
          throw new UnsupportedOperationException();
        }
      };

  private Map connectorMap;

  public MockInstantiator() {

    connectorMap = new HashMap();

    setupConnector(TRAVERSER_NAME1, "MockRepositoryEventLog1.txt");
    setupConnector(TRAVERSER_NAME2, "MockRepositoryEventLog1.txt");

    setupTraverser(TRAVERSER_NAME_NOOP, new NoopQueryTraverser());
    setupTraverser(TRAVERSER_NAME_LONG_RUNNING, new LongRunningQueryTraverser());
    setupTraverser(TRAVERSER_NAME_NEVER_ENDING, new NeverEndingQueryTraverser());
    setupTraverser(TRAVERSER_NAME_INTERRUPTIBLE, new InterruptibleQueryTraverser());
  }

  public void setupTraverser(String traverserName, Traverser traverser) {
    connectorMap.put(traverserName, new ConnectorInstance(
        new ConnectorInterfaces(traverserName, traverser,
            nullAuthenticationManager, nullAuthorizationManager),
        new GenerationalStateStore(connectorStateStore, traverserName)));
  }

  public void setupConnector(String connectorName, String resourceName) {
    MockRepositoryEventList mrel = new MockRepositoryEventList(resourceName);
    MockRepository mockRepository = new MockRepository(mrel);
    Repository repository = new MockJcrRepository(mockRepository);
    Connector connector = new JcrConnector(repository);

    TraversalManager qtm;
    AuthenticationManager authenticationManager;
    AuthorizationManager authorizationManager;
    try {
      Session session = connector.login();
      qtm = session.getTraversalManager();
      authenticationManager = session.getAuthenticationManager();
      authorizationManager = session.getAuthorizationManager();
    } catch (Exception e) {
      // won't happen
      e.printStackTrace();
      throw new RuntimeException();
    }

    Pusher pusher = new MockPusher(System.out);
    QueryTraverser queryTraverser =
        new QueryTraverser(pusher, qtm, this, connectorName);

    connectorMap.put(connectorName, new ConnectorInstance(
        new ConnectorInterfaces(connectorName, queryTraverser,
                                authenticationManager, authorizationManager),
        new GenerationalStateStore(connectorStateStore, connectorName)));
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
    return getConnectorInstance(connectorName)
        .getConnectorInterfaces().getTraverser();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #restartConnectorTraversal(java.lang.String)
   */
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    getConnectorInstance(connectorName).getStateStore()
        .removeConnectorState(null, connectorName);
  }

  public String getConnectorInstancePrototype(String connectorTypeName) {
    return "";
  }

  public Iterator getConnectorTypeNames() {
    return connectorMap.keySet().iterator();
  }

  public void removeConnector(String connectorName) {
    ConnectorInstance inst = 
        (ConnectorInstance) connectorMap.remove(connectorName);
    if (inst != null) {
      inst.getStateStore().removeConnectorState(null, connectorName);
    }
    connectorScheduleStore.removeConnectorSchedule(null, connectorName);
    connectorConfigStore.removeConnectorConfiguration(null, connectorName);
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

  public Iterator getConnectorNames() {
    return connectorMap.keySet().iterator();
  }

  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorInstance(connectorName).getTypeName();
  }

  public ConfigureResponse setConnectorConfig(String connectorName,
      String typeName, Map configKeys, Locale locale, boolean update)
      throws ConnectorNotFoundException {
    getConnectorInstance(connectorName).setTypeName(typeName);
    connectorConfigStore.storeConnectorConfiguration(null, connectorName,
        PropertiesUtils.fromMap(configKeys));
    return null;
  }

  public Map getConnectorConfig(String connectorName) {
    Properties props = 
        connectorConfigStore.getConnectorConfiguration(null, connectorName);
    return (props == null) ? (Map) new HashMap() : (Map) props;
  }

  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    connectorScheduleStore.storeConnectorSchedule(null, connectorName,
                                                  connectorSchedule);
  }

  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return connectorScheduleStore.getConnectorSchedule(null, connectorName);
  }

  public void setConnectorState(String connectorName, String connectorState)
      throws ConnectorNotFoundException {
    getConnectorInstance(connectorName).getStateStore()
        .storeConnectorState(null, connectorName, connectorState);
  }

  public String getConnectorState(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorInstance(connectorName).getStateStore()
        .getConnectorState(null, connectorName);
  }

  private ConnectorInstance getConnectorInstance(String connectorName)
      throws ConnectorNotFoundException {
    if (connectorMap.containsKey(connectorName)) {
      return (ConnectorInstance) connectorMap.get(connectorName);
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }

  private ConnectorInterfaces getConnectorInterfaces(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorInstance(connectorName).getConnectorInterfaces();
  }


  static class ConnectorInstance {
    private final ConnectorInterfaces connectorInterfaces;
    private final GenerationalStateStore stateStore;
    private String typeName;
    public ConnectorInstance(ConnectorInterfaces connectorInterfaces,
                             GenerationalStateStore stateStore) {
      this.connectorInterfaces = connectorInterfaces;
      this.stateStore = stateStore;
      this.typeName = "";
    }
    public GenerationalStateStore getStateStore() {
      return this.stateStore;
    }
    public ConnectorInterfaces getConnectorInterfaces() {
      return this.connectorInterfaces;
    }
    public void setTypeName(String typeName) {
      this.typeName = typeName;
    }
    public String getTypeName() {
      return this.typeName;
    }
  }
}
