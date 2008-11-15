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

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.GsaConstants;
import com.google.enterprise.saml.common.PostableHttpServlet;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler for SAML login from the Google Search Appliance.
 */
public class SamlAuthn extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {

  private static final long serialVersionUID = 1L;
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

    /**
     * If this request carries cookie, we use registered security connectors to
     * figure out user identity from the cookie.
     */
    Cookie[] jar = request.getCookies();
    if (jar != null) {
      Map<String, String> cookieJar = new HashMap<String, String>(jar.length);
      for (int i = 0; i < jar.length; i++) {
        cookieJar.put(jar[i].getName(), jar[i].getValue());
      }
 
      if (handleAuthn(response, gsaUrlString,
                      request.getParameter(GsaConstants.GSA_RELAY_STATE_PARAM_NAME),
                      null, null, cookieJar) != null)
        return;
    }
    
    String formHtml = "<html>\n<head>\n"
        +  "<title>Please Login</title>\n</head>\n" + "<body>\n"
        + "<form action=\"" + request.getRequestURI() + "?Referer="
        + gsaUrlString + "&" + request.getQueryString() +"\" method=POST>\n"
        + "User Name: " + "<input type=text size=20 name=username><br>\n"
        + "Password: " + "<input type=text size=20 name=password><br>\n"
        + "<input type=submit>\n" + "</form>\n</body>\n</html>";

    response.getWriter().print(formHtml);
  }

  @SuppressWarnings("unchecked")
  private HttpServletResponse handleAuthn(HttpServletResponse response,
      String gsaUrlString, String relay, String username, String password,
      Map<String, String> cookieJar) throws IOException {
    
    ServletContext servletContext = this.getServletContext();
    ConnectorManager manager = (ConnectorManager) 
      Context.getInstance(servletContext).getManager();
    BackEnd backend = manager.getBackEnd();
    List<ConnectorStatus> connList = manager.getConnectorStatuses();
    if (connList == null || connList.isEmpty()) {
      instantiateConnector(manager);
      connList = manager.getConnectorStatuses();
    }
    for (ConnectorStatus connStatus: connList) {
      String connectorName = connStatus.getName();
      LOGGER.info("Got security plug-in " + connectorName);
      
      AuthenticationResponse authnResponse = 
        manager.authenticate(connectorName, username, password, cookieJar);
      if ((authnResponse != null) && authnResponse.isValid()) {
        // TODO make sure authnResponse has subject in BackEnd
        String subject = (authnResponse.getData() == null) ? username :
          authnResponse.getData();
        String redirectUrl = backend.loginRedirect(gsaUrlString, relay, subject);           

        if (redirectUrl == null) {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          response.sendError(404);
          return response;
        }
        
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(redirectUrl);
        return response;
      }
    }
    
    return null;
  }

  /**
   * Extract username/password from login form, lookup the user then generate
   * SAML artifact.
   */
  @Override
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if (username.length() < 1 || password.length() < 1) {
      PrintWriter out = response.getWriter();
      out.println("<title>Error</title>");
      out.println("No user name or password entered");
      out.close();
      return;
    }

    LOGGER.info("gsaUrlString: " + request.getParameter("Referer"));
    if (handleAuthn(response, 
          request.getParameter("Referer"),
          request.getParameter(GsaConstants.GSA_RELAY_STATE_PARAM_NAME),
          username, password, null) == null) {
      // The user is not authenticated, respond with DENY
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.sendError(404);
    }
  }

  // TODO get rid of this when we have a way of configuring plug-ins
  private void instantiateConnector(ConnectorManager manager) {
    String connectorName = "Lei";
    String connectorType = "CookieConnector";
    String language = "en";
    
    Map<String, String> configData;   
    configData = ImmutableMap.of("CookieName", "SMSESSION",
        "ServerUrl", "http://gama.corp.google.com/user1/ssoAgent.asp",
        "HttpHeaderName", "User-Name");
    try {
      manager.setConnectorConfig(connectorName, connectorType,
                                 configData, language, false);
    } catch (ConnectorNotFoundException e) {
      LOGGER.info("ConnectorNotFound: " + e.toString());
    } catch (InstantiatorException e) {
      LOGGER.info("Instantiator: " + e.toString());
    } catch (PersistentStoreException e) {
      LOGGER.info("PersistentStore: " + e.toString());
    }
  }
  
}
