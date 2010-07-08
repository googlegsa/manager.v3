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

import java.io.File;

import com.google.enterprise.connector.instantiator.TypeInfo;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 */
public class TypeMapTest extends TestCase {

  private static final String TEST_DIR_NAME = "testdata/tempTypeMapTests";
  private File baseDirectory;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Make sure that the test directory does not exist
    baseDirectory = new File(TEST_DIR_NAME);
    Assert.assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    // Then recreate it empty
    Assert.assertTrue(baseDirectory.mkdirs());
  }

  @Override
  protected void tearDown() throws Exception {
    Assert.assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    super.tearDown();
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.instantiator.TypeMap
   * #getTypeInfo(java.lang.String)}.
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
    Assert.assertEquals(typeName, typeInfo.getConnectorTypeName());
  }

}
