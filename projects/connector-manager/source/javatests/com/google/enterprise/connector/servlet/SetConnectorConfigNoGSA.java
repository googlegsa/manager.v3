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

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SetConnectorConfigNoGSA extends HttpServlet {

  /**
   * Returns the connector config (form) for now.
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
    out.print("<BODY><H3>Connector Config:</H3>");
    out.print("<HR><FORM METHOD=POST " +
      "ACTION=\"/connector-manager/setConnectorConfigTest\">");
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
    String lang = "en";
    String connectorName = req.getParameter("connectorName");
    String connectorType = req.getParameter("connectorType");
    StringWriter writer = new StringWriter();
    writer.write("<" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">");
    writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">"
        + connectorName + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">");
    writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">"
        + connectorType + "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">");
    writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS
        + " name=\"name1\" value=\"" + req.getParameter("name1") + "\"/>");
    writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS
        + " name=\"name2\" value=\"" + req.getParameter("name2") + "\"/>");
    writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS
        + " name=\"name3\" value=\"" + req.getParameter("name3") + "\"/>");
    writer.write("</" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">");
    writer.close();

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    SetConnectorConfigHandler handler = new SetConnectorConfigHandler(
        manager, lang, writer.getBuffer().toString());
    ServletUtil.writeConfigureResponse(
        out, handler.getStatus(), handler.getConfigRes());
    out.close();
  }
}
