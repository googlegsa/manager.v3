package com.google.enterprise.connector.sp2cloud;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.Ace.Type;
import com.google.gdata.client.docs.DocsService;

public class DoclistPusherTest extends TestCase {
  private final String ADMIN_ID = "admin@sharepoint-connector.com";
  private final String TUSER1_ID = "tuser1@sharepoint-connector.com";
  private final String TUSER2_ID = "tuser2@sharepoint-connector.com";
  private final String TUSER3_ID = "tuser3@sharepoint-connector.com";
  private final String TGROUP1_ID = "tgroup1@sharepoint-connector.com";

	private final String ADMIN_TOKEN =
	  "DQAAAIoAAACjIYL-YfwW3Emlgj-fG2vl5tiRtOK9OijniQG-RmK1HpiR-Uiwxd_pCWYVFHneQKsQvXRMnlGtwGeU9AXQeqkdXFLjFF56LCpDI4LngAg720G06dBG0jnekusWJn1jZdd7zz6vgFPxRRsowURKapW9_LQ0oTE2SULQmnVGDTm3WUiyHtHNFUpoXxZJhnj1w0Q";
	private Random generator;
	private DoclistPusher pusher;
	private String rootFolderId;

	@Override
	public void setUp() throws Exception {
		DocsService client = DoclistPusher.mkClient(ADMIN_ID, ADMIN_TOKEN);
		generator = new Random();
		pusher = new DoclistPusher(client);
		rootFolderId = "root_" + generator.nextInt(Integer.MAX_VALUE);
	}

	private Ace newAce(String name, Ace.GPermission gPermission, Type type) {
		Ace result = new Ace(name, null, type);
		result.setGPermission(gPermission);
		return result;
	}

  public void testFolders() throws Exception {
  	List<Ace> rootAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.READ, Ace.Type.USER));
  	Folder root = mkFolder(null, rootFolderId, rootAcl, ADMIN_ID);
  	pusher.pushFolder(null, root);
  	List<Ace> childAcl = Arrays.asList(newAce(TUSER2_ID, Ace.GPermission.READ, Ace.Type.USER),
  		newAce(TUSER3_ID, Ace.GPermission.FULLCONTROL, Ace.Type.USER),
  		newAce(TGROUP1_ID, Ace.GPermission.READ, Ace.Type.USER));
    Folder child = mkFolder(root, "child1", childAcl, ADMIN_ID);
  	pusher.pushFolder(root, child);
  }

  public void testDocuments() throws Exception {
  	List<Ace> rootAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.READ, Ace.Type.USER));
  	Folder root = mkFolder(null, rootFolderId, rootAcl, ADMIN_ID);
  	pusher.pushFolder(null, root);
  	Document document = new Document("d1_" + generator.nextInt(Integer.MAX_VALUE), "d1", null, rootAcl, ADMIN_ID, "text/plain");
  	pusher.pushDocument(null, document, new ByteArrayInputStream("Hi Eric\n".getBytes("US-ASCII")));

  	List<Ace> docAcl = Arrays.asList(newAce(TUSER1_ID, Ace.GPermission.FULLCONTROL, Ace.Type.USER));
  	document = new Document("d2", "d2_id", root.getId(), docAcl, ADMIN_ID, "text/plain");
  	pusher.pushDocument(null, document, new ByteArrayInputStream("Hi Eric2\n".getBytes("US-ASCII")));

  }

  private Folder mkFolder(Folder parent, String folderId, List<Ace> acl, String owner) {
  	String parentId = parent == null ? null : parent.getId();
  	Folder result = new Folder("f_" + folderId, folderId, parentId, acl, owner, false);
  	return result;
  }
}
