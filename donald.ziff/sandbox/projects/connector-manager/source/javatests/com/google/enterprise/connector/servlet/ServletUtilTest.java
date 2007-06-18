// Copyright (C) 2006 Google Inc.
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

import junit.framework.Assert;
import junit.framework.TestCase;

public class ServletUtilTest extends TestCase {

  public void testPrependCmPrefix() {
    onePrependTest("foo name=\"bar\" bar", "foo name=\"CM_bar\" bar");
    onePrependTest("name=\"bar\"", "name=\"CM_bar\"");
    onePrependTest("name=\"bar\" name=\"baz\"", "name=\"CM_bar\" name=\"CM_baz\"");
    onePrependTest("name='bar' name=\"baz\"", "name='CM_bar' name=\"CM_baz\"");
    onePrependTest("name = 'bar'   name   =  \"baz\"",
        "name = 'CM_bar'   name   =  \"CM_baz\"");
  }

  private void onePrependTest(String original, String expected) {
    String result = ServletUtil.prependCmPrefix(original);
    Assert.assertEquals(expected, result);
    Assert.assertEquals(original, ServletUtil.stripCmPrefix(result));
  }
}
