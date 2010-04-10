// Copyright 2009 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.traversal;

/**
 * Policy for determining how long to delay before running the next batch for a
 * connector instance.
 */
public enum TraversalDelayPolicy {
  /**
   * No Delay.
   */
  IMMEDIATE,

  /**
   * Delay appropriate for the end of a traversal or other situations the
   * connector should provide extra delay before running the next batch.
   */
  POLL,

  /**
   * Delay appropriate for after a possibly transient error. This will provide
   * extra delay to avoid quickly hitting the problem again before it has been
   * corrected.
   */
  ERROR;
}
