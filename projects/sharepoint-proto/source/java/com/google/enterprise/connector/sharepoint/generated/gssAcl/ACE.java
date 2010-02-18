/**
 * ACE.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssAcl;

public class ACE  implements java.io.Serializable {
    private java.lang.String principal;

    private com.google.enterprise.connector.sharepoint.generated.gssAcl.PrincipalType type;

    private java.lang.String[] grantRightMask;

    private java.lang.String[] denyRightMask;

    public ACE() {
    }

    public ACE(
           java.lang.String principal,
           com.google.enterprise.connector.sharepoint.generated.gssAcl.PrincipalType type,
           java.lang.String[] grantRightMask,
           java.lang.String[] denyRightMask) {
           this.principal = principal;
           this.type = type;
           this.grantRightMask = grantRightMask;
           this.denyRightMask = denyRightMask;
    }


    /**
     * Gets the principal value for this ACE.
     * 
     * @return principal
     */
    public java.lang.String getPrincipal() {
        return principal;
    }


    /**
     * Sets the principal value for this ACE.
     * 
     * @param principal
     */
    public void setPrincipal(java.lang.String principal) {
        this.principal = principal;
    }


    /**
     * Gets the type value for this ACE.
     * 
     * @return type
     */
    public com.google.enterprise.connector.sharepoint.generated.gssAcl.PrincipalType getType() {
        return type;
    }


    /**
     * Sets the type value for this ACE.
     * 
     * @param type
     */
    public void setType(com.google.enterprise.connector.sharepoint.generated.gssAcl.PrincipalType type) {
        this.type = type;
    }


    /**
     * Gets the grantRightMask value for this ACE.
     * 
     * @return grantRightMask
     */
    public java.lang.String[] getGrantRightMask() {
        return grantRightMask;
    }


    /**
     * Sets the grantRightMask value for this ACE.
     * 
     * @param grantRightMask
     */
    public void setGrantRightMask(java.lang.String[] grantRightMask) {
        this.grantRightMask = grantRightMask;
    }


    /**
     * Gets the denyRightMask value for this ACE.
     * 
     * @return denyRightMask
     */
    public java.lang.String[] getDenyRightMask() {
        return denyRightMask;
    }


    /**
     * Sets the denyRightMask value for this ACE.
     * 
     * @param denyRightMask
     */
    public void setDenyRightMask(java.lang.String[] denyRightMask) {
        this.denyRightMask = denyRightMask;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ACE)) return false;
        ACE other = (ACE) obj;
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
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.grantRightMask==null && other.getGrantRightMask()==null) || 
             (this.grantRightMask!=null &&
              java.util.Arrays.equals(this.grantRightMask, other.getGrantRightMask()))) &&
            ((this.denyRightMask==null && other.getDenyRightMask()==null) || 
             (this.denyRightMask!=null &&
              java.util.Arrays.equals(this.denyRightMask, other.getDenyRightMask())));
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
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getGrantRightMask() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGrantRightMask());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGrantRightMask(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDenyRightMask() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDenyRightMask());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDenyRightMask(), i);
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
        new org.apache.axis.description.TypeDesc(ACE.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ACE"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("principal");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Principal"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Type"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "PrincipalType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("grantRightMask");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GrantRightMask"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPBasePermissions"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("denyRightMask");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "DenyRightMask"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPBasePermissions"));
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
