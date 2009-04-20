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
 * group name,FQDN,subpath,AuthnMech,(optionally)loginUrl
 *
 * All the fields following the group name refer to a specific identity element.  Thus,
 * multiple lines may share the same group name to represent multiple identity elements
 * that belong to the same credentials group.
 *
 * The group name may be any arbitrary alphanumerical string.
 *
 * The AuthnMech string may be any of the names in
 * com.google.enterprise.saml.common.AuthNMechanism.
 */
public class CsvConfig implements IdentityConfig {

  private static final Logger LOGGER = Logger.getLogger(CsvConfig.class.getName());
  private final String configFile;

  public CsvConfig(String configFile) {
    this.configFile = configFile;
  }

  public List<CredentialsGroupConfig> getConfig() throws IOException {
    return getConfig(new FileReader(FileUtil.getContextFile(configFile)));
  }

  /**
   * For each valid line in the provided configuration, getConfig will create
   * an IdentityElementConfig and place it in the CredentialsGroupConfig specified by that
   * line.  getConfig will create new CredentialsGroupConfigs as necessary.
   */
  public List<CredentialsGroupConfig> getConfig(Reader in) throws IOException {
    CSVReader reader = new CSVReader(in);
    String[] nextLine;

    HashMap<String,CredentialsGroupConfig> cgcMap = new HashMap<String,CredentialsGroupConfig>();
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

      String cgcName = nextLine[0];
      if (!cgcMap.containsKey(cgcName)) {
        cgcMap.put(cgcName, new CredentialsGroupConfig(cgcName));
      }
      new IdentityElementConfig(authMech,
          "".equals(nextLine[4]) ? nextLine[1] + nextLine[2] : nextLine[4],
          cgcMap.get(cgcName));
    }

    return new ArrayList<CredentialsGroupConfig>(cgcMap.values());
  }

  public AuthNMechanism authNMechFromString(String mech) {
    try {
      return AuthNMechanism.valueOf(mech);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  // This is useful in unit testing.
  public static List<CredentialsGroupConfig> readConfigFile(String configFile) throws IOException {
    return (new CsvConfig(configFile)).getConfig();
  }

  public static List<CredentialsGroupConfig> readConfigFile(Reader in) throws IOException {
    return (new CsvConfig("AuthSites.conf")).getConfig(in);
  }
}
