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

package com.google.enterprise.connector.util.diffing;

/**
 * Enumerations for reasons for filtering a document rather than
 * sending it to the GSA.
 *
 * @since 2.8
 */
public enum FilterReason {
  /** File path does note match start patterns or matches exclude patterns. */
  PATTERN_MISMATCH,

  /** File is larger than the maximum allowed. */
  TOO_BIG,

  /** File has an unsupported mime type. */
  UNSUPPORTED_MIME_TYPE,

  /** IO error occurred while processing. */
  IO_EXCEPTION
}
