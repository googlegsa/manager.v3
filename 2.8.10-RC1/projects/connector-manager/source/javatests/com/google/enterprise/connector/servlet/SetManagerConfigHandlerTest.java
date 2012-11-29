// Copyright 2006 Google Inc. All Rights Reserved.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.logging.Logger;

/**
 * Tests SetManagerConfigHandler class for SetManagerConfig servlet class.
 */
public class SetManagerConfigHandlerTest extends TestCase {
  private static final Logger LOGGER =
    Logger.getLogger(SetManagerConfigHandlerTest.class.getName());

  private final MockManager manager = MockManager.getInstance();

  protected void setUp() {
    manager.setLocked(false);
  }

  public void testBasic() {
    doTest("10.32.20.102", 9941);
  }

  public void testEmptyHost() {
    doTest("", 9411);
  }

  /*
   * Test using old XML that includes CertAuthn Element.
   */
  public void testCertAuthnElement() {
    String xmlBody = "<ManagerConfig>"
        + "<CertAuthn>false</CertAuthn>"
        + "<FeederGate host=\"10.32.20.102\" port=\"9411\">a gate</FeederGate>"
        + "</ManagerConfig>";
    doTest("10.32.20.102", 9941);
  }

  public void testReregister() {
    doTest("this", 9411);
    doTest("this", 9411);
  }

  public void testManagerLockedHost() {
    doTest("this", 9411);
    doTest("that", 9411,
        ConnectorMessageCode.ATTEMPT_TO_CHANGE_LOCKED_CONNECTOR_MANAGER);
  }

  public void testManagerLockedPort() {
    doTest("this", 9411);
    doTest("this", 8300,
        ConnectorMessageCode.ATTEMPT_TO_CHANGE_LOCKED_CONNECTOR_MANAGER);
  }

  public void testAddSecurePort() {
    doTest("this", 9411);
    doTest("", "this", 9411, 9412,
        ConnectorMessageCode.ATTEMPT_TO_CHANGE_LOCKED_CONNECTOR_MANAGER);
  }

  public void testAddProtocol() {
    doTest("this", 9411);
    doTest("http", "this", 9411, -1,
        ConnectorMessageCode.ATTEMPT_TO_CHANGE_LOCKED_CONNECTOR_MANAGER);
  }

  /** It's OK if a request doesn't specify a value for an existing property. */
  public void testSkipSecurePort() {
    doTest("", "this", 9411, 9412);
    doTest("this", 9411);
  }

  /** It's OK if a request doesn't specify a value for an existing property. */
  public void testSkipProtocol() {
    doTest("http", "this", 9411, -1);
    doTest("this", 9411);
  }

  private void doTest(String host, int port) {
    doTest(host, port, ConnectorMessageCode.SUCCESS);
  }

  private void doTest(String protocol, String host, int port, int securePort) {
    doTest(protocol, host, port, securePort, ConnectorMessageCode.SUCCESS);
  }

  private void doTest(String host, int port, int messageId) {
    doTest("", host, port, -1, messageId);
  }

  private void doTest(String protocol, String host, int port, int securePort,
      int messageId) {
    String xmlBody = setXMLBody(protocol, host, port, securePort);
  LOGGER.info("xmlBody: " + xmlBody);
    SetManagerConfigHandler hdl =
      new SetManagerConfigHandler(manager, xmlBody);
    assertEquals(messageId, hdl.getStatus().getMessageId());
    assertEquals(protocol, hdl.getFeederGateProtocol());
    assertEquals(host, hdl.getFeederGateHost());
    assertEquals(port, hdl.getFeederGatePort());
    assertEquals(securePort, hdl.getFeederGateSecurePort());
  }

  public String setXMLBody(String protocol, String host, int port,
      int securePort) {
    return "<" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">\n"
        + "  <FeederGate host=\"" + host + "\" port=\"" + port + "\" "
        + ((Strings.isNullOrEmpty(protocol))
            ? "" : "protocol=\"" + protocol + "\" ")
        + ((securePort < 0) ? "" : "securePort=\"" + securePort + "\" ")
        + "/>\n"
        + "</" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">";
  }
}
