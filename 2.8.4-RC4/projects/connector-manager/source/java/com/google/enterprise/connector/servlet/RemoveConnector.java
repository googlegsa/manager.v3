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

import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Admin servlet for removing a connector by a given name.
 */
public class RemoveConnector extends ConnectorManagerGetServlet {
  private static final Logger LOGGER =
      Logger.getLogger(RemoveConnector.class.getName());

  @Override
  protected void processDoGet(
      String connectorName, String lang, Manager manager, PrintWriter out) {
    handleDoGet(connectorName, manager, out);
  }

  /**
   * Handler for doGet in order to do unit tests.
   * Returns the simple response if successfully removing the manager config.
   *
   * @param manager Manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoGet(
      String connectorName, Manager manager, PrintWriter out) {
    ConnectorMessageCode status = new ConnectorMessageCode();
    try {
      manager.removeConnector(connectorName);
    } catch (ConnectorNotFoundException e) {
      status = new ConnectorMessageCode(
        ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
    } catch (InstantiatorException e) {
      status.setMessageId(ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
    } catch (PersistentStoreException e) {
      status.setMessageId(ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE, e);
    }

    ServletUtil.writeResponse(out, status);
  }

}
