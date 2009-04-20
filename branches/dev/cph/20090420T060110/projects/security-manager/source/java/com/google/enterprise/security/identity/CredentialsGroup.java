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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * The credentials associated with a single group, in which the username (and password, if
 * any) are assumed to be the same for all credentials.  There may be multiple mechanisms
 * supported for a particular group, each of which is independently verified.  Cookies are
 * shared between all mechanisms.
 */
public class CredentialsGroup {

  private final CredentialsGroupConfig config;
  private final List<IdentityElement> elements;
  private final List<Cookie> cookies;

  private CredentialsGroup(CredentialsGroupConfig config) {
    this.config = config;
    elements = new ArrayList<IdentityElement>();
    cookies = new ArrayList<Cookie>();
  }

  public static List<CredentialsGroup> newGroups(List<CredentialsGroupConfig> configs) {
    List<CredentialsGroup> groups = new ArrayList<CredentialsGroup>();
    for (CredentialsGroupConfig groupConfig : configs) {
      CredentialsGroup group = new CredentialsGroup(groupConfig);
      for (IdentityElementConfig identityConfig : groupConfig.getElements()) {
        new IdentityElement(identityConfig, group);
      }
      groups.add(group);
    }
    return groups;
  }

  // Used for testing only:
  public static CredentialsGroup dummy() {
    return new CredentialsGroup(null);
  }

  public String getName() {
    return config.getName();
  }

  public String getHumanName() {
    return config.getHumanName();
  }

  public List<IdentityElement> getElements() {
    return elements;
  }

  // TODO(cph): Need more intelligent selection if the usernames are ever different.
  public String getUsername() {
    for (IdentityElement element: elements) {
      if (element.getUsername() != null) {
        return element.getUsername();
      }
    }
    return null;
  }

  // TODO(cph): This is almost certainly wrong.  As used in SamlAuthn, it means "should we
  // try to verify this group?".  So what are the conditions in which that's true?  One
  // simple heuristic: if there are no verified elements and we have at least one element
  // that could be verified, then we should do so.  So we need to change IdentityElement
  // to have an isVerifiable() method.
  public boolean isVerifiable() {
    for (IdentityElement element: elements) {
      if ((element.getUsername() != null) && (element.getPassword() != null)) {
        return true;
      }
    }
    return false;
  }

  public boolean isVerified() {
    if (!isVerifiable()) {
      return false;
    }
    for (IdentityElement element: elements) {
      if (element.getVerificationStatus() == VerificationStatus.REFUTED) {
        return false;
      }
    }
    for (IdentityElement element: elements) {
      if (element.getVerificationStatus() == VerificationStatus.VERIFIED) {
        return true;
      }
    }
    return false;
  }

  public Collection<Cookie> getCookies() {
    return Collections.unmodifiableCollection(cookies);
  }

  public void addCookie(Cookie c) {
    cookies.add(c);
  }

  // For testing:
  public void clearCookies() {
    cookies.clear();
  }

  public Cookie getCookieNamed(String name) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }
}
