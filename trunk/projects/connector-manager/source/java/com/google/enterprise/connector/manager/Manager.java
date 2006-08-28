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

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.spi.ResultSet;

import org.w3c.dom.Document;

import java.util.List;

/**
 * The main interface to the Connector Manager. Front ends such as servlets or
 * main() programs may be built using this.
 */
public interface Manager {

  /**
   * Stores configuration changes to the Connector Manager itself.
   * 
   * @param d Document containing XML configuration - DTD TBD.
   * @return true for success.
   */
  public boolean storeConfig(Document d);

  /**
   * Returns a list of connector implementations that this manager knows about.
   * 
   * @return A list of Strings - the name of each implementation.
   */
  public List getConnectorImplementationList();

  /**
   * Stores configuration for a new connector instance
   * 
   * @param d Document containing XML configuration - DTD TBD.
   * @return true for success.
   */
  public boolean storeConnectorConfig(Document d);


  /**
   * Gets a special form the connector implementor may supply to do
   * connector-implementation-specific configuration.
   * 
   * @param connectorImplementationName
   * @return the form as a String. null is returned if the implementation does
   *         not specify a config form.
   */
  public String getConfigForm(String connectorImplementationName);

  /**
   * Returns the status of a connector instance, in XML.
   * 
   * @param connectorInstanceName
   * @return Document containing XML configuration - DTD TBD.
   */
  public Document getConnectorStatus(String connectorInstanceName);

  /**
   * Authenticates a user against a named connector.
   * 
   * @param connectorInstanceName
   * @param username
   * @param password
   * @return true for success.
   */
  public boolean authenticate(String connectorInstanceName, String username,
      String password);

  /**
   * Gets authorization from a named connector for a set of documents by ID.
   * 
   * @param connectorInstanceName
   * @param docidList The document set represented as a list of Strings: the
   *        docid for each document
   * @param username The username as a string
   * @return A List of booleans parallel to the input list of IDs: the boolean
   *         in the corresponding position indicates whether that user can see
   *         that document.
   */
  public List authorizeDocids(String connectorInstanceName, List docidList,
      String username);

  /**
   * Gets authorization from a named connector for a set of documents by token.
   * 
   * @param connectorInstanceName
   * @param tokenList The document set represented as a list of Strings: the
   *        security token for a class of documents
   * @param username The username as a string
   * @return A List of booleans parallel to the input list of IDs: the boolean
   *         in the corresponding position indicates whether that user can see
   *         that document.
   */
  public ResultSet authorizeTokens(String connectorInstanceName,
      List tokenList, String username);

}
