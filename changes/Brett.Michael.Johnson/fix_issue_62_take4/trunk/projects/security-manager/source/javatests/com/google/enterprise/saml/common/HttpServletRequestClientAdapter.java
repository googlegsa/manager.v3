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

package com.google.enterprise.saml.common;

import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpServletRequestClientAdapter implements an HTTPOutTransport interface that can be used by the
 * OpenSAML encoders.  The message created by OpenSAML is stored in a MockHttpServletRequest which
 * can be passed to a servlet as its input.
 */
public class HttpServletRequestClientAdapter implements HTTPOutTransport {

  private final MockHttpServletRequest request;
  private final ByteArrayOutputStream entityStream;

  public HttpServletRequestClientAdapter() {
    request = new MockHttpServletRequest();
    entityStream = new ByteArrayOutputStream();
  }

  public MockHttpServletRequest getRequest() {
    request.setContent(entityStream.toByteArray());
    return request;
  }

  public String getHTTPMethod() {
    return request.getMethod();
  }

  public String getHeaderValue(String name) {
    // This appears to be necessary for at least some HttpServletRequest implementations.
    if (name.equalsIgnoreCase("Content-Type")) {
      return request.getContentType();
    } else if (name.equalsIgnoreCase("Content-Length")) {
      return Integer.toString(request.getContentLength());
    } else {
      return request.getHeader(name);
    }
  }

  public int getStatusCode() {
    throw new UnsupportedOperationException();
  }

  public HTTP_VERSION getVersion() {
    throw new UnsupportedOperationException();
  }

  public void setHeader(String name, String value) {
    request.addHeader(name, value);
  }

  public void setStatusCode(int status) {
    throw new UnsupportedOperationException();
  }

  public void setVersion(HTTP_VERSION version) {
    throw new UnsupportedOperationException();
  }

  public OutputStream getOutgoingStream() {
    return entityStream;
  }

  public void sendRedirect(String location) {
    setHeader("Location", location);
    setStatusCode(303);
    try {
      Writer s = new OutputStreamWriter(getOutgoingStream());
      s.write("<html><head><title>Redirect</title></head>");
      s.write("<body><h1>303 See Other</h1><p>Redirect to ");
      s.write(location);
      s.write("</p></body></html>\n");
      s.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void addParameter(String name, String value) {
    request.addParameter(name, value);
  }

  public void setAttribute(String name, Object value) {
    request.setAttribute(name, value);
  }

  public void setCharacterEncoding(String encoding) {
    request.setCharacterEncoding(encoding);
  }

  // HTTPTransport interface:

  public String getPeerAddress() {
    return request.getRemoteAddr();
  }

  public String getPeerDomainName() {
    return request.getRemoteHost();
  }

  public String getParameterValue(String name) {
    return request.getParameter(name);
  }

  public List<String> getParameterValues(String name) {
    String[] values = request.getParameterValues(name);
    int n = values.length;
    List<String> result = new ArrayList<String>(n);
    for (int i = 0; i < n; i += 1) {
      result.add(values[i]);
    }
    return result;
  }

  // Transport interface:

  public String getCharacterEncoding() {
    return request.getCharacterEncoding();
  }

  public Object getAttribute(String name) {
    return request.getAttribute(name);
  }

  public Credential getLocalCredential() {
    return null;
  }

  public Credential getPeerCredential() {
    return null;
  }

  public boolean isAuthenticated() {
    throw new UnsupportedOperationException();
  }

  public boolean isConfidential() {
    return request.isSecure();
  }

  public boolean isIntegrityProtected() {
    return request.isSecure();
  }

  public void setAuthenticated(boolean value) {
    throw new UnsupportedOperationException();
  }

  public void setConfidential(boolean isConfidential) {
    throw new UnsupportedOperationException();
  }

  public void setIntegrityProtected(boolean isIntegrityProtected) {
    throw new UnsupportedOperationException();
  }
}
