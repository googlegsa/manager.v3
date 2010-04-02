package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;

import java.io.InputStream;

public interface CloudPusher {
	void pushDocument(Document document, CloudAcl cloudAcl, 
	    InputStream inputStream) throws Exception;
    void pushFolder(FolderInfo folderInfo) throws Exception;
}
