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

package com.google.enterprise.connector.security.identity;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This mis-named class models the configuration info for a credentials group.  This is
 * the part of the credentials group that is the same for all sessions, and is specified
 * by administrator configuration.  Each session then has a CredentialsGroup object
 * corresponding to each of these objects, which carries the session-specific data.
 */
public class AuthnDomainGroup {

  private final String humanName;  // A string to identify this group in the login form.
  private final List<AuthnDomain> elements;  // The verification elements for this group.

  public AuthnDomainGroup(String humanName) {
    this.humanName = humanName;
    elements = new ArrayList<AuthnDomain>();
  }

  public String getHumanName() {
    return humanName;
  }

  void addElement(AuthnDomain element) {
    elements.add(element);
  }

  public List<AuthnDomain> getElements() {
    return ImmutableList.copyOf(elements);
  }
}
