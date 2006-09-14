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

import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.Iterator;

/**
 * Instantiator methods for manipulating connector types
 *
 */
public interface ConnectorTypeInstantiator {

  /**
   * Finds a named connector type.
   * 
   * @param connectorTypeName The connector type to find
   * @return the ConnectorType, fully instantiated
   * @throws ConnectorTypeNotFoundException if the connector type is not found
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException;

  /**
   * Gets all the known connector type names
   * 
   * @return an iterator of String names
   */
  public Iterator getConnectorTypeNames();

  /**
   * Gets the prototype definition for instances of this type
   * @param connectorTypeName The connector type for which to get the prototype
   * @return prototype String
   * @throws ConnectorTypeNotFoundException if the connector type is not found
   */
  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException;
}
