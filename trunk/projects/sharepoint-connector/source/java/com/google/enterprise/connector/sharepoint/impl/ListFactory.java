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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.SiteDataStub;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub.GetListCollection;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub.GetListCollectionResponse;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub._sList;

public class ListFactory extends ConnectorImpl {

	String _soapAddress = "/_vti_bin/SiteData.asmx";

	static ArrayList factories = new ArrayList();

	ArrayList baseLists = new ArrayList();

	private static Log logger = LogFactory.getLog(ListFactory.class);

	public ListFactory(ClientContext context) throws ConnectorException {
		super.setContext(context);
		try {
			super.setSoap(new SiteDataStub(), context.getSite() + _soapAddress);
		} catch (Exception e) {
			Util.rethrow("ListFactory constructor failed", logger, e);
		}
		synchronized (factories) {
			factories.add(this);
		}
	}

	private HashMap discoverLists(ClientContext context) {
		HashMap lists = new HashMap();
		try {
			SiteDataStub stub = ((SiteDataStub) getSoap());
			GetListCollectionResponse resp = stub
					.GetListCollection(new GetListCollection());
			_sList[] sList = resp.getVLists().get_sList();
			if (sList == null) {
				return lists;
			}
			for (int i = 0; i < sList.length; ++i) {
				String defaultViewURL = sList[i].getDefaultViewUrl();
				// some internal lists are retrieved but can't be accessed
				// we don't want to try
				if (defaultViewURL == null || "".equals(defaultViewURL)) {
					continue;
				}
				String name = sList[i].getInternalName();
				String lastAccessTime = Util.getLastAccessTime(name);
				// whether user wants to crawl everything
				if (!ClientContext.isCrawlAll()
						&& !ClientContext.isCrawlStructure()) {
					if (lastAccessTime != null && !"".equals(lastAccessTime)) {
						Date last = Util.stringToTime(lastAccessTime);
						// this time is adjusted to local time of the client
						// (connector)when
						// returned by Sharepoint through web service
						String sModified = sList[i].getLastModified();
						Date modified = Util.stringToTime(sModified);
						if (last.after(modified)) {
							continue;
						}
					}
				}
				String type = sList[i].getBaseType();
				if (lists.get(type) == null) {
					lists.put(type, new HashMap());
				}
				String title = sList[i].getTitle();
				((HashMap) lists.get(type)).put(name, title);
			}
	//		Util.recordList(context, sList.length);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return lists;
	}

	void crawlLists(ClientContext context, HashMap lists) {
		BaseList conn = null;
		Iterator it = lists.keySet().iterator();
		while (it.hasNext()) {
			try {
				String baseType = (String) it.next();
				HashMap entries = (HashMap) lists.get(baseType);
				if (baseType.equals(BaseList.TYPE_GENERIC_LIST)) {
					conn = new List(context, entries);
				} else if (baseType.equals(BaseList.TYPE_DOC)) {
					conn = new Documents(context, entries);
				} else if (baseType.equals(BaseList.TYPE_DISCUSSION)) {
					conn = new Discussions(context, entries);
				} else if (baseType.equals(BaseList.TYPE_SURVEY)) {
					conn = new Survey(context, entries);
				} else if (baseType.equals(BaseList.TYPE_ISSUE)) {
					conn = new Issue(context, entries);
				} else {
					String unexpected = "";
					Iterator extraLists = entries.values().iterator();
					while (extraLists.hasNext()) {
						unexpected += ", ";
					}
					throw new ConnectorException("Unexpected BaseType = "
							+ baseType + ", list names: " + unexpected);
				}
				if (conn == null) {
					continue;
				}
				baseLists.add(conn);
			} catch (ConnectorException e) {

			}
		}

	}

	public void crawlListByType(String sType) {
		ClientContext context = getContext();
		HashMap lists = discoverLists(context);
		BaseList conn = null;
		Iterator it = lists.keySet().iterator();
		while (it.hasNext()) {
			try {
				String baseType = (String) it.next();
				HashMap entries = (HashMap) lists.get(baseType);
				if (!baseType.equals(sType)) {
					continue;
				}
				if (sType.equals(BaseList.TYPE_GENERIC_LIST)) {
					conn = new List(context, entries);
				} else if (sType.equals(BaseList.TYPE_DOC)) {
					conn = new Documents(context, entries);
				} else if (sType.equals(BaseList.TYPE_DISCUSSION)) {
					conn = new Discussions(context, entries);
				} else if (sType.equals(BaseList.TYPE_SURVEY)) {
					conn = new Survey(context, entries);
				} else if (sType.equals(BaseList.TYPE_ISSUE)) {
					conn = new Issue(context, entries);
				} else {
					String unexpected = "";
					Iterator extraLists = entries.values().iterator();
					while (extraLists.hasNext()) {
						unexpected += ", ";
					}
					throw new ConnectorException("Unexpected BaseType = "
							+ baseType + ", list names: " + unexpected);
				}
				if (conn == null) {
					continue;
				}
				conn.query();
			} catch (ConnectorException e) {
				Util.processException(logger, e);
			}
		}
	}

	public ArrayList getLists() {
		return baseLists;
	}

	public void crawl() {
		ClientContext context = getContext();
		HashMap lists = discoverLists(context);
		crawlLists(context, lists);
	}

	public static ArrayList getListFactories() {
		return factories;
	}
}
