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

package com.google.enterprise.saml.common;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.binding.artifact.AbstractSAML2Artifact;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactBuilderFactory;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactType0004;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactType0004Builder;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.soap.common.SOAPObject;
import org.opensaml.ws.soap.common.SOAPObjectBuilder;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;

import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;

public class OpenSamlUtil {

  public static final String GOOGLE_PROVIDER_NAME = "Google Search Appliance";
  public static final String GOOGLE_ISSUER = "google.com";

  static {
    try {
      DefaultBootstrap.bootstrap();
    } catch (ConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  private static final XMLObjectBuilderFactory objectBuilderFactory =
      Configuration.getBuilderFactory();
  private static final SAML2ArtifactBuilderFactory artifactObjectBuilderFactory =
      Configuration.getSAML2ArtifactBuilderFactory();

  // TODO(cph): @SuppressWarnings is needed because objectBuilderFactory.getBuilder() returns a
  // supertype of the actual type.
  @SuppressWarnings("unchecked")
  private static <T extends SAMLObject> SAMLObjectBuilder<T> makeSamlObjectBuilder(QName name) {
    return (SAMLObjectBuilder<T>) objectBuilderFactory.getBuilder(name);
  }

  private static final SAMLObjectBuilder<Action> actionBuilder =
      makeSamlObjectBuilder(Action.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<Artifact> artifactBuilder =
      makeSamlObjectBuilder(Artifact.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<ArtifactResolutionService> artifactResolutionServiceBuilder =
      makeSamlObjectBuilder(ArtifactResolutionService.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<ArtifactResolve> artifactResolveBuilder =
      makeSamlObjectBuilder(ArtifactResolve.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<ArtifactResponse> artifactResponseBuilder =
      makeSamlObjectBuilder(ArtifactResponse.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<Assertion> assertionBuilder =
      makeSamlObjectBuilder(Assertion.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthnContext> authnContextBuilder =
      makeSamlObjectBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthnContextClassRef> authnContextClassRefBuilder =
      makeSamlObjectBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthnRequest> authnRequestBuilder =
      makeSamlObjectBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthnStatement> authnStatementBuilder =
      makeSamlObjectBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthzDecisionQuery> authzDecisionQueryBuilder =
      makeSamlObjectBuilder(AuthzDecisionQuery.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthzDecisionStatement> authzDecisionStatementBuilder =
      makeSamlObjectBuilder(AuthzDecisionStatement.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<AuthzService> authzServiceBuilder =
      makeSamlObjectBuilder(AuthzService.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<Issuer> issuerBuilder =
      makeSamlObjectBuilder(Issuer.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<NameID> nameIDBuilder =
      makeSamlObjectBuilder(NameID.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<NameIDPolicy> nameIdPolicyBuilder =
      makeSamlObjectBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<Response> responseBuilder =
      makeSamlObjectBuilder(Response.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<SingleSignOnService> singleSignOnServiceBuilder =
      makeSamlObjectBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<Status> statusBuilder =
      makeSamlObjectBuilder(Status.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<StatusCode> statusCodeBuilder =
      makeSamlObjectBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
  private static final SAMLObjectBuilder<Subject> subjectBuilder =
      makeSamlObjectBuilder(Subject.DEFAULT_ELEMENT_NAME);

  // TODO(cph): @SuppressWarnings is needed because objectBuilderFactory.getBuilder() returns a
  // supertype of the actual type.
  @SuppressWarnings("unchecked")
  private static <T extends SOAPObject> SOAPObjectBuilder<T> makeSoapObjectBuilder(QName name) {
    return (SOAPObjectBuilder<T>) objectBuilderFactory.getBuilder(name);
  }

  private static final SOAPObjectBuilder<Body> soapBodyBuilder =
      makeSoapObjectBuilder(Body.DEFAULT_ELEMENT_NAME);
  private static final SOAPObjectBuilder<Envelope> soapEnvelopeBuilder =
      makeSoapObjectBuilder(Envelope.DEFAULT_ELEMENT_NAME);

  private static final SAML2ArtifactType0004Builder artifactObjectBuilder =
      (SAML2ArtifactType0004Builder) artifactObjectBuilderFactory
          .getArtifactBuilder(SAML2ArtifactType0004.TYPE_CODE);

  private static final IdentifierGenerator idGenerator;
  static {
    try {
      idGenerator = new SecureRandomIdentifierGenerator();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  // Non-instantiable class.
  private OpenSamlUtil() {
  }

  private static void initializeRequest(RequestAbstractType request) {
    request.setID(generateIdentifier());
    request.setVersion(SAMLVersion.VERSION_20);
    request.setIssueInstant(new DateTime());
  }

  private static void initializeResponse(StatusResponseType response, Status status,
      RequestAbstractType request) {
    response.setID(generateIdentifier());
    response.setVersion(SAMLVersion.VERSION_20);
    response.setIssueInstant(new DateTime());
    response.setStatus(status);
    if (request != null) {
      response.setInResponseTo(request.getID());
    }
  }

  public static Action makeAction(String name) {
    Action action = actionBuilder.buildObject();
    action.setAction(name);
    return action;
  }

  public static Artifact makeArtifact(String value) {
    Artifact element = artifactBuilder.buildObject();
    element.setArtifact(value);
    return element;
  }

  public static ArtifactResolutionService makeArtifactResolutionService(String binding,
      String location) {
    ArtifactResolutionService service = artifactResolutionServiceBuilder.buildObject();
    service.setBinding(binding);
    service.setLocation(location);
    return service;
  }

  public static ArtifactResolutionService makeArtifactResolutionService(String binding,
      String location, String responseLocation) {
    ArtifactResolutionService service = makeArtifactResolutionService(binding, location);
    service.setResponseLocation(responseLocation);
    return service;
  }

  public static ArtifactResolve makeArtifactResolve(Artifact artifact) {
    ArtifactResolve request = artifactResolveBuilder.buildObject();
    initializeRequest(request);
    request.setArtifact(artifact);
    return request;
  }

  public static ArtifactResolve makeArtifactResolve(String value) {
    return makeArtifactResolve(makeArtifact(value));
  }

  public static ArtifactResponse makeArtifactResponse(RequestAbstractType request, Status status,
      SAMLObject message) {
    ArtifactResponse artifactResponse = artifactResponseBuilder.buildObject();
    initializeResponse(artifactResponse, status, request);
    artifactResponse.setMessage(message);
    return artifactResponse;
  }

  public static Assertion makeAssertion(Issuer issuer) {
    Assertion assertion = assertionBuilder.buildObject();
    assertion.setID(generateIdentifier());
    assertion.setVersion(SAMLVersion.VERSION_20);
    assertion.setIssueInstant(new DateTime());
    assertion.setIssuer(issuer);
    return assertion;
  }

  public static Assertion makeAssertion(Issuer issuer, Subject subject) {
    Assertion assertion = makeAssertion(issuer);
    assertion.setSubject(subject);
    return assertion;
  }

  public static AuthnContext makeAuthnContext(AuthnContextClassRef classRef) {
    AuthnContext context = authnContextBuilder.buildObject();
    context.setAuthnContextClassRef(classRef);
    return context;
  }

  public static AuthnContext makeAuthnContext(String uri) {
    return makeAuthnContext(makeAuthnContextClassRef(uri));
  }

  public static AuthnContextClassRef makeAuthnContextClassRef(String uri) {
    AuthnContextClassRef classRef = authnContextClassRefBuilder.buildObject();
    classRef.setAuthnContextClassRef(uri);
    return classRef;
  }

  public static AuthnRequest makeAuthnRequest() {
    AuthnRequest request = authnRequestBuilder.buildObject();
    initializeRequest(request);
    return request;
  }

  public static AuthnStatement makeAuthnStatement(String uri) {
    return makeAuthnStatement(makeAuthnContext(uri), new DateTime());
  }

  public static AuthnStatement makeAuthnStatement(AuthnContext context) {
    return makeAuthnStatement(context, new DateTime());
  }

  public static AuthnStatement makeAuthnStatement(AuthnContext context, DateTime authnInstant) {
    AuthnStatement statement = authnStatementBuilder.buildObject();
    statement.setAuthnContext(context);
    statement.setAuthnInstant(authnInstant);
    return statement;
  }

  public static AuthzDecisionQuery makeAuthzDecisionQuery(Subject subject, String resource,
      Action action) {
    AuthzDecisionQuery query = authzDecisionQueryBuilder.buildObject();
    query.setSubject(subject);
    query.setResource(resource);
    query.getActions().add(action);
    return query;
  }

  public static AuthzDecisionQuery makeAuthzDecisionQuery(String name, String resource,
      String action) {
    return makeAuthzDecisionQuery(makeSubject(name), resource, makeAction(action));
  }

  public static AuthzDecisionStatement makeAuthzDecisionStatement(String resource,
      DecisionTypeEnumeration decision, Action action) {
    AuthzDecisionStatement statement = authzDecisionStatementBuilder.buildObject();
    statement.setResource(resource);
    statement.setDecision(decision);
    statement.getActions().add(action);
    return statement;
  }

  public static AuthzDecisionStatement makeAuthzDecisionStatement(String resource,
      DecisionTypeEnumeration decision, String action) {
    return makeAuthzDecisionStatement(resource, decision, makeAction(action));
  }

  public static AuthzService makeAuthzService(String binding, String location) {
    AuthzService service = authzServiceBuilder.buildObject();
    service.setBinding(binding);
    service.setLocation(location);
    return service;
  }

  public static AuthzService makeAuthzService(String binding, String location,
      String responseLocation) {
    AuthzService service = makeAuthzService(binding, location);
    service.setResponseLocation(responseLocation);
    return service;
  }

  public static Issuer makeIssuer(String name) {
    Issuer issuer = issuerBuilder.buildObject();
    issuer.setValue(name);
    return issuer;
  }

  public static NameID makeNameId(String name) {
    NameID id = nameIDBuilder.buildObject();
    id.setValue(name);
    return id;
  }

  public static NameIDPolicy makeNameIdPolicy(String format) {
    NameIDPolicy idPolicy = nameIdPolicyBuilder.buildObject();
    idPolicy.setFormat(format);
    return idPolicy;
  }

  public static Response makeResponse(RequestAbstractType request, Status status) {
    Response response = responseBuilder.buildObject();
    initializeResponse(response, status, request);
    return response;
  }

  public static SingleSignOnService makeSingleSignOnService(String binding, String location) {
    SingleSignOnService service = singleSignOnServiceBuilder.buildObject();
    service.setBinding(binding);
    service.setLocation(location);
    return service;
  }

  public static SingleSignOnService makeSingleSignOnService(String binding, String location,
      String responseLocation) {
    SingleSignOnService service = makeSingleSignOnService(binding, location);
    service.setResponseLocation(responseLocation);
    return service;
  }

  public static Status makeStatus(StatusCode code) {
    Status status = statusBuilder.buildObject();
    status.setStatusCode(code);
    return status;
  }

  public static Status makeStatus(String value) {
    return makeStatus(makeStatusCode(value));
  }

  public static StatusCode makeStatusCode(String value) {
    StatusCode code = statusCodeBuilder.buildObject();
    code.setValue(value);
    return code;
  }

  public static Subject makeSubject() {
    return subjectBuilder.buildObject();
  }

  public static Subject makeSubject(NameID id) {
    Subject samlSubject = makeSubject();
    samlSubject.setNameID(id);
    return samlSubject;
  }

  public static Subject makeSubject(String name) {
    return makeSubject(makeNameId(name));
  }

  public static Body makeSoapBody() {
    return soapBodyBuilder.buildObject();
  }

  public static Envelope makeSoapEnvelope() {
    return soapEnvelopeBuilder.buildObject();
  }

  public static AbstractSAML2Artifact newArtifactObject(
      SAMLMessageContext<SAMLObject, SAMLObject, NameID> requestContext) {
    return artifactObjectBuilder.buildArtifact(requestContext);
  }

  public static String generateIdentifier() {
    return idGenerator.generateIdentifier();
  }

  public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject> SAMLMessageContext<TI, TO, TN> makeSamlMessageContext() {
    return new BasicSAMLMessageContext<TI, TO, TN>();
  }
}
