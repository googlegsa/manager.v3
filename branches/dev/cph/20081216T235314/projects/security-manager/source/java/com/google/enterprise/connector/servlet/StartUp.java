// Copyright (C) 2008 Google Inc.
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

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.manager.Context;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The main purpose of this servlet is to have its "init" method called when the
 * container starts up. This is by done by means of the web.xml file. But it
 * also has a get and post that do the same thing.
 */
public class StartUp extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(StartUp.class.getName());

  @Override
  public void init() {
    LOGGER.info("init");
    ServletContext servletContext = this.getServletContext();
    doManagerStartup(servletContext);
    LOGGER.info("init done");
  }

  @Override
  public void destroy() {
    LOGGER.info("destroy");
    Context.getInstance().shutdown(true);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doPost(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    ServletContext servletContext = this.getServletContext();
    doManagerStartup(servletContext);
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    PrintWriter out = res.getWriter();
    out.println("<HTML><HEAD><TITLE>Connector Manager Started</TITLE></HEAD>"
        + "<BODY>Connector manager has been successfully started.</BODY>" + "</HTML>");
    out.close();
    LOGGER.info("Security Manager started.");
  }

  public static void doManagerStartup(ServletContext servletContext) {
    LOGGER.info(getManagerSplash());

    Context context = Context.getInstance(servletContext);
    context.start();
  }

  public static final String MANAGER_NAME = "Google Enterprise Security Manager";

  /**
   * Get Connector Manager, OS, JVM version information.
   */
  public static String getManagerSplash() {
    return MANAGER_NAME + " " + JarUtils.getJarVersion(ServletUtil.class) + "; "
        + System.getProperty("java.vendor") + " " + System.getProperty("java.vm.name") + " "
        + System.getProperty("java.version") + "; " + System.getProperty("os.name") + " "
        + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
  }
}
