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

package com.google.enterprise.security.identity;

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.spi.VerificationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

/**
 * This class models the per-session information for a credentials group, which is a set
 * of identity verification elements that share the same username and password for each
 * user.  This object directly holds the per-session data, while the configuration info,
 * which is shared by all sessions, is stored in a separate object.
 */
public class CredentialsGroup {

  private final AuthnDomainGroup configInfo;
  private final HttpSession session;
  private final List<DomainCredentials> elements;
  private String username;
  private String password;

  private CredentialsGroup(AuthnDomainGroup configInfo, HttpSession session) {
    this.configInfo = configInfo;
    this.session = session;
    elements = new ArrayList<DomainCredentials>();
  }

  public static List<CredentialsGroup> newGroups(
      List<AuthnDomainGroup> adgs, HttpSession session) {
    List<CredentialsGroup> cgs = new ArrayList<CredentialsGroup>();
    for (AuthnDomainGroup adg : adgs) {
      CredentialsGroup cg = new CredentialsGroup(adg, session);
      for (AuthnDomain ad : adg.getElements()) {
        new DomainCredentials(ad, cg);
      }
      cgs.add(cg);
    }
    return cgs;
  }

  // Used for testing only:
  static CredentialsGroup dummy(HttpSession session) {
    return new CredentialsGroup(null, session);
  }

  public HttpSession getSession() {
    return session;
  }

  public String getHumanName() {
    return configInfo.getHumanName();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    if ((username != null) && (username.length() == 0)) {
      username = null;
    }
    maybeResetVerification(this.username, username);
    this.username = username;
  }

  public String getPassword() {
    return password;
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
      for (DomainCredentials element: elements) {
        element.setVerificationStatus(VerificationStatus.TBD);
      }
    }
  }

  void addElement(DomainCredentials element) {
    elements.add(element);
  }

  public List<DomainCredentials> getElements() {
    return ImmutableList.copyOf(elements);
  }

  public boolean isVerifiable() {
    return ((username != null) && (password != null));
  }

  public boolean isVerified() {
    if (!isVerifiable()) {
      return false;
    }
    for (DomainCredentials element: elements) {
      if (element.getVerificationStatus() == VerificationStatus.REFUTED) {
        return false;
      }
    }
    for (DomainCredentials element: elements) {
      if (element.getVerificationStatus() == VerificationStatus.VERIFIED) {
        return true;
      }
    }
    return false;
  }

  public Vector<Cookie> getCookies() {
    Vector<Cookie> cookies = new Vector<Cookie>();
    for (DomainCredentials element: elements) {
      cookies.addAll(element.getCookies());
    }
    return cookies;
  }
}
