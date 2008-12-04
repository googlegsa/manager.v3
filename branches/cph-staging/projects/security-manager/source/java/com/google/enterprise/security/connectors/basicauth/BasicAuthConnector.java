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

package com.google.enterprise.security.connectors.basicauth;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.logging.Logger;

import javax.xml.ws.http.HTTPException;

public class BasicAuthConnector implements Connector, Session, AuthenticationManager {

  @SuppressWarnings("unused")
  private final String something;

  private static final Logger LOGGER =
    Logger.getLogger(BasicAuthConnector.class.getName());

  public BasicAuthConnector(String data) {
    this.something = data;      // not used
  }

  public AuthenticationResponse authenticate(AuthenticationIdentity identity) {
    AuthenticationResponse notfound = new AuthenticationResponse(false, null);
    String username = identity.getUsername();
    String password = identity.getPassword();
    if (username == null || password == null) {
      return notfound;
    }
    
    HttpClient httpClient = new HttpClient();
    httpClient.getState().setCredentials(new AuthScope(null, -1, null),
        new UsernamePasswordCredentials(username, password));
    GetMethod getMethod = new GetMethod(identity.getLoginUrl());
    getMethod.setDoAuthentication(true);
    getMethod.setRequestHeader("User-Agent", "SecMgr");
    int status = 0;
    try {
      status = httpClient.executeMethod(getMethod);
      System.out.println(getMethod.getStatusLine());
      if (status > 300) {
        throw new HTTPException(status);
      }
     } catch (Exception e) {
      LOGGER.warning(e.toString());
    } finally {
      getMethod.releaseConnection();
    }

    if (status == 200) {
      return new AuthenticationResponse(true, username);
    }
    return new AuthenticationResponse(false, null);
  }

  public Session login() {
    return this;
  }

  public AuthenticationManager getAuthenticationManager() {
    return this;
  }

  public AuthorizationManager getAuthorizationManager() {
    throw new UnsupportedOperationException();
  }

  public TraversalManager getTraversalManager() {
    throw new UnsupportedOperationException();
  }

}
