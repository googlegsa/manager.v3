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

/**
 * Factory that creates {@link DocPusher} instances that feed
 * {@link FeedConnection}.
 */
// TODO: Support multiple sinks where different connector instances
// might feed different sinks.
public class DocPusherFactory implements PusherFactory {
  private final FeedConnection feedConnection;

  public DocPusherFactory(FeedConnection feedConnection) {
    this.feedConnection = feedConnection;
  }

  //@Override
  public Pusher newPusher(String dataSource) throws PushException {
    return new DocPusher(feedConnection, dataSource);
  }
}
