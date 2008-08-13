// Copyright 2007 Google Inc. All Rights Reserved.
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

/**
 * Utility class containing methods used to encode and construct XML for the 
 * Connector Manager.
 */
public class XmlUtils {

  private static final String XML_LESS_THAN = "&lt;";
  private static final String XML_AMPERSAND = "&amp;";
  private static final String XML_QUOTE = "&quot;";
  private static final String XML_APOSTROPHE = "&apos;";

  private XmlUtils() {
    // prevents instantiation
  }

  public static void XmlEncodeAttrValue(String val, StringBuffer buf) {
    for (int i = 0; i < val.length(); i++) {
      char c = val.charAt(i);
      /**
       * Only these characters need to be encoded, according to
       * http://www.w3.org/TR/REC-xml/#NT-AttValue. Actually, we could only
       * encode one of the quote characters if we knew that that was the one
       * used to wrap the value, but we'll play it safe and encode both. TODO:
       * what happens to white-space?
       */
      switch (c) {
      case '<':
        buf.append(XML_LESS_THAN);
        break;
      case '&':
        buf.append(XML_AMPERSAND);
        break;
      case '"':
        buf.append(XML_QUOTE);
        break;
      case '\'':
        buf.append(XML_APOSTROPHE);
        break;
      default:
        buf.append(c);
        break;
      }
    }
  }

  /*
   * Wraps an xm tag with < and >.
   */
  public static String xmlWrapStart(String str) {
    StringBuffer buf = new StringBuffer();
    buf.append("<");
    buf.append(str);
    buf.append(">");
    return buf.toString();
  }

  /*
   * Wraps an xml tag with </ and >.
   */
  public static String xmlWrapEnd(String str) {
    StringBuffer buf = new StringBuffer();
    buf.append("</");
    buf.append(str);
    buf.append(">\n");
    return buf.toString();
  }

  /**
   * Used to write out an attribute for an element.  Surrounding whitespace will
   * not be added to the buffer.  The given value will be XML Encoded before 
   * appending to the buffer.
   * 
   * <p>For example, given attrName="foo" and attrValue="val&lt;bar" writes out:
   * <pre>foo="val&amp;lt;bar"</pre>
   * @param attrName the attribute name.
   * @param attrValue the attribute value.
   * @param buf the StringBuffer to append the attribute.
   */
  public static void xmlAppendAttrValuePair(String attrName, String attrValue,
      StringBuffer buf) {
    buf.append(attrName);
    buf.append("=\"");
    XmlEncodeAttrValue(attrValue, buf);
    buf.append("\"");
  }

}
