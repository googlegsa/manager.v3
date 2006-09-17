/**
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

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.ListsStub;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListCollection;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListCollectionResponse;
import com.google.enterprise.connector.spi.PropertyMap;

/**
 * This class handles SPS sites (either linked or created)
 * 
 */
public class SiteDirectory extends BaseList {

  private static Log logger = LogFactory.getLog(ConnectorImpl.class);

  public SiteDirectory(ClientContext context) throws ConnectorException {
    super(context, null);
    // don't push the "list" discovered here
    this.setPushFeed(false);
    // always get all the sites and crawl them
    this.setCrawlAll(true);
  }

  /**
   * Find the URL of each SPS created top site as well as sites that are linked
   * into SPS site, and crawl it
   */
  protected PropertyMap processItem(OMElement em) {
    OMElement record = omfactory.createOMElement(
      ConnectorConstants.FEEDER_RECORD, null);
    String siteURL = em.getAttributeValue(new QName("ows_SiteURL"));
    // will clone a new context to process this site
    ClientContext context = (ClientContext) getContext().clone();
    String rootURL = context.getSite();
    // this is an absolute URL
    if (siteURL.startsWith("http")) {
      try {
        context.processServerName(siteURL);
      } catch (UnknownHostException e) {
        logger.error("Can't process the site discovered from SPS site: "
          + rootURL);
        logger.error(e.getMessage());
      }
    } else {
      rootURL = rootURL.replaceAll(Sharepoint.SiteDirectoryTag, "");
      if (siteURL.endsWith("/")) {
        siteURL = siteURL.substring(0, siteURL.length() - 1);
      }
      if (rootURL.endsWith("/")) {
        rootURL = rootURL.substring(0, rootURL.length() - 1);
      }
      context.setSite(rootURL + siteURL);
    }
    try {
      WebFactory factory = new WebFactory(context);
      factory.crawl();
    } catch (ConnectorException e) {
      logger.error(e.getMessage());
    }
    return null;
  }

  public boolean discoverLists() {
    HashMap[] lists = new HashMap[6];
    try {
      GetListCollectionResponse response = ((ListsStub) getSoap())
        .GetListCollection(new GetListCollection());
      OMElement els = response.getGetListCollectionResult();
      Iterator it = els.getChildren();
      if (!it.hasNext()) { // Lists
        return false; //no lists
      }
      Iterator listIt = ((OMElement) it.next()).getChildElements();
      // super.getListItems(list);
      HashMap entries = new HashMap();
      // There should be only one list
      while (listIt.hasNext()) {
        OMElement el = (OMElement) listIt.next();
        String name = el.getAttributeValue(new QName("Name"));
        String title = el.getAttributeValue(new QName("Title"));
        entries.put(name, title);
      }
      this.setLists(entries);
    } catch (Exception e) {
      Util.processException(logger, e);
      return false;
    }
    return true;
  }

  protected Hashtable getSkipFields() {
    return new Hashtable();
  }

}
