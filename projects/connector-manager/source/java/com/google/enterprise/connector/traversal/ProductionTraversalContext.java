// Copyright 2007-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.spi.SimpleTraversalContext;

import java.util.Set;

/**
 * Wrapper for the context provided by the Connector Manager to the traversal
 * process (if the developer requests it by the TraversalContextAware
 * interface).
 * <p>
 * This class is quasi-immutable - in practice it is initialized by
 * the setters during connector manager start up and never changes afterwards.
 */
public class ProductionTraversalContext extends SimpleTraversalContext {
  private MimeTypeMap mimeTypeMap = new MimeTypeMap();

  public synchronized void setFileSizeLimitInfo(
      FileSizeLimitInfo fileSizeLimitInfo) {
    setMaxDocumentSize(fileSizeLimitInfo.maxDocumentSize());
  }

  public synchronized void setMimeTypeMap(MimeTypeMap mimeTypeMap) {
    this.mimeTypeMap = mimeTypeMap;
  }

  /* @Override */
  public synchronized int mimeTypeSupportLevel(String mimeType) {
    return mimeTypeMap.mimeTypeSupportLevel(mimeType);
  }

  /* @Override */
  public synchronized String preferredMimeType(Set<String> mimeTypes) {
    return mimeTypeMap.preferredMimeType(mimeTypes);
  }
}
