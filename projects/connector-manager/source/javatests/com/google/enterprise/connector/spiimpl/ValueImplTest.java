// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spiimpl;

import static org.junit.Assert.assertEquals;

import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Value;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Calendar;

/**
 * Tests that all value classes support toBoolean without throwing an exception.
 */
public class ValueImplTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /** Asserts that a Value is the expected class and has the expected value. */
  private void assertToBooleanEquals(boolean expected,
      Class<? extends ValueImpl> expectedClass, Value value) {
    assertEquals(expectedClass, value.getClass());
    assertEquals(expected, expectedClass.cast(value).toBoolean());
  }

  @Test
  public void testToBoolean_binaryNull() {
    byte[] nullValue = null;
    thrown.expect(NullPointerException.class);
    Value.getBinaryValue(nullValue);
  }

  @Test
  public void testToBoolean_binaryEmpty() {
    assertToBooleanEquals(false, BinaryValue.class,
        Value.getBinaryValue(new byte[0]));
  }

  @Test
  public void testToBoolean_binaryNonempty() {
    assertToBooleanEquals(false, BinaryValue.class,
        Value.getBinaryValue("hello, world".getBytes()));
  }

  @Test
  public void testToBoolean_dateNull() {
    assertToBooleanEquals(true, DateValue.class,
        Value.getDateValue(null));
  }

  @Test
  public void testToBoolean_dateNonnull() {
    assertToBooleanEquals(false, DateValue.class,
        Value.getDateValue(Calendar.getInstance()));
  }

  @Test
  public void testToBoolean_doubleZero() {
    assertToBooleanEquals(true, DoubleValue.class,
        Value.getDoubleValue(0.0));
  }

  @Test
  public void testToBoolean_doubleNonzero() {
    assertToBooleanEquals(false, DoubleValue.class,
        Value.getDoubleValue(3.1415926));
  }

  @Test
  public void testToBoolean_longZero() {
    assertToBooleanEquals(true, LongValue.class,
        Value.getLongValue(0L));
  }

  @Test
  public void testToBoolean_longNonzero() {
    assertToBooleanEquals(false, LongValue.class,
        Value.getLongValue(18155583303981L));
  }

  @Test
  public void testToBoolean_principalNull() {
    Principal nullValue = null;
    assertToBooleanEquals(true, PrincipalValue.class,
        Value.getPrincipalValue(nullValue));
  }

  @Test
  public void testToBoolean_principalNonnull() {
    assertToBooleanEquals(true, PrincipalValue.class,
        Value.getPrincipalValue("Je suis Mort"));
  }

  @Test
  public void testToBoolean_stringNull() {
    assertToBooleanEquals(true, StringValue.class,
        Value.getStringValue(null));
  }

  @Test
  public void testToBoolean_stringEmpty() {
    assertToBooleanEquals(true, StringValue.class,
        Value.getStringValue(""));
  }

  @Test
  public void testToBoolean_stringNonempty() {
    assertToBooleanEquals(true, StringValue.class,
        Value.getStringValue("hello, world"));
  }

  @Test
  public void testToBoolean_stringFalse() {
    assertToBooleanEquals(false, StringValue.class,
        Value.getStringValue("false"));
  }
}
