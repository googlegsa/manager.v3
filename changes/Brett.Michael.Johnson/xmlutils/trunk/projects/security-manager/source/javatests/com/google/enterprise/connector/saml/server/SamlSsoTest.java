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

package com.google.enterprise.connector.saml.server;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;

import com.google.enterprise.connector.common.HttpExchange;
import com.google.enterprise.connector.common.MockHttpClient;
import com.google.enterprise.connector.common.MockHttpTransport;
import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.common.SecurityManagerUtil;
import com.google.enterprise.connector.common.ServletBase;
import com.google.enterprise.connector.common.StringPair;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.saml.client.MockArtifactConsumer;
import com.google.enterprise.connector.saml.client.MockServiceProvider;
import com.google.enterprise.connector.saml.common.Metadata;
import com.google.enterprise.connector.security.connectors.formauth.MockFormAuthServer1;
import com.google.enterprise.connector.security.connectors.formauth.MockFormAuthServer2;
import com.google.enterprise.connector.security.identity.AuthnDomain;
import com.google.enterprise.connector.security.identity.AuthnDomainGroup;
import com.google.enterprise.connector.security.identity.AuthnMechanism;
import com.google.enterprise.connector.security.identity.CredentialsGroup;
import com.google.enterprise.connector.security.identity.MockIdentityConfig;

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

public class SamlSsoTest extends SecurityManagerTestCase {
  private static final Logger LOGGER = Logger.getLogger(SamlSsoTest.class.getName());
  private static final String SP_URL =
      "http://localhost:8973/security-manager/mockserviceprovider";
  private static final String FORM1_URL =
      "http://localhost:8973/security-manager/mockformauthserver1";
  private static final String FORM2_URL =
      "http://localhost:8973/security-manager/mockformauthserver2";

  private MockHttpClient userAgent;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    Metadata metadata = ServletBase.getMetadata();

    // Initialize transport
    MockHttpTransport transport = new MockHttpTransport();
    userAgent = new MockHttpClient(transport);
    MockArtifactConsumer artifactConsumer = new MockArtifactConsumer();
    SecurityManagerUtil.setHttpClient(new MockHttpClient(transport));

    EntityDescriptor gsaEntity = metadata.getEntity(GSA_TESTING_ISSUER);
    SPSSODescriptor sp = gsaEntity.getSPSSODescriptor(SAML20P_NS);
    transport.registerServlet(sp.getDefaultAssertionConsumerService(), artifactConsumer);

    EntityDescriptor smEntity = metadata.getSmEntity();
    IDPSSODescriptor idp = smEntity.getIDPSSODescriptor(SAML20P_NS);
    SamlAuthn samlAuthn = new SamlAuthn();
    transport.registerServlet(idp.getSingleSignOnServices().get(0),
                              samlAuthn);
    transport.registerServlet(idp.getDefaultArtificateResolutionService(),
                              new SamlArtifactResolve());

    transport.registerServlet(SP_URL, new MockServiceProvider());
    transport.registerServlet(FORM1_URL, new MockFormAuthServer1());
    transport.registerServlet(FORM2_URL, new MockFormAuthServer2());
    ServletBase.getBackEnd().setMaxPrompts(1);
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
    List<AuthnDomainGroup> adgs = setUpMultipleConfig();
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential("joe", "plumber", adgs.get(0), params);
    addCredential("jim", "electrician", adgs.get(1), params);
    assertResults(SC_OK, 2, tryCredentials(params));
  }

  public void testMultiplePartial() throws IOException, MalformedURLException {
    List<AuthnDomainGroup> adgs = setUpMultipleConfig();
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential("joe", "plumber", adgs.get(0), params);
    addCredential("jim", "plumber", adgs.get(1), params);
    assertResults(SC_OK, 1, tryCredentials(params));
  }

  public void testMultipleBad() throws IOException, MalformedURLException {
    List<AuthnDomainGroup> adgs = setUpMultipleConfig();
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential("joe", "electrician", adgs.get(0), params);
    addCredential("jim", "plumber", adgs.get(1), params);
    assertResults(SC_FORBIDDEN, 0, tryCredentials(params));
  }

  private List<AuthnDomainGroup> setUpMultipleConfig() {
    MockIdentityConfig config = new MockIdentityConfig();
    List<AuthnDomainGroup> groups = config.getConfig();
    AuthnDomainGroup g1 = new AuthnDomainGroup("group1");
    new AuthnDomain("domain1", AuthnMechanism.FORMS_AUTH, FORM1_URL, "authority1", g1);
    groups.add(g1);
    AuthnDomainGroup g2 = new AuthnDomainGroup("group2");
    new AuthnDomain("domain2", AuthnMechanism.FORMS_AUTH, FORM2_URL, "authority2", g2);
    groups.add(g2);
    BackEnd backend = BackEnd.class.cast(Context.getInstance().getRequiredBean("BackEnd", BackEnd.class));
    backend.setIdentityConfig(config);
    return groups;
  }

  private void assertResults(int statusCode, int nGood, HttpExchange exchange) {
    assertEquals("Incorrect response status code", statusCode, exchange.getStatusCode());
    assertEquals("Incorrect number of verified groups", nGood, countGoodGroups());
  }

  private int countGoodGroups() {
    int nGood = 0;
    for (CredentialsGroup group: BackEndImpl.sessionCredentialsGroups(userAgent.getSession())) {
      if (group.isVerified()) {
        nGood += 1;
      }
    }
    return nGood;
  }

  private HttpExchange trySingleCredential(String username, String password)
      throws IOException, MalformedURLException {
    AuthnDomainGroup adg =
        ServletBase.getBackEnd().getIdentityConfig().getConfig().get(0);
    List<StringPair> params = new ArrayList<StringPair>();
    addCredential(username, password, adg, params);
    return tryCredentials(params);
  }

  private void addCredential(String username, String password,
                             AuthnDomainGroup adg, List<StringPair> params) {
    String name = adg.getHumanName();
    params.add(new StringPair("u" + name, username));
    params.add(new StringPair("pw" + name, password));
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
