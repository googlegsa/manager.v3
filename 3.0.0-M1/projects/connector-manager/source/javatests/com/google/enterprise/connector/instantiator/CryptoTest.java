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

import junit.framework.Assert;
import junit.framework.TestCase;
import java.io.File;
import java.io.FileWriter;

public class CryptoTest extends TestCase {

  private static String keyStorePasswdPath = "test_keystore_passwd";

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
    String plainText = "this is clear";
    String cipherText =
        EncryptedPropertyPlaceholderConfigurer.encryptString(plainText);
    System.out.println("ciphertext = " + cipherText);
    String decryptText =
        EncryptedPropertyPlaceholderConfigurer.decryptString(cipherText);
    Assert.assertEquals(decryptText, plainText);
  }

  /*
   * Tests encryption and decryption when no keystore password file
   * is specified.
   */
  public final void testEncryptDecrytWithoutKeyStorePasswd() {
    encryptAndDecrypt();
  }

  /*
   * Tests encryption and decryption when given a keystore passwd in a file.
   */
  public final void testEncryptDecrytWithKeyStorePasswd() {
    EncryptedPropertyPlaceholderConfigurer.setKeyStorePasswdPath(
        keyStorePasswdPath);
    encryptAndDecrypt();
  }

  /*
   * Tests encryption and decryption when given a keystore passwd file that
   * does not exist.
   */
  public final void testEncryptDecrytWithBadKeyStorePasswd() {
    EncryptedPropertyPlaceholderConfigurer.setKeyStorePasswdPath("bogusfile");
    encryptAndDecrypt();
  }
}
