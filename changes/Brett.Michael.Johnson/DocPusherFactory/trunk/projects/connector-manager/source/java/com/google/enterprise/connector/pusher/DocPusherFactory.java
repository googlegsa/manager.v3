// Copyright (C) 2009 Google Inc.
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

import com.google.enterprise.connector.traversal.FileSizeLimitInfo;

/**
 * Factory that creates {@link DocPusher} instances that feed
 * {@link FeedConnection}.
 */
// TODO: Support multiple sinks where different connector instances
// might feed different sinks.
public class DocPusherFactory implements PusherFactory {
  /**
   * FeedConnection that is the sink for our generated XmlFeeds.
   */
  private final FeedConnection feedConnection;

  /**
   * Configured maximum document size and maximum feed file size supported.
   */
  private final FileSizeLimitInfo fileSizeLimit;

  /**
   * Creates a {@code DocPusherFactory} object from the specified
   * {@code feedConnection}.
   *
   * @param feedConnection a FeedConnection
   */
  public DocPusherFactory(FeedConnection feedConnection) {
    this(feedConnection, new FileSizeLimitInfo());
  }

  /**
   * Creates a {@code DocPusherFactory} object from the specified
   * {@code feedConnection}.  The supplied {@link FileSizeLimitInfo} specifies
   * constraints as to the size of a Document's content and the size of
   * generated Feed files.
   *
   * @param feedConnection a FeedConnection
   * @param fileSizeLimitInfo FileSizeLimitInfo constraints on document content
   *        and feed size.
   */
  public DocPusherFactory(FeedConnection feedConnection,
                          FileSizeLimitInfo fileSizeLimit) {
    this.feedConnection = feedConnection;
    this.fileSizeLimit = fileSizeLimit;
  }

  //@Override
  public Pusher newPusher(String dataSource) throws PushException {
    return new DocPusher(feedConnection, dataSource, fileSizeLimit);
  }
}
