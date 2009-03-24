// Copyright 2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.common;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *  Unit Test for iHarder Base64
 */
public class Base64Test extends TestCase {

  /**
   *  Construct a JUnit Test Suite
   *
   * @return    The test suite
   */
  public static Test suite() {
    return new TestSuite(Base64Test.class);
  }

  static String input = " Google's indices consist of information that has been" +
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

  static String expect = "IEdvb2dsZSdzIGluZGljZXMgY29uc2lzdCBvZiBpbmZvcm1hdGlvbi" +
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


  /* Test the basic encode and decode functions. */
  public void testFixed() throws Exception {
    String result = Base64.encode(input.getBytes());
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result));
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement. */
  public void testWriteToByteArray() throws Exception {
    byte[] resultBytes = new byte[expect.length()];
    int length = Base64.encode(input.getBytes(), 0, input.length(),
                               resultBytes, 0, Base64.ALPHABET);

    assertTrue(length == expect.length());

    String result = new String(resultBytes);
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result));
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement. */
  public void testWriteToByteArray2() throws Exception {
    byte[] resultBytes = new byte[expect.length() + 9];
    int length = Base64.encode(input.getBytes(), 0, input.length(),
                               resultBytes, 4, Base64.ALPHABET);

    assertTrue(length == expect.length());

    String result = new String(resultBytes, 4, length);
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result));
    assertEquals(input, decode);
  }

  /* Test my special write-to-preallocated array enhancement,
     specifying a source offset.
  */
  public void testWriteToByteArray3() throws Exception {
    byte[] sourceBytes = new byte[input.length() + 9];
    System.arraycopy(input.getBytes(), 0, sourceBytes, 5, input.length());

    byte[] resultBytes = new byte[expect.length()];
    int length = Base64.encode(sourceBytes, 5, input.length(),
                               resultBytes, 0, Base64.ALPHABET);

    assertTrue(length == expect.length());

    String result = new String(resultBytes);
    assertEquals(expect, result);

    String decode = new String(Base64.decode(result));
    assertEquals(input, decode);
  }

  public void testSpeed() throws Exception {
    byte[] input = new byte[5*1024*1024];
    byte[] output = new byte[4 + ((input.length * 4) / 3)];
    long start = System.currentTimeMillis();
    Base64.encode(input, 0, input.length, output, 0, Base64.ALPHABET);
    long duration = System.currentTimeMillis() - start;
    System.out.println("testSpeed: " + duration + " millisecs");
    // Old Base64Encoder used to run 6x longer
    assertTrue(duration < 50);
  }
}