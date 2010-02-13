package com.google.enterprise.connector.sp2c_migration;

import java.util.List;

/**
 * Holder object for a sharepoint folder's meta-data.
 *
 * @author strellis
 * @author nitendra_thakur
 */
public class Folder extends DirEntry {
	/**
	 * Root folder depicts a document library directly under a SharePoint site.
	 * A document library is used to store document in SharePoint
	 */
	private boolean isRootFolder;

  public Folder(String name, String id, String parentId, List<Ace> acl,
			String owner, boolean isRootFolder) {
		super(name, id, parentId, acl, owner);
		this.isRootFolder = isRootFolder;
	}

    /**
	 * Identifies if the folder is a root folder in the SharePoint site
	 *
	 * @return true is folder is a root folder; false, otherwise
	 */
    public boolean isRootFolder() {
		return isRootFolder;
	}

    @Override
	public int hashCode() {
		if (null == getName()) {
			return super.hashCode();
		} else if (null == getParentId()) {
			return getName().length();
		} else {
			return 17 * getName().length() + getParentId().length();
		}
	}

    @Override
	public boolean equals(Object obj) {
		if (null == getId() || !(obj instanceof Folder)) {
			return false;
		}
		Folder folder = (Folder) obj;
		return getId().equals(folder.getId());
    }
}
