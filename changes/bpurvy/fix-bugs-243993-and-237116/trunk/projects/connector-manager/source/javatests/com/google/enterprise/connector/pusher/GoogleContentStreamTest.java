package com.google.enterprise.connector.pusher;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class GoogleContentStreamTest extends TestCase {

  /**
   * An inner class whose purpose is just to test that the underlying
   * InputStream does get called appropriately. Each method records that
   * it was called.  It's expected / required that the method wasCalled()
   * be called once and only once after each InputStream method, to verify
   * that the correct method was called. (any other pattern will give
   * incorrect results).
   * MockInputStream mocks a stream with one byte in it, a line feed.
   */
  class MockInputStream extends InputStream {
    public static final byte oneByte = 0x0A;
    private boolean wasCalled = false;
    public MockInputStream() {
      wasCalled = false;
    }
    
    public boolean wasCalled() {
      boolean temp = wasCalled;
      wasCalled = false;
      return temp;
    }
    
    public int read() throws IOException {
      wasCalled = true;
      return oneByte;
    }
    
    public int read(byte[] b) throws IOException {
      b[0] = oneByte;
      wasCalled = true;
      return 1;  
    }
    
    public int available() throws IOException {
      wasCalled = true;
      return 1;
    }
    
    public int read(byte[] b,int off, int len) throws IOException {
      b[0] = oneByte;
      wasCalled = true;
      return 1;
    }
    
    public long skip(long n) throws IOException {
      wasCalled = true;
      return 0;
    }
    
    public void close() throws IOException {
      wasCalled = true;
    }
    
    public void mark(int readlimit) {
      wasCalled = true;
    }
    
    public void reset() throws IOException {
      wasCalled = true;
    }
    
    public boolean markSupported() {
      wasCalled = true;
      return true;
    }
  }
  
  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Tests that it passes through properly when the content stream is non-null
   */
  public void testNotNull() {
    MockInputStream mock = new MockInputStream();
    GoogleContentStream google = new GoogleContentStream(mock);
    try {
      byte[] arr = new byte[10];
      assertEquals(google.read(), MockInputStream.oneByte);
      assertTrue(mock.wasCalled());
      
      assertEquals(google.read(arr), 1);
      assertEquals(arr[0], MockInputStream.oneByte);
      assertTrue(mock.wasCalled());
      
      assertEquals(google.read(arr, 0, 1), 1);
      assertEquals(arr[0], MockInputStream.oneByte);
      assertTrue(mock.wasCalled());
      
      google.available();
      assertTrue(mock.wasCalled());
      
      google.mark(1);
      assertTrue(mock.wasCalled());
      
      assertTrue(google.markSupported());
      assertTrue(mock.wasCalled());
      
      google.reset();
      assertTrue(mock.wasCalled());
      
      google.close();
      assertTrue(mock.wasCalled());
      
      google.skip(1);
      assertTrue(mock.wasCalled());      
    } catch (IOException e) {
      fail(e.toString());
    }
  }
  
  /**
   * Verifies that the GoogleContentStream returns the expected content
   * @param google GoogleContentStream, which presumably is returning its
   *     default content (although this method has no way of knowing that)
   * @param content the expected contents of the stream (which must be 
   *     < 256 chars long)
   */
  void verifyStream(GoogleContentStream google, String content) {
    if (content.length() > 256) {
      fail("Internal test error. 'content' length > 256");
    }
    byte[] byteContents = content.getBytes();
    try {
      assertTrue(google.markSupported());
      google.mark(content.length() + 1);
      
      assertEquals(google.available(), content.length());
      int byteOne = google.read();
      assertEquals(byteOne, content.codePointAt(0));

      byte[] arr = new byte[256];
      arr[0] = (byte) byteOne;
      int length = google.read(arr, 1, 255);
      assertEquals(length, content.length() - 1);
      assertEquals(new String(arr, 0, content.length()), content);
      
      // now go back to the beginning
      google.reset();
      google.mark(content.length() + 1);
      
      length = google.read(arr);
      assertEquals(length, content.length());
      assertEquals(new String(arr, 0, content.length()), content);
      
      // finally, back to the beginning and skip:
      google.reset();
      google.mark(content.length() + 1);
      google.skip(1);
      length = google.read(arr);
      assertEquals(length, content.length() - 1);
      assertEquals(new String(arr, 0, content.length() -1), 
          content.substring(1));
    } catch (IOException e) {
      fail(e.toString());
    }
  }
  
  /**
   * Test that the GoogleContentStream does give back the 'defaultContent'
   * Just to make things interesting, we change defaultContent to something
   * other than what it normally is.
   */
  public void testNull() {
    String myDefault = "default";
    try {
      GoogleContentStream.setDefaultContent(myDefault);
    } catch (UnsupportedEncodingException e) {
      fail(e.toString());
    }
    GoogleContentStream google = new GoogleContentStream(null);
    verifyStream(google, myDefault);
  }
  
  /**
   * Test that GoogleContentStream works when given an InputStream of
   * zero length.
   *
   */
  public void testZeroLength() {
    ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
    GoogleContentStream google = new GoogleContentStream(input);
    verifyStream(google, GoogleContentStream.getDefaultContent());
  }
}
