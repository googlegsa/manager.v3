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
import com.google.gdata.data.acl.AclScope.Type;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.docs.PdfEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

public class DoclistPusher implements CloudPusher {
  private final DocsService client;
  private final String clientUserId;
  private final boolean convert;
  
  private final Map<Folder, String> folderToContentUrlMap = new HashMap<Folder, String>();
  private final Map<String, Folder> idToFolderMap = new HashMap<String, Folder>();

  // URL of top level doclist container.
  private static final String DOCLIST_ROOT_CONTENT_URL = "https://docs.google.com/feeds/default/private/full/";

  DoclistPusher(DocsService client, String clientUserId, boolean convert) {
    this.client = client;
    this.clientUserId = clientUserId;
    this.convert = convert;
  }

  public void pushDocument(Folder parent, Document document,
      InputStream inputStream) throws Exception {

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

    String rootContentUrl = "https://docs.google.com/feeds/default/private/full/";
    if (!convert) {
      rootContentUrl += "?convert=false";
    }
    DocumentListEntry dle = client.insert(new URL(rootContentUrl), newDocument);
    
    pushAcl(document, dle, parent);
    
    //setOwner(dle, document.getOwner());

    String parentContentUrl = getParentContentUrl(parent);
    System.out.println("parentContentUrl=" + parentContentUrl);
    if (!DOCLIST_ROOT_CONTENT_URL.equals(parentContentUrl)) {
      System.out.println("Moving document " + dle.getDocId() + " to folder " + parentContentUrl);
      moveToFolder(client, dle, parentContentUrl);
    }
  }

  public void pushFolder(Folder parent, Folder folder) throws Exception {
    if (folderToContentUrlMap.get(folder.getId()) != null) {
      throw new IllegalArgumentException(
          "Attempt to push a folder more than once " + folder);
    }
    String rootContentUrl = "https://docs.google.com/feeds/default/private/full/";
    DocumentListEntry dle = createFolder(rootContentUrl, folder.getName());
    String contentUrl = ((MediaContent) dle.getContent()).getUri();
    folderToContentUrlMap.put(folder, contentUrl);
    pushAcl(folder, dle, parent);
    setOwner(dle, folder.getOwner());

    String parentContentUrl = getParentContentUrl(parent);
    if (!DOCLIST_ROOT_CONTENT_URL.equals(parentContentUrl)) {
      System.out.println("Moving document " + dle.getDocId() + " to folder " + parentContentUrl);
      moveToFolder(client, dle, parentContentUrl);
    }
    idToFolderMap.put(folder.getId(), folder);
  }
  
  private void setOwner(DocumentListEntry dle, String owner) throws MalformedURLException, IOException, ServiceException, InterruptedException{
    if (!owner.equals(clientUserId)) {
      addAce(owner, Type.USER, AclRole.OWNER, dle);
    }
  }

  private String getParentContentUrl(Folder parent) throws MalformedURLException {
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
  
  private DocumentListEntry moveToFolder(DocsService client,
      DocumentListEntry sourceEntry, String destFolderContentUrl)
      throws IOException, ServiceException {

    DocumentListEntry newEntry = null;

    String docType = sourceEntry.getType();
    if (docType.equals("document")) {
      newEntry = new DocumentEntry();
    } else if (docType.equals("presentation")) {
      newEntry = new PresentationEntry();
    } else if (docType.equals("spreadsheet")) {
      newEntry = new SpreadsheetEntry();
    } else if (docType.equals("folder")) {
      newEntry = new FolderEntry();
    } else if (docType.equals("pdf")) {
      newEntry = new PdfEntry();
    } else {
      newEntry = new DocumentListEntry(); // Unknown type
    }
    newEntry.setId(sourceEntry.getId());

    return client.insert(new URL(destFolderContentUrl), newEntry);
  }

  private void pushAcl(DirEntry dirEntry, DocumentListEntry dle, Folder parent)
      throws MalformedURLException, IOException, ServiceException {
    for (Ace ace : dirEntry.getAcl()) {
      if (aceExistsInParentFolder(ace, parent)) {
        System.out.println("Skipping ace for " + ace.getName());
      } else {
        AclScope.Type type = ace.getType().equals(Ace.Type.USER) ? AclScope.Type.USER
            : AclScope.Type.GROUP;
        AclRole role = getAclRoleForGPermission(ace.getGPermission());
        addAce(ace.getName(), type,  role, dle);
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

  private void addAce(String name, AclScope.Type type, AclRole role, DocumentListEntry dle)  throws IOException,
      ServiceException, MalformedURLException {
    System.out.println("Adding ace for " + name);
    AclScope scope = new AclScope(type, name);

    AclEntry aclEntry = new AclEntry();
    aclEntry.setRole(role);
    aclEntry.setScope(scope);

    client.insert(new URL(dle.getAclFeedLink().getHref()), aclEntry);
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

  public static DocsService mkClient(String user, String userToken)
      throws AuthenticationException {
    DocsService client = new DocsService("cloud push");
    client.setUserToken(userToken);
    System.out.println(client);
    return client;
  }
}
