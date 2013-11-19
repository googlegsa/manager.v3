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

package com.google.enterprise.connector.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.enterprise.connector.traversal.MimeTypeMap;
import com.google.enterprise.connector.traversal.ProductionTraversalContext;

import eu.medsea.util.EncodingGuesser;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

/** Tests for MimeTypeDetector.  */
public class MimeTypeDetectorTest extends TestCase {
  private static final File NO_EXTENSION =
      new File("testdata/tmp/mimeTypeDetectorTest");

  /**
   * Include two consecutive nulls to force MimeTypeDetector to think
   * it's binary rather than text/plain.
   */
  private static final byte[] PDF_PREFIX =
      "%PDF-1.3\n%\0\0\n".getBytes(Charsets.UTF_8);

  private MimeTypeDetector mimeTypeDetector;
  private final InputStreamFactory notUsedInputStreamFactory =
      new NotUsedInputStreamFactory();

  @Override
  public void setUp() {
    MimeTypeMap mimeTypeMap = new MimeTypeMap();
    mimeTypeMap.setPreferredMimeTypes(
        Sets.newHashSet("text/plain", "text/html", "text/xml"));
    mimeTypeMap.setSupportedMimeTypes(Sets.newHashSet(
        "application/pdf", "application/msword", "application/xml"));
    mimeTypeMap.setExcludedMimeTypes(Sets.newHashSet("text/x-uuencode"));
    ProductionTraversalContext traversalContext =
        new ProductionTraversalContext();
    traversalContext.setMimeTypeMap(mimeTypeMap);
    MimeTypeDetector.setTraversalContext(traversalContext);
    mimeTypeDetector = new MimeTypeDetector();
  }

  public void testSetSupportedEncodings() throws Exception {
    @SuppressWarnings("unchecked") Collection<String> encodings =
        EncodingGuesser.getSupportedEncodings();
    assertTrue(encodings.contains("UTF-8"));
    assertTrue(encodings.contains("ISO-8859-1"));
    assertTrue(encodings.contains("windows-1252"));
    assertTrue(encodings.contains(Charset.defaultCharset().toString()));

    // Need to make a deep copy to restore properly.
    Collection<String> originalEncodings = Sets.newHashSet(encodings);

    MimeTypeDetector.setSupportedEncodings(
        Sets.newHashSet("UTF-16", "US-ASCII"));
    @SuppressWarnings("unchecked") Collection<String> newEncodings =
        EncodingGuesser.getSupportedEncodings();
    assertTrue(newEncodings.contains("UTF-16"));
    assertTrue(newEncodings.contains("US-ASCII"));
    assertTrue(newEncodings.contains(Charset.defaultCharset().toString()));

    // Restore original supported encodings.
    EncodingGuesser.setSupportedEncodings(originalEncodings);
  }

  public void testIllegalArguments() throws Exception {
    try {
      mimeTypeDetector.getMimeType(null, (byte[]) null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }

    try {
      mimeTypeDetector.getMimeType(null, (InputStreamFactory) null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }
  }

  public void testFileExtensionOnly() throws Exception {
    assertEquals("text/html", mimeTypeDetector.getMimeType(
                 "a/\\big.htm", (byte[]) null));

    assertEquals("text/html", mimeTypeDetector.getMimeType(
                 "a/big.html", (InputStreamFactory) null));
  }

  public void testUnknownMimeType() throws Exception {
    // "Unknown" mime type is actually "application/octet-stream".
    assertEquals("application/octet-stream",
                 MimeTypeDetector.UNKNOWN_MIME_TYPE);

    // Truly unknown.
    assertEquals(MimeTypeDetector.UNKNOWN_MIME_TYPE,
                 mimeTypeDetector.getMimeType("a/zork.xyzzy",
                                              (InputStreamFactory) null));

    // A file whose only mimetype is "application/octet-stream" should work.
    // Note: Has internal knowledge of MimeUtil mime-types.properties.
    assertEquals("application/octet-stream", mimeTypeDetector.getMimeType(
                 "a/compiled.o", (InputStreamFactory) null));

    // A file whose mimetype includes "application/octet-stream" as well
    // as others, should return the other, even if it ranks less than
    // "application/octet-stream".
    // Note: Has internal knowledge of MimeUtil mime-types.properties.
    assertEquals("text/x-uuencode", mimeTypeDetector.getMimeType(
                 "a/uuencoded.uu", (InputStreamFactory) null));
  }

  public void testFileExtension() throws Exception {
    assertEquals("text/html", mimeTypeDetector.getMimeType(
        "a/\\big.htm", notUsedInputStreamFactory));

    assertEquals("text/xml", mimeTypeDetector.getMimeType(
        "smb://a.b/a/\\big.xml", notUsedInputStreamFactory));

    assertEquals("application/pdf", mimeTypeDetector.getMimeType(
        "a/\\a.b.cig.pdf", notUsedInputStreamFactory));

    assertEquals("application/msword", mimeTypeDetector.getMimeType(
        "a/big.doc", notUsedInputStreamFactory));
  }

  public void testFileContent() throws Exception {
    assertEquals("text/plain", mimeTypeDetector.getMimeType(
        "a/big", "I am a string of text".getBytes()));

    String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<dog>beagle</dog>\n";

    assertEquals("text/xml", mimeTypeDetector.getMimeType(
        "a/big", xml.getBytes()));
  }

  public void testFileContentOnly() throws Exception {
    assertEquals("text/plain", mimeTypeDetector.getMimeType(
        null, "I am a string of text".getBytes()));
  }

  public void testFileContentStream() throws Exception {
    InputStreamFactory inputStreamFactory =
        new StringInputStreamFactory("I am a string of text");

    assertEquals("text/plain", mimeTypeDetector.getMimeType(
        "a/big", inputStreamFactory));

    String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<dog>beagle</dog>\n";

    inputStreamFactory = new StringInputStreamFactory(xml);
    assertEquals("text/xml", mimeTypeDetector.getMimeType(
        "a/big", inputStreamFactory));
  }

  public void testFileContentStreamOnly() throws Exception {
    InputStreamFactory inputStreamFactory =
        new StringInputStreamFactory("I am a string of text");
    assertEquals("text/plain", mimeTypeDetector.getMimeType(
        null, inputStreamFactory));

    // Try an actual ms office doc larger than the detector input buffer.
    inputStreamFactory =
        new FileInputStreamFactory("testdata/mocktestdata/test.doc");
    assertEquals("application/msword", mimeTypeDetector.getMimeType(
        null, inputStreamFactory));
  }

  /** Tests that MimeTypeDetector does not try to read the file. */
  public void testNoFileAccess() throws IOException {
    // Testing a file with no extension that looks like PDF.
    Files.write(PDF_PREFIX, NO_EXTENSION);
    try {
      // With no content, we should get an unknown type.
      assertEquals(MimeTypeDetector.UNKNOWN_MIME_TYPE,
          mimeTypeDetector.getMimeType(NO_EXTENSION.getPath(), (byte[]) null));

      // With text content, we should get text/plain.
      assertEquals("text/plain", mimeTypeDetector.getMimeType(
          NO_EXTENSION.getPath(), "I am a string of text".getBytes()));
    } finally {
      NO_EXTENSION.delete();
    }
  }

  private static class NotUsedInputStreamFactory implements InputStreamFactory {
    public InputStream getInputStream() {
      throw new UnsupportedOperationException();
    }
  }

  private static class StringInputStreamFactory implements InputStreamFactory {
    private final String string;

    StringInputStreamFactory(String string) {
      this.string = string;
    }

    public InputStream getInputStream() {
      return new ByteArrayInputStream(string.getBytes());
    }
  }
}
