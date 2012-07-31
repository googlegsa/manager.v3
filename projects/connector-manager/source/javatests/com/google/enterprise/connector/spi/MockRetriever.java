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

import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * A mock implementation for the Retriever interface.
 */
public class MockRetriever implements Retriever {

  /** A docid that has no content. */
  public static final String DOCID_NO_CONTENT = "noContent";

  /** A docid that has empty content. */
  public static final String DOCID_EMPTY_CONTENT = "emptyContent";

  /** A docid that has no last modified date. */
  public static final String DOCID_NO_LASTMODIFIED = "noLastModified";

  /** A docid that has no mime type. */
  public static final String DOCID_NO_MIMETYPE = "noMimeType";

  /** A docid that is not found. */
  public static final String DOCID_NOT_FOUND = "nonexistent";

  /** A docid that has insufficient permissions. */
  public static final String DOCID_NO_ACCESS = "topSecret";

  /** A docid that throws RepositoryExeception. */
  public static final String DOCID_REPOSITORY_EXCEPTION = "repositoryError";

  /* @Override */
  public InputStream getContent(String docid) throws RepositoryException {
    if (DOCID_NOT_FOUND.equals(docid)) {
      throw new DocumentNotFoundException("Not Found");
    } else if (DOCID_NO_ACCESS.equals(docid)) {
      throw new DocumentAccessException("Access Denied");
    } else if (DOCID_REPOSITORY_EXCEPTION.equals(docid)) {
      throw new RepositoryException("Repository Error");
    } else if (DOCID_NO_CONTENT.equals(docid)) {
      return null;
    } else if (DOCID_EMPTY_CONTENT.equals(docid)) {
      return new ByteArrayInputStream("".getBytes());
    } else {
      return new ByteArrayInputStream(docid.getBytes());
    }
  }

  /* @Override */
  public Document getMetaData(String docid) throws RepositoryException {
    if (DOCID_NOT_FOUND.equals(docid)) {
      throw new DocumentNotFoundException("Not Found");
    } else if (DOCID_NO_ACCESS.equals(docid)) {
      throw new DocumentAccessException("Access Denied");
    } else if (DOCID_REPOSITORY_EXCEPTION.equals(docid)) {
      throw new RepositoryException("Repository Error");
    } else if (DOCID_NO_LASTMODIFIED.equals(docid)) {
      Map<String, Object> props =
          ConnectorTestUtils.createSimpleDocumentBasicProperties(docid);
      props.remove(SpiConstants.PROPNAME_CONTENT);
      props.remove(SpiConstants.PROPNAME_LASTMODIFIED);
      props.remove(SpiConstants.PROPNAME_MIMETYPE);
      return ConnectorTestUtils.createSimpleDocument(props);
    } else if (DOCID_NO_MIMETYPE.equals(docid)) {
      Map<String, Object> props =
          ConnectorTestUtils.createSimpleDocumentBasicProperties(docid);
      props.remove(SpiConstants.PROPNAME_MIMETYPE);
      return ConnectorTestUtils.createSimpleDocument(props);
    } else {
      Map<String, Object> props =
          ConnectorTestUtils.createSimpleDocumentBasicProperties(docid);
      props.remove(SpiConstants.PROPNAME_CONTENT);
      return ConnectorTestUtils.createSimpleDocument(props);
    }
  }
}
