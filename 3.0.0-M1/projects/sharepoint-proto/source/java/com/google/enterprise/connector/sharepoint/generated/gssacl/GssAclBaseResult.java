/**
 * GssAclBaseResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public abstract class GssAclBaseResult  implements java.io.Serializable {
    private java.lang.String siteCollectionUrl;

    private java.lang.String siteCollectionGuid;

    private java.lang.String logMessage;

    public GssAclBaseResult() {
    }

    public GssAclBaseResult(
           java.lang.String siteCollectionUrl,
           java.lang.String siteCollectionGuid,
           java.lang.String logMessage) {
           this.siteCollectionUrl = siteCollectionUrl;
           this.siteCollectionGuid = siteCollectionGuid;
           this.logMessage = logMessage;
    }


    /**
     * Gets the siteCollectionUrl value for this GssAclBaseResult.
     *
     * @return siteCollectionUrl
     */
    public java.lang.String getSiteCollectionUrl() {
        return siteCollectionUrl;
    }


    /**
     * Sets the siteCollectionUrl value for this GssAclBaseResult.
     *
     * @param siteCollectionUrl
     */
    public void setSiteCollectionUrl(java.lang.String siteCollectionUrl) {
        this.siteCollectionUrl = siteCollectionUrl;
    }


    /**
     * Gets the siteCollectionGuid value for this GssAclBaseResult.
     *
     * @return siteCollectionGuid
     */
    public java.lang.String getSiteCollectionGuid() {
        return siteCollectionGuid;
    }


    /**
     * Sets the siteCollectionGuid value for this GssAclBaseResult.
     *
     * @param siteCollectionGuid
     */
    public void setSiteCollectionGuid(java.lang.String siteCollectionGuid) {
        this.siteCollectionGuid = siteCollectionGuid;
    }


    /**
     * Gets the logMessage value for this GssAclBaseResult.
     *
     * @return logMessage
     */
    public java.lang.String getLogMessage() {
        return logMessage;
    }


    /**
     * Sets the logMessage value for this GssAclBaseResult.
     *
     * @param logMessage
     */
    public void setLogMessage(java.lang.String logMessage) {
        this.logMessage = logMessage;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssAclBaseResult)) return false;
        GssAclBaseResult other = (GssAclBaseResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&
            ((this.siteCollectionUrl==null && other.getSiteCollectionUrl()==null) ||
             (this.siteCollectionUrl!=null &&
              this.siteCollectionUrl.equals(other.getSiteCollectionUrl()))) &&
            ((this.siteCollectionGuid==null && other.getSiteCollectionGuid()==null) ||
             (this.siteCollectionGuid!=null &&
              this.siteCollectionGuid.equals(other.getSiteCollectionGuid()))) &&
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
        if (getSiteCollectionUrl() != null) {
            _hashCode += getSiteCollectionUrl().hashCode();
        }
        if (getSiteCollectionGuid() != null) {
            _hashCode += getSiteCollectionGuid().hashCode();
        }
        if (getLogMessage() != null) {
            _hashCode += getLogMessage().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssAclBaseResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclBaseResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("siteCollectionUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SiteCollectionUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("siteCollectionGuid");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SiteCollectionGuid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
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
