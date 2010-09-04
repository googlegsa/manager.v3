// Copyright (C)2009 Google Inc.
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
import com.google.enterprise.connector.encryptpassword.EncryptPassword;
import com.google.enterprise.connector.importexport.DumpConnectors;
import com.google.enterprise.connector.persist.MigrateStore;
import com.google.enterprise.connector.servlet.ServletUtil;

/**
 * Dump the Version info from the Manifest for the Connector Manager's JAR file.
 * This is set as the default main() for the JAR if running the jar stand-alone.
 * Simply run the command:
 *   java -jar /path/to/connector.jar
 */
public class ManagerMain {

  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      // TODO: There should be a more general mechanism for invoking
      // command line apps.
      if ("EncryptPassword".equalsIgnoreCase(args[0])) {
        EncryptPassword.main(shift(args));
      } else if ("DumpConnectors".equalsIgnoreCase(args[0])) {
        DumpConnectors.main(shift(args));
      } else if ("MigrateStore".equalsIgnoreCase(args[0])) {
        MigrateStore.main(shift(args));
      }
    }
    // The default behavior is to display the product version.
    System.out.println(ServletUtil.MANAGER_NAME + " v"
                       + JarUtils.getJarVersion(ManagerMain.class));
  }

  /**
   * Returns a subarray of the supplied array.  This performs the equivelent of
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
}
