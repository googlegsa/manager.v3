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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Mock document object for unit tests.
 * <p>
 * Document model: a document has String content, a String ID, a
 * timestamp, and a list of miscellaneous
 * properties (which may be empty)
 * <p>
 * Almost all of this code is boiler-plate: a constructor and getters.
 */
public class MockRepositoryDocument {
  private MockRepositoryDateTime timeStamp;
  private String content;
  private String docid;
  private MockRepositoryPropertyList proplist;

  public String getContent() {
    return content;
  }

  public String getDocID() {
    return docid;
  }

  public MockRepositoryPropertyList getProplist() {
    return proplist;
  }

  public MockRepositoryDateTime getTimeStamp() {
    return timeStamp;
  }

  public MockRepositoryDocument(MockRepositoryDateTime timeStamp, 
                                String docid,
                                String content,
                                MockRepositoryPropertyList proplist) {
    this.timeStamp = timeStamp;
    this.docid = docid;
    this.content = content;
    this.proplist = proplist;
  }

  public MockRepositoryDocument(JSONObject jo) {
    try {
      this.timeStamp = new MockRepositoryDateTime(jo.getInt("timestamp"));
    } catch (JSONException e) {
      throw new IllegalArgumentException("JSON object missing timestamp");
    }
    jo.remove("timestamp");
    try {
      this.docid = jo.getString("docid");
    } catch (JSONException e) {
      throw new IllegalArgumentException("JSON object missing docid");
    }
    jo.remove("docid");
    this.content = jo.optString("content");
    jo.remove("content");
    this.proplist = new MockRepositoryPropertyList(jo);
  }
}
