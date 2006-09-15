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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;


/**
 * Admin servlet to set connector config.
 * TODO: This is a temp solution since it is not able to talk to GSA. 
 * 
 */
public class SetConnectorConfig extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(GetConnectorList.class.getName());

  /**
   * Returns the connector config (form) for now.
   * TODO: doGet just call doPost.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    PrintWriter out = res.getWriter();
    out.print("<HTML><HEAD><TITLE>Set Connector Config</TITLE></HEAD>");
    out.print("<BODY><H3>Connector Config:</H3><UL>");
    out.print("</UL><HR><FORM METHOD=POST " +
      "ACTION=\"/connector-manager/setConnectorConfig\">");
    out.print("Connector Name   <INPUT TYPE=\"TEXT\" NAME=\"connectorName\"><BR>");
    out.print("Connector Type   <INPUT TYPE=\"TEXT\" NAME=\"connectorType\"><BR>");
    out.print("Name1   <INPUT TYPE=\"TEXT\" NAME=\"name1\"><BR>");
    out.print("Name2   <INPUT TYPE=\"TEXT\" NAME=\"name2\"><BR>");
    out.print("Name3   <INPUT TYPE=\"TEXT\" NAME=\"name3\"><BR>");
    out.print("<INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE=\"submit\">");
    out.print("</FORM></BODY></HTML>");
    out.close();
  }

  /**
   * Returns the simple response if successfully setting the connector config.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doPost(HttpServletRequest req,
                        HttpServletResponse res)
      throws ServletException, IOException {
    String status = "0";
    Map configData = new TreeMap();
    String lang = "en";
    String connectorName = req.getParameter("connectorName");
    String connectorType = req.getParameter("connectorType");
    configData.put("name1", req.getParameter("name1"));
    configData.put("name2", req.getParameter("name2"));
    configData.put("name3", req.getParameter("name3"));
    res.setContentType(ServletUtil.MIMETYPE_XML); //"text/plain"); //
    PrintWriter out = res.getWriter();
    MockManager mockManager = MockManager.getInstance();
    try {
      ConfigureResponse configRes =
      mockManager.setConnectorConfig(connectorName, configData, lang);
    } catch (ConnectorNotFoundException e) {
      LOG.info("ConnectorNotFoundException");
      status = e.toString();
      e.printStackTrace();
    } catch (PersistentStoreException e) {
      LOG.info("PersistentStoreException");
      status = e.toString();
      e.printStackTrace();
    }

    handleDoPost(out, status);
    out.close();
  }

  /**
   * Handler for doPost in order to do unit tests.
   * @param out PrintWriter Output for response
   * @param status String
   * 
   */
  public void handleDoPost(PrintWriter out, String status) {
     
    ServletUtil.writeSimpleResponse(out, status);
  }
}
