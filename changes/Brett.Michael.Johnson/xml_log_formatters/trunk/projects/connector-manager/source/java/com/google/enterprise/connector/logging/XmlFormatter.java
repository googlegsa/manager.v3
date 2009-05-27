// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/* TODO: Add MDC logging. */
public class XmlFormatter extends Formatter {
  private static final String NL = System.getProperty("line.separator");

  // Constants used by the java.util.logging compatible formatter.
  private final String recordTag = "<record>";
  private final int recordTagLen = recordTag.length();

  // Constants used by the log4j compatible formatter.
  private static final String eventTag = "log4j:event";
  private static final String locationTag = "log4j:locationInfo";
  private static final String messageTag = "log4j:message";
  private static final String ndcTag = "log4j:NDC";
  private static final String throwableTag = "log4j:throwable";
  private static final String cdataStart = "<![CDATA[";
  private static final String cdataEnd = "]]>";

  // Which underlying formatter are we using? Either the log4j-compatible
  // formatter or the java.util.logging compatible formatter.
  private Formatter formatter;

  public XmlFormatter() {
    // Load the format from logging.properties.
    // If requested format is "log4j" or "chainsaw", generate log4j-style XML,
    // otherwise generate java.util.logging.XMLFormat-style XML.
    String propName = getClass().getName() + ".format";
    String format = LogManager.getLogManager().getProperty(propName);

    if (format != null && format.trim().length() > 0) {
      format = format.trim().toLowerCase();
      if ("log4j".equals(format) || "chainsaw".equals(format)) {
        formatter = new Log4jXmlFormatter();
      } else {
        formatter = new UtilLoggingXmlFormatter();
      }
    }
  }

  @Override
  public String format(LogRecord record) {
    return formatter.format(record);
  }

  /**
   * A log formatter resembling java.util.logging.XMLFormatter,
   * adding NDC Logging capabilities. The output is easier to read
   * than log4jFormatter, but NDC data doesn't work in Chainsaw.
   */
  private class UtilLoggingXmlFormatter extends java.util.logging.XMLFormatter {
    public String format(LogRecord record) {
      String output = super.format(record);
      String ndc = NDC.peek();
      if (ndc != null && ndc.length() > 0) {
        int point = output.indexOf(recordTag);
        if (point >= 0) {
          point += recordTagLen;
          if (ndc.indexOf('&') >= 0) {
            ndc = ndc.replaceAll("&", "&amp;");
          }
          if (ndc.indexOf('<') >= 0) {
            ndc = ndc.replaceAll("<", "&lt;");
          }
          if (ndc.indexOf('>') >= 0) {
            ndc = ndc.replaceAll(">", "&gt;");
          }
          output = output.substring(0, point) + NL + "  <ndc>" + ndc + "</ndc>"
              + output.substring(point);
        }
      }
      return output;
    }
  }


  /**
   * A log formatter resembling the Log4j XMLFormatter,
   * based upon java.util.logging.LogRecord rather than
   * log4j LoggerEvents.  This log Formatter generates
   * XML output that resembles log4j output, so that it
   * is viewable using Chainsaw.
   */
  private class Log4jXmlFormatter extends SimpleFormatter {
    public String format(LogRecord record) {
      StringBuilder buf = new StringBuilder();

      // Start event element.
      buf.append('<').append(eventTag);
      appendAttr(buf, "logger", record.getLoggerName());
      appendAttr(buf, "timestamp", Long.toString(record.getMillis()));
      appendAttr(buf, "level", record.getLevel().getName());
      appendAttr(buf, "thread", Thread.currentThread().getName());
      buf.append('>').append(NL);

      // Add NDC element.
      String ndc = NDC.peek();
      if (ndc != null && ndc.length() > 0) {
        appendCdata(buf, ndcTag, ndc);
      }

      // Add location element.
      buf.append('<').append(locationTag);
      appendAttr(buf, "class", record.getSourceClassName());
      appendAttr(buf, "method", record.getSourceMethodName());
      buf.append(" file=\"\" line=\"\"/>").append(NL);

      // Add message element.
      appendCdata(buf, messageTag, super.formatMessage(record));

      // Add throwable element.
      Throwable thrown = record.getThrown();
      if (thrown != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        thrown.printStackTrace(pw);
        pw.flush();
        appendCdata(buf, throwableTag, sw.toString());
        pw.close();
      }

      // Close the event element.
      buf.append("</").append(eventTag).append('>').append(NL).append(NL);

      return buf.toString();
    }
  }

  /**
   * Append the supplied attribute to the XML element.
   *
   * @param buf {@code StringBuilder} we are appending to.
   * @param attr The attribute name.
   * @param value The attribute value.
   */
  private static void appendAttr(StringBuilder buf, String attr, String value) {
    if (value != null && value.length() > 0) {
      buf.append(' ').append(attr).append("=\"");
      appendAttrValue(buf, value);
      buf.append('"');
    }
  }

  /**
   * XML encodes an attribute value, escaping some characters as
   * character entities, and dropping invalid control characters.
   * <p>
   * Only four characters need to be encoded, according to
   * http://www.w3.org/TR/REC-xml/#NT-AttValue: &lt; &amp; " '.
   * Actually, we could only encode one of the quote characters if
   * we knew that that was the one used to wrap the value, but we'll
   * play it safe and encode both.
   * <p>
   * We drop invalid XML characters, following
   * http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char :
   * <pre>
   * Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
   * </pre>
   * Java uses UTF-16 internally, so Unicode characters U+10000 to
   * U+10FFFF are encoded using the surrogate characters excluded
   * above, 0xD800 to 0xDFFF. So we allow just 0x09, 0x0A, 0x0D,
   * and the range 0x20 to 0xFFFD.
   *
   * @param buf the {@code StringBuffer} to which to append the attribute value.
   * @param attrValue the attribute value.
   */
  private static void appendAttrValue(StringBuilder buf, String attrValue) {
    for (int i = 0; i < attrValue.length(); i++) {
      char c = attrValue.charAt(i);
      switch (c) {
      case '<':
        buf.append("&lt;");
        break;
      case '&':
        buf.append("&amp;");
        break;
      case '"':
        buf.append("&quot;");
        break;
      case '\'':
        buf.append("&apos;");
        break;
      case '\t':
      case '\n':
      case '\r':
        buf.append(' ');
        break;
      default:
        if (c >= 0x20 && c <= 0xFFFD) {
          buf.append(c);
        }
        break;
      }
    }
  }

  /**
   * Append the supplied content in a CDATA block.
   * Escape the contents of a CDATA block, if necessary.
   * CDATA blocks cannot be nested, so we will remove the
   * CDATA start and end tags.
   *
   * @param buf {@code StringBuilder} we are appending to.
   * @param tag XML element tag to wrap around CDATA.
   * @param str String to enclose in CDATA tags.
   */
  private static void appendCdata(StringBuilder buf, String tag, String str) {
    if (str.indexOf(cdataStart) >= 0) {
      str = str.replaceAll(cdataStart, " ");
    }
    if (str.indexOf(cdataEnd) >= 0) {
      str = str.replaceAll(cdataEnd, " ");
    }
    buf.append('<').append(tag).append('>');
    buf.append(cdataStart).append(str).append(cdataEnd);
    buf.append("</").append(tag).append('>').append(NL);
  }
}
