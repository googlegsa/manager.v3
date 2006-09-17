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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.UserProfileServiceStub;
import com.google.enterprise.connector.sharepoint.gen.UserProfileServiceStub.GetUserProfileByIndex;
import com.google.enterprise.connector.sharepoint.gen.UserProfileServiceStub.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.gen.UserProfileServiceStub.PropertyData;

/**
 * This class is for processing SPS' user accounts;
 * 
 * 
 */
public class User extends ConnectorImpl {

  static String _soapAddress = "/_vti_bin/UserProfileService.asmx";
  static String personalSpaceTag = "PersonalSpace",
      accountNameTag = "AccountName";
  private static Log logger = LogFactory.getLog(User.class);
  private HashMap accounts = new HashMap();

  public User(int index) throws ConnectorException {
    try {
      ClientContext context = new ClientContext(index);
      super.setContext(context);
      super.setSoap(
        new UserProfileServiceStub(context.getSite() + _soapAddress), context
          .getSite()
          + _soapAddress);
    } catch (Exception e) {
      Util.rethrow("User constructor failed", logger, e);
    }
  }

  /**
   * Starting point for the thread
   */
  public void run() {
    crawl();
  }

  /**
   * Stores all user accounts in a hashmap, for later retrieval of personal
   * sites
   */
  public void crawl() {
    UserProfileServiceStub stub = (UserProfileServiceStub) super.getSoap();
    int index = 0;
    while (index >= 0) {
      GetUserProfileByIndex getUserProfileByIndex = new GetUserProfileByIndex();
      getUserProfileByIndex.setIndex(index);
      GetUserProfileByIndexResult result = null;
      try {
        result = stub.GetUserProfileByIndex(getUserProfileByIndex)
          .getGetUserProfileByIndexResult();
      } catch (Exception e) {

      }
      if (result == null || result.getUserProfile() == null) {
        break;
      }
      PropertyData data[] = result.getUserProfile().getPropertyData();
      if (data == null) {
        break;
      }
      String acct = null, space = null;
      for (int i = 0; i < data.length; ++i) {
        String name = data[i].getName();
        if (personalSpaceTag.equals(name)) {
          space = data[i].getValue();
        } else if (accountNameTag.equals(name)) {
          acct = data[i].getValue();
        }
      }
      accounts.put(acct, space);
      String next = result.getNextValue();
      index = Integer.parseInt(next);
    }
  }

  /**
   * @return Returns the accounts.
   */
  public HashMap getAccounts() {
    return accounts;
  }
}
