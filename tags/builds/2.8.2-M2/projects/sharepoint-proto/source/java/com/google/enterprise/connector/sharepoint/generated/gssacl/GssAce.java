/**
 * GssAce.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssAce  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal principal;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission permission;

    public GssAce() {
    }

    public GssAce(
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal principal,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission permission) {
           this.principal = principal;
           this.permission = permission;
    }


    /**
     * Gets the principal value for this GssAce.
     *
     * @return principal
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal getPrincipal() {
        return principal;
    }


    /**
     * Sets the principal value for this GssAce.
     *
     * @param principal
     */
    public void setPrincipal(com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal principal) {
        this.principal = principal;
    }


    /**
     * Gets the permission value for this GssAce.
     *
     * @return permission
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission getPermission() {
        return permission;
    }


    /**
     * Sets the permission value for this GssAce.
     *
     * @param permission
     */
    public void setPermission(com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission permission) {
        this.permission = permission;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssAce)) return false;
        GssAce other = (GssAce) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.principal==null && other.getPrincipal()==null) ||
             (this.principal!=null &&
              this.principal.equals(other.getPrincipal()))) &&
            ((this.permission==null && other.getPermission()==null) ||
             (this.permission!=null &&
              this.permission.equals(other.getPermission())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getPrincipal() != null) {
            _hashCode += getPrincipal().hashCode();
        }
        if (getPermission() != null) {
            _hashCode += getPermission().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssAce.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAce"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("principal");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Principal"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("permission");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Permission"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssSharepointPermission"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
