// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.MockSharepointServerUsingFileSystem;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.gdata.client.docs.DocsService;

public class TraversalEngineTest {
  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
  private static final String ADMIN_TOKEN =
      "DQAAAIoAAACjIYL-YfwW3Emlgj-fG2vl5tiRtOK9OijniQG-RmK1HpiR-Uiwxd_pCWYVFHneQKsQvXRMnlGtwGeU9AXQeqkdXFLjFF56LCpDI4LngAg720G06dBG0jnekusWJn1jZdd7zz6vgFPxRRsowURKapW9_LQ0oTE2SULQmnVGDTm3WUiyHtHNFUpoXxZJhnj1w0Q";

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    /*
      SharepointSite site = new MockSharepointServerUsingFileSystem("/home/johnfelton/Desktop/Migrate to the Cloud Test Files", "user", "password");
      DocsService client = DoclistPusher.mkClient(ADMIN_ID, ADMIN_TOKEN);
      CloudPusher cloudPusher = new DoclistPusher(client, ADMIN_ID, true);
      TraversalEngine traverser = new TraversalEngine(site, cloudPusher);
      Folder sourceFolder = traverser.findSharePointFolderFromPath("");
      traverser.pushFolderHierarchy(sourceFolder);
      traverser.pushDocumentHierarchy(sourceFolder);
      */
  }

}
