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

package com.google.enterprise.security.identity;

import com.google.enterprise.connector.spi.AuthenticationIdentity;

import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import javax.servlet.http.Cookie;

/**
 * The credentials associated with a single authentication domain.  (Does not include
 * username/password, which is stored in the associated Credentials Group.)
 */ 
public class DomainCredentials implements AuthenticationIdentity {

  private enum Decision {
    TBD,            // haven't gone through verification yet
    VERIFIED,       // recognized by IdP
    REPUDIATED,     // unrecognized by IdP
  }

  private final AuthnDomain domain;
  private final CredentialsGroup group;
  private Decision decision;
  private final Vector<Cookie> cookies;

  public DomainCredentials(AuthnDomain domain, CredentialsGroup group) {
    this.domain = domain;
    this.group = group;
    decision = Decision.TBD;
    cookies = new Vector<Cookie>();
    group.getElements().add(this);
  }

  public AuthnDomain getDomain() {
    return domain;
  }

  public CredentialsGroup getGroup() {
    return group;
  }

  public String getUsername() {
    return group.getUsername();
  }

  public String getPassword() {
    return group.getPassword();
  }

  /**
   * Get a named cookie value.
   *
   * @param cookieName a non-null, non-empty String
   * @return the cookie value, as a String, or null if not set
   */
  public String getCookie(String cookieName) {
    for (Cookie c : cookies) {
      if (c.getName().equals(cookieName)) {
        return c.getValue();
      }
    }
    return null;
  }

  /**
   * Set a named cookie value. If there was a previous value for this cookie,
   * the old value is replaced by the specified value. If the value is null or
   * empty, this has the effect of removing the cookie, if present.
   *
   * @param cookieName a non-null, non-empty String
   * @param value the new value for this cookie; if null or empty, then the
   *        cookie is removed
   * @return the previous value associated with this cookie name, or null if
   *         there was no value
   */
  public String setCookie(String cookieName, String value) {
    String oldVal = "";
    for (Cookie c : cookies) {
      if (c.getName().equals(cookieName)) {
        if (null == value || "".equals(value)) {
          cookies.remove(c);
        }
        oldVal = c.getValue();
        c.setValue(value);
        return oldVal;
      }
    }

    cookies.add(new Cookie(cookieName, value));
    return null;
  }

  public void setCookie(Cookie c) {
    cookies.add(c);
  }

  public Set getCookieNames() {
    HashSet<String> s = new HashSet<String>();
    for (Cookie c : cookies) {
      s.add(c.getName());
    }
    return s;
  }

  public String getLoginUrl() {
    return domain.getLoginUrl();
  }

  public void resetVerification() {
    this.decision = Decision.TBD;
  }

  public boolean needsVerification() {
    return decision == Decision.TBD;
  }

  public boolean isVerified() {
    return decision == Decision.VERIFIED;
  }

  public void setVerified(boolean isVerified) {
    this.decision = isVerified ? Decision.VERIFIED : Decision.REPUDIATED;
  }

  public Vector<Cookie> getCookies() {
    return cookies;
  }

  public Cookie getFullCookie(String name) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  private String dumpCookies() {
    StringBuilder sb = new StringBuilder();
    for (Cookie c : cookies) {
      sb.append(c.getName() + "::" + c.getValue() + ";");
    }
    return sb.toString();
  }

  public String dumpInfo() {
    return getUsername() + ":" + getPassword() + ":" + dumpCookies(); 
  }
}
