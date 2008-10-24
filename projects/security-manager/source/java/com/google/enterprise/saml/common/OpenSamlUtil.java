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

import java.security.NoSuchAlgorithmException;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactBuilder;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactBuilderFactory;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactType0004;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;

// TODO(cph): The result of objectBuilderFactory.getBuilder() must be cast to the correct type, but
// this generates an unchecked warning.
@SuppressWarnings("unchecked")
public class OpenSamlUtil {

  public static final String GOOGLE_PROVIDER_NAME = "Google Search Appliance";
  public static final String GOOGLE_ISSUER = "google.com";

  private static final XMLObjectBuilderFactory objectBuilderFactory;
  private static final SAML2ArtifactBuilderFactory artifactBuilderFactory;

  private static final SAMLObjectBuilder<ArtifactResponse> artifactResponseBuilder;
  private static final SAMLObjectBuilder<Assertion> assertionBuilder;
  private static final SAMLObjectBuilder<AuthnContext> authnContextBuilder;
  private static final SAMLObjectBuilder<AuthnContextClassRef> authnContextClassRefBuilder;
  private static final SAMLObjectBuilder<AuthnRequest> authnRequestBuilder;
  private static final SAMLObjectBuilder<AuthnStatement> authnStatementBuilder;
  private static final SAMLObjectBuilder<Issuer> issuerBuilder;
  private static final SAMLObjectBuilder<NameID> nameIDBuilder;
  private static final SAMLObjectBuilder<NameIDPolicy> nameIdPolicyBuilder;
  private static final SAMLObjectBuilder<Response> responseBuilder;
  private static final SAMLObjectBuilder<SingleSignOnService> singleSignOnServiceBuilder;
  private static final SAMLObjectBuilder<Status> statusBuilder;
  private static final SAMLObjectBuilder<StatusCode> statusCodeBuilder;
  private static final SAMLObjectBuilder<Subject> subjectBuilder;

  private static final SAML2ArtifactBuilder<SAML2ArtifactType0004> artifactBuilder;

  private static final IdentifierGenerator idGenerator;

  static {
    try {
      DefaultBootstrap.bootstrap();
    } catch (ConfigurationException e) {
      throw new IllegalStateException(e);
    }

    objectBuilderFactory = Configuration.getBuilderFactory();
    artifactBuilderFactory = Configuration.getSAML2ArtifactBuilderFactory();

    // This block of statements is what requires the @SuppressWarnings("unchecked") above.
    artifactResponseBuilder = (SAMLObjectBuilder<ArtifactResponse>) objectBuilderFactory
        .getBuilder(ArtifactResponse.DEFAULT_ELEMENT_NAME);
    assertionBuilder = (SAMLObjectBuilder<Assertion>) objectBuilderFactory
        .getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
    authnContextBuilder = (SAMLObjectBuilder<AuthnContext>) objectBuilderFactory
        .getBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
    authnContextClassRefBuilder = (SAMLObjectBuilder<AuthnContextClassRef>) objectBuilderFactory
        .getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
    authnRequestBuilder = (SAMLObjectBuilder<AuthnRequest>) objectBuilderFactory
        .getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
    authnStatementBuilder = (SAMLObjectBuilder<AuthnStatement>) objectBuilderFactory
        .getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
    issuerBuilder = (SAMLObjectBuilder<Issuer>) objectBuilderFactory
        .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
    nameIDBuilder = (SAMLObjectBuilder<NameID>) objectBuilderFactory
        .getBuilder(NameID.DEFAULT_ELEMENT_NAME);
    nameIdPolicyBuilder = (SAMLObjectBuilder<NameIDPolicy>) objectBuilderFactory
        .getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
    responseBuilder = (SAMLObjectBuilder<Response>) objectBuilderFactory
        .getBuilder(Response.DEFAULT_ELEMENT_NAME);
    singleSignOnServiceBuilder = (SAMLObjectBuilder<SingleSignOnService>) objectBuilderFactory
        .getBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME);
    statusBuilder = (SAMLObjectBuilder<Status>) objectBuilderFactory
        .getBuilder(Status.DEFAULT_ELEMENT_NAME);
    statusCodeBuilder = (SAMLObjectBuilder<StatusCode>) objectBuilderFactory
        .getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
    subjectBuilder = (SAMLObjectBuilder<Subject>) objectBuilderFactory
        .getBuilder(Subject.DEFAULT_ELEMENT_NAME);

    artifactBuilder = artifactBuilderFactory
        .getArtifactBuilder(SAML2ArtifactType0004.TYPE_CODE);

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

  private static void initializeResponse(StatusResponseType response,
      Status status, RequestAbstractType request) {
    response.setID(generateIdentifier());
    response.setVersion(SAMLVersion.VERSION_20);
    response.setIssueInstant(new DateTime());
    response.setStatus(status);
    if (request != null) {
      response.setInResponseTo(request.getID());
    }
  }

  public static ArtifactResponse makeArtifactResponse(
      RequestAbstractType request, Status status, SAMLObject message) {
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

  public static AuthnStatement makeAuthnStatement(AuthnContext context,
      DateTime authnInstant) {
    AuthnStatement statement = authnStatementBuilder.buildObject();
    statement.setAuthnContext(context);
    statement.setAuthnInstant(authnInstant);
    return statement;
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

  public static Endpoint makeSingleSignOnService(String binding, String location) {
    Endpoint endpoint = singleSignOnServiceBuilder.buildObject();
    endpoint.setBinding(binding);
    endpoint.setLocation(location);
    return endpoint;
  }

  public static Endpoint makeSingleSignOnService(String binding,
      String location, String responseLocation) {
    Endpoint endpoint = makeSingleSignOnService(binding, location);
    endpoint.setResponseLocation(responseLocation);
    return endpoint;
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

  public static SAML2ArtifactType0004 makeArtifact(byte[] artifact) {
    return artifactBuilder.buildArtifact(artifact);
  }

  public static SAML2ArtifactType0004 makeArtifact(
      SAMLMessageContext requestContext) {
    return artifactBuilder.buildArtifact(requestContext);
  }

  public static String generateIdentifier() {
    return idGenerator.generateIdentifier();
  }
}
