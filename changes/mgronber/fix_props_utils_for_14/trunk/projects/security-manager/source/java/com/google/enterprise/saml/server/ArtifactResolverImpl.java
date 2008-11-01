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

package com.google.enterprise.saml.server;

import org.opensaml.saml2.core.*;
import com.google.enterprise.saml.common.OpenSamlUtil;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Artifact resolver for the AuthN on the Security Manager.
 *
 * At present this resolver generates a canned response with a hard-coded
 * identity.
 */
public class ArtifactResolverImpl implements ArtifactResolver {

  private static final Logger LOGGER =
      Logger.getLogger(ArtifactResolverImpl.class.getName());

  private String hostname;

  public ArtifactResponse resolve(ArtifactResolve request) {
    try {
      resolveHostname();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    return buildArtifactResponse(StatusCode.SUCCESS_URI, hostname, "ruth_test1",
        AuthnContext.PPT_AUTHN_CTX);
  }

  // -----------------------------------
  // helper functions for constructing SAMLObjects

  /**
   * Builds an ArtifactResponse object for a desired StatusCode, Issuer, NameID,
   * and AuthnContext.  The resulting SAMLObject conforms to the schema defined
   * by the GSA AuthN SPI at:
   * http://code.google.com/apis/searchappliance/documentation/50/authn_authz_spi.html
   *
   * @param statusCode   a status string as defined in the StatusCode class
   * @param issuer       the hostname of the issuer of this response; typically this
   *                     is the hostname of the service that is generating this response
   * @param nameId       the identity of the person being authenticated in this response
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
   * @throws java.net.UnknownHostException on failure
   */
  private void resolveHostname() throws UnknownHostException {
    InetAddress addr = InetAddress.getLocalHost();
    hostname = addr.getHostName();
  }

}
