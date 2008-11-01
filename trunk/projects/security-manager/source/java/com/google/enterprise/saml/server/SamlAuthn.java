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
import com.google.enterprise.security.manager.Context;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler for SAML login from the Google Search Appliance.
 */
public class SamlAuthn extends HttpServlet {

  private static final Logger LOGGER =
      Logger.getLogger(SamlAuthn.class.getName());

  /**
   * Eventually this method will generate a login form for the user
   * that will then POST to a Submit handler (possibly within this same class)
   * that then redirects back to the GSA with an artifact and the RelayState.
   *
   * For now, we skip the login form and redirect immediately with a dummy
   * artifact.
   */
  @Override
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException {
    String gsaUrlString = request.getHeader("Referer").substring(
        0, request.getHeader("Referer").indexOf("search?"));    
    response.setContentType("text/html");

    LOGGER.info("gsaUrlString: " + gsaUrlString);

    String formHtml = "<html>\n<head>\n"
        +  "<title>Please Login</title>\n</head>\n" + "<body>\n"
        + "<form action=\"" + request.getRequestURI() + "?Referer="
        + gsaUrlString + "&" + request.getQueryString() +"\" method=POST>\n"
        + "User Name: " + "<input type=text size=20 name=username><br>\n"
        + "Password: " + "<input type=text size=20 name=password><br>\n"
        + "<input type=submit>\n" + "</form>\n</body>\n</html>";

    response.getWriter().print(formHtml);
  }

  /**
   * Extract username/password from login form, lookup the user then generate
   * SAML artifact.
   */
  @Override
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException {
    handlePost(request, response);
  }
  
  /**
   * Redirect the GETTER back to the referring service with a generated
   * artifact and the provided RelayState.
   */
  private void handlePost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();

    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if (username.length() < 1 || password.length() < 1) {
      out.println("<title>Error</title>");
      out.println("No user name or password entered");
      out.close();
      return;
    }

    Context context = Context.getInstance(getServletContext());
    BackEnd backend = context.getBackEnd();

    // TODO: implement user look-up
    String redirectUrl = backend.loginRedirect(
        request.getParameter("Referer"),
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
