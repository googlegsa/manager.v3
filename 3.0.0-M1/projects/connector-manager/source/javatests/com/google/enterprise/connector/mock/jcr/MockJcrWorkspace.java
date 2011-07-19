// Copyright (C) 2006-2008 Google Inc.
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

import org.xml.sax.ContentHandler;

import java.io.InputStream;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;

/**
 * MockJcrWorkspace implements the corresponding JCR interface, with these
 * limitations:
 * <ul>
 * <li> This is a "level 1" (read-only) implementation. All level 2
 * (side-effecting) calls throw UnsupportedOperation exceptions. These are
 * grouped at the bottom of the class implementation.
 * <li> Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure. Eventually, these will be documented as part
 * of framework documentation. In this implementation, they also throw
 * UnsupportedOperation exceptions. These are grouped above the level 2 calls.
 * <li> Some level 1 calls are not currently needed by our implementation, but
 * may be soon. These are marked with todos and throw UnsupportedOperation
 * exceptions.
 * </ul>
 */
public class MockJcrWorkspace implements Workspace {

  MockJcrRepository repo;
  MockJcrSession session;

  /**
   * Create a Workspace object from a
   * @param repo
   * @param session
   */
  public MockJcrWorkspace(MockJcrRepository repo, MockJcrSession session) {
    this.repo = repo;
    this.session = session;
  }

  /**
   * Returns the Session with which this was created
   * @return Session
   */
  public Session getSession() {
    return session;
  }

  /**
   * Returns a name for this Workspace
   * @return The constant "MockJcrWorkspace"
   */
  public String getName() {
    return "MockJcrWorkspace";
  }

  /**
   * Gets a QueryManager for this Workspace
   * @return MockJcrQueryManager
   */
  public QueryManager getQueryManager() {
    return new MockJcrQueryManager(repo.getRepo().getStore());
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public ObservationManager getObservationManager() {
    throw new UnsupportedOperationException();
  }

  /**
   * The following methods are JCR level 1 - but we do not anticipate using them
   */

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public NamespaceRegistry getNamespaceRegistry() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public NodeTypeManager getNodeTypeManager() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String[] getAccessibleWorkspaceNames() {
    throw new UnsupportedOperationException();
  }


   // The following methods are JCR level 2 - these would never be needed

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   */
  public void copy(String arg0, String arg1){
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   */
  public void copy(String arg0, String arg1, String arg2) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   * @param arg3
   */
  public void clone(String arg0, String arg1, String arg2, boolean arg3) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   */
  public void move(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   */
  public void restore(Version[] arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @return nothing
   */
  public ContentHandler getImportContentHandler(String arg0, int arg1) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   */
  public void importXML(String arg0, InputStream arg1, int arg2) {
    throw new UnsupportedOperationException();
  }
}
