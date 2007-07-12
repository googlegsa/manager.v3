// Copyright 2007 Google Inc.  All Rights Reserved.
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
package com.google.enterprise.connector.instantiator;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.*;
import java.security.cert.CertificateException;
import java.io.*;



/**
 * @author haldar@google.com (Vivek Haldar)
 *
 */
public class EncryptedPropertyPlaceholderConfigurer extends
    PropertyPlaceholderConfigurer {

  private static final String KEY_NAME = "EXTERNAL_CM_KEY";
  
  private static final Logger LOGGER =
    Logger.getLogger(InstanceInfo.class.getName());

  private String connectorName;

  private static String keyStorePath = "external_cm.keystore";
  
  private static String keyStorePasswdPath = null;
  
  public void convertProperties(Properties properties) {
    String encPasswd = properties.getProperty("Password");
    if (encPasswd != null) {
      String plainPasswd = decryptString(encPasswd);
      properties.setProperty("Password", plainPasswd);
    }
  }
  
  public static void setKeyStorePath(String k) {
    keyStorePath = k;
    LOGGER.log(Level.INFO, "Using keystore " + k);
  }
  
  public static void setKeyStorePasswdPath(String k) {
    keyStorePasswdPath = k;    
  }  
  
  public static KeyStore getKeyStore() throws 
                   KeyStoreException, CertificateException, 
                   NoSuchAlgorithmException, IOException {
    KeyStore ks = KeyStore.getInstance("JCEKS");
    FileInputStream fis = null;
    File f = new File(keyStorePath);
    if (f.exists()) {
      fis = new FileInputStream(f);
    }
    String keyStorePasswd = getKeyStorePasswd();
    char [] keyPassChars = keyStorePasswd.toCharArray();
    if (keyStorePasswd.length() == 0) keyPassChars = null;    
    ks.load(fis, keyStorePasswd.toCharArray());
    return ks;
  }
  
  private static String getKeyStorePasswd() {
    if (keyStorePasswdPath == null) return "";
    try {
      File f = new File(keyStorePasswdPath);
      BufferedReader in = new BufferedReader(new FileReader(f));
      String passwd = in.readLine();
      return passwd;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Could not open keystore passwd file");
    }
    return "";
  }
  
  public static String encryptString(String plainText) {
    try {
      SecretKey key = KeyGenerator.getInstance("AES").generateKey();
      Cipher encryptor = Cipher.getInstance("AES");
      encryptor.init(Cipher.ENCRYPT_MODE, key);
 
      KeyStore ks = getKeyStore();
      String keyStorePasswd = getKeyStorePasswd();
      char [] keyStorePasswdChars = keyStorePasswd.toCharArray();

      ks.setKeyEntry(KEY_NAME, key, keyStorePasswdChars, null);
      File file = new File(keyStorePath);
      FileOutputStream keyStoreFile = new FileOutputStream(file);
      ks.store(keyStoreFile, keyStorePasswdChars);
      
      // Encode the string into bytes using utf-8
      byte[] utf8 = plainText.getBytes("UTF8");

      // Encrypt
      byte[] enc = encryptor.doFinal(utf8);

      // Encode bytes to base64 to get a string
      return new sun.misc.BASE64Encoder().encode(enc);
      
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
    }
    return null; // should never get here
  }
  
  public static String decryptString(String cipherText) {
    try {
      KeyStore ks = getKeyStore();
      String keyStorePasswd = getKeyStorePasswd();
      char [] keyStorePasswdChars = keyStorePasswd.toCharArray();
      Key secretKey = ks.getKey(KEY_NAME, keyStorePasswdChars);
      
      Cipher decryptor = Cipher.getInstance("AES");
      decryptor.init(Cipher.DECRYPT_MODE, secretKey);
      
      // Decode base64 to get bytes
      byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(cipherText);
      // Decrypt
      byte[] utf8 = decryptor.doFinal(dec);
      // Decode using utf-8
      return new String(utf8, "UTF8");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
    }
    return ""; // should never get here
  }
}
