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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * The main purpose of this servlet is to have its "init" method called when the
 * container starts up. This is by done by means of the web.xml file. But I also
 * gave it a get and post that do the same thing.
 *
 */
public class StartUp extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(StartUp.class.getName());

  @Override
  public void init() throws ServletException {
    NDC.push("Init");
    try {
      LOGGER.info("init");
      ServletContext servletContext = this.getServletContext();
      doConnectorManagerStartup(servletContext);
      LOGGER.info("init done");
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Connector Manager Startup failed: ", ioe);
      Context.getInstance().setInitFailureCause(ioe);
      throw new ServletException("Connector Manager Startup failed", ioe);
    } catch (RuntimeException re) {
      LOGGER.log(Level.SEVERE, "Connector Manager Startup failed: ", re);
      Context.getInstance().setInitFailureCause(re);
      throw re;
    } catch (Error e) {
      LOGGER.log(Level.SEVERE, "Connector Manager Startup failed: ", e);
      Context.getInstance().setInitFailureCause(e);
      throw e;
    } finally {
      NDC.remove();
    }
  }

  @Override
  public void destroy() {
    NDC.push("Shutdown");
    try {
      LOGGER.info("destroy");
      Context.getInstance().shutdown(true);
    } finally {
      NDC.remove();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doPost(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    ServletContext servletContext = this.getServletContext();
    doConnectorManagerStartup(servletContext);
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    PrintWriter out = res.getWriter();
    out.println("<HTML><HEAD><TITLE>Connector Manager Started</TITLE></HEAD>"
        + "<BODY>Connector manager has been successfully started.</BODY>"
        + "</HTML>");
    out.close();
    LOGGER.info("Connector Manager started.");
  }

  private void doConnectorManagerStartup(ServletContext servletContext)
      throws IOException {
    LOGGER.info(ServletUtil.getManagerSplash());

    // read in and set initialization parameters
    String kp = servletContext.getInitParameter("keystore_passwd_file");
    if (!Strings.isNullOrEmpty(kp)) {
      EncryptedPropertyPlaceholderConfigurer.setKeyStorePasswdPath(
         getRealPath(servletContext, kp));
    }

    String ks = servletContext.getInitParameter("keystore_file");
    if (!Strings.isNullOrEmpty(ks)) {
      EncryptedPropertyPlaceholderConfigurer.setKeyStorePath(
          getRealPath(servletContext, ks));
    }

    String kt = servletContext.getInitParameter("keystore_type");
    if (!Strings.isNullOrEmpty(kt)) {
      EncryptedPropertyPlaceholderConfigurer.setKeyStoreType(kt);
    }

    String ka = servletContext.getInitParameter("keystore_crypto_algo");
    if (!Strings.isNullOrEmpty(ka)) {
      EncryptedPropertyPlaceholderConfigurer.setKeyStoreCryptoAlgo(ka);
    }

    // Note: default context location is /WEB-INF/applicationContext.xml
    LOGGER.info("Making an XmlWebApplicationContext");
    XmlWebApplicationContext ac = new XmlWebApplicationContext();
    ac.setServletContext(servletContext);
    ac.refresh();

    Context context = Context.getInstance();
    context.setServletContext(ac, servletContext.getRealPath("/WEB-INF"));
    context.start();
  }

  /**
   * Tries to normalize a pathname, as if relative to the context.
   * Absolute paths are allowed (unlike traditional web-app behaviour).
   * file: URLs are allowed as well and are treated like absolute paths.
   * All relative paths are made relative the the web-app WEB-INF directory.
   * Attempts are made to recognize paths that are already relative to
   * WEB-INF (they begin with WEB-INF or /WEB-INF).
   *
   * @param servletContext the ServletContext
   * @param name the file name
   */
  private String getRealPath(final ServletContext servletContext, String name)
      throws IOException {
    return ServletUtil.getRealPath(name,
        new Function<String, String>() {
          public String apply(String path) {
            // If servlet container cannot translated the virtual path to a
            // real path, so use the supplied path.
            String realPath = servletContext.getRealPath("/WEB-INF/" + path);
            return (realPath == null) ? path : realPath;
          }
        });
  }
}
