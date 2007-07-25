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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryProperty.PropertyType;
import com.google.enterprise.connector.spi.old.SimpleValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * MockJcrValue implements the corresponding JCR interface.
 */
public class MockJcrValue implements Value {

  String val;
  PropertyType type;
  InputStream streamVal;

  public MockJcrValue(MockRepositoryProperty p) {
    this.type = p.getType();
    if (p.getType().equals(PropertyType.STREAM)) {
      this.streamVal = p.getStreamValue();
    } else {
      this.val = p.getValue();
    }
  }

  public MockJcrValue(PropertyType type, String v) {
    this.val = v;
    this.type = type;    
  }

  public String getString() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    String result;
    if (type.equals(PropertyType.DATE)) {
      Calendar c = getDate();
      result = SimpleValue.calendarToIso8601(c);
    } else if (type.equals(PropertyType.STREAM)) {
      if (null == val) {
        val = StringUtils.streamToString(streamVal);
      }
      result = val;
    } else {
      result = val;
    }
    return result;
  }

  public InputStream getStream() throws IllegalStateException,
      RepositoryException {
      if (null == streamVal) {
        // Convert non-stream values into a stream
        InputStream is = new ByteArrayInputStream(val.getBytes());
        return is;
      } else {
        // Return the original stream as is
        return streamVal;
      }
  }

  public long getLong() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    return Long.parseLong(val);
  }

  public double getDouble() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    // TODO(ziff): will be needed later for full testing
    throw new UnsupportedOperationException();
  }

  public Calendar getDate() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    // TODO: must fix this where sometimes we get the date value as a long but
    // sometimes we get the value in the format "Tue, 15 Nov 1994 12:45:26 GMT".
    long ticks = getLong();
    Calendar result = Calendar.getInstance();
    result.setTimeInMillis(ticks * 1000);
    return result;
  }

  public boolean getBoolean() throws ValueFormatException,
      IllegalStateException, RepositoryException {
    if (val.equalsIgnoreCase("t") || val.equalsIgnoreCase("true")) {
      return true;
    }
    return false;
  }

  public int getType() {
    return mockRepositoryTypeToJCRType(type);
  }

  // The mapping below will need to be maintained as we add new 
  // datatypes to the MockRepository implementation. 
  // For reference, I have copied the following constants from JCR:
  // public static final int STRING = 1;
  // public static final int BINARY = 2;
  // public static final int LONG = 3;
  // public static final int DOUBLE = 4;
  // public static final int DATE = 5;
  // public static final int BOOLEAN = 6;
  // public static final int NAME = 7;
  // public static final int PATH = 8;
  // public static final int REFERENCE = 9;
  // public static final int UNDEFINED = 0;

  protected static int mockRepositoryTypeToJCRType(PropertyType t) {
    // TODO(ziff): maintain this along with MockRepositoryProperty.PropertyType
    int result = javax.jcr.PropertyType.UNDEFINED;
    if (t == PropertyType.STRING) {
        result = javax.jcr.PropertyType.STRING;
    }
    else if (t == PropertyType.DATE) {
        result = javax.jcr.PropertyType.DATE;
    }
    else if (t == PropertyType.INTEGER) {
        result = javax.jcr.PropertyType.LONG;
    }
    return result;
  }
}
