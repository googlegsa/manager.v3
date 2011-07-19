// Copyright 2011 Google Inc.
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

package org.slf4j;

/**
 * Stubbed out SLF4J Logger for the benefit of mime-util.
 */
public interface Logger {
  /** Returns true if fatal logging is enabled. */
  public boolean isFatalEnabled();

  /** Log a severe error message. */
  public void fatal(String message);

  /** Log a severe error message with cause. */
  public void fatal(String message, Throwable t);

  /** Returns true if error logging is enabled. */
  public boolean isErrorEnabled();

  /** Log an error message. */
  public void error(String message);

  /** Log an error message with cause. */
  public void error(String message, Throwable t);

  /** Returns true if warn logging is enabled. */
  public boolean isWarnEnabled();

  /** Log a warning message. */
  public void warn(String message);

  /** Log an warning message with cause. */
  public void warn(String message, Throwable t);

  /** Returns true if debug logging is enabled. */
  public boolean isDebugEnabled();

  /** Log a debug message. */
  public void debug(String message);

  /** Log a debug message with cause. */
  public void debug(String message, Throwable t);

  /** Returns true if info logging is enabled. */
  public boolean isInfoEnabled();

  /** Log a info message. */
  public void info(String message);

  /** Log a info message with cause. */
  public void info(String message, Throwable t);

  /** Returns true if trace logging is enabled. */
  public boolean isTraceEnabled();

  /** Log a trace message. */
  public void trace(String message);

  /** Log a trace message with cause. */
  public void trace(String message, Throwable t);
}
