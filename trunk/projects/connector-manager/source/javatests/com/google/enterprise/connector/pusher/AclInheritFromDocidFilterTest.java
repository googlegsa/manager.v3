// Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.pusher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AclInheritFromDocidFilterTest extends TestCase {
  private final DocumentFilterFactory objectUnderTest =
      new AclInheritFromDocidFilter(
          new UrlConstructor("testconnector", FeedType.CONTENT));

  /**
   * Filters a document with the given properties and expects only the
   * docid and aclinheritfrom properties in the output, and expects
   * the given PROPNAME_ACLINHERITFROM value.
   */
  private void testFilter(Map<String, String> properties,
      String expectedInheritFrom) throws RepositoryException {
    Document input = ConnectorTestUtils.createSimpleDocument(properties);
    Document output = objectUnderTest.newDocumentFilter(input);
    assertEquals(ImmutableSet.of(
            SpiConstants.PROPNAME_DOCID,
            SpiConstants.PROPNAME_ACLINHERITFROM),
        output.getPropertyNames());
    assertEquals(expectedInheritFrom, Value.getSingleValueString(output,
            SpiConstants.PROPNAME_ACLINHERITFROM));
  }

  /** Only docid is set => null inheritfrom. */
  public void testOnlyDocid() throws RepositoryException {
    testFilter(
        ImmutableMap.of(SpiConstants.PROPNAME_DOCID, "42"),
        null);
  }

  /** Only inheritfrom is set => original inheritfrom, no others. */
  public void testOnlyInheritFrom() throws RepositoryException {
    testFilter(
        ImmutableMap.of(
            SpiConstants.PROPNAME_DOCID, "42",
            SpiConstants.PROPNAME_ACLINHERITFROM, "tycoon"),
        "tycoon");
  }

  /** All properties are set => original inheritfrom, no others. */
  public void testAllProperties() throws RepositoryException {
    testFilter(
        ImmutableMap.of(
            SpiConstants.PROPNAME_DOCID, "42",
            SpiConstants.PROPNAME_ACLINHERITFROM, "tycoon",
            SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, "1729",
            SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "taxicab"),
        "tycoon");
  }

  /** No inheritfrom is set, others are set => new inheritfrom, no others. */
  public void testNoInheritFrom() throws RepositoryException {
    testFilter(
        ImmutableMap.of(
            SpiConstants.PROPNAME_DOCID, "42",
            SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, "1729",
            SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "taxicab"),
        "googleconnector://testconnector.localhost/doc?docid=1729&taxicab");
  }

  /**
   * Empty inheritfrom is set, others are set => original inheritfrom,
   * no others.
   */
  public void testEmptyInheritFrom() throws RepositoryException {
    testFilter(
        ImmutableMap.of(
            SpiConstants.PROPNAME_DOCID, "42",
            SpiConstants.PROPNAME_ACLINHERITFROM, "",
            SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, "1729",
            SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "taxicab"),
        "");
  }
}
