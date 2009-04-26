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

package com.google.enterprise.security.identity;

import com.google.enterprise.common.FileUtil;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * CsvConfig is the class that reads the security manager's authn sites
 * configuration from a CSV file.
 *
 * The expected format of the config file is as follows:
 *
 * credentials-group-name,FQDN,subpath,AuthnMech[,sampleUrl]
 *
 * All the fields following the credentials-group name refer to a specific means of
 * verification.  Thus, multiple lines may share the same credentials-group name to
 * represent multiple means of verification that belong to the same credentials group.
 *
 * The credentials-group name may be any arbitrary alphanumerical string.
 *
 * The AuthnMech string may be any value specified in GsaConstants.AuthNMechanism.
 */
public class CsvConfig implements IdentityConfig {

  private static final Logger LOGGER = Logger.getLogger(CsvConfig.class.getName());
  private final String configFile;

  public CsvConfig(String configFile) {
    this.configFile = configFile;
  }

  public List<AuthnDomainGroup> getConfig() throws IOException {
    return getConfig(new FileReader(FileUtil.getContextFile(configFile)));
  }

  /**
   * For each valid line in the provided configuration, getConfig will create
   * an AuthnDomain and place it in the AuthnDomainGroup specified by that
   * line, creating new AuthnDomainGroups as necessary.
   */
  public List<AuthnDomainGroup> getConfig(Reader in) throws IOException {
    CSVReader reader = new CSVReader(in);
    String[] nextLine;

    HashMap<String,AuthnDomainGroup> adgMap = new HashMap<String,AuthnDomainGroup>();
    while ((nextLine = reader.readNext()) != null) {
      if (nextLine.length < 5) {
        LOGGER.severe("Invalid configuration line, skipping: \n" + nextLine);
        continue;
      }
      AuthNMechanism authMech = authNMechFromString(nextLine[3]);
      if (null == authMech) {
        LOGGER.severe("Invalid AuthnMechanism for this line, " +
            "skipping this site: \n" + nextLine);
        continue;
      }

      String adgName = nextLine[0];
      if (!adgMap.containsKey(adgName)) {
        adgMap.put(adgName, new AuthnDomainGroup(adgName));
      }
      new AuthnDomain(nextLine[1] + nextLine[2], authMech,
          "".equals(nextLine[4]) ? nextLine[1] + nextLine[2] : nextLine[4],
          adgMap.get(adgName));
    }

    return new ArrayList<AuthnDomainGroup>(adgMap.values());
  }

  public AuthNMechanism authNMechFromString(String mech) {
    try {
      return AuthNMechanism.valueOf(mech);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  // This is useful in unit testing.
  public static List<AuthnDomainGroup> readConfigFile(String configFile) throws IOException {
    return (new CsvConfig(configFile)).getConfig();
  }

  public static List<AuthnDomainGroup> readConfigFile(Reader in) throws IOException {
    return (new CsvConfig("AuthSites.conf")).getConfig(in);
  }
}
