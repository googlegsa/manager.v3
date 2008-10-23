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

package com.google.enterprise.connector.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/**
 * Handler for SAML login from the Google Search Appliance.
 */
public class SamlAuthN extends HttpServlet {

  private static final String GSA_ARTIFACT_HANDLER_NAME =
      "SamlArtifactConsumer";
  private static final String GSA_ARTIFACT_PARAM_NAME = "SAMLart";
  private static final String GSA_RELAY_STATE_PARAM_NAME = "RelayState";

  private static final Logger LOGGER =
      Logger.getLogger(SamlAuthN.class.getName());


  /**
   * Eventually this method will generate a login form for the user
   * that will then POST to a Submit handler (possibly within this same class)
   * that then redirects back to the GSA with an artifact and the RelayState.
   *
   * For now, we skip the login form and redirect immediately with a dummy
   * artifact.
   */
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
    handleGet(request, response);
  }

  /**
   * Redirect the GETTER back to the referring service with a generated
   * artifact and the provided RelayState.
   */
  private void handleGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
    String referer = request.getHeader("Referer");
    String relayState = request.getParameter(GSA_RELAY_STATE_PARAM_NAME);
    String urlEncodedRelayState = URLEncoder.encode(relayState, "UTF-8");
    String gsaUrl = referer.substring(0, referer.indexOf("search?"));
    String redirectUrl =
        gsaUrl + GSA_ARTIFACT_HANDLER_NAME
        + "?" + GSA_ARTIFACT_PARAM_NAME + "=" + generateArtifactString() 
        + "&" + GSA_RELAY_STATE_PARAM_NAME + "=" + urlEncodedRelayState;

    LOGGER.info("Referer: " + referer);
    LOGGER.info("RelayState: " + relayState);
    LOGGER.info("URLEncoded RelayState: " + urlEncodedRelayState);
    LOGGER.info("GSA URL: " + gsaUrl);
    LOGGER.info("Redirect URL: " + redirectUrl);

    response.setStatus(HttpServletResponse.SC_FOUND);
    response.sendRedirect(redirectUrl);
  }

  /**
   * This method should return a randomly generated artifact string that will be
   * used to reference an AuthNRequest later on.  At the moment we just hardcode
   * this since we have no real AuthN backend yet.
   *
   * @return an artifact string
   */
  private String generateArtifactString() {
    return "foo";
  }

}
