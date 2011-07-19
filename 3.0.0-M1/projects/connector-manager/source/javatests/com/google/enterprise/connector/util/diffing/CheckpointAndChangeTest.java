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

import com.google.enterprise.connector.util.diffing.Change;
import com.google.enterprise.connector.util.diffing.CheckpointAndChange;
import com.google.enterprise.connector.util.diffing.DeleteDocumentHandle;
import com.google.enterprise.connector.util.diffing.DeleteDocumentHandleFactory;
import com.google.enterprise.connector.util.diffing.DiffingConnectorCheckpoint;
import com.google.enterprise.connector.util.diffing.MonitorCheckpoint;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * Test for {@link CheckpointAndChange}.
 */
public class CheckpointAndChangeTest extends TestCase {
  private static final MonitorCheckpoint MCP =
    new MonitorCheckpoint("foo", 13, 9876543210L, 1234567890L);

  private DeleteDocumentHandleFactory internalFactory;
  private MockDocumentHandleFactory clientFactory;

  @Override
  public void setUp() {
    internalFactory = new DeleteDocumentHandleFactory();
    clientFactory = new MockDocumentHandleFactory();
  }

  public void testCheckpointAndChange_internal() throws Exception {
    DiffingConnectorCheckpoint fccp = DiffingConnectorCheckpoint.newFirst();
    DeleteDocumentHandle ddh = new DeleteDocumentHandle("abc");
    Change c = new Change(Change.FactoryType.INTERNAL, ddh, MCP);
    CheckpointAndChange checkpointAndChange = new CheckpointAndChange(fccp, c);
    String stringForm = checkpointAndChange.getJson().toString();
    JSONObject json = new JSONObject(stringForm);
    CheckpointAndChange copy = new CheckpointAndChange(json, internalFactory,
        clientFactory);
    assertEquals(fccp, copy.getCheckpoint());
    assertEquals(c.getMonitorCheckpoint(),
        copy.getChange().getMonitorCheckpoint());
    assertEquals(c.getDocumentHandle().getDocumentId(),
        copy.getChange().getDocumentHandle().getDocumentId());
  }

  public void testCheckpointAndChange_client() throws Exception {
    DiffingConnectorCheckpoint fccp = DiffingConnectorCheckpoint.newFirst();
    MockDocumentHandle mdh = new MockDocumentHandle("abc", "data");
    Change c = new Change(Change.FactoryType.CLIENT, mdh, MCP);
    CheckpointAndChange checkpointAndChange = new CheckpointAndChange(fccp, c);
    String stringForm = checkpointAndChange.getJson().toString();
    JSONObject json = new JSONObject(stringForm);
    CheckpointAndChange copy = new CheckpointAndChange(json, internalFactory,
        clientFactory);
    assertEquals(fccp, copy.getCheckpoint());
    assertEquals(c.getMonitorCheckpoint(),
        copy.getChange().getMonitorCheckpoint());
    MockDocumentHandle mdhCopy =
        (MockDocumentHandle)copy.getChange().getDocumentHandle();
    assertEquals(mdh.getDocumentId(), mdhCopy.getDocumentId());
    assertEquals(mdh.getExtra(), mdhCopy.getExtra());
  }

}
