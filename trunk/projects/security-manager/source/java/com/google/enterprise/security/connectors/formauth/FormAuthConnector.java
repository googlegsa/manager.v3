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

import com.google.enterprise.common.HttpExchange;
import com.google.enterprise.common.SecurityManagerUtil;
import com.google.enterprise.common.StringPair;
import com.google.enterprise.connector.common.CookieUtil;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.VerificationStatus;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

// TODO: install certificate

public class FormAuthConnector implements Connector, Session, AuthenticationManager {

  @SuppressWarnings("unused")
  private final String cookieName;

  private static final Logger LOGGER =
    Logger.getLogger(FormAuthConnector.class.getName());

  public FormAuthConnector(String cookieName) {
    this.cookieName = cookieName; // TODO for cookie cracker use
  }

  public AuthenticationResponse authenticate(AuthenticationIdentity raw) {
    SecAuthnIdentity identity = SecAuthnIdentity.class.cast(raw);
    String username = identity.getUsername();
    String password = identity.getPassword();

    if (username == null || password == null) {
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return null;
    }

    String sampleUrl = identity.getSampleUrl();
    LOGGER.info("Trying to authenticate against " + sampleUrl);

    Collection<Cookie> originalCookies = identity.getCookies();
    Vector<Cookie> cookies = copyCookies(originalCookies);
    logCookies(LOGGER, "original cookies", cookies);

    // GET sampleUrl, following redirects until we hit a form; fill the form, post it; get
    // the response, remember the cookie returned to us.
    StringBuffer form = new StringBuffer();
    String formUrl;
    try {
      formUrl = fetchLoginForm(sampleUrl, form, cookies);
    } catch (IOException e) {
      LOGGER.info("Could not GET login form from " + sampleUrl + ": " + e.toString());
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return new AuthenticationResponse(false, null);
    }
    logCookies(LOGGER, "after fetchLoginForm", cookies);

    List<StringPair> param;
    String postUrl;
    try {
      String[] action = new String[1];
      param = parseLoginForm(form, username, password, action);
      if (param == null || param.size() < 2) {
        throw new IOException("where are the input fields???");
      }
      // construct URL for form posting
      if (action[0] == null) {
        postUrl = formUrl;
      } else {
        postUrl = new URL(new URL(formUrl), action[0]).toString();
      }
    } catch (IOException e) {
      LOGGER.info("Could not parse login form: " + e.toString());
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return new AuthenticationResponse(false, null);
    }

    LOGGER.info("POST to: " + postUrl);
    try {
      int status = submitLoginForm(postUrl, param, cookies);
      if (status != 200) {
        LOGGER.info("Authentication failure status: " + status);
        identity.setVerificationStatus(VerificationStatus.REFUTED);
        return new AuthenticationResponse(false, null);
      }
    } catch (IOException e) {
      LOGGER.info("Could not POST login form: " + e.toString());
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return new AuthenticationResponse(false, null);
    }
    logCookies(LOGGER, "after submitLoginForm", cookies);

    // We are form auth, we expect to have at least one cookie
    if (!anyCookiesChanged(cookies, originalCookies)) {
      LOGGER.info("Authentication status OK but no cookie");
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return new AuthenticationResponse(false, null);
    }

    // Transfer all retrieved cookies to the identity
    for (Cookie cookie: cookies) {
      identity.addCookie(cookie);
    }
    identity.setVerificationStatus(VerificationStatus.VERIFIED);
    return new AuthenticationResponse(true, username);
  }

  /**
   * Gets a login form from a site.
   *
   * Follows redirects until it gets to the form or exceeds a hard-coded limit.
   *
   * @param sampleUrl A sample URL protected by forms authentication.
   * @param bodyBuffer A buffer in which the form is stored.
   * @param cookies A container to hold any cookies collected during the operation.
   * @return The URL of the form.
   */
  private String fetchLoginForm(String sampleUrl, StringBuffer bodyBuffer, Vector<Cookie> cookies)
      throws IOException {
    int redirectCount = 0;
    URL url = new URL(sampleUrl);
    String lastRedirect;
    String redirected = sampleUrl;
    StringBuffer redirectBuffer = new StringBuffer();
    int kMaxNumRedirectsToFollow = 4;

    while (true) {
      CookieUtil.fetchPage(SecurityManagerUtil.getHttpClient().getExchange(url),
                           url,
                           null, // proxy,
                           "SecMgr", cookies,
                           bodyBuffer,
                           redirectBuffer, null,
                           null); // LOGGER
      lastRedirect = redirected;
      redirected = redirectBuffer.toString();
      if (redirected.length() > 0) {
        if (++redirectCount > kMaxNumRedirectsToFollow) {
          throw new IOException("Max num of redirects exceeded");
        }
        // prepare for another fetch.
        url = new URL(url, redirected);
        redirected = url.toString();
      } else {
        break;
      }
    }
    return lastRedirect;
  }

  /**
   * Parse an HTML form into its component fields.
   *
   * @param user The username we are going to submit to the form.
   * @param pass The password we are going to submit to the form.
   * @param action Will hold the form's "action" on return.
   * @return The form field names and values suitable for POSTing.
   */
  private List<StringPair> parseLoginForm(StringBuffer form, String user, String pass,
      String[] action) throws IOException {
    LOGGER.info("Form to parse: " + form.toString());
    List<StringPair> names = new ArrayList<StringPair>();

    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(form.toString()).getElementsByName("form", true);
    if (forms.length < 1) {
      LOGGER.info("No <form> elements in input");
      return names;
    }
    StringBuffer buffer = new StringBuffer("Got form");
    {
      String name = forms[0].getAttributeByName("name");
      if (name != null) {
        buffer.append("; name=");
        buffer.append(name);
      }
    }
    TagNode[] inputs = forms[0].getElementsByName("input", true);
    buffer.append("; ");
    buffer.append(inputs.length);
    buffer.append(" input fields");
    // We are screwed if there is more than one input with type "text", or more than
    // one input with type "password".
    for (TagNode node : inputs) {
      String inputType = node.getAttributeByName("type");
      // Fill in default type if none specified.
      if (inputType == null || inputType.isEmpty()) {
        inputType = "text";
      }
      String inputName = node.getAttributeByName("name");
      buffer.append("; type=");
      buffer.append(inputType);
      buffer.append(", name=");
      buffer.append(inputName);
      if (inputType.equalsIgnoreCase("text")) {
        names.add(new StringPair(inputName, user));
      } else if (inputType.equalsIgnoreCase("password")) {
        names.add(new StringPair(inputName, pass));
      } else if (inputType.equalsIgnoreCase("hidden")) {
        names.add(new StringPair(inputName, node.getAttributeByName("value")));
      }
    }
    // Some forms lack the required "action" attribute
    action[0] = forms[0].getAttributeByName("action");
    if (action[0] != null) {
      buffer.append("; action=");
      buffer.append(action[0]);
    }
    LOGGER.info(buffer.toString());
    return names;
  }

  /**
   * Submit a filled-in form to a site.
   *
   * Follows any redirects generated by the submission.
   *
   * @param loginUrl The URL to post the values to.
   * @param parameters The parameters to be posted.
   * @param cookies The cookies being submitted; new cookies are added here.
   * @return An HTTP status code for the operation.
   */
  private int submitLoginForm(
      String loginUrl, List<StringPair> parameters, Vector<Cookie> cookies)
      throws IOException {
    int redirectCount = 0;
    URL url = new URL(loginUrl);
    StringBuffer bodyBuffer = new StringBuffer();
    StringBuffer redirectBuffer = new StringBuffer();
    int status = 0;
    int kMaxNumRedirectsToFollow = 4;
    // post only once, follow redirect if needed
    HttpExchange exchange = SecurityManagerUtil.getHttpClient().postExchange(url, parameters);

    while (true) {
      status = CookieUtil.fetchPage(exchange, url,
                                    null, // proxy,
                                    "SecMgr", cookies,
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
        url = new URL(url, redirected);
        exchange = SecurityManagerUtil.getHttpClient().getExchange(url);
      } else {
        break;
      }
    }
    return status;
  }

  private static Vector<Cookie> copyCookies(Collection<Cookie> cookies) {
    Vector<Cookie> result = new Vector<Cookie>(cookies.size());
    for (Cookie c: cookies) {
      result.add(Cookie.class.cast(c.clone()));
    }
    return result;
  }

  private static boolean anyCookiesChanged(Collection<Cookie> newCookies,
                                           Collection<Cookie> oldCookies) {
    for (Cookie c: newCookies) {
      if (!containsCookie(oldCookies, c, true)) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsCookie(Collection<Cookie> cookies, Cookie cookie,
                                        boolean considerValue) {
    for (Cookie c: cookies) {
      if (compareCookies(c, cookie, considerValue)) {
        return true;
      }
    }
    return false;
  }

  private static boolean compareCookies(Cookie c1, Cookie c2, boolean considerValue) {
    return
        stringEquals(c1.getName(), c2.getName())
        && stringEquals(c1.getDomain(), c2.getDomain())
        && considerValue ? stringEquals(c1.getValue(), c2.getValue()) : true;
  }

  private static boolean stringEquals(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
  }

  private static void logCookies(Logger LOGGER, String tag, Collection<Cookie> cookies) {
    String value = CookieUtil.setCookieHeaderValue(cookies, false);
    if (value == null) {
      value = "(none)";
    }
    LOGGER.info(tag + ": " + value);
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
