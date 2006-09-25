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
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SetManagerConfigNoGSA extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(SetManagerConfigNoGSA.class.getName());

  /**
   * Returns the manager config (form) for now.
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
    out.print("<HTML><HEAD><TITLE>Set Manager Config</TITLE></HEAD>");
    out.print("<BODY><H3>Manager Config:</H3>");
    out.print("<HR><FORM METHOD=POST " +
      "ACTION=\"/connector-manager/setManagerConfigTest\"><TABLE>");
    out.println("<tr><td>Cert Authn <input type=\"checkbox\""
        + "NAME=\"" + ServletUtil.XMLTAG_CERT_AUTHN + "\"></td></tr>");
    out.println("<tr><td>GSA Host</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"" + ServletUtil.XMLTAG_FEEDERGATE_HOST
        + "\"></td></tr><tr>");
    out.println("<tr><td>GSA Port</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"" + ServletUtil.XMLTAG_FEEDERGATE_PORT
        + "\"></td></tr><tr>");
    out.println("<tr><td>Max Feed Rate</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"" + ServletUtil.XMLTAG_MAX_FEED_RATE + "\"></td></tr><tr>");
    out.println("<tr><td><INPUT TYPE=\"SUBMIT\" "
        + "NAME=\"action\" VALUE=\"submit\"></td></tr>");
    out.println("</TABLE></FORM></BODY></HTML>");
    out.close();
  }

  /**
   * Returns the simple response if successfully setting the manager config.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doPost(HttpServletRequest req,
                        HttpServletResponse res)
      throws ServletException, IOException {
    StringWriter writer = new StringWriter();
    writer.write("<" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">");
    writer.write("  <" + ServletUtil.XMLTAG_CERT_AUTHN + ">"
        + req.getParameter(ServletUtil.XMLTAG_CERT_AUTHN)
        + "</" + ServletUtil.XMLTAG_CERT_AUTHN + ">");
    writer.write("  <" + ServletUtil.XMLTAG_FEEDERGATE + " "
        + ServletUtil.XMLTAG_FEEDERGATE_HOST + "=\""
        + req.getParameter(ServletUtil.XMLTAG_FEEDERGATE_HOST) + "\" "
        + ServletUtil.XMLTAG_FEEDERGATE_PORT + "=\""
        + req.getParameter(ServletUtil.XMLTAG_FEEDERGATE_PORT) + "\"/>");
    writer.write("  <" + ServletUtil.XMLTAG_MAX_FEED_RATE + ">"
        + req.getParameter(ServletUtil.XMLTAG_MAX_FEED_RATE)
        + "</" + ServletUtil.XMLTAG_MAX_FEED_RATE + ">");
    writer.write("</" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">");
    writer.close();

    LOG.info(writer.getBuffer().toString());
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    SetManagerConfigHandler handler = new SetManagerConfigHandler(
        manager, writer.getBuffer().toString());
    ServletUtil.writeSimpleResponse(out, handler.getStatus());
    out.close();
  }
}
