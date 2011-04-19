// Copyright 2010 Google Inc.
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

/**
 * If a {@link Connector} implements this marker interface, then the Connector
 * Manager will call the setter when it instantiates it, supplying a
 * {@link ConnectorPersistentStore} object, which encapsulates how a connector
 * can persist and use information about documents or any other information.
 * <p/>
 * <strong>Note:</strong> The Connector Manager calls {@code setDatabaseAccess}
 * after the Connector has been instantiated by Spring, but before calling
 * {@link Connector#login()}.  If the {@link Connector} bean configuration
 * specifies {@code dependency-check="all"} or if {@code setDatabaseAccess}
 * has been annotated {@code @Required}, the bean configuration must
 * specify a default value, such as {@code <null/>}, to satisfy the dependency
 * during bean construction.
 *
 * @since 2.8
 */
public interface ConnectorPersistentStoreAware {
  void setDatabaseAccess(ConnectorPersistentStore databaseAccess);
}
