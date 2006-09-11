package com.google.enterprise.connector.servlet;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class GetConnectorListTest extends TestCase {
  private static final Logger logger = Logger
    .getLogger(GetConnectorListTest.class.getName());

  /**
   * Test method for
   * {@link com.google.enterprise.connector.servlet.GetConnectorList 
   * #handleDoGet
   * @throws IOException
   * 
   * connectorTypes = null
   */
  public void testHandleDoGet1() throws IOException {
    List connectorTypes = null;
    String expectedResult = 
        "<CmResponse>\n" +
        "  <StatusId>0</StatusId>\n" +
        "  <ConnectorTypes>null</ConnectorTypes>\n" +
        "</CmResponse>\n";
    doTest(connectorTypes, expectedResult);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.servlet.GetConnectorList 
   * #handleDoGet
   * @throws IOException
   * 
   * connectorTypes = {"Documentum", "Sharepoint", "Filenet"}
   */
  public void testHandleDo2() throws IOException {
    List connectorTypes = Arrays.asList(new String[]{
        "Documentum", "Sharepoint", "Filenet"});
    String expectedResult =
        "<CmResponse>\n" +
        "  <StatusId>0</StatusId>\n" +
        "  <ConnectorTypes>\n" +
        "    <ConnectorType>Documentum</ConnectorType>\n" +
        "    <ConnectorType>Sharepoint</ConnectorType>\n" +
        "    <ConnectorType>Filenet</ConnectorType>\n" +
        "  </ConnectorTypes>\n" +
        "</CmResponse>\n";
    doTest(connectorTypes, expectedResult);
  }


  private void doTest(List connectorTypes, String expectedResult)
      throws IOException {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorList.handleDoGet(out, connectorTypes);
    out.flush();
    StringBuffer result = writer.getBuffer();
    logger.info(result.toString());
    logger.info(expectedResult);
    Assert.assertEquals(expectedResult, result.toString());
  }
}
