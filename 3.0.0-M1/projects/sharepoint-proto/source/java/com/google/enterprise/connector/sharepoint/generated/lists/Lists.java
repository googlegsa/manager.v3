/**
 * Lists.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.lists;

public interface Lists extends javax.xml.rpc.Service {
    public java.lang.String getListsSoap12Address();

    public com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_PortType getListsSoap12() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_PortType getListsSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getListsSoapAddress();

    public com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_PortType getListsSoap() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_PortType getListsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
