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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConnectorType;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Instantiator for ConnectorType objects. Uses Spring and the classpath.
 */
public class SpringConnectorTypeInstantiator implements ConnectorTypeInstantiator {

  private static final Logger LOGGER =
      Logger.getLogger(SpringConnectorTypeInstantiator.class.getName());

  private SortedMap connectorTypeMap = null;
  private SortedMap connectorInstancePrototypeMap = null;
  private String classpathTypePattern = "classpath*:conf/connectorType.xml";
  private String classpathInstancePrototypePattern =
      "classpath*:conf/connectorInstance.xml";
  private boolean initialized = false;

  /**
   * @param classpathInstancePrototypePattern the
   *        classpathInstancePrototypePattern to set
   */
  public void setClasspathInstancePrototypePattern(
      String classpathInstancePrototypePattern) {
    this.classpathInstancePrototypePattern = classpathInstancePrototypePattern;
  }

  /**
   * Sets the classpathTypePattern
   * 
   * @param classpathTypePattern the classpathTypePattern to set
   */
  public void setClasspathPattern(String classpathTypePattern) {
    this.classpathTypePattern = classpathTypePattern;
  }

  private ApplicationContext makeApplicationContext() {
    ApplicationContext ac =
        new ClassPathXmlApplicationContext(classpathTypePattern);
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
    // now initialize the prototypes
    findConnectorPrototypes(ac);
  }

  private void initialize() {
    if (initialized) {
      return;
    }
    if (connectorTypeMap == null) {
      instantiateAllConnectorTypes();
    }
    if (connectorTypeMap == null) {
      throw new IllegalStateException();
    }
    initialized = true;
  }

  private void findConnectorPrototypes(ApplicationContext ac) {
    List resources = findPrototypeResources(ac);
    List prototypeStrings = getPrototypeString(resources);
    connectorInstancePrototypeMap = new TreeMap();
    mapPrototypesToConnectorNames(prototypeStrings);
  }

  private void mapPrototypesToConnectorNames(List prototypeStrings) {
    // loop through the instantiated connectors and find the right resource for
    // each one
    for (Iterator connectorTypeIter = getConnectorTypeNames(); connectorTypeIter
        .hasNext();) {
      String connectorTypeName = (String) connectorTypeIter.next();
      String pattern = "<bean id=\"" + connectorTypeName + "Instance";
      boolean found = false;
      for (Iterator prototypeIter = prototypeStrings.iterator(); prototypeIter
          .hasNext();) {
        String prototypeString = (String) prototypeIter.next();
        if (prototypeString.indexOf(pattern) >= 0) {
          connectorInstancePrototypeMap.put(connectorTypeName, prototypeString);
          prototypeIter.remove();
          found = true;
          break;
        }
      }
      if (!found) {
        LOGGER.warning("Connector type " + connectorTypeName
            + " has no instance prototype");
      }
    }
  }

  private List getPrototypeString(List resources) {
    List result = new LinkedList();
    for (Iterator i = resources.iterator(); i.hasNext();) {
      Resource r = (Resource) i.next();
      InputStream is = null;
      try {
        is = r.getInputStream();
      } catch (IOException e) {
        LOGGER.warning("IOException during connector type initialization");
        // TODO(ziff): dump stack trace, cause
      }
      String prototypeString = StringUtils.streamToString(is);
      result.add(prototypeString);
    }
    return result;
  }

  private List findPrototypeResources(ApplicationContext ac) {
    Resource[] resourceArray;
    try {
      resourceArray = ac.getResources(classpathInstancePrototypePattern);
    } catch (IOException e1) {
      throw new IllegalArgumentException();
    }
    List resources = new LinkedList(Arrays.asList(resourceArray));
    return resources;
  }

  /**
   * Default, no-argument constructor
   * 
   */
  public SpringConnectorTypeInstantiator() {
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.instantiator.ConnectorTypeInstantiator#getConnectorInstancePrototype(java.lang.String)
   */
  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    if (connectorTypeName == null || connectorTypeName.length() < 1) {
      throw new IllegalArgumentException();
    }
    initialize();
    String result =
        (String) connectorInstancePrototypeMap.get(connectorTypeName);
    if (result == null) {
      throw new ConnectorTypeNotFoundException(connectorTypeName);
    }
    return result;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.instantiator.ConnectorTypeInstantiator#getConnectorType(java.lang.String)
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

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.instantiator.ConnectorTypeInstantiator#getConnectorTypeNames()
   */
  public Iterator getConnectorTypeNames() {
    initialize();
    return Collections.unmodifiableSet(connectorTypeMap.keySet()).iterator();
  }

}
