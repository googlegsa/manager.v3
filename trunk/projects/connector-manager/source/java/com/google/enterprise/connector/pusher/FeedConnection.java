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

package com.google.enterprise.connector.pusher;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for a url connection. This takes in data and url to which the 
 * data needs to be sent.
 */
public interface FeedConnection {

  /**
   * Open a connection to this url and sends data.
   * @param dataSource see
   * http://code.google.com/enterprise/documentation/feedsguide.html
   * @param feedType see
   * http://code.google.com/enterprise/documentation/feedsguide.html
   * @param data see
   * http://code.google.com/enterprise/documentation/feedsguide.html
   * @return response from the server.
   * @throws IOException
   */
  public String sendData(String dataSource, String feedType, InputStream data)
      throws IOException;
  
}
