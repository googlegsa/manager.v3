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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests servlet GetConnectorConfigToEdit
 *
 */
public class GetConnectorConfigToEditTest extends TestCase {
  private static final Logger LOG =
      Logger.getLogger(GetConnectorConfigToEditTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.
   * GetConnectorConfigToEdit#handleDoGet(java.io.PrintWriter,
   * com.google.enterprise.connector.manager.Manager,
   * java.lang.String, java.lang.String)}.
   */
  public void testHandleDoGet() {
    String expectedResult =
        "<CmResponse>\n" +
        "  <StatusId>0</StatusId>\n" +
        "  <ConfigureResponse>\n" +
        "    <FormSnippet><![CDATA[<tr>\n" +
        "<td>Username</td>\n" + 
        "<td>\n" + 
        "<input type=\"text\" name=\"CM_Username\" />\n" + 
        "</td>\n" + 
        "</tr>\n" + 
        "<tr>\n" + 
        "<td>Password</td>\n" + 
        "<td>\n" + 
        "<input type=\"password\" name=\"CM_Password\" />\n" + 
        "</td>\n" + 
        "</tr>\n" + 
        "<tr>\n" + 
        "<td>Color</td>\n" + 
        "<td>\n" + 
        "<input type=\"text\" name=\"CM_Color\" />\n" + 
        "</td>\n" + 
        "</tr>\n" + 
        "<tr>\n" + 
        "<td>Repository File</td>\n" + 
        "<td>\n" + 
        "<input type=\"text\" name=\"CM_Repository File\" />\n" + 
        "</td>\n" + 
        "</tr>\n" + 
        "]]></FormSnippet>\n" +
        "    <message>Sample form for connectorAlang en</message>\n" +
        "  </ConfigureResponse>\n" + 
        "</CmResponse>\n";
    String connectorName = "connectorA";
    Manager manager = MockManager.getInstance();
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorConfigToEdit.handleDoGet(connectorName, "en", manager, out);
    StringBuffer result = writer.getBuffer();
    LOG.info(result.toString());
    LOG.info(expectedResult);
    out.flush();
    Assert.assertEquals (StringUtils.normalizeNewlines(expectedResult), 
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }

}
