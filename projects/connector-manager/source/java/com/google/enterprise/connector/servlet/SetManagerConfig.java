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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;


public class SetManagerConfig extends HttpServlet {

  /**
   * Returns the simple response if successfully setting the manager config.
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   *
   */
  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
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
      throws ServletException, IOException
  {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    BufferedReader reader = req.getReader();
    res.setContentType(ServletUtil.MIMETYPE_XML);
    String xmlBody = StringUtils.readAllToString(reader);

    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    SetManagerConfigHandler hdl =
        new SetManagerConfigHandler(manager, xmlBody);
    ServletUtil.writeSimpleResponse(out, hdl.getStatus());
    out.close();
  }
}
