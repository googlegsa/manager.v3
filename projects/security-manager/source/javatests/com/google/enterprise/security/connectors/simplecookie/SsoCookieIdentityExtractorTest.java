// Copyright 2008 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.security.connectors.simplecookie;

import junit.framework.TestCase;

import java.net.MalformedURLException;

import javax.servlet.ServletException;

/* 
 * Tests for the {@link SsoCookieIdentityExtractor} class.
 */
public class SsoCookieIdentityExtractorTest extends TestCase {
  final String serverUrl = "localhost:5678";
  final String httpHeader = "User-Name";
  
  @Override
  protected void setUp() {
    try {
      super.setUp();
      MockSsoIdp server = new MockSsoIdp(serverUrl, httpHeader);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void testExtract() {
    SsoCookieIdentityExtractor c = new SsoCookieIdentityExtractor(serverUrl, httpHeader);
    assertEquals("fred", c.extract("SMSESSION=foxjumpovertheriver"));
    assertEquals("joeb", c.extract("SMSESSION=jackjumpoverthecandle"));
    assertNull(c.extract("SMSESSION=deadbeafdeadbeaf"));
  }

}
