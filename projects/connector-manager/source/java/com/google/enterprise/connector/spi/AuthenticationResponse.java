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

/**
 * The response for the {@link AuthenticationManager}.authenticate method.
 */
public class AuthenticationResponse {

  private final boolean valid;
  private final String data;

  /**
   * Makes an AuthenticationResponse.
   * @param valid   Indicates that authentication was successful (valid)
   * @param data   Reserved for future use.  May be set but will be ignored.
   */
  public AuthenticationResponse(boolean valid, String data) {
    this.valid = valid;
    this.data = data;
  }

  /**
   * Tests whether authentication was valid
   * @return true if authentication was valid
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Get the client data
   * @return data - may be null
   */
  public String getData() {
    return data;
  }
}
