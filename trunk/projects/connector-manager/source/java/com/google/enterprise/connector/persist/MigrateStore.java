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
import com.google.common.collect.ImmutableSortedSet;
import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.manager.Context;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility to migrate connector data from one PersistentStore
 * to another.
 *
 * <pre>
 * usage: MigrateStore [-?] [-v] [-c connector_name] [-l] [source_name] [dest_name]
 *        -?, --help        Display this help.
 *        -v, --version     Display version.
 *        -c, --connector   Connector(s) to migrage (default is all connectors).
 *        -l, --list        List available PersistentStores.
 *        -f, --force       Overwrite existing data in destination PersistentStore.
 *        source_name       Name of source PeristentStore (e.g. FilePersistentStore)
 *        dest_name         Name of destination PeristentStore (e.g. JdbcPersistentStore)
 * </pre>
 */
public class MigrateStore extends AbstractCommandLineApp {
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
  public String getDescription() {
    return "Migrates Connector configurations between Persistent Stores.";
  }

  @Override
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax()
        + "[-l] [-f] [-c connector] [source_name] [dest_name]";
  }

  @Override
  public Options getOptions() {
    Options options = super.getOptions();
    options.addOption("l", "list", false, "List available PersistentStores.");
    options.addOption("f", "force", false,
        "Overwrite existing data in destination PersistentStore.");
    options.addOption(OptionBuilder.withLongOpt("connector")
                      .hasArg()
                      .withArgName("connector_name")
                      .withDescription("Connector to migrate.")
                      .create('c'));
    return options;
  }

  @Override
  protected String getUsageFooter() {
    StringBuilder builder = new StringBuilder(NL);
    builder.append(getName());
    builder.append(" migrates connector configurations from the source ");
    builder.append("Persistent Store location to destination Persistent ");
    builder.append("Store location. This is useful when upgrading older ");
    builder.append("connector installations, when moving connector ");
    builder.append("deployments from test to production, or when moving ");
    builder.append("from the embedded database to a corporate database.");
    builder.append(NL).append(NL);
    builder.append("One or more connectors to migrate may be specified using ");
    builder.append("-c options.  If unspecified, all connectors are migrated.");
    builder.append(NL).append(NL);
    builder.append("If configuration data for a connector already exists ");
    builder.append("in the destination store, it will not be overwritten ");
    builder.append("unless forced to do so by using the --force option.");
    return builder.toString();
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
        Collection<String> storeNames = getStoreNames();
        sourceName = selectStoreName("source", storeNames);
        if (sourceName == null) {
          return;
        }
        storeNames.remove(sourceName);
        destName = selectStoreName("destination", storeNames);
        if (destName == null) {
          return;
        }
      }
      if (sourceName.equals(destName)) {
        System.err.println(
            "Source and destination PersistentStores must be different.");
        return;
      }

      PersistentStore sourceStore = getPersistentStoreByName(sourceName);

      // Determine which connectors to migrate.
      Collection<String> connectors = null;
      String[] connectorNames = commandLine.getOptionValues('c');
      if (connectorNames != null) {
        connectors = ImmutableSortedSet.copyOf(connectorNames);
      } else if (args.length != 2) {
        // If no connectors were specified on the command line, and we had
        // to prompt the user for the source and destination stores, then also
        // prompt the user for a connector to migrate.
        String name = selectConnectorName(getConnectorNames(sourceStore));
        if (name != null) {
          connectors = ImmutableSortedSet.of(name);
        }
      }

      // Actually perform the migration.
      PersistentStore destStore = getPersistentStoreByName(destName);
      if (sourceStore != null && destStore != null) {
        // Adjust the logging levels so that StoreMigrator messages are logged
        // to the Console.
        Logger.getLogger(StoreMigrator.class.getName()).setLevel(Level.INFO);
        StoreMigrator.migrate(sourceStore, destStore, connectors,
            commandLine.hasOption("force"));
        StoreMigrator.checkMissing(destStore, connectors);
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
  private String selectStoreName(String which, Collection<String> choices) {
    return pickMenu("Available PersistentStores:",
                    "Please select the " + which + " PersistentStore: ",
                    choices);
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
   * Returns a Collection of the names of configured, but not disabled,
   * PersistentStores.
   */
  @SuppressWarnings("unchecked")
  private Collection<String> getStoreNames() {
    TreeSet<String> names = new TreeSet<String>();
    Map<String, PersistentStore> stores = (Map<String, PersistentStore>)
        Context.getInstance().getApplicationContext()
        .getBeansOfType(PersistentStore.class);
    for (Map.Entry<String, PersistentStore> entry : stores.entrySet()) {
      // Only include PersistentStores that are not disabled.
      if (!entry.getValue().isDisabled()) {
        names.add(entry.getKey());
      }
    }
    return names;
  }

  /**
   * Returns a connector name to migrate selected by the user.
   */
  private String selectConnectorName(Collection<String> connectorNames) {
    return pickMenu("Available Connector Instances:",
                    "Please select Connectors to migrate [All]: ",
                    connectorNames);
  }

  /**
   * Returns a Collection of the names of configured PersistentStores.
   */
  private Collection<String> getConnectorNames(PersistentStore  sourceStore) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        sourceStore.getInventory();
    TreeSet<String> names = new TreeSet<String>();
    for (StoreContext context : inventory.keySet()) {
      names.add(context.getConnectorName());
    }
    return names;
  }

  /**
   * Prints a menu from a Collection of choices, asks the user to pick one.
   *
   * @param header Text to display above the list of choices.
   * @param prompt Text to display on the prompt line.
   * @param choices Available choices.
   * @return item chosen from the choices, or null if none was selected.
   */
  private String pickMenu(String header, String prompt,
                          Collection<String> choices) {
    try {
      String[] items = choices.toArray(new String[0]);
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      do {
        System.out.println("");
        System.out.println(header);
        for (int i = 0; i < items.length; i++) {
          System.out.println("  " + (i + 1) + ")  " + items[i]);
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
        if (choiceNum >= 0 && choiceNum < items.length) {
          return items[choiceNum];
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
