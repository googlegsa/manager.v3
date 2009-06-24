// Copyright (C) 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

/**
 * A {@link Runnable} that supports cancellation and timeout.
 */
public interface TimedCancelable extends Runnable {
  /**
   * Cancel the operation performed by this {@link Runnable}.
   * While this {@link Runnable#run} method is running in one thread this
   * may be called in another so implementors must provide any needed
   * synchronization.
   */
  public void cancel();

  /**
   * Complete the operations performed by this {@link Runnable} due to the
   * expiration of its time interval. While this {@link Runnable#run} method is
   * running in one thread this may be called in another so implementors must
   * provide any needed synchronization.
   */
  public void timeout(TaskHandle taskHandle);
}
