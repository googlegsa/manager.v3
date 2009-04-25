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

import com.google.enterprise.connector.spi.VerificationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.Cookie;

/**
 * The credentials associated with a single authentication-domain group.  Only the username and
 * password are stored here; other credentials are domain-specific and are stored in the domain
 * credentials comprising the group.
 */
public class CredentialsGroup {

  private final AuthnDomainGroup authnDomainGroup;
  private final List<DomainCredentials> elements;
  private String username;
  private String password;

  private CredentialsGroup(AuthnDomainGroup authnDomainGroup) {
    this.authnDomainGroup = authnDomainGroup;
    elements = new ArrayList<DomainCredentials>();
  }

  public static List<CredentialsGroup> newGroups(List<AuthnDomainGroup> adgs) {
    List<CredentialsGroup> cgs = new ArrayList<CredentialsGroup>();
    for (AuthnDomainGroup adg : adgs) {
      CredentialsGroup cg = new CredentialsGroup(adg);
      for (AuthnDomain ad : adg.getDomains()) {
        new DomainCredentials(ad, cg);
      }
      cgs.add(cg);
    }
    return cgs;
  }

  // Used for testing only:
  public static CredentialsGroup dummy() {
    return new CredentialsGroup(null);
  }

  public String getHumanName() {
    return authnDomainGroup.getHumanName();
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

  public List<DomainCredentials> getElements() {
    return elements;
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
