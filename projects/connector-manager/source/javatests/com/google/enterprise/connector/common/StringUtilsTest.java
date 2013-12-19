// Copyright 2006 Google Inc.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.Logger;

public class StringUtilsTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(StringUtilsTest.class.getName());

  String testString = "now is the time for all \n"
      + "good men to come to the aid \n" + "# this is a comment \n"
      + "of their country // wow!\n";

  String expectedContents = "now is the time for all \n"
    + "good men to come to the aid \n" + "of their country \n";

  /** Test Illegal args to streamToString() */
  public void testIllegalArgs() {
    try {
      StringUtils.streamToString((String) null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected;
    }
    try {
      StringUtils.streamToString("");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected;
    }
    try {
      StringUtils.streamToString("nonexistent");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected;
    }
  }

  public void testStreamToString() {
    StringReader sr = new StringReader(testString);
    BufferedReader br = new BufferedReader(sr);
    String contents = StringUtils.streamToString(br);
    assertTrue(expectedContents.equals(contents));
  }

  public void testStreamToStringExactLen() throws Exception {
    StringReader sr = new StringReader(testString);
    BufferedReader br = new BufferedReader(sr);
    String contents = StringUtils.streamToString(br);
    assertTrue(expectedContents.equals(contents));
  }

  public void testStreamToStringFromFile() {
    String contents = StringUtils.streamToString("testFile1.txt");
    logger.info(contents);
    logger.info("length of contents " + contents.length());
    String expectedContents = "now is the time for all \n"
      + "good men to come to the aid\n" + "of their country\n";
    logger.info(expectedContents);
    logger.info("length of expected contents "
      + expectedContents.length());
    assertTrue(expectedContents.equals(contents));
  }

  public void testReadAllToString() throws Exception {
    String contents =
        StringUtils.readAllToString(new StringReader(expectedContents));
    assertTrue(expectedContents.equals(contents));
  }

  /** Test read that may overflow internal buffer. */
  public void testBounderyConditions() throws Exception {
    checkBigRead(0);
    checkBigRead(1);
    checkBigRead(StringUtils.BUFFER_SIZE - 1);
    checkBigRead(StringUtils.BUFFER_SIZE);
    checkBigRead(StringUtils.BUFFER_SIZE + 1);
    checkBigRead((2 * StringUtils.BUFFER_SIZE) - 1);
    checkBigRead(2 * StringUtils.BUFFER_SIZE);
    checkBigRead((2 * StringUtils.BUFFER_SIZE) + 1);
  }

  private void checkBigRead(int len) throws Exception {
    byte[] input = new byte[len];
    if (len > 0) {
      Arrays.fill(input, (byte) 'x');
      input[0] = 'a';
      input[len - 1] = 'z';
    }
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    String output = StringUtils.streamToStringAndThrow(bais);
    assertEquals(new String(input, "UTF-8"), output);
  }
}
