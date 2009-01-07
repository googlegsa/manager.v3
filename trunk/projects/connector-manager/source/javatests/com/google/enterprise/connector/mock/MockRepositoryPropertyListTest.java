// Copyright (C) 2006 Google Inc.
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

import com.google.enterprise.connector.mock.MockRepositoryProperty.PropertyType;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Unit tests for property lists
 */
public class MockRepositoryPropertyListTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(MockRepositoryPropertyListTest.class.getName());

  /**
   * Basic property tests
   */
  public void testPropsSimple () {
    MockRepositoryProperty [] pa1 = {
      new MockRepositoryProperty("author",
        PropertyType.STRING,
        "Lionel Hardcastle"),
      new MockRepositoryProperty("title",
        PropertyType.STRING,
        "My Life in Kenya")
    };
    MockRepositoryProperty [] pa2 = {
      new MockRepositoryProperty("author",
        PropertyType.STRING,
        "Herman Melville"),
      new MockRepositoryProperty("title",
        PropertyType.STRING,
        "Moby Dick")
      };
    MockRepositoryProperty [] pa3 = {
      new MockRepositoryProperty("author",
        PropertyType.STRING,
        "William Shakespeare")
    };

    MockRepositoryPropertyList pl1 = new MockRepositoryPropertyList(pa1);
    MockRepositoryPropertyList pl2 = new MockRepositoryPropertyList(pa2);
    MockRepositoryPropertyList pl3 = new MockRepositoryPropertyList(pa3);

    verifyProperty(pl1, "author", "Lionel Hardcastle");
    verifyProperty(pl2, "title", "Moby Dick");
    verifyProperty(pl3, "author", "William Shakespeare");

    verifyProperty(pl1, "genre", null);

    MockRepositoryProperty p1 =
      new MockRepositoryProperty("genre",
                                 PropertyType.STRING,
                                 "Autobiography");
    pl1.setProperty(p1);
    verifyProperty(pl1, "genre", "Autobiography");

    pl1.merge(pl2);
    verifyProperty(pl1, "title", "Moby Dick");
    verifyProperty(pl1, "genre", "Autobiography");

  }

  private void verifyProperty(MockRepositoryPropertyList pl,
      String key, String value) {
    String actualValue = pl.lookupStringValue(key);
    assertTrue("Proplist should have value " + value + " for key " + key,
      (actualValue == null ? value == null : actualValue.equals(value)));
  }

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
    MockRepositoryPropertyList pl = new MockRepositoryPropertyList(jo);
    logger.info("Constructed property list " + pl);
    logger.info("Input object " + jo);
  }

}
