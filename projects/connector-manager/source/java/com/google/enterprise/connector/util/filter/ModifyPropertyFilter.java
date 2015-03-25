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

package com.google.enterprise.connector.util.filter;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.BinaryValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A {@link Document} filter that alters the values of the specified
 * {@link Property Properties}.  The filter will scrutinize the
 * {@link Value Values} returned by the supplied {@link Property Properties}.
 * If the value (as a string) matches the regular expression {@code pattern},
 * then all matching regions of the value will be replaced with the
 * {@code replacement} string.
 * <p/>
 * If the {@code overwrite} flag is {@code true}, the modified
 * property values replace any matching values of the target property.
 * Otherwise, the modified property values supplement any existing values
 * of the target property.
 * <p/>
 * <b>Example {@code documentFilters.xml} Configurations:</b>
 * <p/>
 * The following example replaces all instances of the word "Foo" with "Bar"
 * in the {@code Category} property.
 * <pre><code>
   &lt;bean id="FooToBar"
       class="com.google.enterprise.connector.util.filter.ModifyPropertyFilter"&gt;
     &lt;property name="propertyName" value="Category"/&gt;
     &lt;property name="pattern" value="Foo"/&gt;
     &lt;property name="replacement" value="Bar"/&gt;
     &lt;property name="overwrite" value="true"/&gt;
   &lt;/bean&gt;
   </code></pre>
 * The following example adds "Paul Erd&ouml;s" to the list of {@code Authors}
 * of documents for which I am also an author. This will give me an Erd&ouml;s
 * Number of 1!
 * <pre><code>
   &lt;!-- Add Erd&ouml;s as co-author of all my documents. --&gt;
   &lt;bean id="AddErdosAuthor"
       class="com.google.enterprise.connector.util.filter.AddPropertyFilter"&gt;
     &lt;property name="propertyName" value="Author"/&gt;
     &lt;property name="pattern" value="C'est Moi"/&gt;
     &lt;property name="replacement" value="Paul Erd&ouml;s"/&gt;
     &lt;property name="overwrite" value="false"/&gt;
   &lt;/bean&gt;
   </code></pre>
 * The following example replaces one or more instances of the characters
 * '.' or '_' with a single space for all values of the {@code Foo} and
 * {@code Bar} properties.  The original values are kept, and new values
 * with whitespace delimiters are added to the properties.
 * <pre><code>
   &lt;!-- Replace '.' and '_' with a space. --&gt;
   &lt;bean id="DotUnderscoreToWhiteSpace"
      class="com.google.enterprise.connector.util.filter.ModifyPropertyFilter"&gt;
     &lt;property name="propertyNames"&gt;
       &lt;set&gt;
         &lt;value&gt;Foo&lt;/value&gt;
         &lt;value&gt;Bar&lt;/value&gt;
       &lt;/set&gt;
     &lt;/property&gt;
     &lt;property name="pattern" value="[_.]+"/&gt;
     &lt;property name="replacement" value=" "/&gt;
     &lt;property name="overwrite" value="false"/&gt;
   &lt;/bean&gt;
   </code></pre>
 * <p>
 * When used with binary values, the entire value is buffered and the
 * modified value is stored in a {@code byte} array.
 *
 * @since 2.8
 */
/*
 * TODO: Find a way to process the InputStreams without buffering them,
 * maybe using java.util.Scanner or a similar third-party tool.
 * TODO: Binary values based on byte arrays are likely rare outside
 * the tests, but it might be nice to build the string from the
 * underlying byte array directly, rather than copying it.
 */
public class ModifyPropertyFilter extends AbstractDocumentFilter {

  /** The logger for this class. */
  private static final Logger LOGGER =
      Logger.getLogger(ModifyPropertyFilter.class.getName());

  /** The names of the Properties to filter. */
  protected Set<String> propertyNames;

  /** The names of the mimetypes to filter. */
  protected Set<String> mimeTypes;

  /** The name of the encoding type used to convert binary data to string */
  protected String encoding = "UTF-8";

  /** The regex pattern to match in the property {@link Value Values}. */
  protected Pattern pattern;

  /** The replacement string for matching regions in the values. */
  protected String replacement = "";

  /**
   * If {@code true}, overwrite the matching property values; otherwise supply
   * the modified value as an additional Value (like multi-valued Properties).
   */
  protected boolean overwrite = false;

  /**
   * Sets the the name of the {@link Property} to filter.
   * <p/>
   * A convenience method that is equivalent to calling
   * {@code setPropertyNames(Collections.singleton(propertyName)}.
   *
   * @param propertyName the name of the {@link Property} to filter
   * @throws IllegalArgumentException if {@code propertyName} is {@code null}
   *         or empty
   */
  public void setPropertyName(String propertyName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName),
                                "propertyName may not be null or empty");
    this.propertyNames = Collections.singleton(propertyName);
  }

  /**
   * Sets the the names of the {@link Property Properties} to filter.
   *
   * @param propertyNames a {@code Set} of names of the
   *        {@link Property Properties} to filter
   * @throws NullPointerException if {@code propertyNames} is {@code null}
   */
  public void setPropertyNames(Set<String> propertyNames) {
    Preconditions.checkNotNull(propertyNames, "propertyNames may not be null");
    this.propertyNames = propertyNames;
  }

  /**
   * Sets the media types of the {@link Document} objects to modify.
   *
   * @param mimeTypes a {@code Set} of names of the media types to filter
   * @throws NullPointerException if {@code mimeTypes} is {@code null}
   */
  public void setMimeTypes(Set<String> mimeTypes) {
    Preconditions.checkNotNull(mimeTypes, "mimeTypes may not be null");
    this.mimeTypes = mimeTypes;
  }

  /**
   * Sets the media types of the {@link Document} objects to modify.
   *
   * @param mimeType the name of the media type to filter
   * @throws NullPointerException if {@code mimeType} is {@code null}
   */
  public void setMimeType(String mimeType) {
    Preconditions.checkNotNull(mimeType, "mimeType may not be null");
    this.mimeTypes = Collections.singleton(mimeType);
  }

  /**
   * Sets the regular expression pattern to match in the values.
   * The supplied {@code pattern} must conform to the syntax defined in
   * <a href="http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html">
   * {@code java.util.regex.Pattern}</a>.
   *
   * @param pattern the regular expression pattern to match in the values
   * @throws PatternSyntaxException if {@code pattern}'s syntax is invalid
   * @throws IllegalArgumentException if {@code pattern} is {@code
   * null} or empty
   */
  public void setPattern(String pattern) throws PatternSyntaxException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(pattern),
                                "pattern may not be null or empty");
    this.pattern = Pattern.compile(pattern);
  }

  /**
   * Sets the replacement string for matching regions in the values.
   * The {@code replacement} string may refer to
   * <a href="http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#cg">
   * capturing groups</a> from the {@code pattern} as {@code $1, $2}, etc.
   * Therefore, literal instances of {@code '\'} and {@code '$'} in the
   * replacement string need to be properly
   * <a href="http://docs.oracle.com/javase/6/docs/api/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuffer,%20java.lang.String)">
   * escaped</a>.
   *
   * @param replacement the replacement String for matching regions in the
   *        values
   */
  public void setReplacement(String replacement) {
    this.replacement = Strings.nullToEmpty(replacement);
  }

  /**
   * Sets the {@code overwrite} values flag. If {@code true}, matching values
   * are overwritten with the modified value.  If {@code false}, matching
   * values are augmented by adding an additional modified value.
   * Default {@code overwrite} is {@code false}.
   *
   * @param overwrite the overwrite flag
   */
  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  /**
   * Sets the the name of the character encoding type to be used.
   *
   * @param encoding name of encoding type
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Finds a {@link Property} by {@code name}. If the {@code source}
   * {@link Document} has a property of that name, then that property
   * is returned.
   * <p/>
   * If any of the Property's values (as a string) match the regular
   * expression {@code pattern}, then all matching regions of the value
   * will be replaced with the {@code replacement} string.
   * <p/>
   * The modified value may either augment or overwrite the original value,
   * based upon the {@code overwrite} flag.
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(propertyNames != null, "must set propertyNames");
    Preconditions.checkState(pattern != null, "must set pattern");

    if (!propertyNames.contains(name)) {
      // Not a property of interest. Just fetch it from the source.
      return source.findProperty(name);
    }

    // For properties of interest, fetch the values and examine them.
    // If a value matches the pattern, either replace or augment that value.
    LinkedList<Value> values = new LinkedList<Value>();
    for (Value value : super.getPropertyValues(source, name)) {
      String original = null;
      String modified = null;
      Value originalValue = null;
      Value modifiedValue = null;
      if (value instanceof BinaryValue) {
        String mimeType =
            Value.getSingleValueString(source, SpiConstants.PROPNAME_MIMETYPE);
        if (Strings.isNullOrEmpty(mimeType)) {
          // There is no mimetype property in the document.
          return source.findProperty(name);
        }
        if (mimeType.contains(";")) {
          mimeType = mimeType.substring(0, mimeType.indexOf(";"));
        }
        // Initializing with default set
        if (mimeTypes == null) {
          mimeTypes = initDefaultMimeTypes();
        }
        // TODO(kiran) should allow match top-level
        // (e.g. "text/xml" matches "text")
        if (!mimeTypes.contains(mimeType)) {
          return source.findProperty(name);
        }
        // It's a Binary Value, to be read using input stream
        InputStream in = ((BinaryValue) value).getInputStream();
        byte[] data = null;
        try {
          data = ByteStreams.toByteArray(in);
        } catch (IOException e) {
          throw new RepositoryException("Error while reading the source", e);
        }
        originalValue = Value.getBinaryValue(data);
        try {
          original = new String(data, encoding);
        } catch (UnsupportedEncodingException e) {
          throw new RepositoryException("Error while converting"
              + " data with " + encoding, e);
        }
        modified = pattern.matcher(original).replaceAll(replacement);
        try {
          modifiedValue = Value.getBinaryValue(modified.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
          throw new RepositoryException("Error while converting"
              + " data with " + encoding, e);
        }
      } else {
        original = Strings.nullToEmpty(value.toString());
        originalValue = value;
        modified = pattern.matcher(original).replaceAll(replacement);
        modifiedValue = Value.getStringValue(modified);
      }
      if (original.equals(modified)) {
        values.add(originalValue);
      } else if (overwrite) {
        values.add(modifiedValue);
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Property Filter replaced " + name + " value "
                        + "\"" + originalValue
                        + "\" with \"" + modifiedValue + "\"");
        }
      } else {
        values.add(originalValue);
        values.add(modifiedValue);
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Property Filter injected " + name
                        + " value \"" + modifiedValue + "\"");
        }
      }
    }
    return new SimpleProperty(values);
  }

  private Set<String> initDefaultMimeTypes() {
    Set<String> mimeTypes = new TreeSet<String>();
    mimeTypes.add("text/xml");
    mimeTypes.add("text/xhtml");
    mimeTypes.add("text/tab-separated-values");
    mimeTypes.add("text/x-sgml");
    mimeTypes.add("text/calendar");
    mimeTypes.add("text/csv");
    mimeTypes.add("text/plain");
    mimeTypes.add("text/html");
    mimeTypes.add("text/sgml");
    mimeTypes.add("application/plain");
    mimeTypes.add("application/rdf+xml");
    mimeTypes.add("application/xhtml+xml");
    mimeTypes.add("application/xml");
    mimeTypes.add("message/http");
    mimeTypes.add("message/s-http");
    return mimeTypes;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(super.toString());
    buf.append(": (");
    buf.append(propertyNames);
    buf.append(" , \"");
    buf.append(pattern.pattern());
    buf.append("\" , \"");
    buf.append(replacement);
    buf.append("\" , ");
    buf.append(overwrite);
    buf.append(" , \"");
    buf.append(encoding);
    buf.append("\" , ");
    buf.append(mimeTypes);
    buf.append(")");
    return buf.toString();
  }
}
