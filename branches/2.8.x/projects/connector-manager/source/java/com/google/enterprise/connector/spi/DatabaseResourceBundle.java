// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public interface DatabaseResourceBundle {
  /**
   * Gets a resource that is specific to the active database implementation.
   * This API is modeled on {@link ResourceBundle#getString(String)} and is
   * intended to be used in a
   * similar way. That is, it may return a String (typically, SQL) into which
   * parameter substitution may be done using
   * {@link MessageFormat#format(Object)} and similar techniques. Note: the
   * deployment of resource files is not described here. See external Connector
   * Developer documentation.
   * <p/>
   * The implementation will assure that the correct resource is returned for
   * this connector type and for the active database implementation. See the
   * Developer's guide for details on how implementors can supply resources to
   * the installation.
   * <p/>
   * If there is no resource defined for this key, {@code null} is returned
   * (unlike {@link ResourceBundle#getString(String)}, which throws an
   * exception).
   *
   * @param key as {@link ResourceBundle#getString(String)}
   * @return as {@link ResourceBundle#getString(String)}
   */
  public String getString(String key);

  /**
   * The same comments apply as {{@link #getString(String)}, only this
   * API corresponds to {@link ResourceBundle#getStringArray(String)}.
   * <p/>
   * If there is no resource defined for this key, an array of length zero is
   * returned (unlike {@link ResourceBundle#getString(String)}, which throws an
   * exception).
   *
   * @param key as {@link ResourceBundle#getStringArray(String)}
   * @return as {@link ResourceBundle#getStringArray(String)}
   */
  public String[] getStringArray(String key);
}
