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

package com.google.enterprise.connector.sp2c_migration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission;
import com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType;
import com.google.enterprise.connector.sp2c_migration.Ace.Type;

public class SharepointSiteImpl implements SharepointSite {
    String id;
    String url;
    WebServiceClient wsClient;

    SharepointSiteImpl(String id, String url, WebServiceClient wsClient) {
        this.id = id;
        this.url = url;
        this.wsClient = wsClient;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public List<Folder> getRootFolders() throws Exception {
        Map<String, Folder> rootFoldersMap = wsClient.getListsAsFolders(id);
        Set<String> folderUrls = rootFoldersMap.keySet();
        String[] urls = new String[folderUrls.size()];
        int i = 0;
        for (String url : folderUrls) {
            urls[i++] = url;
        }

        GssAcl[] acls = wsClient.getAclForUrls(urls);
        List<Folder> folders = new ArrayList<Folder>();
        for (GssAcl acl : acls) {
            List<Ace> allAces = new ArrayList<Ace>();
            for (GssAce ace : acl.getAllAce()) {
                allAces.add(getAceFromSPAce(ace));
            }
            Folder folder = rootFoldersMap.get(acl.getEntityUrl());
            folder.setOwner(acl.getOwner());
            folder.setAcl(allAces);
            folders.add(folder);
        }
        return folders;
    }

    @Override
    public List<Folder> getFolders(Folder rootfolder) throws Exception {
        Map<String, Folder> foldersMap = wsClient.getFolders(rootfolder, null);
        Set<String> folderUrls = foldersMap.keySet();
        String[] urls = new String[folderUrls.size()];
        int i = 0;
        for (String url : folderUrls) {
            urls[i++] = url;
        }

        GssAcl[] acls = wsClient.getAclForUrls(urls);
        List<Folder> folders = new ArrayList<Folder>();
        for (GssAcl acl : acls) {
            List<Ace> allAces = new ArrayList<Ace>();
            for (GssAce ace : acl.getAllAce()) {
                allAces.add(getAceFromSPAce(ace));
            }
            Folder folder = foldersMap.get(acl.getEntityUrl());
            folder.setOwner(acl.getOwner());
            folder.setAcl(allAces);
            folders.add(folder);
        }
        return folders;
    }

    @Override
    public List<Document> getDocuments(Folder rootfolder) throws Exception {
        Map<String, Document> documentsMap = wsClient.getDocuments(rootfolder, null);
        Set<String> docUrls = documentsMap.keySet();
        String[] urls = new String[docUrls.size()];
        int i = 0;
        for (String url : docUrls) {
            urls[i++] = url;
        }

        GssAcl[] acls = wsClient.getAclForUrls(urls);
        List<Document> documents = new ArrayList<Document>();
        for (GssAcl acl : acls) {
            List<Ace> allAces = new ArrayList<Ace>();
            for (GssAce ace : acl.getAllAce()) {
                allAces.add(getAceFromSPAce(ace));
            }
            Document document = documentsMap.get(acl.getEntityUrl());
            document.setOwner(acl.getOwner());
            document.setAcl(allAces);
            documents.add(document);
        }
        return documents;
    }

    /**
     * To download document's content and set its mime type
     */
    public InputStream getDocumentContent(Document document) throws Exception {
        HttpMethodBase method = wsClient.downloadContent(document.getDocUrl());
        if (null == method) {
            throw new Exception("Problem while downloading content");
        }

        final Header mimeType = method.getResponseHeader("Content-Type");
        if (mimeType != null) {
            document.setMimeType(mimeType.getValue());
        }
        return method.getResponseBodyAsStream();
    }

    private Ace getAceFromSPAce(GssAce ace) {
        GssPrincipal principal = ace.getPrincipal();
        String principalName = principal.getName();
        Type principalType = Type.USER;
        if (PrincipalType._DOMAINGROUP.equals(principal.getType().getValue())) {
            principalType = Type.DOMAINGROUP;
        } else if (PrincipalType._SPGROUP.equals(principal.getType().getValue())) {
            principalType = Type.SPGROUP;
            principalName = "[" + getUrl() + "][" + principalName + "]";
        }

        GssSharepointPermission gssPermission = ace.getPermission();
        Ace.SharepointPermissions permissions = new Ace.SharepointPermissions(
                gssPermission.getAllowedPermissions(),
                gssPermission.getDeniedPermission());

        return new Ace(principalName, permissions, principalType);
    }

    @Override
    public Set<String> getDirectChildSites() throws Exception {
        return wsClient.getDirectChildsites();
    }
}
