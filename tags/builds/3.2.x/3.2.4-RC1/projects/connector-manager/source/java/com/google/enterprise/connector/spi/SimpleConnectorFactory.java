// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.spi;

import java.util.Map;

/**
 * Simple implementation of the {@link ConnectorFactory} interface.
 * Connector developers may want to use this to implement unit tests.
 *
 * @since 2.4
 */
public class SimpleConnectorFactory implements ConnectorFactory {
  private final Connector instance;

  /**
   * Constructs a factory without a connector instance. A subclass
   * that calls this constructor should also override the {@link
   * #makeConnector} method.
   */
  protected SimpleConnectorFactory() {
    this(null);
  }

  public SimpleConnectorFactory(Connector instance) {
    this.instance = instance;
  }

  /**
   * @throws RepositoryException if a subclass overrides this method and throws
   *         RepositoryException.
   */
  @Override
  public Connector makeConnector(Map<String, String> config)
      throws RepositoryException {
    return instance;
  }
}
