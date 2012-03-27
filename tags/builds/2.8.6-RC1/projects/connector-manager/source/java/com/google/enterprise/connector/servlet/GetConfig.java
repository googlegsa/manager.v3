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
import com.google.enterprise.connector.manager.Context;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Admin servlet to retrieve the configuration of the Connector Manager
 * and all Connector instances.  This servlet returns a ZIP archive of all
 * the configuration files.  Access to this servlet is restricted to either
 * localhost or gsa.feed.host, based upon the HTTP RemoteAddress.</p>
 *
 * <p><b>Usage:</b>
 * <br>To retrieve the zipped configuration archive:
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConfig</pre>
 * <br>or
 * <br><pre>  http://[cm_host_addr]/connector-manager/getConfig/configuration.zip</pre>
 * </p>
 *
 * <p><br><b>Redirects and curl:</b>
 * When requesting the configuration files, this servlet returns a redirect
 * to the actual ZIP filename, configuration.zip.
 * This allows the browser, wget, or curl to pull the true filename off
 * the redirected URL so that it can name the file when storing locally.
 * If using curl to retrieve the files, please to use 'curl -L' to tell
 * curl to follow the redirect.  Unfortunately, when using 'curl -O' to
 * save the file locally, curl uses the pre-redirected name, rather than
 * the post-redirected name when naming the local file.  This forces you
 * to use 'curl -L -o output_filename'.</p>
 *
 * <p>Wget handles redirects appropriately without intervention, and names
 * the saved file as expected.</p>
 */
public class GetConfig extends HttpServlet {
  private static final String archiveName = "configuration.zip";
  private static final String[] excludedFiles =
      { "lib", "connector_manager.keystore", "web.xml" };
  private static final FilenameFilter fileFilter = new ConfigFilenameFilter();

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
      throws IOException, FileNotFoundException {
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
      throws IOException, FileNotFoundException {
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.RED, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    NDC.push("Support");
    try {
      Context context = Context.getInstance();

      // Fetch the name of the archive file to return. getPathInfo() returns
      // items with a leading '/', so we want to pull off only the basename.
      // WARNING: For security reasons, the PathInfo parameter must never
      // be passed directly to a File() or shell command.
      String fileName = baseName(req.getPathInfo());
      if (fileName == null) {
        // Force a redirect to the archive file.
        res.sendRedirect(res.encodeRedirectURL(
            baseName(req.getServletPath()) + "/" + archiveName));
      } else if (fileName.equalsIgnoreCase(archiveName)) {
        res.setContentType(ServletUtil.MIMETYPE_ZIP);
        ServletOutputStream out = res.getOutputStream();
        try {
          handleDoGet(context.getCommonDirPath(), out);
        } finally {
          out.close();
        }
      } else {
        // Force a redirect to the archive file.
        res.sendRedirect(res.encodeRedirectURL(archiveName));
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
   * Handler for doGet in order to do unit tests.
   *
   * @param configDir root of configuration files
   * @param out OutputStream where the response is written
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void handleDoGet(String configDir, OutputStream out)
      throws IOException, FileNotFoundException {
    File dir = new File(configDir);
    String relativeDir = dir.getParentFile().getCanonicalPath() + "/";

    if (dir.exists() && dir.isDirectory()) {
      //create a ZipOutputStream to zip the data to
      ZipOutputStream zout = new ZipOutputStream(out);
      zipDir(relativeDir, dir, zout);
      //close the stream
      zout.finish();
    } else {
      throw new FileNotFoundException("Configuration directory "
          + configDir + " not found.");
    }
  }

  /**
   * Add the directory and its contents to the ZipOutputStream
   *
   * @param relativeDir the directory make ZipEntries relative to.
   * @param dir the directory to add to the ZIP file.
   * @param zout a ZipOutputStream
   * @throws IOException
   */
  public static void zipDir(String relativeDir, File dir, ZipOutputStream zout)
      throws IOException {
    // Get a listing of the directory content.
    String[] dirList = dir.list(fileFilter);
    // Loop through dirList, and zip the files.
    for (int i = 0; i < dirList.length; i++) {
      File file = new File(dir, dirList[i]);
      if (file.isDirectory()) {
        // If the File object is a directory, call this
        // function again to add its content recursively.
        zipDir(relativeDir, file, zout);
        continue;
      }
      // We have plain file, not a directory.
      // Add a ZIP entry describing the file.
      String relativePath = file.getCanonicalPath();
      if (relativePath.startsWith(relativeDir)) {
        relativePath = relativePath.substring(relativeDir.length());
      }
      ZipEntry zentry = new ZipEntry(relativePath);
      zentry.setSize(file.length());
      zentry.setTime(file.lastModified());
      zout.putNextEntry(zentry);

      // Copy the file to the compressed stream.
      InputStream in = new FileInputStream(file);
      byte[] buf = new byte[16384];
      int bytesRead;
      while ((bytesRead = in.read(buf)) > 0) {
        zout.write(buf, 0, bytesRead);
      }
      in.close();
      zout.closeEntry();
    }
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
   * This is a FilenameFilter that filters out configuration
   * files that we do not wish to return.
   */
  private static class ConfigFilenameFilter implements FilenameFilter {
    /**
     * Tests if the specified file matches one of the files to exclude.
     *
     * @param dir the directory containing the file.
     * @param fileName a file in the directory.
     * @returns true if the fileName is not excluded, false otherwise.
     */
    public boolean accept(File dir, String fileName) {
      // Ignore ".", "..", hidden files, and old file versions.
      if (fileName.startsWith(".") || fileName.endsWith("~")) {
        return false;
      }
      // Iterate over the excluded filenames, looking for a match.
      for (int i = 0; i < excludedFiles.length; i++) {
        if (fileName.equalsIgnoreCase(excludedFiles[i])) {
          return false;
        }
      }
      return true;
    }
  }
}
