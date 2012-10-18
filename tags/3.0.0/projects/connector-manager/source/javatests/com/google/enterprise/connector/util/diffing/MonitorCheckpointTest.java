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

import com.google.enterprise.connector.util.diffing.MonitorCheckpoint;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Unit test for {@link MonitorCheckpoint}
 */
public class MonitorCheckpointTest extends TestCase {
  public void testFields() {
    MonitorCheckpoint mcp = new MonitorCheckpoint("foo", 13, 987654321, 876543210);
    assertEquals("foo", mcp.getMonitorName());
    assertEquals(13, mcp.getSnapshotNumber());
    assertEquals(987654321, mcp.getOffset1());
    assertEquals(876543210, mcp.getOffset2());
  }

  public void testEquals() {
    MonitorCheckpoint mcp1 = new MonitorCheckpoint("foo", 13, 987654321, 876543210);

    MonitorCheckpoint mcp2 = new MonitorCheckpoint("foo", 13, 987654321, 876543210);
    assertEquals(mcp1, mcp2);
    assertEquals(mcp1.hashCode(), mcp2.hashCode());

    MonitorCheckpoint mcp3 = new MonitorCheckpoint("bar", 13, 987654321, 876543210);
    assertFalse(mcp1.equals(mcp3));
    assertFalse(mcp1.hashCode() == mcp3.hashCode());

    MonitorCheckpoint mcp4 = new MonitorCheckpoint("foo", 17, 987654321, 876543210);
    assertFalse(mcp1.equals(mcp4));
    assertFalse(mcp1.hashCode() == mcp4.hashCode());

    MonitorCheckpoint mcp5 = new MonitorCheckpoint("foo", 13, 907654321, 876543210);
    assertFalse(mcp1.equals(mcp5));
    assertFalse(mcp1.hashCode() == mcp5.hashCode());

    MonitorCheckpoint mcp6 = new MonitorCheckpoint("foo", 13, 907654321, 8765432170L);
    assertFalse(mcp1.equals(mcp6));
    assertFalse(mcp1.hashCode() == mcp6.hashCode());
  }

  public void testJson() throws JSONException {
    MonitorCheckpoint mcp1 = new MonitorCheckpoint("foo", 13, 987654321, 876543210);
    JSONObject json = mcp1.getJson();
    MonitorCheckpoint mcp2 = new MonitorCheckpoint(json);
    assertEquals(mcp1, mcp2);
  }
}
