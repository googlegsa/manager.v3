// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.security.identity;

import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.servlet.http.Cookie;

/**
 * The credentials associated with a single authentication domain.  (Does not include
 * username/password, which is stored in the associated Credentials Group.)
 */
public class DomainCredentials implements SecAuthnIdentity {

  private final AuthnDomain domain;
  private final CredentialsGroup group;
  private VerificationStatus status;
  private final Vector<Cookie> cookies;

  DomainCredentials(AuthnDomain domain, CredentialsGroup group) {
    this.domain = domain;
    this.group = group;
    status = VerificationStatus.TBD;
    cookies = new Vector<Cookie>();
    group.getElements().add(this);
  }

  public static DomainCredentials dummy() {
    return new DomainCredentials(null, CredentialsGroup.dummy());
  }

  public String getDomain() {
    return domain.getName();
  }

  public AuthNMechanism getMechanism() {
    return domain.getMechanism();
  }

  public String getSampleUrl() {
    return domain.getSampleUrl();
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

  public void addCookie(Cookie c) {
    cookies.add(c);
  }

  // For testing:
  public void clearCookies() {
    cookies.clear();
  }

  public VerificationStatus getVerificationStatus() {
    return status;
  }

  public void setVerificationStatus(VerificationStatus status) {
    this.status = status;
  }

  public Collection<Cookie> getCookies() {
    return Collections.unmodifiableCollection(cookies);
  }

  public Cookie getCookieNamed(String name) {
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
