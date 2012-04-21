// Copyright 2006 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Tests TestConnectivity servlet class.
 */
public class TestConnectivityTest extends TestCase {

  /**
   * Test method for
   * {@link TestConnectivity#doGet(HttpServletRequest, HttpServletResponse)}.
   */
  public void testDoGet() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/testConnectivity");
    MockHttpServletResponse res = new MockHttpServletResponse();
    new TestConnectivity().doGet(req, res);
    checkResponse(new StringBuffer(res.getContentAsString()), true);
  }

  /**
   * Test method for {@link TestConnectivity#handleDoGet(java.io.PrintWriter)}.
   */
  public void testHandleDoGet() {
    checkHandleDoGet(false);
    checkHandleDoGet(true);
  }

  private void checkHandleDoGet(boolean reqIsFeedHost) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    TestConnectivity.handleDoGet(out, reqIsFeedHost);
    out.flush();
    checkResponse(writer.getBuffer(), reqIsFeedHost);
    out.close();
  }

  private void checkResponse(StringBuffer response, boolean reqIsFeedHost) {
    String expectedResponse = "<CmResponse>\n"
        + "  <StatusCode>" + ((reqIsFeedHost)? "0" : "5501") + "</StatusCode>\n"
        + "  <StatusId>0</StatusId>\n"
        + "</CmResponse>\n";

    ConnectorTestUtils.removeManagerVersion(response);
    assertEquals(StringUtils.normalizeNewlines(expectedResponse),
        StringUtils.normalizeNewlines(response.toString()));
  }
}
