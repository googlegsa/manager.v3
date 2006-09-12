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
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.Traverser;

import javax.jcr.query.QueryManager;

/**
 * 
 */
public class MockInstantiator implements Instantiator {

  public static final String TRAVERSER_NAME1 = "foo";
  public static final String TRAVERSER_NAME2 = "bar";
  
  private static final ConnectorType CONNECTOR_TYPE;
  private static final Traverser TRAVERSER1;
  private static final Traverser TRAVERSER2;

  static {
    CONNECTOR_TYPE = null;

    // init TRAVERSER1
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());

    String connectorName = TRAVERSER_NAME1;
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);
    MockPusher pusher = new MockPusher(System.out);
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();

    TRAVERSER1 =
      new QueryTraverser(pusher, qtm, connectorStateStore, connectorName);

    // init TRAVERSER2
    mrel =
      new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    r = new MockRepository(mrel);
    qm = new MockJcrQueryManager(r.getStore());

    connectorName = TRAVERSER_NAME2;
    qtm = new SpiQueryTraversalManagerFromJcr(qm);
    pusher = new MockPusher(System.out);
    connectorStateStore = new MockConnectorStateStore();

    TRAVERSER2 =
      new QueryTraverser(pusher, qtm, connectorStateStore, connectorName);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConfigurer(java.lang.String)
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException{
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
    if (TRAVERSER_NAME1.equals(connectorName)) {
      return TRAVERSER1;
    } else if (TRAVERSER_NAME2.equals(connectorName)) {
      return TRAVERSER2;
    } else {
      throw new ConnectorNotFoundException("Connector not found: "
        + connectorName);
    }
  }

}
