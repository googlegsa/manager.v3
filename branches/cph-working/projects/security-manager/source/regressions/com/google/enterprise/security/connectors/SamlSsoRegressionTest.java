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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

public class SamlSsoRegressionTest extends TestCase {

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
    GetMethod method = new GetMethod(spUrl);
    int status = userAgent.executeMethod(method);
    assertEquals("Incorrect response status code", HttpStatus.SC_OK, status);
    byte[] entity = method.getResponseBody();
    method.releaseConnection();
  }
}
