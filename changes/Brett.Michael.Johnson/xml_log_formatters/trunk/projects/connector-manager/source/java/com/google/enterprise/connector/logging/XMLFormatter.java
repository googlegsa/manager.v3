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
import java.util.logging.LogRecord;

/**
 * A log formatter resembling java.util.logging.XMLFormatter,
 * adding NDC Logging capabilities.  Easier to read than the
 * Log4jCompatibleXMLFormatter, but doesn't work in Chainsaw.
 */
/* TODO: Add MDC logging. */
public class XMLFormatter extends java.util.logging.XMLFormatter {
  private final String recordTag = "<record>";
  private final int recordTagLen = recordTag.length();
  private static final String NL = System.getProperty("line.separator");

  @Override
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
