// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.servlet.ServletUtil;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@code List} of {@link ImportExportConnector}s that can be serialized
 * to and from XML.
 */
public class ImportExportConnectorList extends ArrayList<ImportExportConnector> {
  private static final Logger LOGGER =
      Logger.getLogger(ImportExportConnectorList.class.getName());

  /**
   * Deserializes a List of ImportExportConnectors from XML.
   *
   * @param connectorsElement a ConnectorInstances XML Element.
   * @param connectorClass a class whose {@code newInstance()} method is used to
   *        create ImportExportConnector instances that are added to the List.
   */
  public void fromXml(Element connectorsElement,
      Class<? extends ImportExportConnector> connectorClass) {
    NodeList connectorElements = connectorsElement.getElementsByTagName(
        ServletUtil.XMLTAG_CONNECTOR_INSTANCE);
    for (int i = 0; i < connectorElements.getLength(); i++) {
      Element connectorElement = (Element) connectorElements.item(i);
      try {
        ImportExportConnector connector = connectorClass.newInstance();
        connector.fromXml(connectorElement);
        add(connector);
      } catch (java.lang.InstantiationException ie) {
        LOGGER.log(Level.SEVERE,
            "Failed to create ImportExportConnector instance: ", ie);
      } catch (java.lang.IllegalAccessException iae) {
        LOGGER.log(Level.SEVERE,
            "Failed to create ImportExportConnector instance: ", iae);
      }
    }
  }

  /**
   * Serializes this List of Connectors to an XML output stream.
   *
   * @param out PrintWriter to write XML output.
   * @param indent starting indent for the XML tags.
   */
  public void toXml(PrintWriter out, int indent) {
    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_INSTANCES, false);

    for (ImportExportConnector connector : this) {
      connector.toXml(out, indent + 1);
    }

    ServletUtil.writeXMLTag(out, indent,
        ServletUtil.XMLTAG_CONNECTOR_INSTANCES, true);
  }
}
