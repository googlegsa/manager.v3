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

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.client.MockArtifactConsumer;
import com.google.enterprise.saml.client.MockServiceProvider;
import com.google.enterprise.saml.client.MockUserAgent;
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

import static com.google.enterprise.saml.common.OpenSamlUtil.GSA_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.SM_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResolutionService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertionConsumerService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeEntityDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIdpSsoDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSingleSignOnService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSpSsoDescriptor;
import static com.google.enterprise.saml.common.SamlTestUtil.generatePostContent;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpGet;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

public class SamlSsoTest extends TestCase {

  private static final String spUrl = "http://localhost:1234/provide-service";
  private static final String acsUrl = "http://localhost:1234/consume-assertion";
  private static final String ssoUrl = "http://localhost:5678/authn";
  private static final String arUrl = "http://localhost:5678/resolve-artifact";

  private final MockHttpTransport transport;
  private final MockUserAgent userAgent;

  public SamlSsoTest(String name) throws ServletException {
    super(name);

    Context.getInstance().setStandaloneContext(
        "source/webdocs/prod/applicationContext.xml",
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);

    // Build metadata for security manager
    EntityDescriptor smEntity = makeEntityDescriptor(SM_ISSUER);
    IDPSSODescriptor idp = makeIdpSsoDescriptor(smEntity);
    makeSingleSignOnService(idp, SAML2_REDIRECT_BINDING_URI, ssoUrl);
    makeArtifactResolutionService(idp, SAML2_SOAP11_BINDING_URI, arUrl).setIsDefault(true);

    // Build metadata for GSA
    EntityDescriptor gsaEntity = makeEntityDescriptor(GSA_ISSUER);
    SPSSODescriptor sp = makeSpSsoDescriptor(gsaEntity);
    makeAssertionConsumerService(sp, SAML2_ARTIFACT_BINDING_URI, acsUrl).setIsDefault(true);

    // Initialize transport
    transport = new MockHttpTransport();
    userAgent = new MockUserAgent(transport);

    transport.registerServlet(spUrl, new MockServiceProvider(gsaEntity, smEntity));
    transport.registerServlet(acsUrl, new MockArtifactConsumer(gsaEntity, smEntity, transport));
    transport.registerServlet(ssoUrl, new SamlAuthn(smEntity, gsaEntity));
    transport.registerServlet(arUrl, new SamlArtifactResolve(smEntity, gsaEntity));
  }

  public void testGoodCredentials() throws ServletException, IOException, MalformedURLException {
    MockHttpServletResponse response = tryCredentials("joe", "plumber");
    assertEquals("Incorrect response status code", SC_OK, response.getStatus());
  }

  public void testBadCredentials() throws ServletException, IOException, MalformedURLException {
    MockHttpServletResponse response = tryCredentials("foo", "bar");
    assertEquals("Incorrect response status code", SC_UNAUTHORIZED, response.getStatus());
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
    request2.addParameter("username", username);
    request2.addParameter("password", password);
    generatePostContent(request2);
    return userAgent.exchange(request2);
  }
}
