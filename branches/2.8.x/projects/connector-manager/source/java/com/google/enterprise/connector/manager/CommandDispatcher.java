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

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.encryptpassword.EncryptPassword;
import com.google.enterprise.connector.importexport.DumpConnectors;
import com.google.enterprise.connector.importexport.ImportExport;
import com.google.enterprise.connector.persist.MigrateStore;
import com.google.enterprise.connector.servlet.ServletUtil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.util.TreeMap;

/**
 * The Connector Manager Command Processor.  This is thean entry point
 * for command line access to Connector Manager features.  It is
 * typically invoked from {@link ManagerMain} when that Jar Main class
 * is called with command line arguments, while avoiding ManagerMain direct
 * dependence on external libraries (specifically Apache Commons CLI).
 * <p>
 * There are shell scripts, {@code Manager} and {@code Manager.bat}, that
 * may be used to more conveniently invoke the command processor.
 * <p>
 * <pre>
 * Usage: Manager [-?] [-v] [command] [options] [arguments]
 *        -?, --help     Display the set of available commands
 *        -v, --version  Display the Connector Manager version
 *
 * To get help for a command, specify the command along with -? or --help
 * For instance:
 *   Manager MigrateStore --help
 * </pre>
 */
public class CommandDispatcher extends AbstractCommandLineApp {

  // TODO: The command implementations should really register themselves.
  private static TreeMap<String, Command> commands;
  {
    commands = new TreeMap<String, Command>();
    addCommand(new DumpConnectors(), false);
    addCommand(new EncryptPassword(), false);
    addCommand(new MigrateStore(), false);
    addCommand(new ImportExport(), true);
  }

  /**
   * Adds a new command line app to the set of supported commands.
   *
   * @param app the command line application.
   * @param hidden if true, do not list the command in help.
   */
  private static void addCommand(AbstractCommandLineApp app, boolean hidden) {
    Command command = new Command(app, hidden);
    commands.put(command.name.toLowerCase(), command);
  }

  /**
   * Represents a Command Line application.
   */
  private static class Command {
    String name;
    String description;
    boolean hidden;
    Class<? extends AbstractCommandLineApp> appClass;

    public Command(AbstractCommandLineApp app, boolean hidden) {
      this.name = app.getName().trim();
      this.appClass = app.getClass();
      this.hidden = hidden;
      if (!hidden) {
        this.description = app.getDescription();
      }
    }
  }

  private final String[] originalArgs;

  /**
   * Construct a new CommandDispatcher, preserving the original args.
   */
  public CommandDispatcher(String[] args) {
    this.originalArgs = args;
  }

  @Override
  public String getName() {
    return ServletUtil.MANAGER_NAME;
  }

  @Override
  public String getDescription() {
    return ServletUtil.MANAGER_NAME + " command processor.";
  }

  @Override
  public String getCommandLineSyntax() {
    return "Manager [-?] [-v]";
  }

  @Override
  public String getUsageHeader() {
    return "or     Manager [command] [options] [arguments]";
  }


  @Override
  protected void printUsage() {
    super.printUsage();
    System.err.print(getAdditionalUsage());
  }

  // Doesn't override getUsageFooter() because HelpFormatter
  // strips the leading whitespace from my lines.
  private String getAdditionalUsage() {
    StringBuilder builder = new StringBuilder();
    // Figure out the longest command name for formatting output.
    int longestCommand = 0;
    for (Command command : commands.values()) {
      if (command.name.length() > longestCommand) {
        longestCommand = command.name.length();
      }
    }

    // Now add the descriptions of the available commands.
    builder.append("Available commands:").append(NL);
    for (Command command : commands.values()) {
      addDescription(command, builder, longestCommand);
    }

    builder.append(NL);
    builder.append("To get help for a command, specify the command along");
    builder.append(" with -? or --help").append(NL);
    builder.append("For instance:").append(NL);
    builder.append("  Manager MigrateStore --help").append(NL).append(NL);
    return builder.toString();
  }

  private void addDescription(Command command, StringBuilder builder,
                              int longestCommand) {
    if (!command.hidden) {
      builder.append("  ").append(command.name);
      for (int i = command.name.length(); i < longestCommand; i++) {
        builder.append(' ');
      }
      builder.append("  ").append(command.description).append(NL);
    }
  }

  @Override
  public CommandLine parseArgs(String[] args) {
    try {
      // Stop parsing at first non-option, so we don't accidently try
      // to interperet options intended for the commands themselves.
      commandLine = new PosixParser().parse(getOptions(), args, true);
      return commandLine;
    } catch (ParseException pe) {
      printUsageAndExit(-1);
    }
    return null;
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    String[] args = commandLine.getArgs();
    if (args.length > 0) {
      Command command = commands.get(args[0].toLowerCase());
      if (command != null) {
        AbstractCommandLineApp app = command.appClass.newInstance();
        app.run(app.parseArgs(shift(originalArgs)));
        return;
      }
      printUsageAndExit(-1);
    }
    if (commandLine.hasOption(HELP_OPTION.getLongOpt())) {
      printUsageAndExit(0);
    }

    // The default behavior is to display the product version.
    printVersion();
  }

  /**
   * Returns a subarray of the supplied array.  This performs the equivalent of
   * the 'shift' shell command.
   *
   * @param args An array of String arguments
   * @return args[1..n] subarray
   */
  private static String[] shift(String[] args) {
    String[] shifted = new String[args.length - 1];
    System.arraycopy(args, 1, shifted, 0, shifted.length);
    return shifted;
  }

  /**
   * Proper main() in case this is called directly.
   */
  public static void main(String[] args) throws Exception {
    CommandDispatcher app = new CommandDispatcher(args);
    app.run(app.parseArgs(args));
    System.exit(0);
  }
}
