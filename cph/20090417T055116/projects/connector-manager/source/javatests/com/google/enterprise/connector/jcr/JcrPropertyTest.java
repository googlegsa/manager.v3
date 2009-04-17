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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.mock.jcr.MockJcrProperty;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple test fr JcrProperty
 */
public class JcrPropertyTest extends TestCase {

  public void testSimple() throws RepositoryException {
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
      MockJcrProperty testJCRProp = new MockJcrProperty(testProp);
      Property p = new JcrProperty(testJCRProp);
      Value v = p.nextValue();
      Assert.assertEquals("skeedle", v.toString());
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("baz");
      MockJcrProperty testJCRProp = new MockJcrProperty(testProp);
      Property p = new JcrProperty(testJCRProp);
      Value v = p.nextValue();
      Assert.assertEquals("42", v.toString());
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("abc");
      MockJcrProperty testJCRProp = new MockJcrProperty(testProp);
      Property p = new JcrProperty(testJCRProp);
      int counter = 0;
      Value v = null;
      while ((v = p.nextValue()) != null) {
        String res = v.toString();
        switch (counter) {
        case 0:
          Assert.assertEquals("2", res);
          break;
        case 1:
          Assert.assertEquals("3", res);
          break;
        case 2:
          Assert.assertEquals("5", res);
          break;
        case 3:
          Assert.assertEquals("7", res);
          break;
        case 4:
          Assert.assertEquals("11", res);
          break;
        }
        counter++;
      }
      Assert.assertEquals(5, counter);
    }

    {
      // TODO: date test
    }

    {
      MockRepositoryProperty testProp = pl.getProperty("ghi");
      MockJcrProperty testJCRProp = new MockJcrProperty(testProp);
      Property p = new JcrProperty(testJCRProp);
      Value v = p.nextValue();
      Assert.assertNull(v);
    }

  }
}
