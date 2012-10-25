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

package com.google.enterprise.connector.importexport;

import com.google.common.collect.ImmutableSortedSet;
import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.manager.Context;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * A utility to dump connector configuration to a file.
 *
 * <pre>
 * usage: DumpConnectors [-?] [-v] [-c connector_name] output_file
 *        -?, --help       Display this help.
 *        -v, --version    Display version.
 *        -l, --list       List available Connectors.
 *        -c, --connector  Connector to include.
 *        output_file      Destination file output.
 * </pre>
 */
public class DumpConnectors extends AbstractCommandLineApp {
  /** Retrieve the TypeMap from the Spring Context. */
  private TypeMap getTypeMap() {
    return (TypeMap) Context.getInstance().getRequiredBean(
        "TypeMap", TypeMap.class);
  }

  /** Retrieve the ExportConnectors from the Spring Context. */
  private ExportConnectors getExportConnectors() {
    return (ExportConnectors) Context.getInstance().getRequiredBean(
        "ExportConnectors", ExportConnectors.class);
  }

  @Override
  public String getName() {
    return "DumpConnectors";
  }

  @Override
  public String getDescription() {
    return "Dumps Connector configurations as XML to a file.";
  }

  @Override
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax() + "[-c connector] <output_file>";
  }

  @Override
  public Options getOptions() {
    Options options = super.getOptions();
    options.addOption("l", "list", false, "List available connectors.");
    Option o = new Option("c", "connector_name", true, "Connector to export.");
    o.setArgName("connector_name");
    options.addOption(o);
    return options;
  }

  @Override
  protected String getUsageFooter() {
    StringBuilder builder = new StringBuilder(NL);
    builder.append(getName());
    builder.append(" writes an XML representation of the configurations of ");
    builder.append("all connector instances to the specified output_file.");
    builder.append(NL).append(NL);
    builder.append("One or more connectors to migrate may be specified using ");
    builder.append("-c options.  If unspecified, all connectors are exported.");
    return builder.toString();
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    // Must specify output filename.
    String[] args = commandLine.getArgs();

    initStandAloneContext(false);
    // Since we did not start the Context, we need to init TypeMap.
    getTypeMap().init();

    try {
      // If user asks for a list of available Connectors, print it and exit.
      if (commandLine.hasOption("list")) {
        listConnectors();
        return;
      }

      // Determine which connectors to export.
      Collection<String> connectors = null;
      String[] connectorNames = commandLine.getOptionValues('c');
      if (connectorNames != null) {
        connectors = ImmutableSortedSet.copyOf(connectorNames);
      }

      // Must specify output file.
      if (args.length != 1) {
        printUsage();
        return;
      }

      // Write the connector configurations out to the specified file.
      PrintWriter out = new PrintWriter(new OutputStreamWriter(
          new FileOutputStream(args[0]), "UTF-8"));
      getExportConnectors().getConnectors(connectors).toXml(out, 0);
      out.close();
    } finally {
      shutdown();
    }
  }

  /**
   * Prints out a list of available Connectors.
   */
  private void listConnectors() {
    printVersion();
    System.out.println("Available Connectors:");
    for (ImportExportConnector connector : getExportConnectors().getConnectors()) {
      System.out.println("    " + connector.getName());
    }
    System.out.println("");
  }

  /**
   * Proper main() in case this is called directly.
   */
  public static void main(String[] args) throws Exception {
    DumpConnectors app = new DumpConnectors();
    app.run(app.parseArgs(args));
    System.exit(0);
  }
}
