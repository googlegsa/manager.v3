package com.google.enterprise.connector.sp2c_migration;

import java.util.List;

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

public class SP2CMain {
    public static void main(String[] args) {
        try {
            SharepointSite spSite = SharepointSiteFactory.getSharepointSite(args[0], args[1], args[2], args[3]);
            System.out.println("Traversing Sharepoint site: " + spSite.getUrl());
            List<Folder> rootFolders = spSite.getRootFolders();
            System.out.println("Root Folders [ " + rootFolders + " ]");
            for (Folder rootFolder : rootFolders) {
                System.out.println("Traversing root folder [ " + rootFolder    + " ] ");
                List<Folder> folders = spSite.getFolders(rootFolder);
                System.out.println("Folders [ " + folders + " ]");
                List<Document> documents = spSite.getDocuments(rootFolder);
                System.out.println("Documents [ " + documents + " ]");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
