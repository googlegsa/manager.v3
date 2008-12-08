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

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

/**
 * Calls the authenticate method of a supplied {@link AuthenticationManager},
 * using a supplied {@link AuthenticationIdentity}, but first reconciles the
 * identity with a security context Map, and also reconciles the context on the
 * way out. If no security context is supplied, this amounts to simply calling
 * the authentication manager directly.
 */
public class AuthnCaller {

  private static final Logger LOGGER = Logger.getLogger(AuthnCaller.class.getName());

  private final AuthenticationManager authnManager;
  private final AuthenticationIdentity identity;
  private final List<Cookie> cookies;

  @SuppressWarnings("unchecked")
  public AuthnCaller(AuthenticationManager authnManager, AuthenticationIdentity identity,
                     List<Cookie> cookies) {
    this.authnManager = authnManager;
    this.identity = identity;
    this.cookies = cookies;
  }

  public AuthenticationResponse authenticate() {
    if (authnManager == null) {
      return null;
    }
    for (Cookie c: cookies) {
      identity.setCookie(c);
    }
    AuthenticationResponse authenticationResponse = null;
    try {
      authenticationResponse = authnManager.authenticate(identity);
    } catch (RepositoryLoginException e) {
      LOGGER.log(Level.WARNING, "Login: ", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Repository: ", e);
    }

    reconcileReturnedCookiesWithContext();
    return authenticationResponse;
  }

  /**
   * This implementation currently just throws away any changes the
   * authentication manager may have made to pre-existing cookies. It only adds
   * the values for new cookies.
   */
  private void reconcileReturnedCookiesWithContext() {
    for (Object o: identity.getCookieNames()) {
      String name = String.class.cast(o);
      if (!knownCookie(name)) {
        cookies.add(identity.getCookie(name));
      }
    }
  }

  private boolean knownCookie(String name) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
