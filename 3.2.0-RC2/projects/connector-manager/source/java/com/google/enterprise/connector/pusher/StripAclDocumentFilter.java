// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.pusher;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.util.filter.AbstractDocumentFilter;

import java.util.Set;

/**
 * A DocumentFilter that strips all ACL properties from the Document's
 * Properties.
 */
public class StripAclDocumentFilter extends AbstractDocumentFilter {
  private static Predicate<String> predicate =
      Predicates.not(DocUtils.aclPredicate);

  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    return Sets.filter(source.getPropertyNames(), predicate);
  }

  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    return (predicate.apply(name)) ? source.findProperty(name) : null;
  }
}
