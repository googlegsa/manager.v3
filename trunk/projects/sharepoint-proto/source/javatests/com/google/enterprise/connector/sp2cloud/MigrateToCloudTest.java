package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.MockSharepointServerFromMemory;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.gdata.client.docs.DocsService;

public class MigrateToCloudTest {

  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
  private static final String ADMIN_TOKEN =
      "DQAAAIoAAACjIYL-YfwW3Emlgj-fG2vl5tiRtOK9OijniQG-RmK1HpiR-Uiwxd_pCWYVFHneQKsQvXRMnlGtwGeU9AXQeqkdXFLjFF56LCpDI4LngAg720G06dBG0jnekusWJn1jZdd7zz6vgFPxRRsowURKapW9_LQ0oTE2SULQmnVGDTm3WUiyHtHNFUpoXxZJhnj1w0Q";

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    SharepointSite site =
        new MockSharepointServerFromMemory("/home/johnfelton/Desktop/Migrate to the Cloud Test Files",
            "user", "password");
    DocsService client = DoclistPusher.mkClient(ADMIN_ID, ADMIN_TOKEN);
    CloudPusher cloudPusher = new DoclistPusher(client, false);
    SharePointToCloudMigrator.migrate(cloudPusher, site);
  }

}
