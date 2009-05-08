// Copyright (C) 2006 Chris Harris, Carlisle UK
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

package com.google.enterprise.connector.common;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * This BeanFactoryPostProcessor provides Dependency Injection of values
 * for static setters.
 *
 * @author Chris Harris, Carlisle UK.
 */
public class StaticInitializerBeanFactoryPostProcessor
    implements BeanFactoryPostProcessor {
  private Map<String, Map<String, Object>> classes;
  private BeanWrapperImpl bri;

  public StaticInitializerBeanFactoryPostProcessor() {
    bri = new BeanWrapperImpl();
  }

  public void postProcessBeanFactory(
      ConfigurableListableBeanFactory configurableListableBeanFactory)
      throws BeansException {
    for (String className : classes.keySet()) {
      Map<String, Object> vars = classes.get(className);
      Class<?> c = null;
      try {
        c = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new NoSuchBeanDefinitionException("Class not found for "
                                                + className);
      }
      Method[] methods = c.getMethods();
      for (String fieldName : vars.keySet()) {
        Object value = vars.get(fieldName);
        Method method = findStaticSetter(methods, fieldName);
        if (method == null) {
          throw new RuntimeException("No static setter method found for class "
                                     + className + ", field " + fieldName,null);
        }
        Object newValue =
            bri.convertIfNecessary(value, getPropertyType(method), null);
        try {
          method.invoke(null, new Object[] {newValue});
        } catch (Exception e) {
          throw new RuntimeException("Invocation of method " + method.getName()
              + " on class " + className + " with value " + value + " failed.",
              e);
        }
      }
    }
  }

  private Class<?> getPropertyType(Method setter) {
    Class<?> params[] = setter.getParameterTypes();
    if (params.length != 1) {
      throw new RuntimeException("bad write method arg count: " + setter);
    }
    return  params[0];
  }

  /**
   * Look for a static setter method for field named fieldName in Method[].
   * Return null if none found.
   *
   * @param methods
   * @param fieldName
   * @return static setter method for field, or null if none found.
   */
  private Method findStaticSetter(Method[] methods, String fieldName) {
    String methodName = setterName(fieldName);
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].getName().equals(methodName) &&
          Modifier.isStatic(methods[i].getModifiers())) {
        return methods[i];
      }
    }
    return null;
  }

  /**
   * Return the standard setter name for field fieldName.
   *
   * @param fieldName
   * @return the standard setter name for field fieldName.
   */
  private String setterName(String fieldName) {
    String nameToUse = fieldName;
    if (fieldName.length() == 1) {
      if (Character.isLowerCase(fieldName.charAt(0))) {
        nameToUse = fieldName.toUpperCase();
      }
    } else {
      if (Character.isLowerCase(fieldName.charAt(0)) &&
          Character.isLowerCase(fieldName.charAt(1))) {
        nameToUse = fieldName.substring(0, 1).toUpperCase()
            + fieldName.substring(1);
      }
    }
    return "set" + nameToUse;
  }

  public void setClasses(Map<String, Map<String, Object>> classes) {
    this.classes = classes;
  }
}
