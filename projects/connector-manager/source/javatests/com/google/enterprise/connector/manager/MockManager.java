// Copyright 2006 Google Inc.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.ExtendedConfigureResponse;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.AuthorizationResponse.Status;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

  private static final String CONNECTOR1 = "connector1";
  private static final String CONNECTOR2 = "connector2";
  private static final String CONNECTOR3 = "connector3";
  private static final String CONNECTOR4 = "connector4";

  private boolean shouldVerifyIdentity;
  private String domain;
  private String username;
  private String password;
  private Collection<String> groups;

  /** Stand-in for the manager.locked property. */
  private boolean isLocked = false;

  // Protected constructor used by JUnit test subclasses.
  protected MockManager() {
    shouldVerifyIdentity = false;
  }

  public static MockManager getInstance() {
    return INSTANCE;
  }

  /* @Override */
  public AuthenticationResponse authenticate(String connectorName,
      AuthenticationIdentity identity) {
    if (shouldVerifyIdentity) {
      // Domains connector1 and connector2 only work for their respective
      // connectors.
      // This is used to test some connectors failing, but others passing.
      if ((CONNECTOR1.equals(identity.getDomain()) &&
           !CONNECTOR1.equals(connectorName)) ||
          (CONNECTOR2.equals(identity.getDomain()) &&
           !CONNECTOR2.equals(connectorName))) {
        return new AuthenticationResponse(false, null, null);
      }

      StringBuilder sb = new StringBuilder();
      if (!verifyIdentity(identity, sb)) {
        return new AuthenticationResponse(false, null, null);
      }
    }
    return new AuthenticationResponse(true, null, groups);
  }

  // Note this is trying to duplicate the AuthorizationParser.matchesIdentity()
  // behavior with the difference that this does not fail fast.
  private boolean verifyIdentity(AuthenticationIdentity identity,
      StringBuilder sb) {
    boolean result = true;
    if (!verifyComponent("domain", domain, identity.getDomain(), sb)) {
      result = false;
    }
    if (!verifyComponent("username", username, identity.getUsername(), sb)) {
      result = false;
    }
    // NULL password means do not authenticate, but return any groups.
    if (identity.getPassword() != null) {
      if (!verifyComponent("password", password, identity.getPassword(), sb)) {
        result = false;
      }
    }
    return result;
  }

  private boolean verifyComponent(String componentName, String expected,
      String actual, StringBuilder sb) {
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
  public Collection<AuthorizationResponse> authorizeDocids(String connectorName,
      List<String> docidList, AuthenticationIdentity identity) {
    // Connector4 always returns a null response.
    if (CONNECTOR4.equals(connectorName)) {
      return null;
    }

    // Sort the docids, so we process them in a predictable order for testing.
    docidList = new ArrayList<String>(docidList);
    Collections.sort(docidList);

    Set<AuthorizationResponse> results = new TreeSet<AuthorizationResponse>();
    StringBuilder sb = new StringBuilder();
    boolean permit = (shouldVerifyIdentity)? verifyIdentity(identity, sb) : true;
    if (sb.length() > 0) {
      LOGGER.info(sb.toString());
    }

    // Connector1 returns same response for every doc.
    if (CONNECTOR1.equals(connectorName)) {
      for (String docid : docidList) {
        results.add(new AuthorizationResponse(permit, docid));
      }
    }

    // Connector2 returns indeterminate every other doc.
    if (CONNECTOR2.equals(connectorName)) {
      boolean odd = false;
      Status status = (permit) ? Status.PERMIT : Status.DENY;
      for (String docid : docidList) {
        results.add(new AuthorizationResponse(
            ((odd) ? Status.INDETERMINATE : status), docid));
        odd = !odd;
      }
    }

    // Connector3 strictly returns permits, but only every other doc.
    if (CONNECTOR3.equals(connectorName)) {
      if (permit) {
        for (String docid : docidList) {
          LOGGER.info("Docid: " + docid + "  permit: " + permit);//DEBUGGING
          if (permit) {
            results.add(new AuthorizationResponse(permit, docid));
          }
          permit = !permit;
        }
      }
    }
    return results;
  }

  /* @Override */
  public Set<String> getConnectorTypeNames() {
    return new TreeSet<String>(Arrays.asList(
        new String[] {"Documentum", "Filenet", "Sharepoint"}));
  }

  /* @Override */
  public ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    throw new ConnectorTypeNotFoundException("Unsupported Operation");
  }

  public String getConnectorInstancePrototype(String typeName) {
    return "<?xml?><beans><bean id=\"" + typeName + "Instance\"/></beans>";
  }

  /* @Override */
  public ConfigureResponse getConfigForm(String connectorTypeName,
      String language) throws InstantiatorException {
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
    return new ExtendedConfigureResponse(new ConfigureResponse(message,
        formSnippet), getConnectorInstancePrototype(connectorTypeName));
  }

  /* @Override */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String language) throws InstantiatorException {
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
  public ConnectorStatus getConnectorStatus(String connectorName)
      throws ConnectorNotFoundException {
    String name = connectorName;
    String type = "Documentum";
    int status = 0;
    String schedule = connectorName + ":100:0:0-0";
    return new ConnectorStatus(name, type, status, schedule);
  }

  /* @Override */
  public List<ConnectorStatus> getConnectorStatuses() {
    List<ConnectorStatus> statuses = new ArrayList<ConnectorStatus>();
    try {
      statuses.add(getConnectorStatus(CONNECTOR1));
      statuses.add(getConnectorStatus(CONNECTOR2));
    } catch (ConnectorNotFoundException ignored) {
      // Ignored.
    }
    return statuses;
  }

  /* @Override */
  public ConfigureResponse setConnectorConfiguration(String connectorName,
        Configuration configuration, String language, boolean update)
        throws InstantiatorException {
    LOGGER.info("setConnectorConfig() connectorName: " + connectorName);
    LOGGER.info("setConnectorConfig() update: " + update);
    LOGGER.info("configData: ");
    for (Map.Entry<String, String> entry : configuration.getMap().entrySet()) {
      LOGGER.info(entry.getKey() + "/" + entry.getValue());
    }
    if (configuration.getXml() == null) {
      LOGGER.info("configXml: null");
    } else {
      LOGGER.info("configXml:");
      LOGGER.info(configuration.getXml());
    }
    // null is a success response
    return null;
  }

  final Properties managerConfig = new Properties();

  /* @Override */
  public Configuration getConnectorConfiguration(String connectorName)
      throws ConnectorNotFoundException {
    return new Configuration("Mock", new HashMap<String, String>(), null);
  }

  /* @Override */
  public Properties getConnectorManagerConfig() {
    return managerConfig;
  }

  /* @Override */
  public void setConnectorManagerConfig(String feederGateProtocol,
      String feederGateHost, int feederGatePort, int feederGateSecurePort) {
    if (!Strings.isNullOrEmpty(feederGateProtocol)) {
      managerConfig.put(Context.GSA_FEED_PROTOCOL_PROPERTY_KEY,
          feederGateProtocol);
    }
    if (!Strings.isNullOrEmpty(feederGateHost)) {
      managerConfig.put(Context.GSA_FEED_HOST_PROPERTY_KEY,
          feederGateHost);
    }
    managerConfig.put(Context.GSA_FEED_PORT_PROPERTY_KEY,
        String.valueOf(feederGatePort));
    if (feederGateSecurePort >= 0) {
      managerConfig.put(Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY,
          String.valueOf(feederGateSecurePort));
    }

    isLocked = true;
  }

  /* @Override */
  public void setSchedule(String connectorName, String schedule) {
    // do nothing
  }

  /* @Override */
  public void removeConnector(String connectorName)
      throws ConnectorNotFoundException, PersistentStoreException {
    if (CONNECTOR2.equals(connectorName)) {
      throw new ConnectorNotFoundException();
    }
    LOGGER.info("Removing connector: " + connectorName);
  }

  /* @Override */
  public void restartConnectorTraversal(String connectorName) {
    // do nothing;
  }

  /* @Override */
  public boolean isLocked() {
    return isLocked;
  }

  /** The tests need to reset isLocked. */
  public void setLocked(boolean isLocked) {
    this.isLocked = isLocked;
  }

  public void setShouldVerifyIdentity(boolean b) {
    shouldVerifyIdentity = b;
  }

  public void setExpectedIdentity(String domain, String username,
      String password, Collection<String> groups) {
    this.domain = domain;
    this.username = username;
    this.password = password;
    this.groups = groups;
  }
}
