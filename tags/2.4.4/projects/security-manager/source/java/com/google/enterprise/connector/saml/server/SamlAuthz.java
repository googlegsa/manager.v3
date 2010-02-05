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

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAuthzDecisionStatement;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runEncoder;
import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.enterprise.connector.common.PostableHttpServlet;
import com.google.enterprise.connector.saml.common.HTTPSOAP11MultiContextDecoder;
import com.google.enterprise.connector.saml.common.HTTPSOAP11MultiContextEncoder;
import com.google.enterprise.connector.saml.common.OpenSamlUtil;
import com.google.enterprise.connector.saml.common.SamlLogUtil;
import com.google.enterprise.connector.servlet.SecurityManagerServlet;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SamlAuthz extends SecurityManagerServlet implements PostableHttpServlet {

  /** Required for serializable classes. */
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(SamlAuthz.class.getName());

  private static final Authorizer DENY_ALL_AUTHORIZER = new SamlAuthz.Authorizer() {
    /* @Override */
    public Set<String> authorize(String username, Collection<String> resources) {
      return new HashSet<String>();
    }
  };
  private Authorizer authorizer = DENY_ALL_AUTHORIZER;

  public void setAuthorizer(Authorizer authorizer) {
    this.authorizer = authorizer;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    authorize(req, resp);
  }

  private void authorize(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    List<SAMLMessageContext<AuthzDecisionQuery, Response, NameID>> contexts =
        new ArrayList<SAMLMessageContext<AuthzDecisionQuery, Response, NameID>>();

    String localEntityId = getSmEntityId();
    HttpServletRequestAdapter httpServletRequestAdapter = new HttpServletRequestAdapter(req);
    SAMLMessageDecoder decoder = new HTTPSOAP11MultiContextDecoder();

    Multimap<String, String> resourcesByUsername = ArrayListMultimap.create();

    EntityDescriptor localEntity = getEntity(localEntityId);

    while (true) {
      SAMLMessageContext<AuthzDecisionQuery, Response, NameID> context = makeSamlMessageContext();
      initializeLocalEntity(context, localEntity, localEntity.getPDPDescriptor(SAML20P_NS),
          AuthzService.DEFAULT_ELEMENT_NAME);
      context.setInboundMessageTransport(httpServletRequestAdapter);

      // Decode a request
      try {
        runDecoder(decoder, context);
      } catch (IndexOutOfBoundsException e) {
        // normal indication that there are no more messages to decode
        break;
      }
      AuthzDecisionQuery authzDecisionQuery = context.getInboundSAMLMessage();
      String message = "authzDecisionQuery as XML:";
      SamlLogUtil.logXml(LOGGER, Level.INFO, message, authzDecisionQuery);
      contexts.add(context);

      String resource = authzDecisionQuery.getResource();
      String username = authzDecisionQuery.getSubject().getNameID().getValue();
      resourcesByUsername.put(username, resource);
    }

    Map<String, Set<String>> authorizedResourcesByUsername = new HashMap<String, Set<String>>();
    for (String username : resourcesByUsername.keySet()) {
      authorizedResourcesByUsername.put(username, authorizer.authorize(username,
          resourcesByUsername.get(username)));
    }

    HTTPSOAP11MultiContextEncoder multiContextEncoder = new HTTPSOAP11MultiContextEncoder();
    HttpServletResponseAdapter httpServletResponseAdapter = 
      new HttpServletResponseAdapter(resp, true);

    for (SAMLMessageContext<AuthzDecisionQuery, Response, NameID> context : contexts) {
      AuthzDecisionQuery authzDecisionQuery = context.getInboundSAMLMessage();
      String resource = authzDecisionQuery.getResource();
      String username = authzDecisionQuery.getSubject().getNameID().getValue();

      Set<String> set = authorizedResourcesByUsername.get(username);
      DecisionTypeEnumeration d =
          set.contains(resource) ? DecisionTypeEnumeration.PERMIT : DecisionTypeEnumeration.DENY;

      // Create decision statement
      // Note: for now, we disregard the Action in the query and just assert that
      // http get is permitted or denied.
      // Todo: change this?  Perhaps return indeterminate for anything except get
      Action responseAction = OpenSamlUtil.makeAction(Action.HTTP_GET_ACTION, Action.GHPP_NS_URI);
      AuthzDecisionStatement authzDecisionStatement = makeAuthzDecisionStatement(resource, d);
      authzDecisionStatement.getActions().add(responseAction);

      // Create a response
      Response response =
          OpenSamlUtil.makeResponse(authzDecisionQuery, OpenSamlUtil
              .makeStatus(StatusCode.SUCCESS_URI));
      response.setIssuer(makeIssuer(localEntityId));

      Subject responseSubject = OpenSamlUtil.makeSubject(username);

      Assertion assertion = OpenSamlUtil.makeAssertion(makeIssuer(localEntityId), responseSubject);
      assertion.getAuthzDecisionStatements().add(authzDecisionStatement);
      response.getAssertions().add(assertion);

      // Encode response.
      context.setOutboundSAMLMessage(response);

      String message = "authzDecisionStatement as XML:";
      SamlLogUtil.logXml(LOGGER, Level.INFO, message, authzDecisionStatement);

      initResponse(resp);
      context.setOutboundMessageTransport(httpServletResponseAdapter);
      runEncoder(multiContextEncoder, context);
    }
    try {
      multiContextEncoder.finish();
    } catch (MessageEncodingException e) {
      throw new IOException(e);
    }
  }

  public static interface Authorizer {
    Set<String> authorize(String username, Collection<String> resources);
  }
}
