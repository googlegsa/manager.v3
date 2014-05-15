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

import junit.framework.TestCase;

import net.jmatrix.eproperties.EProperties;

import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * Tests for DatabasePropertyResourceBundle.
 */
public class DatabasePropertyResourceBundleTest extends TestCase {

  public void testEPropertyConstructor() {
    EProperties props = new EProperties();
    props.setProperty("test.property", "test");

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    assertEquals("test", bundle.getString("test.property"));
  }

  public void testPropertyConstructor() {
    Properties props = new Properties();
    props.setProperty("test.property", "test");

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    assertEquals("test", bundle.getString("test.property"));
  }

  public void testGetString() {
    EProperties props = new EProperties();
    props.setProperty("test.property", "test");

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    assertEquals("test", bundle.getString("test.property"));

    assertNull(bundle.getString("bogus"));
  }

  public void testGetStringAsArray() {
    EProperties props = new EProperties();
    props.setProperty("test.property", "test");

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    String[] values =  bundle.getStringArray("test.property");
    assertEquals(1, values.length);
    assertEquals("test", values[0]);
  }

  public void testGetStringArray() throws Exception {
    String propString = "test.property=( \"Hello\" , \"World\" )\n";
    ByteArrayInputStream bais =
        new ByteArrayInputStream(propString.getBytes("ISO-8859-1"));

    EProperties props = new EProperties();
    props.load(bais);

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    String[] values =  bundle.getStringArray("test.property");
    assertEquals(2, values.length);
    assertEquals("Hello", values[0]);
    assertEquals("World", values[1]);

    assertNull(bundle.getStringArray("bogus"));
  }

  public void testGetStringArrayWithEmbeddedCommas() throws Exception {
    String propString =
        "test.property=( \"Hello, Bonjour, Buenos Dias\" , \"World\" )\n";
    ByteArrayInputStream bais =
        new ByteArrayInputStream(propString.getBytes("ISO-8859-1"));

    EProperties props = new EProperties();
    props.load(bais);

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    String[] values =  bundle.getStringArray("test.property");
    assertEquals(2, values.length);
    assertEquals("Hello, Bonjour, Buenos Dias", values[0]);
    assertEquals("World", values[1]);
  }

  public void testGetStringArrayWithEmbeddedParens() throws Exception {
    String propString = "test.property=( \"Hello (I Love You)\" ,"
      + "\"Won't you tell me your name?\" )\n";
    ByteArrayInputStream bais =
        new ByteArrayInputStream(propString.getBytes("ISO-8859-1"));

    EProperties props = new EProperties();
    props.load(bais);

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    String[] values =  bundle.getStringArray("test.property");
    assertEquals(2, values.length);
    assertEquals("Hello (I Love You)", values[0]);
    assertEquals("Won't you tell me your name?", values[1]);
  }

  public void testSubstitution() {
    EProperties props = new EProperties();
    props.setProperty("substitution.property", "Hello");
    props.setProperty("test.property", "${substitution.property} World");

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    assertEquals("Hello World", bundle.getString("test.property"));
  }

  public void testGetStringArrayWithSubstitutionsInQuotes() throws Exception {
    String propString = "hello=Hello\n" + "world=World\n"
        + "helloworld=( \"${hello}\", \"${world}\" )\n";
    ByteArrayInputStream bais =
        new ByteArrayInputStream(propString.getBytes("ISO-8859-1"));

    EProperties props = new EProperties();
    props.load(bais);

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    String[] values =  bundle.getStringArray("helloworld");
    assertEquals(2, values.length);
    assertEquals("Hello", values[0]);
    assertEquals("World", values[1]);
  }

  public void testSetGetParent() {
    EProperties props = new EProperties();
    props.setProperty("test.property", "test");

    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);
    assertEquals("test", bundle.getString("test.property"));
    assertNull(bundle.getString("parent.property"));

    EProperties parentProps = new EProperties();
    parentProps.setProperty("parent.property", "parent");
    DatabasePropertyResourceBundle parentBundle =
        new DatabasePropertyResourceBundle(parentProps);

    bundle.setParent(parentBundle);

    assertSame(parentBundle, bundle.getParent());
    assertEquals("parent", bundle.getString("parent.property"));
  }

  public void testParentSubstitution() {
    EProperties props = new EProperties();
    props.setProperty("test.property", "${substitution.property} World");
    DatabasePropertyResourceBundle bundle =
        new DatabasePropertyResourceBundle(props);

    EProperties parentProps = new EProperties();
    props.setProperty("substitution.property", "Hello");
    DatabasePropertyResourceBundle parentBundle =
        new DatabasePropertyResourceBundle(parentProps);

    bundle.setParent(parentBundle);
    assertEquals("Hello World", bundle.getString("test.property"));
  }
}
