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

package com.google.enterprise.connector.util.filter;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SkippedDocumentException;

/**
 * An interface for factories that create {@link Document} filters.
 * Document filter acts to transform the information retrieved from
 * its source document.  Typically document filters would add,
 * remove, or modify a Document's {@link Property Properties},
 * including the document content.  A document filter might even
 * throw {@link SkippedDocumentException} to prevent a document
 * from being fed to the GSA.
 * <p/>
 * Multiple document filters may be chained together, forming
 * a transformational document processing pipeline.
 *
 * @see DocumentFilterChain
 * @since 2.8
 */
public interface DocumentFilterFactory {
  /**
   * Returns a new {@link Document} that acts as a filter for the
   * supplied {@code source} Document.
   *
   * @param source the input {@link Document} for the filter - must not be
   *        {@code null}
   * @return a document filter
   */
  public Document newDocumentFilter(Document source);
}
