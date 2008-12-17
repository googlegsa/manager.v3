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

package com.google.enterprise.common;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A real instance of HttpClientInterface, using the HttpClient library for transport.
 */
public class HttpClientAdapter implements HttpClientInterface {
  private static final int CONNECTION_TIMEOUT = 3000;
  private static final int IDLE_TIMEOUT = 3000;

  private final HttpConnectionManager connectionManager;

  public HttpClientAdapter() {
    // set up ConnectionManager to be used.
    connectionManager = new MultiThreadedHttpConnectionManager();

    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
    params.setConnectionTimeout(CONNECTION_TIMEOUT);
    connectionManager.setParams(params);

    IdleConnectionTimeoutThread idleConnectionTimeoutThread
        = new IdleConnectionTimeoutThread();
    idleConnectionTimeoutThread.setTimeoutInterval(IDLE_TIMEOUT);
    idleConnectionTimeoutThread.start();
    idleConnectionTimeoutThread.addConnectionManager(connectionManager);
  }

  /** {@inheritDoc} */
  public HttpExchange newExchange(String method, URL url, List<StringPair> parameters) {
    return new ClientExchange(connectionManager, method, url, parameters);
  }

  private static class ClientExchange implements HttpExchange {
    private static final Logger LOGGER = Logger.getLogger(ClientExchange.class.getName());

    private final HttpMethod httpMethod;
    private final HttpClient httpClient;

    public ClientExchange(HttpConnectionManager connectionManager,
                          String method, URL url, List<StringPair> parameters) {
      if ("POST".equalsIgnoreCase(method)) {
        httpMethod = new PostMethod(url.toString());
        httpMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        PostMethod pm = PostMethod.class.cast(httpMethod);
        for (StringPair p: parameters) {
          pm.addParameter(p.getName(), p.getValue());
        }
      } else if ("GET".equalsIgnoreCase(method)) {
        httpMethod = new GetMethod(url.toString());
      } else {
        throw new IllegalArgumentException("unknown method: " + method);
      }
      httpMethod.setFollowRedirects(false);
      httpMethod.setPath(url.getPath());
      String query = url.getQuery();
      if ((query != null) && (query.length() > 0)) {
        httpMethod.setQueryString(query);
      }
      httpClient = new HttpClient(connectionManager);
      httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    }

    /** {@inheritDoc} */
    public void setProxy(String proxy) {
      if (null == proxy) {
        return;
      }

      String[] proxyInfo = proxy.split(":");
      if (proxyInfo.length != 2) {
        LOGGER.warning("Error parsing proxy config file entry.");
        return;
      }

      httpClient.getHostConfiguration().
        setProxy(proxyInfo[0], Integer.parseInt(proxyInfo[1]));
    }

    /** {@inheritDoc} */
    public void setRequestHeader(String name, String value) {
      httpMethod.setRequestHeader(name, value);
    }

    /** {@inheritDoc} */
    public int exchange() throws IOException {
      try {
        return httpClient.executeMethod(httpMethod);
      } catch (HttpException e) {
        throw new IOException(e);
      }
    }

    /** {@inheritDoc} */
    public String getResponseEntityAsString() throws IOException {
      return httpMethod.getResponseBodyAsString();
    }

    /** {@inheritDoc} */
    public String getResponseHeaderValue(String name) {
      for (Header header: httpMethod.getResponseHeaders(name)) {
        if (header.getName().equals(name)) {
          return header.getValue();
        }
      }
      return null;
    }

    /** {@inheritDoc} */
    public List<String> getResponseHeaderValues(String name) {
      List<String> result = new ArrayList<String>();
      for (Header header: httpMethod.getResponseHeaders(name)) {
        if (header.getName().equals(name)) {
          result.add(header.getValue());
        }
      }
      return result;
    }

    /** {@inheritDoc} */
    public int getStatusCode() {
      return httpMethod.getStatusCode();
    }

    /** {@inheritDoc} */
    public void close() {
      httpMethod.releaseConnection();
    }
  }
}
