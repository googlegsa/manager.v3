/*
 * Copyright (C) 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.enterprise.connector.sharepoint.impl;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * 
 * This class processes "Issues" objects in Sharepoint
 */
public class Issue extends List {

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gsf.connector.BaseList#crawl()
   */
  public Issue(ClientContext context, HashMap entries) throws ConnectorException {
    super(context, entries);
  }

  protected String getBaseTypeName() {
    return "issue";
  }

  /**
   * 
   * @return fields that should not be included in the meta data
   */
  protected Hashtable getSkipFields() {
    Hashtable skips = new Hashtable();
    skips.put("IssueID", "1");
    skips.put("RelatedID", "1");
    skips.put("LinkedIssueIDNoMenu", "1");
    skips.put("RemoveRelatedID", "1");
    skips.put("SelectTitle", "1");
    skips.put("RelatedIssue", "1");
    skips.put("owshiddenversion", "1");
    skips.put("_ModerationComments", "1");
    skips.put("Attachments", "1");
    skips.put("Author", "1");
    skips.put("Editor", "1");
    skips.put("_ModerationStatus", "1");
    skips.put("LinkTitleNoMenu", "1");
    skips.put("Edit", "1");
    skips.put("InstanceID", "1");
    skips.put("Order", "1");
    skips.put("GUID", "1");
    return skips;
  }
}
