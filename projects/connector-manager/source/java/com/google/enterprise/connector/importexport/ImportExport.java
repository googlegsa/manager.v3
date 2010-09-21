// Copyright (C) 2007-2009 Google Inc.
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

package com.google.enterprise.connector.importexport;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.servlet.SAXParseErrorHandler;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities to import/export connectors from/to XML.
 */
public class ImportExport {
  private static final Logger LOGGER =
      Logger.getLogger(ImportExport.class.getName());

  /*
   * Exports a list of connectors.
   * @return a List of ImportExportConnectors
   */
  private static final List<ImportExportConnector> getConnectors(
      Manager manager) {
    List<ImportExportConnector> connectors =
        new ArrayList<ImportExportConnector>();
    for (ConnectorStatus connectorStatus : manager.getConnectorStatuses()) {
      String name = connectorStatus.getName();
      String type = connectorStatus.getType();
      String scheduleString = connectorStatus.getSchedule();
      Map<String, String> config = null;
      try {
        config = manager.getConnectorConfig(connectorStatus.getName());
      } catch (ConnectorNotFoundException e) {
        // should never happen
        LOGGER.log(Level.WARNING, e.getMessage(), e);
      }
      ImportExportConnector connector = new ImportExportConnector(
          name, type, scheduleString, config);
      connectors.add(connector);
    }

    return connectors;
  }

  /*
   * (non-Javadoc)
   *
   * Imports a list of connectors.  Replaces the existing connectors with the
   * connectors in <code>connectors</code>.  For each connector in
   * <code>connectors</code>, update an existing connector if the
   * connector names match or create a new connector if it doesn't already
   * exist.  Remove any existing connectors which are not included in
   * <code>connectors</code>.
   * @param noRemove <code>setConnectors</code> removes previous connectors
   * which are not included in <code>connectors</code> if and only if
   * <code>noremove</code> is false.
   */
  private static final void setConnectors(Manager manager,
      List<ImportExportConnector> connectors, boolean noRemove)
      throws InstantiatorException, PersistentStoreException {
    Set<String> previousConnectorNames = new HashSet<String>();

    for (ConnectorStatus connector : manager.getConnectorStatuses()) {
      previousConnectorNames.add(connector.getName());
    }

    for (ImportExportConnector connector : connectors) {
      String name = connector.getName();
      String type = connector.getType();
      Map<String, String> config = connector.getConfig();
      String schedule = connector.getScheduleString();

      try {
        String language = "en";
        boolean update = previousConnectorNames.contains(name);

        // set connector config
        ConfigureResponse configureResponse =
            manager.setConnectorConfig(name, type, config, language, update);
        if (configureResponse != null) {
          String msg = "setConnectorConfig(name=" + name + "\"): "
              + configureResponse.getMessage();
          LOGGER.log(Level.WARNING, msg);
          continue;
        }

        // set schedule, if given
        if (schedule != null ) {
          try {
            manager.setSchedule(name, schedule);
          } catch (ConnectorNotFoundException e) {
            // should never happen
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new RuntimeException(e);
          }
        }

        previousConnectorNames.remove(name);
      } catch (ConnectorExistsException e) {
        // should never happen
        LOGGER.log(Level.WARNING, e.getMessage(), e);
      } catch (ConnectorNotFoundException e) {
        // should never happen
        LOGGER.log(Level.WARNING, e.getMessage(), e);
      }
    }

    // remove previous connectors which no longer exist
    if (!noRemove) {
      for (String name : previousConnectorNames) {
        try {
          manager.removeConnector(name);
        } catch (ConnectorNotFoundException e) {
          // should never happen
          LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Deserializes connectors from XML.
   * @return a List of ImportExportConnectors
   */
  @SuppressWarnings("deprecation")
  public static List<ImportExportConnector> fromXmlConnectorsElement(
      Element connectorsElement) {
    List<ImportExportConnector> connectors =
        new ArrayList<ImportExportConnector>();

    NodeList connectorElements = connectorsElement.getElementsByTagName(
        ServletUtil.XMLTAG_CONNECTOR_INSTANCE);

    for (int i = 0; i < connectorElements.getLength(); i++) {
      Element connectorElement = (Element) connectorElements.item(i);
      String name = ServletUtil.getFirstElementByTagName(
          connectorElement, ServletUtil.XMLTAG_CONNECTOR_NAME);
      String type = ServletUtil.getFirstElementByTagName(
          connectorElement, ServletUtil.XMLTAG_CONNECTOR_TYPE);
      String scheduleString = ServletUtil.getFirstElementByTagName(
          connectorElement, ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);
      // TODO: Remove this when v2.0 and older no longer needs to be supported.
      if (scheduleString == null) {
        // Could be dealing with old format.
        scheduleString = ServletUtil.getFirstElementByTagName(
            connectorElement, ServletUtil.XMLTAG_CONNECTOR_SCHEDULE);
      }
      Element configElement = (Element) connectorElement.getElementsByTagName(
          ServletUtil.XMLTAG_CONNECTOR_CONFIG).item(0);
      Map<String, String> config = ServletUtil.getAllAttributes(
          configElement, ServletUtil.XMLTAG_PARAMETERS);
      ImportExportConnector connector = new ImportExportConnector(
          name, type, scheduleString, config);
      connectors.add(connector);
    }

    return connectors;
  }

  /**
   * Deserialializes connectors from XML string.
   * @return a List of ImportExportConnectors
   */
  public static List<ImportExportConnector> fromXmlString(String xmlString) {
    Document document =
        ServletUtil.parse(xmlString, new SAXParseErrorHandler(), null);
    Element connectorsElement = document.getDocumentElement();
    return fromXmlConnectorsElement(connectorsElement);
  }

  /**
   * Serializes connectors to XML.
   * @param connectors a List of ImportExportConnectors
   */
  public static String asXmlString(List<ImportExportConnector> connectors) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    ServletUtil.writeXMLTag(
        pw, 0, ServletUtil.XMLTAG_CONNECTOR_INSTANCES, false);

    for (ImportExportConnector connector : connectors) {
      String name = connector.getName();
      String type = connector.getType();
      String scheduleString = connector.getScheduleString();
      Map<String, String> config = connector.getConfig();
      ServletUtil.writeXMLTag(
          pw, 1, ServletUtil.XMLTAG_CONNECTOR_INSTANCE, false);
      ServletUtil.writeXMLElement(
          pw, 2, ServletUtil.XMLTAG_CONNECTOR_NAME, name);
      ServletUtil.writeXMLElement(
          pw, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE, type);
      StringBuilder builder = new StringBuilder();
      ServletUtil.writeXMLTagWithAttrs(builder, 2,
          ServletUtil.XMLTAG_CONNECTOR_SCHEDULES,
          ServletUtil.ATTRIBUTE_VERSION + "3" + ServletUtil.QUOTE,
          false);
      builder.append(scheduleString);
      ServletUtil.writeXMLTag(builder, 0,
          ServletUtil.XMLTAG_CONNECTOR_SCHEDULES, true);
      pw.println(builder.toString());
      ServletUtil.writeXMLTag(
          pw, 2, ServletUtil.XMLTAG_CONNECTOR_CONFIG, false);
      Map<String, String> sorted = new TreeMap<String, String>(config);
      for (Map.Entry<String, String> me : sorted.entrySet()) {
        builder.setLength(0);
        try {
          builder.append(ServletUtil.ATTRIBUTE_NAME);
          XmlUtils.xmlAppendAttrValue(me.getKey(), builder);
          builder.append(ServletUtil.QUOTE).append(ServletUtil.ATTRIBUTE_VALUE);
          XmlUtils.xmlAppendAttrValue(me.getValue(), builder);
          builder.append(ServletUtil.QUOTE);
        } catch (IOException e) { /* Can't happen with StringBuilder */ }
        ServletUtil.writeXMLTagWithAttrs(
            pw, 3, ServletUtil.XMLTAG_PARAMETERS, builder.toString(), true);
      }
      ServletUtil.writeXMLTag(
          pw, 2, ServletUtil.XMLTAG_CONNECTOR_CONFIG, true);
      ServletUtil.writeXMLTag(
          pw, 1, ServletUtil.XMLTAG_CONNECTOR_INSTANCE, true);
    }

    ServletUtil.writeXMLTag(
        pw, 0, ServletUtil.XMLTAG_CONNECTOR_INSTANCES, true);

    return sw.toString();
  }

  /**
   * read a list of connectors from an XML file.
   * @return a List of ImportExportConnectors
   */
  public static List<ImportExportConnector> readFromFile(String filename)
      throws IOException {
    Reader isr = new InputStreamReader(new FileInputStream(filename), "UTF-8");
    String string = StringUtils.readAllToString(isr);
    return fromXmlString(string);
  }

  /**
   * write a list of connectors to an XML file.
   * @param connectors a List of ImportExportConnectors
   */
  public static void writeToFile(String filename,
      List<ImportExportConnector> connectors) throws IOException {
    OutputStreamWriter osw =
        new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
    osw.write(asXmlString(connectors));
    osw.close();
  }

  /**
   * A utility to import/export connectors from/to an XML file.
   * usage: <code>ImportExport (export|import|import-no-remove)
   *        &lt;filename&gt;filename</code>
   */
  public static final void main(String[] args) throws Exception {
    Context context = Context.getInstance();
    context.setStandaloneContext("WEB-INF/applicationContext.xml",
                                 new File("WEB-INF").getAbsolutePath());
    Manager manager = context.getManager();

    if (args.length == 2 && args[0].equals("export")) {
      writeToFile(args[1], getConnectors(manager));
    } else if (args.length == 2 && args[0].equals("import")) {
      setConnectors(manager, readFromFile(args[1]), false);
    } else if (args.length == 2 && args[0].equals("import-no-remove")) {
      setConnectors(manager, readFromFile(args[1]), true);
    } else {
      System.err.println(
          "usage: ImportExport (export|import|import-no-remove) <filename>");
    }
  }
}
