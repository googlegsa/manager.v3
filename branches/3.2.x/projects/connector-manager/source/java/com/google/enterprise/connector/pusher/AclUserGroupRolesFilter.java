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
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.PrincipalValue;
import com.google.enterprise.connector.util.filter.AbstractDocumentFilter;

import java.util.LinkedList;
import java.util.Set;

/**
 * A {@link DocumentFilter} that converts a set of document properties that
 * look like:
 * <pre>
 *   google:aclusers=[joe, mary, admin]
 *   google:user:roles:joe=[reader]
 *   google:user:roles:mary=[reader, writer]
 *   google:user:roles:admin=[owner]
 * </pre>
 * into one property that looks like:
 * <pre>
 *   google:aclusers=[joe=reader, mary=reader, mary=writer, admin=owner]
 * </pre>
 * or
 * <pre>
 *   google:aclgroups=[sales, support, eng]
 *   google:group:roles:sales=[reader]
 *   google:group:roles:support=[reader, writer]
 *   google:group:roles:eng=[owner]
 * </pre>
 * into one property that looks like:
 * <pre>
 *   google:aclgroups=[sales=reader, support=reader, support=writer, eng=owner]
 * </pre>
 */
public class AclUserGroupRolesFilter extends AbstractDocumentFilter {

  private static Predicate<String> rolesPredicate = new Predicate<String>() {
    @SuppressWarnings("deprecation")
    public boolean apply(String input) {
      return !(input.startsWith(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX) ||
               input.startsWith(SpiConstants.USER_ROLES_PROPNAME_PREFIX));
    }
  };

  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    return Sets.filter(source.getPropertyNames(), rolesPredicate);
  }

  @Override
  @SuppressWarnings("deprecation")
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    if (SpiConstants.PROPNAME_ACLGROUPS.equals(name)) {
      return processAclProperty(source, name,
                                SpiConstants.GROUP_ROLES_PROPNAME_PREFIX);
    } else if (SpiConstants.PROPNAME_ACLUSERS.equals(name)) {
      return processAclProperty(source, name,
                                SpiConstants.USER_ROLES_PROPNAME_PREFIX);
    } else {
      return source.findProperty(name);
    }
  }

  /**
   * @param document the document being processed.
   * @param aclPropName the name of the property being processed. Should be one
   *        of {@link SpiConstants#PROPNAME_ACLGROUPS} or
   *        {@link SpiConstants#PROPNAME_ACLUSERS}.
   * @return either the original property if no conversion was necessary or a
   *         new converted property containing ACL Entries.
   * @throws RepositoryException if there was a problem extracting properties.
   */
  private static Property processAclProperty(Document document,
      String aclPropName, String aclRolePrefix) throws RepositoryException {
    LinkedList<Value> acl = new LinkedList<Value>();
    Property scopeProp = document.findProperty(aclPropName);
    Value scopeVal;
    while ((scopeVal = scopeProp.nextValue()) != null) {
      Principal principal = (scopeVal instanceof PrincipalValue)
          ? ((PrincipalValue) scopeVal).getPrincipal()
          : new Principal(scopeVal.toString().trim());
      String aclScope = principal.getName();
      if (Strings.isNullOrEmpty(aclScope)) {
        continue;
      }
      Property scopeRoleProp = document.findProperty(aclRolePrefix + aclScope);
      if (scopeRoleProp != null) {
        // Add ACL Entry (scope=role pair) to the list.
        Value roleVal;
        while ((roleVal = scopeRoleProp.nextValue()) != null) {
          String role = roleVal.toString().trim();
          if (role.length() > 0) {
            acl.add(Value.getPrincipalValue(new Principal(
                principal.getPrincipalType(),
                principal.getNamespace(), aclScope + '=' + role,
                principal.getCaseSensitivityType())));
          } else {
            // XXX: Empty role implies reader?
            acl.add(scopeVal);
          }
        }
      } else {
        // No roles for this scope; just add scope to the list.
        acl.add(scopeVal);
      }
    }
    return new SimpleProperty(acl);
  }
}
