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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SkippedDocumentException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link DocumentFilterChain} constructs a chain of {@link Document}
 * filters.  The filters are constructed from a {@link List} of
 * {@link DocumentFilterFactory DocumentFilterFactories}, and linked
 * together like pop-beads, each using the previous as its source Document.
 */
public class DocumentFilterChain implements DocumentFilterFactory {

  // The list of factories used to construct the filter chain.
  private final List<? extends DocumentFilterFactory> factories;

  /**
   * Constructs an empty {@link DocumentFilterChain}. Documents will
   * will pass through unchanged.
   */
  public DocumentFilterChain() {
    this.factories = Collections.emptyList();
  }

  /**
   * Constructs a {@link DocumentFilterChain} that uses the supplied
   * List of {@link DocumentFilterFactory DocumentFilterFactories}
   * assemble a document filter chain.
   *
   * @param factories a List of {@link DocumentFilterFactory}
   */
  public DocumentFilterChain(List<? extends DocumentFilterFactory> factories) {
    Preconditions.checkNotNull(factories);
    this.factories = factories;
  }

  /**
   * Constructs a document procssing pipeline, assembled from filters fetched
   * from each of the {@link DocumentFilterFactory DocumentFilterFactories}
   * in the list.  Returns the head of the chain.  The supplied {@code source}
   * Document will be the input for the tail of the chain.
   *
   * @param source the input {@link Document} for the filters
   * @return the head of the chain of filters
   */
  /* @Override */
  public Document newDocumentFilter(Document source) {
    Preconditions.checkNotNull(source);
    for (DocumentFilterFactory factory : factories) {
      source = factory.newDocumentFilter(source);
    }
    return source;
  }

  @Override
  public String toString() {
    return "DocumentFilterChain: " + factories.toString();
  }
}
