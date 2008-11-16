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

import com.google.enterprise.saml.client.MockServiceProvider;
import com.google.enterprise.saml.client.MockUserAgent;
import com.google.enterprise.saml.common.MockHttpTransport;

import junit.framework.TestCase;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.saml.common.OpenSamlUtil.makeEntityDescriptor;
import static com.google.enterprise.saml.common.SamlTestUtil.generatePostContent;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpGet;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;
import static com.google.enterprise.saml.common.SamlTestUtil.servletRequestToString;
import static com.google.enterprise.saml.common.SamlTestUtil.servletResponseToString;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class SamlSsoTest extends TestCase {
  private static final Logger logger = Logger.getLogger(SamlSsoTest.class.getName());

  private static final String uaUrl = "http://localhost/";
  private static final String spUrl = "http://localhost:1234/provide-service";
  private static final String acsUrl = "http://localhost:1234/consume-assertion";
  private static final String ssoUrl = "http://localhost:5678/authn-request";
  private static final String formPostUrl = "http://localhost:5678/authn-verify";
  private static final String arUrl = "http://localhost:5678/resolve-artifact";

  private final MockUserAgent userAgent;

  public SamlSsoTest(String name) throws ServletException {
    super(name);

    EntityDescriptor spEntity =
        makeEntityDescriptor("http://google.com/enterprise/saml/common/service-provider");
    EntityDescriptor idpEntity =
        makeEntityDescriptor("http://google.com/enterprise/saml/common/identity-provider");

    // Initialize transport
    MockHttpTransport transport = new MockHttpTransport();
    userAgent = new MockUserAgent(transport);

    MockServiceProvider sp = new MockServiceProvider(spEntity, idpEntity, spUrl, acsUrl, transport);
    transport.registerServlet(spUrl, sp);
    transport.registerServlet(acsUrl, sp);

    MockIdentityProvider idp = new MockIdentityProvider(idpEntity, spEntity, ssoUrl, formPostUrl, arUrl);
    transport.registerServlet(ssoUrl, idp);
    transport.registerServlet(formPostUrl, idp);
    transport.registerServlet(arUrl, idp);
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
    MockHttpServletResponse response1 = userAgent.exchange(makeMockHttpGet(uaUrl, spUrl));

    // Parse credentials-gathering form
    assertEquals("Incorrect response status code", SC_OK, response1.getStatus());
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(response1.getContentAsString()).getElementsByName("form", true);
    assertEquals("Wrong number of forms in response", 1, forms.length);
    {
      String method = forms[0].getAttributeByName("method");
      assertNotNull("<form> missing method attribute", method);
      assertTrue("<form> method not POST", "POST".equalsIgnoreCase(method));
    }
    String action = forms[0].getAttributeByName("action");
    assertNotNull("<form> missing action attribute", action);

    // Submit credentials-gathering form
    MockHttpServletRequest request2 = makeMockHttpPost(uaUrl, action);
    request2.addParameter("username", username);
    request2.addParameter("password", password);
    generatePostContent(request2);
    return userAgent.exchange(request2);
  }

  void logRequest(HttpServletRequest request, String tag) throws IOException {
    logger.log(Level.INFO, "Request: " + servletRequestToString(request, tag));
  }

  void logResponse(HttpServletResponse response, String tag) throws IOException {
    logger.log(Level.INFO,
               "Response: " + servletResponseToString((MockHttpServletResponse) response, tag));
  }
}
