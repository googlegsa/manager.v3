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

package com.google.enterprise.connector.common;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.enterprise.connector.util.Base16;

import java.text.MessageFormat;

/**
 * Bare minimum PDF utilities.
 */
public class PdfUtil {

  private PdfUtil() { // Prevent instantiation.
    throw new AssertionError("Do not instantiate PdfUtil");
  }

  /**
   * Minimal PDF boilerplate, according to Adobe PDF Reference Manual.
   */
  private static String PDF_OBJS = "%PDF-1.1\n"
      + "1 0 obj\n<</Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n"
      + "2 0 obj\n<</Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n"
      + "3 0 obj\n<</Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 72 72]\n>>\n"
      + "endobj\n4 0 obj\n<<{0}\n>>\nendobj\n";

  private static String PDF_XREF = "xref\n0 5\n0000000000 65535 f\r\n"
      + "0000000009 00000 n\r\n0000000057 00000 n\r\n"
      + "0000000113 00000 n\r\n0000000181 00000 n\r\n"
      + "trailer\n<</Size 5\n/Root 1 0 R\n/Info 4 0 R\n>>\nstartxref\n{0}\n"
      + "%%EOF\n";

  /**
   * Creates a tiny, empty PDF document.
   *
   * @return a String containing a PDF encoding of an empty document
   */
  public static String emptyPdf() {
    return titledEmptyPdf(null);
  }

  /**
   * Creates a tiny, empty PDF document with a Title entry in the
   * Document Information Dictionary.
   *
   * Note that the GSA PDF text extraction process only pulls out
   * the first 128 characters of the Title.
   *
   * @param title the title of the document
   * @return a String containing a PDF encoding of a titled document
   */
  public static String titledEmptyPdf(String title) {
    StringBuilder buf = new StringBuilder();
    // If title is null or empty, do not insert a /Title element.
    // For some reason, the GSA pdf titles can not have periods.
    buf.append(MessageFormat.format(PDF_OBJS, Strings.isNullOrEmpty(title)
        ? "" : "/Title " + toBinaryString(title.replace('.', ' '))));
    buf.append(MessageFormat.format(PDF_XREF, buf.length()));
    return buf.toString();
  }

  /**
   * PDF literal strings are limited to 8-bit characters.
   * Unicode characters need to be encoded as UTF-16BE in
   * a stream of hexadecimal characters.
   *
   * @param text some plain text
   * @return a PDF hexadecimal string encoding the text
   */
  public static String toBinaryString(String text) {
    // Leading FEFF indicates big-endian 16-bit Unicode text.
    StringBuilder buf = new StringBuilder("<FEFF");
    byte[] bytes = text.getBytes(Charsets.UTF_16BE);
    Base16.upperCase().encode(bytes, buf);
    buf.append('>');
    return buf.toString();
  }
}
