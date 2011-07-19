// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

public class UsageException extends Exception {
  private UsageException(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  static final UsageException newCommandLineArgumentMissingDashes(String argument){
    return new UsageException("Command line argument missing '--' ("
        + argument + ")", null);
  }
  static final UsageException newCommandLineArgumentMissingEquals(String argument){
    return new UsageException("Command line argument missing '=' (" + argument
        + ")", null);
  }
  static final UsageException newUnsupportedCommandLineArgument(String argument){
    return new UsageException("Unknown command line argument (" + argument
        + ")", null);
  }
  static final UsageException newInvalidCommandLineArgumentValue(String argument){
    return new UsageException("Invalid command line argument value (" + argument
        + ")", null);
  }
  static final UsageException newUnsupportedProperty(String name){
    return new UsageException("UnsupportedProperty(" + name
        + ")", null);
  }
  static final UsageException newInvalidPropertyValue(String name, String value){
    return new UsageException("Invalid property value(" + name + "=" + value + ")", null);
  }
  static final UsageException newMissingRequiredValue(String name){
    return new UsageException("Required configuration value not specified (" + name + ")", null);
  }
}
