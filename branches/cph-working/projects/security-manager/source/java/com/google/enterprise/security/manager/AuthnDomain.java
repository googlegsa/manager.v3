// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.security.manager;

import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

public class AuthnDomain extends SessionObject {

  private static final String NAME_KEY = "name";
  private static final String MECHANISM_KEY = "mechanism";

  public AuthnDomain(SessionInterface session, String name, AuthNMechanism mechanism) {
    super(session);
    setString(NAME_KEY, name);
    setEnum(MECHANISM_KEY, mechanism);
  }

  public String getName() {
    return getString(NAME_KEY);
  }

  public AuthNMechanism getMechanism() {
    return getEnum(MECHANISM_KEY, AuthNMechanism.class);
  }
}
