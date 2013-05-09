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

import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;
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

  /** user domain. */
  protected String userDomain;
  protected boolean overwriteUserDomain = false;

  private static final Set<String> aclUsers;
  private static final Set<String> aclUsersGroups;

  static {
    aclUsers = new HashSet<String>();
    aclUsers.add(SpiConstants.PROPNAME_ACLUSERS);
    aclUsers.add(SpiConstants.PROPNAME_ACLDENYUSERS);

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
   * Sets the domain for users for ACL Principals.
   * 
   * @param userDomain the domain name to set for user principals.
   * @since 3.0.8
   */
  public void setUserDomain(String userDomain) {
    this.userDomain = Strings.emptyToNull(userDomain);
  }

  /**
   * Sets the overwrite value flag.
   * 
   * @param overwriteUserDomain the overwrite flag. Overwrites the existing
   *        domain values if true, or preserves existing domain values 
   *        if false.
   * @since 3.0.8
   */
  public void setOverwriteUserDomain(boolean overwriteUserDomain) {
    this.overwriteUserDomain = overwriteUserDomain;
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
    if (caseSensitivityType == null && userDomain == null) {
      return source.findProperty(name);
    }

    if (aclUsersGroups.contains(name)) {
      List<Value> values = super.getPropertyValues(source, name);
      ArrayList<Value> newValues = new ArrayList<Value>(values.size());
      for (Value value : values) {
        Principal principal = (value instanceof PrincipalValue)
            ? ((PrincipalValue) value).getPrincipal()
            : new Principal(value.toString().trim());

        if (caseSensitivityType != null &&
            principal.getCaseSensitivityType() != caseSensitivityType) {
          if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Changing " + principal.getName()
                + " caseSensitivityType to " + caseSensitivityType);
          }

          Principal newPrincipal = new Principal(principal.getPrincipalType(),
              principal.getNamespace(), getPrincipalName(principal, name),
                  caseSensitivityType);

          newValues.add(new PrincipalValue(newPrincipal));
        } else {
          Principal newPrincipal = new Principal(principal.getPrincipalType(),
                  principal.getNamespace(), getPrincipalName(principal, name),
                  principal.getCaseSensitivityType());
          newValues.add(new PrincipalValue(newPrincipal));
        }
      }
      return new SimpleProperty(newValues);
    } else {
      return source.findProperty(name);
    }
  }

  private String getPrincipalName(Principal principal, String name) {
    String principalName = principal.getName();
    if (aclUsers.contains(name)) {
      if (userDomain == null
          || principal.getPrincipalType() == PrincipalType.UNQUALIFIED) {
        return principalName;
      } else {
        String userName;
        if (principalName.contains("\\")) {
          userName = principalName.substring(principalName.indexOf("\\") + 1);
        } else if (principalName.contains("@")) {
          userName = principalName.substring(0, principalName.indexOf("@"));
        } else {
          userName = principalName;
        }
        if (overwriteUserDomain) {
          return userDomain + "\\" + userName;
        } else if (principalName.contains("\\")) {
          return principalName;
        } else {
          return userDomain + "\\" + userName;
        }
      }
    } else {
      return principalName;
    }
  }
}
