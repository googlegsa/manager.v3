// Copyright 2008 Google Inc.
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

package com.google.enterprise.connector.saml.server;

import static com.google.enterprise.connector.common.ServletTestUtil.makeMockHttpPost;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeStatus;

import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.saml.common.Metadata;

import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.io.MarshallingException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import java.io.IOException;

/**
 * Unit test for SamlArtifactResolve handler.
 */
public class SamlArtifactResolveTest extends SecurityManagerTestCase {

  private Metadata metadata;
  private SamlArtifactResolve samlArtifactResolveInstance;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    metadata = Metadata.class.cast(
        Context.getInstance().getRequiredBean("Metadata", Metadata.class));
    samlArtifactResolveInstance = new SamlArtifactResolve();
    samlArtifactResolveInstance.init(new MockServletConfig());
  }

  /**
   * At the moment this test just makes sure the post handler codepath executes
   * without hitting an exception and returns non-empty content.
   * @throws MarshallingException
   */
  public void testPostHandler()
      throws IOException, MarshallingException {
    MockHttpServletRequest mockRequest = makeMockHttpPost(null, "http://localhost/");
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    String encodedArtifact = "AAQAACFRlGU7Pe4QCIfrpMEtVVuJSKUCzJE+6GPdLFM4AjN18B06VmSmJgs=";
    String entity =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
        "  <soap11:Body>\n" +
        "    <samlp:ArtifactResolve ID=\"_649840c6ad89709ca2a8c45e173ff141\"\n" +
        "                           IssueInstant=\"2008-11-10T08:22:11.339Z\"\n" +
        "                           Version=\"2.0\"\n" +
        "                           xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
        "      <saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" +
        GSA_TESTING_ISSUER +
        "</saml:Issuer>\n" +
        "      <samlp:Artifact>" + encodedArtifact + "</samlp:Artifact>\n" +
        "    </samlp:ArtifactResolve>\n" +
        "  </soap11:Body>\n" +
        "</soap11:Envelope>\n";
    mockRequest.setContent(entity.getBytes("UTF-8"));

    BackEnd backend = 
      BackEnd.class.cast(Context.getInstance().getRequiredBean("BackEnd", BackEnd.class));
    backend.getArtifactMap().put(
        encodedArtifact,
        GSA_TESTING_ISSUER,
        metadata.getSmEntity().getEntityID(),
        makeResponse(null, makeStatus(StatusCode.SUCCESS_URI)));

    samlArtifactResolveInstance.doPost(mockRequest, mockResponse);

    String returnedContent = mockResponse.getContentAsString();
    System.out.println("content: \n" + returnedContent);

    /** make sure we got something back */
    assertTrue(returnedContent.length() > 0);
  }
}
