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

import junit.framework.TestCase;

/**
 * Test ConnectorManagerServletResponse
 *
 */
public class ConnectorManagerServletResponseTest extends TestCase {

  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerServletResponseTest.class.getName());

  /**
   * Test constructor of ConnectorMessageCode
   * i.e. messageId = ConnectorMessageCode.SUCCESS
   *
   */
  public void testGetResponseStatus1() {
    ConnectorMessageCode expected = new ConnectorMessageCode();
    connectorMessageTest(expected);
  }

  /**
   * Test the constructor of ConnectorMessageCode with messageId
   *
   */
  public void testGetResponseStatus2() {
    int messageId = ConnectorMessageCode.RESPONSE_EMPTY_RESPONSE;
    ConnectorMessageCode expected = new ConnectorMessageCode(messageId);
    connectorMessageTest(expected);
  }

  /**
   * Test the constructor of ConnectorMessageCode with full status:
   * (messageId, message, params)
   *
   */
  public void testGetResponseStatus3() {
    int messageId = ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS;
    String message = "Connector {0} exists.";
    String[] params = {"connectorA"};
    ConnectorMessageCode expected = new ConnectorMessageCode(
        messageId, message, params);
    connectorMessageTest(expected);
  }

  private void connectorMessageTest(ConnectorMessageCode expected) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    ServletUtil.writeResponse(out, expected);
    out.close();
    String xmlResponse = writer.getBuffer().toString();
    LOGGER.info("xmlResponse: " + xmlResponse);
    ConnectorManagerServletResponse cmResponse =
        new ConnectorManagerServletResponse(xmlResponse);
    ConnectorMessageCode cmMessage = cmResponse.getCmMessageCode();
    assertEquals(cmMessage.getMessageId(), expected.getMessageId());
    assertEquals(cmMessage.getMessage(), expected.getMessage());
    if (cmMessage.getParams() == null) {
      assertTrue(expected.getParams() == null);
      return;
    }
    if (expected.getParams() == null) {
      assertTrue(cmMessage.getParams() == null);
      return;
    }
    assertEquals(cmMessage.getParams().length, expected.getParams().length);
    for (int i = 0; i < cmMessage.getParams().length; ++i) {
      assertTrue(cmMessage.getParams()[i].equals(expected.getParams()[i]));
    }
  }

}
