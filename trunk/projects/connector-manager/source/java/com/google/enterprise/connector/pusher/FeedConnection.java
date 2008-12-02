// Copyright (C) 2006-2008 Google Inc.
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

import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for a feed connection. This takes in a data source name and a data
 * source object that contains the data to be sent.  The actual connection to
 * the feed server should be established by the implementation during
 * construction or initialization.
 */
public interface FeedConnection {

  /**
   * Sends data contained in the given data object identified as the given data
   * source name.
   * @param dataSource the name of the data source.  The data source name should
   *    match the regex [a-zA-Z_][a-zA-Z0-9_-]*, the first character must be a
   *    letter or underscore, the rest of the characters can be alphanumeric,
   *    dash, or underscore.
   * @param feedData an object that encapsulates the feed data that needs to be
   *    sent by the <code>FeedConnection</code>.
   * @return response from the feed server.
   * @throws FeedException if problem extracting the data or sending it.
   * @throws RepositoryException if problem retrieving data from the Connector.
   */
  public String sendData(String dataSource, FeedData feedData)
      throws FeedException, RepositoryException;
}
