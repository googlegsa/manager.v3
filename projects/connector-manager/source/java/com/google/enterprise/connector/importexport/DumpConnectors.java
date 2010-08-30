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

import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.servlet.SAXParseErrorHandler;
import com.google.enterprise.connector.servlet.ServletUtil;

import org.apache.commons.cli.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

/**
 * A utility to dump connector configuration to a file.
 *
 * usage: DumpConnectors [-?] [-v] output_file
 *        -?, --help      Display this help.
 *        -v, --version   Display version.
 *        output_file     Destination file output.
 */
public class DumpConnectors extends AbstractCommandLineApp {
  private static final Logger LOGGER =
      Logger.getLogger(DumpConnectors.class.getName());

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
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax() + " output_file";
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    // Must specify output filename.
    String[] args = commandLine.getArgs();
    if (args.length != 1) {
      printUsageAndExit(-1);
    }

    initStandAloneContext(false);
    // Since we did not start the Context, we need to init TypeMap.
    getTypeMap().init();

    try {
      // Write the connector configurations out to the specified file.
      PrintWriter out = new PrintWriter(new OutputStreamWriter(
          new FileOutputStream(args[0]), "UTF-8"));
      getExportConnectors().getConnectors().toXml(out, 0);
      out.close();
    } finally {
      shutdown();
    }
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
