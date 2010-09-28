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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * Identifies a database implementation by product name and version.
 * This may be used to request {@link DatabaseResourceBundles} in much
 * the same way the {@code java.util.Locale} is used to specify which
 * {@code java.util.ResourceBundle} to load.
 */
public class DatabaseInfo {
  private final String EMPTY_STRING = "";
  private final String productName;
  private final String majorVersion;
  private final String minorVersion;
  private final String description;
  private final String resourceBundleExtension;

  /**
   * Constructs a {@code DatabaseInfo} with the supplied
   * identification strings, any or all of which may be {@code null}.
   *
   * @param productName the database product name
   * @param majorVersion the database major version
   * @param minorVersion the database minor version
   * @param description a descriptive string describing the database.
   */
  public DatabaseInfo(String productName, String majorVersion,
                      String minorVersion, String description) {
    Preconditions.checkArgument(isSanitized(productName),
                                "Illegal characters in " + productName);
    Preconditions.checkArgument(isSanitized(majorVersion),
                                "Illegal characters in " + majorVersion);
    Preconditions.checkArgument(isSanitized(minorVersion),
                                "Illegal characters in " + minorVersion);
    this.productName = productName;
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.description = description;
    this.resourceBundleExtension = bundleNameFragment(productName)
        + bundleNameFragment(majorVersion) + bundleNameFragment(minorVersion);
  }

  /**
   * Returns {@code true} if the supplied string is acceptable
   * as one of the parameters to the {@code DatabaseInfo} constructor
   * (other than {@code description}).  These strings are used to
   * construct a ResourceBundle extension used for locating
   * {@link DatabaseResourceBundle}s.
   */
  public static boolean isSanitized(String string) {
    return Strings.isNullOrEmpty(string) ? true :
           Pattern.matches("[a-z0-9_-]+", string);
  }

  /**
   * Sanitizes the supplied string for use as a portion of a
   * ResourceBundleExtension.  Specifically, a sanitized string
   * should consist of nothing other than lowercase alphabetics
   * [a-z], numerics [0-9], underscore '_', and hyphen '-'.
   * <p>
   * The following actions are taken sanitize the input string:
   * <ul><li>Alphabetics are converted to lowercase.</li>
   * <li>Leading and trailing invalid characters are removed.</li>
   * <li>The remaining invalid characters are converted to hyphens.</li>
   * </ul>
   * <p>
   * For example, the string "Hello World!" would sanitize to "hello-world".
   *
   * @param string the {@code String} to be sanitized.
   * @return sanitized version of the supplied {@code string}
   */
  public static String sanitize(String string) {
    if (string != null) {
      return string.toLowerCase().replaceAll("[^a-z0-9_]+", " ").trim()
             .replace(' ', '-');
    }
    return string;
  }

  /**
   * If the supplied string is not {@code null} or empty, prepend an underscore,
   * else return the empty string.
   */
  private String bundleNameFragment(String string) {
    return Strings.isNullOrEmpty(string) ? "" : "_" + string;
  }

  /** @return the {@code productName} */
  public String getProductName() {
    return productName;
  }

  /** @return the {@code majorVersion} */
  public String getMajorVersion() {
    return majorVersion;
  }

  /** @return the {@code minorVersion} */
  public String getMinorVersion() {
    return minorVersion;
  }

  /** @return the {@code description} string */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the {@link DatabaseResourceBundle} extension that may be added
   * to a {@code ResourceBundle baseName} to build the name of the resource
   * that is specific to the database implementation identified by this
   * {@code DatabaseInfo} instance.
   * <p>
   * This is the programmatic name of the {@code DatabaseInfo},
   * with the database {@code productName}, {@code majorVersion},
   * and {@code minorVersion} separated by underbars ({@code '_'}).
   *
   * @return the resource bundle extension
   */
  public String getResourceBundleExtension() {
    return resourceBundleExtension;
  }

  @Override
  public String toString() {
    return description;
  }
}
