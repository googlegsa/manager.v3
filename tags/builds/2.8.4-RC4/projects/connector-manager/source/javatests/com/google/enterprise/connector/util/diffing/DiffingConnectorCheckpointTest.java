// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.util.diffing.DiffingConnectorCheckpoint;

import junit.framework.TestCase;

/**
 * Tests for {@link DiffingConnectorCheckpoint}.
 */
public class DiffingConnectorCheckpointTest extends TestCase {

  public void testNewFirst() throws Exception {
    DiffingConnectorCheckpoint fccp = DiffingConnectorCheckpoint.newFirst();
    assertEquals(0, fccp.getMajorNumber());
    assertEquals(0, fccp.getMinorNumber());
    assertTrue(fccp.compareTo(fccp) == 0);
    assertEquals(fccp, fccp);
    DiffingConnectorCheckpoint fccp2 = DiffingConnectorCheckpoint.fromJsonString(fccp.toString());
    assertEquals(fccp, fccp2);
    assertEquals(fccp.hashCode(), fccp2.hashCode());
    assertTrue(fccp.compareTo(fccp2) == 0);
  }

  public void testNext() throws Exception {
    DiffingConnectorCheckpoint fccp = DiffingConnectorCheckpoint.newFirst();
    DiffingConnectorCheckpoint fccpN = fccp.next();
    assertEquals(fccp.getMajorNumber(), fccpN.getMajorNumber());
    assertEquals(fccp.getMinorNumber() + 1, fccpN.getMinorNumber());
    assertFalse(fccp.equals(fccpN));
    DiffingConnectorCheckpoint fccpN2 = DiffingConnectorCheckpoint.fromJsonString(fccpN.toString());
    assertEquals(fccpN, fccpN2);
    assertEquals(fccpN.hashCode(), fccpN2.hashCode());
    assertTrue(fccp.compareTo(fccpN) < 0);
    assertTrue(fccpN.compareTo(fccp) > 0);
  }

  public void testNextMajor() throws Exception {
    DiffingConnectorCheckpoint fccp = DiffingConnectorCheckpoint.newFirst();
    DiffingConnectorCheckpoint fccpN = fccp.next();
    DiffingConnectorCheckpoint fccpNextMajor = fccpN.nextMajor();
    assertEquals(fccp.getMajorNumber() + 1, fccpNextMajor.getMajorNumber());
    assertEquals(fccp.getMinorNumber(), fccpNextMajor.getMinorNumber());
    assertFalse(fccp.equals(fccpNextMajor));
    DiffingConnectorCheckpoint fccpNextMajor2 =
        DiffingConnectorCheckpoint.fromJsonString(fccpNextMajor.toString());
    assertEquals(fccpNextMajor, fccpNextMajor2);
    assertEquals(fccpNextMajor.hashCode(), fccpNextMajor2.hashCode());
    assertTrue(fccp.compareTo(fccpNextMajor) < 0);
    assertTrue(fccpN.compareTo(fccpNextMajor) < 0);
    assertTrue(fccpNextMajor.compareTo(fccp) > 0);
    assertTrue(fccpNextMajor.compareTo(fccpN) > 0);
    assertTrue(fccpNextMajor.compareTo(fccpNextMajor2) == 0);
  }

  public void testBadValue() throws Exception {
    try {
      DiffingConnectorCheckpoint.fromJsonString("I am no File Connector Checkpoint");
      fail();
    } catch (IllegalArgumentException iae) {
      // Expected.
    }
  }
}
