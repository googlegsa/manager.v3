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

import com.google.enterprise.common.HttpClientInterface;
import com.google.enterprise.common.HttpExchange;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class BasicAuthConnector implements Connector, Session, AuthenticationManager {

  private final HttpClientInterface httpClient;
  @SuppressWarnings("unused")
  private final String something;

  private static final Logger LOGGER =
    Logger.getLogger(BasicAuthConnector.class.getName());

  public BasicAuthConnector(HttpClientInterface httpClient, String data) {
    this.httpClient = httpClient;
    this.something = data;      // not used
  }

  public AuthenticationResponse authenticate(AuthenticationIdentity identity)
      throws RepositoryException {
    AuthenticationResponse notfound = new AuthenticationResponse(false, null);
    String username = identity.getUsername();
    String password = identity.getPassword();
    if (username == null || password == null) {
      return notfound;
    }

    URL loginUrl;
    try {
      loginUrl = new URL(identity.getLoginUrl());
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
    HttpExchange exchange = httpClient.getExchange(loginUrl);
    exchange.setBasicAuthCredentials(username, password);
    exchange.setRequestHeader("User-Agent", "SecMgr");
    int status = 0;
    try {
      status = exchange.exchange();
    } catch (IOException e) {
      LOGGER.info(e.toString());
      throw new RepositoryException(e);
    } finally {
      exchange.close();
    }

    if (status == 200) {
      return new AuthenticationResponse(true, username);
    }
    return notfound;
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
