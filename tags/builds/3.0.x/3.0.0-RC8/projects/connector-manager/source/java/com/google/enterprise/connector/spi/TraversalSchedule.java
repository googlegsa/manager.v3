// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * A traversal schedule for a connector.
 *
 * @since 3.0
 */
public interface TraversalSchedule {

  /** Returns the target traversal rate in documents per minute. */
  public int getTraversalRate();

  /** Returns the number of seconds to delay after finding no new content. */
  public int getRetryDelay();

  /** Returns {@code true} if the traversal schedule is disabled. */
  public boolean isDisabled();

  /**
   * Returns {@code true} if the current time is within a scheduled traversal
   * interval.
   */
  public boolean inScheduledInterval();

  /**
   * Returns the number of seconds until the next scheduled traversal interval.
   * A return value of 0 (zero) indicates the current time is within a scheduled
   * traversal interval.  A returned value of -1 indicates there is no next
   * traversal interval.
   */
  public int nextScheduledInterval();

  /**
   * Returns {@code true} if traversals could run at this time,
   * equivalent to <pre>!isDisabled() && inScheduledInterval()</pre>.
   */
  public boolean shouldRun();
}


