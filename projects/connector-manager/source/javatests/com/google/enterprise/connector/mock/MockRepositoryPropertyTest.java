// Copyright (C) 2006-2009 Google Inc.
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

package com.google.enterprise.connector.mock;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Unit tests for properties
 */
public class MockRepositoryPropertyTest extends TestCase {
  private static final Logger logger =
    Logger.getLogger(MockRepositoryPropertyTest.class.getName());
  /**
   * Test the JSON constructor
   */
  public void testJsonConstructor() {
    String input = "{\"foo\"      : \"bar\"," +
            "\"baz\":42, " +
            "blip:blop, " +
            "xyzzy: {type:string, value:skeedle}, " +
            "pow: {type:string, value:[wow,yow,skeedle]}, " +
            "abc: {type:integer, value:[2, 3, 5, 7, 11]}, " +
            "def: {type:date, value:[10, 20, 30]}, " +
            "ghi: {type:string, value:[]}, " +
            "} ";
    JSONObject jo;
    try {
      jo = new JSONObject(input);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    for (Iterator<?> keys = jo.keys(); keys.hasNext(); ) {
      String name = (String) keys.next();
      Object value;
      try {
        value = jo.get(name);
      } catch (JSONException e) {
        throw new IllegalArgumentException("test input can not be parsed");
      }
      MockRepositoryProperty p = new MockRepositoryProperty(name, value);
      logger.info("Created property " + p);
    }
    logger.info("Input object " + jo);
  }

  /**
   * Test multiple-valued property functionality
   */
  public void testMultiValues() {
    String input = "{type:string, value:skeedle}";
    JSONObject jo;
    try {
      jo = new JSONObject(input);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryProperty p = new MockRepositoryProperty("xyzzy", jo);
    String[] values = p.getValues();
    Assert.assertEquals(false, p.isRepeating());
    Assert.assertEquals(1, values.length);
    Assert.assertEquals("skeedle", values[0]);

    input = "{type:integer, value:[2, 3, 5, 7, 11]}";
    try {
      jo = new JSONObject(input);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    p = new MockRepositoryProperty("xyzzy", jo);
    values = p.getValues();
    Assert.assertEquals(true, p.isRepeating());
    Assert.assertEquals(5, values.length);
    Assert.assertEquals("7", values[3]);

    input = "{type:string, value:[]}";
    try {
      jo = new JSONObject(input);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    p = new MockRepositoryProperty("xyzzy", jo);
    values = p.getValues();
    Assert.assertEquals(true, p.isRepeating());
    Assert.assertEquals(0, values.length);
  }

}
