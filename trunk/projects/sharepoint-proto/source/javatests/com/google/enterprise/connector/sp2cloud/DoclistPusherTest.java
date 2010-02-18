package com.google.enterprise.connector.sp2cloud;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.Ace.Type;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.docs.PdfEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

public class DoclistPusherTest extends TestCase {
  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
  private static final String TUSER1_ID = "strellis@sharepoint-connector.com";
  private static final String TUSER2_ID = "johnfelton@sharepoint-connector.com";
  private static final String TUSER3_ID = "ziff@sharepoint-connector.com";
  private static final String TGROUP1_ID = "engineering@sharepoint-connector.com";

  private static final String ADMIN_TOKEN =
	  "DQAAAIoAAACjIYL-YfwW3Emlgj-fG2vl5tiRtOK9OijniQG-RmK1HpiR-Uiwxd_pCWYVFHneQKsQvXRMnlGtwGeU9AXQeqkdXFLjFF56LCpDI4LngAg720G06dBG0jnekusWJn1jZdd7zz6vgFPxRRsowURKapW9_LQ0oTE2SULQmnVGDTm3WUiyHtHNFUpoXxZJhnj1w0Q";
	private Random generator;
	private DoclistPusher pusher;
	private String rootFolderId;
	private Map<String, Folder> idToFolderMap;

	@Override
	public void setUp() throws Exception {
		DocsService client = DoclistPusher.mkClient(ADMIN_ID, ADMIN_TOKEN);
		generator = new Random();
		pusher = new DoclistPusher(client, ADMIN_ID, false);
		rootFolderId = "root_" + generator.nextInt(Integer.MAX_VALUE);
		idToFolderMap = new HashMap<String, Folder>();
	}

    private Ace newAce(String name, Ace.GPermission gPermission, Type type) {
      Ace result = new Ace(name, null, type);
      result.setGPermission(gPermission);
      return result;
  }

  public void testFolders() throws Exception {
    List<CloudAce> cloudAcl = Arrays.asList(new CloudAce(TUSER1_ID, AclScope.Type.USER, AclRole.READER));
  	List<Ace> rootAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.READ, Ace.Type.USER));
  	Folder root = mkFolder(null, rootFolderId, rootAcl, ADMIN_ID);
  	pusher.pushFolder(null, root, cloudAcl);
  	
    List<CloudAce> childCloudAcl = Arrays.asList(new CloudAce(TUSER2_ID, AclScope.Type.USER, AclRole.READER),
        new CloudAce(TUSER3_ID, AclScope.Type.USER, AclRole.WRITER),
        new CloudAce(TGROUP1_ID, AclScope.Type.USER, AclRole.READER) );
 	List<Ace> childAcl = Arrays.asList(newAce(TUSER2_ID, Ace.GPermission.READ, Ace.Type.USER),
  		newAce(TUSER3_ID, Ace.GPermission.FULLCONTROL, Ace.Type.USER),
  		newAce(TGROUP1_ID, Ace.GPermission.READ, Ace.Type.USER));
    Folder child = mkFolder(root, "child1", childAcl, ADMIN_ID);
  	pusher.pushFolder(root, child, childCloudAcl);
  }

  public void testDocuments() throws Exception {
    List<CloudAce> cloudAcl = Arrays.asList(new CloudAce(TUSER1_ID, AclScope.Type.USER, AclRole.READER));
    List<Ace> rootAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.READ, Ace.Type.USER));
  	Folder root = mkFolder(null, rootFolderId, rootAcl, ADMIN_ID);
  	pusher.pushFolder(null, root, cloudAcl);
  	Document document = new Document("d1_" + generator.nextInt(Integer.MAX_VALUE), "d1", null, rootAcl, TUSER3_ID, "text/plain", "not-used");
  	pusher.pushDocument(null, document, cloudAcl, new ByteArrayInputStream("Hi Eric\n".getBytes("US-ASCII")));

    List<CloudAce> docCloudAcl = Arrays.asList(new CloudAce(TUSER1_ID, AclScope.Type.USER, AclRole.WRITER));
  	List<Ace> docAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.FULLCONTROL, Ace.Type.USER));
  	document = new Document("d2", "d2_id", root.getId(), docAcl, TUSER1_ID, "text/plain", "not-used");
  	pusher.pushDocument(root, document, docCloudAcl, new ByteArrayInputStream("Hi Eric2\n".getBytes("US-ASCII")));
  }
  


  private DocumentListEntry moveToFolder(DocsService client, DocumentListEntry sourceEntry, DocumentListEntry destFolderEntry)
      throws IOException, MalformedURLException, ServiceException {

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

    String destFolderUri = ((MediaContent) destFolderEntry.getContent()).getUri();

    return client.insert(new URL(destFolderUri), newEntry);
  }


  
  public void aatestAclChanges() throws Exception {
    //
    // Create parent.
    final String DOCLIST_ROOT_CONTENT_URL = "https://docs.google.com/feeds/default/private/full/";
    DocumentListEntry newEntry = new FolderEntry();
    newEntry.setTitle(new PlainTextConstruct("AA_ROOT"));
    URL feedUrl = new URL(DOCLIST_ROOT_CONTENT_URL);
    DocsService client = DoclistPusher.mkClient(ADMIN_ID, ADMIN_TOKEN);
    DocumentListEntry rootdle =  client.insert(feedUrl, newEntry);
    
    //
    // Create a child folder.
    newEntry = new FolderEntry();
    newEntry.setTitle(new PlainTextConstruct("CHILD-1"));
    //feedUrl = new URL(((MediaContent) dle.getContent()).getUri());
    DocumentListEntry childdle =  client.insert(feedUrl, newEntry);
    
    
    //add 2 readers
    System.out.println("parent acl href: " + rootdle.getAclFeedLink().getHref());
//    for(AclEntry ace : dle.getAclFeed().getEntries()) {
//      printAclEntry(ace);
//    }
    
    System.out.println("child acl href: " + childdle.getAclFeedLink().getHref());

    {
      AclRole role = new AclRole("owner");
      AclScope scope = new AclScope(AclScope.Type.USER, "strellis@sharepoint-connector.com");
      AclEntry ace = new AclEntry();
      ace.setRole(role);
      ace.setScope(scope);
      AclEntry aclEntry = client.insert(new URL(rootdle.getAclFeedLink().getHref()), ace);
      printAclEntry(aclEntry);
    }

    {
      AclRole role = new AclRole("writer");
      AclScope scope = new AclScope(AclScope.Type.USER, "ziff@sharepoint-connector.com");
      AclEntry ace = new AclEntry();
      ace.setRole(role);
      ace.setScope(scope);
      AclEntry aclEntry = client.insert(new URL(rootdle.getAclFeedLink().getHref()), ace);
      printAclEntry(aclEntry);
    }

    //strellisEntry.delete();
    
    
//    AclEntry adminEntry = client.getEntry(new URL(childdle.getSelfLink().getHref()+"/acl/user%3Aadmin%40sharepoint-connector.com"), AclEntry.class);
//    printAclEntry(adminEntry);
//    adminEntry.delete();
    
    {
      AclRole role = new AclRole("owner");
      AclScope scope = new AclScope(AclScope.Type.USER, "strellis@sharepoint-connector.com");
      AclEntry ace = new AclEntry();
      ace.setRole(role);
      ace.setScope(scope);
      AclEntry aclEntry = client.insert(new URL(childdle.getAclFeedLink().getHref()), ace);
      printAclEntry(aclEntry);
    }
    {
      AclRole role = new AclRole("reader");
      AclScope scope = new AclScope(AclScope.Type.USER, "ziff@sharepoint-connector.com");
      AclEntry ace = new AclEntry();
      ace.setRole(role);
      ace.setScope(scope);
      AclEntry aclEntry = client.insert(new URL(childdle.getAclFeedLink().getHref()), ace);
      printAclEntry(aclEntry);
    }
    
    moveToFolder(client,childdle,rootdle);
    //ziffEntry.delete();
    
    DocumentListEntry newDocument = new DocumentListEntry();
    MediaStreamSource mediaSource = new MediaStreamSource(new FileInputStream("/home/strellis/aaurls.txt"), "text/plain");
    MediaContent content = new MediaContent();
    content.setMediaSource(mediaSource);
    content.setMimeType(new ContentType("text/plain"));
    newDocument.setContent(content);

    newDocument.setTitle(new PlainTextConstruct("aaurls.txt"));
    //
    // Note removing ?convert=false from rootContentUrl avoids the exception
    String rootContentUrl = "https://docs.google.com/feeds/default/private/full/?convert=false";
    DocumentListEntry documentdle = client.insert(new URL(rootContentUrl), newDocument);
    
    {
      AclRole role = new AclRole("owner");
      AclScope scope = new AclScope(AclScope.Type.USER, "strellis@sharepoint-connector.com");
      AclEntry ace = new AclEntry();
      ace.setRole(role);
      ace.setScope(scope);
      AclEntry aclEntry = client.insert(new URL(documentdle.getAclFeedLink().getHref()), ace);
      printAclEntry(aclEntry);
    }

    //Change child owner here.
    moveToFolder(client,documentdle,rootdle);
    

  }

  public void printAclEntry(AclEntry entry) {
    System.out.println(" -- " + entry.getScope().getValue() + ": " + entry.getRole().getValue());
  }

  private Folder mkFolder(Folder parent, String folderId, List<Ace> acl, String owner) {
  	String parentId = parent == null ? null : parent.getId();
  	Folder result = new Folder("f_" + folderId, folderId, "URL", parentId, acl, owner, false);
  	idToFolderMap.put(result.getId(), result);
  	return result;
  }
}
