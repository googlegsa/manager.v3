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

import junit.framework.TestCase;

public class PdfUtilTest extends TestCase {

  public void testEmptyDocument() throws Exception {
    String expected = "%PDF-1.1\n"
      + "1 0 obj\n<</Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n"
      + "2 0 obj\n<</Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n"
      + "3 0 obj\n<</Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 72 72]\n>>\n"
      + "endobj\n4 0 obj\n<<\n>>\nendobj\n"
      + "xref\n0 5\n0000000000 65535 f\r\n"
      + "0000000009 00000 n\r\n0000000057 00000 n\r\n"
      + "0000000113 00000 n\r\n0000000181 00000 n\r\n"
      + "trailer\n<</Size 5\n/Root 1 0 R\n/Info 4 0 R\n>>\nstartxref\n202\n"
      + "%%EOF\n";

    String actual = PdfUtil.emptyPdf();
    assertEquals(actual, expected, actual);
  }

  public void testTitledDocument() throws Exception {
    String expected = "%PDF-1.1\n"
      + "1 0 obj\n<</Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n"
      + "2 0 obj\n<</Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n"
      + "3 0 obj\n<</Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 72 72]\n>>\n"
      + "endobj\n4 0 obj\n<</Title "
      + "<FEFF00480065006C006C006F00200077006F0072006C00640021>\n>>\nendobj\n"
      + "xref\n0 5\n0000000000 65535 f\r\n"
      + "0000000009 00000 n\r\n0000000057 00000 n\r\n"
      + "0000000113 00000 n\r\n0000000181 00000 n\r\n"
      + "trailer\n<</Size 5\n/Root 1 0 R\n/Info 4 0 R\n>>\nstartxref\n263\n"
      + "%%EOF\n";

    String actual = PdfUtil.titledEmptyPdf("Hello world!");
    assertEquals(actual, expected, actual);
  }

  public void testUnicodeChars() throws Exception {
    String expected = "%PDF-1.1\n"
      + "1 0 obj\n<</Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n"
      + "2 0 obj\n<</Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n"
      + "3 0 obj\n<</Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 72 72]\n>>\n"
      + "endobj\n4 0 obj\n<</Title <FEFF01870127012F01480207021902290020"
      + "0261026D01B401A5019502820020219200202EC100202EAC>\n>>\nendobj\n"
      + "xref\n0 5\n0000000000 65535 f\r\n"
      + "0000000009 00000 n\r\n0000000057 00000 n\r\n"
      + "0000000113 00000 n\r\n0000000181 00000 n\r\n"
      + "trailer\n<</Size 5\n/Root 1 0 R\n/Info 4 0 R\n>>\nstartxref\n295\n"
      + "%%EOF\n";

    // Looks like "Chinese glyphs -> tiger eye"
    String title = "\u0187\u0127\u012F\u0148\u0207\u0219\u0229 \u0261\u026D"
        + "\u01B4\u01A5\u0195\u0282 \u2192 \u2EC1 \u2EAC";
    String actual = PdfUtil.titledEmptyPdf(title);
    assertEquals(actual, expected, actual);
  }

  /** This is useful to write out the PDF files and view them in Acrobat. */
  /*
  private void writeFile(String text) throws Exception {
    java.io.FileOutputStream f = new java.io.FileOutputStream(getName() + ".pdf");
    f.write(text.getBytes("UTF-8"));
    f.close();
  }
  */
}
