package com.google.enterprise.connector.sp2cloud;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.DirEntry;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

public class DoclistPusher implements CloudPusher {
  private final DocsService client;
  private final boolean convert;
  private final Map<Folder, String> folderToContentUrlMap = new HashMap<Folder, String>();
  private final Map<String, Folder> idToFolderMap = new HashMap<String, Folder>();

  // URL of top level doclist container.
  private static final String DOCLIST_ROOT_CONTENT_URL = "https://docs.google.com/feeds/default/private/full/";

  DoclistPusher(DocsService client, boolean convert) {
    this.client = client;
    this.convert = convert;
  }

  public void pushDocument(Folder parent, Document document,
      InputStream inputStream) throws Exception {
    String parentContentUrl = getParentContentUrl(parent);
    if (!convert) {
      parentContentUrl += "?convert=false";
    }

    DocumentListEntry newDocument = new DocumentListEntry();
    MediaStreamSource mediaSource = new MediaStreamSource(inputStream, document
        .getMimeType());
    MediaContent content = new MediaContent();
    content.setMediaSource(mediaSource);
    content.setMimeType(new ContentType(document.getMimeType()));
    newDocument.setContent(content);

    newDocument.setTitle(new PlainTextConstruct(document.getName()));
    // Prevent collaborators from sharing the document with others?
    // newDocument.setWritersCanInvite(false);
    System.out.println("parentContentUrl=" + parentContentUrl);
    DocumentListEntry dle = client.insert(new URL(parentContentUrl), newDocument);
    pushAcl(document, dle, parent);
  }

  public void pushFolder(Folder parent, Folder folder) throws Exception {
    if (folderToContentUrlMap.get(folder.getId()) != null) {
      throw new IllegalArgumentException(
          "Attempt to push a folder more than once " + folder);
    }
    String parentContentUrl = getParentContentUrl(parent);
    DocumentListEntry dle = createFolder(parentContentUrl, folder.getName());
    String contentUrl = ((MediaContent) dle.getContent()).getUri();
    folderToContentUrlMap.put(folder, contentUrl);
    pushAcl(folder, dle, parent);
    idToFolderMap.put(folder.getId(), folder);
  }

  public static DocsService mkClient(String user, String userToken)
      throws AuthenticationException {
    DocsService client = new DocsService("cloud push");
    client.setUserToken(userToken);
    System.out.println(client);
    return client;
  }

  private String getParentContentUrl(Folder parent) {
    if (parent == null) {
      return DOCLIST_ROOT_CONTENT_URL;
    } else {
      String result = folderToContentUrlMap.get(parent);
      if (result == null) {
        throw new IllegalArgumentException(
            "Attempt to access a parent folder that has not been created. "
                + parent);
      }
      return result;
    }
  }

  private DocumentListEntry createFolder(String parentFolderUrl, String title)
      throws IOException, ServiceException {
    DocumentListEntry newEntry = new FolderEntry();
    newEntry.setTitle(new PlainTextConstruct(title));
    URL feedUrl = new URL(parentFolderUrl);
    return client.insert(feedUrl, newEntry);
  }

  private void pushAcl(DirEntry dirEntry, DocumentListEntry dle, Folder parent)
      throws MalformedURLException, IOException, ServiceException {
    if (dirEntry.getAcl() != null) {
      for (Ace ace : dirEntry.getAcl()) {
        addAce(ace, dle, parent);
      }
    }
  }

  private AclRole getAclRoleForGPermission(Ace.GPermission gPermission) {
    switch (gPermission) {
    case FULLCONTROL:
    case WRITE:
      return AclRole.WRITER;
    case READ:
      return AclRole.READER;
    default:
      throw new IllegalArgumentException("Unsupported gPermission: "
          + gPermission);
    }
  }

  private void addAce(Ace ace, DocumentListEntry entry, Folder parent)
      throws IOException, MalformedURLException, ServiceException {
    if (aceExistsInParentFolder(ace, parent)) {
      System.out.println("Skipping ace for " + ace.getName());
    } else {
      System.out.println("Adding ace for " + ace.getName());
      AclScope scope = new AclScope(
          ace.getType().equals(Ace.Type.USER) ? AclScope.Type.USER
              : AclScope.Type.GROUP, ace.getName());

      AclEntry aclEntry = new AclEntry();
      aclEntry.setRole(getAclRoleForGPermission(ace.getGPermission()));
      aclEntry.setScope(scope);

      client.insert(new URL(entry.getAclFeedLink().getHref()), aclEntry);
    }
  }

  private boolean aceExistsInParentFolder(Ace ace, Folder parent) {
    if (parent == null) {
      return false;
    }
    for (Ace folderAce : parent.getAcl()) {
      if (ace.getName().equals(folderAce.getName())) {
        if (ace.getGPermission().compareTo(folderAce.getGPermission()) >= 0) {
          return true;
        }
      }
    }
    if (idToFolderMap.containsKey(parent.getParentId())) {
      return aceExistsInParentFolder(ace, idToFolderMap.get(parent
          .getParentId()));
    } else {
      return false;
    }
  }
}
