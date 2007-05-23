// Copyright (C) 2006 Google Inc.
package com.google.enterprise.connector.pusher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/**
 * GoogleContentStream is an InputStream that "wraps" the real content
 * stream for a feed item. If the content stream is non-null and non-empty,
 * then GoogleContentStream just passes through all calls to it. If both of
 * those conditions do not hold, then a "defaultContent" string is used
 * instead. The defaultContent string is initially a single space, but can
 * be changed to any UTF-8 encodeable string.  Care should be taken to
 * insure that a document composed of the string chosen is indexable by
 * the GSA. 
 */
public class GoogleContentStream extends InputStream {
  
  /**
   * defaultContent is the value of google:content that will be returned
   * if the underlying stream is empty. This CAN be changed to anything
   * that the GSA will index on.
   */
  private static String defaultContent = " "; 
  private static byte[] blanks = " ".getBytes(); // backup for encoding errors
  private InputStream source;
  
  /**
   * isBound marks whether this stream is "bound" to a real input stream. The
   * first time we're called, we see if the stream used in the constructor
   * has data in it; if so, we bind on that; if not, we bind on a stream
   * based on defaultContent.
   */
  private boolean isBound = false;
  
  /**
   * Get the string that will be used in place of a null or empty 'content'
   * @return defaultContent
   */
  public static String getDefaultContent() {
    return defaultContent;
  }
  
  /**
   * Set the string that will be used in place of a null or empty 'content'.
   * @param content Must be encodeable into UTF-8
   * @throws UnsupportedEncodingException if content is not encodeable 
   *     into UTF-8
   */
  public static void setDefaultContent(String content)
  throws UnsupportedEncodingException {
    content.getBytes("UTF-8"); // throws UnsupportedEncodingException
    defaultContent = content;
  }
  
  /**
   * Constructor. Initialize with the "real" input source from the feed,
   * even if it's empty. If it IS empty, a substitute stream based on
   * defaultContent will be used instead. 
   * @param source null value is allowed; in that case, the "defaultContent"
   * will be used.
   */
  public GoogleContentStream(InputStream source) {
    if (source == null) {
      bindToString();
    } else {
      this.source = source;
    }
  }

  public int read() throws IOException {
    if (!isBound) {
      isBound = true;
      int byteFirst = source.read();
      if (byteFirst < 0) {
        bindToString();
        return source.read();
      } else {
        return byteFirst;
      }
    } else {
      return source.read();
    }
  }
  
  public int read(byte[] b) throws IOException {
    if (!isBound) {
      isBound = true;
      int bytesFirst = source.read(b);
      if (bytesFirst <= 0) {
        bindToString();
        return source.read(b);
      } else {
        return bytesFirst;
      }
    } else {
      return source.read(b);
    }
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    if (!isBound) {
      isBound = true;
      int bytesFirst = source.read(b, off, len);
      if (bytesFirst <= 0) {
        bindToString();
        return source.read(b, off, len);
      } else {
        return bytesFirst;
      }
    } else {
      return source.read(b, off, len);
    }
  }
  
  public int available() throws IOException {
    if (!isBound) {
      isBound = true;
      int avail = source.available();
      if (avail <= 0) {
        bindToString();
        return source.available();
      } else {
        return avail;
      }
    } else {
      return source.available();
    }
  }
  
  public boolean markSupported() {
    if (!isBound) {
      isBound = true;
      try {
        int avail = source.available();
        if (avail <= 0) {
          bindToString();
        }
      } catch (IOException e) {
        bindToString();
      }
    }
    return source.markSupported();
  }
  
  public void mark(int readlimit) {
    if (markSupported()) {
      source.mark(readlimit);
    }
  }
  
  public void close() throws IOException {
    source.close();
  }
  
  public void reset() throws IOException {
    if (markSupported()) {
      source.reset();
    }
  }
  
  public long skip(long n) throws IOException {
    if (!isBound) {
      isBound = true;
      try {
        int avail = source.available();
        if (avail <= 0) {
          bindToString();
        }
      } catch (IOException e) {
        bindToString();
      }     
    }
    return source.skip(n);
  }
  
  private void bindToString() {
    try {
      source = new ByteArrayInputStream(defaultContent.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      source = new ByteArrayInputStream(blanks);
    }
    isBound = true;
  }
  
  
}
