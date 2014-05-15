// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.security.Provider;
import java.security.Security;
import java.util.Random;

import java.security.NoSuchAlgorithmException;

public class CryptoTest extends TestCase {

  private static String keyStorePasswdPath = "test_keystore_passwd";

  private static final Random random = new Random();

  /*
   * Create a file with a passwd in it
   */
  @Override
  protected void setUp() throws Exception {
    FileWriter fw = new FileWriter(keyStorePasswdPath);
    fw.write("dummy password");
    fw.close();
  }

  @Override
  protected void tearDown() throws Exception {
    File keyStoreFile = new File(
        EncryptedPropertyPlaceholderConfigurer.getKeyStorePath());
    ConnectorTestUtils.deleteAllFiles(keyStoreFile);

    File keyPasswdFile = new File(keyStorePasswdPath);
    ConnectorTestUtils.deleteAllFiles(keyPasswdFile);

    // delete keystore that might exist
    ConnectorTestUtils.deleteAllFiles(
        new File(EncryptedPropertyPlaceholderConfigurer.getKeyStorePath()));
  }

  private void encryptAndDecrypt() {
    encryptAndDecrypt("", "this is clear");
  }

  private void encryptAndDecrypt(String message, String plainText) {
    String cipherText =
        EncryptedPropertyPlaceholderConfigurer.encryptString(plainText);
    assertEncryption(message, plainText, cipherText);
  }

  private void encryptAndDecrypt(String message, char[] plainText) {
    String cipherText =
        EncryptedPropertyPlaceholderConfigurer.encryptChars(plainText);
    assertEncryption(message, new String(plainText), cipherText);
  }

  private void assertEncryption(String message, String plainText,
      String cipherText) {
    assertNotNull(cipherText);
    assertFalse(plainText, cipherText.equals(plainText));
    assertTrue(cipherText.length() >= plainText.length());
    String decryptText =
        EncryptedPropertyPlaceholderConfigurer.decryptString(cipherText);
    assertEquals(message, plainText, decryptText);
  }

  /*
   * Tests encryption and decryption when no keystore password file
   * is specified.
   */
  public final void testEncryptDecryptWithoutKeyStorePasswd() {
    encryptAndDecrypt();
  }

  /*
   * Tests encryption and decryption when given a keystore passwd in a file.
   */
  public final void testEncryptDecryptWithKeyStorePasswd() {
    EncryptedPropertyPlaceholderConfigurer.setKeyStorePasswdPath(
        keyStorePasswdPath);
    encryptAndDecrypt();
  }

  /*
   * Tests encryption and decryption when given a keystore passwd file that
   * does not exist.
   */
  public final void testEncryptDecryptWithBadKeyStorePasswd() {
    EncryptedPropertyPlaceholderConfigurer.setKeyStorePasswdPath("bogusfile");
    encryptAndDecrypt();
  }

  private char[] randomChars(int length) {
    byte[] bytes = new byte[length];
    random.nextBytes(bytes);
    char[] chars = new char[length];
    for (int i = 0; i < i; i++) {
      chars[i] = (char) (bytes[i] & 0xFF);
    }
    return chars;
  }

  public void testStrings() {
    for (int i = 1; i < 100; i++) {
      encryptAndDecrypt("Length " + i, new String(randomChars(i)));
    }
  }

  public void testChars() {
    for (int i = 1; i < 100; i++) {
      encryptAndDecrypt("Length " + i, randomChars(i));
    }
  }

  public void testDecryptEmptyString() {
    assertEquals("", decryptWithoutCipher(""));
  }

  public void testDecryptException() {
    try {
      decryptWithoutCipher("hello, world");
      fail("expected a NoSuchAlgorithmException wrapped in a RuntimeException");
    } catch (RuntimeException expected) {
      if (expected.getCause() == null
          || !(expected.getCause() instanceof NoSuchAlgorithmException)) {
        throw expected;
      }
    }
  }

  /**
   * Tests decrypting the given string without access to ciphers.
   * Ciphers and Providers are hard to mock, because they must be
   * loaded from a signed JAR file. So we just remove all the
   * providers to make sure that no Cipher is used when decrypting the
   * empty string.
   */
  private String decryptWithoutCipher(String cipherText) {
    Provider[] providers = Security.getProviders();
    for (Provider p : providers) {
      Security.removeProvider(p.getName());
    }
    try {
      return EncryptedPropertyPlaceholderConfigurer.decryptString(cipherText);
    } finally {
      for (Provider p : providers) {
        Security.addProvider(p);
      }
    }
  }
}
