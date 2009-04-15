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

public class BooleanValue extends ValueImpl {

  boolean booleanValue;

  private static BooleanValue TRUE_INSTANCE = new BooleanValue(true);
  private static BooleanValue FALSE_INSTANCE = new BooleanValue(false);

  private BooleanValue(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public static BooleanValue makeBooleanValue(boolean bool) {
    return bool ? TRUE_INSTANCE : FALSE_INSTANCE;
  }

  @Override
  public String toFeedXml() {
    return Boolean.toString(booleanValue);
  }

  @Override
  public String toString() {
    return Boolean.toString(booleanValue);
  }

  public static BooleanValue makeBooleanValue(String s) {
    boolean b = !("false".equalsIgnoreCase(s) || "f".equalsIgnoreCase(s));
    return makeBooleanValue(b);
  }

  @Override
  public boolean toBoolean() {
    return booleanValue;
  }

}
