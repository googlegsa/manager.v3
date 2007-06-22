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

package com.google.enterprise.connector.spi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Simple implementation of the ConnectorType interface. Implementors may use
 * this directly or for reference. This implementation has no I18N. It is
 * initialized by being given a List of configuration keys uses a list of
 * configuration keys. These keys are used for both validation and display;
 * i.e., the strings in the list are used to name the configuration elements
 * that this instance requires, and are used as display values in the html forms
 * this instance generates.
 * <p>
 * This simple implementation considers any parameter to be valid, so long as it
 * is non-null and non-empty.
 * <p>
 * Implementors may want to override the
 * validateConfigPair(java.lang.String, java.lang.String) method. This is used
 * to validate a particular key-value pair.
 */
public class SimpleConnectorType implements ConnectorType {
  private static final Logger LOGGER = Logger
      .getLogger(SimpleConnectorType.class.getName());

  private static final String VALUE = "value";
  private static final String NAME = "name";
  private static final String TEXT = "text";
  private static final String TYPE = "type";
  private static final String INPUT = "input";
  private static final String CLOSE_ELEMENT = "/>";
  private static final String OPEN_ELEMENT = "<";
  private static final String PASSWORD = "password";
  private static final String TR_END = "</tr>\r\n";
  private static final String TD_END = "</td>\r\n";
  private static final String TD_START = "<td>";
  private static final String TR_START = "<tr>\r\n";

  private List keys = null;
  private Set keySet = null;
  private String initialConfigForm = null;

  public SimpleConnectorType() {
    //
  }

  /**
   * Set the keys that are required for configuration. One of the overloadings
   * of this method must be called exactly once before the SPI methods are used.
   * 
   * @param keys
   *          A list of String keys
   */
  public void setConfigKeys(List keys) {
    if (this.keys != null) {
      throw new IllegalStateException();
    }
    this.keys = keys;
    this.keySet = new HashSet(keys);
  }

  /**
   * Set the keys that are required for configuration. One of the overloadings
   * of this method must be called exactly once before the SPI methods are used.
   * 
   * @param keys
   *          An array of String keys
   */
  public void setConfigKeys(String[] keys) {
    setConfigKeys(Arrays.asList(keys));
  }

  /**
   * Sets the form to be used by this configurer. This is optional. If this
   * method is used, it must be called before the SPI methods are used.
   * 
   * @param formSnippet
   *          A String snippet of html - see the COnfigurer interface
   */
  public void setInitialConfigForm(String formSnippet) {
    if (this.initialConfigForm != null) {
      throw new IllegalStateException();
    }
    this.initialConfigForm = formSnippet;
  }

  private String getInitialConfigForm() {
    if (initialConfigForm != null) {
      return initialConfigForm;
    }
    if (keys == null) {
      throw new IllegalStateException();
    }
    this.initialConfigForm = makeConfigForm(null);
    return initialConfigForm;
  }

  /**
   * Validates whether a string is an acceptable value for a specific key.
   * 
   * @param key
   * @param val
   * @return true if the val is acceptable for this key
   */
  public boolean validateConfigPair(String key, String val) {
    if (val == null || val.length() == 0) {
      return false;
    }
    return true;
  }

  private boolean validateConfigMap(Map configData) {
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();
      String val = (String) configData.get(key);
      if (!validateConfigPair(key, val)) {
        return false;
      }
    }
    return true;
  }

  private void appendAttribute(StringBuffer buf, String attrName,
      String attrValue) {
    buf.append(" ");
    buf.append(attrName);
    buf.append("=\"");
    // TODO xml-encode the special characters (< > " etc.)
    buf.append(attrValue);
    buf.append("\"");
  }

  /**
   * Make a config form snippet using the keys (in the supplied order) and, if
   * passed a non-null config map, pre-filling values in from that map
   * 
   * @param configMap
   * @return config form snippet
   */
  private String makeConfigForm(Map configMap) {
    StringBuffer buf = new StringBuffer(2048);
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();
      appendStartRow(buf, key, false);
      buf.append(OPEN_ELEMENT);
      buf.append(INPUT);
      if (key.equalsIgnoreCase(PASSWORD)) {
        appendAttribute(buf, TYPE, PASSWORD);
      } else {
        appendAttribute(buf, TYPE, TEXT);
      }
      appendAttribute(buf, NAME, key);
      if (configMap != null) {
        String value = (String) configMap.get(key);
        if (value != null) {
          appendAttribute(buf, VALUE, value);
        }
      }
      appendEndRow(buf);
    }
    return buf.toString();
  }

  private String makeValidatedForm(Map configMap) {
    StringBuffer buf = new StringBuffer(2048);
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();

      String value = (String) configMap.get(key);
      if (!validateConfigPair(key, value)) {
        appendStartRow(buf, key, true);
        buf.append(OPEN_ELEMENT);
        buf.append(INPUT);
        if (key.equalsIgnoreCase(PASSWORD)) {
          appendAttribute(buf, TYPE, PASSWORD);
        } else {
          appendAttribute(buf, TYPE, TEXT);
        }
      } else {
        appendStartRow(buf, key, false);
        buf.append(OPEN_ELEMENT);
        buf.append(INPUT);
        if (key.equalsIgnoreCase(PASSWORD)) {
          appendAttribute(buf, TYPE, PASSWORD);
        } else {
          appendAttribute(buf, TYPE, TEXT);
          appendAttribute(buf, VALUE, value);
        }
      }
      appendAttribute(buf, NAME, key);
      appendEndRow(buf);
    }

    // toss in all the stuff that's in the map but isn't in the keyset
    // taking care to list them in alphabetic order (this is mainly for
    // testability).
    Iterator i = new TreeSet(configMap.keySet()).iterator();
    while (i.hasNext()) {
      String key = (String) i.next();
      if (!keySet.contains(key)) {
        // add another hidden field to preserve this data
        String val = (String) configMap.get(key);
        buf.append("<input type=\"hidden\" value=\"");
        buf.append(val);
        buf.append("\" name=\"");
        buf.append(key);
        buf.append("\"/>\r\n");
      }
    }
    return buf.toString();
  }

  private void appendStartRow(StringBuffer buf, String key, boolean red) {
    buf.append(TR_START);
    buf.append(TD_START);
    if (red) {
      buf.append("<font color=red>");
    }
    buf.append(key);
    if (red) {
      buf.append("</font>");
    }
    buf.append(TD_END);
    buf.append(TD_START);
  }

  private void appendEndRow(StringBuffer buf) {
    buf.append(CLOSE_ELEMENT);
    buf.append(TD_END);
    buf.append(TR_END);
  }

  /**
   * Returns an embedded configurer, which may depend on the configData already
   * supplied. This method is here primarily so that implementors can override
   * it.
   * 
   * @param configData
   * @param locale
   * @return another Configurer, which may be null
   */
  ConnectorType getEmbeddedConfigurer(Map configData, Locale locale) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Configurer#getConfigForm(java.util.Locale)
   */
  public ConfigureResponse getConfigForm(Locale locale) {
    ConfigureResponse result = new ConfigureResponse("",
        getInitialConfigForm());
    LOGGER.info("getConfigForm form:\n" + result.getFormSnippet());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Configurer#validateConfig(java.util.Map,
   *      java.util.Locale)
   */
  public ConfigureResponse validateConfig(Map configData,
      Locale locale) {
    if (validateConfigMap(configData)) {
      // all is ok
      return null;
    }
    String form = makeValidatedForm(configData);
    LOGGER.info("validateConfig new form:\n" + form);
    return new ConfigureResponse(
        "Some required configuration is missing", form);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Configurer
   *      #getPopulatedConfigForm(java.util.Map,java.util.Locale)
   */
  public ConfigureResponse getPopulatedConfigForm(Map configMap,
      Locale locale) {
    ConfigureResponse result = new ConfigureResponse("",
        makeConfigForm(configMap));
    return result;
  }

}
