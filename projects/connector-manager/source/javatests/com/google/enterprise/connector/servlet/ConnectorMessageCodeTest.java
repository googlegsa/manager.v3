// Copyright 2009 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.servlet;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Tests the {@link ConnectorMessageCode} class.
 */
public class ConnectorMessageCodeTest extends TestCase {
  private static final String MESSAGE_STRING = "foo exists";
  private static final String MESSAGE_STRING_PAR = "Node %s is empty";
  private static final String NEW_MESSAGE = "This is a new message";
  private static final Object[] PARAMS1 = {"foo"};
  private static final Object[] PARAMS2 = {"foo", "bar"};
  private static final Object[] NEW_PARAMS = {"new foo"};

  private ConnectorMessageCode defaultCode;
  private ConnectorMessageCode successCode;
  private ConnectorMessageCode messageIdCode;
  private ConnectorMessageCode messageIdParamCode;
  private ConnectorMessageCode messageIdParam2Code;
  private ConnectorMessageCode messageIdStringCode;
  private ConnectorMessageCode messageIdStringParamCode;
  private ConnectorMessageCode successIdCode;
  private ConnectorMessageCode successIdParamCode;
  private ConnectorMessageCode aboveSuccessIdCode;

  @Override
  protected void setUp() throws Exception {
    // Create representative set of messages using the various constructors.
    defaultCode = new ConnectorMessageCode();
    successCode = new ConnectorMessageCode(ConnectorMessageCode.SUCCESS);
    messageIdCode = new ConnectorMessageCode(
        ConnectorMessageCode.CONNECTOR_DISCONNECTED);
    messageIdParamCode = new ConnectorMessageCode(
        ConnectorMessageCode.INVALID_CONNECTOR_MANAGER_NAME, PARAMS1);
    messageIdParam2Code = new ConnectorMessageCode(
        ConnectorMessageCode.MISMATCHED_CONNECTOR_MANAGER, PARAMS2);
    messageIdStringCode = new ConnectorMessageCode(
        ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS, MESSAGE_STRING);
    messageIdStringParamCode = new ConnectorMessageCode(
        ConnectorMessageCode.RESPONSE_EMPTY_NODE, MESSAGE_STRING_PAR, PARAMS1);
    successIdCode = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL);
    successIdParamCode = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL, PARAMS1);
    aboveSuccessIdCode = new ConnectorMessageCode(ConnectorMessageCode.UNSUPPORTED_CALL);
  }

  @Override
  protected void tearDown() throws Exception {
    defaultCode = null;
    successCode = null;
    messageIdCode = null;
    messageIdParamCode = null;
    messageIdParam2Code = null;
    messageIdStringCode = null;
    messageIdStringParamCode = null;
    successIdCode = null;
    successIdParamCode = null;
  }

  private void assertEmpty(Object[] params) {
    assertNotNull(params);
    assertEquals(0, params.length);
  }

  private void assertEqualParams(String message, Object[] expected,
      Object[] params) {
    assertEquals(message, Arrays.toString(expected), Arrays.toString(params));
  }

  /**
   * Test method for various constructors and related methods.
   */
  public void testConstructors() {
    assertTrue("defaultCode.isSuccess()", defaultCode.isSuccess());
    assertFalse("defaultCode.hasMessage()", defaultCode.hasMessage());
    assertEquals("defaultCode.getMessageId()",
        ConnectorMessageCode.SUCCESS, defaultCode.getMessageId());

    assertTrue("successCode.isSuccess()", successCode.isSuccess());
    assertFalse("successCode.hasMessage()", successCode.hasMessage());
    assertEquals("successCode.getMessageId()",
        ConnectorMessageCode.SUCCESS, successCode.getMessageId());

    assertFalse("messageIdCode.isSuccess()", messageIdCode.isSuccess());
    assertFalse("messageIdCode.hasMessage()", messageIdCode.hasMessage());
    assertEquals("messageIdCode.getMessageId()",
        ConnectorMessageCode.CONNECTOR_DISCONNECTED,
        messageIdCode.getMessageId());

    assertFalse("messageIdParamCode.isSuccess()",
        messageIdParamCode.isSuccess());
    assertFalse("messageIdParamCode.hasMessage()",
        messageIdParamCode.hasMessage());
    assertEquals("messageIdParamCode.getMessageId()",
        ConnectorMessageCode.INVALID_CONNECTOR_MANAGER_NAME,
        messageIdParamCode.getMessageId());
    assertEqualParams("messageIdParamCode.getParams()", PARAMS1,
        messageIdParamCode.getParams());

    assertFalse("messageIdParam2Code.isSuccess()",
        messageIdParam2Code.isSuccess());
    assertFalse("messageIdParam2Code.hasMessage()",
        messageIdParam2Code.hasMessage());
    assertEquals("messageIdParam2Code.getMessageId()",
        ConnectorMessageCode.MISMATCHED_CONNECTOR_MANAGER,
        messageIdParam2Code.getMessageId());
    assertEqualParams("messageIdParam2Code.getParams()",
        PARAMS2, messageIdParam2Code.getParams());

    assertFalse("messageIdStringCode.isSuccess()",
        messageIdStringCode.isSuccess());
    assertFalse("messageIdStringCode.hasMessage()",
        messageIdStringCode.hasMessage());
    assertEquals("messageIdStringCode.getMessageId()",
        ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS,
        messageIdStringCode.getMessageId());
    Object[] expected = {MESSAGE_STRING};
    assertTrue("messageIdStringCode.getParams()",
        Arrays.equals(expected, messageIdStringCode.getParams()));

    assertFalse("messageIdStringParamCode.isSuccess()",
        messageIdStringParamCode.isSuccess());
    assertTrue("messageIdStringParamCode.hasMessage()",
        messageIdStringParamCode.hasMessage());
    assertEquals("messageIdStringParamCode.getMessageId()",
        ConnectorMessageCode.RESPONSE_EMPTY_NODE,
        messageIdStringParamCode.getMessageId());
    assertEquals("messageIdStringParamCode.getMessage()",
        MESSAGE_STRING_PAR,
        messageIdStringParamCode.getMessage());
    assertEqualParams("messageIdStringParamCode.getParams()", PARAMS1,
        messageIdStringParamCode.getParams());

    assertFalse("successIdCode.isSuccess()", successIdCode.isSuccess());
    assertFalse("successIdCode.hasMessage()", successIdCode.hasMessage());
    assertEquals("successIdCode.getMessageId()",
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL,
        successIdCode.getMessageId());
    assertTrue("successIdCode.isSuccessMessage()",
        successIdCode.isSuccessMessage());

    assertFalse("successIdParamCode.isSuccess()",
        successIdParamCode.isSuccess());
    assertFalse("successIdParamCode.hasMessage()",
        successIdParamCode.hasMessage());
    assertEquals("successIdParamCode.getMessageId()",
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL,
        successIdParamCode.getMessageId());
    assertTrue("successIdParamCode.isSuccessMessage()",
        successIdParamCode.isSuccessMessage());
    assertEqualParams("successIdParamCode.getParams()", PARAMS1,
        successIdParamCode.getParams());
  }

  public void testSetMessageId() {
    String message = messageIdStringParamCode.getMessage();
    Object[] params = messageIdStringParamCode.getParams();
    messageIdStringParamCode.setMessageId(
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL);
    assertEquals(message, messageIdStringParamCode.getMessage());
    assertEqualParams("messageIdStringParamCode.getParams()", params,
        messageIdStringParamCode.getParams());

    messageIdStringParamCode.setMessageId(ConnectorMessageCode.SUCCESS);
    assertEquals(null, messageIdStringParamCode.getMessage());
    assertEqualParams("messageIdStringParamCode.getParams()", new Object[0],
        messageIdStringParamCode.getParams());
  }

  public void testSetMessage() {
    // Success code is not suppose to report a message even if one present since
    // that is also a trigger for error.
    successCode.setMessage(NEW_MESSAGE);
    assertFalse("successCode.hasMessage()", successCode.hasMessage());
    assertEquals("successCode.getMessage()",
        NEW_MESSAGE, successCode.getMessage());

    // Change the message.
    messageIdStringParamCode.setMessage(NEW_MESSAGE);
    assertTrue("messageIdStringParamCode.hasMessage()",
        messageIdStringParamCode.hasMessage());
    assertEquals("messageIdStringParamCode.getMessage()",
        NEW_MESSAGE, messageIdStringParamCode.getMessage());

    // Set a message on a success message - not the success code.
    successIdCode.setMessage(NEW_MESSAGE);
    assertTrue("successIdCode.hasMessage()", successIdCode.hasMessage());
    assertEquals("successIdCode.getMessageId()",
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL,
        successIdCode.getMessageId());
    assertTrue("successIdCode.isSuccessMessage()",
        successIdCode.isSuccessMessage());
  }

  public void testSetParams() {
    // Add params to a success code.
    successCode.setParams(NEW_PARAMS);
    assertTrue("successCode.isSuccess()", successCode.isSuccess());
    assertEqualParams("successCode.getParams()",
        NEW_PARAMS, successCode.getParams());

    // Change params on a error message.
    messageIdParamCode.setParams(NEW_PARAMS);
    assertEqualParams("messageIdParamCode.getParams()",
        NEW_PARAMS, messageIdParamCode.getParams());

    // Change params on a success message.
    successIdParamCode.setParams(NEW_PARAMS);
    assertEqualParams("successIdParamCode.getParams()",
        NEW_PARAMS, successIdParamCode.getParams());
  }

  private void testNullOrEmptyParams(Object[] params) {
    ConnectorMessageCode code;
    code = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS, params);
    assertEmpty(code.getParams());

    code = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS, null, params);
    assertEmpty(code.getParams());

    code = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS, MESSAGE_STRING, params);
    assertEmpty(code.getParams());
  }

  public void testNullParams() {
    assertEmpty(defaultCode.getParams());
    assertEmpty(successCode.getParams());
    assertEmpty(messageIdCode.getParams());
    testNullOrEmptyParams(null);
  }

  public void testEmptyParams() {
    testNullOrEmptyParams(new Object[0]);
  }

  public void testIsSuccessMessage() {
    assertFalse("successIdCode.isSuccessMessage()",
        successCode.isSuccessMessage());
    assertFalse("successIdCode.isSuccessMessage()",
        messageIdCode.isSuccessMessage());
    assertTrue("successIdCode.isSuccessMessage()",
        successIdCode.isSuccessMessage());
    assertFalse("successIdCode.isSuccessMessage()",
        aboveSuccessIdCode.isSuccessMessage());
  }

  public void testHasMessage() {
    ConnectorMessageCode code;

    assertFalse(successCode.hasMessage());

    code = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS, MESSAGE_STRING);
    assertFalse(code.hasMessage());

    code = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL);
    assertFalse(code.hasMessage());

    code = new ConnectorMessageCode(
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL, "", null);
    assertFalse(code.hasMessage());

    assertTrue(messageIdStringParamCode.hasMessage());
  }
}
