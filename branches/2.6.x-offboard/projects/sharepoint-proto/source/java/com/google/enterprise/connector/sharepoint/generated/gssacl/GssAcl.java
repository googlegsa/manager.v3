/**
 * GssAcl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssAcl  implements java.io.Serializable {
    private java.lang.String entityUrl;

    private java.lang.String owner;

    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce[] allAce;

    private java.lang.String logMessage;

    public GssAcl() {
    }

    public GssAcl(
           java.lang.String entityUrl,
           java.lang.String owner,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce[] allAce,
           java.lang.String logMessage) {
           this.entityUrl = entityUrl;
           this.owner = owner;
           this.allAce = allAce;
           this.logMessage = logMessage;
    }


    /**
     * Gets the entityUrl value for this GssAcl.
     *
     * @return entityUrl
     */
    public java.lang.String getEntityUrl() {
        return entityUrl;
    }


    /**
     * Sets the entityUrl value for this GssAcl.
     *
     * @param entityUrl
     */
    public void setEntityUrl(java.lang.String entityUrl) {
        this.entityUrl = entityUrl;
    }


    /**
     * Gets the owner value for this GssAcl.
     *
     * @return owner
     */
    public java.lang.String getOwner() {
        return owner;
    }


    /**
     * Sets the owner value for this GssAcl.
     *
     * @param owner
     */
    public void setOwner(java.lang.String owner) {
        this.owner = owner;
    }


    /**
     * Gets the allAce value for this GssAcl.
     *
     * @return allAce
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce[] getAllAce() {
        return allAce;
    }


    /**
     * Sets the allAce value for this GssAcl.
     *
     * @param allAce
     */
    public void setAllAce(com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce[] allAce) {
        this.allAce = allAce;
    }


    /**
     * Gets the logMessage value for this GssAcl.
     *
     * @return logMessage
     */
    public java.lang.String getLogMessage() {
        return logMessage;
    }


    /**
     * Sets the logMessage value for this GssAcl.
     *
     * @param logMessage
     */
    public void setLogMessage(java.lang.String logMessage) {
        this.logMessage = logMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssAcl)) return false;
        GssAcl other = (GssAcl) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.entityUrl==null && other.getEntityUrl()==null) ||
             (this.entityUrl!=null &&
              this.entityUrl.equals(other.getEntityUrl()))) &&
            ((this.owner==null && other.getOwner()==null) ||
             (this.owner!=null &&
              this.owner.equals(other.getOwner()))) &&
            ((this.allAce==null && other.getAllAce()==null) ||
             (this.allAce!=null &&
              java.util.Arrays.equals(this.allAce, other.getAllAce()))) &&
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
        if (getEntityUrl() != null) {
            _hashCode += getEntityUrl().hashCode();
        }
        if (getOwner() != null) {
            _hashCode += getOwner().hashCode();
        }
        if (getAllAce() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAllAce());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAllAce(), i);
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
        new org.apache.axis.description.TypeDesc(GssAcl.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAcl"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "EntityUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("owner");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Owner"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allAce");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "AllAce"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAce"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAce"));
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
