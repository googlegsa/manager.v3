/**
 * UpdateContentType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.webs;

public class UpdateContentType  implements java.io.Serializable {
    private java.lang.String contentTypeId;

    private com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeContentTypeProperties contentTypeProperties;

    private com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeNewFields newFields;

    private com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeUpdateFields updateFields;

    private com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeDeleteFields deleteFields;

    public UpdateContentType() {
    }

    public UpdateContentType(
           java.lang.String contentTypeId,
           com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeContentTypeProperties contentTypeProperties,
           com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeNewFields newFields,
           com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeUpdateFields updateFields,
           com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeDeleteFields deleteFields) {
           this.contentTypeId = contentTypeId;
           this.contentTypeProperties = contentTypeProperties;
           this.newFields = newFields;
           this.updateFields = updateFields;
           this.deleteFields = deleteFields;
    }


    /**
     * Gets the contentTypeId value for this UpdateContentType.
     * 
     * @return contentTypeId
     */
    public java.lang.String getContentTypeId() {
        return contentTypeId;
    }


    /**
     * Sets the contentTypeId value for this UpdateContentType.
     * 
     * @param contentTypeId
     */
    public void setContentTypeId(java.lang.String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }


    /**
     * Gets the contentTypeProperties value for this UpdateContentType.
     * 
     * @return contentTypeProperties
     */
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeContentTypeProperties getContentTypeProperties() {
        return contentTypeProperties;
    }


    /**
     * Sets the contentTypeProperties value for this UpdateContentType.
     * 
     * @param contentTypeProperties
     */
    public void setContentTypeProperties(com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeContentTypeProperties contentTypeProperties) {
        this.contentTypeProperties = contentTypeProperties;
    }


    /**
     * Gets the newFields value for this UpdateContentType.
     * 
     * @return newFields
     */
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeNewFields getNewFields() {
        return newFields;
    }


    /**
     * Sets the newFields value for this UpdateContentType.
     * 
     * @param newFields
     */
    public void setNewFields(com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeNewFields newFields) {
        this.newFields = newFields;
    }


    /**
     * Gets the updateFields value for this UpdateContentType.
     * 
     * @return updateFields
     */
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeUpdateFields getUpdateFields() {
        return updateFields;
    }


    /**
     * Sets the updateFields value for this UpdateContentType.
     * 
     * @param updateFields
     */
    public void setUpdateFields(com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeUpdateFields updateFields) {
        this.updateFields = updateFields;
    }


    /**
     * Gets the deleteFields value for this UpdateContentType.
     * 
     * @return deleteFields
     */
    public com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeDeleteFields getDeleteFields() {
        return deleteFields;
    }


    /**
     * Sets the deleteFields value for this UpdateContentType.
     * 
     * @param deleteFields
     */
    public void setDeleteFields(com.google.enterprise.connector.sharepoint.generated.webs.UpdateContentTypeDeleteFields deleteFields) {
        this.deleteFields = deleteFields;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateContentType)) return false;
        UpdateContentType other = (UpdateContentType) obj;
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
            ((this.contentTypeProperties==null && other.getContentTypeProperties()==null) || 
             (this.contentTypeProperties!=null &&
              this.contentTypeProperties.equals(other.getContentTypeProperties()))) &&
            ((this.newFields==null && other.getNewFields()==null) || 
             (this.newFields!=null &&
              this.newFields.equals(other.getNewFields()))) &&
            ((this.updateFields==null && other.getUpdateFields()==null) || 
             (this.updateFields!=null &&
              this.updateFields.equals(other.getUpdateFields()))) &&
            ((this.deleteFields==null && other.getDeleteFields()==null) || 
             (this.deleteFields!=null &&
              this.deleteFields.equals(other.getDeleteFields())));
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
        if (getContentTypeProperties() != null) {
            _hashCode += getContentTypeProperties().hashCode();
        }
        if (getNewFields() != null) {
            _hashCode += getNewFields().hashCode();
        }
        if (getUpdateFields() != null) {
            _hashCode += getUpdateFields().hashCode();
        }
        if (getDeleteFields() != null) {
            _hashCode += getDeleteFields().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateContentType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">UpdateContentType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentTypeId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentTypeProperties");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "contentTypeProperties"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>contentTypeProperties"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("newFields");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "newFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>newFields"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateFields");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "updateFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>updateFields"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deleteFields");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "deleteFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">>UpdateContentType>deleteFields"));
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
