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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

/**
 * Dynamically assembles a chain of {@link DocumentFilter}s that transform
 * ACL properties based upon the ACL support in the current GSA.  This also
 * transforms legacy user and group roles into aclusers and aclgroups syntax.
 */
public class AclTransformFilter implements DocumentFilterFactory {
  private final UrlConstructor urlConstructor;
  private final AclInheritFromDocidFilter aclInheritFromDocidFilter;
  private final AclUserGroupRolesFilter aclUserGroupRolesFilter;

  public AclTransformFilter(UrlConstructor urlConstructor) {
    this.urlConstructor = urlConstructor;

    // Construct the DocumentFilterFactories.
    aclInheritFromDocidFilter = new AclInheritFromDocidFilter(urlConstructor);
    aclUserGroupRolesFilter = new AclUserGroupRolesFilter();
  }

  /**
   * Constructs a ACL property procssing pipeline, assembled from filters
   * determined by the GSA ACL support and current document features.
   *
   * @param source the input {@link Document} for the filters
   * @return the head of the chain of filters
   */
  @Override
  @SuppressWarnings("deprecation") // For the role properties.
  public Document newDocumentFilter(Document source)
      throws RepositoryException {
    Preconditions.checkNotNull(source);
    Document filter = source;

    // If connector supplies ACLINHERITFROM_DOCID instead of ACLINHERITFROM
    // property, add a filter that builds an ACLINHERTITFROM property from
    // ACLINHERITFROM_DOCID, ACLINHERITFROM_FEEDTYPE and
    // ACLINHERITFROM_FRAGMENT.
    if (Strings.isNullOrEmpty(DocUtils.getOptionalString(source,
        SpiConstants.PROPNAME_ACLINHERITFROM))
        && !Strings.isNullOrEmpty(DocUtils.getOptionalString(source,
        SpiConstants.PROPNAME_ACLINHERITFROM_DOCID))) {
      filter = aclInheritFromDocidFilter.newDocumentFilter(filter);
    }

    // If the connector supplies old style user and group roles, transform them.
    for (String name : source.getPropertyNames()) {
      if (name.startsWith(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX) ||
          name.startsWith(SpiConstants.USER_ROLES_PROPNAME_PREFIX)) {
        filter = aclUserGroupRolesFilter.newDocumentFilter(filter);
        break;
      }
    }

    return filter;
  }
}
