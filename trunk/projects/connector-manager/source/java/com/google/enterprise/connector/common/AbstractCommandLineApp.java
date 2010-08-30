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

package com.google.enterprise.connector.common;

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.servlet.SAXParseErrorHandler;
import com.google.enterprise.connector.servlet.ServletUtil;

import org.apache.commons.cli.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract superclass for building Connector Manager command line apps.
 */
public abstract class AbstractCommandLineApp {
  private static final Logger LOGGER =
      Logger.getLogger(AbstractCommandLineApp.class.getName());

  /**
   * A couple of basic Options that all command line apps should support.
   * Note that user request for help and version is handled especially by
   * this framework, so the subclass need not bother with them.
   * However, the subclass should add these options to the Options object
   * it constructs.
   */
  protected static final Option HELP_OPTION =
      new Option("?", "help", false, "Display this help.");

  protected static final Option VERSION_OPTION =
      new Option("v", "version", false, "Display version string.");


  /** Parsed CommandLine. */
  protected CommandLine commandLine;

  /**
   * Returns the name of the command line application.
   */
  public abstract String getName();

  /**
   * Executes the command line app.
   *
   * @param commandLine a parsed {@code org.apache.commons.cli.CommandLine}.
   */
  public abstract void run(CommandLine commenadLine) throws Exception;

  /**
   * Returns a Command Line Syntax as a String. This is used to generate
   * the usage output.  The base class includes the application name, plus
   * the base class options (help and version). Subclasses may override this
   * method adding their additional Options, plus non-option arguments.
   */
  public String getCommandLineSyntax() {
    return getName() + " [-?] [-v] ";
  }

  /**
   * Returns a base set of Options, including {@code VERSION_OPTION} and
   * {@code HELP_OPTION}.  Subclasses may override this method and add
   * additional app-specific options to the set.
   *
   * @return {@code org.apache.commons.cli.Options}
   */
  protected Options getOptions() {
    Options options = new Options();
    options.addOption(HELP_OPTION);
    options.addOption(VERSION_OPTION);
    return options;
  }

  /**
   * Initializes a standalone Connector Manager application context.  If the
   * CommandLineApp starts a standalone context, it must call {@link shutdown()}
   * before exiting.
   *
   * @param doStart If {@code true}, start the Context via {@link Context#start}
   *        (with traversals disabled), otherwise construct all the initial
   *        beans, but do not actually start the Connector Manager appliction.
   */
  protected void initStandAloneContext(boolean doStart) {
    // Turn down the logging output to the console.
    setLoggingLevels();

    // At this time, the current directory *must* be the parent of the Connector
    // Manager WEB-INF directory, or Spring instantiation fails.
    File webInfDir = new File(System.getProperty("user.dir"), "WEB-INF");
    if (!(webInfDir.exists() && webInfDir.isDirectory())) {
      System.err.println(
          "Current directory must be webapps/connector-manager/");
      System.exit(-1);
    }

    // If a catalina.base property is not specified, make a guess based on the
    // knowledge that cwd is likely ${catalina.base}/webapps/connector-manager.
    if (System.getProperty("catalina.base") == null) {
      try {
        System.setProperty("catalina.base", webInfDir.getAbsoluteFile()
                           .getParentFile().getParentFile().getParent());
      } catch (NullPointerException npe) {
        // Bad guess. Go on without it.
      }
    }

    // Establish the webapp keystore configuration before initializing
    // the Context.
    configureCryptor(webInfDir);

    // Setup the standalone application Context.
    Context context = Context.getInstance();
    context.setStandaloneContext("WEB-INF/applicationContext.xml",
                                 webInfDir.getAbsolutePath());

    // At this point the beans have been created, but the Connector Manager
    // has not started up.
    if (doStart) {
      context.setFeeding(false);
      context.start();
    }
  }

  /**
   * Sets the Logging Levels.  This is typically used to turn logging down
   * to WARNING or SEVERE to avoid excessive logging to the console logger
   * when running our command line app.  Subclasses may override this if
   * they want different logging behaviour.
   */
  // TODO: Look for logging levels on the command line via -D...
  protected void setLoggingLevels() {
    // Turn down the logging output to the console.
    Logger.getLogger("com.google.enterprise.connector").setLevel(Level.WARNING);
    Logger.getLogger("org.springframework").setLevel(Level.WARNING);
  }

  /**
   * Shuts down the the command line application context.  Subclasses may
   * override this method, but should call super.shutdown() if they do.
   */
  protected void shutdown() {
    Context.getInstance().shutdown(true);
  }

  /**
   * Returns the Version string for this application.
   */
  protected String getVersion() {
    return this.getName() + " v" + JarUtils.getJarVersion(this.getClass());
  }

  /**
   * Displays the product version.
   */
  protected void printVersion() {
    System.err.println(getVersion());
    System.err.println("");
  }

  /**
   * Displays the product version and exits.  This is called automatically
   * if the user invokes the app with "-v" or "--version".
   *
   * @param exitCode code to supply to {@code System.exit()}
   */
  protected void printVersionAndExit(int exitCode) {
    printVersion();
    System.exit(exitCode);
  }

  /**
   * Displays the product usage.
   * invokes the app with "-?", "-h" or "--help" or required {@code Options}
   * are not supplied.  Subclasses may call this if the supplied command line
   * options are inconsistent with correct operation.
   **/
  protected void printUsage() {
    printVersion();
    PrintWriter out = new PrintWriter(System.err, true);
    HelpFormatter helper = new HelpFormatter();
    helper.printHelp(out, 79, getCommandLineSyntax(), null,
                     getOptions(), 7, 4, "\n");
  }

  /**
   * Displays the product usage, then exits with the supplied code.
   * This is called automatically if the user invokes the app with "-?",
   * or "--help" or required {@code Options} are not supplied.
   * Subclasses may call this if the supplied command line options are
   * inconsistent with correct operation.
   *
   * @param exitCode code to supply to  {@code System.exit()}
   */
  protected void printUsageAndExit(int exitCode) {
    printUsage();
    System.exit(exitCode);
  }

  /**
   * Parses the supplied command line arguments according to the configured
   * {@code Options} generating a {@code CommandLine}.  If parsing the options
   * fails for any reason, or the user specifically requested help,
   * then {@link printUsageAndExit(int)} is called. Similarly, if the user
   * requests the product version, then {@link printVersionAndExit(int)}
   *  is called.
   *
   * @param args String array of supplied command line arguments.
   */
  protected CommandLine parseArgs(String[] args) {
    try {
      commandLine = new PosixParser().parse(getOptions(), args);
      if (commandLine.hasOption(HELP_OPTION.getLongOpt())) {
        printUsageAndExit(0);
      } else if (commandLine.hasOption(VERSION_OPTION.getLongOpt())) {
        printVersionAndExit(0);
      }
      return commandLine;
    } catch (ParseException pe) {
      printUsageAndExit(-1);
    }
    return null;
  }

  // This is the default keystore config from out-of-box web.xml.
  private String keystore_type = "JCEKS";
  private String keystore_crypto_algo = "AES";
  private String keystore_passwd_file = "keystore_passwd";
  private String keystore_file = "connector_manager.keystore";

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
   * Configure a {@link EncryptedPropertyPlaceholderConfigurer}.
   * This must be done before starting up a standalone {@link Context}.
   * Subclasses may override this if they wish to configure the
   * {@link EncryptedPropertyPlaceholderConfigurer} differently.
   *
   * @param webInfDir {@code connector-manager/WEB-INF} directory.
   */
  protected void configureCryptor(File webInfDir) {
    File webXml = new File(webInfDir, "web.xml");
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

    // Supply EncryptedPropertyPlaceholder with the keystore config.
    EncryptedPropertyPlaceholderConfigurer.setKeyStoreType(keystore_type);
    EncryptedPropertyPlaceholderConfigurer
        .setKeyStorePasswdPath(keystore_passwd_file);
    EncryptedPropertyPlaceholderConfigurer
        .setKeyStoreCryptoAlgo(keystore_crypto_algo);

    // Because of differences in ServletContext and StandaloneContext,
    // there are differences in the expected location of the keystore file.
    // See keystore configuration in the StartUp servlet for details.
    String keystorePath = new File(webInfDir, keystore_file).getAbsolutePath();
    EncryptedPropertyPlaceholderConfigurer.setKeyStorePath(keystorePath);
  }
}
