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

import java.util.logging.Level;

/**
 * Stubbed out SLF4J Logger for the benefit of mime-util.
 * This implementation is a thin wrapper around java util Logging.
 */
public class LoggerImpl implements Logger {

  /** Map SLF4J logging levels to java.util.logging.Levels */
  private static final Level FATAL = Level.SEVERE;
  private static final Level ERROR = Level.WARNING;
  private static final Level WARN  = Level.WARNING;
  private static final Level INFO  = Level.INFO;
  private static final Level DEBUG = Level.FINER;
  private static final Level TRACE = Level.FINEST;

  private final java.util.logging.Logger logger;

  /** Constructor, wrapping supplied java util Logger. */
  public LoggerImpl(java.util.logging.Logger logger) {
    this.logger = logger;
  }

  /** Returns true if fatal logging is enabled. */
  public boolean isFatalEnabled() {
    return logger.isLoggable(FATAL);
  }

  /** Log a severe error message. */
  public void fatal(String message) {
    log(FATAL, message);
  }

  /** Log a severe error message with cause. */
  public void fatal(String message, Throwable t) {
    log(FATAL, message, t);
  }

  /** Returns true if error logging is enabled. */
  public boolean isErrorEnabled() {
    return logger.isLoggable(ERROR);
  }

  /** Log an error message. */
  public void error(String message) {
    log(ERROR, message);
  }

  /** Log an error message with cause. */
  public void error(String message, Throwable t) {
    log(ERROR, message, t);
  }

  /** Returns true if warn logging is enabled. */
  public boolean isWarnEnabled() {
    return logger.isLoggable(WARN);
  }

  /** Log a warning message. */
  public void warn(String message) {
    log(WARN, message);
  }

  /** Log an warning message with cause. */
  public void warn(String message, Throwable t) {
    log(WARN, message, t);
  }

  /** Returns true if debug logging is enabled. */
  public boolean isDebugEnabled() {
    return logger.isLoggable(DEBUG);
  }

  /** Log a debug message. */
  public void debug(String message) {
    log(DEBUG, message);
  }

  /** Log a debug message with cause. */
  public void debug(String message, Throwable t) {
    log(DEBUG, message, t);
  }

  /** Returns true if info logging is enabled. */
  public boolean isInfoEnabled() {
    return logger.isLoggable(INFO);
  }

  /** Log a info message. */
  public void info(String message) {
    log(INFO, message);
  }

  /** Log a info message with cause. */
  public void info(String message, Throwable t) {
    log(INFO, message, t);
  }

  /** Returns true if trace logging is enabled. */
  public boolean isTraceEnabled() {
    return logger.isLoggable(TRACE);
  }

  /** Log a trace message. */
  public void trace(String message) {
    log(TRACE, message);
  }

  /** Log a trace message with cause. */
  public void trace(String message, Throwable t) {
    log(TRACE, message, t);
  }

  /* The above logging methods should not call each other, and the
   * below log methods should not call each other.  This ensures that
   * the true caller of the logger is at stacktrace level 3.
   */
  private void log(Level level, String message) {
    StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
    logger.logp(level, caller.getClassName(), caller.getMethodName(),
                message);
  }

  private void log(Level level, String message, Throwable t) {
    StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
    logger.logp(level, caller.getClassName(), caller.getMethodName(),
                message, t);
  }
}
