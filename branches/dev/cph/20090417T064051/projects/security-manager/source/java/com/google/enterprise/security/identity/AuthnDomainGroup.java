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

import java.util.ArrayList;
import java.util.List;

/**
 * A group of authentication domains that share the same username/password for every user.
 * This grouping is used to partition the login form.
 */ 
public class AuthnDomainGroup {

  private final String humanName;  // A string to identify this group in the login form.
  private final List<AuthnDomain> domains;

  public AuthnDomainGroup(String humanName) {
    this.humanName = humanName;
    domains = new ArrayList<AuthnDomain>();
  }

  public String getHumanName() {
    return humanName;
  }

  public void addDomain(AuthnDomain domain) {
    if (domains.contains(domain)) {
      return;
    }
    domains.add(domain);
  }

  public List<AuthnDomain> getDomains() {
    return domains;
  }
}
