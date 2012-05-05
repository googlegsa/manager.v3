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

import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SystemClock;

import junit.framework.TestCase;

/**
 * Unit Test for iHarder Base64.
 */
public class Base64Test extends TestCase {

  static final String input =
      " Google's indices consist of information that has been" +
      " identified, indexed and compiled through an automated" +
      " process with no advance review by human beings. Given" +
      " the enormous volume of web site information added," +
      " deleted, and changed on a frequent basis, Google cannot" +
      " and does not screen anything made available through its" +
      " indices. For each web site reflected in Google's" +
      " indices, if either (i) a site owner restricts access to" +
      " his or her web site or (ii) a site is taken down from" +
      " the web, then, upon receipt of a request by the site" +
      " owner or a third party in the second instance, Google" +
      " would consider on a case-by-case basis requests to" +
      " remove the link to that site from its indices. However," +
      " if the operator of the site does not take steps to" +
      " prevent it, the automatic facilities used to create" +
      " the indices are likely to find that site and index it" +
      " again in a relatively short amount of time.";

  static final String expect =
      "IEdvb2dsZSdzIGluZGljZXMgY29uc2lzdCBvZiBpbmZvcm1hdGlvbi" +
      "B0aGF0IGhhcyBiZWVuIGlkZW50aWZpZWQsIGluZGV4ZWQgYW5kIGNv" +
      "bXBpbGVkIHRocm91Z2ggYW4gYXV0b21hdGVkIHByb2Nlc3Mgd2l0aC" +
      "BubyBhZHZhbmNlIHJldmlldyBieSBodW1hbiBiZWluZ3MuIEdpdmVu" +
      "IHRoZSBlbm9ybW91cyB2b2x1bWUgb2Ygd2ViIHNpdGUgaW5mb3JtYX" +
      "Rpb24gYWRkZWQsIGRlbGV0ZWQsIGFuZCBjaGFuZ2VkIG9uIGEgZnJl" +
      "cXVlbnQgYmFzaXMsIEdvb2dsZSBjYW5ub3QgYW5kIGRvZXMgbm90IH" +
      "NjcmVlbiBhbnl0aGluZyBtYWRlIGF2YWlsYWJsZSB0aHJvdWdoIGl0" +
      "cyBpbmRpY2VzLiBGb3IgZWFjaCB3ZWIgc2l0ZSByZWZsZWN0ZWQgaW" +
      "4gR29vZ2xlJ3MgaW5kaWNlcywgaWYgZWl0aGVyIChpKSBhIHNpdGUg" +
      "b3duZXIgcmVzdHJpY3RzIGFjY2VzcyB0byBoaXMgb3IgaGVyIHdlYi" +
      "BzaXRlIG9yIChpaSkgYSBzaXRlIGlzIHRha2VuIGRvd24gZnJvbSB0" +
      "aGUgd2ViLCB0aGVuLCB1cG9uIHJlY2VpcHQgb2YgYSByZXF1ZXN0IG" +
      "J5IHRoZSBzaXRlIG93bmVyIG9yIGEgdGhpcmQgcGFydHkgaW4gdGhl" +
      "IHNlY29uZCBpbnN0YW5jZSwgR29vZ2xlIHdvdWxkIGNvbnNpZGVyIG" +
      "9uIGEgY2FzZS1ieS1jYXNlIGJhc2lzIHJlcXVlc3RzIHRvIHJlbW92" +
      "ZSB0aGUgbGluayB0byB0aGF0IHNpdGUgZnJvbSBpdHMgaW5kaWNlcy" +
      "4gSG93ZXZlciwgaWYgdGhlIG9wZXJhdG9yIG9mIHRoZSBzaXRlIGRv" +
      "ZXMgbm90IHRha2Ugc3RlcHMgdG8gcHJldmVudCBpdCwgdGhlIGF1dG" +
      "9tYXRpYyBmYWNpbGl0aWVzIHVzZWQgdG8gY3JlYXRlIHRoZSBpbmRp" +
      "Y2VzIGFyZSBsaWtlbHkgdG8gZmluZCB0aGF0IHNpdGUgYW5kIGluZG" +
      "V4IGl0IGFnYWluIGluIGEgcmVsYXRpdmVseSBzaG9ydCBhbW91bnQg" +
      "b2YgdGltZS4=";

  static final String expectNL =
      "IEdvb2dsZSdzIGluZGljZXMgY29uc2lzdCBvZiBpbmZvcm1hdGlvbiB0aGF0IGhhcyBiZWVuIGlk\n" +
      "ZW50aWZpZWQsIGluZGV4ZWQgYW5kIGNvbXBpbGVkIHRocm91Z2ggYW4gYXV0b21hdGVkIHByb2Nl\n" +
      "c3Mgd2l0aCBubyBhZHZhbmNlIHJldmlldyBieSBodW1hbiBiZWluZ3MuIEdpdmVuIHRoZSBlbm9y\n" +
      "bW91cyB2b2x1bWUgb2Ygd2ViIHNpdGUgaW5mb3JtYXRpb24gYWRkZWQsIGRlbGV0ZWQsIGFuZCBj\n" +
      "aGFuZ2VkIG9uIGEgZnJlcXVlbnQgYmFzaXMsIEdvb2dsZSBjYW5ub3QgYW5kIGRvZXMgbm90IHNj\n" +
      "cmVlbiBhbnl0aGluZyBtYWRlIGF2YWlsYWJsZSB0aHJvdWdoIGl0cyBpbmRpY2VzLiBGb3IgZWFj\n" +
      "aCB3ZWIgc2l0ZSByZWZsZWN0ZWQgaW4gR29vZ2xlJ3MgaW5kaWNlcywgaWYgZWl0aGVyIChpKSBh\n" +
      "IHNpdGUgb3duZXIgcmVzdHJpY3RzIGFjY2VzcyB0byBoaXMgb3IgaGVyIHdlYiBzaXRlIG9yIChp\n" +
      "aSkgYSBzaXRlIGlzIHRha2VuIGRvd24gZnJvbSB0aGUgd2ViLCB0aGVuLCB1cG9uIHJlY2VpcHQg\n" +
      "b2YgYSByZXF1ZXN0IGJ5IHRoZSBzaXRlIG93bmVyIG9yIGEgdGhpcmQgcGFydHkgaW4gdGhlIHNl\n" +
      "Y29uZCBpbnN0YW5jZSwgR29vZ2xlIHdvdWxkIGNvbnNpZGVyIG9uIGEgY2FzZS1ieS1jYXNlIGJh\n" +
      "c2lzIHJlcXVlc3RzIHRvIHJlbW92ZSB0aGUgbGluayB0byB0aGF0IHNpdGUgZnJvbSBpdHMgaW5k\n" +
      "aWNlcy4gSG93ZXZlciwgaWYgdGhlIG9wZXJhdG9yIG9mIHRoZSBzaXRlIGRvZXMgbm90IHRha2Ug\n" +
      "c3RlcHMgdG8gcHJldmVudCBpdCwgdGhlIGF1dG9tYXRpYyBmYWNpbGl0aWVzIHVzZWQgdG8gY3Jl\n" +
      "YXRlIHRoZSBpbmRpY2VzIGFyZSBsaWtlbHkgdG8gZmluZCB0aGF0IHNpdGUgYW5kIGluZGV4IGl0\n" +
      "IGFnYWluIGluIGEgcmVsYXRpdmVseSBzaG9ydCBhbW91bnQgb2YgdGltZS4=";

  /* Test the basic encode and decode functions. */
  public void testFixed() throws Exception {
    String result = Base64.encode(input.getBytes());
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result), "UTF-8");
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement. */
  public void testWriteToByteArray() throws Exception {
    byte[] resultBytes = new byte[expect.length()];
    int length = Base64.encode(input.getBytes(), 0, input.length(),
        resultBytes, 0, Base64.ALPHABET, Integer.MAX_VALUE);

    assertTrue(length == expect.length());

    String result = new String(resultBytes, "UTF-8");
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result), "UTF-8");
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement,
   * specifying a destination offset. */
  public void testWriteToByteArray2() throws Exception {
    byte[] resultBytes = new byte[expect.length() + 9];
    int length = Base64.encode(input.getBytes(), 0, input.length(),
        resultBytes, 4, Base64.ALPHABET, Integer.MAX_VALUE);

    assertTrue(length == expect.length());

    String result = new String(resultBytes, 4, length, "UTF-8");
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result), "UTF-8");
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement,
   * specifying a source offset. */
  public void testWriteToByteArray3() throws Exception {
    byte[] sourceBytes = new byte[input.length() + 9];
    System.arraycopy(input.getBytes(), 0, sourceBytes, 5, input.length());

    byte[] resultBytes = new byte[expect.length()];
    int length = Base64.encode(sourceBytes, 5, input.length(),
        resultBytes, 0, Base64.ALPHABET, Integer.MAX_VALUE);

    assertTrue(length == expect.length());

    String result = new String(resultBytes, "UTF-8");
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result), "UTF-8");
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement w/newlines. */
  public void testWriteToByteArray4() throws Exception {
    byte[] resultBytes = new byte[expectNL.length()];
    int length = Base64.encode(input.getBytes(), 0, input.length(),
        resultBytes, 0, Base64.ALPHABET, 76);

    assertTrue(length == expectNL.length());

    String result = new String(resultBytes, "UTF-8");
    assertEquals(expectNL, result);

    String decode = new String(Base64.decode(result), "UTF-8");
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement,
   * specifying a destination offset and line length. */
  public void testWriteToByteArray5() throws Exception {
    byte[] resultBytes = new byte[expectNL.length() + 9];
    int length = Base64.encode(input.getBytes(), 0, input.length(),
        resultBytes, 4, Base64.ALPHABET, 76);

    assertTrue(length == expectNL.length());

    String result = new String(resultBytes, 4, length, "UTF-8");
    assertEquals(expectNL, result);

    String decode = new String(Base64.decode(result), "UTF-8");
    assertEquals(input, decode);
  }

  /* Test performance of encoder. This might still fail on slow machines. */
  public void testSpeed() throws Exception {
    byte[] input = new byte[5*1024*1024];
    byte[] output = new byte[4 + ((input.length * 4) / 3)];
    // Force these arrays to be paged in before starting the clock.
    System.arraycopy(input, 0, output, 0, input.length);
    System.arraycopy(input, 0, output, input.length,
                     output.length - input.length);
    Clock clock = new SystemClock();
    long start = clock.getTimeMillis();
    Base64.encode(input, 0, input.length, output, 0, Base64.ALPHABET,
                  Integer.MAX_VALUE);
    long duration = clock.getTimeMillis() - start;
    System.out.println("testSpeed: " + duration + " millisecs");
    // OriginalBase64Encoder used to run 20x longer than this one.
    // TODO: This threshold is already 10x longer than this test
    // takes on my machine, so I don't think this is a valid
    // regression test.  Fails on code coverage and Pulse.
    // assertTrue(duration < 300);
  }
}
