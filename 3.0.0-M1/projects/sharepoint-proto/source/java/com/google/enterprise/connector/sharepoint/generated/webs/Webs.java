/**
 * Webs.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.webs;

public interface Webs extends javax.xml.rpc.Service {
    public java.lang.String getWebsSoap12Address();

    public com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_PortType getWebsSoap12() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_PortType getWebsSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getWebsSoapAddress();

    public com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_PortType getWebsSoap() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.webs.WebsSoap_PortType getWebsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
