package com.google.enterprise.connector.sharepoint;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.BaseList;
import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ListFactory;
import com.google.enterprise.connector.sharepoint.impl.Sharepoint;
import com.google.enterprise.connector.sharepoint.impl.Util;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

/**
 * The underlying implementation of the iterator goes over multiple list
 * factories and lists. After each list is processed, "lastAccessTime" is kept.
 * How do I know a list is processed? Since the "resultset" iterator goes across
 * multiple lists, only when the "next()" is trying to retrieve something from
 * the next list, will I know for sure that the items from current list have all
 * been retrieved. That's the time I "flush" it.
 * 
 * @author jeffling
 * 
 */
public class SharepointResultSet implements ResultSet {

	SharepointIterator iterator = new SharepointIterator();

	private static Log logger = LogFactory.getLog(SharepointResultSet.class);

	public SharepointResultSet() {
	}

	public Iterator iterator() throws RepositoryException {
		return iterator;
	}

	public ClientContext getContext() {
		return iterator.getContext();
	}

	private class SharepointIterator implements Iterator {
		Iterator lists = null;

		ClientContext context;

		BaseList list;

		int currentFactory = 0;

		/**
		 * There are two loops: one is an array of ListFactory, collected from
		 * multiple sites; For each ListFactory, there is another array of
		 * "lists"
		 * 
		 * @return ResultSet, representing a single list
		 */
		private boolean moveToNextList() {
			while (true) {
				// see if the current listfactory has any list left
				while (true) {
					list = getList();
					if (list != null) {
						// now this list has an item
						if (list.hasNext())
							return true;
					} else {// run out of list in this factory
						break;
					}
				}
				// now move to next ListFactory
				ArrayList factories = ListFactory.getListFactories();
				ListFactory fac = null;
				if (currentFactory >= factories.size()) {
					return false;
				}
				fac = (ListFactory) factories.get(currentFactory);
				currentFactory++;
				if (fac == null) {
					return false;
				}
				lists = fac.getLists().iterator();
				context = fac.getContext();
			}
		}

		/**
		 * Get each list in the lists of a single factory in turn
		 * 
		 * @return
		 */
		private BaseList getList() {
			if (lists != null) {
				if (!lists.hasNext()) {
					return null;
				}
				return (BaseList) lists.next();
			}
			return null;
		}

		public Object next() {
			return list.next();
		}

		public boolean hasNext() {
			if (list != null && list.hasNext()) {
				return true;
			}
			boolean hasNext = moveToNextList();
			if (!hasNext) {
				return moveToNextList();
			}
			return hasNext;
		}

		public void remove() {
			throw new UnsupportedOperationException("remove not implemented");
		}

		public ClientContext getContext() {
			return context;
		}

	}
}
