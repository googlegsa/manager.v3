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


import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.logging.Logger;

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;

/**
 * Tests SetManagerConfigHandler class for SetManagerConfig servlet class.
 *
 */
public class SetManagerConfigHandlerTest extends TestCase {
  private static final Logger LOGGER =
    Logger.getLogger(SetManagerConfigHandlerTest.class.getName());
  private String host;
  private int port;

  public void testHandleDoPost1() {
    host = "10.32.20.102";
    port = 9411;
    doTest(setXMLBody());
  }

  public void testHandleDoPost2() {
    host = "";
    port = 9411;
    doTest(setXMLBody());
  }

  /*
   * Test using old XML that includes CertAuthn Element.
   */
  public void testHandleDoPost3() {
    host = "10.32.20.102";
    port = 9411;
    String xmlBody =
        "<ManagerConfig>" +
        "  <CertAuthn>false</CertAuthn>" +
        "  <FeederGate host=\"10.32.20.102\" port=\"9411\">a gate</FeederGate>" +
        "</ManagerConfig>";
    doTest(xmlBody);
  }

  private void doTest(String xmlBody) {
  LOGGER.info("xmlBody: " + xmlBody);
    Manager manager = MockManager.getInstance();
    SetManagerConfigHandler hdl =
      new SetManagerConfigHandler(manager, xmlBody);
    LOGGER.info("host: " + hdl.getFeederGateHost() + " " + this.host);
    LOGGER.info("Port: " + hdl.getFeederGatePort());
    Assert.assertEquals(this.host, hdl.getFeederGateHost());
    Assert.assertEquals(this.port, hdl.getFeederGatePort());
  }


public String setXMLBody() {
  return
    "<" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">\n" +
      "  <FeederGate host=\"" + this.host + "\" port=\""+ this.port + "\"/>\n" +
      "</" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">";
  }
}