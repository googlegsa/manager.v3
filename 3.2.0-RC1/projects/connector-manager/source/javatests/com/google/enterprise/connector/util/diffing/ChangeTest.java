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
import com.google.enterprise.connector.util.diffing.DeleteDocumentHandle;
import com.google.enterprise.connector.util.diffing.DeleteDocumentHandleFactory;
import com.google.enterprise.connector.util.diffing.MonitorCheckpoint;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 */
public class ChangeTest extends TestCase {
  private static final MonitorCheckpoint MCP =
      new MonitorCheckpoint("foo", 13, 9876543210L, 1234567890L);

  private DeleteDocumentHandleFactory internalFactory;
  private MockDocumentHandleFactory clientFactory;

  @Override
  public void setUp() {
    internalFactory = new DeleteDocumentHandleFactory();
    clientFactory = new MockDocumentHandleFactory();
    }

  public void testChange_fromJsonInternalFactory() throws Exception {
    DeleteDocumentHandle ddh = new DeleteDocumentHandle("abc");
    Change c = new Change(Change.FactoryType.INTERNAL, ddh, MCP);
    String stringForm = c.getJson().toString();
    JSONObject json = new JSONObject(stringForm);
    Change copy = new Change(json, internalFactory, clientFactory);
    assertEquals(c.getMonitorCheckpoint(), copy.getMonitorCheckpoint());
    assertEquals(c.getDocumentHandle().getDocumentId(),
        copy.getDocumentHandle().getDocumentId());
  }

  public void testChange_fromJsonClientFactory() throws Exception {
    MockDocumentHandle mdh = new MockDocumentHandle("aa", "extra");
    Change c = new Change(Change.FactoryType.CLIENT, mdh, MCP);
    String stringForm = c.getJson().toString();
    JSONObject json = new JSONObject(stringForm);
    Change copy = new Change(json, internalFactory, clientFactory);
    assertEquals(c.getMonitorCheckpoint(), copy.getMonitorCheckpoint());
    MockDocumentHandle mdhCopy = (MockDocumentHandle)copy.getDocumentHandle();
    assertEquals(mdhCopy.getDocumentId(), mdh.getDocumentId());
    assertEquals(mdhCopy.getExtra(), mdh.getExtra());
  }
}
