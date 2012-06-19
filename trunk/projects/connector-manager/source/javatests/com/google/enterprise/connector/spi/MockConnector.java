// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * A mock implementation for the Connector interface.
 */
public class MockConnector implements Connector {
  private final TraversalManager traversalManager;
  private final AuthenticationManager authenticationManager;
  private final AuthorizationManager authorizationManager;
  private final Retriever retriever;
  private final Lister lister;

  public MockConnector() {
    this(null, null, null, null, null);
  }

  public MockConnector(TraversalManager traversalManager,
                       AuthenticationManager authenticationManager,
                       AuthorizationManager authorizationManager,
                       Retriever retriever, Lister lister) {
    this.traversalManager = traversalManager;
    this.authenticationManager = authenticationManager;
    this.authorizationManager = authorizationManager;
    this.retriever = retriever;
    this.lister = lister;
  }

  /* @Override */
  public Session login() throws RepositoryLoginException, RepositoryException {
    return new MockSession(traversalManager, authenticationManager,
                           authorizationManager, retriever, lister);
  }
}
