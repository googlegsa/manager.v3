/**
 * WebsSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.webs;

public interface WebsSoap_PortType extends java.rmi.Remote {
    public com.google.enterprise.connector.sharepoint.generated.webs.GetWebCollectionResponseGetWebCollectionResult getWebCollection() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.GetWebResponseGetWebResult getWeb(java.lang.String webUrl) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.GetListTemplatesResponseGetListTemplatesResult getListTemplates() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.GetAllSubWebCollectionResponseGetAllSubWebCollectionResult getAllSubWebCollection() throws java.rmi.RemoteException;
    public java.lang.String webUrlFromPageUrl(java.lang.String pageUrl) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.GetContentTypesResponseGetContentTypesResult getContentTypes() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.GetContentTypeResponseGetContentTypeResult getContentType(java.lang.String contentTypeId) throws java.rmi.RemoteException;
    public java.lang.String createContentType(java.lang.String displayName, java.lang.String parentType, com.google.enterprise.connector.sharepoint.generated.webs.CreateContentTypeNewFields newFields, com.google.enterprise.connector.sharepoint.generated.webs.CreateContentTypeContentTypeProperties contentTypeProperties) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeResponseUpdateContentTypeResult updateContentType(java.lang.String contentTypeId, com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeContentTypeProperties contentTypeProperties, com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeNewFields newFields, com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeUpdateFields updateFields, com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeDeleteFields deleteFields) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.DeleteContentTypeResponseDeleteContentTypeResult deleteContentType(java.lang.String contentTypeId) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult updateContentTypeXmlDocument(java.lang.String contentTypeId, com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeXmlDocumentNewDocument newDocument) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.RemoveContentTypeXmlDocumentResponseRemoveContentTypeXmlDocumentResult removeContentTypeXmlDocument(java.lang.String contentTypeId, java.lang.String documentUri) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.GetColumnsResponseGetColumnsResult getColumns() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateColumnsResponseUpdateColumnsResult updateColumns(com.google.enterprise.connector.sharepoint.generated.webs.UpdateColumnsNewFields newFields, com.google.enterprise.connector.sharepoint.generated.webs.UpdateColumnsUpdateFields updateFields, com.google.enterprise.connector.sharepoint.generated.webs.UpdateColumnsDeleteFields deleteFields) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.webs.CustomizedPageStatus getCustomizedPageStatus(java.lang.String fileUrl) throws java.rmi.RemoteException;
    public void revertFileContentStream(java.lang.String fileUrl) throws java.rmi.RemoteException;
    public void revertAllFileContentStreams() throws java.rmi.RemoteException;
    public void customizeCss(java.lang.String cssFile) throws java.rmi.RemoteException;
    public void revertCss(java.lang.String cssFile) throws java.rmi.RemoteException;
    public java.lang.String getActivatedFeatures() throws java.rmi.RemoteException;
}
