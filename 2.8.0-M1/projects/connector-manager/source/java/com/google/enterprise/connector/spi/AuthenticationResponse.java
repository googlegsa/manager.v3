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
 * The response for the {@link AuthenticationManager}.authenticate method.
 */
public class AuthenticationResponse {

  private final boolean valid;
  private final String data;
  private final Collection<String> groups;

  /**
   * Makes an AuthenticationResponse.
   *
   * @param valid  Indicates that authentication was successful (valid).
   * @param data   Reserved for future use.  May be set but will be ignored.
   */
  public AuthenticationResponse(boolean valid, String data) {
    this(valid, data, null);
  }

  /**
   * Makes an AuthenticationResponse.
   *
   * @param valid  Indicates that authentication was successful (valid).
   * @param data   Reserved for future use.  May be set but will be ignored.
   * @param groups A Collection of groups to which the user belongs.
   * @since 2.6.10
   */
  public AuthenticationResponse(boolean valid, String data,
                                Collection<String> groups) {
    this.valid = valid;
    this.data = data;
    this.groups = groups;
  }

  /**
   * Tests whether authentication was valid.
   *
   * @return true if authentication was valid
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Get the client data.
   *
   * @return data - may be null
   */
  public String getData() {
    return data;
  }

  /**
   * Gets the groups to which the user belongs.
   *
   * @return Collection of group names - may be null
   * @since 2.6.10
   */
  public Collection<String> getGroups() {
    return groups;
  }
}
