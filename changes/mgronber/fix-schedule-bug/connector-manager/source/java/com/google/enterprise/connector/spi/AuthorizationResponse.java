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
 * The carrier type of the list returned by the
 * {@link AuthorizationManager}.authorizeDocids method.
 */
public class AuthorizationResponse {

  private final boolean valid;
  private final String docid;

  /**
   * Makes an AuthorizationResponse.
   * 
   * @param valid Indicates that authorization was successful (valid)
   * @param docid The docid for which authorization succeeded - should not
   *        be null or empty
   */
  public AuthorizationResponse(boolean valid, String docid) {
    this.valid = valid;
    this.docid = docid;
  }

  /**
   * Tests whether authorization was valid
   * 
   * @return true if authorization was valid
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Gets the docid.
   * 
   * @return docid - should not be null or empty
   */
  public String getDocid() {
    return docid;
  }
}
