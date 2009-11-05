// Copyright 2009 Google Inc.
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

import junit.framework.TestCase;

/**
 * Verifies that basic password fingerprint generation, verification and
 * transformations to/from Strings are done correctly.
 */
public class SecurePasswordHasherTest extends TestCase {

  private static final String kUsername = "username";

  // The password used to generate fingerprints.
  private static final String kPassword = "password12345";

  // Here 'Good' means 'properly base64 encoded'.
  private static final String kGoodHash = "USLWaU35LdrcCaj0FoLmYw==";

  // Here 'Good' means 'properly base64 encoded'.
  private static final String kGoodSeed = "he/d3W5L/WCh5w7t2omYuQ==";

  private static final String kBadPassword = "secret";

  private static final String kBadHash = "bad_hash";

  private static final String kBadSeed = "bad_seed";

  private static final String kAlgorithm = "PBKDF2WithHmacSHA1";

  // This is a non-default number of iterations used to
  // ensure that this works as well.
  private static final int kIterations = 10;

  public void testVerifyFingerprint() {
    SecurePasswordHasher.Fingerprint fingerprint =
        SecurePasswordHasher.getFingerprint(kPassword);

    assertTrue(SecurePasswordHasher.verifyFingerprint(kPassword, fingerprint));

    assertFalse(SecurePasswordHasher.verifyFingerprint(kBadPassword, fingerprint));

    // Verify changing the number of iterations does not verify correctly.
    fingerprint = new SecurePasswordHasher.Fingerprint(fingerprint.hash(),
        fingerprint.seed(), fingerprint.algorithm(), kIterations);
    assertFalse(SecurePasswordHasher.verifyFingerprint(kPassword, fingerprint));

    // The hash and seed used here will probably not correctly parse as
    // base64 strings.
    fingerprint = new SecurePasswordHasher.Fingerprint(kBadHash,
        kBadSeed, kAlgorithm, kIterations);
    assertFalse(SecurePasswordHasher.verifyFingerprint(kPassword, fingerprint));
    assertFalse(SecurePasswordHasher.verifyFingerprint(kBadPassword, fingerprint));

    // Here we use valid base64 strings, but ones that should not
    // verify correctly.
    fingerprint = new SecurePasswordHasher.Fingerprint(kGoodHash,
        kGoodSeed, kAlgorithm, kIterations);
    assertFalse(SecurePasswordHasher.verifyFingerprint(kPassword, fingerprint));
    assertFalse(SecurePasswordHasher.verifyFingerprint(kBadPassword, fingerprint));
  }

  public void testParseFingerprint() {
    SecurePasswordHasher.Fingerprint fingerprint =
        SecurePasswordHasher.getFingerprint(kPassword);

    String stringFingerprint = fingerprint.toString();

    SecurePasswordHasher.Fingerprint parsedFingerprint =
        SecurePasswordHasher.Fingerprint.parseFingerprint(stringFingerprint);

    assertEquals(parsedFingerprint, fingerprint);

    assertTrue(SecurePasswordHasher.verifyFingerprint(kPassword, parsedFingerprint));
  }

  public void testMacInput() {
    String mac = SecurePasswordHasher.getMac(kUsername, kPassword);
    System.out.println(mac);

    // Ensure mac function is deterministic (unlike fingerprint function)
    assertEquals(mac, SecurePasswordHasher.getMac(kUsername, kPassword));

    // Ensure mac function is deterministic (unlike fingerprint function)
    assertFalse(mac.equals(SecurePasswordHasher.getMac(kUsername, kBadPassword)));
  }
}
