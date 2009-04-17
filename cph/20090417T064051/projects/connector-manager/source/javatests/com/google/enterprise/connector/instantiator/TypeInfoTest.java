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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.instantiator.TypeInfo;
import com.google.enterprise.connector.instantiator.TypeInfo.BeanInstantiationFailureException;
import com.google.enterprise.connector.instantiator.TypeInfo.FactoryCreationFailureException;
import com.google.enterprise.connector.instantiator.TypeInfo.InstanceXmlMissingException;
import com.google.enterprise.connector.instantiator.TypeInfo.TypeInfoException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class TypeInfoTest extends TestCase {

  private static final Logger LOGGER =
      Logger.getLogger(TypeInfoTest.class.getName());

  /**
   * Test method for
   * {@link com.google.enterprise.connector.instantiator.TypeInfo
   * #fromSpringResourceAndThrow(org.springframework.core.io.Resource)}.
   */
  public final void testFromSpringResourcePositive() {
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = null;
    boolean exceptionThrown = false;
    try {
      typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    } catch (TypeInfoException e) {
      exceptionThrown = true;
    }
    Assert.assertFalse(exceptionThrown);
    Assert.assertNotNull(typeInfo);
  }

  public final void testFromSpringResourceNegative1() {
    // missing type resource causes FactoryCreationFailureException
    String resourceName =
        "testdata/connectorTypeTests/negative1/connectorType.xml";
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = null;
    boolean correctExceptionThrown = false;
    try {
      typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    } catch (FactoryCreationFailureException e) {
      LOGGER.log(Level.WARNING, "Factory Creation Failure", e);
      correctExceptionThrown = true;
    } catch (TypeInfoException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    }
    Assert.assertTrue(correctExceptionThrown);
    Assert.assertNull(typeInfo);
  }

  public final void testFromSpringResourceNegative2() {
    // malformed xml type resource also prompts FactoryCreationFailureException
    String resourceName =
        "testdata/connectorTypeTests/negative2/connectorType.xml";
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = null;
    boolean correctExceptionThrown = false;
    try {
      typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    } catch (FactoryCreationFailureException e) {
      LOGGER.log(Level.WARNING, "Factory Creation Failure", e);
      correctExceptionThrown = true;
    } catch (TypeInfoException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    }
    Assert.assertTrue(correctExceptionThrown);
    Assert.assertNull(typeInfo);
  }

  public final void testFromSpringResourceNegative3() {
    // xml resource specifies a property that doesn't exist - this prompts
    // BeanInstantiationFailureException
    String resourceName =
        "testdata/connectorTypeTests/negative3/connectorType.xml";
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = null;
    boolean correctExceptionThrown = false;
    try {
      typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    } catch (BeanInstantiationFailureException e) {
      LOGGER.log(Level.WARNING, "Factory Creation Failure", e);
      correctExceptionThrown = true;
    } catch (TypeInfoException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    }
    Assert.assertTrue(correctExceptionThrown);
    Assert.assertNull(typeInfo);
  }

  public final void testFromSpringResourceNegative4() {
    // valid type xml but missing instance xml prompts
    // InstanceXmlMissingException
    String resourceName =
        "testdata/connectorTypeTests/negative4/connectorType.xml";
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = null;
    boolean correctExceptionThrown = false;
    try {
      typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    } catch (InstanceXmlMissingException e) {
      LOGGER.log(Level.WARNING, "Factory Creation Failure", e);
      correctExceptionThrown = true;
    } catch (TypeInfoException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    }
    Assert.assertTrue(correctExceptionThrown);
    Assert.assertNull(typeInfo);
  }

}
