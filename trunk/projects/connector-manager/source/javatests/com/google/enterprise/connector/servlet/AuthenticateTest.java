// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Tests the AuthenticateTest servlet class
 *
 */
public class AuthenticateTest extends TestCase {
  private static final Logger LOG =
    Logger.getLogger(AuthenticateTest.class.getName());

  /**
   * Test method for 
   * {@link com.google.enterprise.connector.servlet.Authenticate#handleDoPost(
   * java.io.PrintWriter, java.lang.String,
   * com.google.enterprise.connector.manager.Manager)}.
   */
  public void testHandleDoPost() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <username>fooUser</username>\n" + 
      "    <password>fooPassword</password>\n" + 
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" + 
      "  <AuthnResponse>\n" + 
      "    <Success ConnectorName=\"connector1\">\n" + 
      "      <Identity>fooUser</Identity>\n" + 
      "    </Success>\n" + 
      "    <Success ConnectorName=\"connector2\">\n" + 
      "      <Identity>fooUser</Identity>\n" + 
      "    </Success>\n" + 
      "  </AuthnResponse>\n" + 
      "</CmResponse>\n"; 
    doTest(xmlBody, expectedResult);
  }

  private void doTest(String xmlBody, String expectedResult) {
    LOG.info("xmlBody: " + xmlBody);
    Manager manager = MockManager.getInstance();
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    Authenticate.handleDoPost(out, xmlBody, manager);
    out.flush();
    StringBuffer result = writer.getBuffer();
    LOG.info(result.toString());
    LOG.info(expectedResult);
    Assert.assertEquals (StringUtils.normalizeNewlines(expectedResult), 
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }
}
