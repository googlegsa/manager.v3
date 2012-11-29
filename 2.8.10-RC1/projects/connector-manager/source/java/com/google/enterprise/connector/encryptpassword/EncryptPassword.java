// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility to encrypt passwords from the command line.
 *
 * usage: EncryptPassword [-?] [-v] [-p password]
 *        -?, --help      Display this help.
 *        -v, --version   Display version.
 *        -p, --password  Encrypt the supplied password.
 */
public class EncryptPassword extends AbstractCommandLineApp {
  /**
   * Encrypt password static method suitable for calling by the Installer.
   * As a side effect, the supplied plain-text password character array
   * is cleared.
   *
   * @param password the plain-text password to encrypt
   * @return the encrypted password
   */
  public static final String encrypt(char[] password) throws Exception {
    EncryptPassword app = new EncryptPassword();
    app.initStandAloneContext(false);
    return app.encryptPassword(password);
  }

  @Override
  public String getName() {
    return "EncryptPassword";
  }

  @Override
  public String getDescription() {
    return "Encrypts passwords using the Connector Manager keystore.";
  }

  @Override
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax() + "[-p password]";
  }

  @Override
  public Options getOptions() {
    Options options = super.getOptions();
    Option option =
        new Option("p", "password", true, "Encrypt the supplied password.");
    option.setArgName("password");
    options.addOption(option);
    return options;
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    initStandAloneContext(false);
    char[] password = getPassword(commandLine.getOptionValue("password"));
    if (password != null) {
      System.out.println(encryptPassword(password));
    }
  }

  @Override
  protected void initStandAloneContext(boolean ignored) {
    // Turn down the logging output to the console.
    Logger.getLogger("com.google.enterprise.connector").setLevel(Level.WARNING);

    // Find the Connector Manager WEB-INF directory.
    File webInfDir = locateWebInf();
    if (webInfDir == null) {
      System.err.println(
          "Unable to locate the connector-manager webapp directory.");
      System.err.println("Try changing to that directory, or use");
      System.err.println("-Dmanager.dir=/path/to/webapps/connector-manager");
      System.exit(-1);
    }

    // Establish the webapp keystore configuration before initializing
    // the Context.
    try {
      configureCryptor(webInfDir);
    } catch (IOException e) {
      System.err.println("Failed to read keystore configuration: " + e);
      System.exit(-1);
    }
  }

  /**
   * A command line utility to encrypt passwords.
   */
  public static final void main(String[] args) throws Exception {
    EncryptPassword app = new EncryptPassword();
    app.run(app.parseArgs(args));
    System.exit(0);
  }

  /**
   * Returns the password to encrypt.  If a password was not supplied on the
   * command line, prompt for it.
   *
   * @param password password option supplied on command line.
   * @return String password
   */
  private char[] getPassword(String password) {
    if (password != null) {
      // If password was supplied on command line, return it.
      return password.toCharArray();
    }

    // Since we will be prompting, might as well display the version.
    printVersion();

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
      return EncryptedPropertyPlaceholderConfigurer.encryptChars(password);
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
        Class<?>[] paramTypes = new Class[] { String.class, Object[].class };
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
