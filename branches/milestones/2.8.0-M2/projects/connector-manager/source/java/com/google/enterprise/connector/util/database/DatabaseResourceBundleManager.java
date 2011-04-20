// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.database;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link DatabasePropertyResourceBundle} manager.  Loads and caches
 * DatabasePropertyResourceBundles.
 */
public class DatabaseResourceBundleManager {
  private static final Logger LOGGER =
      Logger.getLogger(DatabaseResourceBundleManager.class.getName());

  @VisibleForTesting
  HashMap<String, DatabasePropertyResourceBundle> cache =
      new HashMap<String, DatabasePropertyResourceBundle>();

  /**
   * Gets a resource bundle using the specified {@code baseName}, and the
   * specified {@code resourceBundleExtension}.  The {@code baseName}
   * identifies the specific set of resources to load, while the
   * {@code resourceBundleExtension} is used to locate database-specific
   * localizations of those resources.
   * <p>
   * The passed base name and resource bundle extension are used to
   * create resource bundle names.  The first name is created by concatenating
   * the base name with the resource bundle extension (if provided).
   * From this name all parent bundle names are derived. This results in a
   * list of possible bundle names.
   * <p>
   * <strong>Example</strong> For the {@code baseName "BaseName"}, and the
   * {@code resourceBundleExtension "_mysql_5_1"} (for {@code MySQL 5.1}),
   * the list would look something like this:
   *
   * <ol>
   * <li>BaseName_mysql_5_1</li>
   * <li>BaseName_mysql_5</li>
   * <li>BaseName_mysql</li>
   * <li>BaseName</li>
   * </ol>
   *
   * This list also shows the order in which the bundles will be searched.
   * This implementation looks only for Properties file-backed
   * {@link DatabasePropertyResourceBundle}s. The properties files supported
   * by {@code DatabasePropertyResourceBundle} may be in the enhanced syntax
   * supported by {@link http://eproperties.googlecode.com EProperties}.
   * <p>
   * This method tries to load a {@code .properties} file with the names
   * by replacing dots in the base name with a slash and by appending
   * "{@code .properties}" to the end of the string. If such a resource can be
   * found by calling {@link ClassLoader#getResource(String)} it is used to
   * initialize a {@link DatabasePropertyResourceBundle}.  If this succeeds,
   * it will also load the parents of this resource bundle.
   *
   * @param baseName the base name of the resource bundle, a fully qualified
   *        class name.
   * @param resourceBundleExtension the database-specific localization of the
   *        resource bundle, or {@code null} if no localized bundle should
   *        be searched for.
   * @param classLoader the ClassLoader to use when locating the requested
   *        resource.  If {@code null}, then the default ClassLoader is used.
   *
   * @return a resource bundle for the given base name and the locale, or
   *         {@code null} if no resource bundle for the specified base name
   *         can be found.
   * @throws java.lang.NullPointerException if {@code baseName} is {@code null}
   */
  public synchronized DatabasePropertyResourceBundle getResourceBundle(
      String baseName, String resourceBundleExtension,
      ClassLoader classLoader) {
    Preconditions.checkNotNull(baseName);
    String extension = Strings.nullToEmpty(resourceBundleExtension);
    String key = getKey(baseName, extension);
    if (!cache.containsKey(key)) {
      List<String> bundleNames = getBundleNames(baseName, extension);
      loadBundles(bundleNames, getClassLoader(classLoader));
    }
    return cache.get(key);
  }

  /**
   * Generates a cache lookup key from the supplied {@code baseName} and
   * {@code databaseInfo}.
   */
  @VisibleForTesting
  String getKey(String baseName, String resourceBundleExtension) {
    return baseName + Strings.nullToEmpty(resourceBundleExtension);
  }

  /**
   * Returns the {@link ClassLoader} to use.
   */
  private ClassLoader getClassLoader(ClassLoader classLoader) {
    if (classLoader == null) {
      classLoader = this.getClass().getClassLoader();
      if (classLoader == null) {
        classLoader = ClassLoader.getSystemClassLoader();
      }
    }
    return classLoader;
  }

  /**
   * Builds a list of the bundle names to load, in load order, from
   * the supplied {@code baseName} and {@code resourceBundleExtension}.
   */
  @VisibleForTesting
  List<String> getBundleNames(String baseName, String resourceBundleExtension) {
    List<String> names = new ArrayList<String>();
    for (int i = 0; i <= resourceBundleExtension.length(); i++) {
      i = resourceBundleExtension.indexOf('_', i);
      if (i < 0) i = resourceBundleExtension.length();
      names.add(baseName + resourceBundleExtension.substring(0, i));
    }
    return names;
  }

  /**
   * Loads any bundles in the supplied list that are not already loaded,
   * caches them, and sets parent-child relationship.
   */
  @VisibleForTesting
  void loadBundles(List<String> bundleNames, ClassLoader classLoader) {
    DatabasePropertyResourceBundle parent = null;
    for (String name : bundleNames) {
      DatabasePropertyResourceBundle bundle = cache.get(name);
      if (bundle == null) {
        bundle = loadBundle(name, parent, classLoader);
        if (bundle == null) {
          bundle = parent;
        }
        cache.put(name, bundle);
      }
      parent = bundle;
    }
  }

  /**
   * Loads the specified resource bundle.
   *
   * @param bundleName the name of the resource bundle to load
   * @param parent the DatabasePropertyResourceBundle to assign as parent
   * @param classLoader the ClassLoader to use to locate the requested resource
   *
   * @return a DatabasePropertyResourceBundle or {@code null} if none was found
   */
  @VisibleForTesting
  DatabasePropertyResourceBundle loadBundle(String bundleName,
      DatabasePropertyResourceBundle parent, ClassLoader classLoader)  {
    String resourceName = bundleName.replace('.', '/') + ".properties";
    LOGGER.fine("Looking for SQL ResourceBundle " + resourceName + " ...");
    try {
      URL url = classLoader.getResource(resourceName);
      if (url != null) {
        LOGGER.config("Loading SQL ResourceBundle " + url);
        return new DatabasePropertyResourceBundle(url, parent);
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to load SQL ResourceBundle "
                 + bundleName, e);
    }
    return null;
  }
}
