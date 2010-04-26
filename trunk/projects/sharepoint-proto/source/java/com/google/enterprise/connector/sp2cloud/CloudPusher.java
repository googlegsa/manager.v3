// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;

import java.io.InputStream;

public interface CloudPusher {
	void pushDocument(Document document, FolderInfo parent, String owner, 
	    AclAdjustments aclAdjustments, InputStream inputStream) throws Exception;
    String pushFolder(String folderName, FolderInfo parent, String owner, 
        AclAdjustments aclAdjustments) throws Exception;
}
