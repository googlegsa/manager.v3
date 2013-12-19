// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.spiimpl;

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.util.EofFilterInputStream;
import com.google.enterprise.connector.util.InputStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

public class BinaryValue extends ValueImpl {

  InputStreamFactory factory;
  InputStream binaryValue;

  public BinaryValue(InputStreamFactory inputStreamFactory) {
    this.factory = inputStreamFactory;
  }

  public BinaryValue(InputStream inputStream) {
    this.binaryValue = new EofFilterInputStream(inputStream);
  }

  public BinaryValue(byte[] byteArray) {
    this.binaryValue = new ByteArrayInputStream(byteArray);
  }

  @Override
  public String toFeedXml() {
    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    if (factory != null) {
      return factory.toString();
    } else {
      return binaryValue.toString();
    }
  }

  public InputStream getInputStream() throws RepositoryDocumentException {
    if (factory != null) {
      try {
        return new EofFilterInputStream(factory.getInputStream());
      } catch (IOException e) {
        throw new RepositoryDocumentException("Failed to get InputStream", e);
      }
    } else {
      return binaryValue;
    }
  }

  @Override
  public boolean toBoolean() {
    return (binaryValue == null);
  }

}
