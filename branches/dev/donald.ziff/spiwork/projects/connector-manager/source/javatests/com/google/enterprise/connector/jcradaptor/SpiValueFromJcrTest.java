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

package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.mock.jcr.MockJcrProperty;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.old.Property;
import com.google.enterprise.connector.spi.old.Value;
import com.google.enterprise.connector.spi.old.ValueType;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Simple unit tests for SpiValueFromJcr
 */
public class SpiValueFromJcrTest extends TestCase {

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
      Property p = new SpiPropertyFromJcr(testJCRProp);
      Value v = p.getValue();
      String expectedContents = "skeedle";
      Assert.assertEquals(expectedContents, v.getString());
      Assert.assertEquals(ValueType.STRING, v.getType());
      InputStream stream = v.getStream();
      String streamContents = StringUtils.streamToString(stream);
      Assert.assertEquals(expectedContents, streamContents);
    }


  }

}
