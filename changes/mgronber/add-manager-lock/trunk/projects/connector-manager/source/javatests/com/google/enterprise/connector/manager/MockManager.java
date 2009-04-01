// Copyright (C) 2006-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * A mock implementation for the Manager interface. This implementation
 * basically hardcodes a bunch of responses and can be used before the actual
 * implementation is complete.
 *
 */
public class MockManager implements Manager {
  private static final MockManager INSTANCE = new MockManager();
  private static final Logger LOGGER =
      Logger.getLogger(MockManager.class.getName());
  
  private boolean shouldVerifyIdentity;
  private String domain;
  private String username;
  private String password;

  // Protected constructor used by JUnit test subclasses.
  protected MockManager() {
    shouldVerifyIdentity = false;
  }

  public static MockManager getInstance() {
    return INSTANCE;
  }

  /* @Override */
  public boolean authenticate(String connectorName, 
      AuthenticationIdentity identity) {
    if (!shouldVerifyIdentity) {
      return true;
    }
    boolean result = true;
    StringBuffer sb = new StringBuffer();
    if (!verifyComponent("domain", domain, identity.getDomain(), sb)) {
      result = false;
    }
    if (!verifyComponent("username", username, identity.getUsername(), sb)) {
      result = false;
    }
    if (!verifyComponent("password", password, identity.getPassword(), sb)) {
      result = false;
    }
    if (!result) {
      throw new IllegalStateException(new String(sb));
    }
    return true;
  }
  
  private boolean verifyComponent(String componentName, String expected, 
      String actual, StringBuffer sb) {
    if (expected == null) {
      if (actual != null) {
        sb.append("Expected null " + componentName + " got " + actual + "\n");
        return false;
      }
      return true;
    }
    if (!expected.equals(actual)) {
      sb.append("Expected " + componentName + "\"" + expected + "\" got \"" + actual +"\"\n");      
      return false;
    }
    return true;
  }

  /* @Override */
  public Set authorizeDocids(String connectorName, List docidList,
      String username) {
    Set docidAuthSet = new HashSet();
    docidAuthSet.addAll(docidList);
    return docidAuthSet;
  }

  /* @Override */
  public Set getConnectorTypeNames() {
    return new TreeSet(Arrays.asList(
        new String[] {"Documentum", "Filenet", "Sharepoint"}));
  }

  /* @Override */
  public ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    throw new ConnectorTypeNotFoundException("Unsupported Operation");
  }

  /* @Override */
  public ConfigureResponse getConfigForm(String connectorTypeName,
      String language) {
    String message =
        "Sample form for " + connectorTypeName + "lang " + language;
    String formSnippet =
        "    <tr><td>Repository</td>"
            + "      <td><input type=\"text\" name=\"repository\" value=\"\"></td>"
            + "    </tr>" + "    <tr><td>Username</td>"
            + "      <td><input type=\"text\" name=\"username\" value=\"\">"
            + "      </td></tr>" + "    <tr><td>Password</td>"
            + "      <td><input type=\"password\" name=\"passwd\" value=\"\">"
            + "    </td></tr>" + "    <tr><td>Seed URIs</td>"
            + "      <td><textarea name=\"seedUris\"></textarea></td></tr>";
    return new ConfigureResponse(message, formSnippet);
  }

  /* @Override */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String language) {
    String message = "Sample form for " + connectorName + "lang " + language;
    String formSnippet =
        "<tr>\n" + "<td>Username</td>\n" + "<td>\n"
            + "<input type=\"text\" name=\"Username\" />\n" + "</td>\n"
            + "</tr>\n" + "<tr>\n" + "<td>Password</td>\n" + "<td>\n"
            + "<input type=\"password\" name=\"Password\" />\n" + "</td>\n"
            + "</tr>\n" + "<tr>\n" + "<td>Color</td>\n" + "<td>\n"
            + "<input type=\"text\" name=\"Color\" />\n" + "</td>\n"
            + "</tr>\n" + "<tr>\n" + "<td>Repository File</td>\n" + "<td>\n"
            + "<input type=\"text\" name=\"Repository File\" />\n" + "</td>\n"
            + "</tr>\n";
    return new ConfigureResponse(message, formSnippet);
  }

  /* @Override */
  public ConnectorStatus getConnectorStatus(String connectorName) {
    String name = connectorName;
    String type = "Documentum";
    int status = 0;
    String schedule = connectorName + ":100:0:0-0";
    return new ConnectorStatus(name, type, status, schedule);
  }

  /* @Override */
  public List getConnectorStatuses() {
    List statuses = new ArrayList();
    statuses.add(getConnectorStatus("connector1"));
    statuses.add(getConnectorStatus("connector2"));
    return statuses;
  }

  public ConfigureResponse setConfig(String connectorName, Map configData,
      String language) {
    return null;
  }

  /* @Override */
  public ConfigureResponse setConnectorConfig(String connectorName,
      String connectorTypeName, Map configData, String language,
      boolean update) {
    LOGGER.info("setConnectorConfig() connectorName: " + connectorName);
    LOGGER.info("setConnectorConfig() update: " + update);
    LOGGER.info("configData: ");
    Set set = configData.entrySet();
    Iterator iterator = set.iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      LOGGER.info(entry.getKey() + "/" + entry.getValue());
    }
    // null is a success response
    return null;
  }

  /* @Override */
  public Properties getConnectorManagerConfig() {
    return new Properties();
  }

  /* @Override */
  public void setConnectorManagerConfig(String feederGateHost,
      int feederGatePort) {
    // do nothing
  }

  /* @Override */
  public void setSchedule(String connectorName, String schedule) {
    // do nothing
  }

  /* @Override */
  public void removeConnector(String connectorName)
      throws ConnectorNotFoundException, PersistentStoreException {
    if (connectorName == "connector2") {
      throw new ConnectorNotFoundException();
    }
    LOGGER.info("Removing connector: " + connectorName);
  }

  /* @Override */
  public void restartConnectorTraversal(String connectorName) {
    // do nothing;
  }

  /* @Override */
  public Map getConnectorConfig(String connectorName) {
    return new HashMap();
  }

  /* @Override */
  public boolean isLocked() {
    return false;
  }
  
  public void setShouldVerifyIdentity(boolean b) {
    shouldVerifyIdentity = b;
  }
  
  public void setExpectedIdentity(String domain, String username, 
      String password) {
    this.domain = domain;
    this.username = username;
    this.password = password;
  }
}
