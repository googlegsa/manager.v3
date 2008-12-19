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

  public String sendData(String dataSource,  String feedType, InputStream data)
      throws IOException {
    int bytesRead = 0;
    int val = 0;
    byte[] bytebuf = new byte[32768];
    // Consume the input stream, but discard the contents.
    while ((val = data.read(bytebuf)) != -1) {
      bytesRead += val;
    }
    LOGGER.fine("Null FeedConnection discarded " + bytesRead + " bytes.");
    return GsaFeedConnection.SUCCESS_RESPONSE;
  }
}
