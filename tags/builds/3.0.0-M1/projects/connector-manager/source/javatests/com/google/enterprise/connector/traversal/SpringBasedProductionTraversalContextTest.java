// Copyright (C) 2007 Google Inc.
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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.TraversalContext;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Tests that use Spring to test the ProductionTraversalContext class.
 */
public class SpringBasedProductionTraversalContextTest extends TestCase {
  public final void testSpringBased() {
    ApplicationContext applicationContext;
    applicationContext = new FileSystemXmlApplicationContext(
        "testdata/traversalContextData/applicationContext.xml");
    TraversalContext traversalContext = (TraversalContext) applicationContext
        .getBean("TraversalContext");
    Assert.assertNotNull(traversalContext);
    Assert.assertEquals(2500000, traversalContext.maxDocumentSize());
    Assert.assertTrue(traversalContext.mimeTypeSupportLevel("text/plain") <= 0);
    Assert.assertTrue(traversalContext.mimeTypeSupportLevel("text/notplain")
                      <= 0);
    Assert.assertEquals(1, traversalContext.mimeTypeSupportLevel("ibblefrix"));
  }

  public final void testContextBasedTraversalContext() {
    Context.refresh();
    Context.getInstance().setStandaloneContext(
        Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    Context.getInstance().setFeeding(false);
    Context.getInstance().start();
    TraversalContext traversalContext = Context.getInstance()
        .getTraversalContext();
    Assert.assertNotNull(traversalContext);
    Context.getInstance().shutdown(true);
    Context.refresh();
  }
}
