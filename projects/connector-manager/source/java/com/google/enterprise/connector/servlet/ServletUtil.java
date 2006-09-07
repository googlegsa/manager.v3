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
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

public class ServletUtil {
  public static final String MimeTypeXML = "text/xml";

  public static final String XMLTagResponseRoot = "CmResponse";
  public static final String XMLTagStatusId = "StatusId";
  public static final String XMLTagConnectorTypes = "ConnectorTypes";
  public static final String XMLTagConnectorType = "ConnectorType";

  private static Logger LOG =
    Logger.getLogger(ServletUtil.class.getName());

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
  public static void WriteElement(PrintWriter out, int indentLevel,
                                  String elemName, String elemValue) {
    out.println(IndentStr(indentLevel) +
                "<" + elemName + ">" + elemValue +
                "</" + elemName + ">");
  }

  /** Append an XML tag to a StringBuffer
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation
   * @param tagName name of the XML tag to be added
   * @param endingTag add a beginning tag if true, an ending tag if false
   */
  public static void AddXMLTag(PrintWriter out, int indentLevel,
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

}
