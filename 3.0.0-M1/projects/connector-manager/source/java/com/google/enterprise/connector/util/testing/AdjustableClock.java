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

package com.google.enterprise.connector.util.testing;

import com.google.enterprise.connector.util.Clock;

/**
 * An adjustable clock to allow better testing.
 *
 * @since 3.0
 */
public class AdjustableClock implements Clock {
  private long adjustment;

  /**
   * Constructor that sets the initial time to the current time.
   */
  public AdjustableClock() {
    adjustment = 0;
  }

  /**
   * Constructor that sets the current time to the supplied time.
   *
   * @param initialTime the time, in milliseconds since midnight,
   *        January 1, 1970 UTC
   */
  public AdjustableClock(long initialTime) {
    setTimeMillis(initialTime);
  }

  /**
   * Sets the current time, in milliseconds.
   *
   * @param currentTime the time, in milliseconds since midnight,
   *        January 1, 1970 UTC
   */
  public synchronized void setTimeMillis(long currentTime) {
    adjustment = currentTime - System.currentTimeMillis();
  }

  /**
   * Adjust the current time.  If the {@code adjustment} is positive,
   * the clock advances.  If the {@code adjustment} is negative, you
   * go back in time.
   *
   * @param adjustment the adjustment, in milliseconds.
   */
  public synchronized void adjustTime(long adjustment) {
    this.adjustment += adjustment;
  }

  /**
   * Return the current time, in milliseconds.
   */
  /* @Override */
  public synchronized long getTimeMillis() {
    return (System.currentTimeMillis() + adjustment);
  }
}
