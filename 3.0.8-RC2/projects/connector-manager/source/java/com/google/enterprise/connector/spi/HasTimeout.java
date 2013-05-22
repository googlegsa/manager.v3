// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * HasTimeout.  This should be implemented by a TraversalManager if it wants
 * to supply the ConnectorManager with its own timeout value.
 *
 * @since 1.0
 * @deprecated This Interface has been fully deprecated.  It is never called.
 */
@Deprecated
public interface HasTimeout {
  /**
   * Gets the connector's preferred timeout.
   *
   * @return the timeout value, in milliseconds
   */
  public int getTimeoutMillis();
}
