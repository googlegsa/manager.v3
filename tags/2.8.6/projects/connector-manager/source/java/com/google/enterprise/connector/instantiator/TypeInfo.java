// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.spi.ConnectorType;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container for info about a Connector Type. Instantiable only through a static
 * factory that uses Spring.
 */
public class TypeInfo {
  public  static final String CONNECTOR_INSTANCE_XML = "connectorInstance.xml";
  private static final String CONNECTOR_DEFAULTS_XML = "connectorDefaults.xml";

  private static final Logger LOGGER =
      Logger.getLogger(TypeInfo.class.getName());

  private final String connectorTypeName;
  private final ConnectorType connectorType;
  private final Resource connectorInstancePrototype;
  private final Resource connectorDefaultPrototype;
  private File connectorTypeDir = null;

  /**
   * @return the connectorInstancePrototype
   */
  Resource getConnectorInstancePrototype() {
    return connectorInstancePrototype;
  }

  /**
   * @return the connectorDefaultPrototype
   */
  Resource getConnectorDefaultPrototype() {
    return connectorDefaultPrototype;
  }

  /**
   * @return the connectorType
   */
  ConnectorType getConnectorType() {
    return connectorType;
  }

  /**
   * @return the connectorTypeName
   */
  String getConnectorTypeName() {
    return connectorTypeName;
  }

  /**
   * @return the connectorTypeDir
   */
  File getConnectorTypeDir() {
    return connectorTypeDir;
  }

  /**
   * @param connectorTypeDir the connectorTypeDir to set
   */
  void setConnectorTypeDir(File connectorTypeDir) {
    this.connectorTypeDir = connectorTypeDir;
  }

  private TypeInfo(String connectorTypeName, ConnectorType connectorType,
      Resource connectorInstancePrototype, Resource connectorDefaultPrototype) {
    this.connectorTypeName = connectorTypeName;
    this.connectorType = connectorType;
    this.connectorInstancePrototype = connectorInstancePrototype;
    this.connectorDefaultPrototype = connectorDefaultPrototype;
  }

  public static TypeInfo fromSpringResource(Resource r) {
    TypeInfo result = null;
    try {
      result = fromSpringResourceAndThrow(r);
    } catch (TypeInfoException e) {
      LOGGER.log(Level.WARNING, "", e);
    }
    return result;
  }

  /**
   * Produces info about a connector type from a Spring resource. Throughout, we
   * catch runtime exceptions, since Spring is known to throw them. We want to
   * try to recover by moving on to the next resource if needed.
   *
   * @param r A spring resource pointing to xml bean definitions.
   * @return TypeInfo extracted from that resource
   * @throws FactoryCreationFailureException
   * @throws BeanListFailureException
   * @throws NoBeansFoundException
   * @throws BeanInstantiationFailureException
   * @throws InstanceXmlFailureException
   * @throws InstanceXmlMissingException
   */
  static TypeInfo fromSpringResourceAndThrow(Resource r)
      throws FactoryCreationFailureException, BeanListFailureException, NoBeansFoundException,
      BeanInstantiationFailureException, InstanceXmlFailureException, InstanceXmlMissingException {
    String connectorTypeName = null;
    ConnectorType connectorType = null;
    Resource connectorInstancePrototype = null;
    Resource connectorDefaultPrototype = null;

    // Make a bean factory from the resource.
    XmlBeanFactory factory;
    try {
      factory = new XmlBeanFactory(r);
    } catch (RuntimeException e) {
      throw new FactoryCreationFailureException(r, e);
    }

    // Get the list of Connector Types defined in the bean factory.
    String beanList[];
    try {
      beanList = factory.getBeanNamesForType(ConnectorType.class);
    } catch (Exception e) {
      throw new BeanListFailureException(r, e);
    }

    // Make sure there is at least one Connector Type.
    if (beanList.length < 1) {
      throw new NoBeansFoundException(r);
    }

    // Remember the name of the first one found, and instantiate it.
    connectorTypeName = beanList[0];
    try {
      connectorType = (ConnectorType) factory.getBean(connectorTypeName);
    } catch (Exception e) {
      throw new BeanInstantiationFailureException(r, e, connectorTypeName);
    }

    // Find the instance prototype.
    try {
      connectorInstancePrototype = r.createRelative(CONNECTOR_INSTANCE_XML);
    } catch (Exception e) {
      throw new InstanceXmlFailureException(r, e, connectorTypeName,
                                            CONNECTOR_INSTANCE_XML);
    }

    if (!connectorInstancePrototype.exists()) {
      throw new InstanceXmlMissingException(r, connectorTypeName);
    }

    // Find the default prototype.
    try {
      connectorDefaultPrototype = r.createRelative(CONNECTOR_DEFAULTS_XML);
    } catch (Exception e) {
      throw new InstanceXmlFailureException(r, e, connectorTypeName,
                                            CONNECTOR_DEFAULTS_XML);
    }
    if (!connectorDefaultPrototype.exists()) {
      connectorDefaultPrototype = null;
    }

    TypeInfo result = new TypeInfo(connectorTypeName, connectorType,
        connectorInstancePrototype, connectorDefaultPrototype);

    // If more Connector Types were found, issue a warning.
    if (beanList.length > 1) {
      StringBuilder buf = new StringBuilder();
      for (int i = 1; i < beanList.length; i++) {
        buf.append(" ");
        buf.append(beanList[i]);
      }
      LOGGER.log(Level.WARNING, "Resource " + r.getDescription()
          + "contains multiple Connector Type definitions.  Using the first: "
          + connectorTypeName + ".  Skipping:" + buf.toString());
    }

    return result;
  }

  static class TypeInfoException extends InstantiatorException {
    TypeInfoException(String message, Exception cause) {
      super(message, cause);
    }

    TypeInfoException(String message) {
      super(message);
    }
  }

  static class FactoryCreationFailureException extends TypeInfoException {
    FactoryCreationFailureException(Resource resource, Exception cause) {
      super("Exception from Spring while creating bean "
          + "factory from resource " + resource.getDescription(), cause);
    }
  }
  static class BeanListFailureException extends TypeInfoException {
    BeanListFailureException(Resource resource, Exception cause) {
      super("Exception from Spring while listing beans "
          + " from factory from resource " + resource.getDescription(), cause);
    }
  }
  static class NoBeansFoundException extends TypeInfoException {
    NoBeansFoundException(Resource resource) {
      super("Resource " + resource.getDescription()
          + " contains no definitions for ConnectorType");
    }
  }
  static class BeanInstantiationFailureException extends TypeInfoException {
    BeanInstantiationFailureException(Resource resource, Exception cause,
        String connectorTypeName) {
      super("Exception from Spring while instantiating " + " connector type "
          + connectorTypeName + " from resource " + resource.getDescription(),
          cause);
    }
  }
  static class InstanceXmlFailureException extends TypeInfoException {
    InstanceXmlFailureException(Resource resource, Exception cause,
        String connectorTypeName, String xmlResourceName) {
      super("Exception from Spring while creating " + xmlResourceName
          + " sibling resource for " + connectorTypeName + " from resource "
          + resource.getDescription(), cause);
    }
  }
  static class InstanceXmlMissingException extends TypeInfoException {
    InstanceXmlMissingException(Resource resource, String connectorTypeName) {
      super("Can't find " + CONNECTOR_INSTANCE_XML + " sibling resource for "
          + connectorTypeName + " from resource  " + resource.getDescription());
    }
  }
}
