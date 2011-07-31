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

import com.google.enterprise.connector.pusher.XmlFeed;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@code FilterInputStream} that protects against large documents and empty
 * documents.
 * If we have read more than {@link FileSizeLimitInfo.maxDocumentSize}
 * bytes from the input, we reset the feed to before we started reading
 * content, then provide the alternate content.  Similarly, if we get EOF
 * after reading zero bytes, we provide the alternate content.
 * </p>
 * This filter assumes that a {@link BigEmptyDocumentFilterInputStream}
 * is somewhere up-stream, in that {@link AlternateContentFilterInputStream}
 * looks for the {@link BigDocumentException} and {@link EmptyDocumentException}
 * that would be thrown.
 */
// TODO: WARNING: This will not work for BigDocumentException if using
// chunked HTTP transfer.
public class AlternateContentFilterInputStream extends FilterInputStream {
  private static final Logger LOGGER =
      Logger.getLogger(AlternateContentFilterInputStream.class.getName());

  private static final byte[] SPACE_CHAR = { 0x20 };  // UTF-8 space
  private boolean useAlternate;
  private InputStream alternate;
  private final XmlFeed feed;
  private int resetPoint;

  /**
   * @param in InputStream containing raw document content.
   *        May be {@code null}.
   * @param alternate InputStream containing alternate content to provide
   *        If {@code null}, a default alternate content of a single space
   *        character is used.
   */
  public AlternateContentFilterInputStream(InputStream in,
      InputStream alternate) {
    this(in, alternate, null);
  }

  /**
   * @param in InputStream containing raw document content.
   *        May be {@code null}.
   * @param alternate InputStream containing alternate content to provide
   *        If {@code null}, a default alternate content of a single space
   *        character is used.
   * @param feed XmlFeed under constructions (used for reseting size).
   *        May be {@code null}.
   */
  public AlternateContentFilterInputStream(InputStream in,
      InputStream alternate, XmlFeed feed) {
    super(in);
    if (alternate == null) {
      // Use the default Alternate content: a single space.
      alternate = getDefaultAlternateContent();
    }
    this.useAlternate = (in == null);
    this.alternate = alternate;
    this.feed = feed;
    this.resetPoint = (feed == null) ? 0 : -1;
  }

  /**
   * @return and InputStream to the default alternate content -
   *         a single space character.
   */
  public static InputStream getDefaultAlternateContent() {
    return new ByteArrayInputStream(SPACE_CHAR);
  }

  // Reset the feed to its position when we started reading this stream,
  // and start reading from the alternate input.
  private void switchToAlternate() {
    if (feed != null) {
      feed.reset(resetPoint);
    }
    useAlternate = true;
  }

  @Override
  public int read() throws IOException {
    if (resetPoint == -1) {
      // If I have read nothing yet, remember the reset point in the feed.
      resetPoint = feed.size();
    }
    if (!useAlternate) {
      try {
        return super.read();
      } catch (EmptyDocumentException e) {
        switchToAlternate();
      } catch (BigDocumentException e) {
        LOGGER.finer("Document content exceeds the maximum configured "
                     + "document size, discarding content.");
        switchToAlternate();
      }
    }
    return alternate.read();
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    if (resetPoint == -1) {
      // If I have read nothing yet, remember the reset point in the feed.
      resetPoint = feed.size();
    }
    if (!useAlternate) {
      try {
        return super.read(b, off, len);
      } catch (EmptyDocumentException e) {
        switchToAlternate();
        return 0; // Return alternate content on subsequent call to read().
      } catch (BigDocumentException e) {
        LOGGER.finer("Document content exceeds the maximum configured "
                     + "document size, discarding content.");
        switchToAlternate();
        return 0; // Return alternate content on subsequent call to read().
      }
    }
    return alternate.read(b, off, len);
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public void close() throws IOException {
    super.close();
    alternate.close();
  }
}
