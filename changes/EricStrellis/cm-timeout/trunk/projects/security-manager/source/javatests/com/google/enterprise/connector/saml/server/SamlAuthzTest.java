// Copyright 2008 Google Inc.
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

package com.google.enterprise.connector.saml.server;

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runEncoder;
import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;

import com.google.enterprise.connector.common.HttpExchange;
import com.google.enterprise.connector.common.MockHttpClient;
import com.google.enterprise.connector.common.MockHttpTransport;
import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.common.StringPair;
import com.google.enterprise.connector.saml.common.HTTPSOAP11MultiContextDecoder;
import com.google.enterprise.connector.saml.common.HTTPSOAP11MultiContextEncoder;
import com.google.enterprise.connector.saml.common.HttpExchangeToInTransport;
import com.google.enterprise.connector.saml.common.HttpExchangeToOutTransport;
import com.google.enterprise.connector.saml.common.Metadata;
import com.google.enterprise.connector.saml.common.OpenSamlUtil;
import com.google.enterprise.connector.saml.common.SamlLogUtil;
import com.google.enterprise.connector.servlet.SecurityManagerServlet;

import org.json.JSONException;
import org.json.JSONObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.PDPDescriptor;
import org.opensaml.ws.message.encoder.MessageEncodingException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

/**
 * Unit test for SamlAuthz handler.
 */
public class SamlAuthzTest extends SecurityManagerTestCase {

  private static final Logger LOGGER = Logger.getLogger(SamlAuthzTest.class.getName());

  private MockHttpClient relyingParty;
  private Metadata metadata;
  private AuthzService authzService;
  private SamlAuthz samlAuthz;

  private static final SamlAuthz.Authorizer ALLOW_ALL = new SamlAuthz.Authorizer() {
    /* @Override */
    public Set<String> authorize(String username, Collection<String> resources) {
      return new HashSet<String>(resources);
    }
  };

  private static final SamlAuthz.Authorizer ALLOW_BY_SUBSTRING = new SamlAuthz.Authorizer() {
    /* @Override */
    public Set<String> authorize(String username, Collection<String> resources) {
      Set<String> result = new HashSet<String>();
      for (String resource : resources) {
        if (resource.contains(username)) {
          result.add(resource);
        }
      }
      return result;
    }
  };

  @Override
  public void setUp() throws Exception {
    super.setUp();

    metadata = SecurityManagerServlet.getMetadata();

    // Initialize transport
    MockHttpTransport transport = new MockHttpTransport();
    relyingParty = new MockHttpClient(transport);

    EntityDescriptor smEntity = metadata.getSmEntity();
    PDPDescriptor pdp = smEntity.getPDPDescriptor(SAML20P_NS);
    samlAuthz = new SamlAuthz();
    authzService = pdp.getAuthzServices().get(0);
    transport.registerServlet(authzService, samlAuthz);
  }

  public void testUsingHTTPSOAP11Binding() throws IOException, ServletException {

    // this test uses the opensaml standard HTTPSOAP11Encoder and
    // HTTPSOAP11Decoder
    // so we can only test one query at a time

    String resource = "http://www.abc.com/secret.html";
    String username = "fred";

    samlAuthz.setAuthorizer(ALLOW_ALL);

    SAMLMessageContext<Response, AuthzDecisionQuery, NameID> context = makeSamlMessageContext();
    AuthzDecisionQuery decisionQuery = setupDecisionQuery(username, resource);
    SamlLogUtil.logXml(LOGGER, Level.INFO, "Authz Decision Query ", decisionQuery);

    context.setOutboundSAMLMessage(decisionQuery);

    URL authzUrl = new URL(authzService.getLocation());
    HttpExchange exchange = relyingParty.postExchange(authzUrl, null);

    HttpExchangeToOutTransport out = new HttpExchangeToOutTransport(exchange);
    context.setOutboundMessageTransport(out);
    runEncoder(new HTTPSOAP11Encoder(), context);
    out.finish();

    // Do HTTP exchange
    int status = exchange.exchange();
    if (status != 200) {
      throw new ServletException("Incorrect HTTP status: " + status);
    }

    // Decode the response
    HttpExchangeToInTransport in = new HttpExchangeToInTransport(exchange);
    context.setInboundMessageTransport(in);
    runDecoder(new HTTPSOAP11Decoder(), context);

    Response response = context.getInboundSAMLMessage();
    SamlLogUtil.logXml(LOGGER, Level.INFO, "Response: ", response);

    assertEquals(StatusCode.SUCCESS_URI, response.getStatus().getStatusCode().getValue());

    List<Assertion> assertions = response.getAssertions();
    assertEquals(1, assertions.size());
    Assertion assertion = assertions.get(0);

    String responseUsername = assertion.getSubject().getNameID().getValue();
    assertEquals(username, responseUsername);

    List<Statement> statements = assertion.getStatements();
    assertEquals(1, statements.size());
    Statement statement = statements.get(0);

    AuthzDecisionStatement authzDecisionStatement = AuthzDecisionStatement.class.cast(statement);
    assertEquals(DecisionTypeEnumeration.PERMIT, authzDecisionStatement.getDecision());
    assertEquals(resource, authzDecisionStatement.getResource());
  }

  private AuthzDecisionQuery setupDecisionQuery(String username, String resource) {
    Subject subject = OpenSamlUtil.makeSubject(username);
    Action action = OpenSamlUtil.makeAction(Action.HTTP_GET_ACTION, Action.GHPP_NS_URI);
    AuthzDecisionQuery decisionQuery =
        OpenSamlUtil.makeAuthzDecisionQuery(subject, resource, action);
    decisionQuery.setIssuer(makeIssuer(SecurityManagerTestCase.GSA_TESTING_ISSUER));
    return decisionQuery;
  }

  public void testSimpleUsingHTTPSOAP11MultiContextBinding() throws JSONException,
      MalformedURLException, IOException, ServletException {

    String[] resources = {"http://www.abc.com/secret.html", "http://www.abc.com/notsecret.html"};
    String[] usernames = {"fred", "fred"};
    samlAuthz.setAuthorizer(ALLOW_ALL);

    String expected =
        "{'fred':{'http://www.abc.com/secret.html':true,"
            + "'http://www.abc.com/notsecret.html':true}}";

    Map<String, Map<String, Boolean>> results = runMultiContextTest(usernames, resources);
    Map<String, Map<String, Boolean>> e = makeExpected(expected);
    verifyResults(e, results);
  }

  private Map<String, Map<String, Boolean>> runMultiContextTest(String[] usernames,
      String[] resources) throws MalformedURLException, IOException, ServletException {
    List<StringPair> queries = new ArrayList<StringPair>();
    assertEquals(resources.length, usernames.length);
    for (int i = 0; i < resources.length; i++) {
      queries.add(new StringPair(usernames[i], resources[i]));
    }
    return runMultiContextTest(queries);
  }

  public void testAnotherUsingHTTPSOAP11MultiContextBinding() throws IOException, ServletException,
      JSONException {

    samlAuthz.setAuthorizer(ALLOW_ALL);

    String expected =
        "{'fred':{" + "'http://www.abc.com/secret.html':true," + "'http://xyz.com/fubar':true,"
            + "'http://www.abc.com/notsecret.html':true}}";

    Map<String, Map<String, Boolean>> e = makeExpected(expected);
    Map<String, Map<String, Boolean>> results = runMultiContextTest(e);

    verifyResults(e, results);
  }

  public void testWithAuthorizer() throws IOException, ServletException, JSONException {

    samlAuthz.setAuthorizer(ALLOW_BY_SUBSTRING);

    String expected =
        "{'fred':{" + "'http://www.abc.com/fred/secret.html':true,"
            + "'http://xyz.com/fubar':false," + "'http://www.abc.com/notsecrettofred.html':true}}";

    Map<String, Map<String, Boolean>> e = makeExpected(expected);
    Map<String, Map<String, Boolean>> results = runMultiContextTest(e);

    verifyResults(e, results);
  }

  public void testMultipleUsers() throws IOException, ServletException, JSONException {

    samlAuthz.setAuthorizer(ALLOW_BY_SUBSTRING);

    String expected =
        "{'fred':{" + "'http://www.abc.com/fred/secret.html':true,"
            + "'http://xyz.com/fubar':false," + "'http://www.abc.com/notsecrettofred.html':true},"
            + "'george':{" + "'http://www.abc.com/fred/secret.html':false,"
            + "'http://xyz.com/george/fubar':true,"
            + "'http://www.abc.com/notsecrettofred.html':false}}";

    Map<String, Map<String, Boolean>> e = makeExpected(expected);
    Map<String, Map<String, Boolean>> results = runMultiContextTest(e);

    verifyResults(e, results);
  }

  private void verifyResults(Map<String, Map<String, Boolean>> e,
      Map<String, Map<String, Boolean>> r) {
    for (String user : r.keySet()) {
      assertNotNull("Found results for user " + user + " but none were expected", e.get(user));
    }
    for (String user : e.keySet()) {
      Map<String, Boolean> ru = r.get(user);
      assertNotNull("Expected results for user " + user + " but none were found", ru);
      Map<String, Boolean> eu = e.get(user);
      for (String resource : ru.keySet()) {
        Boolean eub = eu.get(resource);
        assertNotNull("Found a result for user " + user + " resource " + resource
            + " but none expected", eub);
      }
      for (String resource : eu.keySet()) {
        Boolean rub = ru.get(resource);
        assertNotNull("Expected a result for user " + user + " resource " + resource
            + " but none found", rub);
        Boolean eub = eu.get(resource);
        assertEquals("Expected " + eub + " for user " + user + " resource " + resource, eub, rub);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, Boolean>> makeExpected(String expected) throws JSONException {
    JSONObject jexpected = new JSONObject(expected);
    Map<String, Map<String, Boolean>> e = new HashMap<String, Map<String, Boolean>>();
    Iterator<String> users = jexpected.keys();
    while (users.hasNext()) {
      String user = users.next();
      JSONObject jo = jexpected.getJSONObject(user);
      Iterator<String> resources = jo.keys();
      while (resources.hasNext()) {
        String resource = resources.next();
        boolean b = jo.getBoolean(resource);
        updateResults(e, user, resource, b);
      }
    }
    return e;
  }

  private Map<String, Map<String, Boolean>> runMultiContextTest(Map<String, Map<String, Boolean>> e)
      throws MalformedURLException, IOException, ServletException {
    List<StringPair> queries = new ArrayList<StringPair>();
    for (String user : e.keySet()) {
      Map<String, Boolean> eu = e.get(user);
      for (String resource : eu.keySet()) {
        queries.add(new StringPair(user, resource));
      }
    }
    return runMultiContextTest(queries);
  }

  private Map<String, Map<String, Boolean>> runMultiContextTest(List<StringPair> queries)
      throws IOException, MalformedURLException, ServletException {

    // this test uses our own HTTPSOAP11MultiContext encoder and decoder
    // here we can send multiple authz requests in a batch

    SAMLMessageContext<Response, AuthzDecisionQuery, NameID> context = makeSamlMessageContext();

    HttpExchange exchange = setupMultiContextExchange(context, queries);

    // Do HTTP exchange
    int status = exchange.exchange();
    if (status != 200) {
      throw new ServletException("Incorrect HTTP status: " + status);
    }
    HttpExchangeToInTransport in = new HttpExchangeToInTransport(exchange);
    return decodeMultiContextResults(in, context);
  }

  private HttpExchange setupMultiContextExchange(
      SAMLMessageContext<Response, AuthzDecisionQuery, NameID> context, List<StringPair> queries)
      throws IOException, MalformedURLException {
    URL authzUrl = new URL(authzService.getLocation());
    HttpExchange exchange = relyingParty.postExchange(authzUrl, null);
    HttpExchangeToOutTransport out = new HttpExchangeToOutTransport(exchange);

    HTTPSOAP11MultiContextEncoder multiContextEncoder = new HTTPSOAP11MultiContextEncoder();
    for (StringPair sp : queries) {
      AuthzDecisionQuery decisionQuery = setupDecisionQuery(sp.getName(), sp.getValue());
      SamlLogUtil.logXml(LOGGER, Level.INFO, "Authz Decision Query ", decisionQuery);
      context.setOutboundSAMLMessage(decisionQuery);
      context.setOutboundMessageTransport(out);
      runEncoder(multiContextEncoder, context);
    }
    try {
      multiContextEncoder.finish();
    } catch (MessageEncodingException e) {
      throw new IOException(e);
    }
    out.finish();
    return exchange;
  }

  private Map<String, Map<String, Boolean>> decodeMultiContextResults(HttpExchangeToInTransport in,
      SAMLMessageContext<Response, AuthzDecisionQuery, NameID> context) throws IOException {
    // Decode the responses
    context.setInboundMessageTransport(in);

    SAMLMessageDecoder decoder = new HTTPSOAP11MultiContextDecoder();

    Map<String, Map<String, Boolean>> results = new HashMap<String, Map<String, Boolean>>();

    while (true) {
      // Decode a request
      try {
        runDecoder(decoder, context);
      } catch (IndexOutOfBoundsException e) {
        // normal indication that there are no more messages to decode
        break;
      }

      Response response = context.getInboundSAMLMessage();
      SamlLogUtil.logXml(LOGGER, Level.INFO, "Response: ", response);

      assertEquals(StatusCode.SUCCESS_URI, response.getStatus().getStatusCode().getValue());

      List<Assertion> assertions = response.getAssertions();
      assertEquals(1, assertions.size());
      Assertion assertion = assertions.get(0);

      String responseUsername = assertion.getSubject().getNameID().getValue();
      List<Statement> statements = assertion.getStatements();
      assertEquals(1, statements.size());
      Statement statement = statements.get(0);

      AuthzDecisionStatement authzDecisionStatement = AuthzDecisionStatement.class.cast(statement);
      DecisionTypeEnumeration decision = authzDecisionStatement.getDecision();
      String resource = authzDecisionStatement.getResource();
      updateResults(results, responseUsername, resource,
          (decision == DecisionTypeEnumeration.PERMIT));
    }
    return results;
  }

  private void updateResults(Map<String, Map<String, Boolean>> results, String username,
      String resource, boolean b) {
    Map<String, Boolean> map = results.get(username);
    if (map == null) {
      map = new HashMap<String, Boolean>();
      results.put(username, map);
    }
    map.put(resource, b);
  }

}
