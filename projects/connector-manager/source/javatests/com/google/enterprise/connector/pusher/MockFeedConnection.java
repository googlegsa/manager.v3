// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import java.io.InputStream;
import java.io.IOException;

/**
 * Mock <code>GsaFeedConnection</code> that expects to be given a
 * <code>GsaFeedData</code> data object.  Stores the associated data stream into
 * an internal buffer for later comparison.
 */
public class MockFeedConnection implements FeedConnection {

  StringBuffer buf = null;

  public String getFeed() {
    String result;
    if (buf == null) {
      result = "";
    }
    result = buf.toString();
    buf = new StringBuffer(2048);
    return result;
  }

  public MockFeedConnection() {
    buf = new StringBuffer(2048);
  }

  public String sendData(String dataSource, FeedData feedData)
      throws RepositoryException {
    try {
      InputStream data = ((GsaFeedData)feedData).getData();
      String dataStr = StringUtils.streamToStringAndThrow(data);
      buf.append(dataStr);
      System.out.println(dataStr);
    } catch (IOException e) {
      throw new RepositoryDocumentException("I/O error reading data", e);
    }
    return GsaFeedConnection.SUCCESS_RESPONSE;
  }
}
