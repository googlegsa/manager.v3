/**
 * GssAclMonitorLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssAclMonitorLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitor {

    public GssAclMonitorLocator() {
    }


    public GssAclMonitorLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GssAclMonitorLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GssAclMonitorSoap
    private java.lang.String GssAclMonitorSoap_address = "http://gdc04.gdc-psl.net:5555/_vti_bin/GssAcl.asmx";

    public java.lang.String getGssAclMonitorSoapAddress() {
        return GssAclMonitorSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GssAclMonitorSoapWSDDServiceName = "GssAclMonitorSoap";

    public java.lang.String getGssAclMonitorSoapWSDDServiceName() {
        return GssAclMonitorSoapWSDDServiceName;
    }

    public void setGssAclMonitorSoapWSDDServiceName(java.lang.String name) {
        GssAclMonitorSoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType getGssAclMonitorSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GssAclMonitorSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGssAclMonitorSoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType getGssAclMonitorSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub(portAddress, this);
            _stub.setPortName(getGssAclMonitorSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGssAclMonitorSoapEndpointAddress(java.lang.String address) {
        GssAclMonitorSoap_address = address;
    }


    // Use to get a proxy class for GssAclMonitorSoap12
    private java.lang.String GssAclMonitorSoap12_address = "http://gdc04.gdc-psl.net:5555/_vti_bin/GssAcl.asmx";

    public java.lang.String getGssAclMonitorSoap12Address() {
        return GssAclMonitorSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GssAclMonitorSoap12WSDDServiceName = "GssAclMonitorSoap12";

    public java.lang.String getGssAclMonitorSoap12WSDDServiceName() {
        return GssAclMonitorSoap12WSDDServiceName;
    }

    public void setGssAclMonitorSoap12WSDDServiceName(java.lang.String name) {
        GssAclMonitorSoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType getGssAclMonitorSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GssAclMonitorSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGssAclMonitorSoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType getGssAclMonitorSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap12Stub(portAddress, this);
            _stub.setPortName(getGssAclMonitorSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGssAclMonitorSoap12EndpointAddress(java.lang.String address) {
        GssAclMonitorSoap12_address = address;
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
            if (com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub(new java.net.URL(GssAclMonitorSoap_address), this);
                _stub.setPortName(getGssAclMonitorSoapWSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap12Stub(new java.net.URL(GssAclMonitorSoap12_address), this);
                _stub.setPortName(getGssAclMonitorSoap12WSDDServiceName());
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
        if ("GssAclMonitorSoap".equals(inputPortName)) {
            return getGssAclMonitorSoap();
        }
        else if ("GssAclMonitorSoap12".equals(inputPortName)) {
            return getGssAclMonitorSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclMonitor");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclMonitorSoap"));
            ports.add(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclMonitorSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("GssAclMonitorSoap".equals(portName)) {
            setGssAclMonitorSoapEndpointAddress(address);
        }
        else
if ("GssAclMonitorSoap12".equals(portName)) {
            setGssAclMonitorSoap12EndpointAddress(address);
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
