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

import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * Unit test for CsvConfig.
 */
public class CsvConfigTest extends SecurityManagerTestCase {

  public void testParser() throws IOException {
    String config = "groupA,http://leiz.mtv.corp.google.com,/basic/,BASIC_AUTH,\n" +
        "groupA,http://gama.corp.google.com,/secured/,FORMS_AUTH,\n" +
        "groupB,http://mooglegoogle.com,/moogle/,FORMS_AUTH,http://loginurl.com/login\n";
    List<CredentialsGroupConfig> groupConfigs = CsvConfig.readConfigFile(new StringReader(config));

    assertEquals(2, groupConfigs.size());
    for (CredentialsGroupConfig group : groupConfigs) {
      if ("groupA".equals(group.getHumanName())) {
        assertEquals(2, group.getElements().size());
        IdentityElementConfig leizBasicDomain = group.getElements().get(0);
        assertEquals(AuthNMechanism.BASIC_AUTH, leizBasicDomain.getMechanism());
        assertEquals("http://leiz.mtv.corp.google.com/basic/", leizBasicDomain.getSampleUrl());
        continue;
      }
      if ("groupB".equals(group.getHumanName())) {
        IdentityElementConfig moogleDomain = group.getElements().get(0);
        assertEquals("http://loginurl.com/login", moogleDomain.getSampleUrl());
        continue;
      }

      // should never reach here
      assertTrue(false);
    }
  }

  public void testParserInvalidInput() throws IOException {
    String config = "groupA,not_enough,parameters\n" +
        "groupB,http://www.yahoo.com,/securepage,WRONG_AUTHMETHOD,,\n" +
        "groupC,http://www.mooglegoogle.com,/moogle/,FORMS_AUTH,,\n";
    List<CredentialsGroupConfig> groupConfigs = CsvConfig.readConfigFile(new StringReader(config));

    assertEquals(1, groupConfigs.size());
    assertEquals("groupC",groupConfigs.get(0).getHumanName());
    assertEquals(1, groupConfigs.get(0).getElements().size());
  }
}
