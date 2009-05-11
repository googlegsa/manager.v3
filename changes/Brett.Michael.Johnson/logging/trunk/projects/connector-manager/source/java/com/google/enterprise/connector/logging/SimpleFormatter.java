// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A log formatter resembling java.util.logging.SimpleFormatter,
 * adding MDC and NDC Logging capabilities.
 */
public class SimpleFormatter extends java.util.logging.SimpleFormatter {
  public final String DEBUGGING_PATTERN = "%d{MMM dd, yyyy h:mm:ss a} [%T %t - %x] %C{3}.%M()%n%p: %m%n";
  public final String DEFAULT_PATTERN = "%d{MMM dd, yyyy h:mm:ss a} [%x] %C %M%n%p: %m%n";
  public final String ONE_LINE_PATTERN = "%d [%x] %p: %C{3}.%M(): %m%n";

  // The default layout pattern resembles java.util.logging.SimpleFormatter,
  // with an addtional NDC component.
  private LayoutPattern layout;

  public SimpleFormatter() {
    // load the format from logging.properties
    String propName = getClass().getName() + ".format";
    String format = LogManager.getLogManager().getProperty(propName);
    if (format == null || format.trim().length() == 0) {
      format = DEFAULT_PATTERN;
    }
    layout = new LayoutPattern(format, new java.util.logging.SimpleFormatter());
  }

  @Override
  public String format(LogRecord record) {
    String output = layout.format(record);
    Throwable thrown = record.getThrown();
    if (thrown != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      thrown.printStackTrace(ps);
      ps.flush();
      output += baos.toString();
      ps.close();
    }
    return output;
  }
}