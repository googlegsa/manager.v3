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

import com.google.enterprise.common.HttpClientInterface;
import com.google.enterprise.common.StringPair;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

// TODO: install certificate

public class FormAuthConnector implements Connector, Session, AuthenticationManager {

  private final HttpClientInterface httpClient;
  @SuppressWarnings("unused")
  private final String cookieName;

  private static final Logger LOGGER =
    Logger.getLogger(FormAuthConnector.class.getName());

  public FormAuthConnector(HttpClientInterface httpClient, String cookieName) {
    this.httpClient = httpClient;
    this.cookieName = cookieName; // TODO for cookie cracker use
  }

  public AuthenticationResponse authenticate(AuthenticationIdentity identity) {
    // String cookieVal = identity.getCookie(cookieName);
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

    Vector<Cookie> originalCookies = getCookies(identity);
    Vector<Cookie> cookies = copyCookies(originalCookies);

    // GET siteUri, till we hit a form; fill the form, post it; get the response,
    // remember the cookie returned to us.
    StringBuffer form = new StringBuffer();
    String redirect;
    try {
      redirect = fetchLoginForm(siteUri, form, cookies);
    } catch (Exception e) {
      LOGGER.info("Could not GET login form from " + siteUri + ": " + e.toString());
      return new AuthenticationResponse(false, null);
    }

    List<StringPair> param;
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
      return new AuthenticationResponse(false, null);
    }

    // submit FORM
    try {
      submitLoginForm(redirect, param, cookies);
    } catch (IOException e) {
      LOGGER.info("Could not POST login form: " + e.toString());
      return new AuthenticationResponse(false, null);
    }

    // We are form auth, we expect to have at least one cookie
    if (!anyCookiesChanged(cookies, originalCookies)) {
      return new AuthenticationResponse(false, null);
    }

    // Save cookies back to the identity.  Save only newly added cookies; we don't want to
    // overwrite an IP-bound cookie currently held by the user agent.
    for (Cookie cookie: cookies) {
      if (!containsCookie(originalCookies, cookie, false)) {
        identity.setCookie(cookie);
      }
    }
    return new AuthenticationResponse(true, username);
  }

  /*
   * @returns the URL the form should be posted to
   */
  private String fetchLoginForm(String urlToFetch, StringBuffer bodyBuffer, Vector<Cookie> cookies)
      throws Exception {
    int redirectCount = 0;
    URL url = new URL(urlToFetch);
    String lastRedirect = null;
    String redirected = null;
    StringBuffer redirectBuffer = new StringBuffer();
    int kMaxNumRedirectsToFollow = 4;

    while (true) {
      CookieUtil.fetchPage(httpClient, "GET", url,
                           null, // proxy,
                           "SecMgr", cookies,
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

  private List<StringPair> parseLoginForm(StringBuffer form, String user, String pass,
      String[] action) throws IOException {
    List<StringPair> names = new ArrayList<StringPair>();

    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(form.toString()).getElementsByName("form", true);
    if (forms.length < 1) {
      return names;
    }
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
        names.add(new StringPair(inputName, user));
      }
      if (inputType.equals("password")) {
        names.add(new StringPair(inputName, pass));
      }
      if (inputType.equals("hidden")) {
        names.add(new StringPair(inputName, node.getAttributeByName("value")));
      }
    }
    // Some form may need to be submitted to a different URL
    action[0] = forms[0].getAttributeByName("action");
    if (action[0] != null)
      System.out.println("Action is " + action[0]);

    return names;
  }

  private void submitLoginForm(
      String loginUrl, List<StringPair> parameters, Vector<Cookie> cookies)
      throws IOException {
    int redirectCount = 0;
    URL url = new URL(loginUrl);
    StringBuffer bodyBuffer = new StringBuffer();
    StringBuffer redirectBuffer = new StringBuffer();
    int status = 0;
    int kMaxNumRedirectsToFollow = 4;
    String httpMethod = "POST"; // post only once, follow redirect is needed

    while (true) {
      status = CookieUtil.fetchPage(httpClient, httpMethod, url,
                                    null, // proxy,
                                    "SecMgr", cookies,
                                    parameters,
                                    bodyBuffer,
                                    redirectBuffer, null,
                                    null); // LOGGER
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
  }

  // This has to re-create all the cookies because AuthenticationIdentity doesn't provide
  // a way to get at the originals.
  private Vector<Cookie> getCookies(AuthenticationIdentity identity) {
    Vector<Cookie> result = new Vector<Cookie>();
    @SuppressWarnings("unchecked")
        Set<String> names = identity.getCookieNames();
    for (String name: names) {
      result.add(new Cookie(name, identity.getCookie(name)));
    }
    return result;
  }

  private Vector<Cookie> copyCookies(Vector<Cookie> cookies) {
    Vector<Cookie> result = new Vector<Cookie>(cookies.size());
    for (Cookie c: cookies) {
      result.add(copyCookie(c));
    }
    return result;
  }

  private Cookie copyCookie(Cookie cookie) {
    Cookie result = new Cookie(cookie.getName(), cookie.getValue());
    result.setComment(cookie.getComment());
    result.setDomain(cookie.getDomain());
    result.setMaxAge(cookie.getMaxAge());
    result.setPath(cookie.getPath());
    result.setSecure(cookie.getSecure());
    result.setVersion(cookie.getVersion());
    return result;
  }

  private boolean anyCookiesChanged(Vector<Cookie> newCookies, Vector<Cookie> oldCookies) {
    for (Cookie c: newCookies) {
      if (!containsCookie(oldCookies, c, true)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsCookie(Vector<Cookie> cookies, Cookie cookie, boolean considerValue) {
    for (Cookie c: cookies) {
      if (compareCookies(c, cookie, considerValue)) {
        return true;
      }
    }
    return false;
  }

  private boolean compareCookies(Cookie c1, Cookie c2, boolean considerValue) {
    return
        stringEquals(c1.getName(), c2.getName())
        && stringEquals(c1.getDomain(), c2.getDomain())
        && considerValue ? stringEquals(c1.getValue(), c2.getValue()) : true;
  }

  private boolean stringEquals(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
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
