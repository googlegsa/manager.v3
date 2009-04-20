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

import javax.servlet.http.Cookie;

/**
 * An identity element in a credentials group.
 * Each element has a specific authentication mechanism and sample URL.
 * The elements in a credentials group are supposed to share the same username and
 * password, but may differ in some circumstances.  Each identity element is independently
 * verified.
 */
public class IdentityElement implements SecAuthnIdentity {

  private final IdentityElementConfig config;
  private final CredentialsGroup group;
  private String username;
  private String password;
  private VerificationStatus status;

  IdentityElement(IdentityElementConfig config, CredentialsGroup group) {
    this.config = config;
    this.group = group;
    status = VerificationStatus.TBD;
    group.getElements().add(this);
  }

  public static IdentityElement dummy() {
    return new IdentityElement(null, CredentialsGroup.dummy());
  }

  public AuthNMechanism getMechanism() {
    return config.getMechanism();
  }

  public String getSampleUrl() {
    return config.getSampleUrl();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDomain() {
    return group.getName();
  }

  public void setUsername(String username) {
    if ((username != null) && (username.length() == 0)) {
      username = null;
    }
    maybeResetVerification(this.username, username);
    this.username = username;
  }

  public void setPassword(String password) {
    if ((password != null) && (password.length() == 0)) {
      password = null;
    }
    maybeResetVerification(this.password, password);
    this.password = password;
  }

  private void maybeResetVerification(String s1, String s2) {
    if ((s1 == null) ? (s2 != null) : s1.equals(s2)) {
      status = VerificationStatus.TBD;
    }
  }

  public VerificationStatus getVerificationStatus() {
    return status;
  }

  public void setVerificationStatus(VerificationStatus status) {
    this.status = status;
  }

  public Collection<Cookie> getCookies() {
    return group.getCookies();
  }

  public void addCookie(Cookie c) {
    group.addCookie(c);
  }

  public Cookie getCookieNamed(String name) {
    return group.getCookieNamed(name);
  }
}
