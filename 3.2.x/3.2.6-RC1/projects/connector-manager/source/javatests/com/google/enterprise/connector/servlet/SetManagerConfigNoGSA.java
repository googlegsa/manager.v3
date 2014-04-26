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

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetManagerConfigNoGSA extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(SetManagerConfigNoGSA.class.getName());

  /**
   * Returns the manager config (form) for now.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    PrintWriter out = res.getWriter();
    out.print("<HTML><HEAD><TITLE>Set Manager Config</TITLE></HEAD>");
    out.print("<BODY><H3>Manager Config:</H3>");
    out.print("<HR><FORM METHOD=POST " +
      "ACTION=\"/connector-manager/setManagerConfigTest\"><TABLE>");
    out.println("<tr><td>GSA Host</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"" + ServletUtil.XMLTAG_FEEDERGATE_HOST
        + "\"></td></tr><tr>");
    out.println("<tr><td>GSA Port</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"" + ServletUtil.XMLTAG_FEEDERGATE_PORT
        + "\"></td></tr><tr>");
    out.println("<tr><td><INPUT TYPE=\"SUBMIT\" "
        + "NAME=\"action\" VALUE=\"submit\"></td></tr>");
    out.println("</TABLE></FORM></BODY></HTML>");
    out.close();
  }

  /**
   * Returns the simple response if successfully setting the manager config.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    StringWriter writer = new StringWriter();
    writer.write("<" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">");
    writer.write("<" + ServletUtil.XMLTAG_FEEDERGATE + " "
        + ServletUtil.XMLTAG_FEEDERGATE_HOST + "=\""
        + req.getParameter(ServletUtil.XMLTAG_FEEDERGATE_HOST) + "\" "
        + ServletUtil.XMLTAG_FEEDERGATE_PORT + "=\""
        + req.getParameter(ServletUtil.XMLTAG_FEEDERGATE_PORT) + "\"/>");
    writer.write("</" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">");
    writer.close();

    LOG.info(writer.getBuffer().toString());
    res.setContentType(ServletUtil.MIMETYPE_XML);

    // Get the URL for the Connector Manager servlet context.
    StringBuffer requestUrl = req.getRequestURL();
    int index = requestUrl.indexOf(req.getServletPath());
    if (index > 0) {
      requestUrl.setLength(index);
    }
    if (requestUrl.charAt(requestUrl.length() - 1) == '/') {
      requestUrl.setLength(requestUrl.length() - 1);
    }
    String webappUrl = requestUrl.toString();

    PrintWriter out = res.getWriter();
    Manager manager = Context.getInstance().getManager();
    SetManagerConfigHandler handler = new SetManagerConfigHandler(
        manager, writer.getBuffer().toString(), webappUrl);
    ServletUtil.writeResponse(out, handler.getStatus());
    out.close();
  }
}
