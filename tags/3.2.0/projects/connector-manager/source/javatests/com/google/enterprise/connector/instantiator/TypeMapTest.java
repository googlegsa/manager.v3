// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.io.File;
import java.util.Set;

/**
 * Test for {@link TypeMap}
 */
public class TypeMapTest extends TestCase {

  private static final String TEST_DIR_NAME = "testdata/tmp/TypeMapTests";
  private final File baseDirectory = new File(TEST_DIR_NAME);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Make sure that the test directory does not exist.
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    // Then recreate it empty.
    assertTrue(ConnectorTestUtils.mkdirs(baseDirectory));
  }

  @Override
  protected void tearDown() throws Exception {
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    super.tearDown();
  }

  /**
   * Test method for
   * {@link TypeMap#getTypesDirectory()};
   */
  public final void testTypesDirectory() {
    TypeMap typeMap = new TypeMap(TEST_DIR_NAME);
    typeMap.init();
    assertEquals(new File(baseDirectory, "connectors"),
                 typeMap.getTypesDirectory());
  }

  /**
   * Test method for
   * {@link TypeMap#getConnectorTypeNames()};
   */
  public final void testGetTypeNames() {
    TypeMap typeMap = new TypeMap(TEST_DIR_NAME);
    typeMap.init();
    Set<String> typeNames = typeMap.getConnectorTypeNames();
    assertTrue(typeNames.contains("TestConnectorA"));
    assertTrue(typeNames.contains("TestConnectorB"));
  }

  /**
   * Test method for
   * {@link TypeMap#getTypeInfo(java.lang.String)}.
   */
  public final void testGetTypeInfo() throws ConnectorTypeNotFoundException {
    TypeMap typeMap = new TypeMap(TEST_DIR_NAME);
    typeMap.init();
    verifyType(typeMap, "TestConnectorA");
    verifyType(typeMap, "TestConnectorB");
  }

  private void verifyType(TypeMap typeMap, String typeName)
      throws ConnectorTypeNotFoundException {
    TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
    assertEquals(typeName, typeInfo.getConnectorTypeName());
  }
}
