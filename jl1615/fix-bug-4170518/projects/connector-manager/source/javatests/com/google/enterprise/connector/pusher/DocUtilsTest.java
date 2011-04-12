// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests some of the {@link DocUtils} methods.
 */
public class DocUtilsTest extends TestCase {

  public final void testGetFeedType() throws RepositoryException {
    Map<String, Object> docProps = new HashMap<String, Object>();

    // Default to content feed.
    docProps.put(SpiConstants.PROPNAME_DOCID, "doc1");
    Document document = ConnectorTestUtils.createSimpleDocument(docProps);
    assertEquals(XmlFeed.XML_FEED_INCREMENTAL, DocUtils.getFeedType(document));

    // Now default to web feed.
    docProps.put(SpiConstants.PROPNAME_SEARCHURL, "http://host/doc1.ext");
    document = ConnectorTestUtils.createSimpleDocument(docProps);
    assertEquals(XmlFeed.XML_FEED_METADATA_AND_URL,
        DocUtils.getFeedType(document));
    
    // Now explicitly set content feed with searchurl.
    docProps.put(SpiConstants.PROPNAME_FEEDTYPE, FeedType.CONTENT.name());
    document = ConnectorTestUtils.createSimpleDocument(docProps);
    assertEquals(XmlFeed.XML_FEED_INCREMENTAL, DocUtils.getFeedType(document));

    // Now explicitly set web feed.
    docProps.put(SpiConstants.PROPNAME_FEEDTYPE, FeedType.WEB.name());
    document = ConnectorTestUtils.createSimpleDocument(docProps);
    assertEquals(XmlFeed.XML_FEED_METADATA_AND_URL,
        DocUtils.getFeedType(document));
    
    // Now test illegal value.  Should go with default behavior.
    docProps.put(SpiConstants.PROPNAME_FEEDTYPE, "BOGUS");
    document = ConnectorTestUtils.createSimpleDocument(docProps);
    assertEquals(XmlFeed.XML_FEED_METADATA_AND_URL,
        DocUtils.getFeedType(document));
  }
}
