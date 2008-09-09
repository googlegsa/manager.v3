// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Tests TestConnectivity servlet class.
 *
 */
public class TestConnectivityTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.servlet.TestConnectivity#handleDoGet(java.io.PrintWriter)}.
   */
  public void testHandleDoGet() {
    String expectedResult = ServletUtil.XML_SIMPLE_RESPONSE;
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    TestConnectivity.handleDoGet(out);
    out.flush();
    StringBuffer result = writer.getBuffer();
    ConnectorTestUtils.removeManagerVersion(result);
    Assert.assertEquals(StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }

}
