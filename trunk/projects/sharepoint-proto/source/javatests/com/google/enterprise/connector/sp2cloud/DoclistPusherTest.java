package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.Ace.Type;
import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DoclistPusherTest extends TestCase {
  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
  private static final String TUSER1_ID = "strellis@sharepoint-connector.com";
  private static final String TUSER2_ID = "johnfelton@sharepoint-connector.com";
  private static final String TUSER3_ID = "ziff@sharepoint-connector.com";
  private static final String TGROUP1_ID = "engineering@sharepoint-connector.com";
  private static final String CONSUMER_KEY = "sharepoint-connector.com";
  private static final String CONSUMER_SECRET = "A6dY3I1uW89q999msVYEX07l";

  private Random generator;
  private DoclistPusher pusher;
  private String rootFolderId;
  private Map<String, Folder> idToFolderMap;
  private DocsService client;

  @Override
  public void setUp() throws Exception {
    // DocsService client = DoclistPusher.mkClient(ADMIN_ID, ADMIN_TOKEN);
    client = DoclistPusher.mkOauthClient(CONSUMER_KEY, CONSUMER_SECRET);
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

  public void clientLoginTest() throws Exception {
    DocsService client = new DocsService("sp2c-v1");
    client.setUserCredentials("admin@sharepoint-connector.com", "testing");
    UserToken auth_token = (UserToken) client.getAuthTokenFactory()
        .getAuthToken();

    System.out.println("token=" + auth_token.getValue());
  }

  // Generated 3/9 life = ?
  String TK = "DQAAAI0AAACtL50YtS8-Zaksz6dHNGRqdok_JFP-DKxSDWbNVtQySK4PIIRc2GE9Zv1iC-Gt-SwrM6xcCuJBk4gSVt09HIpmq3n0mu2ZlTKbTF671NAPk6TD9w7-EP7rjJojBjACUQPhtSieb-iU6af9PqwyPW-XybYudrU1A9RCOaOIvcedr9A8GtDJWms1YrPolPG0Elw";

  public void tokenLoginTest() throws Exception {
    client.setUserToken(TK);
    System.out.println("made it!");
  }

  public void testFolders() throws Exception {
    List<CloudAce> cloudAcl = Arrays.asList(
        new CloudAce(TUSER1_ID, AclScope.Type.USER, AclRole.READER), 
        new CloudAce(TUSER3_ID, AclScope.Type.USER, AclRole.READER));
    List<Ace> rootAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.READ,
        Ace.Type.USER));
    Folder root = mkFolder(null, rootFolderId, rootAcl, ADMIN_ID);
    pusher.pushFolder(null, root, cloudAcl);

    //Add child1 to root as TUSER3_ID
    List<CloudAce> childCloudAcl = Arrays.asList(new CloudAce(TUSER2_ID,
        AclScope.Type.USER, AclRole.READER), new CloudAce(TGROUP1_ID,
        AclScope.Type.USER, AclRole.READER));
    List<Ace> childAcl = Arrays.asList(
        newAce(TUSER2_ID, Ace.GPermission.READ, Ace.Type.USER), 
        newAce(TGROUP1_ID, Ace.GPermission.READ, Ace.Type.USER));
    Folder child = mkFolder(root, "child1", childAcl, TUSER3_ID);
    pusher.pushFolder(root, child, childCloudAcl);
    
    //Add child2 to root as ADMIN_ID
    List<CloudAce> child2CloudAcl = new ArrayList<CloudAce>();
    List<Ace> child2Acl = new ArrayList<Ace>();
    Folder child2 = mkFolder(root, "child2", childAcl, ADMIN_ID);
    pusher.pushFolder(root, child2, child2CloudAcl);
    
    //Add a grandchild to child1 as TUSER3_ID
    List<CloudAce> grandchildCloudAcl = new ArrayList<CloudAce>();
    List<Ace> grandchildAcl = new ArrayList<Ace>();
    Folder grandchild = mkFolder(root, "grandchild", grandchildAcl, TUSER3_ID);
    pusher.pushFolder(child, grandchild, grandchildCloudAcl);
  }

  public void testDocuments() throws Exception {
    List<CloudAce> cloudAcl = Arrays.asList(new CloudAce(TUSER1_ID,
        AclScope.Type.USER, AclRole.READER));
    List<Ace> rootAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.READ,
        Ace.Type.USER));
    Folder root = mkFolder(null, rootFolderId, rootAcl, ADMIN_ID);
    pusher.pushFolder(null, root, cloudAcl);
    Document document = new Document("d1_"
        + generator.nextInt(Integer.MAX_VALUE), "d1", null, rootAcl, TUSER3_ID,
        "text/plain", "not-used");
    pusher.pushDocument(null, document, cloudAcl, new ByteArrayInputStream(
        "Hi Eric\n".getBytes("US-ASCII")));

    List<CloudAce> docCloudAcl = Arrays.asList(
        new CloudAce(TUSER2_ID, AclScope.Type.USER, AclRole.WRITER));
    List<Ace> docAcl = Arrays.asList(
        newAce(TUSER2_ID, Ace.GPermission.FULLCONTROL, Ace.Type.USER));
    document = new Document("d2", "d2_id", root.getId(), docAcl, TUSER1_ID,
        "text/plain", "not-used");
    pusher.pushDocument(root, document, docCloudAcl, new ByteArrayInputStream(
        "Hi Eric2\n".getBytes("US-ASCII")));
  }

  public void printAclEntry(AclEntry entry) {
    System.out.println(" -- " + entry.getScope().getValue() + ": "
        + entry.getRole().getValue());
  }

  private Folder mkFolder(Folder parent, String folderId, List<Ace> acl,
      String owner) {
    String parentId = parent == null ? null : parent.getId();
    Folder result = new Folder("f_" + folderId, folderId, "URL", parentId, acl,
        owner, false);
    idToFolderMap.put(result.getId(), result);
    return result;
  }
}
