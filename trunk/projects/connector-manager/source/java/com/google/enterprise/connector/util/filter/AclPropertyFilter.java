// Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.util.filter;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spiimpl.PrincipalValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Document} filter that forces the {@link CaseSensitivityType} field
 * for all ACL {@link com.google.enterprise.connector.spi.Principal Principals}
 * supplied by the connector to be set to a specified value.
 * <p/>
 * This will over-ride any {@link CaseSensitivityType} that may have been set
 * by the connector, and supply one if none was set by the connector.
 * <p/>
 * <b>Example {@code documentFilters.xml} Configurations:</b>
 * <p/>
 * The following example forces all ACL Principals for all fed documents to be
 * considered case-insensitive.
 * <pre><code>
   &lt;!-- Force case-insensitive ACLs. --&gt;
   &lt;bean id="CaseInsensitiveACLs"
      class="com.google.enterprise.connector.util.filter.AclPropertyFilter"&gt;
     &lt;property name="caseSensitivityType" value="everything-case-insensitive"/&gt;
   &lt;/bean&gt;
   </code></pre>
 *
 * @since 3.0
 */
public class AclPropertyFilter extends AbstractDocumentFilter {

  /** The logger for this class. */
  private static final Logger LOGGER = Logger.getLogger(
      AclPropertyFilter.class.getName());

  /** Case sensitivity type flag. */
  protected CaseSensitivityType caseSensitivityType;

  private static final Set<String> aclUsersGroups;

  static {
    aclUsersGroups = new HashSet<String>();
    aclUsersGroups.add(SpiConstants.PROPNAME_ACLUSERS);
    aclUsersGroups.add(SpiConstants.PROPNAME_ACLDENYUSERS);
    aclUsersGroups.add(SpiConstants.PROPNAME_ACLGROUPS);
    aclUsersGroups.add(SpiConstants.PROPNAME_ACLDENYGROUPS);
  }

  /**
   * Sets the {@link CaseSensitivityType} to be used for all ACL
   * {@link com.google.enterprise.connector.spi.Principal Principals}
   * supplied by the connector.
   *
   * @param caseSensitivityType a String representation of a
   *        {@link CaseSensitivityType}; i.e. {@code everything-case-sensitive}
   *        or {@code everything-case-insensitive}.
   */
  public void setCaseSensitivityType(String caseSensitivityType) {
    if (caseSensitivityType.equalsIgnoreCase(
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE.toString())) {
      this.caseSensitivityType = 
          CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE;
    } else if (caseSensitivityType.equalsIgnoreCase(
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE.toString())) {
      this.caseSensitivityType = CaseSensitivityType.EVERYTHING_CASE_SENSITIVE;
    }
  }

  /**
   * Finds a {@link Property} by {@code name}. If the requested property is 
   * ACL property, then checks for case sensitivity type and modifies the value
   * if different than specified in filter. Returns ACL property with modified 
   * values.
   * <p/>
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    if (caseSensitivityType == null) {
      return source.findProperty(name);
    }

    if (aclUsersGroups.contains(name)) {
      List<Value> values = super.getPropertyValues(source, name);
      ArrayList<Value> newValues = new ArrayList<Value>(values.size());
      for (Value value : values) {
        Principal principal = (value instanceof PrincipalValue)
            ? ((PrincipalValue) value).getPrincipal()
            : new Principal(value.toString().trim());

        if (principal.getCaseSensitivityType() != caseSensitivityType) {
          if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Changing " + principal.getName()
                + " caseSensitivityType to " + caseSensitivityType);
          }

          Principal newPrincipal = new Principal(principal.getPrincipalType(),
              principal.getNamespace(), principal.getName(),
                  caseSensitivityType);

          newValues.add(new PrincipalValue(newPrincipal));
        } else {
          newValues.add(new PrincipalValue(principal));
        }
      }
      return new SimpleProperty(newValues);
    } else {
      return source.findProperty(name);
    }
  }
}
