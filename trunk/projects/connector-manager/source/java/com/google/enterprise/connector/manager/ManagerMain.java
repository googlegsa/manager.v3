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

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.servlet.ServletUtil;

/**
 * The Connector Manager Main entry point.  This is the entry point
 * for command line access to Connector Manager features.
 * <p>
 * This is set as the default {@code main()} for the JAR if running the
 * jar stand-alone. Simply run the command:
 * <pre>
 *   java -jar /path/to/connector.jar
 * </pre>
 * By default, if ManagerMain is invoked with no command line argumants,
 * it displays to the console the Version info from the Manifest for the
 * Connector Manager's JAR file.
 * <p>
 * If invoked with command line arguments, control is turned over to the
 * {@link CommandDispatcher}.
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
public class ManagerMain {
  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      // If command line arguments are supplied pass them off to the command
      // dispatcher.
      CommandDispatcher.main(args);
    } else {
      // The default behavior is to display the product version.
      System.out.println(ServletUtil.MANAGER_NAME + " v"
                         + JarUtils.getJarVersion(ManagerMain.class));
    }
  }
}
