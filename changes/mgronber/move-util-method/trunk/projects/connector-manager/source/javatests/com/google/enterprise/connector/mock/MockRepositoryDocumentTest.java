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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Unit tests for MockRepositoryDocument
 */
public class MockRepositoryDocumentTest extends TestCase {
  /**
   * Test the Json constructor
   *
   */
  public void testJsonConstructor() {
    String in = "{timestamp:50, docid:doc3, content:\"\", name:foo}";
    JSONObject jo;
    try {
      jo = new JSONObject(in);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryDocument document = new MockRepositoryDocument(jo);
    Assert.assertEquals("doc3", document.getDocID());
    MockRepositoryProperty property = document.getProplist()
      .getProperty("name");
    Assert.assertEquals("foo", property.getValue());
    Assert.assertEquals("", document.getContent());
    Assert
      .assertEquals(new MockRepositoryDateTime(50), document.getTimeStamp());
  }
}
