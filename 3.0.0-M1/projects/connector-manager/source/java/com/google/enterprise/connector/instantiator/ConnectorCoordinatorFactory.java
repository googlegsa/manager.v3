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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.spi.Connector;

/**
 * Interface for Factories that construct ConnectorCoordinator instances.
 */
public interface ConnectorCoordinatorFactory {
  /**
   * Factory method constructs a new {@link ConnectorCoordinator}
   * for the named {@link Connector} instance.
   *
   * @param connectorName the Connector instance name.
   * @return a new ConnectorCoordinator.
   */
  public ConnectorCoordinator newConnectorCoordinator(String connectorName);
}
