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
import org.apache.xerces.parsers.SAXParser;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.w3c.dom.Element;
import org.xml.sax.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SamlAuthz handler.  Accepts a SOAP-bound batch of SAML AuthzDecisionQueries,
 * performs authz on each query with a designated backend, and replies with
 * a SOAP-bound batch of SAML Responses that each contain an
 * AuthzDecisionStatement.
 */
public class SamlAuthz extends HttpServlet {

  public static final String HARDCODED_SUBJECT_NAME = "ruth_test1";
  public static final String HARDCODED_AUTHZ_NAMESPACE = "urn:oasis:names:tc:SAML:1.0:action:ghpp";

  private static final Logger LOGGER =
      Logger.getLogger(SamlAuthz.class.getName());

  private BackEnd backend;

  public SamlAuthz() {
    this(BackEndImpl.getInstance());
  }

  /**
   * Available for testing.
   * @param backend
   */
  protected SamlAuthz(BackEnd backend) {
    super();
    this.backend = backend;
  }

  /**
   * For now, responds with "yes" for all AuthzDecisionQueries with a
   * SOAP-bound batch of SAML Responses compliant to the GSA AuthZ SPI
   * (with in batched Authz mode).
   */
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {
    handleDoPost(request, response);
  }

  /**
   * Actual post handler, factored out for the sake of unit testing.
   */
  protected void handleDoPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {

    BufferedReader reader = new BufferedReader(req.getReader());
    LOGGER.info("Content Length: " + req.getContentLength());
    LOGGER.info("Content Type moo: " + req.getContentType());
    SAXParser p = new SAXParser();
    MultiAuthzQueryDecisionHandler ch = new MultiAuthzQueryDecisionHandler();
    p.setContentHandler(ch);
    try {
      p.parse(new InputSource(reader));
    } catch (SAXException e) {
      e.printStackTrace();
    }

    List<SAMLObject> responses = new ArrayList<SAMLObject>();
    for (String url : ch.getUrls()) {
      LOGGER.info("url found: " + url);
      LOGGER.info("with id: " + ch.getIdForUrl(url));
      responses.add(generateDecisionResponse(url, ch.getIdForUrl(url), HARDCODED_SUBJECT_NAME, DecisionTypeEnumeration.PERMIT));
    }

    SOAPMessage soapMsg = soapify(responses);

    try {
      soapMsg.writeTo(res.getOutputStream());
    } catch (SOAPException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  }

  /**
   * Generate a SAML Response object for a given URL, ID string, subject, and
   * decision.  The Response object should be compliant to the GSA AuthZ SPI,
   * i.e. it contains a unique ID and an Assertion element containing an
   * AuthzDecisionStatement.
   *
   * @return a Response SAMLObject
   */
  private Response generateDecisionResponse(String url, String id, String subject, DecisionTypeEnumeration decision) {
    Response response = OpenSamlUtil.makeResponse(null, OpenSamlUtil.makeStatus(StatusCode.SUCCESS_URI));
    response.setID(id);
    Assertion assertion = OpenSamlUtil.makeAssertion(
        OpenSamlUtil.makeIssuer("localhost"),
        OpenSamlUtil.makeSubject(subject));
    AuthzDecisionStatement decisionStmt =
        OpenSamlUtil.makeAuthzDecisionStatement("GET", decision, url);
    decisionStmt.getActions().get(0).setNamespace(HARDCODED_AUTHZ_NAMESPACE);
    assertion.getAuthzDecisionStatements().add(decisionStmt);
    response.getAssertions().add(assertion);
    return response;
  }

  /**
   * For a given SAMLObject, generate a SOAP message that envelopes the
   * SAMLObject.
   * <p/>
   * TODO(con): Combine this with the soapify method from SamlArtifactResolve
   * and move it into a common utility class.
   *
   * @param samlObjects a SAMLObject
   * @return a SOAPMessage, or null on failure
   */
  private SOAPMessage soapify(List<SAMLObject> samlObjects) {
    // Get the marshaller factory
    MarshallerFactory marshallerFactory = org.opensaml.Configuration.getMarshallerFactory();

    // Get the Subject marshaller
    Marshaller marshaller = marshallerFactory.getMarshaller(samlObjects.get(0));

    try {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = msgFactory.createMessage();

      // Marshall the Subject
      for (SAMLObject samlObject : samlObjects) {

        Element respElement = marshaller.marshall(samlObject);
        soapMessage.getSOAPPart().getEnvelope().getBody()
            .addDocument(respElement.getOwnerDocument());
      }
      return soapMessage;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,
          "Failed to convert SAML object to a SOAP message:\n", e);
    }
    return null;
  }

  /**
   * ContentHandler implementation to parse an Authz SOAP message with SAX.
   * <p/>
   * At present, this class does nothing more than gather the URLs and the
   * Request IDs associated with each URL.  Eventually, we want to gather
   * user information as well to do proper authz.
   * <p/>
   * TODO(con): Make instances of this class more reusable.  At present the
   * expectation is that a new MAQDHandler is created per batch of documents.
   * Making this reusable would involve some kind of state-tracking and
   * resetting the HashMap whenever a new Document is being parsed.
   * Alternatively, trash this and use OpenSAML's SOAP libraries.
   */
  class MultiAuthzQueryDecisionHandler implements ContentHandler {

    private HashMap<String, String> urlToId;

    public MultiAuthzQueryDecisionHandler() {
      urlToId = new HashMap<String, String>();
    }

    public Set<String> getUrls() {
      return urlToId.keySet();
    }

    public String getIdForUrl(String url) {
      if (urlToId.containsKey(url)) {
        return urlToId.get(url);
      }
      return null;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceUri, String localName, String qName, Attributes attributes) throws SAXException {
      if (localName.equals("AuthzDecisionQuery")) {
        String url = attributes.getValue("", "Resource");
        String id = attributes.getValue("", "ID");
        urlToId.put(url, id);
      }
    }

    public void endElement(String namespaceUri, String localName, String qName) throws SAXException {
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

  }
}
