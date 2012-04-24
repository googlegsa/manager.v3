/**
 * GssAclMonitorSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public interface GssAclMonitorSoap_PortType extends java.rmi.Remote {
    public java.lang.String checkConnectivity() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult getAclForUrls(java.lang.String[] urls) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult getAclChangesSinceToken(java.lang.String fromChangeToken, java.lang.String toChangeToken) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult resolveSPGroup(java.lang.String[] groupId) throws java.rmi.RemoteException;
    public java.lang.String[] getListsWithInheritingRoleAssignments() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments getListItemsWithInheritingRoleAssignments(java.lang.String listGuId, int rowLimit, int lastItemId) throws java.rmi.RemoteException;
}
