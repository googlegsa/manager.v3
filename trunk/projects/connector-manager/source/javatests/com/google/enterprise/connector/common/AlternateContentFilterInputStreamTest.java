// Copyright 2011 Google Inc.
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

import com.google.enterprise.connector.pusher.MockFeedConnection;
import com.google.enterprise.connector.pusher.XmlFeed;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Tests AlternateContentFilterInputStream.
 */
public class AlternateContentFilterInputStreamTest extends TestCase {
  private static final String CONTENT = "Original Content";
  private static final String ALTERNATE_CONTENT = "Alternate Content";
  private static final String PREFIX = "Prefix ";
  private static final String SUFFIX = " Suffix";

  /** Test null source InputStream yields alternate content. */
  public void testNullSourceInputStream() throws Exception {
    checkACIS((InputStream) null, ALTERNATE_CONTENT);
  }

  /** Test empty source InputStream yields alternate content. */
  public void testEmptySourceInputStream() throws Exception {
    checkACIS(new EmptyInputStream(), ALTERNATE_CONTENT);
  }

  /** Test large source InputStream yields alternate content. */
  public void testBigSourceInputStream() throws Exception {
    checkACIS(new BigInputStream(), ALTERNATE_CONTENT);
  }

  /** Test normal source InputStream does not return alternate content. */
  public void testGoodInputStream() throws Exception {
    checkACIS(CONTENT, CONTENT);
  }

  /** Test null alternate InputStream returns default of 1 space. */
  public void testNullAlternateInputStream() throws Exception {
    checkACISReadByte(null, null, " ");
  }

  /** Test XmlFeed Rollback. */
  public void testFeedRollback() throws Exception {
    checkACIS(new BigInputStream(), newFeed(), ALTERNATE_CONTENT);
  }

  /** Test No XmlFeed Rollback. */
  public void testNoFeedRollback() throws Exception {
    checkACIS(new ByteArrayInputStream(CONTENT.getBytes()), newFeed(), CONTENT);
  }

  /** Test XmlFeed Rollback using single byte read(). */
  public void testFeedRollBackReadByte() throws Exception {
    checkACISReadByte(new BigInputStream(), ALTERNATE_CONTENT);
  }

  /** Test XmlFeed Rollback using single byte read(). */
  public void testFeedRollBackEmptyReadByte() throws Exception {
    checkACISReadByte(new EmptyInputStream(), ALTERNATE_CONTENT);
  }

  /** Test No XmlFeed Rollback using single byte read(). */
  public void testNoFeedRollBackReadByte() throws Exception {
    checkACISReadByte(new ByteArrayInputStream(CONTENT.getBytes()), CONTENT);
  }

  /** Returns a new XmlFeed */
  private XmlFeed newFeed() throws IOException {
    return new XmlFeed("test", FeedType.CONTENT, new FileSizeLimitInfo(),
                       null, new MockFeedConnection());
  }

  /** Check the AlternateContentInputStream produces the correct results. */
  private void checkACIS(String input, String expectedResult)
      throws Exception {
    checkACIS(new ByteArrayInputStream(input.getBytes()), expectedResult);
  }

  /** Check the AlternateContentInputStream produces the correct results. */
  private void checkACIS(InputStream source, String expectedResult)
      throws Exception {
    checkACIS(source, null, expectedResult);
  }

  /** Check the AlternateContentInputStream produces the correct results. */
  private void checkACIS(InputStream source, XmlFeed feed, String expectedResult)
      throws Exception {
    if (feed != null) {
      feed.reset(0);
      feed.write(PREFIX.getBytes());
    }
    InputStream alt = new ByteArrayInputStream(ALTERNATE_CONTENT.getBytes());
    InputStream is = new AlternateContentFilterInputStream(source, alt, null);
    assertNotNull(is);
    byte[] buffer = new byte[8192];
    int totalBytesRead = 0;
    int bytesRead;
    do {
      bytesRead = is.read(buffer, totalBytesRead, buffer.length - totalBytesRead);
      if (bytesRead > 0) {
        if (feed != null) {
          feed.write(buffer, totalBytesRead, bytesRead);
        }
        totalBytesRead += bytesRead;
      }
    } while (bytesRead != -1);
    assertEquals(expectedResult, new String(buffer, 0, totalBytesRead));
    if (feed != null) {
      feed.write(SUFFIX.getBytes());
      assertEquals(PREFIX + expectedResult + SUFFIX, feed.toString());
    }
  }

  /**
   * Check the AlternateContentInputStream single byte read() produces the
   * correct results.
   */
  private void checkACISReadByte(InputStream source, String expectedResult)
      throws Exception {
    checkACISReadByte(source,
                      new ByteArrayInputStream(ALTERNATE_CONTENT.getBytes()),
                      expectedResult);
  }

  /**
   * Check the AlternateContentInputStream single byte read() produces the
   * correct results.
   */
  private void checkACISReadByte(InputStream source, InputStream alt,
      String expectedResult) throws Exception {
    XmlFeed feed = newFeed();
    feed.reset(0);
    feed.write(PREFIX.getBytes());
    InputStream is = new AlternateContentFilterInputStream(source, alt, feed);
    assertFalse(is.markSupported());
    for (int ch; (ch = is.read()) != -1; feed.write(ch)) ;
    feed.write(SUFFIX.getBytes());
    assertEquals(PREFIX + expectedResult + SUFFIX, feed.toString());
  }

  private class BigInputStream extends BigEmptyDocumentFilterInputStream {
    public BigInputStream() {
      super(new ByteArrayInputStream("abcdefg".getBytes()), 4);
    }
  }

  private class EmptyInputStream extends BigEmptyDocumentFilterInputStream {
    public EmptyInputStream() {
      super(new ByteArrayInputStream("".getBytes()), 4);
    }
  }
}
