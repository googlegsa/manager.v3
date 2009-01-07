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

package com.google.enterprise.connector.manager;

import junit.framework.TestCase;

import java.util.Set;

/**
 * Tests for the {@link ConnectorManager} class.
 */
public class ConnectorManagerTest extends TestCase {

  /**
   * We do Context.refresh() before and after so as not to interfere with other
   * tests that might use a Context
   */
  @SuppressWarnings("unchecked")
  public final void testBasicFunctionality() {
    Context.refresh();
    Context.getInstance().setStandaloneContext(
        Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    Context.getInstance().setFeeding(false);
    Context.getInstance().start();

    ConnectorManager manager = (ConnectorManager) Context.getInstance().getManager();

    Set<String> connectorTypeNames = manager.getConnectorTypeNames();
    for (String connectorTypeName: connectorTypeNames) {
      System.out.println(connectorTypeName);
    }
    Context.getInstance().shutdown(true);
    Context.refresh();
  }


}
