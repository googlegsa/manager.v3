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

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Iterator;
import java.util.Map;

/**
 * 
 */
public class SpringInstantiator implements Instantiator {

  // dependencies
  ConnectorTypeInstantiator connectorTypeInstantiator;
  ConnectorInstantiator connectorInstantiator;

  /**
   * @param connectorTypeInstantiator
   * @param connectorInstantiator
   */
  public SpringInstantiator(
      ConnectorTypeInstantiator connectorTypeInstantiator,
      ConnectorInstantiator connectorInstantiator) {
    this.connectorTypeInstantiator = connectorTypeInstantiator;
    this.connectorInstantiator = connectorInstantiator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getConnectorType(java.lang.String)
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    return connectorTypeInstantiator.getConnectorType(connectorTypeName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.Instantiator#getTraverser(java.lang.String)
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return connectorInstantiator.getTraverser(connectorName);
  }

  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    return connectorTypeInstantiator
        .getConnectorInstancePrototype(connectorTypeName);
  }

  public Iterator getConnectorTypeNames() {
    return connectorTypeInstantiator.getConnectorTypeNames();
  }


  public void dropConnector(String connectorName) throws InstantiatorException {
    connectorInstantiator.dropConnector(connectorName);
  }

  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException {
    // get the protype string - this also validates by throwing a
    // ConnectorTypeNotFoundException if the connectorTypeName doesn't exist
    String prototypeString = getConnectorInstancePrototype(connectorTypeName);
    // then dispatch to the connector instantiator
    setConnectorConfig(connectorName, connectorTypeName, configKeys,
        prototypeString);
  }

  private void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys, String prototypeString)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException {
    connectorInstantiator.setConnectorConfig(connectorName, connectorTypeName,
        configKeys, prototypeString);
  }

  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return connectorInstantiator.getAuthenticationManager(connectorName);
  }

  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return connectorInstantiator.getAuthorizationManager(connectorName);
  }

}
