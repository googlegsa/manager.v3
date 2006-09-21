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

/**
 * Tests SetManagerConfigHandler class for SetManagerConfig servlet class.
 *
 */
public class SetManagerConfigHandlerTest extends TestCase {
  private static final Logger LOG =
	  Logger.getLogger(SetManagerConfigHandlerTest.class.getName());
  private boolean certAuth;
  private String host;
  private int port;
  private int maxFeedRate;
  
  public void testHandleDoPost1() {
    certAuth = true;
    host = "10.32.20.102";
    port = 9411;
    maxFeedRate = 20;
    doTest(setXMLBody());
  }

  public void testHandleDoPost2() {
    certAuth = false;
    host = "";
    port = 9411;
    maxFeedRate = 20;
    doTest(setXMLBody());
  }

  public void testHandleDoPost3() {
    certAuth = false;
    host = "10.32.20.102";
    port = 9411;
    maxFeedRate = 20;
    String xmlBody =
        "<ManagerConfig>" +
        "  <CertAuthn>false</CertAuthn>" +
        "  <FeederGate host=\"10.32.20.102\" port=\"9411\">a gate</FeederGate>" +
        "  <MaxFeedRate>20</MaxFeedRate>" +
        "</ManagerConfig>";
    doTest(xmlBody);
  }

  private void doTest(String xmlBody) {
	  LOG.info("xmlBody: " + xmlBody);
    SetManagerConfigHandler hdl = new SetManagerConfigHandler(xmlBody);
    LOG.info("authn: " + hdl.isCertAuth() + " this: " + this.certAuth);
    LOG.info("host: " + hdl.getFeederGateHost() + " " + this.host);
    LOG.info("Port: " + hdl.getFeederGatePort());
    LOG.info("rate: " + hdl.getMaxFeedRate());
    Assert.assertEquals(hdl.isCertAuth(), this.certAuth);
    Assert.assertEquals(hdl.getFeederGateHost(), this.host);
    Assert.assertEquals(hdl.getFeederGatePort(), this.port);
    Assert.assertEquals(hdl.getMaxFeedRate(), this.maxFeedRate);
  }


public String setXMLBody() {
  return
	  "<" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">\n" +
      "  <CertAuthn>" + this.certAuth + "</CertAuthn>\n" +
      "  <FeederGate host=\"" + this.host + "\" port=\""+ this.port + "\"/>\n" +
      "  <MaxFeedRate>" + this.maxFeedRate + "</MaxFeedRate>\n" +
      "</" + ServletUtil.XMLTAG_MANAGER_CONFIG + ">";
  }
}