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

import java.io.Console;
import java.io.File;
import java.io.IOException;
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

    Console console = System.console();
    if (console == null) {
      System.err.println("Error: No Console");
      return null;
    }

    while (true) {
      char[] pw1 = console.readPassword("  Type Password: ");
      if ((pw1 == null) || (pw1.length == 0)) {
        System.exit(0);
      }
      char[] pw2 = console.readPassword("Retype Password: ");
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
}
