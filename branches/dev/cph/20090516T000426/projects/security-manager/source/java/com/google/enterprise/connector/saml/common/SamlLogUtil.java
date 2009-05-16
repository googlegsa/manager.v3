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

package com.google.enterprise.connector.saml.common;

import org.opensaml.common.SAMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.Configuration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSSerializer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SamlLogUtil {
  
  private SamlLogUtil() {
    // prevent instantiation
    throw new UnsupportedOperationException();
  }

  private static final Logger LOGGER = Logger.getLogger(SamlLogUtil.class.getName());

  static volatile boolean initialized = false;

  static LSSerializer writer;

  public static void logXml(Logger logger, Level level, String message, SAMLObject so) {
    if (logger.isLoggable(level)) {
      if (writer == null) {
        logger.log(level, message + " no XML serializer available " + so.toString());
        return;
      }
      Element element = null;
      try {
        element = Configuration.getMarshallerFactory().getMarshaller(so).marshall(so);
      } catch (MarshallingException e) {
        LOGGER.log(Level.WARNING, "MarshallingException while marshalling", e);
        logger.log(level, message + " MarshallingException while marshalling " + so.toString());
        return;
      }
      if (element == null) {
        logger.log(level, message + " SAMLObject marshalls to null " + so.toString());
        return;
      }
      String str = null;
      try {
        str = writer.writeToString(element);
      } catch (DOMException e) {
        LOGGER.log(Level.WARNING, "DOMException while serializing", e);
        logger.log(level, message + " DOMException while serializing " + so.toString());
        return;
      } catch (LSException e) {
        LOGGER.log(Level.WARNING, "LSException while marshalling", e);
        logger.log(level, message + " LSException while marshalling " + so.toString());
        return;
      }
      logger.log(level, message + str);
    }
  }

  static {
    writer = null;
    DOMImplementationRegistry registry = null;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error getting xml registry", e);
    }
    if (registry == null) {
      LOGGER.log(Level.WARNING, "Null registry");
    } else {
      DOMImplementationLS impl = null;
      impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
      if (impl == null) {
        LOGGER.log(Level.WARNING, "Null LS impl");
      } else {
        writer = impl.createLSSerializer();
        if (writer == null) {
          LOGGER.log(Level.WARNING, "Null Serializer");
        }
      }
    }
  }
}
