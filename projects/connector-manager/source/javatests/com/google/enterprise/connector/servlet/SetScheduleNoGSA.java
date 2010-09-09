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

public class SetScheduleNoGSA extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(SetScheduleNoGSA.class.getName());

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
    out.print("<HTML><HEAD><TITLE>Set Schedule</TITLE></HEAD>");
    out.print("<BODY><H3>Schedule:</H3>");
    out.print("<HR><FORM METHOD=POST " +
      "ACTION=\"/connector-manager/setScheduleTest\"><TABLE>");
    out.println("<tr><td>Connector Name</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "\"></td></tr>");
    out.println("<tr><td>Load</td> <td><INPUT TYPE=\"TEXT\" "
        + "NAME=\"load\"></td></tr>");
    out.print("<tr><td><INPUT type=\"radio\" NAME=\"forTime\" VALUE=\"all\">For all day/night</td></tr>");
    out.print("<tr><td><INPUT type=\"radio\" NAME=\"forTime\" VALUE=\"some\">For some time intervals</td></tr>");
    out.print("<tr><td><TEXTAREA NAME=\"" + ServletUtil.XMLTAG_TIME_INTERVALS
        + "\" rows=\"10\" cols=\"16\"></TEXTAREA></td></tr>");
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
    String forTime = req.getParameter("forTime");
    String timeIntervals = null;
    if (forTime.equalsIgnoreCase("some")) {
      timeIntervals = req.getParameter(ServletUtil.XMLTAG_TIME_INTERVALS);
    } else {
      timeIntervals = "0-24";
    }
    StringWriter writer = new StringWriter();
    writer.write("<" + ServletUtil.XMLTAG_CONNECTOR_SCHEDULES + ">\n");
    writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">"
        + req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME)
        + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n");
    writer.write("  <" + ServletUtil.XMLTAG_LOAD + ">"
        + req.getParameter(ServletUtil.XMLTAG_LOAD) + "</"
        + ServletUtil.XMLTAG_LOAD + ">\n");
    writer.write("  <" + ServletUtil.XMLTAG_TIME_INTERVALS + ">");
    if (timeIntervals.indexOf("\r\n") != -1) {
      writer.write(timeIntervals.replaceAll("\r\n", ":"));
    } else {
      writer.write(timeIntervals.replace('\n', ':'));
    }
    writer.write("</" + ServletUtil.XMLTAG_TIME_INTERVALS + ">\n");
    writer.write("</" + ServletUtil.XMLTAG_CONNECTOR_SCHEDULES + ">\n");
    writer.close();

    LOG.info(writer.getBuffer().toString());
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    Manager manager = Context.getInstance().getManager();
    ConnectorMessageCode status = SetSchedule.handleDoPost(writer.getBuffer().toString(),
        manager);
    ServletUtil.writeResponse(out, status);
    out.close();
  }
}
