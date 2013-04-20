// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

/**
 * Factory that returns a {@link DocumentFilterFactory}, including filters
 * definded by the connector instance, if any.
 */
public interface DocumentFilterFactoryFactory {

  /**
   * Gets a {@link DocumentFilterFactory} with the globally defined
   * document filters found in the Connector Manager's
   * {@code documentFilters.xml} advanced configuration.
   *
   * @return a {@link DocumentFilterFactory}
   */
  public DocumentFilterFactory getDocumentFilterFactory();

  /**
   * Gets a {@link DocumentFilterFactory}, including the globally defined
   * document filters and any filters definded by the connector instance
   * in the connector's advanced configuration.
   *
   * @param connectorName the name of a connector instance
   * @return a {@link DocumentFilterFactory}
   */
  public DocumentFilterFactory getDocumentFilterFactory(String connectorName);
}
