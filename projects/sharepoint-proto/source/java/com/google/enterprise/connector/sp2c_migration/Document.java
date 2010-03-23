package com.google.enterprise.connector.sp2c_migration;

import java.util.List;

/**
 * Holder object for a sharepoint document's meta-data.
 * @author strellis
 */
public class Document extends DirEntry {
	private String mimeType;

    // Document URL is required to download the document's content.
	private String docUrl;

    public Document(String name, String id, String parentId, List<Ace> acl,
			String owner, String mimeType, String docUrl) {
		super(name, id, parentId, acl, owner);
		this.mimeType = mimeType;
		this.docUrl = docUrl;
	}

    public String getMimeType() {
		return mimeType;
	}

    public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

    public String getDocUrl() {
		return docUrl;
	}
    
    public Document fixDocumentOwner(String owner) {
        return new Document(getName(), getId(), getParentId(), getAcl(), owner, mimeType, docUrl);
    }

    @Override
	public int hashCode() {
		if (null == getName()) {
			return super.hashCode();
		} else if (null == getParentId()) {
			return getName().length();
		} else {
			return 19 * getName().length() + getParentId().length();
		}
	}

    @Override
	public boolean equals(Object obj) {
		if (null == getId() || !(obj instanceof Document)) {
			return false;
		}
		Document folder = (Document) obj;
		return getId().equals(folder.getId());
	}

}
