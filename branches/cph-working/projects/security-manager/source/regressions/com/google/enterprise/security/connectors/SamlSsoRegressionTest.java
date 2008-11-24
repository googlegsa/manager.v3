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

package com.google.enterprise.security.connectors;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SamlSsoRegressionTest extends TestCase {
  private static final Logger LOGGER = Logger.getLogger(SamlSsoRegressionTest.class.getName());

  private static final String spUrl = "http://localhost:8973/security-manager/mockserviceprovider";

  private final HttpClient userAgent;

  public SamlSsoRegressionTest(String name) {
    super(name);
    userAgent = new HttpClient();
  }

  public void testGoodCredentials() throws HttpException, IOException {
    tryCredentials("joe", "plumber");
  }

  private void tryCredentials(String username, String password) throws HttpException, IOException {
    // Initial request to service provider
    String action = parseForm(tryGet(spUrl));

    // Submit credentials-gathering form
    PostMethod method = new PostMethod(action);
    method.addParameter("username", username);
    method.addParameter("password", password);
    tryPost(method);
  }

  private String tryGet(String url) throws HttpException, IOException {
    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    int status = userAgent.executeMethod(method);
    logResponse(method);
    assertEquals("Incorrect response status code", HttpStatus.SC_OK, status);
    String body = method.getResponseBodyAsString();
    method.releaseConnection();
    return body;
  }

  private String parseForm(String body) throws IOException {
    // Parse credentials-gathering form
    HtmlCleaner cleaner = new HtmlCleaner();
    TagNode[] forms = cleaner.clean(body).getElementsByName("form", true);
    assertEquals("Wrong number of forms in response", 1, forms.length);
    {
      String method = forms[0].getAttributeByName("method");
      assertNotNull("<form> missing method attribute", method);
      assertTrue("<form> method not POST", "POST".equalsIgnoreCase(method));
    }
    String action = forms[0].getAttributeByName("action");
    assertNotNull("<form> missing action attribute", action);
    return action;
  }

  private String tryPost(PostMethod method) throws HttpException, IOException {
    method.setRequestBody(method.getParameters());
    int status = userAgent.executeMethod(method);
    logResponse(method);
    assertTrue("Incorrect response status code",
               (status == HttpStatus.SC_SEE_OTHER)
               || (status == HttpStatus.SC_MOVED_TEMPORARILY));
    Header location = method.getResponseHeader("Location");
    assertNotNull("Missing Location header in redirect", location);
    method.getResponseBody();
    method.releaseConnection();
    return tryGet(location.getValue());
  }

  private static void logResponse(HttpMethodBase method) throws IOException {
    StringWriter out = new StringWriter();
    out.write("Response:\n");
    writeResponse(method, out);
    String message = out.toString();
    out.close();
    LOGGER.log(Level.INFO, message);
  }

  private static void writeResponse(HttpMethodBase method, Writer out)  throws IOException {
    out.write(method.getStatusLine().toString());
    out.write("\n");
    for (Header h: method.getResponseHeaders()) {
      out.write(h.toString());
    }
    out.write("\n");
    out.write(method.getResponseBodyAsString());
  }
}
