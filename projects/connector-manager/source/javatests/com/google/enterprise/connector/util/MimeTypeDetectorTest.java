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

import com.google.common.collect.Sets;
import com.google.enterprise.connector.traversal.MimeTypeMap;
import com.google.enterprise.connector.traversal.ProductionTraversalContext;
import com.google.enterprise.connector.util.diffing.testing.FakeTraversalContext;

import eu.medsea.util.EncodingGuesser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import junit.framework.TestCase;

/** Tests for MimeTypeDetector.  */
public class MimeTypeDetectorTest extends TestCase {
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
    ProductionTraversalContext traversalContext =
        new ProductionTraversalContext();
    traversalContext.setMimeTypeMap(mimeTypeMap);
    MimeTypeDetector.setTraversalContext(traversalContext);
    mimeTypeDetector = new MimeTypeDetector();
  }

  public void testSetSupportedEncodings() throws Exception {
    Collection encodings = EncodingGuesser.getSupportedEncodings();
    assertTrue(encodings.contains("UTF-8"));
    assertTrue(encodings.contains("ISO-8859-1"));
    assertTrue(encodings.contains("windows-1252"));
    assertTrue(encodings.contains(Charset.defaultCharset().toString()));

    // Need to make a deep copy to restore properly.
    Collection originalEncodings = Sets.newHashSet(encodings.toArray());

    MimeTypeDetector.setSupportedEncodings(
        Sets.newHashSet("UTF-16", "US-ASCII"));
    encodings = EncodingGuesser.getSupportedEncodings();
    assertTrue(encodings.contains("UTF-16"));
    assertTrue(encodings.contains("US-ASCII"));
    assertTrue(encodings.contains(Charset.defaultCharset().toString()));

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
