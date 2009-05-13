// Copyright 2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.scheduler;

/**
 * A {@link Cancelable} that runs in a container and performs
 * completion logic when the container has fully completed
 * execution or cancellation.
 * @author EricStrellis@gmail.com (Eric Strellis)
 */
public interface Completable extends Cancelable{
  /**
   * Event to indicate the container has completed all processing for
   * this {@link Cancelable}. This will be called after one of.
   * <p>
   * <OL>
   * <LI> {@link Cancelable#run} returns
   * <LI> {@Link Cancelable#cancel} returns
   * </OL>
   */
  public void onCompletion();
}
