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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * HttpServletResponseClientAdapter implements an HTTPInTransport interface that can be used by the
 * OpenSAML decoders.  It takes a MockHttpServletResponse object which represents the message to be
 * processed by OpenSAML.
 */
public class HttpServletResponseClientAdapter implements HTTPInTransport {

  private final MockHttpServletResponse response;
  private final InputStream entityStream;

  public HttpServletResponseClientAdapter(MockHttpServletResponse response) {
    this.response = response;
    entityStream = new ByteArrayInputStream(response.getContentAsByteArray());
  }

  public String getPeerAddress() {
    throw new IllegalStateException();
  }

  public String getPeerDomainName() {
    throw new IllegalStateException();
  }

  public InputStream getIncomingStream() {
    return entityStream;
  }

  public Object getAttribute(String name) {
    throw new IllegalStateException();
  }

  public String getCharacterEncoding() {
    return response.getCharacterEncoding();
  }

  public Credential getLocalCredential() {
    return null;
  }

  public Credential getPeerCredential() {
    return null;
  }

  public boolean isAuthenticated() {
    throw new IllegalStateException();
  }

  public boolean isConfidential() {
    throw new IllegalStateException();
  }

  public boolean isIntegrityProtected() {
    throw new IllegalStateException();
  }

  public void setAuthenticated(boolean value) {
    throw new IllegalStateException();
  }

  public void setConfidential(boolean value) {
    throw new IllegalStateException();
  }

  public void setIntegrityProtected(boolean value) {
    throw new IllegalStateException();
  }

  public String getHTTPMethod() {
    throw new IllegalStateException();
  }

  public String getHeaderValue(String name) {
    if (name.equalsIgnoreCase("Content-Type")) {
      return response.getContentType();
    } else if (name.equalsIgnoreCase("Content-Length")) {
      return Integer.toString(response.getContentLength());
    } else {
      return (String) response.getHeader(name);
    }
  }

  public String getParameterValue(String name) {
    throw new IllegalStateException();
  }

  public List<String> getParameterValues(String name) {
    throw new IllegalStateException();
  }

  public int getStatusCode() {
    return response.getStatus();
  }

  public HTTP_VERSION getVersion() {
    throw new IllegalStateException();
  }
}
