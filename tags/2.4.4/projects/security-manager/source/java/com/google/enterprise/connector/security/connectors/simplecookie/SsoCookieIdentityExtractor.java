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

package com.google.enterprise.connector.security.connectors.simplecookie;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Extract identity from a web site protected by a SSO system.
 * serverUrl points to a web page, we GET this page using cookies collected from
 * user session, we expect to receive a response with HTTP header of name "httpHeaderName".
 */
public class SsoCookieIdentityExtractor implements CookieIdentityExtractor {
  final String serverUri;
  final String httpHeaderName;

  private static final Logger LOGGER =
    Logger.getLogger(SsoCookieIdentityExtractor.class.getName());

  public SsoCookieIdentityExtractor(String strUrl, String headerName) {
    this.serverUri = strUrl;
    this.httpHeaderName = headerName;
  }

  public String extract(String s) {
    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
    params.setConnectionTimeout(3000); // 3 seconds
    HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    connectionManager.setParams(params);

    GetMethod getMethod = new GetMethod(serverUri);
    // Include cookie with request
    getMethod.setRequestHeader("Cookie", s);
    getMethod.setFollowRedirects(false);
    // set user-agent header
    getMethod.setRequestHeader("User-Agent", "SecMgr");

    HttpClient httpClient = new HttpClient(connectionManager);
    Header idHeader = null;

    try {
      int status = httpClient.executeMethod(getMethod);

      LOGGER.info("Got status: " + status);
      if (status == 200) {
        idHeader = getMethod.getResponseHeader(httpHeaderName);
        LOGGER.info(idHeader.toString());
      }
    } catch (IOException e) {
      LOGGER.warning(e.toString());
    } finally {
      getMethod.releaseConnection();
    }

    return((idHeader != null) ? idHeader.getValue() : null);
  }

}
