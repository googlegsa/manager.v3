// Copyright (C) 2008 Google Inc.
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
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
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

  public HttpExchange getExchange(URL url) {
    GetMethod method = new GetMethod(url.toString());
    setPathFields(method, url);
    return new ClientExchange(connectionManager, method);
  }

  public HttpExchange postExchange(URL url, List<StringPair> parameters) {
    PostMethod method = new PostMethod(url.toString());
    setPathFields(method, url);
    if (parameters != null) {
      method.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      for (StringPair p: parameters) {
        method.addParameter(p.getName(), p.getValue());
      }
    }
    return new ClientExchange(connectionManager, method);
  }

  private void setPathFields(HttpMethod method, URL url) {
    method.setFollowRedirects(false);
    method.setPath(url.getPath());
    String query = url.getQuery();
    if ((query != null) && (query.length() > 0)) {
      method.setQueryString(query);
    }
  }

  private static class ClientExchange implements HttpExchange {
    private static final Logger LOGGER = Logger.getLogger(ClientExchange.class.getName());

    private final HttpMethod httpMethod;
    private final HttpClient httpClient;

    public ClientExchange(HttpConnectionManager connectionManager, HttpMethod httpMethod) {
      this.httpMethod = httpMethod;
      httpClient = new HttpClient(connectionManager);
      httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    }

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

    public void setBasicAuthCredentials(String username, String password) {
      httpClient.getState().setCredentials(
          new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
          new UsernamePasswordCredentials(username, password));
      httpClient.getParams().setAuthenticationPreemptive(true);
      httpMethod.setDoAuthentication(true);
    }

      public void setRequestHeader(String name, String value) {
      httpMethod.setRequestHeader(name, value);
    }

      public int exchange() throws IOException {
      try {
        return httpClient.executeMethod(httpMethod);
      } catch (HttpException e) {
        throw new IOException(e);
      }
    }

      public String getResponseEntityAsString() throws IOException {
      return httpMethod.getResponseBodyAsString();
    }

      public String getResponseHeaderValue(String name) {
      for (Header header: httpMethod.getResponseHeaders(name)) {
        if (header.getName().equals(name)) {
          return header.getValue();
        }
      }
      return null;
    }

      public List<String> getResponseHeaderValues(String name) {
      List<String> result = new ArrayList<String>();
      for (Header header: httpMethod.getResponseHeaders(name)) {
        if (header.getName().equals(name)) {
          result.add(header.getValue());
        }
      }
      return result;
    }

      public int getStatusCode() {
      return httpMethod.getStatusCode();
    }

      public void setRequestBody(byte[] requestContent) {
      String method = httpMethod.getName();
      if ("POST".equalsIgnoreCase(method)) {
        PostMethod pm = PostMethod.class.cast(httpMethod);
        String contentType = pm.getRequestHeader("Content-Type").toString();
        System.out.println("My content-type is " + contentType);
        pm.setRequestEntity(new ByteArrayRequestEntity(requestContent, contentType));
      }
    }

      public void close() {
      httpMethod.releaseConnection();
    }
  }
}
