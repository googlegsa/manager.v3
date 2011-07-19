// Copyright 2007 Google Inc.
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

import java.io.IOException;

/**
 * Utility class containing methods used to encode and construct XML for the
 * Connector Manager.
 */
public class XmlUtils {

  private static final String XML_LESS_THAN = "&lt;";
  private static final String XML_AMPERSAND = "&amp;";
  private static final String XML_QUOTE = "&quot;";

  /** Preferred over &apos; according to http://www.w3.org/TR/xhtml1/#C_16 */
  private static final String XML_APOSTROPHE = "&#39;";

  private XmlUtils() {
    // prevents instantiation
  }

  /* ******* 1.x Legacy Compliant Interface ******** */

  /**
   * Wraps an xm tag with '&lt;' and '&gt;'.
   */
  public static String xmlWrapStart(String str) {
    StringBuilder buf = new StringBuilder();
    buf.append('<');
    buf.append(str);
    buf.append('>');
    return buf.toString();
  }

  /**
   * Wraps an xml tag with '&lt;/' and '&gt;'.
   */
  public static String xmlWrapEnd(String str) {
    StringBuilder buf = new StringBuilder();
    buf.append("</");
    buf.append(str);
    buf.append(">\n");
    return buf.toString();
  }

  /**
   * XML encodes an attribute value, encoding some characters as
   * character entities, and dropping invalid control characters.
   *
   * @param attrValue the attribute value
   * @param buf the {@code StringBuffer} to which to append the attribute
   *
   * @deprecated - Use {@link #xmlAppendAttrValue(String, Appendable)}.
   */
  @Deprecated
  public static void XmlEncodeAttrValue(String attrValue, StringBuffer buf) {
    try {
      xmlAppendAttrValue(attrValue, buf);
    } catch (IOException e) {
      // This can't happen with StringBuffer.
      throw new AssertionError(e);
    }
  }

  /**
   * Used to write out an attribute for an element.  Surrounding whitespace will
   * not be added to the buffer.  The given value will be XML encoded before
   * appending to the buffer.
   * <p/>
   * For example, given attrName="foo" and attrValue="val&lt;bar" writes out:
   * <pre>foo="val&amp;lt;bar"</pre>
   *
   * @param attrName the attribute name
   * @param attrValue the attribute value
   * @param buf the {@code StringBuffer} to which to append the attribute
   *
   * @deprecated - Use {@link #xmlAppendAttr(String, String, Appendable)}.
   */
  @Deprecated
  public static void xmlAppendAttrValuePair(String attrName, String attrValue,
      StringBuffer buf) {
    buf.append(attrName);
    buf.append("=\"");
    XmlEncodeAttrValue(attrValue, buf);
    buf.append('"');
  }


  /* ******* 2.0 Appendable Interface ******** */

  /**
   * Wraps an xm tag with '&lt;' and '&gt;'.
   *
   * @param tag the XML tag to wrap with '&lt;' and '&gt;'
   * @param buf the {@code Appendable} to which to append the start tag
   * @throws IOException from {@code Appendable} (but {@code StringBuffer} or
   *        {@code StringBuilder} will never actually throw {@code IOException})
   * @since 2.4
   */
  public static void xmlAppendStartTag(String tag, Appendable buf)
      throws IOException {
    buf.append('<');
    buf.append(tag);
    buf.append('>');
  }

  /**
   * Wraps an xm tag with '&lt;/' and '&gt;'.
   *
   * @param tag the XML tag to wrap with '&lt;/' and '&gt;'
   * @param buf the {@code Appendable} to which to append the start tag
   * @throws IOException from {@code Appendable} (but {@code StringBuffer} or
   *        {@code StringBuilder} will never actually throw {@code IOException})
   * @since 2.4
   */
  public static void xmlAppendEndTag(String tag, Appendable buf)
      throws IOException {
    buf.append("</");
    buf.append(tag);
    buf.append(">\n");
  }

  /**
   * Writes out an attribute for an element.  If the attribute value
   * is non-{@code null} and non-empty, then the attribute is written out,
   * preceded by a single space.
   * The given value will be XML encoded before appending to the buffer.
   * <p/>
   * For example, given attrName="foo" and attrValue="val&lt;bar" writes out:
   * <pre>foo="val&amp;lt;bar"</pre>
   *
   * @param attrName the attribute name
   * @param attrValue the attribute value
   * @param buf the {@code Appendable} to which to append the attribute
   *        name-value pair
   * @throws IOException from {@code Appendable} (but {@code StringBuffer} or
   *        {@code StringBuilder} will never actually throw {@code IOException})
   * @since 2.4
   */
  public static void xmlAppendAttr(String attrName, String attrValue,
      Appendable buf) throws IOException {
    if (attrValue != null && attrValue.length() > 0) {
      buf.append(' ');
      buf.append(attrName);
      buf.append("=\"");
      xmlAppendAttrValue(attrValue, buf);
      buf.append('"');
    }
  }

  /**
   * XML encodes an attribute value, escaping some characters as
   * character entities, and dropping invalid control characters.
   * <p/>
   * Only four characters need to be encoded, according to the
   * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">
   * W3C XML 1.0 Specification Character definition</a>: {@code < & " '}
   * (less-than, ampersand, double-quote, single-quote).
   * <p/>
   * Actually, we could only encode one of the quote characters if
   * we knew that that was the one used to wrap the value, but we'll
   * play it safe and encode both.
   * <p/>
   * We drop invalid XML characters, following the
   * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">
   * W3C XML 1.0 Specification Character definition</a>:
   * <pre>
   * Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
   * </pre>
   * Java uses UTF-16 internally, so Unicode characters U+10000 to
   * U+10FFFF are encoded using the surrogate characters excluded
   * above, 0xD800 to 0xDFFF. So we allow just 0x09, 0x0A, 0x0D,
   * and the range 0x20 to 0xFFFD.
   *
   * @param attrValue the attribute value
   * @param buf the {@code Appendable} to which to append the attribute value
   * @throws IOException from {@code Appendable} (but {@code StringBuffer} or
   *        {@code StringBuilder} will never actually throw {@code IOException})
   * @since 2.4
   */
  public static void xmlAppendAttrValue(String attrValue, Appendable buf)
      throws IOException {
    for (int i = 0; i < attrValue.length(); i++) {
      char c = attrValue.charAt(i);
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
        case '\t':
        case '\n':
        case '\r':
          // TODO: what happens to white-space?
          buf.append(c);
          break;
        default:
          if (c >= 0x20 && c <= 0xFFFD) {
            buf.append(c);
          }
          break;
      }
    }
  }
}
