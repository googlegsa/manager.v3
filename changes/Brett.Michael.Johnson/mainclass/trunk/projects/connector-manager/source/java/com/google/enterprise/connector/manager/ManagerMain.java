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
import com.google.enterprise.connector.servlet.ServletUtil;

/**
 * Dump the Version info from the Manifest for the Connector Manager's JAR file.
 * This is set as the default main() for the JAR if running the jar stand-alone.
 * Simply run the command:
 *   java -jar /path/to/connector.jar
 */
public class ManagerMain {
  public static void main(String[] args) throws Exception {
    System.out.println(ServletUtil.MANAGER_NAME + " "
                       + JarUtils.getJarVersion(ManagerMain.class));
  }
}

