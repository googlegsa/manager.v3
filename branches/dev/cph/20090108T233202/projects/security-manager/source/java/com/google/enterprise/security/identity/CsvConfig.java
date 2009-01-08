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

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvConfig implements IdentityConfig {

  private final String configFile;

  public CsvConfig(String configFile) {
    this.configFile = configFile;
  }

  public List<AuthnDomainGroup> getConfig() throws IOException {
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
      new AuthnDomain(
          nextLine[0] + nextLine[1], method,
          "".equals(nextLine[3]) ? nextLine[0] + nextLine[1] : nextLine[3], adg);
      adgs.add(adg);
    }
    return adgs;
  }

  // This is useful in unit testing.
  public static List<AuthnDomainGroup> readConfigFile(String configFile) throws IOException {
    return (new CsvConfig(configFile)).getConfig();
  }
}