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

import com.google.enterprise.security.manager.Context;

import org.opensaml.Configuration;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  /**
   * For testing.
   */
  @Override
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException {
    doPost(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException {
    handlePost(request, response);
  }

  protected void handlePost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException {

    Context context = Context.getInstance(getServletContext());
    BackEnd backend = context.getBackEnd();
    
    ArtifactResponse artifactResp = backend.resolveArtifact(null);

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
