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

import com.google.enterprise.connector.jcradaptor.SpiQueryTraversalManagerFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.traversal.LongRunningQueryTraverser;
import com.google.enterprise.connector.traversal.NeverEndingQueryTraverser;
import com.google.enterprise.connector.traversal.NoopQueryTraverser;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.query.QueryManager;

/**
 * 
 */
public class MockInstantiator implements Instantiator {
  public static final String TRAVERSER_NAME1 = "foo";
  public static final String TRAVERSER_NAME2 = "bar";
  public static final String TRAVERSER_NAME_NOOP = "noop";
  public static final String TRAVERSER_NAME_LONG_RUNNING = "longrunning";
  public static final String TRAVERSER_NAME_NEVER_ENDING = "neverending";
  
  private static final ConnectorType CONNECTOR_TYPE;
  private static Map traverserMap;

  static {
    CONNECTOR_TYPE = null;
    traverserMap = new HashMap();
    
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());

    String connectorName = TRAVERSER_NAME1;
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);
    MockPusher pusher = new MockPusher(System.out);
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();

    traverserMap.put(TRAVERSER_NAME1, 
      new QueryTraverser(pusher, qtm, connectorStateStore, connectorName));

    mrel = new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    r = new MockRepository(mrel);
    qm = new MockJcrQueryManager(r.getStore());

    connectorName = TRAVERSER_NAME2;
    qtm = new SpiQueryTraversalManagerFromJcr(qm);
    pusher = new MockPusher(System.out);
    connectorStateStore = new MockConnectorStateStore();

    traverserMap.put(TRAVERSER_NAME2, 
      new QueryTraverser(pusher, qtm, connectorStateStore, connectorName));

    traverserMap.put(TRAVERSER_NAME_NOOP, 
      new NoopQueryTraverser());
    
    traverserMap.put(TRAVERSER_NAME_LONG_RUNNING, 
      new LongRunningQueryTraverser());
    
    traverserMap.put(TRAVERSER_NAME_NEVER_ENDING, 
      new NeverEndingQueryTraverser());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConfigurer(java.lang.String)
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    return CONNECTOR_TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getTraverser(java.lang.String)
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException {
    if (traverserMap.containsKey(connectorName)) {
      return (Traverser) traverserMap.get(connectorName);
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
  }

  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    return "";
  }

  public Iterator getConnectorTypeNames() {
    return traverserMap.keySet().iterator();
  }

  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException {
  }

  public void dropConnector(String connectorName) throws InstantiatorException {
  }

  public AuthenticationManager getAuthenticationManager(String connectorName) throws ConnectorNotFoundException, InstantiatorException {
    throw new UnsupportedOperationException();
  }

  public AuthorizationManager getAuthorizationManager(String connectorName) throws ConnectorNotFoundException, InstantiatorException {
    throw new UnsupportedOperationException();
  }
}
