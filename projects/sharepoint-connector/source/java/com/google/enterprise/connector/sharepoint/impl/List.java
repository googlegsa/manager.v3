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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

/**
 * Processes baseType = 0 lists, such as contacts Calls Lists Web Services from
 * WSS and returns information back to GSA We need to get all the items because
 * if there are more items that can be returned from one page, GSA can't get to
 * the subsequent pages because of the javascript
 */
public class List extends BaseList {

	private static Log logger = LogFactory.getLog(List.class);

	public List(ClientContext context, HashMap entries)
			throws ConnectorException {
		super(context, entries);
	}

	protected PropertyMap processItem(OMElement em) {
		String title = getCurrentListTitle();
		String viewURL = getContext().getSite() + "/Lists/" + title
				+ "/DispForm.aspx?ID=";
		String Id = em.getAttributeValue(new QName("ows_ID"));
		viewURL = viewURL + Id;
		viewURL = Util.encodeURL(viewURL);
		SimplePropertyMap pm = new SimplePropertyMap();
		Property nameProp = new SimpleProperty(
				SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(ValueType.STRING, viewURL));
		pm.put(SpiConstants.PROPNAME_CONTENTURL, nameProp);
		pm.put(SpiConstants.PROPNAME_DOCID, new SimpleValue(ValueType.STRING, this.getCurrentListKey() + "-" + Id));
		// = "Modified", EEE d MMM yyyy HH:mm:ss z
		String modifiedTime = em.getAttributeValue(new QName("ows_Modified"));
		try {
			if (modifiedTime.indexOf("#") > 0)
				modifiedTime = modifiedTime
						.substring(modifiedTime.indexOf("#") + 1);
			modifiedTime = Util.toGSAFormat(modifiedTime);
			nameProp = new SimpleProperty(SpiConstants.PROPNAME_LASTMODIFY,
					new SimpleValue(ValueType.DATE, modifiedTime));
			pm.put(SpiConstants.PROPNAME_LASTMODIFY, nameProp);
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		Hashtable requiredFields = super.getFields();
		Iterator it = em.getAllAttributes();
		while (it.hasNext()) {
			OMAttribute attr = (OMAttribute) it.next();
			String name = attr.getLocalName();
			if (name.equals("ows_Modified")) {
				continue;
			} else if (name.equals("ows_ID")) {
				continue;
			} else if (!requiredFields.keySet().contains(name.substring(4))) {
				continue;
			}
			String val = attr.getAttributeValue();
			name = name.substring(4);
			nameProp = new SimpleProperty(name, new SimpleValue(ClientContext.getFieldType(name), val));
			pm.put(name, nameProp);
		}
		return pm;
	}

	/**
	 * 
	 * @return fields that should not be included in the meta data For generic
	 *         lists, we skip all the fields except ID & modified
	 */
	protected Hashtable getSkipFields() {
		Hashtable skips = new Hashtable();
		skips.put("All", "All");
		return skips;
	}
}
