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
 * Configuration interface for an SPI implementation.
 * 
 * @author ziff@google.com (Your Name Here)
 * 
 */
public interface Configurer {

  /**
   * Get initial configuration form snippet.
   * 
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigurerResponse object. The status will be ignored.
   */
  public ConfigurerResponse getConfigForm(String language);

  /**
   * Get configuration data in form snippet to edit. This is different from
   * getConfigForm that this is to change configuration of a running Connector
   * instance, not to configurate a new Connector instance. If this data is
   * handled by ConnectorManager, we may not need this in Connector SPI.
   * 
   * @param connectorName The connector to update
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigurerResponse object. The status should be:
   *         <ul>
   *         <li> STATUS_OK: if all is well. As above, the message and form may
   *         be null or empty. If the form is null or empty, then the caller
   *         will use a default form.
   *         <li> STATUS_CONNECTOR_NOT_FOUND: if no such connector is found. In
   *         this case, the rest of the result is the same as if the generic
   *         getConfigForm() had been called.
   *         </ul>
   *         Note: It is an error to return STATUS_TRY_AGAIN from this call.
   */
  public ConfigurerResponse getConfigFormForConnector(String connectorName,
      String language);

  /**
   * Set config data for a new Connector or update config data for a running
   * Connector instance
   * 
   * @param connectorName The connector to update
   * @param configData A map of name, value pairs (String, String) of
   *        configuration data to submit
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @return a ConfigurerResponse object. The status should be:
   *         <ul>
   *         <li> STATUS_OK: if all is well and the configuration has been
   *         successfully stored. In this case, the message and form snippet are
   *         expected to be null, and will be ignored.
   *         <li> STATUS_TRY_AGAIN: if the connector requires more
   *         configuration. In this case, it is an error to return a null or
   *         empty form snippet.
   *         </ul>
   *         Note: It is an error to return STATUS_CONNECTOR_NOT_FOUND from this
   *         call.
   */
  public ConfigurerResponse setConfig(String connectorName, Map configData,
      String language);

}
