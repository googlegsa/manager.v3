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
import java.io.PrintWriter;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
  @Override
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<head>");
    out.println("<title>Please Login</title>");
    out.println("</head>");
    out.println("<body>");
    out.print("<form action=\"");
    out.print(request.getRequestURI());
    out.print("?Referer=");
    // Carve out the base URL - POST handler will use this parameter to
    // construct the URL of Artifact Consumer.
    String referer = request.getHeader("Referer");
    out.print(referer.substring(0, referer.indexOf("search")));
    out.print("&");
    out.print(request.getQueryString());
    out.println("\" method=POST>");
    out.println("User Name:");
    out.println("<input type=text size=20 name=username>");
    out.println("<br>");
    out.println("Password:");
    out.println("<input type=text size=20 name=password>");
    out.println("<br>");
    out.println("<input type=submit>");
    out.println("</form>");
    out.println("</body>");
    out.println("</html>");
  }

  /**
   * Extract username/password from login form, lookup the user then generate
   * SAML artifact.
   */
  @Override
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException {
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
