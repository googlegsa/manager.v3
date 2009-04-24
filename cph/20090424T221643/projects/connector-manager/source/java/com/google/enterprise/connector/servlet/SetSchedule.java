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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.scheduler.Schedule;

import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin servlet for SetSchedule
 *
 */
public class SetSchedule extends ConnectorManagerServlet {
  private static final Logger LOGGER =
    Logger.getLogger(SetSchedule.class.getName());

  /**
   * Writes the XML response for setting the schedule.
   */
  @Override
  protected void processDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    ConnectorMessageCode status = handleDoPost(xmlBody, manager);
    ServletUtil.writeResponse(out, status);
  }

  /**
   * Returns an error code (ConnectorMessageCode) for setting the schedule.
   *
   * @param xmlBody String the XML request body string
   * @param manager Manager
   */
  public static ConnectorMessageCode handleDoPost(
      String xmlBody, Manager manager) {
    ConnectorMessageCode status = new ConnectorMessageCode();
    Element root = ServletUtil.parseAndGetRootElement(
        xmlBody, ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);
    if (root == null) {
      status.setMessageId(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return status;
    }

    String connectorName = ServletUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_CONNECTOR_NAME);
    int load = Integer.parseInt(ServletUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_LOAD));
    boolean disabled = (ServletUtil.getFirstElementByTagName(root,
        ServletUtil.XMLTAG_DISABLED) != null);
    int retryDelayMillis = Schedule.defaultRetryDelayMillis();
    String delayStr = ServletUtil.getFirstElementByTagName(root,
        ServletUtil.XMLTAG_DELAY);
    if (delayStr != null) {
      retryDelayMillis = Integer.parseInt(delayStr);
    }
    String timeIntervals = ServletUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_TIME_INTERVALS);

    Schedule schedule = new Schedule(connectorName, disabled, load,
        retryDelayMillis, timeIntervals);

    // TODO: Remove this when the GSA enforces lowercase connector names.
    // Until then, this hack tries to determine if we are setting the
    // schedule for a newly created connector or an existing connector.
    if (!connectorName.equals(connectorName.toLowerCase())) {
      try {
        manager.getConnectorConfig(connectorName);
      } catch (ConnectorNotFoundException e) {
        connectorName = connectorName.toLowerCase();
      }
    }

    try {
      manager.setSchedule(connectorName, schedule.toString());
    } catch (ConnectorNotFoundException e) {
      status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName);
      LOGGER.log(
          Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
    } catch (PersistentStoreException e) {
      status.setMessageId(ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE, e);
    }

    return status;
  }
}
