// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.pusher.FeedException;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * A Pusher that throws the specified exception from the specified location.
 */
public class ExceptionalPusher extends MockPusher {
  protected final Where where;
  protected final Exception exception;

  /**
   * Locations from where ExceptionalPusher will throw its exceptions.
   */
  public static enum Where { NONE, TAKE, FLUSH, CANCEL, STATUS }

  public ExceptionalPusher(Exception exception, Where where) {
    this.exception = exception;
    this.where = where;
  }

  /**
   * Throws either a RuntimeException, PushException, FeedException,
   * or a RepostioryException.
   */
  private static void throwException(Exception exception)
    throws RepositoryException, PushException, FeedException {
    if (exception instanceof PushException) {
      throw (PushException) exception;
    } else if (exception instanceof FeedException) {
      throw (FeedException) exception;
    } else if (exception instanceof RepositoryException) {
      throw (RepositoryException) exception;
    } else if (exception instanceof RuntimeException) {
      // RuntimeExceptions don't need to be declared.
      throw (RuntimeException) exception;
    }
  }

  @Override
  public PusherStatus take(Document document)
      throws PushException, FeedException, RepositoryException {
    if (where == Where.TAKE) {
      throwException(exception);
    }
    return super.take(document);
  }

  @Override
  public void flush()
    throws PushException, FeedException, RepositoryException {
    if (where == Where.FLUSH) {
      throwException(exception);
    }
    super.flush();
  }

  @Override
  public void cancel() {
    if (where == Where.CANCEL) {
      throw new RuntimeException("TestRuntimeException");
    }
    super.cancel();
  }

  @Override
  public PusherStatus getPusherStatus()
      throws PushException, FeedException, RepositoryException {
    if (where == Where.STATUS) {
      throwException(exception);
    }
    return super.getPusherStatus();
  }
}
