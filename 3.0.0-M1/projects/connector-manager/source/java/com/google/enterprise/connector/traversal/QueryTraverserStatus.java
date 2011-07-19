// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.traversal;

/**
 * An interface which can be passed to a Connector's TraversalManager
 * to interact with the ConnectorManager (CM), to provide guidance on its
 * management, in particular, the timeout which the Connector requests.
 */
public interface QueryTraverserStatus {

  /**
   * Request not to be timed out for the specified number of seconds.
   * The ConnectorManager does not guarantee to heed the request, since it
   * may have other scheduling considerations which override.
   * @param timeoutRequested number of seconds requested
   */
  public void requestTimeout(long timeoutRequested);

  /**
   * Inform ConnectorManager of the current status of the traversal
   * @param percentDone non-negative number in [0 .. 100]
   */
  public void setStatus(double percentDone);
}
