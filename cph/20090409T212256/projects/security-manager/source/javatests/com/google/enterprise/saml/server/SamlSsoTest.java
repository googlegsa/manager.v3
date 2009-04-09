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

import com.google.enterprise.common.HttpExchange;
import com.google.enterprise.common.MockHttpClient;
import com.google.enterprise.common.MockHttpTransport;
import com.google.enterprise.common.StringPair;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.client.MockArtifactConsumer;
import com.google.enterprise.saml.client.MockServiceProvider;
import com.google.enterprise.saml.common.Metadata;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.security.identity.AuthnDomain;
import com.google.enterprise.security.identity.AuthnDomainGroup;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.MockIdentityConfig;

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
import java.util.Collection;
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

  private final MockHttpClient userAgent;

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
    assertResults(SC_OK, 1, trySingleCredential("joe", "plumber"));
  }

  public void testBadPassword() throws IOException, MalformedURLException {
    assertResults(SC_FORBIDDEN, 0, trySingleCredential("joe", "biden"));
  }

  public void testBadUsername() throws IOException, MalformedURLException {
    assertResults(SC_FORBIDDEN, 0, trySingleCredential("jim", "plumber"));
  }

  public void testMultipleGood() throws IOException, MalformedURLException {
    setUpMultipleConfig();
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential("joe", "plumber", params);
    addCredential("jim", "electrician", params);
    assertResults(SC_OK, 2, tryCredentials(params));
  }

  public void testMultiplePartial() throws IOException, MalformedURLException {
    setUpMultipleConfig();
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential("joe", "plumber", params);
    addCredential("jim", "plumber", params);
    assertResults(SC_OK, 1, tryCredentials(params));
  }

  public void testMultipleBad() throws IOException, MalformedURLException {
    setUpMultipleConfig();
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential("joe", "electrician", params);
    addCredential("jim", "plumber", params);
    assertResults(SC_FORBIDDEN, 0, tryCredentials(params));
  }

  private void setUpMultipleConfig() {
    MockIdentityConfig config = new MockIdentityConfig();
    List<AuthnDomainGroup> groups = config.getConfig();
    AuthnDomainGroup g1 = new AuthnDomainGroup("group1");
    new AuthnDomain("domain1", AuthNMechanism.FORMS_AUTH, "http://localhost/login1", g1);
    groups.add(g1);
    AuthnDomainGroup g2 = new AuthnDomainGroup("group2");
    new AuthnDomain("domain2", AuthNMechanism.FORMS_AUTH, "http://localhost/login2", g2);
    groups.add(g2);
    ConnectorManager.class.cast(Context.getInstance().getManager())
        .getBackEnd().setIdentityConfig(config);
  }

  private void assertResults(int statusCode, int nGood, HttpExchange exchange) {
    assertEquals("Incorrect response status code", statusCode, exchange.getStatusCode());
    assertEquals("Incorrect number of verified groups", nGood, countGoodGroups());
  }

  private int countGoodGroups() {
    int nGood = 0;
    Collection<CredentialsGroup> groups =
        SamlAuthn.sessionOmniForm(userAgent.getSession()).getCredentialsGroups();
    for (CredentialsGroup group: groups) {
      if (group.isVerified()) {
        nGood += 1;
      }
    }
    return nGood;
  }

  private HttpExchange trySingleCredential(String username, String password)
      throws IOException, MalformedURLException {
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential(username, password, params);
    return tryCredentials(params);
  }

  private void addCredential(String username, String password, List<StringPair> params) {
    int index = params.size() / 2;
    params.add(new StringPair("u" + index, username));
    params.add(new StringPair("pw" + index, password));
  }

  private HttpExchange tryCredentials(List<StringPair> params)
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
    HttpExchange exchange2 = userAgent.postExchange(new URL(action), params);
    exchange2.setFollowRedirects(true);
    exchange2.exchange();
    return exchange2;
  }
}
