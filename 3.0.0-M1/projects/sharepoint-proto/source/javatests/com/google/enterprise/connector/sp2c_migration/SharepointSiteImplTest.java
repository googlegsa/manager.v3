package com.google.enterprise.connector.sp2c_migration;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.enterprise.connector.sp2c_migration.SharepointSiteFactory;

//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

public class SharepointSiteImplTest extends TestCase {

    public void testFolders() throws Exception {
		SharepointSite spSite = SharepointSiteFactory.getSharepointSite("http://ent-test-w2k3-sp2007/migration", "administrator", "test", "");
        List<Folder> rootFolders = spSite.getRootFolders();
        for (Folder rootFolder : rootFolders) {
            System.out.println("Root folder: " + rootFolder);
            List<Folder> folders = spSite.getFolders(rootFolder);
            System.out.println(folders);
            List<Document> documents = spSite.getDocuments(rootFolder);
            if (!documents.isEmpty()) {
              InputStream is = spSite.getDocumentContent(documents.get(0));
            }
            System.out.println(documents);
        }
    }
}
