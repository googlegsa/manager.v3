// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.spi;

import java.util.Collection;

/**
 * The response from the
 * {@link AuthenticationManager#authenticate AuthenticationManager.authenticate}
 * method.
 *
 * @since 1.0
 */
public class AuthenticationResponse {

  private final boolean valid;
  private final String data;
  private final Collection<?> groups;

  /**
   * Makes an {@code AuthenticationResponse}.
   *
   * @param valid  indicates that authentication was successful (valid)
   * @param data   Reserved for future use.  May be set but will be ignored.
   */
  public AuthenticationResponse(boolean valid, String data) {
    this(valid, data, null);
  }

  /**
   * Makes an {@code AuthenticationResponse}.
   * The supplied {@code Collection} of groups may be either:
   * <ul>
   * <li>{@code Collection<String>} for a simple collection of group names</li>
   * <li>{@code Collection<Principal>} for groups with namespaces</li>
   * </ul>
   *
   * @param valid  indicates that authentication was successful (valid)
   * @param data   Reserved for future use.  May be set but will be ignored.
   * @param groups a {@code Collection} of groups to which the user belongs
   * @since 2.6.10
   */
  public AuthenticationResponse(boolean valid, String data,
                                Collection<?> groups) {
    this.valid = valid;
    this.data = data;
    this.groups = groups;
  }

  /**
   * Tests whether authentication was valid.
   *
   * @return {@code true} if authentication was valid
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Get the client data.
   *
   * @return data - may be {@code null}
   */
  public String getData() {
    return data;
  }

  /**
   * Gets the groups to which the user belongs.
   * The returned {@code Collection} of groups is either:
   * <ul>
   * <li>{@code Collection<String>} for a simple collection of group names</li>
   * <li>{@code Collection<Principal>} for groups with namespaces</li>
   * </ul>
   *
   * @return {@code Collection} of group names - may be {@code null}
   * @since 2.6.10
   */
  public Collection<?> getGroups() {
    return groups;
  }

  @Override
  public String toString() {
    return "{ valid = " + valid + ", groups = " + groups
           + ", data = " + data + " }";
  }
}
