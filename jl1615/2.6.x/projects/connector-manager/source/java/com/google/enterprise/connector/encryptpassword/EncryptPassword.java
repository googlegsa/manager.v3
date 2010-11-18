// Copyright 2010 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.encryptpassword;

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;
import com.google.enterprise.connector.servlet.SAXParseErrorHandler;
import com.google.enterprise.connector.servlet.ServletUtil;

import java.io.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * A utility to encrypt passwords from the command line.
 *
 * usage: EncryptPassword [-v] [-h] [-p password]");
 *        -v, --version   Display version.");
 *        -h, --help      Display this help.");
 *        -p, --password  Encrypt the supplied password.");
 */
public class EncryptPassword {
  private static final Logger LOGGER =
      Logger.getLogger(EncryptPassword.class.getName());

  private String webXmlPath = "";
  private String webXmlName = "web.xml";

  // This is the default keystore config from out-of-box web.xml.
  private String keystore_type = "JCEKS";
  private String keystore_crypto_algo = "AES";
  private String keystore_passwd_file = "keystore_passwd";
  private String keystore_file = "connector_manager.keystore";

  // Arguments that may be specified on the command line.
  private String password = null;

  // The EncryptedPropertyPlaceholderConfigurer used to encrypt the password.
  private EncryptedPropertyPlaceholderConfigurer cryptor;


  /**
   * Construct the EncryptPassword application.
   */
  private EncryptPassword() throws Exception {
    configureCryptor();
  }

  /**
   * Construct the EncryptPassword application.
   *
   * @param args supplied command line arguments
   */
  private EncryptPassword(String[] args) throws Exception {
    getArgs(args);
    configureCryptor();
  }

  /**
   * Encrypt password static method suitable for calling by the Installer.
   * As a side effect, the supplied plain-text password character array
   * is cleared.
   *
   * @param password the plain-text password to encrypt
   * @return the encrypted password
   */
  public static final String encrypt(char[] password) throws Exception {
    return new EncryptPassword().encryptPassword(password);
  }

  /**
   * A command line utility to encrypt passwords.
   */
  public static final void main(String[] args) throws Exception {
    EncryptPassword app = new EncryptPassword(args);
    char[] password = app.getPassword();
    if (password != null) {
      System.out.println(app.encryptPassword(password));
    }
    System.exit(0);
  }

  /**
   * Extracts the keystore configuration from the web.xml.
   *
   * @param in an XML InputStream
   */
  private void getKeystoreContextParams(InputStream in) {
    Document document = ServletUtil.parse(in, new SAXParseErrorHandler(),
        new ServletUtil.LocalEntityResolver());
    NodeList params = document.getElementsByTagName("context-param");
    if (params == null) {
      return;
    }
    for (int i = 0; i < params.getLength(); i++) {
      Element param = (Element)params.item(i);
      String name = ServletUtil.getFirstElementByTagName(param, "param-name");
      String value = ServletUtil.getFirstElementByTagName(param, "param-value");
      if (value != null) {
        if ("keystore_type".equals(name)) {
          keystore_type = value;
        } else if ("keystore_crypto_algo".equals(name)) {
          keystore_crypto_algo = value;
        } else if ("keystore_passwd_file".equals(name)) {
          keystore_passwd_file = value;
        } else if ("keystore_file".equals(name)) {
          keystore_file = value;
        }
      }
    }
  }

  /**
   * Opens and parses the web.xml file, extracting the keystore configuration
   * parameters.
   *
   * @param webXml File for web.xml
   */
  private void parseWebXml(File webXml) {
    try {
      InputStream is = new BufferedInputStream(new FileInputStream(webXml));
      getKeystoreContextParams(is);
      is.close();
    } catch (IOException e) {
      System.err.println(
          "Unable to read file: " + webXml.getAbsolutePath());
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Configure a EncryptedPropertyPlaceholderConfigurer.
   */
  private void configureCryptor() {
    // Because of differences in ServletContext and StandaloneContext,
    // we must be running from the WEB-INF directory.  See keystore
    // configuration in the StartUp servlet for details.
    File webXml = new File("web.xml");
    if (!webXml.exists()) {
      System.err.println("You must run EncryptPassword from the "
                         + "connector-manager/WEB-INF directory.");
      System.exit(-1);
    }

    // Extract the Connector Manager Keystore config from web.xml.
    parseWebXml(webXml);

    // Supply EncryptedPropertyPlaceholder with the keystore config.
    cryptor = new EncryptedPropertyPlaceholderConfigurer();
    cryptor.setKeyStoreType(keystore_type);
    cryptor.setKeyStorePath(keystore_file);
    cryptor.setKeyStorePasswdPath(keystore_passwd_file);
    cryptor.setKeyStoreCryptoAlgo(keystore_crypto_algo);
  }


  /**
   * Parses the command line arguments.
   */
  private void getArgs(String[] args) {
    boolean showVersion = false;
    boolean showHelp = false;

    // Interpret the command line args.
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("-h".equals(arg) || "-?".equals(arg) || "--help".equals(arg)) {
        showHelp = true;
      } else if ("-v".equalsIgnoreCase(arg) || "--version".equals(arg)) {
        showVersion = true;
      } else if (arg.startsWith("--password=")) {
        password = arg.substring(arg.indexOf('=') + 1);
      } else if ("-p".equals(arg) || "--password".equals(arg)) {
        if (++i < args.length) {
          password = args[i];
        } else {
          showHelp = true;
        }
      }
    }

    // If the user only asks for version or usage, do so then get out.
    if (showVersion || showHelp) {
      version();
      if (showHelp) {
        usage();
      }
      System.exit(0);
    }
  }

  /** Displays the product version. */
  private void version() {
    System.err.println("EncryptPassword v"
        + JarUtils.getJarVersion(EncryptPassword.class));
    System.err.println("");
  }

  /** Displays the product usage. */
  private void usage() {
    System.err.println("usage: EncryptPassword [-v] [-h] [-p password]");
    System.err.println("       -v, --version   Display version.");
    System.err.println("       -h, --help      Display this help.");
    System.err.println("       -p, --password  Encrypt the supplied password.");
  }

  /**
   * Returns the password to encrypt.  If a password was not supplied on the
   * command line, prompt for it.
   *
   * @return String password
   */
  private char[] getPassword() {
    if (password != null) {
      // If password was supplied on command line, return it.
      return password.toCharArray();
    }

    // Since we will be prompting, might as well display the version.
    version();

    // If we are running under Java 6, use Console.readPassword().
    // Otherwise, do masking in a background thread.
    PasswordReader reader;
    try {
      reader = new Java6PasswordReader();
    } catch (UnsupportedOperationException e) {
      reader = new Java5PasswordReader();
    }

    while (true) {
      char[] pw1 = reader.readPassword("  Type Password: ");
      if ((pw1 == null) || (pw1.length == 0)) {
        System.exit(0);
      }
      char[] pw2 = reader.readPassword("Retype Password: ");
      if (Arrays.equals(pw1, pw2)) {
        System.out.println("\nThe encrypted password is:");
        Arrays.fill(pw2, '\0');
        return pw1;
      }
      System.out.println("\007\nPasswords do not match.  Please try again.\n");
    }
  }

  /**
   * Encrypts the supplied password.  As a side-effect, it clears the supplied
   * plain-text password after the conversion.
   *
   * @param password plain text password
   * @return encrypted password
   */
  private String encryptPassword(char[] password) {
    try {
      return cryptor.encryptChars(password);
    } finally {
      Arrays.fill(password, '\0');
    }
  }

  private static interface PasswordReader {
    /**
     * Read a password from input, masking it as it is typed.
     *
     *@param prompt The prompt to display to the user
     *@return The password as entered by the user
     */
    public char[] readPassword(String prompt);
  }

  /**
   * PasswordReader implementation for Java6.
   */
  private static class Java6PasswordReader implements PasswordReader {
    private Object console;
    private Method readPassword;

    /**
     * Use Reflection to detect Java 6 and invoke Console.readPassword().
     *
     * @throws UnsupportedOperationException if unable to use Java 6
     *         Console.readPassword().
     */
    Java6PasswordReader() {
      try {
        Method getConsole = System.class.getMethod("console");
        console = getConsole.invoke(System.class);
        if (console == null) {
          throw new UnsupportedOperationException("No Console");
        }
        Class[] paramTypes = new Class[] { String.class, Object[].class };
        readPassword = console.getClass().getMethod("readPassword", paramTypes);
      } catch (NoSuchMethodException e) {
        throw new UnsupportedOperationException("Not Java 6");
      } catch (IllegalAccessException e) {
        throw new UnsupportedOperationException(e.getMessage());
      } catch (InvocationTargetException e) {
        throw new UnsupportedOperationException(e.getMessage());
      }
    }

    /**
     * Read a password from input, masking it as it is typed.
     *
     *@param prompt The prompt to display to the user
     *@return The password as entered by the user
     */
    public char[] readPassword(String prompt) {
      Object[] params = new Object[] { prompt, new Object[0] };
      try {
        return (char[])(readPassword.invoke(console, params));
      } catch (IllegalAccessException e) {
        System.err.println("Failed to read password: " + e.getMessage());
      } catch (InvocationTargetException e) {
        System.err.println("Failed to read password: " + e.getMessage());
      }
      return null;
    }
  }

  /**
   * PasswordReader implementation for Java5, which lacks Console.readPassword().
   */
  private static class Java5PasswordReader implements PasswordReader {
    /**
     * Read a password from input, masking it as it is typed.
     *
     *@param prompt The prompt to display to the user
     *@return The password as entered by the user
     */
    public char[] readPassword(String prompt) {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      char[] password;

      // Display prompt.
      System.out.print(prompt);

      // Start up Password Masking Thread.
      MaskingThread masker = new MaskingThread();
      (new Thread(masker)).start();

      try {
        return in.readLine().toCharArray();
      } catch (IOException e) {
        System.err.println("Failed to read password: " + e.getMessage());
      } finally {
        // Shut down the masking thread.
        masker.stop();
      }
      return null;
    }

    /**
     * Masking thread overwrites input with spaces.
     */
    class MaskingThread implements Runnable {
      private boolean done = false;

      /**
       * Begin masking password entry.
       */
      public void run () {
        while (!done) {
          // Backspace over last char and overwrite with a space.
          System.out.print("\010 ");
          try {
            Thread.sleep(1);
          } catch(InterruptedException ignored) {}
        }
      }

      /**
       * Instruct the thread to stop masking.
       */
      public void stop() {
        done = true;
      }
    }
  }
}

