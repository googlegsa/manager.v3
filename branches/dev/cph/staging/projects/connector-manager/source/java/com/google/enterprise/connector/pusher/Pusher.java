// Copyright (C) 2006-2008 Google Inc.
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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for a Pusher - something that takes spi Documents
 * and sends them along on their way.
 */
public interface Pusher {

  /**
   * Takes an spi Document and pushes it along, presumably to the GSA Feed.
   *
   * @param document A Document
   * @param connectorName The name of the Connector sending the Document
   * @throws RepositoryException if transient error accessing the Repository
   * @throws RepositoryDocumentException if fatal error accessing the Document
   * @throws FeedException if a transient error occurs in the Pusher
   * @throws PushException if a transient error occurs in the Pusher
   */
  public void take(Document document, String connectorName)
      throws PushException, FeedException, RepositoryException;
}
