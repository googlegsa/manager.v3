// Copyright 2007-2008 Google Inc.
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

import junit.framework.TestCase;

public class AuthorizationResponseTest extends TestCase {

  public void testHashCodeAndEquals() {
    AuthorizationResponse ar1 = new AuthorizationResponse(true, "docid1");
    AuthorizationResponse ar2 = new AuthorizationResponse(true, "docid1");
    AuthorizationResponse ar3 = new AuthorizationResponse(false, "docid1");
    AuthorizationResponse ar4 = new AuthorizationResponse(true, "docid2");
    assertTrue(ar1.equals(ar1));
    assertTrue(ar1.equals(ar2));
    assertTrue(ar2.equals(ar1));
    assertFalse(ar1.equals(ar3));
    assertFalse(ar1.equals(ar4));
    assertTrue(ar1.hashCode() == ar2.hashCode());
    assertFalse(ar1.hashCode() == ar3.hashCode());
    assertFalse(ar1.hashCode() == ar4.hashCode());
    assertFalse(ar1.equals("docid1"));
    assertFalse(ar1.hashCode() == "docid1".hashCode());
  }
}
