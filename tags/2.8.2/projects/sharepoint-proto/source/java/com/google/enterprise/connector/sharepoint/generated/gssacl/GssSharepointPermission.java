/**
 * GssSharepointPermission.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssSharepointPermission  implements java.io.Serializable {
    private java.lang.String[] allowedPermissions;

    private java.lang.String[] deniedPermission;

    public GssSharepointPermission() {
    }

    public GssSharepointPermission(
           java.lang.String[] allowedPermissions,
           java.lang.String[] deniedPermission) {
           this.allowedPermissions = allowedPermissions;
           this.deniedPermission = deniedPermission;
    }


    /**
     * Gets the allowedPermissions value for this GssSharepointPermission.
     *
     * @return allowedPermissions
     */
    public java.lang.String[] getAllowedPermissions() {
        return allowedPermissions;
    }


    /**
     * Sets the allowedPermissions value for this GssSharepointPermission.
     *
     * @param allowedPermissions
     */
    public void setAllowedPermissions(java.lang.String[] allowedPermissions) {
        this.allowedPermissions = allowedPermissions;
    }


    /**
     * Gets the deniedPermission value for this GssSharepointPermission.
     *
     * @return deniedPermission
     */
    public java.lang.String[] getDeniedPermission() {
        return deniedPermission;
    }


    /**
     * Sets the deniedPermission value for this GssSharepointPermission.
     *
     * @param deniedPermission
     */
    public void setDeniedPermission(java.lang.String[] deniedPermission) {
        this.deniedPermission = deniedPermission;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssSharepointPermission)) return false;
        GssSharepointPermission other = (GssSharepointPermission) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.allowedPermissions==null && other.getAllowedPermissions()==null) ||
             (this.allowedPermissions!=null &&
              java.util.Arrays.equals(this.allowedPermissions, other.getAllowedPermissions()))) &&
            ((this.deniedPermission==null && other.getDeniedPermission()==null) ||
             (this.deniedPermission!=null &&
              java.util.Arrays.equals(this.deniedPermission, other.getDeniedPermission())));
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
        if (getAllowedPermissions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAllowedPermissions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAllowedPermissions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDeniedPermission() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDeniedPermission());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDeniedPermission(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssSharepointPermission.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssSharepointPermission"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allowedPermissions");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "AllowedPermissions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deniedPermission");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "DeniedPermission"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "string"));
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
