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

package com.google.enterprise.connector.pusher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * SMB protocol URLStreamHandler used to verify SMB URLs
 * for the DocPusher.  Although the GSA supports SMB URLs,
 * the default Java implementation of {@link java.net.URL} does not.
 * This subclass of {@link java.net.URLStreamHandler} will parse SMB URLs
 * (which should look like normal URLs), additionally applying some
 * constraints documented
 * <a href="http://www.google.com/support/enterprise/static/gsa/docs/admin/72/gsa_doc_set/admin_crawl/url_patterns.html#1076533">
 * here</a>.
 * <p>
 * This "StreamHandler" will not actually allow a caller to open a stream.
 * Attempting to do so will throw an "Unsupported operation" IOException.
 */
public class SmbURLStreamHandler extends URLStreamHandler {
  private static final int SMB_DEFAULT_PORT = 139;
  private static final SmbURLStreamHandler instance = new SmbURLStreamHandler();

  /**
   * Singleton Constructor.
   */
  private SmbURLStreamHandler() {}

  /**
   * Return Singleton instance of SmbURLStreamHandler.
   */
  public static SmbURLStreamHandler getInstance() {
    return instance;
  }

  /**
   * Return the default port for SMB service.
   */
  @Override
  protected int getDefaultPort() {
    return SMB_DEFAULT_PORT;
  }

  /**
   * This stream handler only does rudimentary URL validation
   * for the Connector Manager.  We don't actually support streaming
   * content via SMB.
   */
  @Override
  public URLConnection openConnection(URL url) throws IOException {
    throw new IOException("Unsupported operation: Cannot openConnection to URL "
                          + url.toExternalForm());
  }

  /**
   * Parse the SMB URL.  At this point the only thing we care about is
   * if the SMB URL passes the constraints mentioned in the this page:
   * http://www.google.com/support/enterprise/static/gsa/docs/admin/72/gsa_doc_set/admin_crawl/url_patterns.html#1076533
   */
  @Override
  protected void parseURL(URL url, String spec, int start, int limit) {
    if (!"smb".equalsIgnoreCase(url.getProtocol())) {
      throw new IllegalArgumentException("URL " + spec
          + " does not appear to be 'smb:' scheme.");
    }
    String subSpec = spec.substring(start, limit);
    if (subSpec.equals("//") || subSpec.equals("////")) {
      throw new IllegalArgumentException("Top level SMB URLs, like " +  spec
          + " are not supported here.");
    }
    if (subSpec.indexOf('\\', start) >= 0) {
      throw new IllegalArgumentException("Backslash character '\\' is not "
          + "permitted in URLs, even SMB URLs: " + spec);
    }

    super.parseURL(url, spec, start, limit);

    // Make sure the hostname is not omitted and is not a workgroup name.
    InetAddress hostAddr = getHostAddress(url);
    if (hostAddr == null) {
      throw new IllegalArgumentException("Host '" + url.getHost()
          + "' does not appear to be a valid server.");
    }
  }
}
