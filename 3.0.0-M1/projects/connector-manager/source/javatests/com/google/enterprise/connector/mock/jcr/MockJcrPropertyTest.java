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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * Unit tests for MockJcrProperty
 */
public class MockJcrPropertyTest extends TestCase {
  public void testSimple() throws ValueFormatException, RepositoryException {
    String input = "{" + "baz: 42, " + "xyzzy: {type:string, value:skeedle}, "
        + "abc: {type:integer, value:[2, 3, 5, 7, 11]}, "
        + "def: {type:date, value:[10, 20, 30]}, "
        + "ghi: {type:integer, value:[]}, " + "}";
    JSONObject jo;
    try {
      jo = new JSONObject(input);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryPropertyList pl = new MockRepositoryPropertyList(jo);

    {
      MockRepositoryProperty testProp = pl.getProperty("xyzzy");
      Property testJCRProp = new MockJcrProperty(testProp);
      Assert.assertEquals("skeedle", testJCRProp.getString());
      Assert.assertEquals(PropertyType.STRING, testJCRProp.getType());
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("baz");
      Property testJCRProp = new MockJcrProperty(testProp);
      Assert.assertEquals(42, testJCRProp.getLong());
      Assert.assertEquals(PropertyType.LONG, testJCRProp.getType());
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("abc");
      Property testJCRProp = new MockJcrProperty(testProp);
      try {
        testJCRProp.getLong();
        // shouldn't get here - previous line should throw exception
        Assert.assertFalse(true);
      } catch (Exception e) {
        // Specifically, ValueFormatException should be thrown
        Assert.assertTrue(e instanceof ValueFormatException);
      }
      Value[] vs = testJCRProp.getValues();
      Assert.assertEquals(PropertyType.LONG, testJCRProp.getType());
      Assert.assertEquals(5, vs.length);
      Assert.assertEquals(2, vs[0].getLong());
      Assert.assertEquals(3, vs[1].getLong());
      Assert.assertEquals(5, vs[2].getLong());
      Assert.assertEquals(7, vs[3].getLong());
      Assert.assertEquals(11, vs[4].getLong());
      Assert.assertEquals(PropertyType.LONG, vs[4].getType());
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("def");
      Property testJCRProp = new MockJcrProperty(testProp);
      Value[] vs = testJCRProp.getValues();
      Assert.assertEquals(PropertyType.DATE, testJCRProp.getType());
      Assert.assertEquals(3, vs.length);
      Calendar date = vs[0].getDate();
      Calendar expected = Calendar.getInstance();
      expected.setTimeInMillis(10 * 1000);
      Assert.assertTrue(date.equals(expected));
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("ghi");
      Property testJCRProp = new MockJcrProperty(testProp);
      Value[] vs = testJCRProp.getValues();
      Assert.assertEquals(PropertyType.LONG, testJCRProp.getType());
      Assert.assertEquals(0, vs.length);
    }
  }

}
