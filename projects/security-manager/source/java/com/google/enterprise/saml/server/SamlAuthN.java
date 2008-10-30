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

import com.google.enterprise.saml.common.GsaConstants;

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

  private static final Logger LOGGER =
      Logger.getLogger(SamlAuthN.class.getName());

  private BackEnd backend;

  public SamlAuthN() {
    this(BackEndImpl.getInstance());
  }

  /**
   * Available for testing.
   * @param backend
   */
  protected SamlAuthN(BackEnd backend) {
    super();
    this.backend = backend;
  }

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

    String redirectUrl = backend.loginRedirect(request.getHeader("Referer"),
        request.getParameter(GsaConstants.GSA_RELAY_STATE_PARAM_NAME));

    if (redirectUrl == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.sendError(404);
      return;
    }
    
    response.setStatus(HttpServletResponse.SC_FOUND);
    response.sendRedirect(redirectUrl);

  }
  
}
