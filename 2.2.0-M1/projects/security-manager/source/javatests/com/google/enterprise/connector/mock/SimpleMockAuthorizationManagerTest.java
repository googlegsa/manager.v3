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

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import junit.framework.Assert;

import java.util.Collection;
import java.util.List;

public class SimpleMockAuthorizationManagerTest extends SecurityManagerTestCase {

  public void testAuthorizeDocids() throws RepositoryException {
    AuthorizationManager am = new SimpleMockAuthorizationManager();
    Assert.assertTrue(runOneTest(am, "http://foo.com/bar"));
    Assert.assertTrue(runOneTest(am, "http://bar.com/foo"));
    Assert.assertFalse(runOneTest(am, "http://bar.com/bar"));
  }

  private boolean runOneTest(AuthorizationManager am, String thisUrl) throws RepositoryException {
    boolean authOk = false;
    List<String> docids = ImmutableList.of(thisUrl);
    Collection<AuthorizationResponse> rs = null;
    rs = am.authorizeDocids(docids, null);
    if (rs != null) {
      for (AuthorizationResponse ar : rs) {
        if (ar.getDocid().equals(thisUrl)) {
          authOk = ar.isValid();
        }
      }
    }
    return authOk;
  }

}
