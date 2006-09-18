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
 * This class process document objects in Sharepoint
 */
public class Documents extends BaseList {
	private static Log logger = LogFactory.getLog(Documents.class);

	public Documents(ClientContext context, HashMap entries)
			throws ConnectorException {
		super(context, entries);
	}

	protected PropertyMap processItem(OMElement em) {
		String viewURL = em.getAttributeValue(new QName("ows_ServerUrl"));
		// in contract to baseType=0 (pure list), the viewURL here doesn't start
		// with "/"
		String Id = em.getAttributeValue(new QName("ows_ID"));
		viewURL = viewURL.substring(viewURL.indexOf(POUND) + 1);
		if (!viewURL.startsWith("/")) {
			viewURL = "/" + viewURL;
		}
		if (viewURL.indexOf("/_catalogs/") >= 0) {
			return null;
		}
		viewURL = getContext().getServer() + viewURL;
		viewURL = Util.encodeURL(viewURL);
		SimplePropertyMap pm = new SimplePropertyMap();
		Property nameProp = new SimpleProperty(
				SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(
						ValueType.STRING, viewURL));
		pm.put(SpiConstants.PROPNAME_CONTENTURL, nameProp);
		pm.put(SpiConstants.PROPNAME_DOCID, getDocId(Id));
		// = "Modified", EEE d MMM yyyy HH:mm:ss z
		String modifiedTime = em.getAttributeValue(new QName("ows_Modified"));
		try {
			if (modifiedTime.indexOf("#") > 0)
				modifiedTime = modifiedTime
						.substring(modifiedTime.indexOf("#") + 1);
			pm.put(SpiConstants.PROPNAME_LASTMODIFY, getLastModifiedTime(modifiedTime));
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		// set meta data
		Hashtable requiredFields = super.getFields();
		Iterator it = em.getAllAttributes();
		while (it.hasNext()) {
			OMAttribute attr = (OMAttribute) it.next();
			String name = attr.getLocalName();
			if ("ows_ServerUrl".equals(name)) {
				continue;
			} else if ("ows_ID".equals(name)) {
				continue;
			} else if ("ows_Modified".equals(name)) {
				continue;
			} else if (!requiredFields.keySet().contains(name.substring(4))) {
				continue;
			}
			String val = attr.getAttributeValue();
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
		skips.put("_SharedFileIndex", "1");
		skips.put("Modified_x0020_By", "1");
		skips.put("HTML_x0020_File_x0020_Type", "1");
		skips.put("File_x0020_Size", "1");
		skips.put("_SourceUrl", "1");
		skips.put("Created_x0020_By", "1");
		skips.put("_ModerationStatus", "1");
		skips.put("Created_x0020_Date", "1");
		skips.put("Author", "1");
		skips.put("Edit", "1");
		skips.put("BaseName", "1");
		skips.put("LinkFilename", "1");
		skips.put("LinkCheckedOutTitle", "1");
		skips.put("FileLeafRef", "1");
		skips.put("EncodedAbsUrl", "1");
		skips.put("Order", "1");
		skips.put("Editor", "1");
		skips.put("DocIcon", "1");
		skips.put("SelectFilename", "1");
		skips.put("FileDirRef", "1");
		skips.put("SelectTitle", "1");
		skips.put("FileRef", "1");
		skips.put("owshiddenversion", "1");
		skips.put("LinkFilenameNoMenu", "1");
		skips.put("Last_x0020_Modified", "1");
		skips.put("owshiddenversion", "1");
		skips.put("FileSizeDisplay", "1");
		skips.put("File_x0020_Type", "1");
		skips.put("CheckedOutUserId", "1");
		skips.put("FSObjType", "1");
		skips.put("VirusStatus", "1");
		skips.put("_ModerationComments", "1");
		skips.put("CheckedOutTitle", "1");
		// pictures
		skips.put("ImageHeight", "1");
		skips.put("ImageWidth", "1");
		skips.put("ImageSize", "1");
		skips.put("RequiredField", "1");
		skips.put("EncodedAbsThumbnailUrl", "1");
		skips.put("EncodedAbsWebImgUrl", "1");
		return skips;
	}
}
