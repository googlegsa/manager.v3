// Copyright 2007 Google Inc.
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

import com.google.common.base.Charsets;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.SecurityUtils;
import com.google.enterprise.connector.util.Base64;
import com.google.enterprise.connector.util.Base64DecoderException;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileLock;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
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
 *     #setKeyStorePasswdPath.
 * <li>The format of the keystore. Your JCE provider must support this.
       Use #setKeyStoreType. The default is "JCEKS", which is
 *     provided by the default Sun JCE provider.
 * <li>The algorithm used for encryption. Your JCE provider must support
 *     this. Use #setKeyStoreCryptoAlgo. The default is "AES",
 *     which is provided by the default Sun JCE provider.
 * </ul>
 *
 * <p>
 * You can also provide these parameters in the {@code config-param}
 * section of the web application's configuration file (web.xml). The
 * relevant parameters are: {@code keystore_file, keystore_passwd_file,
 * keystore_type, keystore_crypto_algo}.
 */
public class EncryptedPropertyPlaceholderConfigurer extends
    PropertyPlaceholderConfigurer {

  private static final Logger LOGGER =
      Logger.getLogger(EncryptedPropertyPlaceholderConfigurer.class.getName());

  private static final String ENCRYPT_MSG = "Could not encrypt ";
  private static final String DECRYPT_MSG = "Could not decrypt ";
  private static final String GENERIC_PROP_NAME = "password";

  private static final String KEY_NAME = "EXTERNAL_CM_KEY";

  private static String keyStorePath = "external_cm.keystore";

  private static String keyStorePasswdPath = null;

  private static String keyStoreType = "JCEKS";

  private static String keyStoreCryptoAlgo = "AES";

  private static SecretKey secretKey = null;

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
            encryptString(prop, properties.getProperty(prop)));
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
            decryptString(prop, properties.getProperty(prop)));
      }
    }
  }

  public static void setKeyStorePath(String k) {
    keyStorePath = k;
    LOGGER.config("Using keystore " + k);
  }

  public static String getKeyStorePath() {
    return keyStorePath;
  }

  public static void setKeyStorePasswdPath(String k) {
    keyStorePasswdPath = k;
    LOGGER.config("Using keystore password file " + k);
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
   * Reads a KeyStore from the supplied file, or creates a new KeyStore
   * if none exists.
   */
  private static KeyStore readKeyStore(RandomAccessFile keyStoreFile,
      char[] keyPassChars) throws IOException, CertificateException,
      NoSuchAlgorithmException, KeyStoreException {
    KeyStore keyStore = KeyStore.getInstance(keyStoreType);

    // If the file is non-empty, read in the KeyStore, otherwise
    // create an empty KeyStore.
    ByteArrayInputStream bais = null;
    if (keyStoreFile.length() > 0L) {
      byte[] buffer = new byte[(int) keyStoreFile.length()];
      keyStoreFile.seek(0L);
      keyStoreFile.read(buffer);
      bais = new ByteArrayInputStream(buffer);
    }

    keyStore.load(bais, keyPassChars);
    return keyStore;
  }

  /*
   * Writes a KeyStore to the supplied file, overwriting it completely.
   */
  private static void writeKeyStore(KeyStore keyStore,
      RandomAccessFile keyStoreFile, char[] keyPassChars) throws IOException,
      CertificateException, NoSuchAlgorithmException, KeyStoreException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    keyStore.store(baos, keyPassChars);

    keyStoreFile.seek(0L);
    keyStoreFile.setLength(0L);
    keyStoreFile.write(baos.toByteArray());
    keyStoreFile.getChannel().force(false);
  }

  /*
   * Reads in password used to secure keystore.
   */
  private static String getKeyStorePasswd() {
    if (keyStorePasswdPath == null) {
      return "";
    }
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
      LOGGER.fine("Keystore passwd file does not exist");
    } catch (IOException e) {
      LOGGER.warning("Could not open keystore passwd file");
    }
    return "";
  }

  /*
   * Reads in secret key from keystore, or generates one and stores it in the
   * keystore if none exists.
   */
  private static synchronized SecretKey getSecretKey()
      throws NoSuchAlgorithmException, KeyStoreException, CertificateException,
             UnrecoverableKeyException, IOException {
    if (secretKey != null) {
      return secretKey;
    }
    char[] keyStorePasswdChars = getKeyStorePasswd().toCharArray();

    File keyStoreFile = new File(keyStorePath);
    keyStoreFile.createNewFile();
    LOGGER.config("Accessing keystore at " + keyStoreFile.getAbsolutePath());

    // Lock the keystore file to avoid concurrent key generation.
    RandomAccessFile raf = new RandomAccessFile(keyStoreFile, "rw");
    try {
      FileLock lock = raf.getChannel().lock();
      try {
        KeyStore keyStore = readKeyStore(raf, keyStorePasswdChars);
        secretKey = (SecretKey)keyStore.getKey(KEY_NAME, keyStorePasswdChars);
        if (secretKey == null) {
          // key did not exist --- create a new key, and store it
          LOGGER.config("Creating new key for password encryption");
          secretKey = KeyGenerator.getInstance(keyStoreCryptoAlgo).generateKey();
          keyStore.setKeyEntry(KEY_NAME, secretKey, keyStorePasswdChars, null);
          writeKeyStore(keyStore, raf, keyStorePasswdChars);
        }
      } finally {
        lock.release();
      }
    } finally {
      raf.close();
    }
    return secretKey;
  }

  public static String encryptString(String plainText) {
    return encryptString(GENERIC_PROP_NAME, plainText);
  }

  public static String encryptString(String name, String plainText) {
    // Convert the String into bytes using utf-8
    byte[] bytes = plainText.getBytes(Charsets.UTF_8);
    String cipherText = encryptBytes(name, bytes);
    // Overwrite the temporary array.
    Arrays.fill(bytes, (byte) 0);
    return cipherText;
  }

  public static String encryptChars(char[] plainText) {
    return encryptChars(GENERIC_PROP_NAME, plainText);
  }

  public static String encryptChars(String name, char[] plainText) {
    // Convert the char[] into bytes using utf-8
    // We do this the hard way to avoid using a String we can't overwrite.
    ByteBuffer buffer = Charsets.UTF_8.encode(CharBuffer.wrap(plainText));
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    String cipherText = encryptBytes(name, bytes);
    // Overwrite the temporary arrays.
    Arrays.fill(bytes, (byte) 0);
    Arrays.fill(buffer.array(), (byte) 0);
    return cipherText;
  }

  public static String encryptBytes(byte[] plainText) {
    return encryptBytes(GENERIC_PROP_NAME, plainText);
  }

  public static String encryptBytes(String name, byte[] plainText) {
    try {
      SecretKey key = getSecretKey();
      Cipher encryptor = Cipher.getInstance(keyStoreCryptoAlgo);
      encryptor.init(Cipher.ENCRYPT_MODE, key);

      // Encrypt the supplied byte buffer.
      byte[] enc = encryptor.doFinal(plainText);
      // Encode bytes to base64 to get a string
      return Base64.encode(enc);
    } catch (NoSuchAlgorithmException e) {
      throw logAndThrow(ENCRYPT_MSG, name,
                        "provider does not have algorithm", e);
    } catch (IOException e) {
      throw logAndThrow(ENCRYPT_MSG, name, "I/O error", e);
    } catch (NoSuchPaddingException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    } catch (InvalidKeyException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    } catch (UnrecoverableKeyException e) {
      throw logAndThrow(ENCRYPT_MSG, name,
                        "key cannot be recovered from keystore", e);
    } catch (KeyStoreException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    } catch (CertificateException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    } catch (IllegalStateException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    } catch (IllegalBlockSizeException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    } catch (BadPaddingException e) {
      throw logAndThrow(ENCRYPT_MSG, name, null, e);
    }
  }

  public static String decryptString(String cipherText) {
    return decryptString(GENERIC_PROP_NAME, cipherText);
  }

  public static String decryptString(String name, String cipherText) {
    try {
      Key secretKey = getSecretKey();
      Cipher decryptor = Cipher.getInstance(keyStoreCryptoAlgo);
      decryptor.init(Cipher.DECRYPT_MODE, secretKey);

      // Decode base64 to get bytes
      byte[] dec = Base64.decode(cipherText);
      // Decrypt
      byte[] utf8 = decryptor.doFinal(dec);
      // Decode using utf-8
      return new String(utf8, "UTF8");
    } catch (NoSuchAlgorithmException e) {
      throw logAndThrow(DECRYPT_MSG, name,
                        "provider does not have algorithm", e);
    } catch (IOException e) {
      throw logAndThrow(DECRYPT_MSG, name, "I/O error", e);
    } catch (KeyStoreException e) {
      throw logAndThrow(DECRYPT_MSG, name, null, e);
    } catch (CertificateException e) {
      throw logAndThrow(DECRYPT_MSG, name, null, e);
    } catch (NoSuchPaddingException e) {
      throw logAndThrow(DECRYPT_MSG, name, null, e);
    } catch (InvalidKeyException e) {
      throw logAndThrow(DECRYPT_MSG, name, null, e);
    } catch (UnrecoverableKeyException e) {
      throw logAndThrow(DECRYPT_MSG, name,
                        "key cannot be recovered from keystore", e);
    } catch (IllegalStateException e) {
      throw logAndThrow(DECRYPT_MSG, name, null, e);
    } catch (BadPaddingException e) {
      throw logAndThrow(DECRYPT_MSG, name,
          "it might be unencrypted or encrypted with a different algorithm", e);
    } catch (IllegalBlockSizeException e) {
      throw logAndThrow(DECRYPT_MSG, name,
          "it might be unencrypted or encrypted with a different algorithm", e);
    } catch (Base64DecoderException e) {
      throw logAndThrow(DECRYPT_MSG, name,
                        "it might not be encrypted at all", e);
    }
  }

  private static RuntimeException logAndThrow(String prefix, String name,
                                              String suffix, Exception e) {
    String msg = prefix + name + ((suffix == null) ? "" : ( ": " + suffix));
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.SEVERE, msg, e);
    } else {
      LOGGER.severe(msg);
    }LOGGER.severe(msg);
    return new RuntimeException(msg);
  }
}
