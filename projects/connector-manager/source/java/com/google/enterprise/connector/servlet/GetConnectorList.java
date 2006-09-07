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
     * @param req 
     * @param res 
     * @throws ServletException 
     * @throws IOException 
     * 
     */
  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType(ServletUtil.MimeTypeXML);
    PrintWriter out = res.getWriter();

    ServletUtil.AddXMLTag(out, 0, ServletUtil.XMLTagResponseRoot, false);
    ServletUtil.WriteElement(out, 1, ServletUtil.XMLTagStatusId, "0");

    MockManager mockManager = MockManager.getInstance();
    List connectorType = mockManager.getConnectorTypes();
    if (connectorType == null || connectorType.size() == 0) {
      logger.info("Connector manager returns null.");
      ServletUtil.AddXMLTag(
          out, 0, ServletUtil.XMLTagResponseRoot, true);
      ServletUtil.WriteElement(
          out, 1, ServletUtil.XMLTagConnectorTypes, "null");
      return;
    }

    ServletUtil.AddXMLTag(
            out, 1, ServletUtil.XMLTagConnectorTypes, false);

    for (Iterator iter = connectorType.iterator(); iter.hasNext(); ) {
      ServletUtil.WriteElement(
                out, 2, ServletUtil.XMLTagConnectorType, (String) iter.next());
    }

    ServletUtil.AddXMLTag(
            out, 1, ServletUtil.XMLTagConnectorTypes, true);
    ServletUtil.AddXMLTag(out, 0, ServletUtil.XMLTagResponseRoot, true);
    out.close();
  }

  /**
   * Returns a list of connector types.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doPost(HttpServletRequest req,
                        HttpServletResponse res)
      throws ServletException, IOException {
    doGet(req, res);
  }
}
