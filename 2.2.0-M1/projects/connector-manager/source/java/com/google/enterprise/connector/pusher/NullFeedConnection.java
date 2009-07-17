// Copyright 2008 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Null Feed takes the feed data and drops it on the floor.
 * This can be used to test Connector feeds without actually feeding
 * the data to the Google Search Appliance.  Turning on teedFeedFile
 * logging can help determine if the feed contents are correct.
 * Leaving it off can help determine if Traversal performance is
 * acceptable.
 */
public class NullFeedConnection implements FeedConnection {

  private static final Logger LOGGER =
      Logger.getLogger(NullFeedConnection.class.getName());

  public NullFeedConnection() {
    LOGGER.info("Using Null FeedConnection.  Fed Content will be discarded.");
  }

  //@Override
  public String sendData(String dataSource, FeedData feedData)
      throws RepositoryException {
    try {
      InputStream data = ((GsaFeedData)feedData).getData();
      int bytesRead = 0;
      int val = 0;
      byte[] bytebuf = new byte[32768];
      // Consume the input stream, but discard the contents.
      while ((val = data.read(bytebuf)) != -1) {
        bytesRead += val;
      }
      LOGGER.fine("Null FeedConnection discarded " + bytesRead + " bytes.");
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE,
                 "IOException while reading: skipping", ioe);
      Throwable t = ioe.getCause();
      if (t != null && (t instanceof RepositoryException)) {
        throw (RepositoryException) t;
      } else {
        throw new RepositoryDocumentException(ioe);
      }
    }
    return GsaFeedConnection.SUCCESS_RESPONSE;
  }

  //@Override
  public int getBacklogCount() {
    return -1;
  }

  //@Override
  public int getScheduleFormat() {
    return 1;
  }

  //@Override
  public String getContentEncodings() {
    return "base64binary, base64compressed";
  }
}
