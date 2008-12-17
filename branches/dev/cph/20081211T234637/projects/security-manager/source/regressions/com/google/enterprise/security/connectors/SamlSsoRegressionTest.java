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
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.ByteArrayOutputStream;
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
    // Initial request to service provider
    String action = parseForm(tryGet(spUrl, HttpStatus.SC_OK));

    // Submit credentials-gathering form into the first credential group
    // which is authenticated against fake-login.corp that accepts everything
    PostMethod method2 = new PostMethod(action);
    method2.addParameter("u0", "voyager");
    method2.addParameter("pw0", "plumer");
    tryPost(method2, HttpStatus.SC_OK);
  }

  public void tryBadCredentials() throws HttpException, IOException {
    // Initial request to service provider
    String action = parseForm(tryGet(spUrl, HttpStatus.SC_OK));

    // Submit credentials-gathering form into the second credential group
    // which doesnot accept joe/plumber
    PostMethod method2 = new PostMethod(action);
    method2.addParameter("u1", "joe");
    method2.addParameter("pw1", "plumer");
    tryPost(method2, HttpStatus.SC_OK);
  }

  private String tryGet(String url, int expectedStatus) throws HttpException, IOException {
    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    logRequest(method);
    int status = userAgent.executeMethod(method);
    logResponse(method);
    assertEquals("Incorrect response status code", expectedStatus, status);
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

  private String tryPost(PostMethod method, int expectedStatus) throws HttpException, IOException {
    method.setRequestBody(method.getParameters());
    logRequest(method);
    int status = userAgent.executeMethod(method);
    logResponse(method);
    assertTrue("Incorrect response status code",
               (status == HttpStatus.SC_SEE_OTHER)
               || (status == HttpStatus.SC_MOVED_TEMPORARILY));
    Header location = method.getResponseHeader("Location");
    assertNotNull("Missing Location header in redirect", location);
    method.getResponseBody();
    method.releaseConnection();
    return tryGet(location.getValue(), expectedStatus);
  }

  private static void logRequest(HttpMethodBase method) throws IOException {
    StringWriter out = new StringWriter();
    out.write("Request:\n");
    writeRequest(method, out);
    String message = out.toString();
    out.close();
    LOGGER.log(Level.INFO, message);
  }

  private static void writeRequest(HttpMethodBase method, Writer out)  throws IOException {
    out.write(method.getName());
    out.write(" ");
    out.write(method.getURI().toString());
    out.write(" ");
    out.write(method.getParams().getVersion().toString());
    out.write("\n");
    for (Header h: method.getRequestHeaders()) {
      out.write(h.toString());
    }
    out.write("\n");
    if (method instanceof EntityEnclosingMethod) {
      EntityEnclosingMethod eem = (EntityEnclosingMethod) method;
      RequestEntity re = eem.getRequestEntity();
      if (re.isRepeatable()) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        re.writeRequest(os);
        out.write(os.toString("UTF-8"));
      }
    }
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
