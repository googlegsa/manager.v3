// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * An enumeration of the possible value types 
 * in a repository.  Note:
 * <ul>
 * <li> There is only one integer type: LONG
 * <li> There is only one floating point type: DOUBLE
 * <li> The BINARY or STRING types may be used for content
 * </ul>
 */
public class ValueType {
  int id;
  String tag;
  
  private static int count = 0;
  
  /**
   * The String type
   */
  public static final ValueType STRING = 
    new ValueType("String");
  
  /**
   * The Binary type.  Should be used for content
   * unless the content can be guaranteed to be a String
   */
  public static final ValueType BINARY = 
    new ValueType("Binary");
  
  /**
   * The Long type.  This is the only integer type
   */
  public static final ValueType LONG = 
    new ValueType("Long");
  
  /**
   * The Double type.  This is the only floating-point type
   */
  public static final ValueType DOUBLE = 
    new ValueType("Double");
  
  /**
   * The Date type.
   */
  public static final ValueType DATE = 
    new ValueType("Date");
  
  /**
   * The Boolean type
   */
  public static final ValueType BOOLEAN = 
    new ValueType("Boolean");
  
  private ValueType(String t) {
    id = count;
    count++;
    tag = t;
  }
}
