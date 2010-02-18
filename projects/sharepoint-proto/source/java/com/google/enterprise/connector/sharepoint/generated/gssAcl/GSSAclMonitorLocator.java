/**
 * GSSAclMonitorLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssAcl;

public class GSSAclMonitorLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitor {

    public GSSAclMonitorLocator() {
    }


    public GSSAclMonitorLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GSSAclMonitorLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GSSAclMonitorSoap
    private java.lang.String GSSAclMonitorSoap_address = "http://gdc04.persistent.co.in:4444/SubSite1/_vti_bin/GssAcl.asmx";

    public java.lang.String getGSSAclMonitorSoapAddress() {
        return GSSAclMonitorSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GSSAclMonitorSoapWSDDServiceName = "GSSAclMonitorSoap";

    public java.lang.String getGSSAclMonitorSoapWSDDServiceName() {
        return GSSAclMonitorSoapWSDDServiceName;
    }

    public void setGSSAclMonitorSoapWSDDServiceName(java.lang.String name) {
        GSSAclMonitorSoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_PortType getGSSAclMonitorSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GSSAclMonitorSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGSSAclMonitorSoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_PortType getGSSAclMonitorSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_BindingStub(portAddress, this);
            _stub.setPortName(getGSSAclMonitorSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGSSAclMonitorSoapEndpointAddress(java.lang.String address) {
        GSSAclMonitorSoap_address = address;
    }


    // Use to get a proxy class for GSSAclMonitorSoap12
    private java.lang.String GSSAclMonitorSoap12_address = "http://gdc04.persistent.co.in:4444/SubSite1/_vti_bin/GssAcl.asmx";

    public java.lang.String getGSSAclMonitorSoap12Address() {
        return GSSAclMonitorSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GSSAclMonitorSoap12WSDDServiceName = "GSSAclMonitorSoap12";

    public java.lang.String getGSSAclMonitorSoap12WSDDServiceName() {
        return GSSAclMonitorSoap12WSDDServiceName;
    }

    public void setGSSAclMonitorSoap12WSDDServiceName(java.lang.String name) {
        GSSAclMonitorSoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_PortType getGSSAclMonitorSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GSSAclMonitorSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGSSAclMonitorSoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_PortType getGSSAclMonitorSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap12Stub(portAddress, this);
            _stub.setPortName(getGSSAclMonitorSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGSSAclMonitorSoap12EndpointAddress(java.lang.String address) {
        GSSAclMonitorSoap12_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_BindingStub(new java.net.URL(GSSAclMonitorSoap_address), this);
                _stub.setPortName(getGSSAclMonitorSoapWSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gssAcl.GSSAclMonitorSoap12Stub(new java.net.URL(GSSAclMonitorSoap12_address), this);
                _stub.setPortName(getGSSAclMonitorSoap12WSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("GSSAclMonitorSoap".equals(inputPortName)) {
            return getGSSAclMonitorSoap();
        }
        else if ("GSSAclMonitorSoap12".equals(inputPortName)) {
            return getGSSAclMonitorSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GSSAclMonitor");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GSSAclMonitorSoap"));
            ports.add(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GSSAclMonitorSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GSSAclMonitorSoap".equals(portName)) {
            setGSSAclMonitorSoapEndpointAddress(address);
        }
        else 
if ("GSSAclMonitorSoap12".equals(portName)) {
            setGSSAclMonitorSoap12EndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
