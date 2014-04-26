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

import java.util.Locale;
import java.util.Map;

/**
 * The root of the SPI for connector configuration. The connector manager will
 * use <a href="http://www.springframework.org/">Spring</a> to instantiate
 * objects that implement this interface. The implementor <strong>must</strong>
 * provide a Spring XML configuration file named {@code connectorType.xml} to
 * control this process. See the package documentation for more details.
 *
 * @since 1.0
 */
public interface ConnectorType {

  /**
   * Get initial configuration form snippet.
   * @param locale a java.util.Locale which the implementation may use to
   *        produce appropriate descriptions and messages
   *
   * @return a ConfigureResponse object
   */
  public ConfigureResponse getConfigForm(Locale locale);

  /**
   * Get populated configuration form snippet. Note, the values contained in the
   * given {@code configMap} will be raw - that is, they will not be encoded and
   * may contain special XML characters. It is the responsibility of the
   * implementation to properly replace any special characters in the
   * {@code configMap} values with predefined entities when they are added to
   * the created form snippet within the returned {@link ConfigureResponse}.
   *
   * @param configMap A map of name, value pairs (String, String) of
   *        configuration data.  Note that configuration Map key names
   *        beginning with "google" are reserved for use by the Connector
   *        Manager.
   * @param locale a java.util.Locale which the implementation may use to
   *        produce appropriate descriptions and messages
   * @return a ConfigureResponse object. The form must be prepopulated with the
   *         supplied data in the map.
   * @see XmlUtils#xmlAppendAttrValue(String, Appendable)
   */
  public ConfigureResponse getPopulatedConfigForm(Map<String, String> configMap,
                                                  Locale locale);

  /**
   * Validates config data and returns a new form snippet and error message if
   * needed.
   *
   * @param configData a {@link java.util.Map} of name, value pairs
   *        (String, String) of configuration data.  The configData map may
   *        contain configuration data items that did not originate from the
   *        original configForm, specifically, additional entries with names
   *        that begin with "google".
   * @param locale a {@link java.util.Locale} which the implementation may use
   *        to produce appropriate descriptions and messages
   * @param connectorFactory a {@link ConnectorFactory} object that can be used
   *        by the {@link ConnectorType} to construct a {@link Connector}
   *        instance, instantiated by the Connector Manager in exactly the
   *        same way as it would if this config were valid and persisted.
   * @return a {@link ConfigureResponse} object. If the returned object is
   *         {@code null}, this means that the configuration is acceptable.
   *         If the returned object is non-{@code null}, then the response
   *         contains a new form snippet (and message, as appropriate).
   *         If the returned object is non-{@code null}, and the response
   *         contains only a {@code Map} of configData (but no message
   *         or form snippet), then the returned configuration is acceptable,
   *         but may be different than the supplied configData.  If a modified
   *         configuration map is returned, it must preserve configuration
   *         items with names beginning with "google".
   * @since 1.0.1
   */
  public ConfigureResponse validateConfig(Map<String, String> configData,
      Locale locale, ConnectorFactory connectorFactory);
}
