// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.connector.security.connectors;

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
import java.util.logging.Logger;

public class SamlSsoRegressionTest extends TestCase {
  private static final Logger LOGGER = Logger.getLogger(SamlSsoRegressionTest.class.getName());
  private static final String SP_URL =
      "http://localhost:8973/security-manager/mockserviceprovider";

  private HttpClient userAgent;

  @Override
  public void setUp() throws Exception {
    userAgent = new HttpClient();
  }

  public void testGoodCredentials() throws HttpException, IOException {
    String action = parseForm(tryGet(SP_URL, HttpStatus.SC_OK));
    PostMethod method = new PostMethod(action);
    method.addParameter("uADG1", "joe");
    method.addParameter("pwADG1", "plumber");
    int status = tryPost(method);
    assertTrue("Incorrect response status code",
               (status == HttpStatus.SC_SEE_OTHER || status == HttpStatus.SC_MOVED_TEMPORARILY));
    Header h = method.getResponseHeader("location");
    assertNotNull(h);
    tryGet(h.getValue(), HttpStatus.SC_OK);
  }

  public void testBadCredentials() throws HttpException, IOException {
    String action = parseForm(tryGet(SP_URL, HttpStatus.SC_OK));
    PostMethod method = new PostMethod(action);
    method.addParameter("uADG1", "joe");
    method.addParameter("pwADG1", "biden");
    assertEquals("Incorrect response status code", HttpStatus.SC_OK, tryPost(method));
  }

  private String tryGet(String url, int expectedStatus) throws HttpException, IOException {
    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    logRequest(method);
    int status = userAgent.executeMethod(method);
    String body = method.getResponseBodyAsString();
    logResponse(method);
    method.releaseConnection();
    assertEquals("Incorrect response status code", expectedStatus, status);
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

  private int tryPost(PostMethod method) throws HttpException, IOException {
    method.setRequestBody(method.getParameters());
    logRequest(method);
    int status = userAgent.executeMethod(method);
    method.getResponseBodyAsString();
    logResponse(method);
    method.releaseConnection();
    return status;
  }

  private static void logRequest(HttpMethodBase method) throws IOException {
    StringWriter out = new StringWriter();
    out.write("Request:\n");
    writeRequest(method, out);
    String message = out.toString();
    out.close();
    LOGGER.info(message);
  }

  private static void writeRequest(HttpMethodBase method, Writer out) throws IOException {
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
    LOGGER.info(message);
  }

  private static void writeResponse(HttpMethodBase method, Writer out) throws IOException {
    out.write(method.getStatusLine().toString());
    out.write("\n");
    for (Header h: method.getResponseHeaders()) {
      out.write(h.toString());
    }
    out.write("\n");
    out.write(method.getResponseBodyAsString());
  }
}
