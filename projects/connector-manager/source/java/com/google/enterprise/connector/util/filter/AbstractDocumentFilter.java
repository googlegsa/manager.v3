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
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Set;
import java.util.logging.Logger;

/**
 * A base {@link Document} filter implementation that does nothing.
 * It is meant to used as a base class for document filter subclasses.
 * Subclasses are exected to override {@code findProperty} and/or
 * {@code getPropertyNames}, but not {@code newDocumentFilter}.
 */
public abstract class AbstractDocumentFilter implements DocumentFilterFactory {

  /** The logger for this class. */
  protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());

  /* @Override */
  public Document newDocumentFilter(Document source) {
    Preconditions.checkNotNull(source, "Source document must not be null");
    return new DocumentFilter(source);
  }

  /**
   * Finds a {@link Property} by {@code name}. If the {@code source}
   * {@link Document} has a property of that name, then that property
   * is returned.
   * <p/>
   * Filter subclasses are likely to override this method.
   *
   * @param source the source {@link Document} for this filter
   * @param name the name of the property to find
   * @return the Property, if found; {@code null} otherwise
   * @throws RepositoryException if a repository access error occurs
   * @throws RepositoryDocumentException if a document has fatal
   *         processing errors
   */
  /* @VisibleForJavaDoc */
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    return source.findProperty(name);
  }

  /**
   * Gets the set of names of all {@link Property Properties} in the
   * {@code source} {@link Document}.
   * <p/>
   * Filter subclasses are likely to override this method.
   *
   * @param source the source {@link Document} for this filter
   * @return the names, as a Set of Strings
   * @throws RepositoryException if a repository access error occurs
   * @throws RepositoryDocumentException if a document has fatal
   *         processing errors
   */
  /* @VisibleForJavaDoc */
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    return source.getPropertyNames();
  }

  @Override
  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  /**
   * A {@link Document} implementation that calls back to the outer class
   * {@code findProperty()} and {code getPropertyNames()} methods, which
   * are likely to be overridden by subclasses.
   */
  private class DocumentFilter implements Document {

    /** The {@link Document} that acts as the source for this filter. */
    protected Document source;

    /**
     * Constructs a {@link DocumentFilter} with the supplied {@code source}
     * Document.
     *
     * @param source the source {@link Document} for this filter
     */
    public DocumentFilter(Document source) {
      this.source = source;
    }

    /* @Override */
    public Property findProperty(String name) throws RepositoryException {
      return AbstractDocumentFilter.this.findProperty(source, name);
    }

    /* @Override */
    public Set<String> getPropertyNames() throws RepositoryException {
      return AbstractDocumentFilter.this.getPropertyNames(source);
    }
  }
}
