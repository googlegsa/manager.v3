// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.spi;

import junit.framework.TestCase;

import java.io.IOException;

public class XmlUtilsTest extends TestCase {

  public void testXmlAppendAttrValue() throws IOException {
    StringBuilder builder = new StringBuilder();
    String xmlString = "one&two<three>four'five\"";
    XmlUtils.xmlAppendAttrValue(xmlString, builder);
    assertEquals("one&amp;two&lt;three>four&#39;five&quot;",
        builder.toString());
    // Clear the builder.
    builder.setLength(0);
    String invalidString = "begin\u0009\u0010\u0020\r\n";
    XmlUtils.xmlAppendAttrValue(invalidString, builder);
    assertEquals("begin\u0009\u0020\r\n", builder.toString());
  }
}
