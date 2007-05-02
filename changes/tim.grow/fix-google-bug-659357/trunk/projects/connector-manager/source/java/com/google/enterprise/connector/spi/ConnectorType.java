// Copyright (C) 2006 Google Inc.
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


import java.util.Map;

/**
 * The root of the SPI for connector configuration. The connector manager will
 * use <a href="http://www.springframework.org/">Spring</a> to instantiate
 * objects that implement this interface. The implementor MUST provide a Spring
 * XML configuration file named connectorType.xml to control this process. See
 * the package documentation for more details.
 */
public interface ConnectorType {

  /**
   * Get initial configuration form snippet.
   * 
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object.
   */
  public ConfigureResponse getConfigForm(String language);

  /**
   * Get populated configuration form snippet.
   * 
   * @param configMap A map of name, value pairs (String, String) of
   *        configuration data
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object. The form must be prepopulated with the
   *         supplied data in the map.
   */
  public ConfigureResponse getPopulatedConfigForm(Map configMap, String language);

  /**
   * Validates config data and returns a new form snippet and error message if
   * needed.
   * 
   * @param configData A map of name, value pairs (String, String) of
   *        configuration data
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigureResponse object. If the returned object is null, this
   *         means that the configuration is acceptable. If the return is
   *         non-null, then the response contains a new form snippet (and
   *         message, as appropriate)
   */
  public ConfigureResponse validateConfig(Map configData, String language);

}
