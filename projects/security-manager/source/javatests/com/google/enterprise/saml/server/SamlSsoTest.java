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

import static com.google.enterprise.saml.common.OpenSamlUtil.makeEntityDescriptor;
import static com.google.enterprise.saml.common.SamlTestUtil.generatePostContent;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpGet;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;
import static com.google.enterprise.saml.common.SamlTestUtil.servletRequestToString;
import static com.google.enterprise.saml.common.SamlTestUtil.servletResponseToString;

public class SamlSsoTest extends TestCase {
  private static final Logger logger = Logger.getLogger(SamlSsoTest.class.getName());

  private static final String uaUrl = "http://localhost/";
  private static final String spUrl = "http://localhost:1234/provide-service";
  private static final String acsUrl = "http://localhost:1234/consume-artifact";
  private static final String ssoUrl = "http://localhost:5678/authn-request";
  private static final String formPostUrl = "http://localhost:5678/authn-verify";
  private static final String arUrl = "http://localhost:5678/resolve-artifact";

  private final EntityDescriptor spEntity;
  private final EntityDescriptor idpEntity;
  private final MockServiceProvider serviceProvider;
  private final MockIdentityProvider identityProvider;

  public SamlSsoTest(String name) throws ServletException {
    super(name);
    spEntity = makeEntityDescriptor("http://google.com/enterprise/saml/common/service-provider");
    idpEntity = makeEntityDescriptor("http://google.com/enterprise/saml/common/identity-provider");
    identityProvider = new MockIdentityProvider(idpEntity, ssoUrl, formPostUrl, arUrl);
    serviceProvider = new MockServiceProvider(spEntity, idpEntity, spUrl, acsUrl, identityProvider);
  }

  public void testInitialExchange() throws ServletException, IOException, MalformedURLException {
    MockHttpServletRequest request1 = makeMockHttpGet(serviceProvider, uaUrl, spUrl);
    MockHttpServletResponse response1 = new MockHttpServletResponse();
    logRequest(request1, "Initial request to service provider");
    serviceProvider.doGet(request1, response1);
    logResponse(response1, "Initial response from service provider");
    {
      int status = response1.getStatus();
      assertTrue("Incorrect response status code", (status == 303) || (status == 302));
    }

    String location = (String) response1.getHeader("Location");
    assertNotNull("No Location header in response", location);
    {
      int q = location.indexOf('?');
      assertEquals("Incorrect Location header in response", ssoUrl,
                   ((q < 0) ? location : location.substring(0, q)));
    }

    MockHttpServletRequest request2 = makeMockHttpGet(identityProvider, uaUrl, location);
    request2.addHeader("Referer", spUrl);
    MockHttpServletResponse response2 = new MockHttpServletResponse();
    logRequest(request2, "Redirect to identity provider");
    identityProvider.doGet(request2, response2);
    logResponse(response2, "Identity provider responds with form");
    assertEquals("Incorrect response status code", 200, response2.getStatus());

    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(response2.getContentAsString()).getElementsByName("form", true);
    assertEquals("Wrong number of forms in response", 1, forms.length);
    {
      String method = forms[0].getAttributeByName("method");
      assertNotNull("<form> missing method attribute", method);
      assertTrue("<form> method not POST", "POST".equalsIgnoreCase(method));
    }
    String action = forms[0].getAttributeByName("action");
    assertNotNull("<form> missing action attribute", action);

    MockHttpServletRequest request3 = makeMockHttpPost(identityProvider, uaUrl, action);
    request3.addHeader("Referer", location);
    request3.addParameter("username", "joe");
    request3.addParameter("password", "plumber");
    generatePostContent(request3);
    MockHttpServletResponse response3 = new MockHttpServletResponse();
    logRequest(request3, "Submit form to identity provider");
    identityProvider.doPost(request3, response3);
    logResponse(response3, "Identity provider responds");
    assertEquals("Incorrect response status code", 200, response3.getStatus());

    // TODO(cph): write artifact-resolution exchange and response to user agent.
  }

  private void logRequest(MockHttpServletRequest request, String tag) throws IOException {
    logger.log(Level.INFO, servletRequestToString(request, tag));
  }

  private void logResponse(MockHttpServletResponse response, String tag) throws IOException {
    logger.log(Level.INFO, servletResponseToString(response, tag));
  }
}
