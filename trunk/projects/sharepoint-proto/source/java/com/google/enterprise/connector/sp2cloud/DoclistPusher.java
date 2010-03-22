package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.DirEntry;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
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
  private final String adminUserId;
  private final boolean convert;
  
  private static class  FolderInfo{
    private final String baseUrl;
    private final String owner;
    private final List<CloudAce> acl;
    
    FolderInfo(String baseUrl, String owner, List<CloudAce> acl) {
      this.baseUrl = baseUrl;
      this.owner = owner;
      this.acl = acl;
    }

    String getOauthUrl(String requesterId) {
      return DoclistPusher.getOauthUrl(baseUrl, requesterId);
    }
    String getBaseUrl() {
      return baseUrl;
    }

    String getOwner() {
      return owner;
    }

    List<CloudAce> getAcl() {
      return acl;
    }
    
    @Override
    public String toString() {
      return "FolderInfo: baseUrl=" + baseUrl
          + " owner=" + owner
          + " acl=" + acl;
    }
  }

  private final Map<Folder, FolderInfo> folderToFolderInfoMap = 
    new HashMap<Folder, FolderInfo>();

  // URL of top level doclist container.
  private static final String DOCLIST_ROOT_CONTENT_URL = 
      "https://docs.google.com/feeds/default/private/full/";

  DoclistPusher(DocsService client, String adminUserId, boolean convert) 
      throws Exception {
    this.client = client;
    this.adminUserId = adminUserId;
    this.convert = convert;
  }

  public void pushDocument(Folder parent, Document document, 
      List<CloudAce> cloudAcl, InputStream inputStream) throws Exception {

    DocumentListEntry dle = createDocument(
        getOauthUrl(DOCLIST_ROOT_CONTENT_URL, document.getOwner()), 
        document, inputStream);
    dle = linkAndSecure(parent, document, cloudAcl, dle);
    //pushAcl(document.getOwner(), cloudAcl, dle);
  }

  private DocumentListEntry createDocument(String parentFolderContentUrl, 
      Document document, InputStream inputStream) 
      throws IOException, ServiceException, MalformedURLException {
    DocumentListEntry newDocument = new DocumentListEntry();
    MediaStreamSource mediaSource = new MediaStreamSource(inputStream, 
        document.getMimeType());
    MediaContent content = new MediaContent();
    content.setMediaSource(mediaSource);
    content.setMimeType(new ContentType(document.getMimeType()));
    newDocument.setContent(content);

    newDocument.setTitle(new PlainTextConstruct(document.getName()));
    // Prevent collaborators from sharing the document with others?
    // newDocument.setWritersCanInvite(false);
    if (!convert) {
      parentFolderContentUrl += "&convert=false";
    }    
    URL feedUrl = new URL(parentFolderContentUrl);
    System.out.println("**Create folder feedUrl="+feedUrl);
    return client.insert(feedUrl, newDocument);
  }

  public void pushFolder(Folder parent, Folder folder, 
      List<CloudAce> cloudAcl) throws Exception {
    if (folderToFolderInfoMap.get(folder.getId()) != null) {
      throw new IllegalArgumentException(
          "Attempt to push a folder more than once " + folder);
    }
    DocumentListEntry dle = createFolder(
        getOauthUrl(DOCLIST_ROOT_CONTENT_URL, folder.getOwner()), 
        folder.getName());
    dle = linkAndSecure(parent, folder, cloudAcl, dle);
    folderToFolderInfoMap.put(folder, new FolderInfo(getBaseContentUrl(dle), 
        folder.getOwner(), cloudAcl));
  }

  private DocumentListEntry linkAndSecure(Folder parent, DirEntry newDirEntry,
      List<CloudAce> cloudAcl, DocumentListEntry dle) throws IOException,
      ServiceException, MalformedURLException {
    pushAcl(newDirEntry.getOwner(), cloudAcl, dle);
    // TODO(strellis): only move if all parent permissions exist on folder too.
    if (parent != null) {
      dle = moveToFolder(client, dle, getParentContentUrl(parent, adminUserId));
      if (parent.getOwner().equals(newDirEntry.getOwner())) {
        deleteRootLink(dle, newDirEntry.getOwner());
      }
    }
    return dle;
  }
  
  private void deleteRootLink(DocumentListEntry dle, String ownerId) 
      throws MalformedURLException, IOException, ServiceException {
    final String url = DOCLIST_ROOT_CONTENT_URL
        + "folder%3Aroot/contents/"
        + dle.getResourceId()
        + "?xoauth_requestor_id="
        + ownerId;
    //TODO(strellis): is refresh to get newest etag needed/appropriate here?
    dle = client.getEntry(new URL(dle.getSelfLink().getHref()), 
        DocumentListEntry.class);
    client.delete(new URL(url), dle.getEtag());
    System.out.println("Made it! deleted " + url);
  }
  
  private String getBaseContentUrl(DocumentListEntry dle) {
    String contentUrl = ((MediaContent) dle.getContent()).getUri();
    int questionmarkIx = contentUrl.indexOf('?');
    if (questionmarkIx != -1) {
      contentUrl = contentUrl.substring(0, questionmarkIx);
    }
    return contentUrl;
  }

  private static String getOauthUrl(String baseUrl, String requesterId) {
    return baseUrl + "?xoauth_requestor_id=" + requesterId;
  }

  private String getParentContentUrl(Folder parent, String requesterId) {
    if (parent == null) {
      return getOauthUrl(DOCLIST_ROOT_CONTENT_URL, requesterId);
    } else {
      FolderInfo folderInfo = folderToFolderInfoMap.get(parent);
      if (folderInfo == null) {
        throw new IllegalArgumentException(
            "Attempt to access a parent folder that has not been created. "
                + parent);
      }
      // TODO(strellis): set owner id for document owner not parent folder owner.
      return folderInfo.getOauthUrl(requesterId);    
    }
  }

  private DocumentListEntry createFolder(String parentFolderUrl, String title)
      throws IOException, ServiceException {
    DocumentListEntry newEntry = new FolderEntry();
    newEntry.setTitle(new PlainTextConstruct(title));
    URL feedUrl = new URL(parentFolderUrl);
    System.out.println("**Create folder feedUrl="+feedUrl);
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

    DocumentListEntry result = client.insert(new URL(destFolderContentUrl), newEntry);
    return result;
  }
  /**
   * Push the ACL for a document or folder to the cloud. The pushed ACL
   * will contain the provided {@link CloudAce} entries with a few modifications.
   * <ol>
   * <li> Any entry for {@link #adminUserId} is dropped.
   * <li> If {@link #adminUserId} does not own the document she is given
   * {@link AclRole#WRITER} access.
   * @throws ServiceException
   * @throws IOException
   */
  private void pushAcl(String ownerId, List<CloudAce> cloudAcl, DocumentListEntry dle)
      throws IOException, ServiceException {
    for (CloudAce cloudAce : cloudAcl) {
      if (!cloudAce.getName().equals(adminUserId) || !cloudAce.getType().equals(AclScope.Type.USER)) {
        addAce(cloudAce, dle);
      }
    }
    if (!adminUserId.equals(ownerId)) {
      addAce(adminUserId, Type.USER, AclRole.WRITER, dle);
    }
  }

  private void addAce(CloudAce cloudAce, DocumentListEntry entry)
    throws IOException, MalformedURLException, ServiceException {
    addAce(cloudAce.getName(), cloudAce.getType(), cloudAce.getRole(), entry);
  }

  private void addAce(String name, Type type, AclRole role,
      DocumentListEntry entry) throws IOException, ServiceException {
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

  public static DocsService mkOauthClient(String consumerKey, String consumerSecret) throws OAuthException {
    GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
    oauthParameters.setOAuthConsumerKey(consumerKey);
    oauthParameters.setOAuthConsumerSecret(consumerSecret);
    OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();

    DocsService client = new DocsService("google-cloud push-v1");
    client.setOAuthCredentials(oauthParameters, signer);
    return client;
  }
}
