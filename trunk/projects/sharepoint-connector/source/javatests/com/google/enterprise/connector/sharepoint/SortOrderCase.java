package com.google.enterprise.connector.sharepoint;

import java.util.Calendar;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.SpiConstants;

public class SortOrderCase extends TestCase implements ICase {
	PropertyMap prev = null;

	public void perform(ClientContext context, Object obj) {
		// TODO Auto-generated method stub
		PropertyMap current = (PropertyMap) obj;
		try {
			if (prev == null) {
				prev = current;
				return;
			}
			String prevId = prev.getProperty(SpiConstants.PROPNAME_DOCID)
					.getValue().getString();
			String currId = current.getProperty(SpiConstants.PROPNAME_DOCID)
					.getValue().getString();
			this.assertNotSame(prevId, currId);
			String prevListId = prevId.substring(0, prevId.indexOf("}"));
			String currListId = currId.substring(0, currId.indexOf("}"));
			if (!prevListId.equals(currListId)) {
				prev = current;
			}
			Calendar prevDate = prev.getProperty(
					SpiConstants.PROPNAME_LASTMODIFY).getValue().getDate();
			Calendar currDate = prev.getProperty(
					SpiConstants.PROPNAME_LASTMODIFY).getValue().getDate();
			this.assertTrue(prevDate.before(currDate)
					|| prevDate.equals(currDate));
		} catch (Exception e) {
			this.assertFalse(true);
		}
	}
}
