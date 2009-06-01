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
   * No Delay
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

  // TODO (strellis): remove this when Traverser.runBatch is converted to
  // return a structured result.
  /**
   * Converts a legacy traversal delay policy as returned by
   * {@link QueryTraverser#runBatch(int)} to a {@link TraversalDelayPolicy}. The
   * legacy policy follows these conventions:
   * <OL>
   * <LI>If legacyPolicy is less than or equal to 0 then 0 or more documents
   * were returned by the batch and the next batch may proceed without delay.
   * <LI>If legacyPolicy equals {@link Traverser#ERROR_WAIT} (-2) an error
   * occurred and the next batch should delay for the error retry interval.
   * <LI>If legacyPolicy equals {@link Traverser#POLLING_WAIT}(-1) the traversal
   * completed and the batch should wait for the connectors retry delay
   * interval.
   * </OL>
   */
  static TraversalDelayPolicy getTraversalDealyPolicyFromLegacyBatchResult(
      int legacyPolicy) {
    if (legacyPolicy > 0) {
      legacyPolicy = 0;
    }
    return values()[-legacyPolicy];
  }
}
