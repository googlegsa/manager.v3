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

import com.google.enterprise.connector.common.ByteArraysOutputStream;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import java.io.IOException;

/**
 * Mock <code>GsaFeedConnection</code> that expects to be given a
 * <code>GsaFeedData</code> data object.  Stores the associated data stream into
 * an internal buffer for later comparison.
 */
public class MockFeedConnection implements FeedConnection {

  private final StringBuffer buf = new StringBuffer(2080);

  public String getFeed() {
    String result = buf.toString();
    buf.setLength(0);
    return result;
  }

  public MockFeedConnection() {
  }

  public String sendData(String dataSource, FeedData feedData)
      throws RepositoryException {
    ByteArraysOutputStream data = ((GsaFeedData)feedData).getData();
    String dataStr = data.toString();
    buf.append(dataStr);
    System.out.println(dataStr);
    return GsaFeedConnection.SUCCESS_RESPONSE;
  }
}
