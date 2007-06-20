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

import com.google.enterprise.connector.spi.TraversalContext;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringBasedProductionTraversalContextTest extends TestCase {
  public final void testSpringBased() {
    ApplicationContext applicationContext;
    applicationContext = new FileSystemXmlApplicationContext(
        "testdata/traversalContextData/applicationContext.xml");
    TraversalContext traversalContext = (TraversalContext) applicationContext
        .getBean("TraversalContext");
    Assert.assertNotNull(traversalContext);
    Assert.assertEquals(2500000, traversalContext.maxDocumentSize());
    Assert.assertEquals(0, traversalContext.mimeTypeSupportLevel("text/plain"));
    Assert.assertEquals(0, traversalContext.mimeTypeSupportLevel("text/plain"));    
    Assert.assertEquals(1, traversalContext.mimeTypeSupportLevel("ibblefrix"));    
  }
}
