// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.util;

import junit.framework.TestCase;

import org.w3c.dom.Element;

/**
 * Unit Test for XmlParseUtil.
 */
public class XmlParseUtilTest extends TestCase {

  /** Tests getOptionalElementByTagName */
  public void testGetOptionalElementByTagName() throws Exception {
    String xml = "<root><foo>blah</foo><bar></bar><baz/><nest><nested>oops"
                 + "</nested></nest></root>";
    Element root = XmlParseUtil.parseAndGetRootElement(xml, "root");
    assertNotNull(root);
    assertEquals("blah", XmlParseUtil.getOptionalElementByTagName(root, "foo"));
    assertEquals("", XmlParseUtil.getOptionalElementByTagName(root, "bar"));
    assertEquals("", XmlParseUtil.getOptionalElementByTagName(root, "baz"));
    assertNull(XmlParseUtil.getOptionalElementByTagName(root, "nonexist"));
    assertEquals("", XmlParseUtil.getOptionalElementByTagName(root, "nest"));
  }
}

