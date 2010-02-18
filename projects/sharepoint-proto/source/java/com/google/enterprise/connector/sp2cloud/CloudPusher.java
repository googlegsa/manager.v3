package com.google.enterprise.connector.sp2cloud;

import java.io.InputStream;
import java.util.List;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;

public interface CloudPusher {
	void pushFolder(Folder parent, Folder folder, List<CloudAce> cloudAcl) throws Exception;
	void pushDocument(Folder parent, Document document, List<CloudAce> cloudAcl, 
	    InputStream inputStream) throws Exception;
}
