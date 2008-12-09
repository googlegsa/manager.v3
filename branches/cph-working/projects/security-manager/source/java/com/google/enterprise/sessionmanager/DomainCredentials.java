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

public class DomainCredentials {

  private enum Decision {
    TBD,            // haven't gone through verification yet
    VERIFIED,       // recognized by IdP
    REPUDIATED,     // unrecognized by IdP
  }

  private final AuthnDomain domain;
  private final CredentialsGroup group;
  private Decision decision;
  private final List<Cookie> cookies;

  public DomainCredentials(AuthnDomain domain, CredentialsGroup group) {
    this.domain = domain;
    this.group = group;
    decision = Decision.TBD;
    cookies = new ArrayList<Cookie>();
    group.getElements().add(this);
  }

  public AuthnDomain getDomain() {
    return domain;
  }

  public CredentialsGroup getGroup() {
    return group;
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

  public List<Cookie> getCookies() {
    return cookies;
  }

  public Cookie getCookie(String name) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }
}
