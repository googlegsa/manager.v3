// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util;

import com.google.enterprise.connector.spi.TraversalContext;

/**
 * A simple timer to control traversal loops and avoid batch timeouts.
 *
 * @since 3.0
 */
public class TraversalTimer {
  private static long defaultTime = 60L * 4L;

  /** Sets the default time in seconds. */
  public static void setDefaultTime(long time) {
    defaultTime = time;
  }

  private final long endTime;

  /**
   * Constructs a timer using the time limit of the {@link TraversalContext}.
   *
   * @param traversalContext the traversal context, which may be {@code null}
   */
  public TraversalTimer(TraversalContext traversalContext) {
    long seconds = (traversalContext != null)
        ? traversalContext.traversalTimeLimitSeconds() / 2 : defaultTime;
    endTime = System.currentTimeMillis() + 1000L * seconds;
  }

  /**
   * Gets whether the timer is still ticking.
   *
   * @return {@code true} if the timer interval has not elapsed, or
   * {@code false} if it has elapsed
   */
  public boolean isTicking() {
    return System.currentTimeMillis() < endTime;
  }

  /**
   * Gets whether the timer is expired. This is the opposite of
   * {@link #isTicking}.
   *
   * @return {@code true} if the timer interval has elapsed, or
   *     {@code false} if it has not elapsed
   */
  public boolean isExpired() {
    return System.currentTimeMillis() >= endTime;
  }
}
