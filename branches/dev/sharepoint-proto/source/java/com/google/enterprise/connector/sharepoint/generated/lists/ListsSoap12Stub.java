/**
 * ListsSoap12Stub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.lists;

public class ListsSoap12Stub extends org.apache.axis.client.Stub implements com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[28];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListResponse>GetListResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListResponseGetListResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListAndView");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListAndViewResponse>GetListAndViewResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponseGetListAndViewResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListAndViewResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DeleteList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("AddList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "description"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "templateID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddListResponse>AddListResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.AddListResponseAddListResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddListResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("AddListFromFeature");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "description"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "featureID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "templateID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddListFromFeatureResponse>AddListFromFeatureResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponseAddListFromFeatureResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddListFromFeatureResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UpdateList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>listProperties"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateListListProperties.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "newFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>newFields"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateListNewFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "updateFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>updateFields"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateListUpdateFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "deleteFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>deleteFields"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateListDeleteFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listVersion"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateListResponse>UpdateListResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponseUpdateListResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateListResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListCollection");
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListCollectionResponse>GetListCollectionResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponseGetListCollectionResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListCollectionResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListItems");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "query"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItems>query"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItems>viewFields"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "rowLimit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "queryOptions"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItems>queryOptions"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "webID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemsResponse>GetListItemsResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListItemsResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListItemChanges");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChanges>viewFields"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesViewFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "since"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contains"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChanges>contains"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesContains.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesResponse>GetListItemChangesResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListItemChangesResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListItemChangesSinceToken");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "query"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>query"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQuery.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "viewFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>viewFields"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenViewFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "rowLimit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "queryOptions"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>queryOptions"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQueryOptions.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "changeToken"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contains"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>contains"), com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenContains.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceTokenResponse>GetListItemChangesSinceTokenResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListItemChangesSinceTokenResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UpdateListItems");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "updates"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateListItems>updates"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsUpdates.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateListItemsResponse>UpdateListItemsResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponseUpdateListItemsResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateListItemsResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("AddDiscussionBoardItem");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "message"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddDiscussionBoardItemResponse>AddDiscussionBoardItemResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponseAddDiscussionBoardItemResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddDiscussionBoardItemResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetVersionCollection");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "strlistID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "strlistItemID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "strFieldName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetVersionCollectionResponse>GetVersionCollectionResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponseGetVersionCollectionResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetVersionCollectionResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("AddAttachment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listItemID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "fileName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "attachment"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddAttachmentResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetAttachmentCollection");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listItemID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetAttachmentCollectionResponse>GetAttachmentCollectionResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetAttachmentCollectionResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DeleteAttachment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listItemID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "url"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CheckOutFile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "pageUrl"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "checkoutToLocal"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "lastmodified"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CheckOutFileResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UndoCheckOut");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "pageUrl"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UndoCheckOutResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CheckInFile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "pageUrl"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "comment"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CheckinType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CheckInFileResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListContentTypes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListContentTypesResponse>GetListContentTypesResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponseGetListContentTypesResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListContentTypesResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListContentType");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListContentTypeResponse>GetListContentTypeResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponseGetListContentTypeResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListContentTypeResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CreateContentType");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "displayName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "parentType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "fields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>CreateContentType>fields"), com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>CreateContentType>contentTypeProperties"), com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeContentTypeProperties.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "addToView"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CreateContentTypeResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UpdateContentType");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>contentTypeProperties"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeContentTypeProperties.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "newFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>newFields"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeNewFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "updateFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>updateFields"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeUpdateFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "deleteFields"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>deleteFields"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeDeleteFields.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "addToView"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypeResponse>UpdateContentTypeResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponseUpdateContentTypeResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateContentTypeResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DeleteContentType");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>DeleteContentTypeResponse>DeleteContentTypeResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponseDeleteContentTypeResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "DeleteContentTypeResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UpdateContentTypeXmlDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "newDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypeXmlDocument>newDocument"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentNewDocument.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypeXmlDocumentResponse>UpdateContentTypeXmlDocumentResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateContentTypeXmlDocumentResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[24] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UpdateContentTypesXmlDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "newDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypesXmlDocument>newDocument"), com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentNewDocument.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypesXmlDocumentResponse>UpdateContentTypesXmlDocumentResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponseUpdateContentTypesXmlDocumentResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateContentTypesXmlDocumentResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[25] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DeleteContentTypeXmlDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "documentUri"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>DeleteContentTypeXmlDocumentResponse>DeleteContentTypeXmlDocumentResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponseDeleteContentTypeXmlDocumentResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "DeleteContentTypeXmlDocumentResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[26] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ApplyContentTypeToList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "webUrl"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "listName"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>ApplyContentTypeToListResponse>ApplyContentTypeToListResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponseApplyContentTypeToListResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "ApplyContentTypeToListResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[27] = oper;

    }

    public ListsSoap12Stub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ListsSoap12Stub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ListsSoap12Stub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddDiscussionBoardItemResponse>AddDiscussionBoardItemResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponseAddDiscussionBoardItemResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddListFromFeatureResponse>AddListFromFeatureResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponseAddListFromFeatureResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>AddListResponse>AddListResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddListResponseAddListResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>ApplyContentTypeToListResponse>ApplyContentTypeToListResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponseApplyContentTypeToListResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>CreateContentType>contentTypeProperties");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeContentTypeProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>CreateContentType>fields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>DeleteContentTypeResponse>DeleteContentTypeResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponseDeleteContentTypeResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>DeleteContentTypeXmlDocumentResponse>DeleteContentTypeXmlDocumentResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponseDeleteContentTypeXmlDocumentResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetAttachmentCollectionResponse>GetAttachmentCollectionResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListAndViewResponse>GetListAndViewResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponseGetListAndViewResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListCollectionResponse>GetListCollectionResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponseGetListCollectionResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListContentTypeResponse>GetListContentTypeResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponseGetListContentTypeResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListContentTypesResponse>GetListContentTypesResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponseGetListContentTypesResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChanges>contains");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesContains.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChanges>viewFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesViewFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesResponse>GetListItemChangesResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>contains");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenContains.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>query");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQuery.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>queryOptions");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQueryOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceToken>viewFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenViewFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemChangesSinceTokenResponse>GetListItemChangesSinceTokenResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItems>query");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItems>queryOptions");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItems>viewFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListItemsResponse>GetListItemsResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetListResponse>GetListResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListResponseGetListResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>GetVersionCollectionResponse>GetVersionCollectionResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponseGetVersionCollectionResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>contentTypeProperties");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeContentTypeProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>deleteFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeDeleteFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>newFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeNewFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>updateFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeUpdateFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypeResponse>UpdateContentTypeResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponseUpdateContentTypeResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypesXmlDocument>newDocument");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentNewDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypesXmlDocumentResponse>UpdateContentTypesXmlDocumentResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponseUpdateContentTypesXmlDocumentResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypeXmlDocument>newDocument");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentNewDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentTypeXmlDocumentResponse>UpdateContentTypeXmlDocumentResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>deleteFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListDeleteFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>listProperties");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListListProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>newFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListNewFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateList>updateFields");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListUpdateFields.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateListItems>updates");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsUpdates.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateListItemsResponse>UpdateListItemsResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponseUpdateListItemsResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateListResponse>UpdateListResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponseUpdateListResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddAttachment");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddAttachment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddAttachmentResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddAttachmentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddDiscussionBoardItem");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItem.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddDiscussionBoardItemResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddList");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddListFromFeature");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeature.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddListFromFeatureResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">AddListResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.AddListResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">ApplyContentTypeToList");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">ApplyContentTypeToListResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">CheckInFile");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CheckInFile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">CheckInFileResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CheckInFileResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">CheckOutFile");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CheckOutFile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">CheckOutFileResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CheckOutFileResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">CreateContentType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CreateContentType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">CreateContentTypeResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteAttachment");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteAttachment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteAttachmentResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteAttachmentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteContentType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteContentTypeResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteContentTypeXmlDocument");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteContentTypeXmlDocumentResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteList");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">DeleteListResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.DeleteListResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetAttachmentCollection");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollection.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetAttachmentCollectionResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListAndView");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListAndView.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListAndViewResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListCollection");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListCollection.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListCollectionResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListContentType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListContentType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListContentTypeResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListContentTypes");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypes.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListContentTypesResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListItemChanges");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChanges.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListItemChangesResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListItemChangesSinceToken");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceToken.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListItemChangesSinceTokenResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListItems");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItems.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetListItemsResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetVersionCollection");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollection.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetVersionCollectionResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UndoCheckOut");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UndoCheckOut.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UndoCheckOutResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UndoCheckOutResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentTypeResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentTypesXmlDocument");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentTypesXmlDocumentResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentTypeXmlDocument");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentTypeXmlDocumentResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateList");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateListItems");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItems.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateListItemsResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateListResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListResponseGetListResult getList(java.lang.String listName) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListResponseGetListResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListResponseGetListResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListResponseGetListResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponseGetListAndViewResult getListAndView(java.lang.String listName, java.lang.String viewName) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListAndView");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListAndView"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, viewName});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponseGetListAndViewResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponseGetListAndViewResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListAndViewResponseGetListAndViewResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public void deleteList(java.lang.String listName) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/DeleteList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "DeleteList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.AddListResponseAddListResult addList(java.lang.String listName, java.lang.String description, int templateID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/AddList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, description, new java.lang.Integer(templateID)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.AddListResponseAddListResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.AddListResponseAddListResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.AddListResponseAddListResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponseAddListFromFeatureResult addListFromFeature(java.lang.String listName, java.lang.String description, java.lang.String featureID, int templateID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/AddListFromFeature");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddListFromFeature"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, description, featureID, new java.lang.Integer(templateID)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponseAddListFromFeatureResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponseAddListFromFeatureResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.AddListFromFeatureResponseAddListFromFeatureResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponseUpdateListResult updateList(java.lang.String listName, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListListProperties listProperties, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListNewFields newFields, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListUpdateFields updateFields, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListDeleteFields deleteFields, java.lang.String listVersion) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/UpdateList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, listProperties, newFields, updateFields, deleteFields, listVersion});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponseUpdateListResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponseUpdateListResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListResponseUpdateListResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponseGetListCollectionResult getListCollection() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListCollection");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListCollection"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponseGetListCollectionResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponseGetListCollectionResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListCollectionResponseGetListCollectionResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult getListItems(java.lang.String listName, java.lang.String viewName, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery query, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields viewFields, java.lang.String rowLimit, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions queryOptions, java.lang.String webID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListItems");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListItems"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, viewName, query, viewFields, rowLimit, queryOptions, webID});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult getListItemChanges(java.lang.String listName, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesViewFields viewFields, java.lang.String since, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesContains contains) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListItemChanges");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListItemChanges"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, viewFields, since, contains});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesResponseGetListItemChangesResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult getListItemChangesSinceToken(java.lang.String listName, java.lang.String viewName, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQuery query, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenViewFields viewFields, java.lang.String rowLimit, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQueryOptions queryOptions, java.lang.String changeToken, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenContains contains) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListItemChangesSinceToken");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListItemChangesSinceToken"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, viewName, query, viewFields, rowLimit, queryOptions, changeToken, contains});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponseUpdateListItemsResult updateListItems(java.lang.String listName, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsUpdates updates) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/UpdateListItems");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateListItems"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, updates});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponseUpdateListItemsResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponseUpdateListItemsResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.UpdateListItemsResponseUpdateListItemsResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponseAddDiscussionBoardItemResult addDiscussionBoardItem(java.lang.String listName, byte[] message) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/AddDiscussionBoardItem");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddDiscussionBoardItem"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, message});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponseAddDiscussionBoardItemResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponseAddDiscussionBoardItemResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.AddDiscussionBoardItemResponseAddDiscussionBoardItemResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponseGetVersionCollectionResult getVersionCollection(java.lang.String strlistID, java.lang.String strlistItemID, java.lang.String strFieldName) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetVersionCollection");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetVersionCollection"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {strlistID, strlistItemID, strFieldName});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponseGetVersionCollectionResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponseGetVersionCollectionResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetVersionCollectionResponseGetVersionCollectionResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public java.lang.String addAttachment(java.lang.String listName, java.lang.String listItemID, java.lang.String fileName, byte[] attachment) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/AddAttachment");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AddAttachment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, listItemID, fileName, attachment});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult getAttachmentCollection(java.lang.String listName, java.lang.String listItemID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetAttachmentCollection");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetAttachmentCollection"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, listItemID});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public void deleteAttachment(java.lang.String listName, java.lang.String listItemID, java.lang.String url) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/DeleteAttachment");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "DeleteAttachment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, listItemID, url});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public boolean checkOutFile(java.lang.String pageUrl, java.lang.String checkoutToLocal, java.lang.String lastmodified) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/CheckOutFile");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CheckOutFile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pageUrl, checkoutToLocal, lastmodified});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public boolean undoCheckOut(java.lang.String pageUrl) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/UndoCheckOut");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UndoCheckOut"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pageUrl});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public boolean checkInFile(java.lang.String pageUrl, java.lang.String comment, java.lang.String checkinType) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/CheckInFile");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CheckInFile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pageUrl, comment, checkinType});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponseGetListContentTypesResult getListContentTypes(java.lang.String listName, java.lang.String contentTypeId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListContentTypes");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListContentTypes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, contentTypeId});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponseGetListContentTypesResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponseGetListContentTypesResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypesResponseGetListContentTypesResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponseGetListContentTypeResult getListContentType(java.lang.String listName, java.lang.String contentTypeId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/GetListContentType");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetListContentType"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, contentTypeId});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponseGetListContentTypeResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponseGetListContentTypeResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.GetListContentTypeResponseGetListContentTypeResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public java.lang.String createContentType(java.lang.String listName, java.lang.String displayName, java.lang.String parentType, com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeFields fields, com.google.enterprise.connector.sharepoint.generated.lists.CreateContentTypeContentTypeProperties contentTypeProperties, java.lang.String addToView) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/CreateContentType");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CreateContentType"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, displayName, parentType, fields, contentTypeProperties, addToView});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponseUpdateContentTypeResult updateContentType(java.lang.String listName, java.lang.String contentTypeId, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeContentTypeProperties contentTypeProperties, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeNewFields newFields, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeUpdateFields updateFields, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeDeleteFields deleteFields, java.lang.String addToView) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/UpdateContentType");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateContentType"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, contentTypeId, contentTypeProperties, newFields, updateFields, deleteFields, addToView});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponseUpdateContentTypeResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponseUpdateContentTypeResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeResponseUpdateContentTypeResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponseDeleteContentTypeResult deleteContentType(java.lang.String listName, java.lang.String contentTypeId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/DeleteContentType");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "DeleteContentType"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, contentTypeId});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponseDeleteContentTypeResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponseDeleteContentTypeResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeResponseDeleteContentTypeResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult updateContentTypeXmlDocument(java.lang.String listName, java.lang.String contentTypeId, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentNewDocument newDocument) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/UpdateContentTypeXmlDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateContentTypeXmlDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, contentTypeId, newDocument});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypeXmlDocumentResponseUpdateContentTypeXmlDocumentResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponseUpdateContentTypesXmlDocumentResult updateContentTypesXmlDocument(java.lang.String listName, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentNewDocument newDocument) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/UpdateContentTypesXmlDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "UpdateContentTypesXmlDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, newDocument});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponseUpdateContentTypesXmlDocumentResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponseUpdateContentTypesXmlDocumentResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.UpdateContentTypesXmlDocumentResponseUpdateContentTypesXmlDocumentResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponseDeleteContentTypeXmlDocumentResult deleteContentTypeXmlDocument(java.lang.String listName, java.lang.String contentTypeId, java.lang.String documentUri) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/DeleteContentTypeXmlDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "DeleteContentTypeXmlDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listName, contentTypeId, documentUri});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponseDeleteContentTypeXmlDocumentResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponseDeleteContentTypeXmlDocumentResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.DeleteContentTypeXmlDocumentResponseDeleteContentTypeXmlDocumentResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponseApplyContentTypeToListResult applyContentTypeToList(java.lang.String webUrl, java.lang.String contentTypeId, java.lang.String listName) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://schemas.microsoft.com/sharepoint/soap/ApplyContentTypeToList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "ApplyContentTypeToList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {webUrl, contentTypeId, listName});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponseApplyContentTypeToListResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponseApplyContentTypeToListResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.lists.ApplyContentTypeToListResponseApplyContentTypeToListResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
