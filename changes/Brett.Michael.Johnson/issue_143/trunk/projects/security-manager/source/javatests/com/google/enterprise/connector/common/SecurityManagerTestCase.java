// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.common;

import com.google.enterprise.connector.manager.Context;

import junit.framework.TestCase;

/**
 * Make sure unit tests initialize application context right.
 */
public class SecurityManagerTestCase extends TestCase {

  public static final String GSA_TESTING_ISSUER = "http://google.com/enterprise/gsa/testing";

  public SecurityManagerTestCase() {
    super();
  }

  public SecurityManagerTestCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(
        Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
    context.start();
  }

  @Override
  protected void tearDown() throws Exception {
    Context.getInstance().shutdown(false);
    Context.refresh();
    super.tearDown();
  }
}
