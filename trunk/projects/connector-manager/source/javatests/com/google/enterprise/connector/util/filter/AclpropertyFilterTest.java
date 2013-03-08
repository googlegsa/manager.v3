// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util.filter;

import java.util.ArrayList;
import java.util.Map;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.PrincipalValue;
import com.google.enterprise.connector.test.ConnectorTestUtils;

public class AclpropertyFilterTest extends DocumentFilterTest {

    /** Creates a AclPropertyFilter. */
  protected Document createFilter(Document doc,
      CaseSensitivityType caseSensitivityType) throws Exception {
    AclPropertyFilter factory = new AclPropertyFilter();
    factory.setCaseSensitivityType(caseSensitivityType.toString());
    return factory.newDocumentFilter(doc);
  }

  public void testAclFilterFindPropertyWithPrincipalValue() throws Exception {
    Map<String, Object> props = ConnectorTestUtils
        .createSimpleDocumentBasicProperties("testDocId");
    Principal principal = new Principal(
        SpiConstants.PrincipalType.UNQUALIFIED,null, "John Doe",
        SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    Document input = ConnectorTestUtils.createSimpleDocument(props);

    Document output = createFilter(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    Property prop = output.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    Value value = prop.nextValue();

    Principal newPrincipal = ((PrincipalValue) value).getPrincipal();
    assertEquals(principal.getName(), newPrincipal.getName());
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
  }

  public void testAclFilterFindPropertyWithSamePrincipalValue()
      throws Exception {
    Map<String, Object> props = ConnectorTestUtils
        .createSimpleDocumentBasicProperties("testDocId");
    Principal principal = new Principal(
        SpiConstants.PrincipalType.UNQUALIFIED, null, "John Doe",
        SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    Document input = ConnectorTestUtils.createSimpleDocument(props);

    Document output = createFilter(input,
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    Property prop = output.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    Value value = prop.nextValue();

    Principal newPrincipal = ((PrincipalValue) value).getPrincipal();
    assertEquals(principal.getName(), newPrincipal.getName());
    assertTrue(newPrincipal.getCaseSensitivityType().equals(
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE));
  }

  public void testAclFilterFindPropertyWithNoPrincipalValue() 
      throws Exception {
    Map<String, Object> props = ConnectorTestUtils
        .createSimpleDocumentBasicProperties("testDocId");
    Principal principal = new Principal(
        SpiConstants.PrincipalType.UNQUALIFIED, null, "John Doe",
        SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    Document input = ConnectorTestUtils.createSimpleDocument(props);

    AclPropertyFilter factory = new AclPropertyFilter();
    Document output = factory.newDocumentFilter(input);

    Property prop = output.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    Value value = prop.nextValue();

    Principal newPrincipal = ((PrincipalValue) value).getPrincipal();
    assertEquals(principal.getName(), newPrincipal.getName());
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());
  }

  public void testAclFilterFindPropertyWithAclInheritFrom() 
      throws Exception {
    Map<String, Object> props = ConnectorTestUtils
        .createSimpleDocumentBasicProperties("testDocId");
    Principal principal = new Principal(
        SpiConstants.PrincipalType.UNQUALIFIED, null, "John Doe",
        SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, "parentId");
    Document input = ConnectorTestUtils.createSimpleDocument(props);

    AclPropertyFilter factory = new AclPropertyFilter();
    Document output = factory.newDocumentFilter(input);

    Property prop = output.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    Value value = prop.nextValue();

    Principal newPrincipal = ((PrincipalValue) value).getPrincipal();
    assertEquals(principal.getName(), newPrincipal.getName());
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());

    Property propInheritFrom = 
        output.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM);
    Value valueInheritfrom = propInheritFrom.nextValue();
    assertEquals("parentId", valueInheritfrom.toString());
  }

  public void testAclFilterFindPropertyWithStringValues() throws Exception {
    Map<String, Object> props = ConnectorTestUtils
        .createSimpleDocumentBasicProperties("testDocId");
    ArrayList<String> userList = new ArrayList<String>();
    userList.add("John Doe");
    userList.add("GI Joe");
    props.put(SpiConstants.PROPNAME_ACLUSERS, userList);

    Document input = ConnectorTestUtils.createSimpleDocument(props);
    Document output = createFilter(input,
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    Property prop = output.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    Value value;
    while ((value = prop.nextValue()) != null) {
      if (value instanceof PrincipalValue) {
        Principal newPrincipal = ((PrincipalValue) value).getPrincipal();
        assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
            newPrincipal.getCaseSensitivityType());
      }
    }
  }
}
