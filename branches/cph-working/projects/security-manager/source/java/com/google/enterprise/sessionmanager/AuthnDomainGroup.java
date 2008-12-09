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

public class AuthnDomainGroup {

  private static final List<AuthnDomainGroup> allGroups = new ArrayList<AuthnDomainGroup>();

  private final String humanName;
  private final List<AuthnDomain> domains;

  public AuthnDomainGroup(String humanName) {
    this.humanName = humanName;
    domains = new ArrayList<AuthnDomain>();
    allGroups.add(this);
  }

  public static List<AuthnDomainGroup> getAllGroups() {
    return allGroups;
  }

  public String getHumanName() {
    return humanName;
  }

  public void addDomain(AuthnDomain domain) {
    domains.add(domain);
  }

  public List<AuthnDomain> getDomains() {
    return domains;
  }
}
