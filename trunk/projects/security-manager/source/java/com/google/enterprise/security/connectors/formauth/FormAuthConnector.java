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

package com.google.enterprise.security.connectors.formauth;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import org.apache.commons.httpclient.NameValuePair;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;
import java.net.URL;

import javax.servlet.http.Cookie;

// TODO: install certificate

public class FormAuthConnector implements Connector, Session, AuthenticationManager {

  private final String cookieName;
  
  private static final Logger LOGGER =
    Logger.getLogger(FormAuthConnector.class.getName());

  public FormAuthConnector(String cookieName) {
    this.cookieName = cookieName; // TODO for cookie cracker use
  }

  public AuthenticationResponse authenticate(AuthenticationIdentity identity) {
    String cookieVal = identity.getCookie(cookieName);
    String username = identity.getUsername();
    String password = identity.getPassword();
    
    if (username == null || password == null) {
      return null; // TODO try to crack a cookie
    }
    
    String siteUri = identity.getLoginUrl();
    if (siteUri == null) {
      LOGGER.info("Could not authenticate: null URL");
      return null;
    }
    int status;
    
    // GET siteUri, till we hit a form; fill the form, post it; get the response,
    // remember the cookie returned to us.
    StringBuffer form = new StringBuffer();
    String redirect;
    try {
      redirect = fetchLoginForm(siteUri, form);
    } catch (Exception e) {
      LOGGER.info("Could not GET login form from " + siteUri + ": " + e.toString());
      return new AuthenticationResponse(false, null);
    }
    
    Vector<NameValuePair> param = null;
    try {
      String[] action = new String[1];
      param = parseLoginForm(form, username, password, action);
      if (param == null || param.size() < 2) {
        throw new IOException("where are the input fields???");
      }
      // construct URL for form posting
      if (action[0] != null) {
        URL formUrl = new URL(redirect);
        URL newUrl = new URL(formUrl.getProtocol(), formUrl.getAuthority(), action[0]);
        redirect = newUrl.toString();
      }
    } catch (IOException e) {
      LOGGER.info("Could not parse login form: " + e.toString());
    }
  
    // submit FORM
    Vector<Cookie> cookies = null;
    try {
      cookies = submitLoginForm(redirect, param.toArray(new NameValuePair[param.size()]));
    } catch (IOException e) {
      LOGGER.info("Could not POST login form: " + e.toString());
      return new AuthenticationResponse(false, null);
    }
    
    // We are form auth, we expect to have at least one cookie 
    if (cookies != null) {
      // TODO stash these cookies to browser and session manager for impersonation
      for (Cookie cookie : cookies) {
        identity.setCookie(cookie);
      }
      return new AuthenticationResponse(true, username);
    }
    return new AuthenticationResponse(false, null);
  }

  /*
   * @returns the URL the form should be posted to
   */
  public String fetchLoginForm(String urlToFetch, StringBuffer bodyBuffer)
      throws Exception {
    int redirectCount = 0;
    URL url = new URL(urlToFetch);
    String lastRedirect = null;
    String redirected = null;
    StringBuffer redirectBuffer = new StringBuffer();
    int status = 0;
    int kMaxNumRedirectsToFollow = 4;
    
    while (true) {
      status =   CookieUtil.fetchPage("GET", url,
                                      null, // proxy,
                                      "SecMgr", null, // cookies,
                                      null, // parameters,
                                      bodyBuffer,
                                      redirectBuffer, null,
                                      null); // LOGGER

      lastRedirect = redirected;
      redirected = redirectBuffer.toString();
      if (redirected.length() > 0) {
        if (++redirectCount > kMaxNumRedirectsToFollow) {
          throw new Exception("Max num of redirects exceeded");
        }
        // prepare for another fetch.
        url = new URL(redirected);
      } else {
        break;
      }
    }

    return lastRedirect;
  }
  
  /**
   *  Pass a HTML form, in order to get the list of input fields.
   *  @param user username we have just collected from Omniform
   *  @param pass password we have just collected from Omniform
   *  @param action buffer for storing the action of the HTML form
   *  
   *  @return the param names and values suitable for POSTing.
   */

  public Vector<NameValuePair> parseLoginForm(StringBuffer form, String user, String pass,
      String[] action) throws IOException {
    Vector<NameValuePair> names = new Vector<NameValuePair>();
 
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(form.toString()).getElementsByName("form", true);
    System.out.println("Got form " + forms[0].getAttributeByName("name"));
    TagNode[] inputs = forms[0].getElementsByName("input", true);
    System.out.println("Got " + inputs.length + " input fields");
    // We are screwed if there is more than one input with type "text", or more than
    // one input with type "password".
    for (TagNode node : inputs) {
      String inputType = node.getAttributeByName("type");
      String inputName = node.getAttributeByName("name");
      System.out.println("input type is " + inputType + ", name is " + inputName);
      if (inputType.equals("text")) {
        names.add(new NameValuePair(inputName, user));
      }
      if (inputType.equals("password")) {
        names.add(new NameValuePair(inputName, pass));
      }
      if (inputType.equals("hidden")) {
        names.add(new NameValuePair(inputName, node.getAttributeByName("value")));
      }
    }
    // Some form may need to be submitted to a different URL
    action[0] = forms[0].getAttributeByName("action");
    if (action[0] != null)
      System.out.println("Action is " + action[0]);

    return names;
  }
  
  public Vector<Cookie> submitLoginForm(String loginUrl, NameValuePair[] parameters)
      throws IOException {
    int redirectCount = 0;
    URL url = new URL(loginUrl);
    StringBuffer bodyBuffer = new StringBuffer();
    StringBuffer redirectBuffer = new StringBuffer();
    int status = 0;
    int kMaxNumRedirectsToFollow = 4;
    Vector<Cookie> cookies = new Vector<Cookie>();
    String httpMethod = "POST"; // post only once, follow redirect is needed
    
    while (true) {
      status =   CookieUtil.fetchPage(httpMethod, url,
                                      null, // proxy,
                                      "SecMgr", cookies,
                                      parameters,
                                      bodyBuffer,
                                      redirectBuffer, null,
                                      null); // LOGGER

      if (cookies.size() == 0)
        System.out.println(bodyBuffer.toString());
      String redirected = redirectBuffer.toString();
      // TODO need smarter redirect logic for weirdo like CAS
      if (redirected.length() > 4) {
        if (++redirectCount > kMaxNumRedirectsToFollow) {
          throw new IOException("Max num of redirects exceeded");
        }
        // prepare for another fetch.
        url = new URL(redirected);
        httpMethod = "GET";

      } else {
        break;
      }
    }
    
    if (status != 200 && status != 302) {
      throw new IOException("Got " + status + "on POST to " + url.toString());
    }
    
    return(cookies.size() > 0 ? cookies : null);
  }
  
  public Session login() {
    return this;
  }

  public AuthenticationManager getAuthenticationManager() {
    return this;
  }

  public AuthorizationManager getAuthorizationManager() {
    throw new UnsupportedOperationException();
  }

  public TraversalManager getTraversalManager() {
    throw new UnsupportedOperationException();
  }

}
