// Copyright (C) 2008-2009 Google Inc.
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

import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleDocumentTest extends TestCase {
  private SimpleDocument document;

  @Override
  protected void setUp() throws Exception {
    Map<String, Object> props = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(10 * 1000);

    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_ACTION,
        SpiConstants.ActionType.DELETE.toString());
    props.put(SpiConstants.PROPNAME_DOCID, "doc1");
    props.put(SpiConstants.PROPNAME_CONTENT, "now is the time");
    props.put(SpiConstants.PROPNAME_DISPLAYURL,
        "http://www.comtesturl.com/test");

    document = ConnectorTestUtils.createSimpleDocument(props);
  }

  @Override
  protected void tearDown() throws Exception {
    document = null;
  }

  public void testFindProperty() throws RepositoryException {
    Set<String> propNames = document.getPropertyNames();
    for (String propName : propNames) {
      Property prop = document.findProperty(propName);
      assertNotNull("Checking for " + propName + " property", prop.nextValue());
    }

    // Now try it again since properties should be able to be reused.
    for (String propName : propNames) {
      Property prop = document.findProperty(propName);
      assertNotNull("Checking for " + propName + " property", prop.nextValue());
    }
  }

  public void testGetPropertyNames() {
    Set<String> propNames = document.getPropertyNames();
    assertTrue(propNames.contains(SpiConstants.PROPNAME_LASTMODIFIED));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_ACTION));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_DOCID));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_CONTENT));
    assertTrue(propNames.contains(SpiConstants.PROPNAME_DISPLAYURL));
  }
}
