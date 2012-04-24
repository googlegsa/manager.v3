/**
 * GssAclMonitorSoap12Stub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssAclMonitorSoap12Stub extends org.apache.axis.client.Stub implements com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CheckConnectivity");
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "CheckConnectivityResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetAclForUrls");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "urls"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString"), java.lang.String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetAclForUrlsResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetAclForUrlsResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetAclChangesSinceToken");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "fromChangeToken"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "toChangeToken"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetAclChangesSinceTokenResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetAclChangesSinceTokenResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ResolveSPGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "groupId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString"), java.lang.String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssResolveSPGroupResult"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ResolveSPGroupResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListsWithInheritingRoleAssignments");
        oper.setReturnType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString"));
        oper.setReturnClass(java.lang.String[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetListsWithInheritingRoleAssignmentsResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "string"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListItemsWithInheritingRoleAssignments");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "listGuId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "rowLimit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "lastItemId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetListItemsWithInheritingRoleAssignments"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetListItemsWithInheritingRoleAssignmentsResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

    }

    public GssAclMonitorSoap12Stub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public GssAclMonitorSoap12Stub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public GssAclMonitorSoap12Stub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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
            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfGssAce");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAce");
            qName2 = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAce");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfGssAcl");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAcl");
            qName2 = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAcl");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfGssAclChange");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChange");
            qName2 = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChange");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfGssPrincipal");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal");
            qName2 = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "string");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAce");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAcl");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclBaseResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclBaseResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChange");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChangeCollection");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetAclChangesSinceTokenResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetAclForUrlsResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetListItemsWithInheritingRoleAssignments");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssResolveSPGroupResult");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssSharepointPermission");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ObjectType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "PrincipalType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPChangeType");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssacl.SPChangeType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://microsoft.com/wsdl/types/", "guid");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

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

    public java.lang.String checkConnectivity() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssAcl.generated.sharepoint.connector.enterprise.google.com/CheckConnectivity");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "CheckConnectivity"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

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

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult getAclForUrls(java.lang.String[] urls) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssAcl.generated.sharepoint.connector.enterprise.google.com/GetAclForUrls");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetAclForUrls"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {urls});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult getAclChangesSinceToken(java.lang.String fromChangeToken, java.lang.String toChangeToken) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssAcl.generated.sharepoint.connector.enterprise.google.com/GetAclChangesSinceToken");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetAclChangesSinceToken"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {fromChangeToken, toChangeToken});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult resolveSPGroup(java.lang.String[] groupId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssAcl.generated.sharepoint.connector.enterprise.google.com/ResolveSPGroup");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ResolveSPGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {groupId});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public java.lang.String[] getListsWithInheritingRoleAssignments() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssAcl.generated.sharepoint.connector.enterprise.google.com/GetListsWithInheritingRoleAssignments");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetListsWithInheritingRoleAssignments"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String[]) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments getListItemsWithInheritingRoleAssignments(java.lang.String listGuId, int rowLimit, int lastItemId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssAcl.generated.sharepoint.connector.enterprise.google.com/GetListItemsWithInheritingRoleAssignments");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GetListItemsWithInheritingRoleAssignments"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listGuId, new java.lang.Integer(rowLimit), new java.lang.Integer(lastItemId)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
