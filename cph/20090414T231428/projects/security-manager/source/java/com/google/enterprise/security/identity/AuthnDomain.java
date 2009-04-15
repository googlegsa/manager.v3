// Copyright (C) 2008, 2009 Google Inc.
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

import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of resources that are authenticated by a single IdP using a particular mechanism.  The
 * resources are described by a set of URL patterns.  Information about the IdP is specific to the
 * mechanism: for example, forms-auth IdPs have a login URL.
 */
public class AuthnDomain {

  private final String name;  // The domain name must be unique.
  private final AuthNMechanism mechanism;
  private final AuthnDomainGroup group;
  private final List<String> urlPatterns;
  private final String sampleUrl;

  public AuthnDomain(String name, AuthNMechanism mechanism, String sampleUrl,
                     AuthnDomainGroup group) {
    this.name = name;
    this.mechanism = mechanism;
    this.group = group;
    this.sampleUrl = sampleUrl;
    urlPatterns = new ArrayList<String>();
    group.addDomain(this);
  }

  public String getName() {
    return name;
  }

  public AuthNMechanism getMechanism() {
    return mechanism;
  }

  public AuthnDomainGroup getGroup() {
    return group;
  }

  public List<String> getUrlPatterns() {
    return urlPatterns;
  }

  public String getSampleUrl() {
    return sampleUrl;
  }
}
