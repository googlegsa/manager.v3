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

package com.google.enterprise.connector.util.diffing.testing;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Utility methods for tests to access temporary directories.
 */
public class TestDirectoryManager  {
  private final File tmpDir;
  public TestDirectoryManager(TestCase testCase) throws IOException {
    String baseTmpDir = System.getProperty("java.io.tmpdir");
    File parent = new File(baseTmpDir).getAbsoluteFile();
    parent = new File(parent, testCase.getClass().getSimpleName());
    parent = new File(parent, "tmp");
    tmpDir = new File(parent, "d-" + UUID.randomUUID().toString());
    if (this.tmpDir.exists()) {
      Files.deleteRecursively(tmpDir);
    }
    if (!tmpDir.mkdirs()) {
      throw new IOException("can't create test dir: " + tmpDir);
    }
  }

  /**
   * Creates a temporary directory for use by a test.
   */
  public final File makeDirectory(String name) throws IOException {
    File result = new File(tmpDir, name);
    if (!result.mkdirs()) {
      throw new IOException("Failed to make directory " + result.getAbsolutePath());
    }
    return result;
  }

  /**
   * Overwrites a text file with the UTF-8 encoding of a String.
   *
   * @param relativePath  path to the file to overwrite, relative to the
   *        temporary directory for this test case.  Paths containing '.' or
   *        '..' may have surprising results and should be avoided.
   */
  public final File writeFile(String relativePath, String contents)
      throws IOException {
    File fileToWrite = new File(tmpDir, relativePath);
    Files.write(contents, fileToWrite, Charsets.UTF_8);
    return fileToWrite;
  }
}