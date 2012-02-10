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

import com.google.enterprise.connector.manager.Context;

import junit.framework.TestCase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Tests for DatabaseResourceBundleManager.
 */
public class DatabaseResourceBundleManagerTest extends TestCase {
  private static final String TEST_DIR =
      "testdata/contextTests/databaseResourceBundleTests/";

  private static final String APPLICATION_CONTEXT =
      TEST_DIR + "DatabaseResourceBundleManagerTest.xml";

  private static final String BASE_PATH =
      TEST_DIR.replace('/', '.');

  private DatabaseResourceBundleManager mgr;

  private ClassLoader classLoader;

  @Override
  protected void setUp() throws Exception {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT, TEST_DIR);
    mgr = new DatabaseResourceBundleManager();
    classLoader = new TestClassLoader();
  }

  public void testGetBundleNames() {
    List<String> names = mgr.getBundleNames("Test", "");
    assertEquals(1, names.size());
    assertEquals("Test", names.get(0));

    names = mgr.getBundleNames("Test", "_apple");
    assertEquals(2, names.size());
    assertEquals("Test", names.get(0));
    assertEquals("Test_apple", names.get(1));

    names = mgr.getBundleNames("Test", "_apple_banana_strawberry_rhubarb");
    assertEquals(5, names.size());
    assertEquals("Test", names.get(0));
    assertEquals("Test_apple", names.get(1));
    assertEquals("Test_apple_banana", names.get(2));
    assertEquals("Test_apple_banana_strawberry", names.get(3));
    assertEquals("Test_apple_banana_strawberry_rhubarb", names.get(4));

    // Don't get fooled by underscore characters embedded in the baseName.
    names = mgr.getBundleNames("BaseName_Underscore", "_apple_banana");
    assertEquals(3, names.size());
    assertEquals("BaseName_Underscore", names.get(0));
    assertEquals("BaseName_Underscore_apple", names.get(1));
    assertEquals("BaseName_Underscore_apple_banana", names.get(2));
  }

  public void testLoadNonExistentBundle() throws Exception {
    assertNull(mgr.loadBundle("NonExistentDatabasePropertyResourceBundle",
                              null, classLoader));
  }

  public void testLoadBundle() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.loadBundle(name, null, classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));
    assertNull(bundle.getParent());
  }

  public void testLoadBundles() throws Exception {
    String name = BASE_PATH + "BaseName";
    String ext = "_dbname_dbversion_dbvariant";
    mgr.loadBundles(mgr.getBundleNames(name, ext), classLoader);
    DatabasePropertyResourceBundle bundle = mgr.cache.get(name + ext);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion_dbvariant",
                 bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  public void testBestFallbackForNonExistentBundle() throws Exception {
    String name = BASE_PATH + "BaseName";
    String ext = "_dbname_dbversion_non_existent_variant";
    mgr.loadBundles(mgr.getBundleNames(name, ext), classLoader);
    DatabasePropertyResourceBundle bundle = mgr.cache.get(name + ext);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  public void testGetResourceBundle() {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, null, classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));
    assertNull(bundle.getParent());
  }

  public void testGetNonExistentResourceBundle() throws Exception {
    assertNull(mgr.getResourceBundle(
        "NonExistentDatabasePropertyResourceBundle", null, classLoader));
  }

  public void testGetResourceBundleWithExtension1() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname", classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  public void testGetResourceBundleWithExtension2() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname_dbversion", classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  public void testGetResourceBundleWithDbInfo3() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname_dbversion_dbvariant", classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion_dbvariant",
                 bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  public void testBestFallbackForNonExistentResourceBundle() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname_dbversion_non_existent_variant",
                              classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  public void testSubstitutionFromParentBundles() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname_dbversion_dbvariant", classLoader);
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion_dbvariant",
                 bundle.getString("bundle.name"));
    assertEquals("Hello", bundle.getString("hello.property"));
    assertEquals("Goodbye Cruel World",
                 bundle.getString("hello.world.property"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname_dbversion", bundle.getString("bundle.name"));
    assertEquals("Goodbye Earth", bundle.getString("hello.world.property"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName_dbname", bundle.getString("bundle.name"));
    assertEquals("Hello World", bundle.getString("hello.world.property"));

    bundle = bundle.getParent();
    assertNotNull(bundle);
    assertEquals("BaseName", bundle.getString("bundle.name"));
    assertEquals("Hello", bundle.getString("hello.property"));
    assertNull(bundle.getString("hello.world.property"));

    bundle = bundle.getParent();
    assertNull(bundle);
  }

  // Check that if I load a bundle, then ask for a parent bundle,
  // I get the one that was cached from loading the first bundle,
  // rather than loading it again.
  public void testCacheParentBundles() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname_dbversion_dbvariant", classLoader);
    assertNotNull(bundle);

    DatabasePropertyResourceBundle parent =
        mgr.getResourceBundle(name, "_dbname_dbversion", classLoader);
    assertNotNull(parent);
    assertSame(parent, bundle.getParent());

    bundle = parent;
    parent = mgr.getResourceBundle(name, "_dbname", classLoader);
    assertNotNull(parent);
    assertSame(parent, bundle.getParent());

    bundle = parent;
    parent = mgr.getResourceBundle(name, null, classLoader);
    assertNotNull(parent);
    assertSame(parent, bundle.getParent());
  }

  // Check that if I load a bundle, then load another bundle with a
  // common parent, I get the parent that was cached from loading the
  // first bundle, rather than loading it again.
  public void testCacheParentBundlesAlternateChild() throws Exception {
    String name = BASE_PATH + "BaseName";
    DatabasePropertyResourceBundle bundle =
        mgr.getResourceBundle(name, "_dbname_dbversion_dbvariant", classLoader);
    assertNotNull(bundle);

    DatabasePropertyResourceBundle parent =
        mgr.getResourceBundle(name, "_dbname_dbversion", classLoader);
    assertNotNull(parent);
    assertSame(parent, bundle.getParent());

    DatabasePropertyResourceBundle bundle2 =
        mgr.getResourceBundle(name, "_dbname_dbversion_dbvariant2", classLoader);
    assertNotNull(bundle);
    assertSame(parent, bundle2.getParent());
  }

  // A ClassLoader that looks for resources relative to the
  // current working directory.
  private class TestClassLoader extends ClassLoader {
    @Override
    public URL getResource(String name) {
      try {
        File file = new File(name);
        if (file.exists() && file.isFile()) {
          return file.toURI().toURL();
        }
      } catch (MalformedURLException e) {
        // Fall through and look on classpath.
      }
      return this.getClass().getClassLoader().getResource(name);
    }
  }
}
