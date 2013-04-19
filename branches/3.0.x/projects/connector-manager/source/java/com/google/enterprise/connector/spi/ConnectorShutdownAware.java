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

package com.google.enterprise.connector.spi;

/**
 * A {@link Connector} object may implement this interface if it wishes
 * to be informed when the Connector instance is being shut down
 * or deleted.
 *
 * @since 2.0
 */
public interface ConnectorShutdownAware {

  /**
   * Instructs the Connector to shutdown.  Any open connection with the ECM
   * should be closed.  Resources held should be released.
   * Shutdown notification will happen when the Connector Manager shuts down,
   * during Connector re-configuration, and before Connector removal.
   * The Connector should take care when releasing static resources that
   * may be shared across Connector instances, as several instances of
   * the ConnectorType may be active at one time.
   *
   * @throws RepositoryException
   */
  public void shutdown() throws RepositoryException;

  /**
   * The Connector instance is being removed.  The Connector should delete
   * any files it may have created in its work directory (as specified by
   * the {@code googleConnectorWorkDir} configuration property), stored
   * Preferences, etc.
   */
  public void delete() throws RepositoryException;
}
