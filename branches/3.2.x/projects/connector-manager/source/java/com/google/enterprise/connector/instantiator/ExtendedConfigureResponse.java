// Copyright 2011 Google Inc.
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

import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.spi.ConfigureResponse;

/**
 * A {@link ConfigureResponse} that also carries a connectorInstance.xml.
 *
 * @see ConnectorType
 */
public class ExtendedConfigureResponse extends ConfigureResponse {
  private final String configXml;

  /**
   * Constructor that builds an {@link ExtendedConfigureResponse} from a
   * {@link ConfigureResponse} and a configuration XML string.
   *
   * @param response a {@link ConfigureResponse}
   * @param configXml An XML String containing the connectorInstance.xml for
   *        the connector instance.
   */
  public ExtendedConfigureResponse(ConfigureResponse response,
                                   String configXml) {
    super(response.getMessage(), response.getFormSnippet(),
          response.getConfigData());
    this.configXml = (getFormSnippet() != null || getConfigData() != null)
                     ? configXml : null;
  }

  /**
   * Constructor that builds an {@link ExtendedConfigureResponse} from a
   * {@link ConfigureResponse} and a Configuration.
   *
   * @param response a {@link ConfigureResponse}
   * @param configuration a {@link Configuration}
   */
  public ExtendedConfigureResponse(ConfigureResponse response,
                                   Configuration configuration) {
    super(response.getMessage(), response.getFormSnippet(),
          (response.getConfigData() != null) ? response.getConfigData()
                                             : configuration.getMap());
    this.configXml = configuration.getXml();
  }

  /**
   * Gets the config XML.
   *
   * @return the config XML - may be {@code null} or empty
   */
  public String getConfigXml() {
    return configXml;
  }
}