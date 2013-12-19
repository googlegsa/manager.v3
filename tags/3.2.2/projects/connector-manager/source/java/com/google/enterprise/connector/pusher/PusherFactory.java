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
 * Interface for a factory that creates {@link Pusher} instances for use by a
 * {@link com.google.enterprise.connector.traversal.Traverser Traverser}.
 */
public interface PusherFactory {
  /**
   * Create a new {@link Pusher} instance appropriate for the supplied
   * dataSource.
   *
   * @param dataSource a data source for a {@code Feed}, typically the name
   *        of a connector instance.
   * @return a {@link Pusher}
   * @throws PushException if no {@link Pusher} is assigned to the 
   *         {@code dataSource}.
   */
  public Pusher newPusher(String dataSource) throws PushException;
}
