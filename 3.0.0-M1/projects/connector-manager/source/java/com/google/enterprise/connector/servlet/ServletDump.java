// Copyright 2010 Google Inc.
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
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility to dump servlet request and responts packages in human-
 * readable form.  Useful for debugging purposes, for instace, as
 * called from doTrace().
 */
/* Moved out of ServletUtil, to avoid having ServletUtil depend upon
 * servlet-api.jar.
 */
public class ServletDump {

  private static Logger LOGGER =
    Logger.getLogger(ServletDump.class.getName());

  private ServletDump() {
    // Prevent Instantiation.
  }

  /**
   * For Debugging: Write out the HttpServletRequest information.
   * This writes an XML stream to the response output that describes
   * most of the data received in the request structure.  It returns
   * true, so that you may call it from doGet() like:
   * <code>  if (dumpServletRequest(req, res)) return;</code>
   * without javac complaining about unreachable code with a straight
   * return.
   *
   * @param req An HttpServletRequest
   * @param res An HttpServletResponse
   * @return true
   */
  public static boolean dumpServletRequest(HttpServletRequest req,
      HttpServletResponse res) throws IOException {
    res.setContentType(ServletUtil.MIMETYPE_XML);
    StringWriter stringWriter = new StringWriter();
    PrintWriter out = new PrintWriter(stringWriter);
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeXMLTag(out, 2, "HttpServletRequest", false);
    ServletUtil.writeXMLElement(out, 3, "Method", req.getMethod());
    ServletUtil.writeXMLElement(out, 3, "AuthType", req.getAuthType());
    ServletUtil.writeXMLElement(out, 3, "ContextPath", req.getContextPath());
    ServletUtil.writeXMLElement(out, 3, "PathInfo", req.getPathInfo());
    ServletUtil.writeXMLElement(out, 3, "PathTranslated",
                                req.getPathTranslated());
    ServletUtil.writeXMLElement(out, 3, "QueryString", req.getQueryString());
    ServletUtil.writeXMLElement(out, 3, "RemoteUser", req.getRemoteUser());
    ServletUtil.writeXMLElement(out, 3, "RequestURI", req.getRequestURI());
    ServletUtil.writeXMLElement(out, 3, "RequestURL",
                                req.getRequestURL().toString());
    ServletUtil.writeXMLElement(out, 3, "ServletPath", req.getServletPath());
    ServletUtil.writeXMLTag(out, 3, "Headers", false);
    for (Enumeration<?> names = req.getHeaderNames(); names.hasMoreElements(); ) {
      String name = (String)(names.nextElement());
      for (Enumeration<?> e = req.getHeaders(name); e.hasMoreElements(); )
        ServletUtil.writeXMLElement(out, 4, name, (String)(e.nextElement()));
    }
    ServletUtil.writeXMLTag(out, 3, "Headers", true);
    ServletUtil.writeXMLTag(out, 2, "HttpServletRequest", true);
    ServletUtil.writeXMLTag(out, 2, "ServletRequest", false);
    ServletUtil.writeXMLElement(out, 3, "Protocol", req.getProtocol());
    ServletUtil.writeXMLElement(out, 3, "Scheme", req.getScheme());
    ServletUtil.writeXMLElement(out, 3, "ServerName", req.getServerName());
    ServletUtil.writeXMLElement(out, 3, "ServerPort",
                                String.valueOf(req.getServerPort()));
    ServletUtil.writeXMLElement(out, 3, "RemoteAddr", req.getRemoteAddr());
    ServletUtil.writeXMLElement(out, 3, "RemoteHost", req.getRemoteHost());
    Enumeration<?> names;
    ServletUtil.writeXMLTag(out, 3, "Attributes", false);
    for (names = req.getAttributeNames(); names.hasMoreElements(); ) {
      String name = (String)(names.nextElement());
      ServletUtil.writeXMLElement(out, 4, name,
                                  req.getAttribute(name).toString());
    }
    ServletUtil.writeXMLTag(out, 3, "Attributes", true);
    ServletUtil.writeXMLTag(out, 3, "Parameters", false);
    for (names = req.getParameterNames(); names.hasMoreElements(); ) {
      String name = (String)(names.nextElement());
      String[] params = req.getParameterValues(name);
      for (int i = 0; i < params.length; i++)
        ServletUtil.writeXMLElement(out, 4, name, params[i]);
    }
    ServletUtil.writeXMLTag(out, 3, "Parameters", true);
    ServletUtil.writeXMLTag(out, 2, "ServletRequest", true);
    ServletUtil.writeRootTag(out, true);
    out.close();

    String dumpStr = stringWriter.getBuffer().toString();
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("HttpRequest handled by "
                  + sun.reflect.Reflection.getCallerClass(2).getName()
                  + "\ncalled by "
                  + sun.reflect.Reflection.getCallerClass(3).getName()
                  + "\n" + dumpStr);
    }

    // Now stuff it out to the response.
    out = res.getWriter();
    out.write(dumpStr);
    out.close();

    return true;
  }
}
