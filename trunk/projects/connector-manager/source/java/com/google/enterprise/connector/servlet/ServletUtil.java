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

import java.io.PrintWriter;


public class ServletUtil {
  public static final String MIMETYPE_XML = "text/xml";
  public static final String MIMETYPE_HTML = "text/html";

  public static final String XMLTAG_RESPONSE_ROOT = "CmResponse";
  public static final String XMLTAG_STATUSID = "StatusId";
  public static final String XMLTAG_CONNECTOR_TYPES = "ConnectorTypes";
  public static final String XMLTAG_CONNECTOR_TYPE = "ConnectorType";
  public static final String XMLTAG_CONNECTOT_STATUS = "ConnectorStatus";
  public static final String XMLTAG_CONNECTOR_NAME = "ConnectorName";
  public static final String XMLTAG_STATUS = "Status";

  public static final int HTML_NORMAL = 0;
  public static final int HTML_HEADING = 1;
  public static final int HTML_LINE = 2;

  private static final String[] XMLIndent = { "",
      "  ",
      "    ",
      "      ",
      "        ",
      "          ",
      "            ",
      "              "};


  private ServletUtil() {
  }

  /**
   * Write a name value pair as an XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param elemValue element value
   */
  public static void writeXMLElement(PrintWriter out, int indentLevel,
                                  String elemName, String elemValue) {
    out.println(IndentStr(indentLevel) +
                "<" + elemName + ">" + elemValue +
                "</" + elemName + ">");
  }

  /** Write an XML tag to a PrintWriter
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation
   * @param tagName name of the XML tag to be added
   * @param endingTag add a beginning tag if true, an ending tag if false
   */
  public static void writeXMLTag(PrintWriter out, int indentLevel,
                          String tagName, boolean endingTag) {
    out.println(IndentStr(indentLevel) +
                (endingTag ? "</" : "<") +
       	        (tagName) + ">");
  }

  // A helper method to ident output string.
  private static String IndentStr(int level) {
    if (level < XMLIndent.length) {
      return XMLIndent[level];
    } else {
      return XMLIndent[XMLIndent.length - 1] +
             IndentStr(level - XMLIndent.length);
    }
  }

  public static void htmlHeadWithTitle(PrintWriter out, String title) {
    out.println("<HTML>");
    out.println("<HEAD><TITLE>" + title + "</TITLE></HEAD><BODY>");
  }

  public static void htmlBody(
      PrintWriter out, int style, String text, boolean linebreak) {
    switch(style) {
      case HTML_NORMAL:
        out.println(text);
        break;        
      case HTML_HEADING:
        out.println("<H1>" + text + "</H1>");
        break;
      case HTML_LINE:
        out.println("<HR>");
        break;
      default:
        break;
    }
    if (linebreak) {
      out.println("<BR>");
    }
  }

  public static void htmlPage(PrintWriter out) {
    out.println("</BODY></HTML>");
  }
}
