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

package com.google.enterprise.connector.spiimpl;

import com.google.enterprise.connector.spi.Principal;

public class PrincipalValue extends ValueImpl {

  private Principal principal;

  /**
   * Builds a PrincipalValue instance from a Principal.
   *
   * @param type the principal
   */
  public PrincipalValue(Principal principal) {
    super();
    this.principal = principal;
  }

  public Principal getPrincipal() {
    return principal;
  }

  /**
   * Returns just the name, to be compatible with plain StringValue
   * principals.
   */
  @Override
  public String toFeedXml() {
    return principal.getName();
  }

  @Override
  public String toString() {
    return principal.toString();
  }

  @Override
  public boolean toBoolean() {
    return true;
  }
}
