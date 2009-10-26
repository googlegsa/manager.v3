// Copyright 2009 Google Inc.
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

import junit.framework.TestCase;

import java.util.Map;

/**
 * Tests for the {@link SimpleConnectorFactory} class.
 */
public class SimpleConnectorFactoryTest extends TestCase {
  private static class MockConnector implements Connector {
    public Session login() {
      return null;
    }
  }

  private static class BadConnectorFactory extends SimpleConnectorFactory {
    /** Use the no-args super constructor. */
    BadConnectorFactory() {
    }
  }

  private static class GoodConnectorFactory extends SimpleConnectorFactory {
    /** Use the no-args super constructor. */
    GoodConnectorFactory() {
    }

    @Override
    public Connector makeConnector(Map<String, String> config) {
      return new MockConnector();
    }
  }

  public void testMakeConnector() throws RepositoryException {
    Connector instance = new MockConnector();
    ConnectorFactory factory = new SimpleConnectorFactory(instance);
    assertSame(instance, factory.makeConnector(null));
  }

  public void testBadFactorySubclass() throws RepositoryException {
    ConnectorFactory factory = new BadConnectorFactory();
    assertNull(factory.makeConnector(null));
  }

  public void testGoodFactorySubclass() throws RepositoryException {
    ConnectorFactory factory = new GoodConnectorFactory();
    Connector first = factory.makeConnector(null);
    assertNotNull(first);
    Connector second = factory.makeConnector(null);
    assertNotNull(second);
    assertNotSame(first, second);
  }
}
