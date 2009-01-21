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

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Unit test for CsvConfig.
 */
public class CsvConfigTest extends TestCase {

  public void testParser() throws IOException {
    String config = "groupA,http://leiz.mtv.corp.google.com,/basic/,BASIC_AUTH,\n" +
        "groupA,http://gama.corp.google.com,/secured/,FORMS_AUTH,\n" +
        "groupB,http://mooglegoogle.com,/moogle/,FORMS_AUTH,http://loginurl.com/login";
    File f = File.createTempFile("foo", null);
    f.deleteOnExit();
    writeConfigToFile(config, f);
    List<AuthnDomainGroup> adgs = CsvConfig.readConfigFile(f.getAbsolutePath());

    assertEquals(2, adgs.size());
    for (AuthnDomainGroup group : adgs) {
      if ("groupA".equals(group.getHumanName())) {
        assertEquals(2, group.getDomains().size());
        AuthnDomain leizBasicDomain = group.getDomains().get(0);
        assertEquals("http://leiz.mtv.corp.google.com/basic/", leizBasicDomain.getName());
        assertEquals(AuthNMechanism.BASIC_AUTH, leizBasicDomain.getMechanism());
        assertEquals("http://leiz.mtv.corp.google.com/basic/", leizBasicDomain.getLoginUrl());
        continue;
      }
      if ("groupB".equals(group.getHumanName())) {
        AuthnDomain moogleDomain = group.getDomains().get(0);
        assertEquals("http://mooglegoogle.com/moogle/", moogleDomain.getName());
        assertEquals("http://loginurl.com/login", moogleDomain.getLoginUrl());
        continue;
      }

      // should never reach here
      assertTrue(false);
    }
  }

  public void testParserInvalidInput() throws IOException {
    String config = "groupA,not_enough,parameters\n" +
        "groupB,http://www.yahoo.com,/securepage,WRONG_AUTHMETHOD,,\n" +
        "groupC,http://www.mooglegoogle.com,/moogle/,FORMS_AUTH,,";
    File f = new File("foo");
    f.deleteOnExit();
    writeConfigToFile(config, f);
    List<AuthnDomainGroup> adgs = CsvConfig.readConfigFile(f.getAbsolutePath());

    assertEquals(1, adgs.size());
    assertEquals("groupC",adgs.get(0).getHumanName());
    assertEquals(1, adgs.get(0).getDomains().size());
    AuthnDomain domain = adgs.get(0).getDomains().get(0);
    assertEquals("http://www.mooglegoogle.com/moogle/", domain.getName());
  }

  private void writeConfigToFile(String config, File f) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(f));
    writer.write(config);
    writer.close();
  }
}
