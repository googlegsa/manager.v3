// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.logging.*;

/**
 * A simple {@code ErrorHandler} implementation to be used with the
 * the SAX Parser. This error handler simply logs the SAX parser errors.
 *
 * @since 2.8
 */
public class SAXParseErrorHandler implements ErrorHandler {
  private static final Logger LOG =
    Logger.getLogger(SAXParseErrorHandler.class.getName());

  public SAXParseErrorHandler() {
  }

  @Override
  public void error(SAXParseException exception) {
    LOG.log(Level.INFO, "Error", exception);
  }

  @Override
  public void fatalError(SAXParseException exception) {
    LOG.log(Level.INFO, "FatalError", exception);
  }

  @Override
  public void warning(SAXParseException exception) {
    LOG.log(Level.INFO, "Warning", exception);
  }
}