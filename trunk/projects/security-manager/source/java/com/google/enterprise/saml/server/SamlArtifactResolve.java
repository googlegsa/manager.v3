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

package com.google.enterprise.saml.server;

import org.opensaml.DefaultBootstrap;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.w3c.dom.Element;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.MessageFactory;

/**
 * Handler for SAML ArtifactResolve requests.
 */
public class SamlArtifactResolve extends HttpServlet {

  private static final Logger LOGGER =
      Logger.getLogger(SamlArtifactResolve.class.getName());

  private String hostname;
  private SAMLObjectBuilder<ArtifactResponse> artifactResponseBuilder;
  private SAMLObjectBuilder<Response> samlResponseBuilder;
  private SAMLObjectBuilder<Issuer> issuerBuilder;
  private SAMLObjectBuilder<StatusCode> statusCodeBuilder;
  private SAMLObjectBuilder<Status> samlStatusBuilder;
  private SAMLObjectBuilder<Assertion> samlAssertionBuilder;
  private SAMLObjectBuilder<Subject> samlSubjectBuilder;
  private SAMLObjectBuilder<NameID> nameIDBuilder;
  private SAMLObjectBuilder<AuthnStatement> authnStmtBuilder;
  private SAMLObjectBuilder<AuthnContext> authnContextBuilder;
  private SAMLObjectBuilder<AuthnContextClassRef> authnContextClassRefBuilder;

  /**
   * Public constructor. Initializes the necessary state for later processing.
   */
  public SamlArtifactResolve() {
    super();
    hostname = "";

    try {
      resolveHostname();
      initBuilders();
    } catch (UnknownHostException e) {
      LOGGER.severe("Could not resolve local hostname.");
    } catch (ConfigurationException e) {
      LOGGER.severe("Could not initialize builders!\n");
    }
  }

  /**
   * For testing.
   */
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {

    ArtifactResponse artifactResp =
        buildArtifactResponse(StatusCode.SUCCESS_URI, hostname, "ruth_test1",
                              AuthnContext.PPT_AUTHN_CTX);
    SOAPMessage soapMsg = soapify(artifactResp);

    if (null == soapMsg) {
      LOGGER.severe("Error in constructing SOAP message, no response will be"
                    + "sent");
      return;
    }
    
    try {
      soapMsg.writeTo(response.getOutputStream());
    } catch (SOAPException e) {
      LOGGER.log(Level.SEVERE, "Error writing out SOAP message.", e);
    }

  }

  // -----------------------------------
  // helper functions for constructing SAMLObjects

  /**
   * Builds an ArtifactResponse object for a desired StatusCode, Issuer, NameID,
   * and AuthnContext.  The resulting SAMLObject conforms to the schema defined
   * by the GSA AuthN SPI at:
   * http://code.google.com/apis/searchappliance/documentation/50/authn_authz_spi.html
   *
   * @param statusCode a status string as defined in the StatusCode class
   * @param issuer the hostname of the issuer of this response; typically this
   * is the hostname of the service that is generating this response
   * @param nameId the identity of the person being authenticated in this response
   * @param authnContext a context string as defined in the AuthnContext class
   * @return an ArtifactResponse object that contains the specified information
   */
  private ArtifactResponse buildArtifactResponse(String statusCode, String issuer,
                                                 String nameId,
                                                 String authnContext) {
    ArtifactResponse artifactResp = artifactResponseBuilder.buildObject();
    artifactResp.setIssueInstant(new DateTime());
    artifactResp.setID("somerandomstring");
    artifactResp.setInResponseTo("alsorandomlooking");
    artifactResp.setIssuer(buildIssuer(issuer));
    artifactResp.setStatus(buildStatus(statusCode));
    artifactResp.setMessage(
        buildResponse(statusCode, issuer, nameId, authnContext));
    return artifactResp;
  }

  /**
   * Builds a Response object out of the specified parameters.
   */
  private Response buildResponse(String statusCode, String issuer,
                                     String nameId, String authnContext) {
    Response resp = samlResponseBuilder.buildObject();
    resp.setIssueInstant(new DateTime());
    resp.setID("blahbalh");
    resp.setVersion(SAMLVersion.VERSION_20);
    resp.setStatus(buildStatus(statusCode));
    resp.getAssertions().add(buildAssertion(issuer, nameId, authnContext));
    return resp;
  }

  /**
   * Builds an Assertion object out of the specified parameters.
   */
  private Assertion buildAssertion(String issuer, String nameId,
                                   String authnContext) {
    Assertion assertion = samlAssertionBuilder.buildObject();
    assertion.setIssueInstant(new DateTime());
    assertion.setID("blahbalh2");
    assertion.setIssuer(buildIssuer(issuer));
    assertion.setSubject(buildSubject(nameId));
    assertion.getAuthnStatements().add(buildAuthnStatement(authnContext));
    return assertion;
  }

  /**
   * Builds an AuthnStatement object out of the specified AuthnContext.
   * @param context
   */
  private AuthnStatement buildAuthnStatement(String context) {
    AuthnStatement authnStmt = authnStmtBuilder.buildObject();
    AuthnContext authnCxt = authnContextBuilder.buildObject();
    AuthnContextClassRef authnContextClassRef =
        authnContextClassRefBuilder.buildObject();
    authnContextClassRef.setAuthnContextClassRef(context);
    authnCxt.setAuthnContextClassRef(authnContextClassRef);
    authnStmt.setAuthnContext(authnCxt);
    authnStmt.setAuthnInstant(new DateTime());
    return authnStmt;
  }

  /**
   * Builds a Status object out of a given StatusCode string.
   * @param statusCode a status string as defined in the StatusCode class
   */
  private Status buildStatus(String statusCode) {
    StatusCode sc = statusCodeBuilder.buildObject();
    sc.setValue(statusCode);
    Status status = samlStatusBuilder.buildObject();
    status.setStatusCode(sc);
    return status;
  }

  /**
   * Builds an Issuer object out of a given Issuer string.
   * @param issuerString a string representing the issuer of a SAMLObject
   */
  private Issuer buildIssuer(String issuerString) {
    Issuer issuer = issuerBuilder.buildObject();
    issuer.setValue(issuerString);
    return issuer;
  }

  /**
   * Builds a Subject object out of a given name.
   * @param subjectName the name of the subject to be created
   */
  private Subject buildSubject(String subjectName) {
    Subject samlSubject = samlSubjectBuilder.buildObject();
    NameID nameId = nameIDBuilder.buildObject();
    nameId.setValue(subjectName);
    samlSubject.setNameID(nameId);
    return samlSubject;
  }

  // -----------------------------------
  // init functions

  /**
   * Attempt to get and set the local hostname from which this servlet is run.
   *
   * @throws UnknownHostException on failure
   */
  private void resolveHostname() throws UnknownHostException {
    InetAddress addr = InetAddress.getLocalHost();
    hostname = addr.getHostName();
  }

  /**
   * Initialize OpenSAML object builder factories.
   *
   * @throws ConfigurationException if builders could not be made
   */
  private void initBuilders() throws ConfigurationException {
    DefaultBootstrap.bootstrap();
    XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
    artifactResponseBuilder
        = (SAMLObjectBuilder<ArtifactResponse>)
        builderFactory.getBuilder(ArtifactResponse.DEFAULT_ELEMENT_NAME);
    samlResponseBuilder
        = (SAMLObjectBuilder<Response>)
        builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
    issuerBuilder =
        (SAMLObjectBuilder<Issuer>) builderFactory
            .getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
    statusCodeBuilder =
        (SAMLObjectBuilder<StatusCode>) builderFactory
            .getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
    samlStatusBuilder
        = (SAMLObjectBuilder<Status>)
        builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
    samlAssertionBuilder =
        (SAMLObjectBuilder<Assertion>) builderFactory
            .getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
    samlSubjectBuilder =
        (SAMLObjectBuilder<Subject>) builderFactory
            .getBuilder(Subject.DEFAULT_ELEMENT_NAME);
    nameIDBuilder =
        (SAMLObjectBuilder<NameID>) builderFactory
            .getBuilder(NameID.DEFAULT_ELEMENT_NAME);
    authnStmtBuilder =
        (SAMLObjectBuilder<AuthnStatement>) builderFactory.getBuilder(
            AuthnStatement.DEFAULT_ELEMENT_NAME);
    authnContextBuilder =
        (SAMLObjectBuilder<AuthnContext>) builderFactory.getBuilder(
            AuthnContext.DEFAULT_ELEMENT_NAME);
    authnContextClassRefBuilder =
        (SAMLObjectBuilder<AuthnContextClassRef>) builderFactory.getBuilder(
            AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
  }

  /**
   * For a given SAMLObject, generate a SOAP message that envelopes the
   * SAMLObject.
   *
   * @param samlObject a SAMLObject
   * @return a SOAPMessage, or null on failure
   */
  private SOAPMessage soapify(SAMLObject samlObject) {
    // Get the marshaller factory
    MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();

    // Get the Subject marshaller
    Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);

    // Marshall the Subject
    Element artifactRespElement = null;
    try {
      artifactRespElement = marshaller.marshall(samlObject);
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = msgFactory.createMessage();
      soapMessage.getSOAPPart().getEnvelope().getBody()
          .addDocument(artifactRespElement.getOwnerDocument());
      return soapMessage;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,
                 "Failed to convert SAML object to a SOAP message:\n", e);
    }
    return null;
  }
}
