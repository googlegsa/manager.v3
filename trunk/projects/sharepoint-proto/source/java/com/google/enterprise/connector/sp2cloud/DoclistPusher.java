package com.google.enterprise.connector.sp2cloud;

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
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoclistPusher implements CloudPusher {
  private final DocsService client;
  private final String clientUserId;
  private final boolean convert;
  
  private final Map<Folder, String> folderToContentUrlMap = new HashMap<Folder, String>();

  // URL of top level doclist container.
  private static final String DOCLIST_ROOT_CONTENT_URL = "https://docs.google.com/feeds/default/private/full/";

  DoclistPusher(DocsService client, String clientUserId, boolean convert) {
    this.client = client;
    this.clientUserId = clientUserId;
    this.convert = convert;
  }

  public void pushDocument(Folder parent, Document document, List<CloudAce> cloudAcl,
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

    String parentContentUrl = getParentContentUrl(parent); // "https://docs.google.com/feeds/default/private/full/";
    if (!convert) {
      parentContentUrl += "?convert=false";
    }
    DocumentListEntry dle = client.insert(new URL(parentContentUrl), newDocument);
    
    pushAcl(cloudAcl, dle);
    
    //setOwner(dle, document.getOwner());

  }

  public void pushFolder(Folder parent, Folder folder, List<CloudAce> cloudAcl) throws Exception {
    if (folderToContentUrlMap.get(folder.getId()) != null) {
      throw new IllegalArgumentException(
          "Attempt to push a folder more than once " + folder);
    }
//    String rootContentUrl = "https://docs.google.com/feeds/default/private/full/";
//    DocumentListEntry dle = createFolder(rootContentUrl, folder.getName());
    DocumentListEntry dle = createFolder(getParentContentUrl(parent), folder.getName());
    String contentUrl = ((MediaContent) dle.getContent()).getUri();
    folderToContentUrlMap.put(folder, contentUrl);
    pushAcl(cloudAcl, dle);
    setOwner(dle, folder.getOwner());

//    String parentContentUrl = getParentContentUrl(parent);
//    if (!DOCLIST_ROOT_CONTENT_URL.equals(parentContentUrl)) {
//      System.out.println("Moving document " + dle.getDocId() + " to folder " + parentContentUrl);
//      moveToFolder(client, dle, parentContentUrl);
//    }
  }

  private void setOwner(DocumentListEntry dle, String owner) throws MalformedURLException, IOException, ServiceException, InterruptedException{
//    if (!owner.equals(clientUserId)) {
//      addAce(owner, Type.USER, AclRole.OWNER, dle);
//    }
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

  private void pushAcl(List<CloudAce> cloudAcl, DocumentListEntry dle)
      throws IOException, ServiceException {
    for (CloudAce cloudAce : cloudAcl) {
      addAce(cloudAce, dle);
    }
  }

  private void addAce(CloudAce cloudAce, DocumentListEntry entry)
    throws IOException, MalformedURLException, ServiceException {
    addAce(cloudAce.getName(), cloudAce.getType(), cloudAce.getRole(), entry);
  }

  private void addAce(String name, Type type, AclRole role, DocumentListEntry entry)
  throws IOException, ServiceException {
    System.out.println("Adding ace for " + name);

  AclScope scope = new AclScope(type, name);
  AclEntry aclEntry = new AclEntry();
  aclEntry.setRole(role);
  aclEntry.setScope(scope);
  
  client.insert(new URL(entry.getAclFeedLink().getHref()), aclEntry);
}

  public static DocsService mkClient(String user, String userToken) {
    DocsService client = new DocsService("cloud push");
    client.setUserToken(userToken);
    System.out.println(client);
    return client;
  }


}
