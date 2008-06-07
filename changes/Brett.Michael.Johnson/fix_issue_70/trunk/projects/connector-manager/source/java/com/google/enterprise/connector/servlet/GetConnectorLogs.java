// Copyright 2008 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.pusher.FeedFileHandler;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;


/**
 * An Admin servlet to retrieve the log files from the connector manager.
 * This servlet allows the user to list available log files, fetch individual
 * log files, and fetch a ZIP archive of all the log files.  At this time
 * the user may retrieve either connector log files or feed log files.
 *
 * Usage:
 * -----
 * To list the available connector log files:
 *   http://[cm_host_addr]/connector_manager/getConnectorLogs
 *
 * To view an individual connector log file:
 *   http://[cm_host_addr]/connector_manager/getConnectorLogs/[log_file_name]
 * where [log_file_name] is the name of one log files returned by the list.
 * For instance, 'google-connectors.otex0.log'.  As a convenience, the log
 * name may be simply the log file generation number, '0' in the above example,
 * and it gets automatically expanded.
 *
 * To retrieve a ZIP archive of all the connector log files:
 *   http://[cm_host_addr]/connector_manager/getConnectorLogs/*
 * or
 *   http://[cm_host_addr]/connector_manager/getConnectorLogs/ALL
 *
 * To list the available feed log files:
 *   http://[cm_host_addr]/connector_manager/getFeedLogs
 *
 * To view an individual feed log file:
 *   http://[cm_host_addr]/connector_manager/getFeedLogs/[log_file_name]
 * where [log_file_name] is the name of one log files returned by the list.
 * For instance, 'google-connectors.feed0.log'.  As a convenience, the log
 * name may be simply the log file generation number, '0' in the above example,
 * and it gets automatically expanded.
 *
 * To retrieve a ZIP archive of all the feed log files:
 *   http://[cm_host_addr]/connector_manager/getFeedLogs/*
 * or
 *   http://[cm_host_addr]/connector_manager/getFeedLogs/ALL
 *
 */
public class GetConnectorLogs extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(GetConnectorLogs.class.getName());

  /**
   * Retrieves the log files for a connector instance.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException, FileNotFoundException {
    doGet(req, res);
  }

  /**
   * Retrieves the log files for a connector instance.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, FileNotFoundException {
    Context context = Context.getInstance(this.getServletContext());

    // Are we retrieving Connector logs or Feed logs?
    LogHandler handler;
    String logsTag;
    try {
      if (req.getServletPath().indexOf("Feed") < 0) {
        handler = new ConnectorLogHandler();
        logsTag = ServletUtil.XMLTAG_CONNECTOR_LOGS;
      } else {
        handler = new FeedLogHandler(context);
        logsTag = ServletUtil.XMLTAG_FEED_LOGS;
      }
    } catch (ConnectorManagerException cme) {
      res.setContentType(ServletUtil.MIMETYPE_XML);
      PrintWriter out = res.getWriter();
      ServletUtil.writeResponse(out, new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_HTTP_SERVLET, cme.getMessage(), null));
      out.close();
      return;
    }

    // Fetch the name of the log file to return.  If none is specified,
    // return a list of the available log files.  getPathInfo() returns
    // items with a leading '/', so we want to pull off only the basename.
    String logName = baseName(req.getPathInfo());

    // If no log file is specified, return a list available logs.
    if ((logName == null) || (logName.length() == 0)) {
      res.setContentType(ServletUtil.MIMETYPE_XML);
      PrintWriter out = res.getWriter();
      showLogNames(handler, logsTag, out);
      out.close();
    }

    // If the user asks for all logs, return a ZIP archive.
    else if ("ALL".equalsIgnoreCase(logName) || "*".equals(logName)) {
      res.sendRedirect(res.encodeRedirectURL(handler.getArchiveName()));
      return;
    } else if (logName.equalsIgnoreCase(handler.getArchiveName())) {
      res.setContentType(ServletUtil.MIMETYPE_ZIP);
      ServletOutputStream out = res.getOutputStream();
      fetchAllLogs(handler, out);
      out.close();
    }

    // If the user asks for a specific log, return only that one.
    else {
      // The user can ask for a log file by its generation (%g) number.
      // If they do that, fileHandlerLogFile() will expand it to the
      // full name of the file.  We then force a redirect to the
      // actual logfile name.  This is so wget or curl can assign
      // the correct name to the file.
      File logFile = handler.getLogFile(logName);
      if (!logName.equals(logFile.getName())) {
        res.sendRedirect(res.encodeRedirectURL(logFile.getName()));
        return;
      }
      if (handler.isXMLFormat)
        res.setContentType(ServletUtil.MIMETYPE_XML);
      else
        res.setContentType(ServletUtil.MIMETYPE_TEXT_PLAIN);
      ServletOutputStream out = res.getOutputStream();
      fetchLog(logFile, out);
      out.close();
    }
  }

  /**
   * Send the requested log file.
   *
   * @param logFile log File to be retrieved.
   * @param out OutputStream to which to write the log file.
   * @throws FileNotFoundException, IOException
   */
  public static void fetchLog(File logFile, OutputStream out)
      throws FileNotFoundException, IOException {
    FileInputStream in = new FileInputStream(logFile);
    byte[] buf = new byte[16384];
    int byteCount;
    while ((byteCount = in.read(buf)) > 0) {
      out.write(buf, 0, byteCount);
    }
    in.close();
  }

  /**
   * Send a ZIP image containing all the log files.
   *
   * @param handler LogHandler access to either Connector logs or Feed logs.
   * @param out OutputStream to which to write the archived log files.
   * @throws FileNotFoundException, IOException, ZipException
   */
  public static void fetchAllLogs(LogHandler handler, OutputStream out)
     throws FileNotFoundException, IOException, ZipException {
    File[] logs = handler.listLogs();
    if (logs != null) {
      ZipOutputStream zout = new ZipOutputStream(out);
      for (int i = 0; i < logs.length; i++) {
        ZipEntry zentry = new ZipEntry(logs[i].getName());
        zentry.setSize(logs[i].length());
        zentry.setTime(logs[i].lastModified());
        zout.putNextEntry(zentry);
        fetchLog(logs[i], zout);
        zout.closeEntry();
      }
      zout.finish();
    }
  }

  /**
   * Send the list of the available log files.
   *
   * @param handler LogHandler access to either Connector logs or Feed logs.
   * @param tag xml tag to put around list of logs.
   * @param out PrintWriter where the response is written
   * @throws IOException, FileNotFoundException
   */
  public static void showLogNames(LogHandler handler, String tag,
      PrintWriter out) throws IOException, FileNotFoundException {
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeMessageCode(out, new ConnectorMessageCode());
    ServletUtil.writeXMLTag(out, 1, tag, false);

    File[] logs = handler.listLogs();
    if (logs != null) {
      for (int i = 0; i < logs.length; i++) {
        ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_LOG, false);
        ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_NAME,
            logs[i].getName());
        ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_SIZE,
            String.valueOf(logs[i].length()));
        ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_LAST_MODIFIED,
            String.valueOf(new Date(logs[i].lastModified())));
        ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_LOG, true);
      }
    }

    ServletUtil.writeXMLTag(out, 1, tag, true);
    ServletUtil.writeRootTag(out, true);
  }

  /**
   * Return the base filename part of the pattern or log name.
   * For instance "/x/y/z"  returns "z", "/x/y/" returns "".
   *
   * @param name unix-style pathname or pattern.
   * @return the base filename (may be null or empty)
   */
  private static String baseName(String name) {
    return (name == null) ? null : name.substring(name.lastIndexOf('/') + 1);
  }

  /**
   * Return the directory name part of the pattern or log name.
   * For instance "/x/y/z"  returns "/x/y/", "/x/y/" returns "/x/y/".
   *
   * @param name unix-style pathname or pattern.
   * @return the base filename (may be null or empty)
   */
  private static String directoryName(String name) {
    return (name == null) ? null : name.substring(0, name.lastIndexOf('/') + 1);
  }


  /**
   * This is a FilenameFilter for the FileHandler log files.
   */
  private static class LogFilenameFilter implements FilenameFilter {
    private Pattern regexPattern = null;

    /**
     * Convert a java.util.logging.FileHandler.pattern into a
     * java.util.regex.Pattern that could be used in a FilenameFilter.
     * The regex pattern only represents the filename part at the end
     * of the FileHandler pattern path (the stuff after the last '/').
     *
     * @param fhPattern a java.util.logging.FileHandler.pattern
     */
    public LogFilenameFilter(String fhPattern) {
      // Only take the filename part of the path.
      fhPattern = baseName(fhPattern);
      int len = fhPattern.length();
      StringBuffer buf = new StringBuffer(2 * len);
      for (int i = 0; i < len; i++) {
        char c = fhPattern.charAt(i);
        // % is the lead-in quote character for FileHandler patterns.
        if (c == '%') {
          try { c = fhPattern.charAt(++i); }
          catch (IndexOutOfBoundsException ignored) { } // % at end?
          if (c == '%')
            buf.append(c);
          else if ((c == 'g') || (c == 'u'))
            buf.append("[0-9]+");
          else
            buf.append('%').append(c);
        }
        // Quote any regex special chars that might appear in the filename.
        else if ("[](){}-^$*+?.,\\".indexOf(c) >= 0)
          buf.append('\\').append(c);
        else
          buf.append(c);
      }
      // FileHandler patterns can optionally implicitly add %g and %u,
      // each preceded by dots.  Be generous and look for those too.
      // Technically not stringent, but good enough for our use.
      buf.append("[0-9\\.]*");

      // Compile the pattern for use by the matcher.
      regexPattern = Pattern.compile(buf.toString());
    }

    /**
     * Does this filename match the regexPattern?
     *
     * @param dir the directory containing the file.
     * @param fileName a file in the directory.
     * @returns true if the fileName matches the pattern, false otherwise.
     */
    public boolean accept(File dir, String fileName) {
      return regexPattern.matcher(fileName).matches();
    }
  }


  /**
   * Abstract access to either the Connector Logs or the Feed Logs.
   * The Connector logs have their FileHandler configuration specified
   * in logging.properties; whereas the Feed logs have their FileHandler
   * configuration specified in the Spring applicationContext.xml file.
   */
  private static class LogHandler {
    public boolean isXMLFormat = true;
    public String pattern = "%h/java%u.log";
    private File logDirectory;

    /**
     * Return an array of all the existing log Files for this LogHandler.
     */
    public File[] listLogs() throws IOException, FileNotFoundException {
      return getLogDirectory().listFiles(new LogFilenameFilter(pattern));
    }

    /**
     * Return a File object representing the directory containing the logs.
     * The directory is determined by the path part of the FileHandler pattern.
     *
     * BUGS: Doesn't handle %u or %g in the directoryName part of the
     * FileHandler pattern.  I don't think java.util.logging.FileHandler
     * tolerates it either.
     *
     * @returns File object representing the log directory.
     * @throws IOException, FileNotFoundException
     */
    public File getLogDirectory() throws IOException, FileNotFoundException {
      if (logDirectory != null)
        return logDirectory;

      String dirName = directoryName(pattern);
      if (dirName == null || dirName.length() == 0) {
        // No path part to pattern? Look for log files in current directory.
        dirName = System.getProperty("user.dir");
      } else {
        int len = dirName.length();
        StringBuffer buf = new StringBuffer(2 * len);
        for (int i = 0; i < len; i++) {
          char c = dirName.charAt(i);
          // % is the lead-in quote character for FileHandler patterns.
          // Replace the %h, %t, and %% substitution patterns with their
          // resolved values.
          if (c == '%') {
            try { c = dirName.charAt(++i); }
            catch (IndexOutOfBoundsException ignored) {} // % at end?
            if (c == '%')
              buf.append(c);
            else if (c == 'h')
              buf.append(System.getProperty("user.home"));
            else if (c == 't')
              buf.append(System.getProperty("java.io.tmpdir"));
            else
              buf.append('%').append(c);
          }
          else
            buf.append(c);
        }
        dirName = buf.toString();
      }
      logDirectory = new File(dirName);
      return logDirectory;
    }

    /**
     * Return a File object representing the named  Log File.
     *
     * This is designed to only construct file paths into the
     * logging directory used by the Logging FileHandler.
     * This prevents arbitrary files from being fetched from
     * elsewhere in the file system.
     *
     * @param logName
     * @returns File object representing the named  Log File.
     * @throws IOException, FileNotFoundException
     */
    public File getLogFile(String logName)
        throws IOException, FileNotFoundException {
      // The caller may specify simply the log generation number
      // (the value of %g for the specific file).  If so, build
      // a logName from that.
      if (Pattern.matches("[0-9]+", logName)) {
        // Pull off the base filename pattern.
        String basePattern = baseName(pattern);

        // Assume no duplicate file collisions with shorthand request.
        basePattern = basePattern.replaceAll("%u", "0");

        // Replace the generation placeholder with the supplied number.
        if (basePattern.indexOf("%g") >= 0)
          logName = basePattern.replaceAll("%g", logName);
        else
          logName = basePattern + '.' + logName; // implicit %g rule.

      } else {
        // The logName was not a generation number.
        // Assume it is the actual log file name.
        // Don't allow full or relative pathnames.
        logName = new File(logName).getName();
      }
      return new File(getLogDirectory(), logName);
    }

    /**
     * Return the filename to give ZIP archives of this handler's log files.
     */
    public String getArchiveName() {
      // Only take the filename part of the path.
      String fhPattern = baseName(pattern);
      int len = fhPattern.length();
      StringBuffer buf = new StringBuffer(2 * len);
      int i;
      for (i = 0; i < len; i++) {
        char c = fhPattern.charAt(i);
        // % is the lead-in quote character for FileHandler patterns.
        if (c == '%') {
          try { c = fhPattern.charAt(++i); }
          catch (IndexOutOfBoundsException ignored) { } // % at end?
          if (c == '%')
            buf.append(c);
          else if ("guth".indexOf(c) < 0) // drop %g, %u, %t, %h
            buf.append('%').append(c);
        }
        else
          buf.append(c);
      }
      // Pluralize .log -> -logs as a convenience.
      if ((i = buf.lastIndexOf(".log")) >= 0)
        buf.replace(i, i + 4, "-logs");

      // Add ZIP filename extension.
      buf.append(".zip");
      return buf.toString();
    }

  }

  // Get Connector Log FileHandler configuration from logging.properies.
  private static class ConnectorLogHandler extends LogHandler {
    public ConnectorLogHandler() throws ConnectorManagerException {
      LogManager logMgr = LogManager.getLogManager();
      String prop = logMgr.getProperty("java.util.logging.FileHandler.pattern");
      if (prop != null && prop.length() > 0)
        super.pattern = prop;
      else {
        throw new ConnectorManagerException(
            "Unable to retrieve Connector Logging configuration.  Please check"
            + " the FileHandler configuration in logging.properties.");
      }
      prop = logMgr.getProperty("java.util.logging.FileHandler.formatter");
      super.isXMLFormat = (prop == null || (prop.indexOf("XML") >= 0));
    }
  }

  // Get Feed Log FileHandler configuration from applicationContext.xml.
  private static class FeedLogHandler extends LogHandler {
    public FeedLogHandler(Context context) throws ConnectorManagerException {
      try {
        FeedFileHandler ffh = (FeedFileHandler) context.getApplicationContext()
            .getBean("FeedHandler", FeedFileHandler.class);
        String ffhPattern = ffh.getPattern();
        if (ffhPattern != null && ffhPattern.length() > 0)
          super.pattern = ffhPattern;

        Formatter formatter = ffh.getFormatter();
        isXMLFormat = (formatter == null ||
                       (formatter.getClass().getName().indexOf("XML") >= 0));
      } catch (BeansException be) {
        throw new ConnectorManagerException(
            "Unable to retrieve Feed Logging configuration: " + be.toString());
      }
    }
  }
}
