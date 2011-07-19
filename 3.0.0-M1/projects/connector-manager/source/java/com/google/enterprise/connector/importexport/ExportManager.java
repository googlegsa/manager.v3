// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.importexport;

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.XmlUtils;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An encapsulation of all the information we export/import Connector
 * Manager instance.
 */
public class ExportManager {
  private static final Logger LOGGER =
      Logger.getLogger(ExportManager.class.getName());

  private final Properties properties;
  private final String[] configLocations;

  public ExportManager() {
    this(Context.getInstance().getConnectorManagerProperties(),
         Context.getInstance().getConfigLocations());
  }

  public ExportManager(Properties properties, String[] configLocations) {
    this.properties = properties;
    this.configLocations = configLocations;
  }

  /**
   * Returns the Connector Manager's configuration Properties.
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * Returns the ApplicationContext configLocations.
   */
  public String[] getConfigLocations() {
    return configLocations;
  }

  /**
   * Serializes the Connector Manager configuration to an XML output stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent starting indent for the XML tags.
   */
  public void toXml(PrintWriter out, int indent) {
    toXml(out, indent, null);
  }

  /**
   * Serializes the Connector Manager configuration and the configurations
   * of all its Connectors to an XML output stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent starting indent for the XML tags.
   * @param connectors an ImportExportConnectorList
   */
  public void toXml(PrintWriter out, int indent,
                    ImportExportConnectorList connectors) {
    ServletUtil.writeXMLTag(out, indent, ServletUtil.XMLTAG_MANAGER, false);
    ServletUtil.writeManagerSplash(out, indent + 1);

    // Write out Connector Manager Properties.
    writeProperties(out, indent + 1);

    // Write out the ApplicationContext XML configuration resources.
    ApplicationContext context = Context.getInstance().getApplicationContext();
    for (String location : getConfigLocations()) {
      try {
        for (Resource resource : context.getResources(location)) {
          try {
            String configXml = StringUtils.streamToStringAndThrow(
                new FileInputStream(resource.getFile()));
            writeConfigXml(out, indent + 1, resource.getFilename(), configXml);
          } catch (IOException ie) {
            LOGGER.log(Level.SEVERE,
                       "Failed to extract configLocation resource: "
                       + resource.getFilename(), ie);
          }
        }
      } catch (IOException ioe) {
        LOGGER.log(Level.SEVERE,
            "Failed to extract configLocation: " + location, ioe);
      }
    }

    // Write out Connector instance configurations, if supplied.
    if (connectors != null) {
      connectors.toXml(out, indent + 1);
    }

    ServletUtil.writeXMLTag(out, indent, ServletUtil.XMLTAG_MANAGER, true);
  }

  /**
   * Write out the Configuration Properties to the XML stream.
   * Sensitive properties (such as passwords) are encrypted in the output.
   *
   * @param out PrintWriter to write XML output.
   * @param indent indent for the XML tag.
   */
  void writeProperties(PrintWriter out, int indent) {
    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_MANAGER_CONFIG, false);

    // Encrypt sensitive properties before writing them out.
    Properties props = PropertiesUtils.copy(getProperties());
    PropertiesUtils.encryptSensitiveProperties(props);
    Map<String, String> sorted =
        new TreeMap<String, String>(PropertiesUtils.toMap(props));

    for (Map.Entry<String, String> me : sorted.entrySet()) {
      StringBuilder builder = new StringBuilder();
      try {
        builder.append(ServletUtil.ATTRIBUTE_NAME);
        XmlUtils.xmlAppendAttrValue(me.getKey(), builder);
        builder.append(ServletUtil.QUOTE).append(ServletUtil.ATTRIBUTE_VALUE);
        XmlUtils.xmlAppendAttrValue(me.getValue(), builder);
        builder.append(ServletUtil.QUOTE);
      } catch (IOException e) {
        // Can't happen with StringBuilder.
      }
      ServletUtil.writeXMLTagWithAttrs(out, indent + 1,
          ServletUtil.XMLTAG_PARAMETERS, builder.toString(), true);
    }
    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_MANAGER_CONFIG, true);
  }

  /**
   * Write out a Configuration XML file to the XML stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent indent for the XML tag.
   * @param name the name of the XML config file.
   * @param configXml the XML configuration.
   */
  private void writeConfigXml(PrintWriter out, int indent, String name,
                              String configXml) {
    StringBuilder builder = new StringBuilder();
    try {
      builder.append(ServletUtil.ATTRIBUTE_NAME);
      XmlUtils.xmlAppendAttrValue(name, builder);
      builder.append(ServletUtil.QUOTE);
    } catch (IOException e) {
      // Can't happen with StringBuilder.
    }

    ServletUtil.writeXMLTagWithAttrs(out, indent,
        ServletUtil.XMLTAG_MANAGER_CONFIG_XML, builder.toString(), false);

    out.print(ServletUtil.XML_CDATA_START);
    out.print(ServletUtil.escapeEndMarkers(configXml));
    out.println(ServletUtil.XML_CDATA_END);
    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_MANAGER_CONFIG_XML, true);
  }
}
