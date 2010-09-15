// Copyright 2007 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.util.SAXParseErrorHandler;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.apache.commons.cli.CommandLine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Legacy Utility to import/export connectors from/to XML.
 * Used by the GSA.
 */
public class ImportExport extends AbstractCommandLineApp {
  private static final Logger LOGGER =
      Logger.getLogger(ImportExport.class.getName());

  /**
   * Returns a ImportExportConnectorList representing the current
   * set of connectors.
   *
   * @return a ImportExportConnectorList
   */
  @VisibleForTesting
  static final ImportExportConnectorList getConnectors() {
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    Instantiator instantiator = Context.getInstance().getInstantiator();
    for (String connectorName : instantiator.getConnectorNames()) {
      try {
        Configuration configuration =
            instantiator.getConnectorConfiguration(connectorName);
        if (configuration != null) {
          Schedule schedule = instantiator.getConnectorSchedule(connectorName);
          connectors.add(new LegacyImportExportConnector(
              connectorName, configuration, schedule, null));
        }
      } catch (ConnectorNotFoundException e) {
        // This shouldn't happen.
        LOGGER.warning("Connector " + connectorName + " not found!");
      }
    }
    return connectors;
  }

  /**
   * Imports a list of connectors. Replaces the existing connectors with the
   * connectors in {@code connectors}. For each connector in {@code connectors},
   * update an existing connector if the connector names match or create a new
   * connector if it doesn't already exist.
   * Unless instructed otherwise, remove any existing connectors which are not
   * included in {@code connectors}.
   *
   * @param connectors a {@link ImportExportConnectorList}
   * @param noRemove {@code setConnectors} removes previous connectors
   *        which are not included in {@code connectors} if and only if
   *        {@code noremove} is {@code false}.
   */
  static final void setConnectors(ImportExportConnectorList connectors,
      boolean noRemove) {
    Instantiator instantiator = Context.getInstance().getInstantiator();
    Set<String> previousConnectorNames =
        new HashSet<String>(instantiator.getConnectorNames());

    for (ImportExportConnector connector : connectors) {
      String name = connector.getName();
      try {
        // Store the Configuration.
        boolean update = previousConnectorNames.contains(name);
        Configuration configuration = connector.getConfiguration();
        ConfigureResponse configureResponse =
            instantiator.setConnectorConfiguration(name, configuration,
                                                   Locale.ENGLISH, update);
        if (configureResponse != null) {
          LOGGER.warning("setConnectorConfiguration(name=" + name + "\"): "
                         + configureResponse.getMessage());
          continue;
        }

        // Store the Schedule.
        instantiator.setConnectorSchedule(name, connector.getSchedule());

        previousConnectorNames.remove(name);
      } catch (ConnectorNotFoundException e) {
        // This shouldn't happen.
        LOGGER.warning("Connector " + name + " not found!");
      } catch (ConnectorExistsException e) {
        // This shouldn't happen.
        LOGGER.warning("Connector " + name + " already exists!");
      } catch (ConnectorTypeNotFoundException e) {
        LOGGER.warning("Connector Type " + connector.getTypeName()
                       + " not found!");
      } catch (InstantiatorException e) {
        LOGGER.log(Level.WARNING, "Failed to create connector " + name + ": ",
                   e);
      }
    }

    // Remove previous connectors which no longer exist.
    if (!noRemove) {
      for (String name : previousConnectorNames) {
        try {
          instantiator.removeConnector(name);
        } catch (InstantiatorException e) {
          LOGGER.log(Level.WARNING, "Failed to remove connector " + name + ": ",
                     e);
        }
      }
    }
  }

  /**
   * Reads a list of connectors from an XML file.
   *
   * @param filename source XML file for connectors.
   * @return an ImportExportConnectorList
   */
  public static ImportExportConnectorList readFromFile(String filename)
      throws IOException {
    String xmlString =
        StringUtils.streamToStringAndThrow(new FileInputStream(filename));
    Document document =
        XmlParseUtil.parse(xmlString, new SAXParseErrorHandler(), null);
    Element connectorsElement = document.getDocumentElement();
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    connectors.fromXml(document.getDocumentElement(),
        LegacyImportExportConnector.class);
    return connectors;
  }

  /**
   * Writes a list of connectors to an XML file.
   *
   * @param connectors an ImportExportConnectorList
   * @param filename destination XML file for connectors.
   */
  public static void writeToFile(String filename,
      ImportExportConnectorList connectors) throws IOException {
    PrintWriter out = new PrintWriter(new OutputStreamWriter(
        new FileOutputStream(filename), "UTF-8"));
    connectors.toXml(out, 0);
    out.close();
  }

  @Override
  public String getName() {
    return "ImportExport";
  }

  @Override
  public String getDescription() {
    return "Imports and Exports Connector configurations.";
  }

  @Override
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax() + "(export|import|import-no-remove) <filename>";
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    initStandAloneContext(true);
    String[] args = commandLine.getArgs();
    try {
      if (args.length == 2 && args[0].equals("export")) {
        writeToFile(args[1], getConnectors());
      } else if (args.length == 2 && args[0].equals("import")) {
        setConnectors(readFromFile(args[1]), false);
      } else if (args.length == 2 && args[0].equals("import-no-remove")) {
        setConnectors(readFromFile(args[1]), true);
      } else {
        printUsage();
      }
    } finally {
      shutdown();
    }
  }

  /**
   * A utility to import/export connectors from/to an XML file.
   * <pre>
   * usage: ImportExport (export|import|import-no-remove) &lt;filename&gt;
   * </pre>
   */
  public static final void main(String[] args) throws Exception {
    ImportExport app = new ImportExport();
    app.run(app.parseArgs(args));
    System.exit(0);
  }
}
