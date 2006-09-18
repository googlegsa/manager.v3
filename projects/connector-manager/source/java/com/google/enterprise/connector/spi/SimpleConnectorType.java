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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Simple implementation of the ConnectorType interface. Implementors may use this
 * directly or for reference. This implementation has no I18N - it just uses a
 * list of configuration keys for both validation and display.
 */
public class SimpleConnectorType implements ConnectorType {

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
   * @param keys A list of String keys
   */
  public void setConfigKeys(List keys) {
    if (this.keys != null) {
      throw new IllegalStateException();
    }
    this.keys = keys;
    this.keySet = new HashSet(this.keys);
  }

  /**
   * Set the keys that are required for configuration. One of the overloadings
   * of this method must be called exactly once before the SPI methods are used.
   * 
   * @param keys An array of String keys
   */
  public void setConfigKeys(String[] keys) {
    setConfigKeys(Arrays.asList(keys));
  }

  /**
   * Sets the form to be used by this configurer. This is optional. If this
   * method is used, it must be called before the SPI methods are used.
   * 
   * @param formSnippet A String snippet of html - see the COnfigurer interface
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
    ConfigureResponse configureResponse = makeConfigForm(null, null);
    this.initialConfigForm = configureResponse.getFormSnippet();
    return initialConfigForm;
  }

  /**
   * Validates a config map to make sure that everything is there and returns a
   * new form if there's something missing. In practice, this method does two
   * things - so closely related that it is one method rather than two: if the
   * configData parameter is null, it creates a default form based on the config
   * keys in the list. If configData is not null, it creates a form just for the
   * ones that are missing, and returns an appropriate message.
   * 
   * @param configData
   * @param language
   * @return a ConfigureResponse with a message, as appropiate, and the
   *         constructed form. If configData was non-null and all items are
   *         valid, then both message and form will be null.
   */
  private ConfigureResponse makeConfigForm(Map configData, String language) {
    StringBuffer buf = new StringBuffer(2048);
    boolean allOk = true;
    for (Iterator i = keys.iterator(); i.hasNext();) {
      String key = (String) i.next();
      buf.append("<tr>\r\n<td>");
      buf.append(key);
      buf.append("</td>\r\n<td>");
      boolean isPassword = key.equalsIgnoreCase("password");
      boolean shouldHide = false;
      String val = "";
      if (configData != null) {
        val = (String) configData.get(key);
        if (val != null && val.length() > 0) {
          shouldHide = true;
          if (isPassword) {
            buf.append("******");
          } else {
            buf.append(val);
          }
        } else {
          // remember that we found at least one item with no value in the map
          allOk = false;
        }
      }
      buf.append("<input type=\"");
      if (shouldHide) {
        buf.append("hidden\" value=\"");
        buf.append(val);
      } else if (key.equalsIgnoreCase("password")) {
        buf.append("password");
      } else {
        buf.append("text");
      }
      buf.append("\" name=\"");
      buf.append(key);
      buf.append("\"/></td>\r\n</tr>\r\n");
    }
    // toss in all the stuff that's in the map but isn't in the keyset
    // taking care to list them in alphabetic order (this is mainly for
    // testability).
    if (configData != null) {
      Iterator i = new TreeSet(configData.keySet()).iterator();
      while (i.hasNext()) {
        String key = (String) i.next();
        if (!keySet.contains(key)) {
          // add another hidden field to preserve this data
          String val = (String) configData.get(key);
          buf.append("<input type=\"hidden\" value=\"");
          buf.append(val);
          buf.append("\" name=\"");
          buf.append(key);
          buf.append("\"/>\r\n");
        }
      }
    }
    if (configData != null) {
      if (!allOk) {
        return new ConfigureResponse(
            "Some configuration keys are missing values", new String(buf));
      } else {
        return new ConfigureResponse(null, null);
      }
    }
    return new ConfigureResponse(null, new String(buf));
  }

  /**
   * Returns an embedded configurer, which may depend on the configData already
   * supplied. This method is here primarily so that implementors can override
   * it.
   * 
   * @param configData
   * @param language
   * @return another Configurer, which may be null
   */
  ConnectorType getEmbeddedConfigurer(Map configData, String language) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Configurer#getConfigForm(java.lang.String)
   */
  public ConfigureResponse getConfigForm(String language) {
    ConfigureResponse result =
        new ConfigureResponse("", getInitialConfigForm());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Configurer#validateConfig(java.util.Map,
   *      java.lang.String)
   */
  public ConfigureResponse validateConfig(Map configData, String language) {
    ConfigureResponse result = makeConfigForm(configData, language);
    String message = result.getMessage();
    if (message != null && message.length() > 0) {
      return result;
    }
    ConnectorType embeddedConfigurer = getEmbeddedConfigurer(configData, language);
    if (embeddedConfigurer == null) {
      return result;
    }
    return embeddedConfigurer.validateConfig(configData, language);
  }

}
