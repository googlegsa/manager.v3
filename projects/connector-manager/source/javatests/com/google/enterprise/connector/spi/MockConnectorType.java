// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.spi;

import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A mock implementation for the ConnectorType interface.
 */
public class MockConnectorType implements ConnectorType {
  private String typeName;

  public static final String PROP_BAD_CONFIG = "test.badConfig";
  public static final String PROP_MODIFY_CONFIG = "test.modifyConfig";
  public static final String PROP_DID_MODIFY_CONFIG = "test.didModifyConfig";

  public MockConnectorType(String typeName) {
    this.typeName = Strings.nullToEmpty(typeName);
  }

  /* @Override */
  public ConfigureResponse getConfigForm(Locale locale) {
    String message = "Sample form for " + typeName + " locale " + locale;
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
  public ConfigureResponse getPopulatedConfigForm(
      Map<String, String> configMap, Locale locale) {
    String message =
        "Sample populated form for " + typeName + " locale " + locale;
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
  public ConfigureResponse validateConfig(Map<String, String> configData,
      Locale locale, ConnectorFactory connectorFactory) {
    String message = "Validate config for " + typeName + " locale " + locale;
    if (configData.containsKey(PROP_BAD_CONFIG)) {
      return getPopulatedConfigForm(configData, locale);
    }
    if (configData.containsKey(PROP_MODIFY_CONFIG)) {
      Map<String, String> newConfigData =
          new HashMap<String, String>(configData);
      newConfigData.put(PROP_DID_MODIFY_CONFIG, "true");
      return new ConfigureResponse(null, null, newConfigData);
    }
    // Good config return.
    return null;
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return typeName.hashCode();
  }

  /**
   * Indicates whether some other object is "equal to" this one. Implemented by
   * running equals on the documentId string and comparing the status.
   *
   * @return {@code true} if this object is the same as the {@code obj}
   *         argument; {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    System.out.println("Equals called with " + obj);
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MockConnectorType other = (MockConnectorType) obj;
    return typeName.equals(other.typeName);
  }

  @Override
  public String toString() {
    return "ConnectorType " + typeName;
  }
}
