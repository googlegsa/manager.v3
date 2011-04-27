// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util;

/**
 * {@link Clock} implementation that returns {@link System#currentTimeMillis()}.
 *
 * @since 2.8
 */
public class SystemClock implements Clock {
  /** A {@code SystemClock} instance. */
  public static Clock INSTANCE = new SystemClock();

  /** @return the current time in milliseconds */
  /* @Override */
  public long getTimeMillis() {
    return System.currentTimeMillis();
  }
}
