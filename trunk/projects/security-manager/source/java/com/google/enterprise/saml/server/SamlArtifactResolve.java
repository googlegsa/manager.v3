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

import com.google.enterprise.saml.common.OpenSamlUtil;

import org.opensaml.Configuration;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Handler for SAML ArtifactResolve requests.
 */
public class SamlArtifactResolve extends HttpServlet {

  private static final Logger LOGGER =
      Logger.getLogger(SamlArtifactResolve.class.getName());

  private String hostname;

  /**
   * Public constructor. Initializes the necessary state for later processing.
   */
  public SamlArtifactResolve() {
    super();
    hostname = "";

    try {
      resolveHostname();
    } catch (UnknownHostException e) {
      LOGGER.severe("Could not resolve local hostname.");
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
  private ArtifactResponse buildArtifactResponse(String statusCode,
                                                 String issuer,
                                                 String nameId,
                                                 String authnContext) {
    ArtifactResponse artifactResp =
        OpenSamlUtil.makeArtifactResponse(null,
                                          OpenSamlUtil.makeStatus(statusCode),
                                          buildResponse(statusCode, issuer,
                                                        nameId, authnContext));
    artifactResp.setIssuer(OpenSamlUtil.makeIssuer(issuer));
    return artifactResp;
  }

  /**
   * Builds a Response object out of the specified parameters.
   */
  private Response buildResponse(String statusCode, String issuer,
                                 String nameId, String authnContext) {
    Response resp =
        OpenSamlUtil.makeResponse(null, OpenSamlUtil.makeStatus(statusCode));
    resp.getAssertions().add(buildAssertion(issuer, nameId, authnContext));
    return resp;
  }

  /**
   * Builds an Assertion object out of the specified parameters.
   */
  private Assertion buildAssertion(String issuer, String nameId,
                                   String authnContext) {
    Assertion assertion =
        OpenSamlUtil.makeAssertion(OpenSamlUtil.makeIssuer(issuer),
                                   OpenSamlUtil.makeSubject(nameId));
    assertion.getAuthnStatements()
        .add(OpenSamlUtil.makeAuthnStatement(authnContext));
    return assertion;
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
