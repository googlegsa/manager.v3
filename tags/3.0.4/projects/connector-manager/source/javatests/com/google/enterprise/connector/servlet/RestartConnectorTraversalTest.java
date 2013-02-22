// Copyright 2011 Google Inc.  All Rights Reserved.
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
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * Tests RestartConnectorTraversal servlet class.
 */
public class RestartConnectorTraversalTest extends TestCase {

  /** Test method for {@link RestartConnectorTraversal#handleDoGet}. */
  public void testHandleDoGet() {
    checkHandleDoGet("connector1",
        ConnectorMessageCode.SUCCESS_RESTART_TRAVERSAL, true);
  }

  /** Test ConnectorNotFoundException. */
  public void testConnectorNotFoundException() {
    checkHandleDoGet("UnknownConnector",
        ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, false);
  }

  /** Test InstantiatorException. */
  public void testInstantiatorException() {
    checkHandleDoGet("BrokenConnector",
        ConnectorMessageCode.EXCEPTION_INSTANTIATOR, false);
  }

  private void checkHandleDoGet(String connectorName, int expectedCode,
                                boolean shouldRestart) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    RestartTraversalManager manager = new RestartTraversalManager();
    RestartConnectorTraversal.handleDoGet(connectorName, manager, out);
    out.flush();
    String result = StringUtils.normalizeNewlines(writer.toString());
    out.close();
    String expectedResult = expectedResult(connectorName, expectedCode);
    System.out.println("\n=============================\n");
    System.out.println("Test: " + getName());
    System.out.println("Expected Response:\n" + expectedResult);
    System.out.println("Actual Response:\n" + result);
    assertEquals(expectedResult, result);
    assertEquals(shouldRestart, manager.didRestartTraversal(connectorName));
  }

  /** Build the expected result string. */
  private String expectedResult(String connectorName, int code) {
    String expected = "<CmResponse>\n  <StatusId>" + code + "</StatusId>\n"
        + "  <CMParams Order=\"0\" CMParam=\"" + connectorName + "\"/>\n"
        + "</CmResponse>\n";
    return StringUtils.normalizeNewlines(expected);
  }

  private static class RestartTraversalManager extends MockManager {
    private HashMap<String, Boolean> restarts = new HashMap<String, Boolean>();

    @Override
    public void restartConnectorTraversal(String connectorName)
        throws ConnectorNotFoundException, InstantiatorException {
      if ("UnknownConnector".equals(connectorName)) {
        throw new ConnectorNotFoundException(connectorName);
      } else if ("BrokenConnector".equals(connectorName)) {
        throw new InstantiatorException(connectorName);
      }
      restarts.put(connectorName, Boolean.TRUE);
    }

    boolean didRestartTraversal(String connectorName) {
      return Boolean.TRUE.equals(restarts.get(connectorName));
    }
  }
}
