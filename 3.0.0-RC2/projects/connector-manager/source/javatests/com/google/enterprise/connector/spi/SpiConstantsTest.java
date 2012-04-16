// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.spi.SpiConstants.FeedType;

import junit.framework.TestCase;

import java.lang.reflect.Field;

public class SpiConstantsTest extends TestCase {
  /**
   * Tests that all of the SPI property name constants match
   * {@code RESERVED_PROPNAME_PREFIX}.
   */
  public void testReservedNamePattern() throws IllegalAccessException {
    for (Field field : SpiConstants.class.getFields()) {
      String name = field.getName();
      if (name.startsWith("PROPNAME")) {
        String value = (String) field.get(null);
        assertTrue(name + " = " + value,
            value.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX));
      }
    }
  }

  /**
   * Asserts that the given name is not a reserved name.
   */
  private void assertIsNotReserved(String value) {
    assertFalse(value,
        value.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX));
  }

  /** Tests variations on names that are not reserved. */
  public void testUnreservedNames() {
    assertIsNotReserved("google_name");
    assertIsNotReserved("mygoogle:name");
    assertIsNotReserved("other:name");
  }

  /** Represents a pair of feed types and their expected compatibility. */
  private static class FeedTypePair {
    public final FeedType thisType;
    public final FeedType otherType;
    public final boolean isCompatible;

    public FeedTypePair(FeedType thisType, FeedType otherType,
        boolean isCompatible) {
      this.thisType = thisType;
      this.otherType = otherType;
      this.isCompatible = isCompatible;
    }
  }

  /**
   * Expected outcomes for thisType.isCompatible(otherType). The
   * non-transitivity is the price for not allowing "ACL, CONTENT" to
   * share a feed file because it would require resetting the XmlFeed
   * type from ACL to CONTENT. References in these tests to "Should
   * be" are indicating the outcomes if isCompatible were transitive.
   */
  private final FeedTypePair[] feedTypePairs = {
    new FeedTypePair(FeedType.CONTENT, FeedType.CONTENT, true),
    new FeedTypePair(FeedType.CONTENT, FeedType.WEB, false),
    new FeedTypePair(FeedType.CONTENT, FeedType.CONTENTURL, false),
    new FeedTypePair(FeedType.WEB, FeedType.CONTENT, false),
    new FeedTypePair(FeedType.WEB, FeedType.WEB, true),
    new FeedTypePair(FeedType.WEB, FeedType.CONTENTURL, true),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.CONTENT, false),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.WEB, true),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.CONTENTURL, true),
  };

  /** Tests the completeness and ordering of the feedTypePairs test data. */
  public void testFeedTypesData() {
    int i = 0;
    for (FeedType first : FeedType.values()) {
      for (FeedType second : FeedType.values()) {
        assertEquals(first, feedTypePairs[i].thisType);
        assertEquals(second, feedTypePairs[i].otherType);
        i++;
      }
    }
    assertEquals(feedTypePairs.length, i);
  }

  /** Tests the compatibility of different feed types. */
  public void testFeedTypeIsCompatible() {
    for (FeedTypePair pair : feedTypePairs) {
      assertEquals(pair.thisType + ".isCompatible(" + pair.otherType + ")",
          pair.isCompatible, pair.thisType.isCompatible(pair.otherType));
    }
  }

  /** Tests the transitivity of feed type compatibility. */
  public void testFeedTypeTransitivity() {
    for (FeedType first : FeedType.values()) {
      for (FeedType second : FeedType.values()) {
        assertEquals(first + " <==> " + second,
            first.isCompatible(second),
            second.isCompatible(first));
      }
    }
  }

  /** Expected compatibility for each possible feed type pair. */
  private final boolean[] expectedCompatibilities = {
    true,  // CONTENT,    CONTENT
    false, // CONTENT,    WEB
    false, // CONTENT,    CONTENTURL
    false, // WEB,        CONTENT
    true,  // WEB,        WEB
    true,  // WEB,        CONTENTURL
    false, // CONTENTURL, CONTENT
    true,  // CONTENTURL, WEB
    true,  // CONTENTURL, CONTENTURL
  };

  /**
   * Tests how various feed type sequences are broken into feed files.
   * The FeedType.isCompatible and DocPusher.take feed file switching
   * on type are intertwined. We're arbitrarily testing the
   * combination here rather than in DocPusherTest.
   */
  public void testFeedTypeFiles() {
    int i = 0;
    for (FeedType first : FeedType.values()) {
      for (FeedType second : FeedType.values()) {
        assertEquals(i + ": < " + first + ", " + second + " >",
            expectedCompatibilities[i], second.isCompatible(first));
        i++;
      }
    }

    // Make sure the expectedFeedFiles test data is complete.
    assertEquals(expectedCompatibilities.length, i);
  }
}
