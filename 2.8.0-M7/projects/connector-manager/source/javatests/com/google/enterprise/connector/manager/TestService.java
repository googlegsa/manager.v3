// Copyright 2009 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.manager;

import java.util.List;
import java.util.logging.Logger;

/**
 * Simple {@link ContextService} that starts a thread and logs the usage of the
 * start and stop methods.
 */
public class TestService implements ContextService {
  private static final Logger LOGGER =
      Logger.getLogger(TestService.class.getName());

  private String serviceName = "TestService";
  private List<TestServiceToken> tokenList;
  private volatile boolean isRunning = false;

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public void setTokenList(List<TestServiceToken> tokenList) {
    this.tokenList = tokenList;
  }

  public void start() {
    LOGGER.info(serviceName + ": Entering start...");
    tokenList.add(new TestServiceToken(serviceName, "start"));
    isRunning = true;
    LOGGER.info(serviceName + ": ...exiting start.");
  }

  public void stop(boolean force) {
    LOGGER.info(serviceName + ": Entering stop...");
    LOGGER.info(serviceName + ": force=" + force);
    tokenList.add(new TestServiceToken(serviceName, "stop", force));
    isRunning = false;
    LOGGER.info(serviceName + ": ...exiting stop.");
  }

  public String getName() {
    return serviceName;
  }

  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Marker used to note service action.
   */
  public static class TestServiceToken {
    private final String service;
    private final String action;
    private final boolean actionForced;

    public TestServiceToken(String serviceName, String action) {
      this(serviceName, action, false);
    }

    public TestServiceToken(String serviceName, String action,
        boolean actionForced) {
      this.service = serviceName;
      this.action = action;
      this.actionForced = actionForced;
    }

    public String getService() {
      return service;
    }

    public String getAction() {
      return action;
    }

    public boolean isActionForced() {
      return actionForced;
    }
  }
}
