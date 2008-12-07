// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.saml.server;

import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.client.MockArtifactConsumer;
import com.google.enterprise.saml.client.MockServiceProvider;
import com.google.enterprise.saml.client.MockUserAgent;
import com.google.enterprise.saml.common.GsaConstants;
import com.google.enterprise.saml.common.MockHttpTransport;

import junit.framework.TestCase;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertionConsumerService;
import static com.google.enterprise.saml.common.SamlTestUtil.generatePostContent;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpGet;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class SamlSsoTest extends TestCase {

  private static final String spUrl = "http://localhost:8973/search";

  private final MockUserAgent userAgent;

  public SamlSsoTest(String name) throws ServletException {
    super(name);

    Context ctx = Context.getInstance();
    ctx.setStandaloneContext("source/webdocs/test/applicationContext.xml",
                             Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    BackEnd backend = ((ConnectorManager) ctx.getManager()).getBackEnd();

    // Initialize transport
    MockHttpTransport transport = new MockHttpTransport();
    userAgent = new MockUserAgent(transport);

    EntityDescriptor gsaEntity = backend.getGsaEntity();
    SPSSODescriptor sp = gsaEntity.getSPSSODescriptor(SAML20P_NS);
    makeAssertionConsumerService(sp, SAML2_ARTIFACT_BINDING_URI,
        "http://localhost:8973/" + GsaConstants.GSA_ARTIFACT_HANDLER_NAME).setIsDefault(true);
    transport.registerServlet(sp.getDefaultAssertionConsumerService(),
                              new MockArtifactConsumer(transport));

    EntityDescriptor smEntity = backend.getSecurityManagerEntity();
    IDPSSODescriptor idp = smEntity.getIDPSSODescriptor(SAML20P_NS);
    transport.registerServlet(idp.getSingleSignOnServices().get(0), new SamlAuthn());
    transport.registerServlet(idp.getDefaultArtificateResolutionService(), new SamlArtifactResolve());

    transport.registerServlet(spUrl, new MockServiceProvider());
  }

  public void testCredentials() throws ServletException, IOException, MalformedURLException {
    MockHttpServletResponse response = tryCredentials("joe", "plumber");
    assertEquals("Incorrect response status code", SC_OK, response.getStatus());
  }

  private MockHttpServletResponse tryCredentials(String username, String password)
      throws ServletException, IOException, MalformedURLException {

    // Initial request to service provider
    MockHttpServletResponse response1 = userAgent.exchange(makeMockHttpGet(null, spUrl));

    // Parse credentials-gathering form
    assertEquals("Incorrect response status code", SC_OK, response1.getStatus());
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(response1.getContentAsString()).getElementsByName("form", true);
    assertEquals("Wrong number of forms in response", 1, forms.length);
    String method = forms[0].getAttributeByName("method");
    assertNotNull("<form> missing method attribute", method);
    assertTrue("<form> method not POST", "POST".equalsIgnoreCase(method));
    String action = forms[0].getAttributeByName("action");
    assertNotNull("<form> missing action attribute", action);

    // Submit credentials-gathering form
    MockHttpServletRequest request2 = makeMockHttpPost(null, action);
    request2.addParameter("u1", username);
    request2.addParameter("pw1", password);
    generatePostContent(request2);
    return userAgent.exchange(request2);
  }
}
