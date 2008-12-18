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

package com.google.enterprise.saml.server;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.saml.common.GsaConstants.AuthNDecision;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.Cookie;

/**
 * Keep track of the user identities we manage.
 *
 */
public class UserIdentity implements AuthenticationIdentity {

  private final String username;
  private final String password;
  private AuthSite site;
  private AuthNDecision status;

  private final Vector<Cookie> cookieJar;

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public UserIdentity(final String username, final String password, final AuthSite site) {
    this.username = username;
    this.password = password;
    this.site = site;
    cookieJar = new Vector<Cookie>();
  }

  public String getCookie(String cookieName) {
    if (cookieName == null || cookieName.length() < 1) {
      throw new IllegalArgumentException();
    }
    for (Cookie c : cookieJar) {
      if (cookieName.equals(c.getName()))
        return c.getValue();
    }
    return null;
  }

  public String setCookie(String cookieName, String value) {
    String oldVal = getCookie(cookieName);
    Cookie c = new Cookie(cookieName, value);
    setCookie(c);
    return oldVal;
  }

  public void setCookie(Cookie c) {
    cookieJar.add(c);
  }

  @SuppressWarnings("unchecked")
  public Set getCookieNames() {
    Set<String> result = new HashSet<String>();
    for (Cookie c : cookieJar) {
      result.add(c.getName());
    }
    return result;
  }

  public Vector<Cookie> getCookies() {
    return this.cookieJar;
  }
  public void setAuthSite(AuthSite site) {
    this.site = site;
  }
  public AuthSite getAuthSite() {
    return this.site;
  }

  public String getLoginUrl() {
    return site.getLoginUri();
  }

  public void setVerified() {
    status = AuthNDecision.VERIFIED;
  }
  public boolean isVerified() {
    return(status == AuthNDecision.VERIFIED);
  }

  /**
   * For logging purposes.
   * @return a string containing user, password, and authsite info representing
   * this UserIdentity
   */
  public String toString() {
    return (username == null ? "null-user" : username) + " " +
           (password == null? "null-password" : password) + " " +
           (site == null ? "null-authsite" : site.getLoginUri());
  }
}
