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
    new FeedTypePair(FeedType.CONTENT, FeedType.ACL, false), // Should be true
    new FeedTypePair(FeedType.WEB, FeedType.CONTENT, false),
    new FeedTypePair(FeedType.WEB, FeedType.WEB, true),
    new FeedTypePair(FeedType.WEB, FeedType.CONTENTURL, true),
    new FeedTypePair(FeedType.WEB, FeedType.ACL, true),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.CONTENT, false),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.WEB, true),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.CONTENTURL, true),
    new FeedTypePair(FeedType.CONTENTURL, FeedType.ACL, true),
    new FeedTypePair(FeedType.ACL, FeedType.CONTENT, true),
    new FeedTypePair(FeedType.ACL, FeedType.WEB, true),
    new FeedTypePair(FeedType.ACL, FeedType.CONTENTURL, true),
    new FeedTypePair(FeedType.ACL, FeedType.ACL, true),
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

  /**
   * Tests the non-transitivity of feed type compatibility. The value
   * for CONTENT.isCompatible(ACL) is the only "incorrect" value.
   */
  public void testFeedTypeIsNotTransitive() {
    for (FeedType first : FeedType.values()) {
      for (FeedType second : FeedType.values()) {
        if ((first == FeedType.CONTENT && second == FeedType.ACL)
            || (first == FeedType.ACL && second == FeedType.CONTENT)) {
          assertNotSame(first + " <==> " + second,
              first.isCompatible(second),
              second.isCompatible(first));
        } else {
          assertEquals(first + " <==> " + second,
              first.isCompatible(second),
              second.isCompatible(first));
        }
      }
    }
  }

 /**
   * Three sequential feed types can be broken into feed files four ways:
   * <dl>
   * <dt> TOGETHER
   * <dd> All together in one feed file
   * <dt> FIRST
   * <dd> First type in a separate feed file from the others
   * <dt> LAST
   * <dd> Last type in a separate feed file from the others
   * <dt> SEPARATE
   * <dd> Each type in a separate feed file
   * </dl>
   */
  private enum FeedFile { TOGETHER, FIRST, LAST, SEPARATE };

  /** Expected feed file splits for each possible feed type triplet. */
  private final FeedFile[] expectedFeedFiles = {
    FeedFile.TOGETHER, // CONTENT,    CONTENT,    CONTENT
    FeedFile.LAST,     // CONTENT,    CONTENT,    WEB
    FeedFile.LAST,     // CONTENT,    CONTENT,    CONTENTURL
    FeedFile.TOGETHER, // CONTENT,    CONTENT,    ACL
    FeedFile.SEPARATE, // CONTENT,    WEB,        CONTENT
    FeedFile.FIRST,    // CONTENT,    WEB,        WEB
    FeedFile.FIRST,    // CONTENT,    WEB,        CONTENTURL
    FeedFile.FIRST,    // CONTENT,    WEB,        ACL
    FeedFile.SEPARATE, // CONTENT,    CONTENTURL, CONTENT
    FeedFile.FIRST,    // CONTENT,    CONTENTURL, WEB
    FeedFile.FIRST,    // CONTENT,    CONTENTURL, CONTENTURL
    FeedFile.FIRST,    // CONTENT,    CONTENTURL, ACL
    FeedFile.TOGETHER, // CONTENT,    ACL,        CONTENT
    FeedFile.LAST,     // CONTENT,    ACL,        WEB
    FeedFile.LAST,     // CONTENT,    ACL,        CONTENTURL
    FeedFile.TOGETHER, // CONTENT,    ACL,        ACL

    FeedFile.FIRST,    // WEB,        CONTENT,    CONTENT
    FeedFile.SEPARATE, // WEB,        CONTENT,    WEB
    FeedFile.SEPARATE, // WEB,        CONTENT,    CONTENTURL
    FeedFile.FIRST,    // WEB,        CONTENT,    ACL
    FeedFile.LAST,     // WEB,        WEB,        CONTENT
    FeedFile.TOGETHER, // WEB,        WEB,        WEB
    FeedFile.TOGETHER, // WEB,        WEB,        CONTENTURL
    FeedFile.TOGETHER, // WEB,        WEB,        ACL
    FeedFile.LAST,     // WEB,        CONTENTURL, CONTENT
    FeedFile.TOGETHER, // WEB,        CONTENTURL, WEB
    FeedFile.TOGETHER, // WEB,        CONTENTURL, CONTENTURL
    FeedFile.TOGETHER, // WEB,        CONTENTURL, ACL
    FeedFile.LAST,     // WEB,        ACL,        CONTENT
    FeedFile.TOGETHER, // WEB,        ACL,        WEB
    FeedFile.TOGETHER, // WEB,        ACL,        CONTENTURL
    FeedFile.TOGETHER, // WEB,        ACL,        ACL

    FeedFile.FIRST,    // CONTENTURL, CONTENT,    CONTENT
    FeedFile.SEPARATE, // CONTENTURL, CONTENT,    WEB
    FeedFile.SEPARATE, // CONTENTURL, CONTENT,    CONTENTURL
    FeedFile.FIRST,    // CONTENTURL, CONTENT,    ACL
    FeedFile.LAST,     // CONTENTURL, WEB,        CONTENT
    FeedFile.TOGETHER, // CONTENTURL, WEB,        WEB
    FeedFile.TOGETHER, // CONTENTURL, WEB,        CONTENTURL
    FeedFile.TOGETHER, // CONTENTURL, WEB,        ACL
    FeedFile.LAST,     // CONTENTURL, CONTENTURL, CONTENT
    FeedFile.TOGETHER, // CONTENTURL, CONTENTURL, WEB
    FeedFile.TOGETHER, // CONTENTURL, CONTENTURL, CONTENTURL
    FeedFile.TOGETHER, // CONTENTURL, CONTENTURL, ACL
    FeedFile.LAST,     // CONTENTURL, ACL,        CONTENT
    FeedFile.TOGETHER, // CONTENTURL, ACL,        WEB
    FeedFile.TOGETHER, // CONTENTURL, ACL,        CONTENTURL
    FeedFile.TOGETHER, // CONTENTURL, ACL,        ACL

    FeedFile.FIRST,    // ACL,        CONTENT,    CONTENT    Should be TOGETHER
    FeedFile.SEPARATE, // ACL,        CONTENT,    WEB        Should be LAST
    FeedFile.SEPARATE, // ACL,        CONTENT,    CONTENTURL Should be LAST
    FeedFile.FIRST,    // ACL,        CONTENT,    ACL        Should be TOGETHER
    FeedFile.LAST,     // ACL,        WEB,        CONTENT
    FeedFile.TOGETHER, // ACL,        WEB,        WEB
    FeedFile.TOGETHER, // ACL,        WEB,        CONTENTURL
    FeedFile.TOGETHER, // ACL,        WEB,        ACL
    FeedFile.LAST,     // ACL,        CONTENTURL, CONTENT
    FeedFile.TOGETHER, // ACL,        CONTENTURL, WEB
    FeedFile.TOGETHER, // ACL,        CONTENTURL, CONTENTURL
    FeedFile.TOGETHER, // ACL,        CONTENTURL, ACL
    FeedFile.LAST,     // ACL,        ACL,        CONTENT    Should be TOGETHER
    FeedFile.TOGETHER, // ACL,        ACL,        WEB
    FeedFile.TOGETHER, // ACL,        ACL,        CONTENTURL
    FeedFile.TOGETHER, // ACL,        ACL,        ACL
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
        for (FeedType third : FeedType.values()) {
          // Check if the first two feed types go in one file or break into two.
          boolean firstBreak;
          FeedType fileType = first;
          if (second.isCompatible(fileType)) {
            firstBreak = false;
          } else {
            fileType = second; // Reset feed file type to the new type.
            firstBreak = true;
          }

          // Check if the next two feed types go in one file or break into two.
          boolean secondBreak;
          if (third.isCompatible(fileType)) {
            secondBreak = false;
          } else {
            fileType = second; // Ditto, but unused in this case.
            secondBreak = true;
          }

          // Record the breakup into files in terms of our enum values.
          FeedFile outcome = (firstBreak)
              ? ((secondBreak) ? FeedFile.SEPARATE : FeedFile.FIRST)
              : ((secondBreak) ? FeedFile.LAST : FeedFile.TOGETHER);

          assertEquals(i + ": < " + first + ", " + second + ", " + third + " >",
              expectedFeedFiles[i], outcome);
          i++;
        }
      }
    }

    // Make sure the expectedFeedFiles test data is complete.
    assertEquals(expectedFeedFiles.length, i);
  }
}
