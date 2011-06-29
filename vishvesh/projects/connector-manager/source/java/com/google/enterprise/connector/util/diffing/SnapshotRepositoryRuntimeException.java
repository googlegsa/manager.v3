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
package com.google.enterprise.connector.util.diffing;

/**
 * Exception for reporting a problem (such as loss of network
 * connectivity to a repository) that precludes getting or using
 * the {@link java.util.Iterator} for a {@link SnapshotRepository}
 *
 * @since 2.8
 */
public class SnapshotRepositoryRuntimeException extends RuntimeException{
  /**
   * Constructs a {@link SnapshotRepositoryRuntimeException}.
   *
   * @param message the message
   * @param cause the root cause of the exception
   */
  public SnapshotRepositoryRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
