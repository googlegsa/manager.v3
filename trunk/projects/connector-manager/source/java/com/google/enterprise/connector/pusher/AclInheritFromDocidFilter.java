// Copyright 2013 Google Inc.
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
import com.google.common.collect.Sets;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.filter.AbstractDocumentFilter;

import java.util.Set;

/**
 * A {@link DocumentFilter} that uses the
 * {@link SpiConstants.PROPNAME_ACLINHERITFROM_DOCID},
 * {@link SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE}, and
 * {@link SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT} properties to
 * construct an {@link SpiConstants.PROPNAME_ACLINHERITFROM} property
 * value, if one does not already exist.
 */
public class AclInheritFromDocidFilter extends AbstractDocumentFilter {
  private static Predicate<String> propsPredicate = new Predicate<String>() {
    public boolean apply(String input) {
      return !(input.equals(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID)
          || input.equals(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE)
          || input.equals(SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT));
    }
  };

  private final UrlConstructor urlConstructor;

  public AclInheritFromDocidFilter(UrlConstructor urlConstructor) {
    this.urlConstructor = urlConstructor;
  }

  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    return Sets.union(
        Sets.filter(source.getPropertyNames(), propsPredicate),
        Sets.<String>newHashSet(SpiConstants.PROPNAME_ACLINHERITFROM));
  }

  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    if (SpiConstants.PROPNAME_ACLINHERITFROM.equals(name)) {
      return new SimpleProperty(
          Value.getStringValue(urlConstructor.getInheritFromUrl(source)));
    } else {
      return source.findProperty(name);
    }
  }
}
