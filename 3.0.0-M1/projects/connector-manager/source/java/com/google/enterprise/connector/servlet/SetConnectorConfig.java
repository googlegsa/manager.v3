// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.PrintWriter;

/**
 * Admin servlet to set connector config.
 */
public class SetConnectorConfig extends ConnectorManagerServlet {
  /**
   * Writes the XML response for setting the connector config.
   */
  @Override
  protected void processDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    NDC.append("Config");
    try {
      SetConnectorConfigHandler handler =
          new SetConnectorConfigHandler(xmlBody, manager);
      ConnectorManagerGetServlet.writeConfigureResponse(out,
          handler.getStatus(), handler.getConfigRes(), handler.isUpdate());
    } finally {
      out.close();
    }
  }
}
