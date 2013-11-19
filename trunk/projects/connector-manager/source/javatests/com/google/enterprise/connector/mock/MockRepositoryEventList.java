// Copyright (C) 2006-2009 Google Inc.
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

package com.google.enterprise.connector.mock;

import com.google.enterprise.connector.manager.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A list of MockRepositoryEvents. The value of this class is its constructor,
 * which knows how to create events from a file, using json
 */
public class MockRepositoryEventList {
  private List<MockRepositoryEvent> eventList = null;
  private String workDirName = null;
  private String repositoryFileName = null;

  private static Logger LOGGER =
      Logger.getLogger(MockRepositoryEventList.class.getName());

  /**
   * @param repositoryFileName the repositoryFileName to set
   */
  public void setRepositoryFileName(String repositoryFileName) {
    this.repositoryFileName = repositoryFileName;
  }

  /**
   * @param workDirName the workDirName to set
   */
  public void setWorkDirName(String workDirName) {
    this.workDirName = workDirName;
  }

  /**
   * This default constructor is currently used in Spring-based tests
   */
  public MockRepositoryEventList() {
  }

  /**
   * Looks for the supplied filename on the classpath, and if it can find it,
   * reads the file and parses
   *
   * @param filename
   */
  public MockRepositoryEventList(String filename) {
    eventList = new LinkedList<MockRepositoryEvent>();
    String filePrefix = Context.getInstance().getCommonDirPath();
    File inputFile = new File(filePrefix + filename);
    try {
      LOGGER.info("Base dir path: " + inputFile.getCanonicalPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    initFromFile(inputFile);
  }

  private void initFromFile(File inputFile) {
    InputStream s;
    InputStreamReader isr;
    try {
      s = new FileInputStream(inputFile);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    try {
      isr = new InputStreamReader(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    BufferedReader br = new BufferedReader(isr);
    String line;
    JSONObject jo;
    try {
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) {
          // skip comment lines
          continue;
        }
        try {
          jo = new JSONObject(line);
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
        Map<String, String> properties = new HashMap<String, String>();
        Iterator<?> keys = jo.keys();
        while (keys.hasNext()) {
          String k = (String) keys.next();
          try {
            properties.put(k, jo.getString(k));
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }
        MockRepositoryEvent event = new MockRepositoryEvent(properties);
        eventList.add(event);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public List<MockRepositoryEvent> getEventList() {
    if (eventList != null) {
      return eventList;
    }
    eventList = new LinkedList<MockRepositoryEvent>();
    if (repositoryFileName != null) {
      if (workDirName != null) {
        File workDir = new File(workDirName);
        if (!workDir.exists()) {
          throw new IllegalArgumentException("Specified working directory "
              + workDir.getPath() + " does not exist");
        }
        File repositoryFile = new File(workDir, repositoryFileName);
        initFromFile(repositoryFile);
      } else {
        File repositoryFile = new File(repositoryFileName);
        initFromFile(repositoryFile);
      }
    }
    return eventList;
  }
}
