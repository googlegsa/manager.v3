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

package com.google.enterprise.sessionmanager;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * The credentials associated with a single authentication-domain group.  Only the username and
 * password are stored here; other credentials are domain-specific and are stored in the domain
 * credentials comprising the group.
 */ 
public class CredentialsGroup {

  private final AuthnDomainGroup metadata;
  private String username;
  private String password;
  private final List<DomainCredentials> elements;

  public CredentialsGroup(AuthnDomainGroup metadata) {
    this.metadata = metadata;
    elements = new ArrayList<DomainCredentials>();
  }

  public static List<CredentialsGroup> newGroups() {
    List<AuthnDomainGroup> elements = AuthnDomainGroup.getAllGroups();
    List<CredentialsGroup> groups = new ArrayList<CredentialsGroup>(elements.size());
    for (AuthnDomainGroup element: elements) {
      CredentialsGroup group = new CredentialsGroup(element);
      for (AuthnDomain domain: element.getDomains()) {
        new DomainCredentials(domain, group);
      }
      groups.add(group);
    }
    return groups;
  }

  public AuthnDomainGroup getMetadata() {
    return metadata;
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
        element.resetVerification();
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
    if (isVerifiable()) {
      for (DomainCredentials element: elements) {
        if (element.isVerified()) {
          return true;
        }
      }
    }
    return false;
  }

  public List<Cookie> getCookies() {
    List<Cookie> cookies = new ArrayList<Cookie>();
    for (DomainCredentials element: elements) {
      cookies.addAll(element.getCookies());
    }
    return cookies;
  }
}
