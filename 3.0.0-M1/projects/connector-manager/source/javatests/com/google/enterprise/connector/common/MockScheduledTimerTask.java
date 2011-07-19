// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.common;

/** This implementation does nothing but register calls. */
class MockScheduledTimerTask extends ScheduledTimerTask {
  private final long delay;
  private final long period;
  private boolean isTriggered = false;

  public MockScheduledTimerTask(long delay, long period) {
    this.delay = delay;
    this.period = period;
  }

  @Override
  public void run() {
    isTriggered = true;
  }

  @Override
  public long getDelay() {
    return delay;
  }

  @Override
  public long getPeriod() {
    return period;
  }

  /**
   * Gets whether {@code detect} has been called.
   *
   * @return {@code true} if {@code detect} has been called, and
   * {@code false} if it has not been called
   */
  public boolean isTriggered() {
    return isTriggered;
  }
}
