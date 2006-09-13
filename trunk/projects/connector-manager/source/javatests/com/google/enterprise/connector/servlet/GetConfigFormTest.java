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

import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Tests GetConfigFormTest servlet class.
 *
 */
public class GetConfigFormTest extends TestCase {
  private static final Logger logger = Logger
  .getLogger(GetConfigFormTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.GetConfigForm#handleDoGet(java.io.PrintWriter, com.google.enterprise.connector.spi.ConfigureResponse)}.
   */
  public void testHandleDoGet1() {
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "  <ConfigureResponse>null</ConfigureResponse>\n" +
      "</CmResponse>\n";
    ConfigureResponse configResponse = null;
    doTest(configResponse, expectedResult);
  }

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.GetConfigForm#handleDoGet(java.io.PrintWriter, com.google.enterprise.connector.spi.ConfigureResponse)}.
   */
  public void testHandleDoGet2() {
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "  <ConfigureResponse>\n" +
      "    <message>null</message>\n" +
      "    <FormSnippet>null</FormSnippet>\n" +
      "  </ConfigureResponse>\n" +
      "</CmResponse>\n";
    String message = null;
    String formSnippet = null;
    ConfigureResponse configResponse = new ConfigureResponse(message, formSnippet);
    doTest(configResponse, expectedResult);
  }

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.GetConfigForm#handleDoGet(java.io.PrintWriter, com.google.enterprise.connector.spi.ConfigureResponse)}.
   */
  public void testHandleDoGet3() {
    String message = "Sample form";
    String formSnippet =
      "    <tr><td>Repository</td>" +
      "      <td><input type=\"text\" name=\"repository\" value=\"\"></td>" +
      "    </tr>" +
      "    <tr><td>Username</td>" +
      "      <td><input type=\"text\" name=\"username\" value=\"\">" +
      "      </td></tr>" +
      "    <tr><td>Password</td>" +
      "      <td><input type=\"password\" name=\"passwd\" value=\"\">" +
      "    </td></tr>" +
      "    <tr><td>Seed URIs</td>" + 
      "      <td><textarea name=\"seedUris\"></textarea></td></tr>";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "  <ConfigureResponse>\n" +
      "    <message>" + message + "</message>\n" +
      "    <FormSnippet>" + formSnippet + "</FormSnippet>\n" +
      "  </ConfigureResponse>\n" +
      "</CmResponse>\n";      
    ConfigureResponse configResponse = new ConfigureResponse(message, formSnippet);
    doTest(configResponse, expectedResult);
  }

  private void doTest(ConfigureResponse configResponse, String expectedResult) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConfigForm.handleDoGet(out, configResponse);
    out.flush();
    StringBuffer result = writer.getBuffer();
    logger.info(result.toString());
    logger.info(expectedResult);
    Assert.assertEquals(ConnectorTestUtils.normalizeNewlines(expectedResult), 
        ConnectorTestUtils.normalizeNewlines(result.toString()));
  }
}
