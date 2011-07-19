// Copyright (C) 2008 Google Inc.
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

/**
 * Interface for a data object to be sent to a {@link FeedConnection}.  The
 * implementation should contain methods to support a matching
 * {@link FeedConnection} implementation.
 */
public interface FeedData {

  /**
   * Returns an indication of the type of feed data encapsulated by this object.
   * Can be used by an associated {@link FeedConnection} object to determine how
   * to extract the data.
   *
   * @return an indication of the type of feed data encapsulated by this object.
   */
  public String getFeedType();

  /**
   * Returns the named source for the feed data.
   *
   * The data source name should match the regex [a-zA-Z_][a-zA-Z0-9_-]*,
   * the first character must be a letter or underscore, the rest of the
   * characters can be alphanumeric, dash, or underscore.
   *
   * @return the data source name.
   */
  public String getDataSource();

}
