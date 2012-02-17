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
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.SecurityUtils;

import java.util.Map;
import java.util.Properties;

/**
 * Represents the peristent objects needed to instantiate a
 * {@link com.google.enterprise.connector.spi.Connector} instance.
 */
public class Configuration {
  private final String typeName;
  private final Map<String, String> configMap;
  private final String configXml;

  /**
   * Constructs a new {@link Configuration} from its individual components.
   *
   * @param typeName the {@link ConnectorType} name
   * @param configMap a {@code Map<String, String>} of configuration properties
   * @param configXml the contents of {@code connectorInstance.xml}
   */
  public Configuration(String typeName, Map<String, String> configMap,
      String configXml) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(typeName));
    Preconditions.checkNotNull(configMap);
    this.typeName = typeName;
    this.configMap = configMap;
    this.configXml = configXml;
  }

  /**
   * Constructs a new {@link Configuration} from a configuation map and an
   * existing the prototype configuration.
   *
   * @param configMap a {@code Map<String, String>} of configuration properties
   * @param prototype a prototype {@link Configuration}
   */
  public Configuration(Map<String, String> configMap, Configuration prototype) {
    Preconditions.checkNotNull(prototype);
    this.typeName = prototype.typeName;
    this.configMap = (configMap != null) ? configMap : prototype.configMap;
    this.configXml = prototype.configXml;
  }

  /**
   * Constructs a new {@link Configuration} by filling in any missing
   * fields of the existing configuration with those of the prototype.
   *
   * @param configuration a {@link Configuration}
   * @param prototype a prototype {@link Configuration}
   */
  public Configuration(Configuration configuration, Configuration prototype) {
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(prototype);
    Preconditions.checkArgument(prototype.typeName.equals(configuration.typeName),
                                "Configurations must be of the same type");
    this.typeName = (Strings.isNullOrEmpty(configuration.typeName))
                    ? prototype.typeName : configuration.typeName;
    this.configMap = (configuration.configMap != null)
                    ? configuration.configMap : prototype.configMap;
    this.configXml = (configuration.configXml != null)
                    ? configuration.configXml : prototype.configXml;
  }

  /**
   * Constructs a new {@link Configuration} by filling in any missing
   * fields of the existing configuration with those of the prototype XML.
   *
   * @param configuration a {@link Configuration}
   * @param prototypeXml a prototype connectorInstance.xml
   */
  public Configuration(Configuration configuration, String prototypeXml) {
    Preconditions.checkNotNull(configuration);

    this.typeName = configuration.typeName;
    this.configMap = configuration.configMap;
    this.configXml = (configuration.configXml != null)
                     ? configuration.configXml : prototypeXml;
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

  @Override
  public String toString() {
    return "{ type = " + typeName + ", configMap = "
           + SecurityUtils.getMaskedMap(configMap)
           + ", configXml = \"" + configXml + "\" }";
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((typeName == null)? 0 : typeName.hashCode());
    result = prime * result + ((configMap == null)? 0 : configMap.hashCode());
    result = prime * result + ((configXml == null)? 0 : configXml.hashCode());
    return result;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @return {@code true} if this object is the same as the {@code obj}
   *         argument; {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Configuration other = (Configuration) obj;
    return (compareObjs(typeName, other.typeName) &&
            compareObjs(configMap, other.configMap) &&
            compareObjs(configXml, other.configXml));
  }

  private static boolean compareObjs(Object obj1, Object obj2) {
    if (obj1 == null) {
      return (obj2 == null);
    } else {
      return obj1.equals(obj2);
    }
  }
}
