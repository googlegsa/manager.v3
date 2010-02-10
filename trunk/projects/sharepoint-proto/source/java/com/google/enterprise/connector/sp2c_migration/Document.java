package com.google.enterprise.connector.sp2c_migration;

import java.util.List;

/**
 * Holder object for a sharepoint document's meta-data.
 * @author strellis
 */
public class Document extends DirEntry {
	private final String mimeType;

    public Document(String name, String id, String parentId, List<Ace> acl,
			String owner, String mimeType) {
		super(name, id, parentId, acl, owner);
		this.mimeType = mimeType;
	}

    public String getMimeType() {
		return mimeType;
	}
}
