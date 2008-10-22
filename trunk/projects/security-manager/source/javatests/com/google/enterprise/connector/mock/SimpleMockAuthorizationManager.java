// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.mock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleMockAuthorizationManager implements AuthorizationManager {

  @Override
  @SuppressWarnings("unchecked")
  public Collection authorizeDocids(Collection docids, AuthenticationIdentity identity) {
    return typedAuthorizeDocids(docids, identity);
  }

  private Collection<AuthorizationResponse> typedAuthorizeDocids(Collection<String> docids,
      AuthenticationIdentity identity) {
    Preconditions.checkContentsNotNull(docids);
    List<AuthorizationResponse> result = new ArrayList<AuthorizationResponse>();
    for (String s : docids) {
      AuthorizationResponse a;
      if (s.contains("foo")) {
        a = new AuthorizationResponse(true, s);
      } else {
        a = new AuthorizationResponse(false, s);
      }
      result.add(a);
    }
    return ImmutableList.copyOf(result);
  }

}
