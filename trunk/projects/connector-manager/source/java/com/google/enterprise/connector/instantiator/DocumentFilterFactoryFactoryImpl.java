// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.common.collect.Lists;
import com.google.enterprise.connector.instantiator.ConnectorCoordinator;
import com.google.enterprise.connector.instantiator.ConnectorCoordinatorMap;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.util.filter.DocumentFilterChain;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/** {@inheritDoc} */
public class DocumentFilterFactoryFactoryImpl
    implements DocumentFilterFactoryFactory {

  private static final Logger LOGGER =
      Logger.getLogger(DocumentFilterFactoryFactoryImpl.class.getName());

  /** Document filter factory for all connector instances. */
  private final DocumentFilterFactory globalFilterFactory;

  /** Used to get document filter factories specific to a connector instance. */
  private final ConnectorCoordinatorMap coordinatorMap;

  public DocumentFilterFactoryFactoryImpl(
      DocumentFilterFactory globalFilterFactory,
      ConnectorCoordinatorMap coordinatorMap) {
    this.globalFilterFactory = globalFilterFactory;
    this.coordinatorMap = coordinatorMap;
  }
  
  /** {@inheritDoc} */
  public DocumentFilterFactory getDocumentFilterFactory() {
    return (globalFilterFactory == null) ? new DocumentFilterChain()
                                         : globalFilterFactory;
  }

  /** {@inheritDoc} */
  public DocumentFilterFactory getDocumentFilterFactory(String connectorName) {
    ConnectorCoordinator coordinator =
        (coordinatorMap == null) ? null : coordinatorMap.get(connectorName);
    if (coordinator != null) {
      try { 
        DocumentFilterFactory connectorFilterFactory =
            coordinator.getDocumentFilterFactory();
        if (connectorFilterFactory != null) {
          if (globalFilterFactory == null) {
            return connectorFilterFactory;
          } else {
            // Put the connector's filters before the global filters.
            return new DocumentFilterChain(Lists.newArrayList(
                connectorFilterFactory, globalFilterFactory));
          }
        }
      } catch (ConnectorNotFoundException e) {
        LOGGER.log(Level.FINE, "Connector not found: {0}", connectorName);
      }
    }
    // No connector instance, return just the globalFilterFactory.
    return getDocumentFilterFactory();
  }

  @Override
  public String toString() {
    return "Global Document Filters: " + globalFilterFactory;
  }    
}
