// Copyright (C) 2008, 2009 Google Inc.
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

import com.google.enterprise.common.HttpClientInterface;
import com.google.enterprise.common.HttpExchange;
import com.google.enterprise.common.MockHttpClient;
import com.google.enterprise.common.MockHttpTransport;
import com.google.enterprise.common.StringPair;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.client.MockArtifactConsumer;
import com.google.enterprise.saml.client.MockServiceProvider;
import com.google.enterprise.saml.common.Metadata;

import junit.framework.TestCase;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class SamlSsoTest extends TestCase {
  private static final Logger LOGGER = Logger.getLogger(SamlSsoTest.class.getName());
  private static final String SP_URL =
      "http://localhost:8973/security-manager/mockserviceprovider";

  private final HttpClientInterface userAgent;

  public SamlSsoTest(String name) throws ServletException {
    super(name);

    Context ctx = Context.getInstance();
    ctx.setStandaloneContext(Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
                             Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    Metadata metadata =
        Metadata.class.cast(ctx.getRequiredBean("Metadata", Metadata.class));

    // Initialize transport
    MockHttpTransport transport = new MockHttpTransport();
    userAgent = new MockHttpClient(transport);
    MockArtifactConsumer artifactConsumer = new MockArtifactConsumer();
    artifactConsumer.setHttpClient(new MockHttpClient(transport));

    EntityDescriptor gsaEntity = metadata.getSpEntity();
    SPSSODescriptor sp = gsaEntity.getSPSSODescriptor(SAML20P_NS);
    transport.registerServlet(sp.getDefaultAssertionConsumerService(), artifactConsumer);

    EntityDescriptor smEntity = metadata.getSmEntity();
    IDPSSODescriptor idp = smEntity.getIDPSSODescriptor(SAML20P_NS);
    SamlAuthn samlAuthn = new SamlAuthn();
    samlAuthn.setMaxPrompts(1);
    transport.registerServlet(idp.getSingleSignOnServices().get(0),
                              samlAuthn);
    transport.registerServlet(idp.getDefaultArtificateResolutionService(),
                              new SamlArtifactResolve());

    transport.registerServlet(SP_URL, new MockServiceProvider());
  }

  public void testGood() throws IOException, MalformedURLException {
    HttpExchange exchange = tryCredentials("joe", "plumber");
    assertEquals("Incorrect response status code", SC_OK, exchange.getStatusCode());
  }

  public void testBadPassword() throws IOException, MalformedURLException {
    HttpExchange exchange = tryCredentials("joe", "biden");
    assertEquals("Incorrect response status code", SC_FORBIDDEN, exchange.getStatusCode());
  }

  public void testBadUsername() throws IOException, MalformedURLException {
    HttpExchange exchange = tryCredentials("jim", "plumber");
    assertEquals("Incorrect response status code", SC_FORBIDDEN, exchange.getStatusCode());
  }

  private HttpExchange tryCredentials(String username, String password)
      throws IOException, MalformedURLException {
    LOGGER.info("start test");

    // Initial request to service provider
    HttpExchange exchange1 = userAgent.getExchange(new URL(SP_URL));
    exchange1.setFollowRedirects(true);
    int status = exchange1.exchange();

    // Parse credentials-gathering form
    assertEquals("Incorrect response status code", SC_OK, status);
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms =
        cleaner.clean(exchange1.getResponseEntityAsString()).getElementsByName("form", true);
    assertEquals("Wrong number of forms in response", 1, forms.length);
    String method = forms[0].getAttributeByName("method");
    assertNotNull("<form> missing method attribute", method);
    assertTrue("<form> method not POST", "POST".equalsIgnoreCase(method));
    String action = forms[0].getAttributeByName("action");
    assertNotNull("<form> missing action attribute", action);

    // Submit credentials-gathering form
    List<StringPair> params = new ArrayList<StringPair>();
    params.add(new StringPair("u0", username));
    params.add(new StringPair("pw0", password));
    HttpExchange exchange2 = userAgent.postExchange(new URL(action), params);
    exchange2.setFollowRedirects(true);
    exchange2.exchange();
    return exchange2;
  }
}
