// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class DoclistPusher implements CloudPusher {
  private final DocsService client;
  private final FolderManager folderManager;
  private final String adminUserId;
  private final boolean convert;

  // URL of top level doclist container.
  private static final String DOCLIST_ROOT_CONTENT_URL =
      "https://docs.google.com/feeds/default/private/full/";

  DoclistPusher(DocsService client, FolderManager folderManager,
      String adminUserId, boolean convert)
      throws Exception {
    this.client = client;
    this.folderManager = folderManager;
    this.adminUserId = adminUserId;
    this.convert = convert;
  }

  public void pushDocument(Document document,
      CloudAcl cloudAcl, InputStream inputStream) throws Exception {

    DocumentListEntry dle = createDocument(
        getOauthUrl(DOCLIST_ROOT_CONTENT_URL, cloudAcl.getOwner()),
        document, inputStream);
    dle = linkAndSecure(folderManager.getFolderInfo(document.getParentId()),
        cloudAcl, dle);
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

  public void pushFolder(FolderInfo folderInfo) throws Exception {
    DocumentListEntry dle = createFolder(
        getOauthUrl(DOCLIST_ROOT_CONTENT_URL,
            folderInfo.getCloudAcl().getOwner()),
            folderInfo.getName());
    dle = linkAndSecure(folderInfo.getParent(), folderInfo.getCloudAcl(), dle);
    folderInfo.setBaseUrl(getBaseContentUrl(dle));
  }

  private DocumentListEntry linkAndSecure(FolderInfo parent,
      CloudAcl cloudAcl, DocumentListEntry dle) throws IOException,
      ServiceException, MalformedURLException {
    if (!adminUserId.equals(cloudAcl.getOwner())) {
      addAce(adminUserId, Type.USER, AclRole.WRITER, dle);
    }
    if (parent != null) {
      dle = moveToFolder(client, dle, getParentContentUrl(parent, adminUserId));
      if (parent != null
          && parent.getCloudAcl().getOwner().equals(cloudAcl.getOwner())) {
        deleteRootLink(dle, cloudAcl.getOwner());
      }
    }
    adjustAcl(parent, cloudAcl, dle);
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

  private String getParentContentUrl(FolderInfo parent, String requesterId) {
    if (parent == null) {
      return getOauthUrl(DOCLIST_ROOT_CONTENT_URL, requesterId);
    } else {
      return getOauthUrl(parent.getBaseUrl(), requesterId);
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

    DocumentListEntry result =
        client.insert(new URL(destFolderContentUrl), newEntry);
    return result;
  }

  /**
   * Push the ACL for a document or folder to the cloud. The pushed ACL
   * will contain the provided {@link CloudAce} adjustments specified by
   * {@link CloudAcl#getAclAdjustments(CloudAcl, String)}.
   * <ol>
   * <li> Any entry for {@link #adminUserId} is dropped.
   * <li> If {@link #adminUserId} does not own the document she is given
   * {@link AclRole#WRITER} access.
   * @throws ServiceException
   * @throws IOException
   */
  private void adjustAcl(FolderInfo parent,
      CloudAcl cloudAcl, DocumentListEntry dle)
      throws IOException, ServiceException {
    AclAdjustments adjustments =
      cloudAcl.getAclAdjustments(parent == null ? null : parent.getCloudAcl(),
      adminUserId);
    for (CloudAce cloudAce : adjustments.getInserts()) {
      if (!cloudAce.isTypeAndNameMatch(Type.USER, adminUserId)) {
        addAce(cloudAce, dle);
      }
    }

    for (CloudAce cloudAce : adjustments.getUpdates()) {
      updateAce(dle.getAclFeedLink().getHref(),
          cloudAce.getRole(), cloudAce.getType(), cloudAce.getName());
    }

    for (CloudAce cloudAce : adjustments.getDeletes()) {
      deleteAce(dle.getAclFeedLink().getHref(),
          cloudAce.getType(), cloudAce.getName());
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

  private void updateAce(String aclFeedUrl, AclRole newRole, AclScope.Type type,
      String name) throws IOException, ServiceException {
    String aceUrl = getAclUrl(aclFeedUrl, type, name);
    System.out.println("Updating ace " + aceUrl);

    AclEntry aclEntry = new AclEntry();
    aclEntry.setRole(newRole);
    aclEntry.setScope(new AclScope(type, name));
    client.update(new URL(aceUrl), aclEntry);
  }

  private void deleteAce(String aclFeedUrl, AclScope.Type type, String name)
      throws IOException, ServiceException {
    String aceUrl = getAclUrl(aclFeedUrl, type, name);
    System.out.println("Deleting ace " + aceUrl);
    client.delete(new URL(aceUrl));
  }

  private String getAclUrl(String aclFeedUrl, AclScope.Type type, String name)
      throws UnsupportedEncodingException {
    int ix = aclFeedUrl.indexOf("?");
    if (ix < 0) {
      throw new IllegalArgumentException("Feed Url must have argument "
          + aclFeedUrl);
    }
    String urlBase = aclFeedUrl.substring(0, ix);
    String urlArguments = aclFeedUrl.substring(ix);
    if (!type.equals(AclScope.Type.USER) && !type.equals(AclScope.Type.GROUP)) {
      throw new IllegalArgumentException("Type must be user or group but is "
          + type.name());
    }
    String typeString = type.equals(AclScope.Type.USER) ? "user" : "group";
    String aclPath = urlBase + "/" + URLEncoder.encode(typeString + ":"
        + name, "UTF-8") + urlArguments;
    return aclPath;
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
