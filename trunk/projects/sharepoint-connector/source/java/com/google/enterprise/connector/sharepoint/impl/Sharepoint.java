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

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.UserProfileServiceStub;
import com.google.enterprise.connector.sharepoint.gen.UserProfileServiceStub.GetUserProfileByIndex;

/**
 * 
 * For Windows Sharepoint Services
 */
public class Sharepoint {

  public static String TAG_ID = "ID";
  public static String TAG_MODIFIED = "Modified";
  public static String TAG_URL = "ServerUrl";
  //
  public static String SP_NAMESPACE = "http://schemas.microsoft.com/sharepoint/soap/";
  public static String SP_PREFIX = "SOAPSDK9";

  private static Log logger = LogFactory.getLog(Sharepoint.class);
  protected static String SiteDirectoryTag = "SiteDirectory";
  ClientContext mainContext = null;
  int serverIndex;
  public Sharepoint(int index) throws UnknownHostException {
    serverIndex = index;
    mainContext = new ClientContext(serverIndex);
  }

  public Sharepoint(String name) throws UnknownHostException {
    mainContext = new ClientContext(name);
  }

  public Sharepoint(ClientContext context) throws UnknownHostException {
    mainContext = context;
  }

  /**
   * This method gets all the sites of this server, and crawls them
   * 
   */

  public void processWSS() {
    try {
      WebFactory factory = new WebFactory(mainContext);
      factory.crawl();
    } catch (ConnectorException e) {
      logger.error(e.getMessage());
    }
  }

  /*
   * process SPS unique features including personal sites, public site
   * directories
   * 
   */
  void processSPS() {
    // process the SPS as a regular WSS site
    processWSS();
    // find all personal sites
    User user = null;
    try {
      user = new User(serverIndex);
      user.crawl();
    } catch (Exception e) {
      Util.processException(logger, e);
    } // crawl personal sites
    if (user != null) {
      ClientContext context = (ClientContext) mainContext.clone();
      HashMap accounts = user.getAccounts();
      logger.info("Total " + accounts.size()
        + " user accounts, start crawling personal sites");
      Iterator sites = accounts.values().iterator();
      String rootSite = context.getSite();
      while (sites.hasNext()) {
        try {
          String site = rootSite + (String) sites.next();
          if (site.endsWith("/")) {
            site = site.substring(0, site.length() - 1);
          }
          context.setSite(site);
          WebFactory factory = new WebFactory(context);
          factory.crawl();
        } catch (ConnectorException e) {
          logger.error(e.getMessage());
        }
      }
    }
    crawlSPSLinkedSites();
  }

  void crawlSPSLinkedSites() {
    logger.info("Start crawling SPS linked sites");
    ClientContext context = (ClientContext) mainContext.clone();
    String rootSite = context.getSite();
    context.setSite(rootSite + "/" + SiteDirectoryTag);
    try {
      SiteDirectory dir = new SiteDirectory(context);
      if (dir.discoverLists()) {
        dir.query();
      }
    } catch (ConnectorException e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * use httpclient to find whether this is a SPS, or WSS site
   * 
   * @return
   */
  boolean isSPS() {
    try {
      User user = new User(serverIndex);
      UserProfileServiceStub stub = (UserProfileServiceStub) user.getSoap();
      GetUserProfileByIndex getUserProfileByIndex = new GetUserProfileByIndex();
      getUserProfileByIndex.setIndex(0);
      stub.GetUserProfileByIndex(getUserProfileByIndex);
      logger.info("It's a SPS site");
      return true;
    } catch (AxisFault fault) {
      logger.info("It's a WSS site");
      return false;
    } catch (Exception e) {
      logger.error(e.getMessage());
      return false;
    }
  }

  public void crawl() throws Exception {
    Date dt = new Date();
    if (isSPS()) {
      processSPS();
    } else {
      processWSS();
    }
    if (ClientContext.getMode() == ConnectorConstants.MODE_FEED) {
      /*
       * ConnectorPool pool = Util.getThreadPool(); do {
       * Thread.currentThread().sleep(2000); } while (pool.getActiveCount() >
       * 0); pool.safeShutDown();
       */
      Util.report();
    }
  }

  public void saveMetaFields() {
    Hashtable metafields = ClientContext.getMetaFields();
    String meta = "";
    // remove these three special meaning fields 'cause they are not metadata
    // but required fields
    metafields.remove(TAG_ID);
    metafields.remove(TAG_MODIFIED);
    metafields.remove(TAG_URL);

    Iterator it = metafields.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      meta += key + ",";
    }
    ClientContext.saveMetaFields(meta);
  }
}
