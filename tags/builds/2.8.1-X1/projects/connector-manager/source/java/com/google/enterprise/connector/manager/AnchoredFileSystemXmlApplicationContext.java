// Copyright 2010 Google Inc.
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

import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * A {@link FileSystemXmlApplicationContext} whose File resources are
 * relative to a base directory, rather the the current working directory.
 */
class AnchoredFileSystemXmlApplicationContext
    extends FileSystemXmlApplicationContext {

  private final File baseDirectory;

  /**
   * Create a new AnchoredFileSystemXmlApplicationContext, loading the
   * definitions from the given XML file and automatically refreshing
   * the context.
   *
   * @param baseDirectory base directory for relative file paths
   * @param configLocation file path
   * @throws BeansException if context creation failed
   */
  public AnchoredFileSystemXmlApplicationContext(String baseDirectory,
      String configLocation) throws BeansException {
    this(baseDirectory, new String[] {configLocation});
  }

  /**
   * Create a new AnchoredFileSystemXmlApplicationContext, loading the
   * definitions from the given XML files and automatically refreshing
   * the context.
   *
   * @param baseDirectory base directory for relative file paths
   * @param configLocations array of file paths
   * @throws BeansException if context creation failed
   */
  public AnchoredFileSystemXmlApplicationContext(String baseDirectory,
      String[] configLocations) throws BeansException {
    this.baseDirectory = new File(baseDirectory);
    setConfigLocations(configLocations);
    refresh();
  }

  /**
   * Returns an array of resource locations, referring to the XML bean
   * definition files that this context is built with.
   */
  // Override to increase visibility from protected to public.
  @Override
  public String[] getConfigLocations() {
    return super.getConfigLocations();
  }

  /**
   * Resolve resource paths as file system paths.
   * <p>Note: Even if a given path starts with a slash, it will get
   * interpreted as relative to the baseDirectory.
   * This is consistent with the semantics in a Servlet container.
   *
   * @param path path to the resource
   * @return Resource handle
   */
  @Override
  protected Resource getResourceByPath(String path) {
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }
    if (new File(path).isAbsolute()) {
      // We could still get an absolute path via the file: url work-around.
      return new FileSystemResource(path);
    } else {
      return new FileSystemResource(new File(baseDirectory, path));
    }
  }
}
