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

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.ListsStub;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetList;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListResponse;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

/**
 * This class processes "Survey" object type in Sharepoint The "items" in a
 * survey are really responses, that's why we don't wanna feed all items to GSA
 * 
 */
public class Survey extends List {

	private static Log logger = LogFactory.getLog(Survey.class);

	public Survey(ClientContext context, HashMap entries)
			throws ConnectorException {
		super(context, entries);
	}

	protected String getBaseTypeName() {
		return "survey";
	}

	/**
	 * Put a single feed xml out for all lists
	 * 
	 * @throws RemoteException
	 * @throws ParseException
	 * @throws MalformedURLException
	 * @throws SOAPException
	 * @throws IOException
	 */
	protected boolean processNextDoc() throws RemoteException, ParseException {
		if (lists.isEmpty()) {
			return false;
		}
		Iterator it = lists.keySet().iterator();
		boolean anyChanges = false;
		while (it.hasNext()) {
			String key = (String) it.next();
			String title = (String) lists.get(key);
			if (!listChanged(key)) {
				continue;
			}
			anyChanges = true;
			logger.info("Found Survey: " + title);
			PropertyMap map = getList(key);
			currentRecords.add(map);
		}
		lists.clear(); // since getNextDoc is called in a loop, we need to know
						// to
		// break out next time
		if (!anyChanges) {
			return false;
		}
		return true;
	}

	private boolean listChanged(String list) throws ParseException,
			RemoteException {
		String since = Util.getLastAccessTime(list);
		if (since == null || "".equals(since.trim())) {
			return true;
		}
		return processListItemChanges(list, since);
	}

	/**
	 * Process a single list
	 * 
	 * @param name
	 * @param doc
	 * @return
	 * @throws SOAPException
	 * @throws RemoteException
	 * @throws ParseException
	 */
	PropertyMap getList(String name) throws RemoteException, ParseException {
		GetList getList = new GetList();
		getList.setListName(name);
		GetListResponse response = ((ListsStub) getSoap()).GetList(getList);
		OMElement result = response.getGetListResult();
		if (!result.getChildElements().hasNext()) {
			return null;
		}
		OMElement el = (OMElement) result.getChildElements().next();
		String viewURL = el.getAttributeValue(new QName(VIEW_URL));
		viewURL = Util.encodeURL(viewURL);
		SimplePropertyMap pm = new SimplePropertyMap();
		Property nameProp = new SimpleProperty(
				SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(
						ValueType.STRING, viewURL));
		pm.put(SpiConstants.PROPNAME_CONTENTURL, nameProp);
		pm.put(SpiConstants.PROPNAME_DOCID, nameProp);
		// = "Modified" yyyy HH:mm:ss z
		String modifiedTime = el.getAttributeValue(new QName(MODIFIED_TIME));
		modifiedTime = Util.toGSAFormat(modifiedTime);
		nameProp = new SimpleProperty(SpiConstants.PROPNAME_LASTMODIFY,
				new SimpleValue(ValueType.DATE, modifiedTime));
		pm.put(SpiConstants.PROPNAME_LASTMODIFY, nameProp);
		// set meta data
		for (int j = 0; j < META_TAGS.length; ++j) {
			String val = el.getAttributeValue(new QName(META_TAGS[j]));
			if (val == null || val.equals("")) {
				continue;
			}
			nameProp = new SimpleProperty(META_TAGS[j], new SimpleValue(
					ClientContext.getFieldType(META_TAGS[j]), val));
			pm.put(META_TAGS[j], nameProp);
		}
		return pm;
	}

	/**
	 * 
	 * @return fields that should not be included in the meta data
	 */
	protected Hashtable getSkipFields() {
		return null;
	}
}
