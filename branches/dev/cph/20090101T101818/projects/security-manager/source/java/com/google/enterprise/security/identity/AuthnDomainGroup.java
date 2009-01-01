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

import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.saml.server.CSVReader;

import java.io.FileReader;
import java.io.IOException;
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

  public static List<AuthnDomainGroup> getAuthnDomainGroups(String configFile)
      throws IOException {
    CSVReader reader = new CSVReader(new FileReader(configFile));
    List<AuthnDomainGroup> adgs = new ArrayList<AuthnDomainGroup>();
    String[] nextLine;

    // Each line is FQDN,realm,auth_method,optional_sample_login_url
    // If sample login is missing, we deduce as FQDN+realm
    while ((nextLine = reader.readNext()) != null) {
      AuthNMechanism method = null;
      if (nextLine[2].equals(AuthNMechanism.BASIC_AUTH.toString())) {
        method = AuthNMechanism.BASIC_AUTH;
      }
      if (nextLine[2].equals(AuthNMechanism.FORMS_AUTH.toString())) {
        method = AuthNMechanism.FORMS_AUTH;
      }
      AuthnDomainGroup adg = new AuthnDomainGroup(nextLine[0] + nextLine[1]);
      @SuppressWarnings("unused")
      AuthnDomain domain = new AuthnDomain(
          nextLine[0] + nextLine[1], method,
          "".equals(nextLine[3]) ? nextLine[0] + nextLine[1] : nextLine[3], adg);
      adgs.add(adg);
    }

    return adgs;
  }
}
