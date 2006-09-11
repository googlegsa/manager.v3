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

import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConnectorType;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Instantiator for ConnectorType objects. Uses Spring and the classpath.
 */
public class ConnectorTypeInstantiator {

  private SortedMap connectorTypeMap = null;;
  private String classpathPattern = "classpath*:conf/connectorType.xml";

  /**
   * Gets the classpathPattern
   * 
   * @return the classpathPattern
   */
  public String getClasspathPattern() {
    return classpathPattern;
  }

  /**
   * Sets the classpathPattern
   * 
   * @param classpathPattern the classpathPattern to set
   */
  public void setClasspathPattern(String classpathPattern) {
    this.classpathPattern = classpathPattern;
  }

  private ApplicationContext makeApplicationContext() {
    ApplicationContext ac =
        new ClassPathXmlApplicationContext(classpathPattern);
    return ac;
  }

  private void instantiateAllConnectorTypes() {
    ApplicationContext ac = makeApplicationContext();
    connectorTypeMap = new TreeMap();
    String[] beanList = ac.getBeanNamesForType(ConnectorType.class);
    for (int i = 0; i < beanList.length; i++) {
      ConnectorType c = (ConnectorType) ac.getBean(beanList[i]);
      connectorTypeMap.put(beanList[i], c);
    }
  }

  private void initialize() {
    if (connectorTypeMap == null) {
      instantiateAllConnectorTypes();
    }
    if (connectorTypeMap == null) {
      throw new IllegalStateException();
    }
  }

  /**
   * Default, no-argument constructor
   * 
   */
  public ConnectorTypeInstantiator() {
  }

  /**
   * Finds a named connector type.
   * 
   * @param connectorTypeName The connector type to find
   * @return the ConnectorType, fully instantiated
   * @throws ConnectorTypeNotFoundException if the connector type is not found
   */
  public ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    if (connectorTypeName == null || connectorTypeName.length() < 1) {
      throw new IllegalArgumentException();
    }
    initialize();
    ConnectorType result =
        (ConnectorType) connectorTypeMap.get(connectorTypeName);
    if (result == null) {
      throw new ConnectorTypeNotFoundException(connectorTypeName);
    }
    return result;
  }


  /**
   * Gets all the known connector type names
   * @return an iterator of String names
   */
  public Iterator getConnectorTypeNames() {
    initialize();
    return Collections.unmodifiableSet(connectorTypeMap.keySet()).iterator();
  }

}
