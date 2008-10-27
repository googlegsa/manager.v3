// Copyright (C) 2008 Google Inc.
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

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Unit test for SamlAuthz handler.
 */
public class SamlAuthzTest extends TestCase {

  private static final String REQUEST_CONTENT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<soapenv:Envelope\n" +
          "  xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
          "  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
          "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
          "  <soapenv:Body>\n" +
          "\n" +
          "    <samlp:AuthzDecisionQuery\n" +
          "      ID=\"kmigpcackfenaibdninipcnmkmajfplommhfapbk\"\n" +
          "      IssueInstant=\"2004-10-20T17:52:29Z\"\n" +
          "      Version=\"2.0\"\n" +
          "      Resource=\"http://www.abc.com/secret.html\"\n" +
          "      xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
          "      xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
          "      <saml:Subject>\n" +
          "        <saml:NameID>Joe Bob</saml:NameID>\n" +
          "      </saml:Subject>\n" +
          "      <saml:Action\n" +
          "        Namespace=\"urn:oasis:names:tc:SAML:1.0:action:ghpp\">\n" +
          "        GET\n" +
          "      </saml:Action>\n" +
          "    </samlp:AuthzDecisionQuery>\n" +
          "    <samlp:AuthzDecisionQuery\n" +
          "      ID=\"thisidisntsorandom\"\n" +
          "      IssueInstant=\"2004-10-20T17:52:29Z\"\n" +
          "      Version=\"2.0\"\n" +
          "      Resource=\"http://www.cba.com/treces.html\"\n" +
          "      xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
          "      xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
          "      <saml:Subject>\n" +
          "        <saml:NameID>Joe Bob</saml:NameID>\n" +
          "      </saml:Subject>\n" +
          "      <saml:Action\n" +
          "        Namespace=\"urn:oasis:names:tc:SAML:1.0:action:ghpp\">\n" +
          "        GET\n" +
          "      </saml:Action>\n" +
          "    </samlp:AuthzDecisionQuery>\n" +
          "\n" +
          "  </soapenv:Body>\n" +
          "</soapenv:Envelope>";

  private SamlAuthz samlAuthzInstance;

  public void setUp() {
    samlAuthzInstance = new SamlAuthz();
  }


  /**
   * This test currently doesn't do much more than execute the authz handler
   * logic and spew the response output.  At the very least, it will catch
   * any unexpected (or expected) exceptions if any part of the logic is
   * incorrect.
   *
   * @throws UnsupportedEncodingException
   */
  public void testBasic() throws UnsupportedEncodingException {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse resp = new MockHttpServletResponse();

    req.setContent(REQUEST_CONTENT.getBytes());
    req.setContentType("text/xml");
    System.out.println("Content length is? " + req.getContentLength());

    try {
      samlAuthzInstance.handleDoPost(req, resp);
      System.out.println("done");
    } catch (IOException e) {
      assertTrue(false);
    }

    System.out.println("content: \n" + resp.getContentAsString());

  }
}
