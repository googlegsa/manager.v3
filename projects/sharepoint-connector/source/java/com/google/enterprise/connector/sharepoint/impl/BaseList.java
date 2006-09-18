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

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.ListsStub;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetList;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListItemChanges;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListItemChangesResponse;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListItems;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListItemsResponse;
import com.google.enterprise.connector.sharepoint.gen.ListsStub.GetListResponse;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

/**
 * generic processing for sharepoint lists, some of the default implementation
 * applies to documents and discussions but not for lists because of the
 * multiple types (doc,discussion etc) share some common features.
 */
public abstract class BaseList extends ConnectorImpl implements Iterator {

	static String _soapAddress = "/_vti_bin/Lists.asmx";

	// tags that will become meta data
	static String META_TAGS[] = { "Title", "Description", "Created", "Version" };

	// attrbutes put in the feed xml
	static String NAME = "Name", MODIFIED_TIME = "Modified",
			VIEW_URL = "DefaultViewUrl";

	static String LIST = "List";

	static String ITEM_OBJ_TYPE = "ows_FSObjType";

	static String POUND = "#";

	private static ListsStub listsStub;

	String currentListTitle, currentListKey;

	HashMap lists = new HashMap();

	Hashtable metaFields = new Hashtable();

	GetListItems getItems;

	Iterator itList = null;

	String paging = null;

	int pageCount = 0;

	Iterator itCurrentRecords = null;

	ArrayList currentRecords = new ArrayList();

	int currentItem = 0;

	HashMap processedList = new HashMap();

	private boolean crawlAll = false, pushFeed = true;

	public static String TYPE_GENERIC_LIST = "GenericList";

	public static String TYPE_DOC = "DocumentLibrary";

	public static String TYPE_DISCUSSION = "DiscussionBoard";

	public static String TYPE_ISSUE = "Issue";

	public static String TYPE_SURVEY = "Survey";

	private static Log logger;

	int totalItems = 0;

	static {
		try {
			logger = LogFactory.getLog(BaseList.class);
			listsStub = new ListsStub();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * 
	 * @param context
	 * @param entries,
	 *            titles and Names of lists
	 * @throws MalformedURLException
	 * @throws ServiceException
	 */
	public BaseList(ClientContext context, HashMap entries)
			throws ConnectorException {
		super.setContext(context);
		try {
			super.setSoap(new ListsStub(), context.getSite() + _soapAddress);
		} catch (Exception e) {
			Util.rethrow("BaseList constructor failed", logger, e);
		}
		setLists(entries);
	}

	/**
	 * process items in a list and append as record nodes in the doc
	 * 
	 * @param list
	 * @param doc
	 * @return 0, no more items 1, has more waiting
	 * @throws RemoteException
	 * @throws ParseException
	 */
	public boolean processFirstPagedItems(String list) throws RemoteException,
			ParseException {
		logger.debug("getListItems " + list);
		getItems = new GetListItems();
		setQuery(getItems);
		getItems.setRowLimit("" + ClientContext.getPageSize());
		list = list.substring(1);
		list = list.substring(0, list.length() - 1);
		getItems.setListName(list);
		// viewFields must be present for sorting to work
		OMElement viewFields = getViewFields();
		getItems.setViewFields(viewFields);
		OMElement op = omfactory.createOMElement("queryOptions", omfactory
				.createOMNamespace(Sharepoint.SP_NAMESPACE,
						Sharepoint.SP_PREFIX));
		getItems.setQueryOptions(op);
		OMElement options = omfactory.createOMElement("QueryOptions", null);
		op.addChild(options);
		OMElement va = omfactory.createOMElement("ViewAttributes", null,
				options);
		OMAttribute attr = omfactory.createOMAttribute("Scope", null,
				"Recursive");
		OMElement nextP = omfactory.createOMElement("Paging", null, options);
		va.addAttribute(attr);
		paging = null;
		return processOnePagedItems(paging);
	}

	boolean processNextPagedItems() throws RemoteException {
		return processOnePagedItems(paging);
	}

	boolean processOnePagedItems(String pagingParam) throws RemoteException {
		if (pagingParam != null) {
			OMElement op = getItems.getQueryOptions();
			OMElement innerOp = (OMElement) op.getChildren().next();
			Iterator it = innerOp.getChildren();
			while (it.hasNext()) {
				OMElement om = (OMElement) it.next();
				if (om.getQName().getLocalPart().equals("Paging")) {
					om.addAttribute("ListItemCollectionPositionNext",
							pagingParam, null);
					break;
				}
			}
			pageCount++;
		}
		GetListItemsResponse response = ((ListsStub) getSoap())
				.GetListItems(getItems);
		OMElement em = response.getGetListItemsResult();
		boolean hasContent = processListResults(em);
		paging = nextPageParam(em);
		return hasContent;
	}

	/**
	 * This is supposed to make a sorted query, but it doesn't work.
	 * 
	 * @return
	 */
	private void setQuery(GetListItems items) {
		OMElement query = omfactory.createOMElement("query", null);
		OMElement Q = omfactory.createOMElement("Query", null, query);
		// Q.setText("<OrderBy&lt;<FieldRef Name=\\\"Modified\"
		// Ascending=\"TRUE\"></FieldRef></OrderBy>");
		// OMElement where = omfactory.createOMElement("Where", null, Q);
		OMElement orderBy = omfactory.createOMElement("OrderBy", null, Q);
		OMElement field = omfactory.createOMElement("FieldRef", null, orderBy);
		field.addAttribute("Name", "Modified", null);
		field.addAttribute("Ascending", "FALSE", null);
		items.setQuery(query);
	}

	protected Hashtable getFields() {
		return metaFields;
	}

	String nextPageParam(OMElement em) {
		Iterator ita = em.getChildElements();
		if (!em.getChildElements().hasNext()) {// result tag
			return null;
		}
		em = (OMElement) ita.next();
		OMElement data = (OMElement) em.getChildElements().next();
		if (!data.getChildElements().hasNext()) {// data tag
			return null;
		}
		OMAttribute attr = data.getAttribute(new QName(
				"ListItemCollectionPositionNext"));
		if (attr != null) {
			return attr.getAttributeValue();
		}
		return null;
	}

	/**
	 * Process a single list. If this is the first time a site is being
	 * retrieved, then get all items of the list; if the site has been
	 * retrieved, then only the changes are captured.
	 * 
	 * @param name,
	 *            name of list
	 * @return DOM document for feeding into GSA
	 * @throws SOAPException
	 * @throws RemoteException
	 * @throws ParseException
	 */
	boolean processListItems(String name) throws RemoteException,
			ParseException {
		// setup document
		String lastAccessTime = Util.getLastAccessTime(name);
		if (getContext().isCrawlAll() || this.isCrawlAll()
				|| lastAccessTime == null || "".equals(lastAccessTime)) {
			// recursively get all items

			logger.debug("Processing " + this.getCurrentListTitle());
			return processFirstPagedItems(name);
			// all the item's time format doesn't have
			// timezone info, and they are not converted to the local timezone
			// that
			// connector is run on
		} else {
			// in this case, all the items' time converted to local timezone
			// where
			// connector is running on, in the ...T...Z format
			logger.debug(this.getCurrentListTitle() + " has been processed");
			return processListItemChanges(name, lastAccessTime);
		}
	}

	/**
	 * Process a single list
	 * 
	 * @param name
	 * @param doc
	 * @return the number of items in a list
	 * @throws SOAPException
	 * @throws RemoteException
	 * @throws ParseException
	 */
	int getListStructure(String name) throws RemoteException, ParseException {
		metaFields.clear();
		GetList getList = new GetList();
		getList.setListName(name);
		GetListResponse response = ((ListsStub) getSoap()).GetList(getList);
		OMElement result = response.getGetListResult();
		if (!result.getChildElements().hasNext()) {
			return 0;
		}
		Iterator ita = result.getChildElements();
		if (!ita.hasNext()) {// List tag
			return 0;
		}
		OMElement em = (OMElement) ita.next();
		String sItemCount = em.getAttribute(new QName("ItemCount"))
				.getAttributeValue();
		int item = Integer.parseInt(sItemCount);
		Util.recordItem(getContext(), item);
		em = (OMElement) em.getChildElements().next(); // fields tag
		Hashtable skips = getSkipFields();
		ita = em.getChildElements(); // field tag
		boolean bSkipAll = (skips.size() == 1 && skips.keySet().contains("All"));
		while (ita.hasNext()) {
			OMElement field = (OMElement) ita.next();
			OMAttribute attr = field.getAttribute(new QName("Name"));
			String fieldName = attr.getAttributeValue();
			if (bSkipAll
					&& (!fieldName.equals(Sharepoint.TAG_ID)
							&& !fieldName.equals(Sharepoint.TAG_MODIFIED) && !fieldName
							.equals(Sharepoint.TAG_URL))) {
				continue;
			}
			if (skips.keySet().contains(fieldName)) {
				continue;
			}
			// now get field type
			OMAttribute typeAttr = field.getAttribute(new QName("Type"));
			String fieldType = typeAttr.getAttributeValue();
			metaFields.put(attr.getAttributeValue(), fieldType);
		}
		ClientContext.getMetaFields().putAll(metaFields);
		totalItems += item;
		return item;
	}

	private String getPagedListTitle() {
		String title = getCurrentListTitle();
		if (pageCount == 0) {
			return title;
		} else {
			return title + "_page_" + pageCount;
		}
	}

	public boolean query() {
		try {
			currentRecords.clear();
			boolean hasContent = processNextDoc();
			if (hasContent) {
				itCurrentRecords = currentRecords.iterator();
			}
			return hasContent;
		} catch (Exception e) {
			Util.processException(logger, e);
			return false;
		}
	}

	/**
	 * Get a single document, a list might be empty, no document, or it might
	 * have too many items, thus more than one doc.
	 * 
	 * @return
	 * @throws RemoteException
	 * @throws ParseException
	 */
	protected boolean processNextDoc() throws RemoteException, ParseException {
		if (paging != null) {
			// there are something unfinished
			processNextPagedItems();
			return true;
		}
		Date lastAccessTime = new Date();
		boolean hasContent = false;
		while (itList.hasNext()) {
			String key = (String) itList.next();
			String title = (String) lists.get(key);
			hasContent = processList(key, title);
			if (hasContent) {
				// there might be something left, that will be processed by next
				// call to getNextDoc
				processedList.put(key, lastAccessTime);
				return true;
			} else {
				Util.saveLastAccessTime(key, lastAccessTime);
			}
		}
		return hasContent;
	}

	private boolean processList(String key, String title)
			throws RemoteException, ParseException {
		logger.debug(getContext().getSite() + " " + title);
		OMDocument doc = null;
		this.setCurrentListKey(key);
		this.setCurrentListTitle(title);
		int itemCount = getListStructure(key);
		// get structure only, or if there are no items
		// Util.recordList(getContext(), title, itemCount);
		if (getContext().isCrawlStructure() || itemCount == 0) {
			return false;
		}
		boolean hasContent = processListItems(key);
		// if no content worth crawling, then null doc is returned
		if (!hasContent) {
			return false;
		}
		// Util.recordCrawledList(1);
		return true;
	}

	public String updateLastAccessTime() throws ParseException {
		// If we are only crawling the structure, we don't mark the time
		if (ClientContext.isCrawlStructure())
			return null;
		int size;
		boolean skipCurrent = false;
		if (itCurrentRecords != null && itCurrentRecords.hasNext()) {
			skipCurrent = true;// current page is not done
		} else if (paging != null) {
			skipCurrent = true; // has next page
		} else {
			skipCurrent = false;
		}
		Iterator keys = processedList.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (skipCurrent && key.equals(getCurrentListKey())) {
				continue;
			}
			Date lastAccessTime = (Date) processedList.get(key);
			Util.saveLastAccessTime(key, lastAccessTime);
		}
		if (skipCurrent) {
			return getCurrentListKey();
		} else {
			return "complete";
		}
	}

	public java.util.List getCurrentRecords() {
		return currentRecords;
	}

	abstract protected PropertyMap processItem(OMElement el);

	/**
	 * 
	 * @return fields that should not be included in the meta data
	 */
	protected abstract Hashtable getSkipFields();

	/**
	 * @return Returns the lists.
	 */
	public HashMap getLists() {
		return lists;
	}

	/**
	 * @param lists
	 *            The lists to set.
	 */
	public void setLists(HashMap lists) {
		this.lists = lists;
		if (lists != null)
			itList = lists.keySet().iterator();
	}

	/**
	 * @return Returns the currentListName.
	 */
	public String getCurrentListTitle() {
		return currentListTitle;
	}

	/**
	 * @param currentListName
	 *            The currentListName to set.
	 */
	public void setCurrentListTitle(String currentListTitle) {
		this.currentListTitle = currentListTitle;
	}

	/**
	 * Returns changed items in a list.
	 * 
	 * @param list
	 * @throws RemoteException
	 * @throws ParseException
	 */
	protected boolean processListItemChanges(String list, String since)
			throws RemoteException, ParseException {
		logger.debug("Inside getListItemChanges");
		GetListItemChanges changes = new GetListItemChanges();
		changes.setListName(list);
		changes.setSince(since);
		changes.setViewFields(getViewFields());
		GetListItemChangesResponse response = ((ListsStub) getSoap())
				.GetListItemChanges(changes);
		OMElement em = response.getGetListItemChangesResult();
		return processListResults(em);
	}

	OMElement getViewFields() {
		logger.debug("get the list of view fields to retrieve");
		// need to nest: <viewFields><ViewFields>, probably Sharepoint bug
		OMElement omfields = omfactory.createOMElement("viewFields", omfactory
				.createOMNamespace(Sharepoint.SP_NAMESPACE,
						Sharepoint.SP_PREFIX));
		OMElement omf = omfactory.createOMElement("ViewFields", null, omfields);
		Iterator it = metaFields.keySet().iterator();
		Hashtable gSkipFields = ClientContext.getSkipFields();
		while (it.hasNext()) {
			String key = (String) it.next();
			// skip globally defined fields in gsc.xml
			if (gSkipFields.keySet().contains(key))
				continue;
			OMElement field = omfactory.createOMElement("FieldRef", null, omf);
			field.addAttribute("Name", key, null);
		}
		return omfields;
	}

	private boolean processListResults(OMElement em) {
		Iterator ita = em.getChildElements();
		if (!em.getChildElements().hasNext()) {// result tag
			return false;
		}
		OMElement data = (OMElement) em.getChildElements().next();
		if (!data.getChildElements().hasNext()) {// data tag
			return false;
		}
		OMElement row = ((OMElement) data.getChildElements().next());
		Iterator it = row.getChildElements();// row
		if (!it.hasNext()) {
			return false;
		}
		PropertyMap map;
		while (it.hasNext()) {
			OMElement item = (OMElement) it.next();
			map = processItem(item);
			if (map != null) {
				currentRecords.add(map);
			}
		}
		return true;
	}

	public boolean hasNext() {
		if (itCurrentRecords != null && itCurrentRecords.hasNext()) {
			return true;
		}
		if (paging != null || itList.hasNext()) {
			try {
				return query();
			} catch (Exception e) {
				logger.error(e.getMessage());
				return false;
			}
		}
		return false;
	}

	public Object next() {
		return itCurrentRecords.next();
	}

	/**
	 * @return Returns the currentListKey.
	 */
	public String getCurrentListKey() {
		return currentListKey;
	}

	/**
	 * @param currentListKey
	 *            The currentListKey to set.
	 */
	public void setCurrentListKey(String currentListKey) {
		this.currentListKey = currentListKey;
	}

	/**
	 * @return Returns the pushFeed.
	 */
	public boolean isPushFeed() {
		return pushFeed;
	}

	/**
	 * @param pushFeed
	 *            The pushFeed to set.
	 */
	public void setPushFeed(boolean pushFeed) {
		this.pushFeed = pushFeed;
	}

	/**
	 * @return Returns the crawlAll.
	 */
	public boolean isCrawlAll() {
		return crawlAll;
	}

	/**
	 * @param crawlAll
	 *            The crawlAll to set.
	 */
	public void setCrawlAll(boolean crawlAll) {
		this.crawlAll = crawlAll;
	}

	public void remove() {
		throw new UnsupportedOperationException("remove not implemented");
	}

	public int getTotalItems() {
		return totalItems;
	}

	protected Property getDocId(String itemId) {
		return new SimpleProperty(SpiConstants.PROPNAME_DOCID, new SimpleValue(
				ValueType.STRING, this.getCurrentListKey() + "-" + itemId));
	}

	protected Property getLastModifiedTime(String last) throws ParseException{
		last = Util.toUniversalFormat(last);
		return new SimpleProperty(SpiConstants.PROPNAME_LASTMODIFY,
				new SimpleValue(ValueType.DATE, last));
	}
}
