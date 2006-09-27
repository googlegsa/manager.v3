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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.spi.PropertyMap;

/**
 * Interface for a pusher - something that takes spi documents
 * and sends them along on their way
 */
public interface Pusher {
  
  /**
   * Takes an spi document and pushes it along, presumably to the GSA
   * @param pm A property map the represent a document.
   * @param connectorName The name of the connector sending the document
   */
  public void take(PropertyMap pm, String connectorName);

}
