// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

/**
 * Tests {@link GetDocumentContent} servlet class.
 */
public class GetDocumentContentTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetDocumentContentTest.class.getName());

  /** Test basic servlet function against a MockManager. */
  public void testGetDocumentContentMockManager() throws Exception {
    Manager manager = MockManager.getInstance();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int status;

    // Connector1 serves up document content equal to docid.
    status = GetDocumentContent.handleDoGet(manager, "connector1",
                                            "xyzzy", buffer);
    assertEquals(200, status);
    assertEquals("xyzzy", buffer.toString());

    // Connector2 serves up no content at all.
    buffer.reset();
    status = GetDocumentContent.handleDoGet(manager, "connector2",
                                            "xyzzy", buffer);
    assertEquals(204, status);
    assertEquals(0, buffer.size());

    // UnknownConnector does not exist.
    buffer.reset();
    status = GetDocumentContent.handleDoGet(manager, "unknownConnector",
                                            "xyzzy", buffer);
    assertEquals(404, status);
    assertEquals(0, buffer.size());
  }

  /** Test real Manager. */
  public void testGetDocumentContentRealManager() throws Exception {
    // Create a stand alone context with real ProductionManager.
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
                                 Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
    context.start();
    Manager manager = context.getManager();

    System.out.println("Connectors: " + manager.getConnectorStatuses());

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int status;

    // UnknownConnector does not exist.
    status = GetDocumentContent.handleDoGet(manager, "unknownConnector",
                                            "xyzzy", buffer);
    assertEquals(404, status);
    assertEquals(0, buffer.size());

    // TODO: test a RetrieverAware Test connector.
  }
}
