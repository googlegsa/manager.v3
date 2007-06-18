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

import java.io.InputStream;
import java.util.Calendar;

/**
 * A single, typed data item.  The
 * type can be obtained through the getType() call. Depending on the type, the
 * appropriately typed getter should be called, as follows:
 * <ul>
 * <li> ValueType.STRING - getString() or getStream()
 * <li> ValueType.BINARY - getStream()
 * <li> ValueType.LONG - getLong()
 * <li> ValueType.DOUBLE - getDouble()
 * <li> ValueType.DATE - getDate()
 * <li> ValueType.BOOLEAN - getBoolean()
 * </ul>
 * If the type of the getter does not match the ValueType, then the
 * implementation will attempt a conversion. If conversion is impossible, an
 * IllegalArgumentException is thrown.
 */
public interface Value {

  /**
   * Get the value as a String.  If the type is not ValueType.STRING, 
   * then conversion is attempted.
   * @return The value as a String
   * @throws IllegalArgumentException If conversion is impossible
   * @throws RepositoryException
   */
  public String getString() throws IllegalArgumentException,
      RepositoryException;

  /**
   * Get the value as a Stream.  This is expected to be used with values
   * of type ValueType.BINARY (particularly, for content).  It will also 
   * work naturally for ValueType.STRING.  Conversion is attempted for 
   * other types.
   * @return The value as a Stream
   * @throws IllegalArgumentException If conversion is impossible
   * @throws IllegalStateException If getStream has already been called
   * @throws RepositoryException
   */
  public InputStream getStream() throws IllegalArgumentException,
    IllegalStateException, RepositoryException;

  /**
   * Get the value as a long.  If the type is not ValueType.LONG, 
   * then conversion is attempted.
   * @return The value as a long
   * @throws IllegalArgumentException If conversion is impossible
   * @throws RepositoryException
   */
  public long getLong() throws IllegalArgumentException,
      RepositoryException;

  /**
   * Get the value as a double.  If the type is not ValueType.DOUBLE, 
   * then conversion is attempted.
   * @return The value as a double
   * @throws IllegalArgumentException If conversion is impossible
   * @throws RepositoryException
   */
  public double getDouble() throws IllegalArgumentException,
      RepositoryException;

  /**
   * Get the value as a Calendar.  If the type is not ValueType.DATE, 
   * then conversion is attempted.
   * @return The value as a Calendar
   * @throws IllegalArgumentException If conversion is impossible
   * @throws RepositoryException
   */
  public Calendar getDate() throws IllegalArgumentException,
      RepositoryException;

  /**
   * Get the value as a boolean.  If the type is not ValueType.BOOLEAN, 
   * then conversion is attempted.
   * @return The value as a boolean
   * @throws IllegalArgumentException If conversion is impossible
   * @throws RepositoryException
   */
  public boolean getBoolean() throws IllegalArgumentException,
      RepositoryException;

  /**
   * Gets the property's type
   * @return The appropriate ValueType
   * @throws RepositoryException
   */
  public ValueType getType() throws RepositoryException;
}