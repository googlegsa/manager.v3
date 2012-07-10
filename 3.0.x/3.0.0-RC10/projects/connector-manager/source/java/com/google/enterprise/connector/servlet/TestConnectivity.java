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
import com.google.enterprise.connector.manager.Context;

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

    // If feeding, check if caller is the configured feed host.
    boolean reqIsFeedHost = (Context.getInstance().isFeeding()) ?
      RemoteAddressFilter.getInstance().isFeedHost(req.getRemoteAddr()) : true;

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    NDC.pushAppend("Support");
    try {
      LOGGER.info("Hello from the TestConnectivity servlet!");
      handleDoGet(out, reqIsFeedHost);
    } finally {
      out.close();
      NDC.pop();
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
   * @param reqIsFeedHost true if the caller is the Feed Host
   */
  public static void handleDoGet(PrintWriter out, boolean reqIsFeedHost) {
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeManagerSplash(out);
    ServletUtil.writeStatusCode(out,
        (reqIsFeedHost) ? ConnectorMessageCode.SUCCESS
                        : ConnectorMessageCode.REQUESTOR_IS_NOT_FEED_HOST);
    ServletUtil.writeRootTag(out, true);
  }
}
