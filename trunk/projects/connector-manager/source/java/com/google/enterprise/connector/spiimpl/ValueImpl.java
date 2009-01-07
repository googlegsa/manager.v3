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

import com.google.enterprise.connector.spi.Value;

import java.io.InputStream;
import java.util.Calendar;

public abstract class ValueImpl extends Value {

  public static Value getStringValue(String s) {
    return new StringValue(s);
  }

  public static Value getBinaryValue(InputStream is) {
    return new BinaryValue(is);
  }

  public static Value getBinaryValue(byte[] ba) {
    return new BinaryValue(ba);
  }

  public static Value getLongValue(long l) {
    return new LongValue(l);
  }

  public static Value getDoubleValue(double d) {
    return new DoubleValue(d);
  }

  public static Value getDateValue(Calendar c) {
    return new DateValue(c);
  }

  public static Value getBooleanValue(boolean b) {
    return BooleanValue.makeBooleanValue(b);
  }

  public static Value getBooleanValue(String s) {
    return BooleanValue.makeBooleanValue(s);
  }

  /**
   * Creates a string representation to be used in a feed document.
   * @return String representation for use in a feed document
   */
  public abstract String toFeedXml();

  /**
   * Converts to boolean if possible
   * @return boolean representation of the Value
   */
  public abstract boolean toBoolean();

}
