// Copyright (c) 2009 Google Inc.
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

import com.google.enterprise.connector.persist.FileStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import org.w3c.dom.Element;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Test for {@link PropertiesUtils}.
 */
public class PropertiesUtilsTest extends TestCase {
  private static final String keyOne = "one";
  private static final String keyTwo = "two";
  private static final String keyThree = "three";
  private static final String keyFour = "four";

  private static final String valueOne = "value one";
  // XML characters.
  private static final String valueTwo = "<\"\'&>";
  // Properties related escape characters.
  private static final String valueThree = "\\ \t \f \n \r # ! = :";
  // Our favorite corner case.
  private static final String valueFour = "one\ntwo\nthree\four";

  protected FileStore store;
  protected File storeDir;

  protected void setUp() throws Exception {
    storeDir = new File("testdata/tmp/filestore");
    assertTrue(storeDir.mkdirs());
    store = new FileStore();
  }

  protected void tearDown() throws Exception {
    //assertTrue(ConnectorTestUtils.deleteAllFiles(storeDir));
  }

  private static final String TEST_CONFIG = "<foo>"
      + "<Param name=\"Color\" value=\"color1&#xA;color2&#xA;color3\"/>"
      + "</foo>";

  public void testNormalFlow() {
      Element root = ServletUtil.parseAndGetRootElement(TEST_CONFIG, "foo");
      Map configData = ServletUtil.getAllAttributes(root,
          ServletUtil.XMLTAG_PARAMETERS);
      Map copyConfigData = new HashMap();
      copyConfigData.putAll(configData);

      // InstanceInfo.fromNewConfig(...)
      Properties props = PropertiesUtils.fromMap(copyConfigData);
      // InstanceInfo.setConnectorConfig(Map configMap)
      Properties props2 = PropertiesUtils.fromMap(copyConfigData);
      StoreContext storeContext = new StoreContext("fooConnector", storeDir);
      store.storeConnectorConfiguration(storeContext, props2);
  }

  public void testLoadFromFile() {
    fail("Not yet implemented");
  }

  public void testStoreToFile() {
    fail("Not yet implemented");
  }

  public void testStoreToString() {
    fail("Not yet implemented");
  }

  public void testLoadFromString() {
    fail("Not yet implemented");
  }

  public void testLoadProperties() {
    fail("Not yet implemented");
  }

  public void testStoreProperties() {
    fail("Not yet implemented");
  }

  public void testFromMap() {
    fail("Not yet implemented");
  }

  public void testCopy() {
    fail("Not yet implemented");
  }

  public void testEncryptSensitiveProperties() {
    fail("Not yet implemented");
  }

  public void testDecryptSensitiveProperties() {
    fail("Not yet implemented");
  }

  public void testStampPropertiesVersion() {
    fail("Not yet implemented");
  }

  public void testGetPropertiesVersion() {
    fail("Not yet implemented");
  }
}
