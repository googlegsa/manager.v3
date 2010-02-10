package com.google.enterprise.connector.sp2cloud;

import java.io.InputStream;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;

public interface CloudPusher {
	void pushFolder(Folder parent, Folder folder) throws Exception;
	void pushDocument(Folder parent, Document document, InputStream inputStream) throws Exception;
}
