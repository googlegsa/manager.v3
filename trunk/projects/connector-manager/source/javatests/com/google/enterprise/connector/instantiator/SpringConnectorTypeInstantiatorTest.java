// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConnectorType;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Tests for SpringConnectorTypeInstantiator. These tests rely on setup: the
 * jars for TestConnector1 and TestConnector2 should be installed on the
 * classpath
 */
public class SpringConnectorTypeInstantiatorTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.instantiator.SpringConnectorTypeInstantiator
   * #getConnectorType(java.lang.String)}.
   */
  public final void testGetConnectorType() {
    ConnectorTypeInstantiator springConnectorTypeInstantiator =
        new SpringConnectorTypeInstantiator();
    verifyConnectorTypeFound(springConnectorTypeInstantiator, "TestConnector1");
    verifyConnectorTypeFound(springConnectorTypeInstantiator, "TestConnector2");
    verifyConnectorTypeNotFound(springConnectorTypeInstantiator, "NoConnector");
  }

  private void verifyConnectorTypeFound(
      ConnectorTypeInstantiator inst,
      String connectorTypeName) {
    ConnectorType connectorType = null;
    try {
      connectorType = inst.getConnectorType(connectorTypeName);
    } catch (ConnectorTypeNotFoundException e) {
      fail("Should find connector type " + connectorTypeName);
    }
    Assert.assertNotNull(connectorType);
    // also look for the prototype
    String prototypeString = null;
    try {
      prototypeString =
          inst.getConnectorInstancePrototype(connectorTypeName);
    } catch (ConnectorTypeNotFoundException e) {
      fail("Should find prototype string for type " + connectorTypeName);
    }
  }

  private void verifyConnectorTypeNotFound(
      ConnectorTypeInstantiator springConnectorTypeInstantiator,
      String connectorTypeName) {
    ConnectorType connectorType = null;
    try {
      connectorType =
          springConnectorTypeInstantiator.getConnectorType(connectorTypeName);
    } catch (ConnectorTypeNotFoundException e) {
      return;
    }
    fail("Should find connector type " + connectorTypeName);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.instantiator.SpringConnectorTypeInstantiator
   * #getConnectorTypeNames()}.
   */
  public final void testGetConnectorTypeNames() {
    String[] expectedConnectorTypeNames =
        new String[] {"TestConnector1", "TestConnector2"};
    Set expectedNamesSet =
        new HashSet(Arrays.asList(expectedConnectorTypeNames));
    ConnectorTypeInstantiator springConnectorTypeInstantiator =
        new SpringConnectorTypeInstantiator();
    Set actualNames = new HashSet();
    int counter = 0;
    for (Iterator i = springConnectorTypeInstantiator.getConnectorTypeNames(); i
        .hasNext(); counter++) {
      String connectorTypeName = (String) i.next();
      verifyConnectorTypeFound(springConnectorTypeInstantiator,
          connectorTypeName);
      actualNames.add(connectorTypeName);
    }
    Assert.assertTrue(actualNames.containsAll(expectedNamesSet));
    // we purposely don't assert the converse so that this test will continue to
    // work even if other connector types are installed
  }

}
