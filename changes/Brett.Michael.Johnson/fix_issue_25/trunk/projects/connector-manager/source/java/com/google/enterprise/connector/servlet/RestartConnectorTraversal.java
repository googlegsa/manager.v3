// Copyright (C) 2006 Google Inc.
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


package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Admin servlet to restart repository traversal for a given connector.
 * This will start crawing the repository from the begining, reindexing
 * the contents.
 */
public class RestartConnectorTraversal extends ConnectorManagerGetServlet {
  private static final Logger LOGGER =
      Logger.getLogger(RestartConnectorTraversal.class.getName());

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.servlet.ConnectorManagerGetServlet#
   *      processDoGet(java.lang.String, java.lang.String,
   *      com.google.enterprise.connector.manager.Manager,
   *      java.io.PrintWriter)
   */
  protected void processDoGet(String connectorName, String lang,
      Manager manager, PrintWriter out) {
    handleDoGet(connectorName, manager, out);
  }

  /**
   * Handler for doGet in order to do unit tests. Returns the connector
   * status.
   *
   * @param connectorName
   * @param manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoGet(String connectorName, Manager manager,
      PrintWriter out) {
    // First check the status of the connector to make sure it exists.
    ConnectorStatus connectorStatus = manager.getConnectorStatus(connectorName);
    if (connectorStatus == null) {
      ServletUtil.writeResponse(out, new ConnectorMessageCode(
          ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_STATUS, connectorName));
      LOGGER.warning(
          "Connector manager returns no status for " + connectorName);
      return;
    }

    try {
      // Force the connector to reindex the repository.
      manager.restartConnectorTraversal(connectorName);
    } catch (ConnectorNotFoundException cnfe) {
      ServletUtil.writeResponse(out, new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName));
      LOGGER.warning("Connector not found " + connectorName);
      return;
    } catch (InstantiatorException ie) {
      ServletUtil.writeResponse(out, new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_INSTANTIATOR, connectorName));
      LOGGER.warning(ie.getMessage());
      return;
    }

    // Write out the connector status.
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeMessageCode(out, new ConnectorMessageCode());
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_RESTART,
        false);
    ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_NAME,
        connectorStatus.getName());
    ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE,
        connectorStatus.getType());
    ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_STATUS,
        Integer.toString(connectorStatus.getStatus()));
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_RESTART,
        true);
    ServletUtil.writeRootTag(out, true);
   }
}
