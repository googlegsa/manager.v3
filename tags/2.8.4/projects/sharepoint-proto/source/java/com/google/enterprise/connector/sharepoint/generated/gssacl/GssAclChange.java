/**
 * GssAclChange.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssAclChange  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType changedObject;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.SPChangeType changeType;

    private java.lang.String hint;

    private boolean isEffectiveInCurrentWeb;

    public GssAclChange() {
    }

    public GssAclChange(
           com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType changedObject,
           com.google.enterprise.connector.sharepoint.generated.gssacl.SPChangeType changeType,
           java.lang.String hint,
           boolean isEffectiveInCurrentWeb) {
           this.changedObject = changedObject;
           this.changeType = changeType;
           this.hint = hint;
           this.isEffectiveInCurrentWeb = isEffectiveInCurrentWeb;
    }


    /**
     * Gets the changedObject value for this GssAclChange.
     *
     * @return changedObject
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType getChangedObject() {
        return changedObject;
    }


    /**
     * Sets the changedObject value for this GssAclChange.
     *
     * @param changedObject
     */
    public void setChangedObject(com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType changedObject) {
        this.changedObject = changedObject;
    }


    /**
     * Gets the changeType value for this GssAclChange.
     *
     * @return changeType
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.SPChangeType getChangeType() {
        return changeType;
    }


    /**
     * Sets the changeType value for this GssAclChange.
     *
     * @param changeType
     */
    public void setChangeType(com.google.enterprise.connector.sharepoint.generated.gssacl.SPChangeType changeType) {
        this.changeType = changeType;
    }


    /**
     * Gets the hint value for this GssAclChange.
     *
     * @return hint
     */
    public java.lang.String getHint() {
        return hint;
    }


    /**
     * Sets the hint value for this GssAclChange.
     *
     * @param hint
     */
    public void setHint(java.lang.String hint) {
        this.hint = hint;
    }


    /**
     * Gets the isEffectiveInCurrentWeb value for this GssAclChange.
     *
     * @return isEffectiveInCurrentWeb
     */
    public boolean isIsEffectiveInCurrentWeb() {
        return isEffectiveInCurrentWeb;
    }


    /**
     * Sets the isEffectiveInCurrentWeb value for this GssAclChange.
     *
     * @param isEffectiveInCurrentWeb
     */
    public void setIsEffectiveInCurrentWeb(boolean isEffectiveInCurrentWeb) {
        this.isEffectiveInCurrentWeb = isEffectiveInCurrentWeb;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssAclChange)) return false;
        GssAclChange other = (GssAclChange) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.changedObject==null && other.getChangedObject()==null) ||
             (this.changedObject!=null &&
              this.changedObject.equals(other.getChangedObject()))) &&
            ((this.changeType==null && other.getChangeType()==null) ||
             (this.changeType!=null &&
              this.changeType.equals(other.getChangeType()))) &&
            ((this.hint==null && other.getHint()==null) ||
             (this.hint!=null &&
              this.hint.equals(other.getHint()))) &&
            this.isEffectiveInCurrentWeb == other.isIsEffectiveInCurrentWeb();
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
        if (getChangedObject() != null) {
            _hashCode += getChangedObject().hashCode();
        }
        if (getChangeType() != null) {
            _hashCode += getChangeType().hashCode();
        }
        if (getHint() != null) {
            _hashCode += getHint().hashCode();
        }
        _hashCode += (isIsEffectiveInCurrentWeb() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssAclChange.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChange"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("changedObject");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ChangedObject"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ObjectType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("changeType");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ChangeType"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPChangeType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hint");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Hint"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isEffectiveInCurrentWeb");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "IsEffectiveInCurrentWeb"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
