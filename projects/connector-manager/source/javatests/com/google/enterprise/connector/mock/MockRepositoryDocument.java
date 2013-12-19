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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.pusher.XmlFeed;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

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
  /*
   * The Repository document can handle both returning either a String or an
   * InputStream.  Callers can call either and will realize the String or
   * InputStream on demand.
   */
  private String content;
  private String contentFile;
  private InputStream inputStream;
  private String docid;
  private MockRepositoryPropertyList proplist;

  /**
   * Get the content as a String.  The getContentStream() method should be used
   * in favor of this method.
   * @return the content as a String
   */
  public String getContent() {
    if (null == content || 0 == content.length()) {
      try {
        InputStream is = getContentStream();
        if (null != is) {
          content = StringUtils.streamToString(is);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return content;
  }

  public InputStream getContentStream() throws FileNotFoundException {
    if (null == inputStream) {
      if (null == contentFile || 0 == contentFile.length()) {
        try {
          if (null == content) {
            return null;
          }
          inputStream =
            new ByteArrayInputStream(
              content.getBytes(XmlFeed.XML_DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        inputStream = new FileInputStream(contentFile);
      }
    }
    return inputStream;
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
    this.contentFile = proplist.lookupStringValue("contentfile");
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
    this.contentFile = jo.optString("contentfile");
    jo.remove("contentfile");
    if (0 != this.content.length() && 0 != this.contentFile.length()) {
      throw new IllegalArgumentException("Only one of content or contentfile " +
            "should be set.");
    }
    this.proplist = new MockRepositoryPropertyList(jo);
  }
}
