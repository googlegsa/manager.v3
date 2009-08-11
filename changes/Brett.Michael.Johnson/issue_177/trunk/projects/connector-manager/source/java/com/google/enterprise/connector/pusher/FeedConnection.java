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
   *
   * @param dataSource the name of the data source. The data source name should
   *        match the regex [a-zA-Z_][a-zA-Z0-9_-]*, the first character must
   *        be a letter or underscore, the rest of the characters can be
   *        alphanumeric, dash, or underscore.
   * @param feedData an object that encapsulates the feed data that needs to be
   *        sent by the <code>FeedConnection</code>.
   * @return response from the feed server.
   * @throws FeedException if problem extracting the data or sending it.
   * @throws RepositoryException if problem retrieving data from the Connector.
   */
  public String sendData(String dataSource, FeedData feedData)
      throws FeedException, RepositoryException;

  /**
   * Returns true if the Feed host has large number of unprocessed Feed items.
   * The Feed host may temporarily stop processing Feed items during periodic
   * maintenance, when resetting the index, during system configuration, or
   * due to certain error conditions. If backlogged, the Feed client may choose
   * to throttle back its feeds until the backlog clears.
   *
   * @return true if the Feed host is known to be backlogged processing feeds,
   *         false otherwise.
   */
  public boolean isBacklogged();

  /**
   * Return the version of serialized {@code Schedule} string supported.
   * The Schedule versions are:
   *  <ul>
   *  <li>0 - Unknown</li>
   *  <li>1 - <code>connectorName:hostLoad:timeIntervals...</code></li>
   *  <li>2 - <code>connectorName:hostLoad:retryDelayMillis:timeIntervals...</code>
   *          adds retryDelayMillis.</li>
   *  <li>3 - <code>#connectorName:hostLoad:retryDelayMillis:timeIntervals...</code>
   *          where leading '#' indicates disabled schedule, and a
   *          retryDelayMillis value of -1 indicates traverse to until
   *          no new content, then automatically disable.</li>
   *  </ul>
   *
   * @return Schedule version number supported.
   */
  public int getScheduleFormat();

  /**
   * Return a String consisting of a comma-separated list supported content
   * encodings.  For instance: "base64binary, base64compressed".
   *
   * @return supported content encodings.
   */
  public String getContentEncodings();

}
