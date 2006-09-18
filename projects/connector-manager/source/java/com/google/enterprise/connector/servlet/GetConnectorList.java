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

import com.google.enterprise.connector.manager.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get a list of connector types.
 * 
 */
public class GetConnectorList extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(GetConnectorList.class.getName());

  /**
   * Returns a list of connector types.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    List connectorTypes = manager.getConnectorTypes();
    handleDoGet(out, connectorTypes);
    out.close();
  }

  /**
   * Returns a list of connector types.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doGet(req, res);
  }

  /**
   * Handler for doGet in order to do unit tests.
   * 
   * @param out
   * @param connectorTypes
   */
  public static void handleDoGet(PrintWriter out, List connectorTypes) {
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
    ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, "0");

    if (connectorTypes == null || connectorTypes.size() == 0) {
      logger.info("Connector manager returns null.");
      ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_CONNECTOR_TYPES,
          "null");
      ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
      return;
    }

    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_TYPES, false);

    for (Iterator iter = connectorTypes.iterator(); iter.hasNext();) {
      ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE,
          (String) iter.next());
    }

    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_TYPES, true);
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
  }
}
