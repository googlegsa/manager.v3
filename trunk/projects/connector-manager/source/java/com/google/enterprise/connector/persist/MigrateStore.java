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

package com.google.enterprise.connector.persist;

import com.google.common.collect.ImmutableMap;

import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.manager.Context;

import org.apache.commons.cli.*;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility to migrate connector data from one PersistentStore
 * to another.
 *
 * usage: MigrateStore [-?] [-v] [-l] [source_name] [dest_name]
 *        -?, --help      Display this help.
 *        -v, --version   Display version.
 *        -l, --list      List available PersistentStores.
 *        -f, --force     Overwrite existing data in destination PersistentStore.
 *        source_name     Name of source PeristentStore (e.g. FilePersistentStore)
 *        dest_name       Name of destination PeristentStore (e.g. JdbcPersistentStore)
 */
public class MigrateStore extends AbstractCommandLineApp {
  private static final Logger LOGGER =
      Logger.getLogger(MigrateStore.class.getName());

  /** Retrieve the TypeMap from the Spring Context. */
  private TypeMap getTypeMap() {
    return (TypeMap) Context.getInstance().getRequiredBean(
        "TypeMap", TypeMap.class);
  }

  @Override
  public String getName() {
    return "MigrateStore";
  }

  @Override
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax() + "[-l] [-f] [source_name] [dest_name]";
  }

  @Override
  public Options getOptions() {
    Options options = super.getOptions();
    options.addOption("l", "list", false, "List available PersistentStores.");
    options.addOption("f", "force", false, "Overwrite existing data in destination PersistentStore.");
    return options;
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    initStandAloneContext(false);
    // Since we did not start the Context, we need to init TypeMap
    // for PersistentStores to function correctly.
    getTypeMap().init();

    try {
      // If user asks for a list of available PersitentStores,
      // print it and exit.
      if (commandLine.hasOption("list")) {
        listStores();
        return;
      }

      // Get then names of the source and destination PersitentStores.
      String sourceName = null;
      String destName = null;
      String[] args = commandLine.getArgs();
      if ((args.length == 1) || (args.length > 2)) {
        printUsageAndExit(-1);
      }
      if (args.length == 2) {
        sourceName = args[0];
        destName = args[1];
      } else {
        sourceName = selectStoreName("source");
        if (sourceName == null) {
          return;
        }
        destName = selectStoreName("destination");
        if (destName == null) {
          return;
        }
      }

      if (sourceName.equals(destName)) {
        System.err.println(
            "Source and destination PersistentStores must be different.");
        return;
      }

      // Actually perform the migration.
      PersistentStore sourceStore = getPersistentStoreByName(sourceName);
      PersistentStore destStore = getPersistentStoreByName(destName);
      if (sourceStore != null && destStore != null) {
        // Adjust the logging levels so that StoreMigrator messages are logged
        // to the Console.
        Logger.getLogger(StoreMigrator.class.getName()).setLevel(Level.INFO);

        StoreMigrator.migrate(sourceStore, destStore,
            commandLine.hasOption("force"));
        StoreMigrator.checkMissing(destStore);
      }
    } finally {
      shutdown();
    }
  }

  /**
   * Prints out a list of available PersistentStores.
   */
  private void listStores() {
    printVersion();
    System.out.println("Available PersistentStores:");
    for (String name : getStoreNames()) {
      System.out.println("    " + name);
    }
    System.out.println("");
  }

  /**
   * Returns a storeName selected by the user.
   */
  private String selectStoreName(String which) {
    return pickMenu("Available PersistentStores:",
                    "Please select the " + which + " PersistentStore: ",
                    getStoreNames());
  }

  /**
   * Returns the named PersistentStore instance.
   */
  private PersistentStore getPersistentStoreByName(String name) {
    try {
      PersistentStore store = (PersistentStore) Context.getInstance()
          .getApplicationContext().getBean(name, PersistentStore.class);
      if (store == null) {
        System.err.println("No PersistentStore named " + name + " was found.");
      }
      return store;
    } catch (NoSuchBeanDefinitionException e) {
      System.err.println("No PersistentStore named " + name + " was found.");
    } catch (BeansException e) {
      System.err.println("Spring failure - can't instantiate " + name + ": ("
                         + e.toString() + ")");
    }
    return null;
  }

  /**
   * Returns a list of the names of configured PersistentStores.
   */
  private String[] getStoreNames() {
    return Context.getInstance().getApplicationContext()
        .getBeanNamesForType(PersistentStore.class);
  }

  /**
   * Prints a menu from a list of choices, asks the user to pick one.
   *
   * @param header Text to display above the list of choices.
   * @param prompt Text to display on the prompt line.
   * @param choices Available choices.
   * @return item chosen from list, or null if none was selected.
   */
  private String pickMenu(String header, String prompt, String[] choices) {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      do {
        System.out.println(header);
        for (int i = 0; i < choices.length; i++) {
          System.out.println("  " + (i + 1) + ")  " + choices[i]);
        }
        System.out.print(prompt);

        String answer = in.readLine();
        if (answer == null || answer.trim().length() == 0) {
          return null;
        }
        int choiceNum;
        try {
          choiceNum = Integer.parseInt(answer) - 1;
        } catch (NumberFormatException nfe) {
          choiceNum = -1; // Force chose again.
        }
        if (choiceNum >= 0 && choiceNum < choices.length) {
          return choices[choiceNum];
        }
        System.err.println("Invalid choice.\n");
      } while (true);
    } catch (IOException e) {
      System.err.println("Failed to read from input: " + e.getMessage());
    }
    return null;
  }

  /**
   * Proper main() in case this is called directly.
   */
  public static void main(String[] args) throws Exception {
    MigrateStore app = new MigrateStore();
    app.run(app.parseArgs(args));
    System.exit(0);
  }
}
