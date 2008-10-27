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

package com.google.enterprise.saml.client;

import com.google.enterprise.saml.common.HttpServletRequestClientAdapter;
import com.google.enterprise.saml.common.HttpServletResponseClientAdapter;
import com.google.enterprise.saml.common.OpenSamlUtil;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.BaseMessageDecoder;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.BaseMessageEncoder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.OutTransport;
import org.opensaml.ws.transport.http.HTTPTransportUtils;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

/**
 * The MockAuthzRequester class implements a servlet pretending to be a SAML authorization
 * requester. It submits authz query requests and receives authz query decisions in response.
 */
public class MockAuthzRequester {
  // private static final Logger LOGGER = Logger.getLogger(MockAuthzRequester.class.getName());
  private static final long serialVersionUID = 1L;
  private final Endpoint endpoint;
  private final MockAuthzResponder responder;
  private static final HTTPSOAP11Encoder encoder = new HTTPSOAP11Encoder();
  private static final HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();

  private static class ExtendedEncoder extends BaseMessageEncoder {
    public ExtendedEncoder() {
      super();
    }

    @Override
    protected void doEncode(MessageContext context) throws MessageEncodingException {
      OutTransport transport = context.getOutboundMessageTransport();
      Element element = marshallMessage(context.getOutboundMessage());
      try {
        Writer out = new OutputStreamWriter(transport.getOutgoingStream(), "UTF-8");
        XMLHelper.writeNode(element, out);
        out.flush();
      } catch (UnsupportedEncodingException e) {
        throw new MessageEncodingException("JVM does not support required UTF-8 encoding");
      } catch (IOException e) {
        throw new MessageEncodingException("Unable to write message content to outbound stream", e);
      }
    }

    public boolean providesMessageConfidentiality(MessageContext arg0)
        throws MessageEncodingException {
      // TODO Auto-generated method stub
      return false;
    }

    public boolean providesMessageIntegrity(MessageContext arg0) throws MessageEncodingException {
      // TODO Auto-generated method stub
      return false;
    }
  }

  private static class ExtendedDecoder extends BaseMessageDecoder {
    public ExtendedDecoder() {
      super();
    }

    protected void doDecode(MessageContext context) throws MessageDecodingException {
      InTransport transport = context.getInboundMessageTransport();
      XMLObject message = unmarshallMessage(transport.getIncomingStream());
      context.setInboundMessage(message);
    }
  }

  private static final ExtendedEncoder multiEncoder = new ExtendedEncoder();
  private static final ExtendedDecoder multiDecoder = new ExtendedDecoder();

  public MockAuthzRequester(String serviceUrl, MockAuthzResponder responder) {
    this.endpoint =
        OpenSamlUtil.makeAuthzService(SAMLConstants.SAML2_SOAP11_BINDING_URI, serviceUrl);
    this.responder = responder;
  }

  public DecisionTypeEnumeration singleRequest(Subject subject, String resource, Action action)
      throws ServletException, IOException {
    AuthzDecisionQuery request = OpenSamlUtil.makeAuthzDecisionQuery(subject, resource, action);
    AuthzDecisionStatement statement = singleRequest(request);
    return statement.getDecision();
  }

  public AuthzDecisionStatement singleRequest(AuthzDecisionQuery request)
      throws ServletException, IOException {
    MockHttpServletRequest servletRequest = encodeSingleRequest(request);
    MockHttpServletResponse servletResponse = responder.decide(servletRequest);
    Response response = decodeSingleResponse(servletResponse);
    Assertion assertion = response.getAssertions().get(0);
    return assertion.getAuthzDecisionStatements().get(0);
  }

  private MockHttpServletRequest encodeSingleRequest(AuthzDecisionQuery request) {
    HttpServletRequestClientAdapter transport = new HttpServletRequestClientAdapter();
    SAMLMessageContext<SAMLObject, AuthzDecisionQuery, NameID> context =
        makeRequestContext(transport);
    context.setOutboundSAMLMessage(request);
    try {
      encoder.encode(context);
    } catch (MessageEncodingException e) {
      throw new RuntimeException(e);
    }
    return transport.getRequest();
  }

  private Response decodeSingleResponse(MockHttpServletResponse response) {
    SAMLMessageContext<Response, SAMLObject, NameID> context = makeResponseContext(response);
    try {
      decoder.decode(context);
    } catch (MessageDecodingException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
    return context.getInboundSAMLMessage();
  }

  public List<AuthzDecisionStatement> multipleRequests(List<AuthzDecisionQuery> requests)
      throws ServletException, IOException {
    MockHttpServletRequest servletRequest = encodeMultipleRequests(requests);
    MockHttpServletResponse servletResponse = responder.decide(servletRequest);
    List<AuthzDecisionStatement> statements = new ArrayList<AuthzDecisionStatement>();
    for (Response response : decodeMultipleRequests(servletResponse)) {
      Assertion assertion = response.getAssertions().get(0);
      statements.add(assertion.getAuthzDecisionStatements().get(0));
    }
    return statements;
  }

  private MockHttpServletRequest encodeMultipleRequests(List<AuthzDecisionQuery> requests) {
    HttpServletRequestClientAdapter transport = new HttpServletRequestClientAdapter();
    SAMLMessageContext<SAMLObject, AuthzDecisionQuery, NameID> context =
        makeRequestContext(transport);
    Envelope message = envelopeMultipleRequests(requests);
    context.setOutboundMessage(message);
    transport.setHeader("SOAPAction", "http://www.oasis-open.org/committees/security");
    HTTPTransportUtils.addNoCacheHeaders(transport);
    HTTPTransportUtils.setUTF8Encoding(transport);
    HTTPTransportUtils.setContentType(transport, "text/xml");
    try {
      multiEncoder.encode(context);
    } catch (MessageEncodingException e) {
      throw new RuntimeException(e);
    }
    return transport.getRequest();
  }

  private Envelope envelopeMultipleRequests(List<AuthzDecisionQuery> requests) {
    Envelope envelope = OpenSamlUtil.makeSoapEnvelope();
    Body body = OpenSamlUtil.makeSoapBody();
    body.getUnknownXMLObjects().addAll(requests);
    envelope.setBody(body);
    return envelope;
  }

  private List<Response> decodeMultipleRequests(MockHttpServletResponse response) {
    SAMLMessageContext<Response, SAMLObject, NameID> context = makeResponseContext(response);
    try {
      multiDecoder.decode(context);
    } catch (MessageDecodingException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
    XMLObject message = context.getInboundMessage();
    if (!(message instanceof Envelope)) {
      throw new IllegalArgumentException();
    }
    Envelope envelope = (Envelope) message;
    Body body = envelope.getBody();
    List<Response> result = new ArrayList<Response>();
    for (XMLObject object : body.getUnknownXMLObjects(Response.DEFAULT_ELEMENT_NAME)) {
      result.add((Response) object);
    }
    return result;
  }

  private SAMLMessageContext<SAMLObject, AuthzDecisionQuery, NameID> makeRequestContext(
      OutTransport transport) {
    SAMLMessageContext<SAMLObject, AuthzDecisionQuery, NameID> context =
        OpenSamlUtil.makeSamlMessageContext();
    context.setOutboundMessageTransport(transport);
    context.setPeerEntityEndpoint(endpoint);
    return context;
  }

  private SAMLMessageContext<Response, SAMLObject, NameID> makeResponseContext(
      MockHttpServletResponse response) {
    SAMLMessageContext<Response, SAMLObject, NameID> context =
        OpenSamlUtil.makeSamlMessageContext();
    context.setInboundMessageTransport(new HttpServletResponseClientAdapter(response));
    context.setPeerEntityEndpoint(endpoint);
    return context;
  }
}
