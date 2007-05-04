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

/**
 * TraversalContext.  This is an interface to a callback object that 
 * the Connector Manager will pass in to a QueryTraversalManager, which 
 * the manager can the use to call back to get information from the 
 * Connector Manager.  Thus, a connector developer does not need to
 * provide an implementation of this object.  However, for testing 
 * purposes, the developer may want to provide a temporary implementation.
 */
public interface TraversalContext {
  /**
   * Gets a size limit for contents passed through the connector framework. 
   * If a developer has a way of asking the repository for the size of
   * a content file before fetching it, then a comparison with this size
   * would save the developer the cost of fetching a content that is too 
   * big to be used.
   * @return The size limit in bytes
   */
  long maxDocumentSize();
  
  /**
   * Gets information about whether a mime type is supported.  Non-positive
   * numbers mean that there is no support for this mime type.  At present,
   * supported mimetypes will return 1, but larger numbers are reserved to
   * indicate preferred types.
   * @return The support level - non-positive means no support
   */  
  int mimeTypeSupportLevel(String mimeType);
}
