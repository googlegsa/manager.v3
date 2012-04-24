/**
 * RemoveContentTypeXmlDocument.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.webs;

public class RemoveContentTypeXmlDocument  implements java.io.Serializable {
    private java.lang.String contentTypeId;

    private java.lang.String documentUri;

    public RemoveContentTypeXmlDocument() {
    }

    public RemoveContentTypeXmlDocument(
           java.lang.String contentTypeId,
           java.lang.String documentUri) {
           this.contentTypeId = contentTypeId;
           this.documentUri = documentUri;
    }


    /**
     * Gets the contentTypeId value for this RemoveContentTypeXmlDocument.
     * 
     * @return contentTypeId
     */
    public java.lang.String getContentTypeId() {
        return contentTypeId;
    }


    /**
     * Sets the contentTypeId value for this RemoveContentTypeXmlDocument.
     * 
     * @param contentTypeId
     */
    public void setContentTypeId(java.lang.String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }


    /**
     * Gets the documentUri value for this RemoveContentTypeXmlDocument.
     * 
     * @return documentUri
     */
    public java.lang.String getDocumentUri() {
        return documentUri;
    }


    /**
     * Sets the documentUri value for this RemoveContentTypeXmlDocument.
     * 
     * @param documentUri
     */
    public void setDocumentUri(java.lang.String documentUri) {
        this.documentUri = documentUri;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RemoveContentTypeXmlDocument)) return false;
        RemoveContentTypeXmlDocument other = (RemoveContentTypeXmlDocument) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.contentTypeId==null && other.getContentTypeId()==null) || 
             (this.contentTypeId!=null &&
              this.contentTypeId.equals(other.getContentTypeId()))) &&
            ((this.documentUri==null && other.getDocumentUri()==null) || 
             (this.documentUri!=null &&
              this.documentUri.equals(other.getDocumentUri())));
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
        if (getContentTypeId() != null) {
            _hashCode += getContentTypeId().hashCode();
        }
        if (getDocumentUri() != null) {
            _hashCode += getDocumentUri().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RemoveContentTypeXmlDocument.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">RemoveContentTypeXmlDocument"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentTypeId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentUri");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "documentUri"));
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
