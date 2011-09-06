// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.logging.NDC;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin servlet to test connectivity.
 */
public class TestConnectivity extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(TestConnectivity.class.getName());

  /**
   * Returns a simple acknowledgement.
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
          .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    NDC.push("Support Manager");
    try {
      LOGGER.info("Hello from the TestConnectivity servlet!");
      handleDoGet(out);
    } finally {
      out.close();
      NDC.clear();
    }
  }

  /**
   * Returns a simple acknowledgement.
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
   * Handler for doGet in order to do unit tests.
   *
   * @param out
   */
  public static void handleDoGet(PrintWriter out) {
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeManagerSplash(out);
    ServletUtil.writeStatusId(out, ConnectorMessageCode.SUCCESS);
    ServletUtil.writeRootTag(out, true);
  }
}
