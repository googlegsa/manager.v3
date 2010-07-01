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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Map;

/**
 * Represents the peristent objects needed to instantiate a
 * {@link com.google.enterprise.connector.spi.Connector} instance.
 */
public class Configuration {
  private final String typeName;
  private final Map<String, String> configMap;
  private final String configXml;

  public Configuration(String typeName, Map<String, String> configMap,
      String configXml) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(typeName));
    Preconditions.checkNotNull(configMap);

    this.typeName = typeName;
    this.configMap = configMap;
    this.configXml = configXml;
  }

  /**
   * Gets the connector type name.
   *
   * @return the connector type name
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Gets the connector instance property map.
   *
   * @return the config data, which may be empty or {@code null}
   */
  public Map<String, String> getMap() {
    return configMap;
  }

  /**
   * Gets the connector instance XML document.
   *
   * @return the connector instance XML document, which may be {@code null}
   */
  public String getXml() {
    return configXml;
  }
}
