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

package com.google.enterprise.security.connectors.simplecookie;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

public class SimpleCookieIdentityConnector implements Connector, Session, AuthenticationManager {

  private final CookieIdentityExtractor c;

  private final String cookieName;
  private final String idCookieName;

  SimpleCookieIdentityConnector(String cookieName, String idCookieName, CookieIdentityExtractor c) {
    this.cookieName = cookieName;
    this.idCookieName = idCookieName;
    this.c = c;
  }

  public AuthenticationResponse authenticate(AuthenticationIdentity identity) {
    AuthenticationResponse notfound = new AuthenticationResponse(false, null);
    String cookie = identity.getCookie(cookieName);
    if (cookie == null) {
      return notfound;
    }
    String newIdentity = c.extract(cookieName + "=" + cookie);
    if (newIdentity == null) {
      return notfound;
    }
    if (idCookieName.length() > 0)
      identity.setCookie(idCookieName, newIdentity);
    return new AuthenticationResponse(true, null);
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
