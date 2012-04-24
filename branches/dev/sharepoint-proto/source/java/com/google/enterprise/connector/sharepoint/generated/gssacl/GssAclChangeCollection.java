/**
 * GssAclChangeCollection.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssAclChangeCollection  implements java.io.Serializable {
    private java.lang.String changeToken;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange[] changes;

    private java.lang.String logMessage;

    public GssAclChangeCollection() {
    }

    public GssAclChangeCollection(
           java.lang.String changeToken,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange[] changes,
           java.lang.String logMessage) {
           this.changeToken = changeToken;
           this.changes = changes;
           this.logMessage = logMessage;
    }


    /**
     * Gets the changeToken value for this GssAclChangeCollection.
     *
     * @return changeToken
     */
    public java.lang.String getChangeToken() {
        return changeToken;
    }


    /**
     * Sets the changeToken value for this GssAclChangeCollection.
     *
     * @param changeToken
     */
    public void setChangeToken(java.lang.String changeToken) {
        this.changeToken = changeToken;
    }


    /**
     * Gets the changes value for this GssAclChangeCollection.
     *
     * @return changes
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange[] getChanges() {
        return changes;
    }


    /**
     * Sets the changes value for this GssAclChangeCollection.
     *
     * @param changes
     */
    public void setChanges(com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange[] changes) {
        this.changes = changes;
    }


    /**
     * Gets the logMessage value for this GssAclChangeCollection.
     *
     * @return logMessage
     */
    public java.lang.String getLogMessage() {
        return logMessage;
    }


    /**
     * Sets the logMessage value for this GssAclChangeCollection.
     *
     * @param logMessage
     */
    public void setLogMessage(java.lang.String logMessage) {
        this.logMessage = logMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssAclChangeCollection)) return false;
        GssAclChangeCollection other = (GssAclChangeCollection) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.changeToken==null && other.getChangeToken()==null) ||
             (this.changeToken!=null &&
              this.changeToken.equals(other.getChangeToken()))) &&
            ((this.changes==null && other.getChanges()==null) ||
             (this.changes!=null &&
              java.util.Arrays.equals(this.changes, other.getChanges()))) &&
            ((this.logMessage==null && other.getLogMessage()==null) ||
             (this.logMessage!=null &&
              this.logMessage.equals(other.getLogMessage())));
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
        if (getChangeToken() != null) {
            _hashCode += getChangeToken().hashCode();
        }
        if (getChanges() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getChanges());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getChanges(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLogMessage() != null) {
            _hashCode += getLogMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssAclChangeCollection.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChangeCollection"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("changeToken");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ChangeToken"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("changes");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Changes"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChange"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChange"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("logMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "LogMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
