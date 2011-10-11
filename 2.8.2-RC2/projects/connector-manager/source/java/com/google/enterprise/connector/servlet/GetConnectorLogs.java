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

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.pusher.FeedFileHandler;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;

/**
 * <p>Admin servlet to retrieve the log files from the Connector Manager.
 * This servlet allows the user to list available log files, fetch individual
 * log files, and fetch a ZIP archive of all the log files.  At this time
 * the user may retrieve connector log files, feed log files, and teed feed
 * files.  Access to this servlet is restricted to either localhost or
 * gsa.feed.host, based upon the HTTP RemoteAddress.</p>
 *
 * <p><b>Usage:</b>
 * <br>To list the available connector log files:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConnectorLogs</pre>
 * </p>
 * <p>To view an individual connector log file:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConnectorLogs/[log_file_name]</pre>
 * <br>where [log_file_name] is the name of one log files returned by
 * the list.  For instance, 'google-connectors.0.log'.  As a convenience,
 * the log name may be simply the log file generation number, '0' in the above
 * example, and it gets automatically expanded.</p>
 * <p>If connector logging is configured to use the
 * {@code org.apache.juli.FileHandler}, the date, or trailing fragment of the
 * date, may be supplied as a convenience.  For instance, given the log file
 * 'google-connectors.2010-02-14.log', the user may request simply '2010-02-14'
 * or '14'.</p>
 *
 * <p>To retrieve a ZIP archive of all the connector log files:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConnectorLogs/*</pre>
 * <br>or
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConnectorLogs/ALL</pre>
 * </p>
 *
 * <p><br>To list the available feed log files:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs</pre></p>
 *
 * <p>To view an individual feed log file:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs/[log_file_name]</pre>
 * <br>where [log_file_name] is the name of one log files returned by the list.
 * For instance, 'google-connectors.feed0.log'.  As a convenience, the log
 * name may be simply the log file generation number, '0' in the above example,
 * and it gets automatically expanded.</p>
 *
 * <p>To retrieve a ZIP archive of all the feed log files:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs/*</pre>
 * <br>or
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs/ALL</pre></p>
 *
 *
 * <p><br>To list the name and size of the teed feed file:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getTeedFeedFile</pre></p>
 *
 * <p>To view the teed feed file:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getTeedFeedFile/[teed_feed_name]</pre>
 * <br>or
 * <br><pre>  http://[cm_host_addr]/connector-manager/getTeedFeedFile/0</pre>
 * <br>where [teed_feed_name] is the base filename of the teed feed file.
 * <br>WARNING: The teed feed file can be HUGE.  It is suggested you either
 * request a manageable byte range (see below) or fetch the ZIP archive file
 * (which may still be HUGE).</p>
 *
 * <p>To retrieve a ZIP archive of the teed feed file:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getTeedFeedFile/*</pre>
 * <br>or
 * <br><pre>  http://[cm_host_addr]/connector-manager/getTeedFeedFile/ALL</pre>
 * <br>or
 * <br><pre>  http://[cm_host_addr]/connector-manager/getTeedFeedFile/[teed_feed_name].zip</pre>
 * <br>where [teed_feed_name] is the filename of the teed feed file.</p>
 *
 *
 * <p><br><b>Byte Range Support:</b>
 * This servet supports a subset of the RFC 2616 byte range specification
 * to retrieve portions of the log files.  Since the connector logs are
 * 50MB each and the teedFeedFile can be gigabytes, requesting a portion
 * of the log may be prudent.  This servlet supports byte range specifier
 * in either the HTTP Range: header or in the Query fragment of the request.
 * </p>
 * <p>For instance:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs/0?bytes=0-1000</pre>
 * <br>returns the first 1001 bytes of the current feed log.</p>
 * <br>
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs/0?bytes=-1000</pre>
 * <br>returns the last 1000 bytes (the tail) of the current feed log.
 * <br>
 * <br><pre>  http://[cm_host_addr]/connector-manager/getFeedLogs/0?bytes=1000-</pre>
 * <br>returns everything after the first 1000 bytes of the current feed log.
 * </p>
 * <p>Multipart byte ranges are NOT supported (ie bytes=0-100,1000-2000).
 * Byte range requests for log listing pages and ZIP archive files are
 * ignored.</p>
 *
 *
 * <p><br><b>Redirects and curl:</b>
 * When using shorthand file specifications, like generation numbers,
 * 'ALL', or '*', this servlet returns a redirect to the actual filename.
 * This allows the browser, wget, or curl to pull the true filename off
 * the redirected URL so that it can name the file when storing locally.
 * If using curl to retrieve the files, please to use 'curl -L' to tell
 * curl to follow the redirect.  Unfortunately, when using 'curl -O' to
 * save the file locally, curl uses the pre-redirected name, rather than
 * the post-redirected name when naming the local file.  This forces you
 * to use 'curl -L -o output_filename' anyway, so you might as well
 * specify the full filename in the URL to begin with.</p>
 *
 * <p>Wget handles redirects appropriately without intervention, and names
 * the saved file as expected.</p>
 *
 *
 * <p><br><b>Compressed Content-Encodings:</b>
 * When serving up individual logs, this servlet supports compressing
 * the output stream using the gzip or deflate Content-Encodings, if
 * the client accepts them.  When using curl, you can enable compressed
 * Content-Encoding by specifying 'curl --compressed ...'.</p>
 */
public class GetConnectorLogs extends HttpServlet {
  /**
   * Retrieves the log files for a connector instance.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
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
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, FileNotFoundException {
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.RED, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    NDC.push("Support");
    try {
      // Are we retrieving Connector logs, Feed logs, or TeedFeed file?
      LogHandler handler;
      String logsTag;
      try {
        Context context = Context.getInstance();
        if (req.getServletPath().indexOf("Teed") > 0) {
          handler = new TeedFeedHandler(context);
          logsTag = ServletUtil.XMLTAG_TEED_FEED;
        } else if (req.getServletPath().indexOf("Feed") > 0) {
          handler = new FeedLogHandler(context);
          logsTag = ServletUtil.XMLTAG_FEED_LOGS;
        } else {
          handler = newConnectorLogHandler();
          logsTag = ServletUtil.XMLTAG_CONNECTOR_LOGS;
        }
      } catch (ConnectorManagerException cme) {
        res.setContentType(ServletUtil.MIMETYPE_XML);
        PrintWriter out = res.getWriter();
        try {
          ServletUtil.writeResponse(out, new ConnectorMessageCode(
              ConnectorMessageCode.EXCEPTION_HTTP_SERVLET, cme.getMessage(),
              null));
        } finally {
          out.close();
        }
        return;
      }

      // Fetch the name of the log file to return.  If none is specified,
      // return a list of the available log files.  getPathInfo() returns
      // items with a leading '/', so we want to pull off only the basename.
      // WARNING: For security reasons, the PathInfo parameter must never
      // be passed directly to a File() or shell command. We are pulling
      // of the base filename part of the PathInfo and restricting file
      // retrievals to the log file directory as configured for the
      // Connector Manager.
      String logName = baseName(req.getPathInfo());

      // If no log file is specified, return a list available logs.
      if ((logName == null) || (logName.length() == 0)) {
        res.setContentType(ServletUtil.MIMETYPE_XML);
        PrintWriter out = res.getWriter();
        try {
          showLogNames(handler, logsTag, out);
        } finally {
          out.close();
        }
      }

      // If the user asks for all logs, return a ZIP archive.
      else if ("ALL".equalsIgnoreCase(logName) || "*".equals(logName)) {
        res.sendRedirect(res.encodeRedirectURL(handler.getArchiveName()));
        return;
      } else if (logName.equalsIgnoreCase(handler.getArchiveName())) {
        res.setContentType(ServletUtil.MIMETYPE_ZIP);
        ServletOutputStream out = res.getOutputStream();
        try {
          fetchAllLogs(handler, out);
        } finally {
          out.close();
        }
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
          String url = res.encodeRedirectURL(logFile.getName());
          String query = req.getQueryString();
          if ((query != null) && (query.length() > 0)) {
            url += '?' + query;
          }
          res.sendRedirect(url);
          return;
        }

        // Did the user ask for a byte range?
        ByteRange range;
        try {
          if ((range = ByteRange.parseByteRange(req)) != null) {
            res.addHeader("Content-Range",
                          range.contentRange(logFile.length()));
          }
        } catch (IllegalArgumentException iae) {
          res.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE,
                        iae.toString());
          return;
        }

        // Specify either text/plain or xml content type, based on log format.
        if (handler.isXmlFormat()) {
          res.setContentType(ServletUtil.MIMETYPE_XML);
        } else {
          res.setContentType(ServletUtil.MIMETYPE_TEXT_PLAIN);
        }
        OutputStream out = getCompressedOutputStream(req, res);
        try {
          fetchLog(logFile, range, out);
        } finally {
          out.close();
        }
      }
    } finally {
      NDC.clear();
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
   * Try to encode the response output stream with a compression mechanism
   * the client supports.  Returns the standard ServletOutputStream if the
   * client does not support gzip or deflate encodings.
   *
   * Known Limitations: Does not take into account encoding weights,
   * especially 'q=0'.
   *
   * @param req an HttpServletRequest
   * @param res an HttpServletResponse
   * @returns outputStream, possibly of a compressed encoding.
   */
  private static OutputStream getCompressedOutputStream(HttpServletRequest req,
      HttpServletResponse res) throws IOException {
    String encodings = req.getHeader("Accept-Encoding");
    if ((encodings != null) && (encodings.length() > 0)) {
      encodings = encodings.toLowerCase();
      if (encodings.indexOf("gzip") >= 0) {
        res.setHeader("Content-Encoding", "gzip");
        return new GZIPOutputStream(res.getOutputStream());
      } else if (encodings.indexOf("deflate") >= 0) {
        res.setHeader("Content-Encoding", "deflate");
        return new ZipOutputStream(res.getOutputStream());
      }
    }
    return res.getOutputStream();
  }

  /**
   * Send the requested log file.
   *
   * @param logFile log File to be retrieved.
   * @param range ByteRange depicting a portion of file, may be null.
   * @param out OutputStream to which to write the log file.
   * @throws FileNotFoundException, IOException
   */
  private static void fetchLog(File logFile, ByteRange range, OutputStream out)
      throws FileNotFoundException, IOException {
    long startPos = 0;
    long length = logFile.length();
    if (range != null) {
      startPos = range.actualStartPosition(length);
      length = range.actualLength(length);
    }

    byte[] buf = new byte[1024 * 1024];
    RandomAccessFile in = new RandomAccessFile(logFile, "r");
    if (startPos > 0) {
      in.seek(startPos);
    }
    while (length > 0) {
      int byteCount =
          in.read(buf, 0, (length > buf.length) ? buf.length : (int)length);
      if (byteCount > 0) {
        out.write(buf, 0, byteCount);
        length -= byteCount;
      } else
        break;
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
  private static void fetchAllLogs(LogHandler handler, OutputStream out)
     throws FileNotFoundException, IOException, ZipException {
    File[] logs = handler.listLogs();
    if (logs != null) {
      ZipOutputStream zout = new ZipOutputStream(out);
      for (int i = 0; i < logs.length; i++) {
        ZipEntry zentry = new ZipEntry(logs[i].getName());
        zentry.setSize(logs[i].length());
        zentry.setTime(logs[i].lastModified());
        zout.putNextEntry(zentry);
        fetchLog(logs[i], null, zout);
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
   */
  private static void showLogNames(LogHandler handler, String tag,
      PrintWriter out) {
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
    if (name != null) {
      // FileHandler patterns use '/' as separatorChar by default.
      int sep = name.lastIndexOf('/');
      // If no '/', then look for system separatorChar.
      if ((sep == -1) && (File.separatorChar != '/')) {
        sep = name.lastIndexOf(File.separatorChar);
      }
      return name.substring(sep + 1);
    }
    return null;
  }

  /**
   * Return the directory name part of the pattern or log name.
   * For instance "/x/y/z"  returns "/x/y/", "/x/y/" returns "/x/y/".
   *
   * @param name unix-style pathname or pattern.
   * @return the base filename (may be null or empty)
   */
  private static String directoryName(String name) {
    if (name != null) {
      // FileHandler patterns use '/' as separatorChar by default.
      int sep = name.lastIndexOf('/');
      // If no '/', then look for system separatorChar.
      if ((sep == -1) && (File.separatorChar != '/')) {
        sep = name.lastIndexOf(File.separatorChar);
      }
      return name.substring(0, sep + 1);
    }
    return null;
  }

  /**
   * This describes a byte range specification, as per RFC2616.
   */
  private static class ByteRange {
    static final long UNSPECIFIED = -1;
    public long startPosition = UNSPECIFIED;
    public long endPosition = UNSPECIFIED;

    public ByteRange(long startPosition, long endPosition) {
      this.startPosition = startPosition;
      this.endPosition = endPosition;
    }

    /**
     * Extract a byte range request from either the HTTP header or the
     * Query fragment.  The syntax for each is identical:  bytes=start-end
     * Does not support multi-part ranges.
     *
     * @param req  an HttpServletRequest
     * @throws IllegalArgumentException if the range does not look
     * like a valid RFC2616 ranges-specifier, or if a multi-part range
     * was specified.
     */
    public static ByteRange parseByteRange(HttpServletRequest req)
        throws IllegalArgumentException {
      // First look for byte range specification in the request Query fragment.
      String bytes = req.getParameter("bytes");
      if (bytes == null) {
        // Next, look for byte range specification in the HTTP header.
        if ((bytes = req.getHeader("Range")) == null) {
          // no Range header given either.
          return null;
        }
        if (bytes.startsWith("bytes=")) {
          bytes = bytes.substring(6).trim();
        } else {
          throw new IllegalArgumentException(bytes);
        }
      }
      // Now extract the actual start and stop byte values.
      long startPosition = UNSPECIFIED;
      long endPosition = UNSPECIFIED;
      int dash = bytes.indexOf('-');
      if (dash != -1) {
        try {
          if (dash > 0) {
            startPosition = Long.parseLong(bytes.substring(0, dash));
          }
          if (++dash < bytes.length()) {
            endPosition = Long.parseLong(bytes.substring(dash));
          }
        } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException(bytes);
        }
      }
      // One or the other may be unspecified, but not both.
      // If both are specified, start must not exceed end.
      if (((startPosition == UNSPECIFIED) && (endPosition == UNSPECIFIED)) ||
          ((endPosition != UNSPECIFIED) && (startPosition > endPosition))) {
        throw new IllegalArgumentException(bytes);
      }
      return new ByteRange(startPosition, endPosition);
    }

    /**
     * Return the number of bytes to read.
     *
     * @param fileSize total number of bytes in file.
     * @return actual number of bytes in requested range.
     */
    public long actualLength(long fileSize) {
      if (startPosition == UNSPECIFIED) {
        // 'tail' request - ie last n bytes of file.
        return (endPosition < fileSize) ? endPosition : fileSize;
      } else if (startPosition >= fileSize) {
        // start position at or after end-of-file.
        return 0;
      } else if ((endPosition == UNSPECIFIED) || (endPosition >= fileSize)) {
        // from startPosition to end-of-file.
        return fileSize - startPosition;
      } else {
        // ranges are inclusive.
        return endPosition - startPosition + 1;
      }
    }

    /**
     * Return the actual startPosition in the file.
     *
     * @param fileSize total number of bytes in file.
     * @return actual starting location to seek to.
     */
    public long actualStartPosition(long fileSize) {
      if (startPosition == UNSPECIFIED) {
        return (fileSize < endPosition) ? 0 : fileSize - endPosition;
      } else {
        return (startPosition >= fileSize) ? fileSize : startPosition;
      }
    }

    /**
     * Return the Content-Range response header value.
     * @param fileSize total number of bytes in file.
     * @return actual starting location to seek to.
     */
    public String contentRange(long fileSize) {
      long start = actualStartPosition(fileSize);
      long end = start + actualLength(fileSize) - 1;
      return "bytes " + start + '-' + end + '/' + fileSize;
    }
  }

  // Get Connector Log FileHandler configuration from logging.properies.
  private static LogHandler newConnectorLogHandler()
      throws ConnectorManagerException {
    LogManager logMgr = LogManager.getLogManager();
    String[] handlers = logMgr.getProperty("handlers").split("[, ]+");
    String handler = null;
    for (int i = 0; i < handlers.length; i++) {
      if (handlers[i].indexOf("FileHandler") >= 0) {
        handler = handlers[i];
        break;
      }
    }
    if (handler == null) {
      throw new ConnectorManagerException(
          "Unable to retrieve Connector Logging configuration.  There"
          + " is no FileHandler configuration in logging.properties.");
    }
    if (handler.indexOf("org.apache.juli.FileHandler") >= 0) {
      return new JuliLogHandler(logMgr, handler);
    } else {
      return new JavaUtilLogHandler(logMgr, handler);
    }
  }

  /**
   * Abstract access to either the Connector Logs or the Feed Logs.
   */
  private static interface LogHandler {
    /**
     * Return an array of all the existing log Files for this LogHandler.
     */
    public File[] listLogs();

    /**
     * Return a File object representing the directory containing the logs.
     *
     * @returns File object representing the log directory.
     */
    public File getLogDirectory();

    /**
     * Return a File object representing the named Log File.
     *
     * This is designed to only construct file paths into the
     * logging directory used by the Logging FileHandler.
     * This prevents arbitrary files from being fetched from
     * elsewhere in the file system.
     *
     * @param logName
     * @returns File object representing the named  Log File.
     */
    public File getLogFile(String logName);

    /**
     * Return the filename to give ZIP archives of this handler's log files.
     */
    public String getArchiveName();

    /**
     * Return {@code true} if this handler's log files are in XML format,
     * {@code false} otherwise.
     */
    public boolean isXmlFormat();
  }

  /**
   * Abstract access to either the Connector Logs or the Feed Logs.
   * The Connector logs have their FileHandler configuration specified
   * in logging.properties; whereas the Feed logs have their FileHandler
   * configuration specified in the Spring applicationContext.xml file.
   */
  private static class JavaUtilLogHandler implements LogHandler {
    boolean isXMLFormat = true;
    String pattern = "%h/java%u.log";
    private File logDirectory;

    public JavaUtilLogHandler() {}

    public JavaUtilLogHandler(LogManager logManager, String handler)
        throws ConnectorManagerException {
      pattern = logManager.getProperty(handler + ".pattern");
      if ((pattern == null) || (pattern.length() == 0)) {
        throw new ConnectorManagerException(
            "Unable to retrieve Connector Logging configuration.  Please"
            + "check the FileHandler configuration in logging.properties.");
      }
      String formatter = logManager.getProperty(handler + ".formatter");
      isXMLFormat =
          (formatter == null || (formatter.toUpperCase().indexOf("XML") >= 0));
    }

    //@Override
    public File[] listLogs() {
      return getLogDirectory().listFiles(new JavaUtilLogFilenameFilter(pattern));
    }

    //@Override
    public File getLogDirectory() {
      if (logDirectory != null) {
        return logDirectory;
      }

      // Known Limitations: Doesn't handle %u or %g in the directoryName part
      // of the FileHandler pattern.
      String dirName = directoryName(pattern);
      if (dirName == null || dirName.length() == 0) {
        // No path part to pattern? Look for log files in current directory.
        dirName = System.getProperty("user.dir");
      } else {
        int len = dirName.length();
        StringBuilder buf = new StringBuilder(2 * len);
        for (int i = 0; i < len; i++) {
          char c = dirName.charAt(i);
          // % is the lead-in quote character for FileHandler patterns.
          // Replace the %h, %t, and %% substitution patterns with their
          // resolved values.
          if (c == '%') {
            try {
              c = dirName.charAt(++i);
            } catch (IndexOutOfBoundsException ignored) {
              // % at end? Just preserve it.
            }
            if (c == '%') {
              buf.append(c);
            } else if (c == 'h') {
              buf.append(System.getProperty("user.home"));
            } else if (c == 't') {
              buf.append(System.getProperty("java.io.tmpdir"));
            } else {
              buf.append('%').append(c);
            }
          } else {
            buf.append(c);
          }
        }
        dirName = buf.toString();
      }
      logDirectory = new File(dirName);
      return logDirectory;
    }

    //@Override
    public File getLogFile(String logName) {
      // The caller may specify simply the log generation number
      // (the value of %g for the specific file).  If so, build
      // a logName from that.
      if (Pattern.matches("[0-9]+", logName)) {
        // Pull off the base filename pattern.
        String basePattern = baseName(pattern);

        // Assume no duplicate file collisions with shorthand request.
        basePattern = basePattern.replaceAll("%u", "0");

        // Replace the generation placeholder with the supplied number.
        if (basePattern.indexOf("%g") >= 0) {
          logName = basePattern.replaceAll("%g", logName);
        } else {
          logName = basePattern + '.' + logName; // implicit %g rule.
        }
      } else {
        // The logName was not a generation number.
        // Assume it is the actual log file name.
        // Don't allow full or relative pathnames.
        logName = new File(logName).getName();
      }

      return new File(getLogDirectory(), logName);
    }

    //@Override
    public String getArchiveName() {
      // Only take the filename part of the path.
      String fhPattern = baseName(pattern);
      int len = fhPattern.length();
      StringBuilder buf = new StringBuilder(2 * len);
      int i;
      for (i = 0; i < len; i++) {
        char c = fhPattern.charAt(i);
        // % is the lead-in quote character for FileHandler patterns.
        if (c == '%') {
          try {
            c = fhPattern.charAt(++i);
          } catch (IndexOutOfBoundsException ignored) {
            // % at end? Just preserve it.
          }
          if (c == '%') {
            buf.append(c);
          } else if ("guth".indexOf(c) < 0) {
            // drop %g, %u, %t, %h
            buf.append('%').append(c);
          }
        } else {
          buf.append(c);
        }
      }

      // Stripping out parts may have left us with a name like ..log.
      while ((i = buf.indexOf("..")) >= 0) {
        buf.deleteCharAt(i);
      }
      // Pluralize .log -> -logs as a convenience.
      if ((i = buf.lastIndexOf(".log")) >= 0) {
        buf.replace(i, i + 4, "-logs");
      }
      // Add ZIP filename extension.
      buf.append(".zip");
      return buf.toString();
    }

    //@Override
    public boolean isXmlFormat() {
      return this.isXMLFormat;
    }

    /**
     * A FilenameFilter for java.util.logging.FileHandler log files.
     */
    private static class JavaUtilLogFilenameFilter implements FilenameFilter {
      private Pattern regexPattern = null;

      /**
       * Convert a java.util.logging.FileHandler.pattern into a
       * java.util.regex.Pattern that could be used in a FilenameFilter.
       * The regex pattern only represents the filename part at the end
       * of the FileHandler pattern path (the stuff after the last '/').
       *
       * @param fhPattern a java.util.logging.FileHandler.pattern
       */
      public JavaUtilLogFilenameFilter(String fhPattern) {
        // Only take the filename part of the path.
        fhPattern = baseName(fhPattern);
        int len = fhPattern.length();
        StringBuilder buf = new StringBuilder(2 * len);
        for (int i = 0; i < len; i++) {
          char c = fhPattern.charAt(i);
          // % is the lead-in quote character for FileHandler patterns.
          if (c == '%') {
            try {
              c = fhPattern.charAt(++i);
            } catch (IndexOutOfBoundsException ignored) {
              // % at end? Just preserve it.
            }
            if (c == '%') {
              buf.append(c);
            } else if ((c == 'g') || (c == 'u')) {
              buf.append("[0-9]+");
            } else {
              buf.append('%').append(c);
            }
          } else if ("[](){}-^$*+?.,\\".indexOf(c) >= 0) {
            // Quote any regex special chars that might appear in the filename.
            buf.append('\\').append(c);
          } else {
            buf.append(c);
          }
        }
        // FileHandler patterns can optionally implicitly add %g and %u,
        // each preceded by dots.  Be generous and look for those too.
        // Technically not stringent, but good enough for our use.
        buf.append("[0-9\\.]*");

        // Compile the pattern for use by the matcher.
        regexPattern = Pattern.compile(buf.toString());
      }

      /**
       * Tests if the specified file matches the regexPattern.
       *
       * @param dir the directory containing the file.
       * @param fileName a file in the directory.
       * @returns true if the fileName matches the pattern, false otherwise.
       */
      public boolean accept(File dir, String fileName) {
        return regexPattern.matcher(fileName).matches();
      }
    }
  }

  /**
   * A LogHandler for logs created by the org.apache.juli.LogManager.
   */
  private static class JuliLogHandler implements LogHandler {
    File logDirectory;
    String logPrefix;
    String logSuffix;
    boolean isXMLFormat;

    public JuliLogHandler(LogManager logManager, String handler) {
      // Looks like we are using the JULI FileHandler.
      String dir = logManager.getProperty(handler + ".directory");
      if (dir == null) {
        dir = "logs";
      }
      logDirectory = new File(dir);

      logPrefix = logManager.getProperty(handler + ".prefix");
      if (logPrefix == null) {
        logPrefix = "juli.";
      }

      logSuffix = logManager.getProperty(handler + ".suffix");
      if (logSuffix == null) {
          logSuffix = ".log";
      }

      String formatter = logManager.getProperty(handler + ".formatter");
      isXMLFormat =
          (formatter == null || (formatter.toUpperCase().indexOf("XML") >= 0));
    }

    //@Override
    public File[] listLogs() {
      return getLogDirectory().listFiles(new JuliLogFilenameFilter());
    }

    //@Override
    public File getLogDirectory() {
      return logDirectory;
    }

    //@Override
    public File getLogFile(String logName) {
      // The caller may specify simply the log date id.
      // If so, build a logName from that.
      if (Pattern.matches("[0-9-]+", logName)) {
        if (logName.length() < 10) {
          // This looks like a fragment of a date.
          File[] files =
              getLogDirectory().listFiles(new JuliLogFilenameFilter(logName));
          if (files.length > 0) {
            logName = baseName(files[0].getName());
          }
        } else {
          logName = logPrefix + logName + logSuffix;
        }
      } else {
        // The logName was not a date.  Assume it is the actual
        // log file name.  Don't allow full or relative pathnames.
        logName = new File(logName).getName();
      }
      return new File(getLogDirectory(), logName);
    }

    //@Override
    public String getArchiveName() {
      StringBuilder buf = new StringBuilder(logPrefix);

      // Strip trailing '.' from prefix, if present.
      if (logPrefix.endsWith(".")) {
        buf.setLength(buf.length() - 1);
      }

      // Pluralize .log -> -logs as a convenience.
      if (logSuffix.equalsIgnoreCase(".log")) {
        buf.append("-logs");
      } else {
        buf.append(logSuffix);
      }

      // Add ZIP filename extension.
      buf.append(".zip");
      return buf.toString();
    }

    //@Override
    public boolean isXmlFormat() {
      return this.isXMLFormat;
    }

    /**
     * A FilenameFilter for org.apache.juli.FileHandler log files.
     */
    private class JuliLogFilenameFilter implements FilenameFilter {
      private Pattern regexPattern = null;

      public JuliLogFilenameFilter() {
        this("");
      }

      // Constructor that allows a fragment of the date.
      public JuliLogFilenameFilter(String fragment) {
        StringBuilder buf = new StringBuilder();
        appendEscaped(buf, logPrefix);
        buf.append("[0-9-]+");
        buf.append(fragment);
        appendEscaped(buf, logSuffix);
        regexPattern = Pattern.compile(buf.toString());
      }

      /**
       * Append {@code src} to {@code buf}, escaping special characters.
       */
      private void appendEscaped(StringBuilder buf, String src) {
        for (char c : src.toCharArray()) {
          // Quote any regex special chars that might appear in the source.
          if ("[](){}-^$*+?.,\\".indexOf(c) >= 0) {
            buf.append('\\');
          }
          buf.append(c);
        }
      }

      /**
       * Tests if the specified file matches the regexPattern.
       *
       * @param dir the directory containing the file.
       * @param fileName a file in the directory.
       * @returns true if the fileName matches the pattern, false otherwise.
       */
      public boolean accept(File dir, String fileName) {
        return regexPattern.matcher(fileName).matches();
      }
    }
  }

  /**
   * A LogHandler for feedlogs, as configured in applicationContext.properties.
   */
  private static class FeedLogHandler extends JavaUtilLogHandler {
    public FeedLogHandler(Context context) throws ConnectorManagerException {
      try {
        FeedFileHandler ffh = (FeedFileHandler) context.getApplicationContext()
            .getBean("FeedHandler", FeedFileHandler.class);
        String ffhPattern = ffh.getPattern();
        if (ffhPattern != null && ffhPattern.length() > 0) {
          super.pattern = ffhPattern;
        }
        Formatter formatter = ffh.getFormatter();
        isXMLFormat = (formatter == null ||
            (formatter.getClass().getName().toUpperCase().indexOf("XML") >= 0));
      } catch (BeansException be) {
        throw new ConnectorManagerException(
            "Unable to retrieve Feed Logging configuration: " + be.toString());
      }
    }
  }

  /**
   * A LogHandler for Teed Feed File, as configured in
   * applicationContext.properties.
   * At this time, there is only one teedFeedFile is specified, so this is
   * a pretty trivial LogHandler implementation.
   */
  private static class TeedFeedHandler implements LogHandler {
    String teedFeedFile;

    public TeedFeedHandler(Context context) throws ConnectorManagerException {
      teedFeedFile = context.getTeedFeedFile();
      if ((teedFeedFile == null) || (teedFeedFile.length() == 0)) {
        throw new ConnectorManagerException(
            "Unable to retrieve Teed Feed File configuration. The teedFeedFile"
            + " property is not defined in applicationContext.properties.");
      }
    }

    //@Override
    public File[] listLogs() {
      return new File[] { new File(teedFeedFile) };
    }

    //@Override
    public File getLogDirectory() {
      File parent = (new File(teedFeedFile)).getParentFile();
      if (parent != null) {
        return parent;
      } else {
        throw new IllegalStateException(
            "The teedFeedFile does not specify a parent directory.");
      }
    }

    //@Override
    public File getLogFile(String logName) {
      return new File(teedFeedFile);
    }

    //@Override
    public String getArchiveName() {
      return new File(teedFeedFile).getName() + ".zip";
    }

    //@Override
    public boolean isXmlFormat() {
      // Even though the teedFeedFile is XML format, it is malformed -
      // especially when byte-served.
      return false;
    }
  }
}
