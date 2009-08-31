// Copyright 2008 Google Inc. All Rights Reserved.
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

/**
 * Class used to encapsulate the feed data to be sent using the
 * <code>GsaFeedConnection</code>.
 */
public class GsaFeedData implements FeedData {
  private String feedType;
  private ByteArraysOutputStream data;

  public GsaFeedData(String feedType, ByteArraysOutputStream data) {
    this.feedType = feedType;
    this.data = data;
  }

  public String getFeedType() {
    return feedType;
  }
  public void setFeedType(String feedType) {
    this.feedType = feedType;
  }
  public ByteArraysOutputStream getData() {
    return data;
  }
  public void setData(ByteArraysOutputStream data) {
    this.data = data;
  }
}
