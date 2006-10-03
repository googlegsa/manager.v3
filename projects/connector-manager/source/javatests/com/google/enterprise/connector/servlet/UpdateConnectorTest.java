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


/**
 * Tests the UpdateConnector servlet class
 *
 */
public class UpdateConnectorTest extends TestCase {

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.
   * UpdateConnector#handleDoGet(com.google.enterprise.connector.manager.Manager,
   *  java.lang.String, java.lang.String, java.lang.String)}.
   */
  public void testHandleDoGet() {
    String connectorName = "cname1";
	String xmlBody = 
        "<ConnectorConfig>\n"
        + "<ConnectorName>" + connectorName + "</ConnectorName>\n"
        + "<ConnectorType>ctype1</ConnectorType>\n"
        + "<config>\n"
        + "<Param name=\"Username\" value=\"fooUser\"/>\n"
        + "<Param name=\"Password\" value=\"fooPassword\"/>\n"
        + "<Param name=\"Repository File\" value=\"Foo Repository\"/>\n"
        + "</config>\n"
        + "</ConnectorConfig>\n";
	String expectedResult =
      "<HTML><HEAD><TITLE>Update Connector Config</TITLE></HEAD>\n" + 
      "<BODY><H3>Update Connector Config:</H3><HR>\n" + 
      "<FORM METHOD=POST ACTION=\"/connector-manager/updateConnector?ConnectorName=cname1&Lang=en\"><TABLE><tr><td>Connector Name: cname1</td></tr><tr>\n" + 
      "<tr>\n" + 
      "<td>Username</td>\n" + 
      "<td>\n" + 
      "<input type=\"text\" name=\"Username\" value=\"fooUser\" />\n" + 
      "</td>\n" + 
      "</tr>\n" + 
      "<tr>\n" + 
      "<td>Password</td>\n" + 
      "<td>\n" + 
      "<input type=\"password\" name=\"Password\" value=\"fooPassword\" />\n" + 
      "</td>\n" + 
      "</tr>\n" + 
      "<tr>\n" + 
      "<td>Color</td>\n" + 
      "<td>\n" + 
      "<input type=\"text\" name=\"Color\" />\n" + 
      "</td>\n" + 
      "</tr>\n" + 
      "<tr>\n" + 
      "<td>Repository File</td>\n" + 
      "<td>\n" + 
      "<input type=\"text\" name=\"Repository File\" value=\"Foo Repository\" />\n" + 
      "</td>\n" + 
      "</tr>\n" + 
      "<tr><td><INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE=\"submit\"></td></tr></TABLE></FORM></BODY></HTML>\n";

    Manager manager = MockManager.getInstance();
    String result = UpdateConnector.handleDoGet(manager, xmlBody, connectorName, "en");
    Assert.assertEquals(StringUtils.normalizeNewlines(expectedResult), 
      StringUtils.normalizeNewlines(result.toString()));
  }

}
