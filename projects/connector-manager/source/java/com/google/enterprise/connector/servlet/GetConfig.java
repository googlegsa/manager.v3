// Copyright 2008 Google Inc.
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

import com.google.enterprise.connector.importexport.ExportConnectors;
import com.google.enterprise.connector.importexport.ExportManager;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Admin servlet to retrieve the configuration of the Connector Manager
 * and all Connector instances.  This servlet returns an XML document
 * containing the configuration.  Access to this servlet is restricted to
 * either localhost or gsa.feed.host, based upon the HTTP RemoteAddress.</p>
 *
 * <p><b>Usage:</b>
 * <br>To retrieve the configuration as an XML document:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConfig</pre>
 * </p>
 */
public class GetConfig extends HttpServlet {

  /**
   * Retrieves the Configuration files for the Connector Manager and all the
   * Connector instances.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doGet(req, res);
  }

  /**
   * Retrieves the configuration data for the Connector Manager and all
   * Connector instances.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.RED, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    NDC.pushAppend("Support");
    try {
      handleDoGet(out);
    } finally {
      out.close();
      NDC.pop();
    }
  }

  /**
   * Specialized {@code doTrace} method that constructs an XML representation
   * of the given request and returns it as the response.
   */
  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    ServletDump.dumpServletRequest(req, res);
  }

  /**
   * Handler for doGet in order to do unit tests.
   *
   * @param out PrintWriter where the response is written
   * @throws IOException
   */
  public static void handleDoGet(PrintWriter out)
      throws IOException {
    Context context = Context.getInstance();
    ExportConnectors exportConnectors = (ExportConnectors)
        context.getRequiredBean("ExportConnectors", ExportConnectors.class);
    new ExportManager().toXml(out, 0, exportConnectors.getConnectors());
  }
}
