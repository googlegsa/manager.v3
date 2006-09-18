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
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Discussions extends BaseList {
	private static Log logger = LogFactory.getLog(Discussions.class);

	public Discussions(ClientContext context, HashMap entries)
			throws ConnectorException {
		super(context, entries);
	}

	protected PropertyMap processItem(OMElement em) {
		String dissId = em.getAttributeValue(new QName("ows_ID"));
		String viewURL = getContext().getSite() + "/Lists/"
				+ getCurrentListTitle() + "/DispForm.aspx?ID=" + dissId;
		viewURL = Util.encodeURL(viewURL);
		SimplePropertyMap pm = new SimplePropertyMap();
		Property nameProp = new SimpleProperty(
				SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(
						ValueType.STRING, viewURL));
		pm.put(SpiConstants.PROPNAME_CONTENTURL, nameProp);
		pm.put(SpiConstants.PROPNAME_DOCID, getDocId(dissId));

		// = "Modified", EEE d MMM yyyy HH:mm:ss z
		String modifiedTime = em.getAttributeValue(new QName("ows_Modified"));
		try {
			pm.put(SpiConstants.PROPNAME_LASTMODIFY, getLastModifiedTime(modifiedTime));
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		// Don't know how to discover MIME_TYPE, but seems GSA can deal with
		// non-text
		// set meta data
		Iterator it = em.getAllAttributes();
		Hashtable requiredFields = super.getFields();
		while (it.hasNext()) {
			OMAttribute attr = (OMAttribute) it.next();
			String name = attr.getLocalName();
			if ("ows_Modified".equals(name)) {
				continue;
			} else if (!requiredFields.keySet().contains(name.substring(4))) {
				continue;
			}
			String val = attr.getAttributeValue();
			// get rid of "ows_"
			name = name.substring(4);
			nameProp = new SimpleProperty(name, new SimpleValue(ClientContext
					.getFieldType(name), val));
			pm.put(name, nameProp);
		}
		return pm;
	}

	/**
	 * 
	 * @return fields that should not be included in the meta data
	 */
	protected Hashtable getSkipFields() {
		Hashtable skips = new Hashtable();
		skips.put("ThreadID", "1");
		skips.put("owshiddenversion", "1");
		skips.put("_ModerationComments", "1");
		skips.put("Attachments", "1");
		skips.put("ThreadingNoIndent", "1");
		skips.put("Author", "1");
		skips.put("Editor", "1");
		skips.put("_ModerationStatus", "1");
		skips.put("Body", "1");
		skips.put("Reply", "1");
		skips.put("Edit", "1");
		skips.put("Threading", "1");
		skips.put("InstanceID", "1");
		skips.put("Ordering", "1");
		skips.put("Order", "1");
		skips.put("GUID", "1");
		return skips;
	}
}
