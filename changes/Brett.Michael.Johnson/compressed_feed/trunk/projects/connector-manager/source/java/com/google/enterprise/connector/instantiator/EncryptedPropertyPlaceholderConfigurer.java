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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.SecurityUtils;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.cert.CertificateException;
import java.io.*;


/**
 * @author haldar@google.com (Vivek Haldar)
 *
 * Extended version of {@link
 * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer}
 * that looks for encrypted sensitive properties (like passwords)
 * and decrypts them when reading them back. The JCE API is used
 * for the relevant key storage and cryptography.
 *
 * <p> You can configure the following parameters used by this class:
 * <ul>
 * <li>File in which keystore is kept. Use #setKeyStorePath.
 * <li>File in which password securing keystore is kept. Use
 *     #setKeyStorePasswdPath}.
 * <li>The format of the keystore. Your JCE provider must support this.
       Use #setKeyStoreType. The default is "JCEKS", which is
 *     provided by the default Sun JCE provider.
 * <li>The algorithm used for encryption. Your JCE provider must support
 *     this. Use #setKeyStoreCryptoAlgo. The default is "AES",
 *     which is provided by the default Sun JCE provider.
 * </ul>
 *
 * <p>
 * You can also provide these parameters in the <code>config-param</code>
 * section of the web application's configuration file (web.xml). The
 * relevant parameters are: <code>keystore_file, keystore_passwd_file,
 * keystore_type, keystore_crypto_algo.</code>
 */
public class EncryptedPropertyPlaceholderConfigurer extends
    PropertyPlaceholderConfigurer {

  private static final String KEY_NAME = "EXTERNAL_CM_KEY";

  private static final Logger LOGGER =
    Logger.getLogger(EncryptedPropertyPlaceholderConfigurer.class.getName());

  private static String keyStorePath = "external_cm.keystore";

  private static String keyStorePasswdPath = null;

  private static String keyStoreType = "JCEKS";

  private static String keyStoreCryptoAlgo = "AES";

  /*
   * Overridden from the base class implementation. This looks for properties
   * with a sensitive name and decrypts them.
   */
  @Override
  public void convertProperties(Properties properties) {
    decryptSensitiveProperties(properties);
    super.convertProperties(properties);
  }

  public static void encryptSensitiveProperties(Properties properties) {
    // New style properties file, encrypt any key with 'password' in it.
    PropertiesUtils.stampPropertiesVersion(properties);
    Enumeration<?> props = properties.propertyNames();
    while (props.hasMoreElements()) {
      String prop = (String) props.nextElement();
      if (SecurityUtils.isKeySensitive(prop)) {
        properties.setProperty(prop,
            encryptString(properties.getProperty(prop)));
      }
    }
  }

  public static void decryptSensitiveProperties(Properties properties) {
    int version = PropertiesUtils.getPropertiesVersion(properties);
    Enumeration<?> props = properties.propertyNames();
    while (props.hasMoreElements()) {
      String prop = (String) props.nextElement();
      // Older properties files (before we started versioning them) only
      // encrypted a property called "Password".  Newer property files
      // encrypt any property with case-insensitive 'password' in the key.
      boolean doCrypt = (version < 1) ? prop.equals("Password") :
          (SecurityUtils.isKeySensitive(prop));
      if (doCrypt) {
        properties.setProperty(prop,
            decryptString(properties.getProperty(prop)));
      }
    }
  }

  public static void setKeyStorePath(String k) {
    keyStorePath = k;
    LOGGER.log(Level.INFO, "Using keystore " + k);
  }

  public static String getKeyStorePath() {
    return keyStorePath;
  }

  public static void setKeyStorePasswdPath(String k) {
    keyStorePasswdPath = k;
  }

  public static void setKeyStoreType(String t) {
    keyStoreType = t;
  }

  public static void setKeyStoreCryptoAlgo(String a) {
    keyStoreCryptoAlgo = a;
  }

  public static String getKeyStoreType() {
    return keyStoreType;
  }

  public static String getKeyStoreCryptoAlgo() {
    return keyStoreCryptoAlgo;
  }

  /*
   * Creates a new keystore if none exists, or reads in existing
   * keystore.
   */
  public static KeyStore getKeyStore() throws
                   KeyStoreException, CertificateException,
                   NoSuchAlgorithmException, IOException {
    KeyStore ks = KeyStore.getInstance(keyStoreType);
    FileInputStream fis = null;
    File f = new File(keyStorePath);
    if (f.exists()) {
      fis = new FileInputStream(f);
      LOGGER.log(Level.INFO, "Using existing keystore at "
          + f.getAbsolutePath());
    }
    String keyStorePasswd = getKeyStorePasswd();
    char [] keyPassChars = keyStorePasswd.toCharArray();
    if (keyStorePasswd.length() == 0) keyPassChars = null;
    ks.load(fis, keyPassChars);
    if (fis != null) fis.close();
    return ks;
  }

  /*
   * Reads in password used to secure keystore.
   */
  private static String getKeyStorePasswd() {
    if (keyStorePasswdPath == null) return "";
    try {
      File f = new File(keyStorePasswdPath);
      BufferedReader in = new BufferedReader(new FileReader(f));
      try {
        String passwd = in.readLine();
        return passwd;
      } finally {
        in.close();
      }
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.FINE, "Keystore passwd file does not exist");
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not open keystore passwd file");
    }
    return "";
  }

  /*
   * Reads in secret key from keystore, or generates one and stores it in the
   * keystore if none exists.
   */
  private static SecretKey getSecretKey() throws NoSuchAlgorithmException,
      KeyStoreException, CertificateException, IOException {

    SecretKey key = null;
    KeyStore keyStore = getKeyStore();
    String keyStorePasswd = getKeyStorePasswd();
    char [] keyStorePasswdChars = keyStorePasswd.toCharArray();

    try {
      key = (SecretKey)keyStore.getKey(KEY_NAME, keyStorePasswdChars);
      if (key == null) {
        // key did not exist --- create a new key, and store it
        LOGGER.log(Level.INFO, "Creating new key for password encryption");
        key = KeyGenerator.getInstance(keyStoreCryptoAlgo).generateKey();
        keyStore.setKeyEntry(KEY_NAME, key, keyStorePasswdChars, null);
        File file = new File(keyStorePath);
        FileOutputStream keyStoreFile = new FileOutputStream(file);
        keyStore.store(keyStoreFile, keyStorePasswdChars);
        keyStoreFile.close();
      }
    } catch (UnrecoverableKeyException e) {
      e.printStackTrace();
      LOGGER.log(Level.SEVERE, "Key cannot be recovered from keystore");
    }
    return key;
  }

  public static String encryptString(String plainText) {
    try {
      SecretKey key = getSecretKey();
      Cipher encryptor = Cipher.getInstance(keyStoreCryptoAlgo);
      encryptor.init(Cipher.ENCRYPT_MODE, key);

      // Encode the string into bytes using utf-8
      byte[] utf8 = plainText.getBytes("UTF8");
      // Encrypt
      byte[] enc = encryptor.doFinal(utf8);
      // Encode bytes to base64 to get a string
      return new sun.misc.BASE64Encoder().encode(enc);
    } catch (NoSuchAlgorithmException e) {
      String msg = "Could not encrypt password: provider does not have algorithm";
      LOGGER.log(Level.SEVERE, msg);
      throw new RuntimeException(msg);
    } catch (IOException e) {
      String msg = "Could not encrypt password: I/O error";
      LOGGER.log(Level.SEVERE, msg);
      throw new RuntimeException(msg);
    } catch (NoSuchPaddingException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    } catch (InvalidKeyException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    } catch (KeyStoreException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    } catch (CertificateException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    } catch (IllegalStateException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    } catch (IllegalBlockSizeException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    } catch (BadPaddingException e) {
      LOGGER.log(Level.SEVERE, "Could not encrypt password");
      throw new RuntimeException("Could not encrypt password");
    }
  }

  public static String decryptString(String cipherText) {
    try {
      Key secretKey = getSecretKey();

      Cipher decryptor = Cipher.getInstance(keyStoreCryptoAlgo);
      decryptor.init(Cipher.DECRYPT_MODE, secretKey);

      // Decode base64 to get bytes
      byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(cipherText);
      // Decrypt
      byte[] utf8 = decryptor.doFinal(dec);
      // Decode using utf-8
      return new String(utf8, "UTF8");
    } catch (NoSuchAlgorithmException e) {
      String msg = "Could not decrypt password: provider does not have algorithm";
      LOGGER.log(Level.SEVERE, msg);
      throw new RuntimeException(msg);
    } catch (IOException e) {
      String msg = "Could not decrypt password: I/O error";
      LOGGER.log(Level.SEVERE, msg);
      throw new RuntimeException(msg);
    } catch (KeyStoreException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    } catch (CertificateException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    } catch (NoSuchPaddingException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    } catch (InvalidKeyException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    } catch (BadPaddingException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    } catch (IllegalStateException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    } catch (IllegalBlockSizeException e) {
      LOGGER.log(Level.SEVERE, "Could not decrypt password");
      throw new RuntimeException("Could not decrypt password");
    }
  }
}
