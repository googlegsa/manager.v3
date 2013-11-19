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

import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.PrincipalValue;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.util.ArrayList;
import java.util.Map;

public class AclpropertyFilterTest extends DocumentFilterTest {

    /** Creates a AclPropertyFilter. */
  protected Document createFilter(Document doc,
      CaseSensitivityType caseSensitivityType, String userDomain,
      boolean overwriteUserDomain) throws Exception {
    AclPropertyFilter factory = new AclPropertyFilter();
    if (caseSensitivityType != null) {
      factory.setCaseSensitivityType(caseSensitivityType.toString());
    }
    if (!Strings.isNullOrEmpty(userDomain)) {
      factory.setUserDomain(userDomain);
      factory.setOverwriteUserDomain(overwriteUserDomain);
    }
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
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE, null, false);
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
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE, null, false);
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
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE, null, false);
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

  public Document createDocument(String username, String denyUser) {
    Map<String, Object> props =
        ConnectorTestUtils.createSimpleDocumentBasicProperties("testDocId");
    if (!Strings.isNullOrEmpty(username)) {
      Principal principal =
          new Principal(SpiConstants.PrincipalType.UNKNOWN, null, username,
              SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
      props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    }

    if (!Strings.isNullOrEmpty(denyUser)) {
      Principal denyprincipal =
          new Principal(SpiConstants.PrincipalType.UNKNOWN, null, denyUser,
              SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
      props.put(SpiConstants.PROPNAME_ACLDENYUSERS, denyprincipal);
    }

    return ConnectorTestUtils.createSimpleDocument(props);
  }

  public Principal getPrincipal(Document output, String name)
      throws Exception {
    Property prop = output.findProperty(name);
    Value value = prop.nextValue();
    return ((PrincipalValue) value).getPrincipal();
  }

  public Principal createFilterAndGetPrinicpal(Document input,
      CaseSensitivityType caseSensitivity, String newUserDomain,
      boolean overwriteUserDomain, String name) throws Exception {
    Document output = createFilter(input, caseSensitivity, newUserDomain,
        overwriteUserDomain);
    return getPrincipal(output, name);
  }

  public void testAclFilterFindPropertyWithUserDomainValue() throws Exception {
    Document input = createDocument("John Doe", null);
    String newUserDomain = "MyCompany";
    Principal newPrincipal =
        createFilterAndGetPrinicpal(input, null, newUserDomain, false,
            SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyWithPrincipalValueAndUserDomainValue()
      throws Exception {
    Document input = createDocument("John Doe", null);
    String newUserDomain = "MyCompany";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE, newUserDomain,
        false, SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyWithEmptyUserDomainValue()
      throws Exception {
    Document input = createDocument("John Doe", null);
    String newUserDomain = "";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,  newUserDomain, false,
        SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertEquals("John Doe", newPrincipal.getName());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyWithEmptyDenyUserDomainValue()
      throws Exception {
    Document input = createDocument(null, "John Doe");
    String newUserDomain = "";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,  newUserDomain, false,
        SpiConstants.PROPNAME_ACLDENYUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertEquals("John Doe", newPrincipal.getName());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyOverwriteUserDomainValue()
      throws Exception {
    Document input = createDocument("noCompany\\John Doe", null);
    String newUserDomain = "MyCompany";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE, newUserDomain, true,
        SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyOverwriteDenyUserDomainValue()
      throws Exception {
    Document input = createDocument(null, "noCompany\\John Doe");
    String newUserDomain = "MyCompany";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE, newUserDomain, true,
        SpiConstants.PROPNAME_ACLDENYUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyOverwriteUserDomainValueFormat2()
      throws Exception {
    Document input = createDocument("John Doe@noCompany", null);
    String newUserDomain = "MyCompany";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE, newUserDomain, true,
        SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyOverwriteDenyUserDomainValueFormat2()
      throws Exception {
    Document input = createDocument(null, "John Doe@noCompany");
    String newUserDomain = "MyCompany";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE, newUserDomain, true,
        SpiConstants.PROPNAME_ACLDENYUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertTrue(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyNoOverwriteUserDomainValue()
      throws Exception {
    Document input = createDocument("noCompany\\John Doe", null);
    String newUserDomain = "MyCompany";
    Principal newPrincipal =
        createFilterAndGetPrinicpal(input, null, newUserDomain, false,
            SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertEquals("noCompany\\John Doe", newPrincipal.getName());
    assertFalse(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterFindPropertyNoOverwriteDenyUserDomainValue()
      throws Exception {
    Document input = createDocument(null, "noCompany\\John Doe");
    String newUserDomain = "MyCompany";
    Principal newPrincipal =
        createFilterAndGetPrinicpal(input, null, newUserDomain, false,
            SpiConstants.PROPNAME_ACLDENYUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertEquals("noCompany\\John Doe", newPrincipal.getName());
    assertFalse(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterUserDomainForPrincipalType() throws Exception {
    Map<String, Object> props = ConnectorTestUtils
        .createSimpleDocumentBasicProperties("testDocId");
    Principal principal = new Principal(
        SpiConstants.PrincipalType.UNQUALIFIED, null, "John Doe",
        SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    Document input = ConnectorTestUtils.createSimpleDocument(props);

    String newUserDomain = "MyCompany";
    Principal newPrincipal = createFilterAndGetPrinicpal(input,
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE, newUserDomain, false,
        SpiConstants.PROPNAME_ACLUSERS);

    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertEquals(principal.getName(), newPrincipal.getName());
    assertFalse(newPrincipal.getName().contains(newUserDomain));
  }

  public void testAclFilterWithNoFilterChanges() throws Exception {
    Document input = createDocument("Jane Doe", "John Doe");

    AclPropertyFilter factory = new AclPropertyFilter();
    Document output = factory.newDocumentFilter(input);

    Property prop = output.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    Value value = prop.nextValue();
    Principal newPrincipal = ((PrincipalValue) value).getPrincipal();
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        newPrincipal.getCaseSensitivityType());
    assertEquals("Jane Doe", newPrincipal.getName());

    Property denyprop =
        output.findProperty(SpiConstants.PROPNAME_ACLDENYUSERS);
    Value denyvalue = denyprop.nextValue();
    Principal denyPrincipal = ((PrincipalValue) denyvalue).getPrincipal();
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        denyPrincipal.getCaseSensitivityType());
    assertEquals("John Doe", denyPrincipal.getName());
  }

  public void assertValues(Principal principal, String userName, String domain,
      boolean changeDomain) {
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
        principal.getCaseSensitivityType());
    assertEquals(userName, principal.getName());
    if (changeDomain) {
      assertTrue(principal.getName().contains(domain));
    } else {
      assertFalse(principal.getName().contains(domain));
    }
  }

  public void addPrincipalToDocument(Map<String, Object> props, String key,
      String name) {
    Principal principal =
        new Principal(SpiConstants.PrincipalType.UNKNOWN, null, name,
            SpiConstants.CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    props.put(key, principal);
  }

  public void testAclFilterForGroupDomainChange() throws Exception {
    Map<String, Object> props =
        ConnectorTestUtils.createSimpleDocumentBasicProperties("testDocId");

    addPrincipalToDocument(props, SpiConstants.PROPNAME_ACLUSERS,
        "noCompany\\John Doe");
    addPrincipalToDocument(props, SpiConstants.PROPNAME_ACLDENYUSERS,
        "anyCompany\\Jane Doe");
    addPrincipalToDocument(props, SpiConstants.PROPNAME_ACLGROUPS,
        "noCompany\\Sales");
    addPrincipalToDocument(props, SpiConstants.PROPNAME_ACLDENYGROUPS,
        "anyCompany\\Testing");

    Document input = ConnectorTestUtils.createSimpleDocument(props);

    String newUserDomain = "MyCompany";
    Document output = createFilter(input,
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE, newUserDomain, true);

    Principal newPrincipal = getPrincipal(output,
        SpiConstants.PROPNAME_ACLUSERS);
    assertValues(newPrincipal, "MyCompany\\John Doe", newUserDomain, true);

    Principal newDenyPrincipal = getPrincipal(output,
        SpiConstants.PROPNAME_ACLDENYUSERS);
    assertValues(newDenyPrincipal, "MyCompany\\Jane Doe", newUserDomain, true);

    Principal newGroupPrincipal = getPrincipal(output,
        SpiConstants.PROPNAME_ACLGROUPS);
    assertValues(newGroupPrincipal, "noCompany\\Sales", newUserDomain, false);

    Principal groupDenyPrincipal = getPrincipal(output,
        SpiConstants.PROPNAME_ACLDENYGROUPS);
    assertValues(groupDenyPrincipal, "anyCompany\\Testing",
        newUserDomain, false);
  }
}
