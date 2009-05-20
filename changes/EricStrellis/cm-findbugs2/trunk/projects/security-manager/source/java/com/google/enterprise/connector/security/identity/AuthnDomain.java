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


/**
 * This mis-named class models the configuration info for a set of DomainCredential
 * objects.  This information captures everything needed in order to verify an identity
 * using a particular mechanism.  For each of these objects, there is a set of per-session
 * DomainCredential objects that capture the data for verifying a particular identity in a
 * particular session.
 */
public class AuthnDomain {

  private final String name;              // The name must be unique.
  private final AuthnMechanism mechanism; // The mechanism to be used.
  private final String sampleUrl;         // A sample URL to GET for verification.
  private final String authority;         // A URI identifying the identity's authority.

  public AuthnDomain(String name, AuthnMechanism mechanism, String sampleUrl,
                     String authority, AuthnDomainGroup adg) {
    this.name = name;
    this.mechanism = mechanism;
    this.sampleUrl = sampleUrl;
    this.authority = authority;
    adg.addElement(this);
  }

  public String getName() {
    return name;
  }

  public AuthnMechanism getMechanism() {
    return mechanism;
  }

  public String getSampleUrl() {
    return sampleUrl;
  }

  public String getAuthority() {
    return authority;
  }
}
