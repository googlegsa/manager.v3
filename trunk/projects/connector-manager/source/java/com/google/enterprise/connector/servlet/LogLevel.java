// Copyright 2011 Google Inc.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;

/**
 * <p>Admin servlet to set and fetch the logging {@link Level} for the
 * Connector Manager's Connector logs or Feed logs.
 * <p>
 * This servlet allows the user to alter the level of logging detail
 * dynamically, without restarting the Connector Manager.  More detailed
 * logs are especially useful when trying to troubleshoot connector problems.
 * <p>
 * Connector logs contain the status of the Connector Manager and all
 * Connector instances as they perform their various tasks.
 * Feed logs identify each document, and it associated meta-data, as
 * they are feed to the GSA.
 * <p>
 * The recognized logging levels in increasing levels of detail are:
 * <ul><li>{@code SEVERE} - Catastrophic failure; the Connector cannot continue
 *         running.</li>
 * <li>{@code WARNING} - Unexpected exceptional conditions, the product may
 *     not function properly.</li>
 * <li>{@code INFO} - Informational message. This is the default connector
 *     logging level upon installation.</li>
 * <li>{@code CONFIG} - Configuration details and higher product functions.</li>
 * <li>{@code FINE} - Generally information related to batches of documents.</li>
 * <li>{@code FINER} - Generally information related individual documents.</li>
 * <li>{@code FINEST} - Generally information related to document meta-data,
 *     processing detail that is exceptionally verbose.</li>
 * </ul>
 * <p>In addition to the above logging levels, the following psuedo-levels are
 * recognized:
 * <ul><li>{@code OFF} - Logging is turned off.  This is the default feed
 *         logging level upon installation.</li>
 * <li>{@code ALL} - Enable most detailed logging (alias of {@code FINEST}).</li>
 * </ul>
 * {@code ALL} and {@code OFF} are more conveniently used with feed logging,
 * which is currently an "all or nothing" style implementation.
 * <p>
 * After altering the desired logging level, allow the product to run for
 * the prescribed time (anywhere from several minutes to days).  Then the
 * {@link com.google.enterprise.connector.servlet.GetConnectorLogs GetConnectorLogs}
 * servlet may subsequently be used to fetch the accumulated log files for
 * analysis.
 *
 * <p><b>Usage:</b>
 * <br>To fetch the current connector logging level:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConnectorLogLevel</pre>
 * </p>
 * <p>To set the connector logging level:
 * <br><pre>  http://[cm_host_addr]/connector-manager/setConnectorLogLevel?level=[level]</pre>
 * <br>where [level] is one of the previously defined logging levels.  Setting
 * the connector logging level to {@code INFO} restores it to the default
 * configuration.  Setting the connector logging level to {@code OFF} is
 * not recommended.
 * <p>For instance:
 * <br><pre>  http://[cm_host_addr]/connector-manager/setConnectorLogLevel?level=ALL</pre>
 *
 * <p><br>To fetch the current feed logging level:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogLevel</pre></p>
 *
 * <p>To set the feed logging level:
 * <br><pre>  http://[cm_host_addr]/connector-manager/setFeedLogLevel?level=[level]</pre>
 * <br>where [level] is one of the previously defined logging levels.  For
 * feed logs, the recommended levels are {@code OFF} and {@code ALL}.
 * <p>For instance:
 * <br><pre>  http://[cm_host_addr]/connector-manager/setFeedLogLevel?level=ALL</pre>
 */
public class LogLevel extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(LogLevel.class.getName());

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
   * Sets Logging levels for connectors and feeds.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doPost(req, res);
  }

  /**
   * Sets Logging levels for connectors and feeds.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
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
      // Are we setting the Level for Connector logs or Feed logs?
      LogLevelHandler handler;
      if (req.getServletPath().indexOf("Feed") > 0) {
        handler = new FeedLogLevelHandler();
      } else {
        handler = new ConnectorLogLevelHandler();
      }

      handleDoPost(req.getServletPath(), handler, req.getParameter("level"),
                   out);

    } finally {
      out.close();
      NDC.pop();
    }
  }

  /**
   * Sets Logging levels for connectors and feeds.
   *
   * @param servletName the name of the servlet
   * @param handler a LogLevelHandler
   * @param logLevel new logging Level
   * @param out a PrintWriter
   * @throws IOException
   */
  // TODO: This extracted method is now testable, so write some tests.
  private void handleDoPost(String servletName, LogLevelHandler handler,
      String logLevel, PrintWriter out) throws IOException {
    try {
      // Set the logging level, if one was specified.
      if (!Strings.isNullOrEmpty(logLevel)) {
        Level level = getLevelByName(logLevel.toUpperCase());
        LOGGER.config("Setting " + handler.getName()
                      + " Logging level to " + level.getName());
        handler.getLogger().setLevel(level);
        handler.persistLevel(level);
      }

      // Return Status of the current logging level for the handler.
      ServletUtil.writeRootTag(out, false);
      ServletUtil.writeMessageCode(out, new ConnectorMessageCode());
      String currentLevel = getLogLevel(handler.getLogger()).getName();
      ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_LEVEL,
                                  currentLevel);
      ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_INFO,
          handler.getName() + " Logging level is " + currentLevel);
      ServletUtil.writeRootTag(out, true);
    } catch (ConnectorManagerException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      // TODO: These should really be new ConnectorMessageCodes
      ServletUtil.writeResponse(out, new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_HTTP_SERVLET,
          servletName + " - " + e.getMessage()));
    }
  }

  /**
   * Returns the current logging {@link Level} for the specified
   * {@link Logger}.  If {@code logger} has no explicitly configured
   * logging level, the {@code Level} of the nearest configured ancester
   * {@code Logger} is returned.
   *
   * @param logger a {@link Logger}
   * @return the current logging {@link Level} for {@code logger}
   */
  private Level getLogLevel(Logger logger) {
    while (logger != null) {
      Level level = logger.getLevel();
      if (level != null) {
        return level;
      }
      logger = logger.getParent();
    }
    return Level.OFF;
  }

  /**
   * Returns the logging {@link Level} for the given name.
   *
   * @param name the name of a logging {@link Level}
   * @return the logging {@link Level} identified by {@code name}
   * @throws ConnectorManagerException if {@code name} is not a valid
   *         {@link Level}
   */
  private Level getLevelByName(String name) throws ConnectorManagerException {
    try {
      return Level.parse(name);
    } catch (IllegalArgumentException e) {
      throw new ConnectorManagerException("Unknown logging level: " + name, e);
    }
  }

  /**
   * Abstract access to either the Connector Logs or the Feed Logs.
   */
  private static interface LogLevelHandler {
    /**
     * Returns a descriptive name of the {@code LogLevelHandler}.
     */
    public String getName();

    /**
     * Returns the {@link Logger} managed by this {@code LogLevelHandler}.
     */
    public Logger getLogger() throws ConnectorManagerException;

    /**
     * Persists the specified {@link Level} for this {@code LogLevelHandler}.
     *
     * @param level the logging level to save.
     */
    public void persistLevel(Level level) throws ConnectorManagerException;
  }

  /**
   * A LogLevelHandler for connector logs, as configured in logging.properties.
   */
  private static class ConnectorLogLevelHandler implements LogLevelHandler {
    private final String LOGGER_NAME = ""; // root logger
    Context context = Context.getInstance();

    @Override
    public String getName() {
      return "Connector";
    }

    @Override
    public Logger getLogger() {
      return Logger.getLogger(LOGGER_NAME);
    }

    @Override
    public void persistLevel(Level level) throws ConnectorManagerException {
      File confFile = new File(new File(context.getCommonDirPath(), "classes"),
                               "logging.properties");
      if (!persistLevel(level, confFile)) {
        String filename = System.getProperty("java.util.logging.config.file");
        if (!Strings.isNullOrEmpty(filename)) {
          persistLevel(level, new File(filename));
        }
      }
    }

    /** Returns true if Level was successfully persisted. */
    private boolean persistLevel(Level level, File confFile)
        throws ConnectorManagerException {
      if (confFile.canRead() && confFile.canWrite()) {
        try {
          Properties props = loadProperties(confFile);
          props.setProperty(LOGGER_NAME + ".level", level.getName());
          storeProperties(confFile, props);
        } catch (IOException e) {
          throw new ConnectorManagerException(
              "Failed to save logging properties", e);
        }
        return true;
      } else {
        return false;
      }
    }

    private Properties loadProperties(File propFile) throws IOException {
      Properties props = new Properties();
      props.load(new FileInputStream(propFile));
      return props;
    }

    private void storeProperties(File propFile, Properties props)
        throws IOException {
      File backupFile =
          new File(propFile.getParent(), propFile.getName() + ".bak");
      // Back up the existing logging.properties file, because
      // Properties.store() makes a mess of the output file.
      if (!backupFile.exists()) {
        propFile.renameTo(backupFile);
      }
      // TODO: Try to preserve logging.properties comments and order.
      props.store(new FileOutputStream(propFile),
                  "Modified by Connector Manager LogLevel Servlet");
    }
  }

  /**
   * A LogLevelHandler for feedlogs, as configured in applicationContext.properties.
   */
  private static class FeedLogLevelHandler implements LogLevelHandler {
    Context context = Context.getInstance();

    @Override
    public String getName() {
      return "Feed";
    }

    @Override
    public Logger getLogger() {
      return (Logger) context.getApplicationContext()
          .getBean("FeedWrapperLogger", Logger.class);
    }

    @Override
    public void persistLevel(Level level) {
      try {
        Properties props = context.getConnectorManagerProperties();
        props.setProperty("feedLoggingLevel", level.getName());
        context.storeConnectorManagerProperties(props);
      } catch (PropertiesException pe) {
        LOGGER.log(Level.WARNING, "Failed to save Connector Logging Level", pe);
      }
    }
  }
}
