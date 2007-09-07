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

import com.google.enterprise.connector.jcr.JcrConnector;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorStateStore;
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

  private static final ConnectorType CONNECTOR_TYPE;
  private static Map connectorMap;

  static {
    CONNECTOR_TYPE = null;
    connectorMap = new HashMap();

    setupConnector(TRAVERSER_NAME1, "MockRepositoryEventLog1.txt");
    setupConnector(TRAVERSER_NAME2, "MockRepositoryEventLog1.txt");

    AuthenticationManager nullAuthenticationManager =
        new AuthenticationManager() {
          public AuthenticationResponse authenticate(AuthenticationIdentity identity) {
            throw new UnsupportedOperationException();
          }
        };

    AuthorizationManager nullAuthorizationManager = new AuthorizationManager() {
      public Collection authorizeDocids(Collection docids,
          AuthenticationIdentity identity) {
        throw new UnsupportedOperationException();
      }
    };

    connectorMap.put(TRAVERSER_NAME_NOOP, new ConnectorInterfaces(
        TRAVERSER_NAME_NOOP, new NoopQueryTraverser(),
        nullAuthenticationManager, nullAuthorizationManager));

    connectorMap.put(TRAVERSER_NAME_LONG_RUNNING, new ConnectorInterfaces(
        TRAVERSER_NAME_LONG_RUNNING, new LongRunningQueryTraverser(),
        nullAuthenticationManager, nullAuthorizationManager));

    connectorMap.put(TRAVERSER_NAME_NEVER_ENDING, new ConnectorInterfaces(
        TRAVERSER_NAME_NEVER_ENDING, new NeverEndingQueryTraverser(),
        nullAuthenticationManager, nullAuthorizationManager));

    connectorMap.put(TRAVERSER_NAME_INTERRUPTIBLE, new ConnectorInterfaces(
        TRAVERSER_NAME_INTERRUPTIBLE, new InterruptibleQueryTraverser(),
        nullAuthenticationManager, nullAuthorizationManager));
  }

  private static void setupConnector(String connectorName, String resourceName) {
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
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();
    QueryTraverser queryTraverser =
        new QueryTraverser(pusher, qtm, connectorStateStore, connectorName);

    ConnectorInterfaces connectorInterfaces =
        new ConnectorInterfaces(connectorName, queryTraverser,
            authenticationManager, authorizationManager);
    connectorMap.put(connectorName, connectorInterfaces);
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
    if (connectorMap.containsKey(connectorName)) {
      return ((ConnectorInterfaces) connectorMap.get(connectorName))
          .getTraverser();
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }

  public String getConnectorInstancePrototype(String connectorTypeName) {
    return "";
  }

  public Iterator getConnectorTypeNames() {
    return connectorMap.keySet().iterator();
  }

  public ConfigureResponse setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys, Locale locale,
      boolean update) {
	  return null;
  }

  public void dropConnector(String connectorName) {
  }

  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    if (connectorMap.containsKey(connectorName)) {
      return ((ConnectorInterfaces) connectorMap.get(connectorName))
          .getAuthenticationManager();
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }

  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    if (connectorMap.containsKey(connectorName)) {
      return ((ConnectorInterfaces) connectorMap.get(connectorName))
          .getAuthorizationManager();
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }

  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale) {
    throw new UnsupportedOperationException();
  }

  public Iterator getConnectorNames() {
    return connectorMap.keySet().iterator();
  }

  public String getConnectorTypeName(String connectorName) {
    return "";
  }

  public Map getConnectorConfig(String connectorName) {
    return new HashMap();
  }
}
