/**
 * GetCustomizedPageStatusResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.webs;

public class GetCustomizedPageStatusResponse  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.webs.CustomizedPageStatus getCustomizedPageStatusResult;

    public GetCustomizedPageStatusResponse() {
    }

    public GetCustomizedPageStatusResponse(
           com.google.enterprise.connector.sharepoint.generated.webs.CustomizedPageStatus getCustomizedPageStatusResult) {
           this.getCustomizedPageStatusResult = getCustomizedPageStatusResult;
    }


    /**
     * Gets the getCustomizedPageStatusResult value for this GetCustomizedPageStatusResponse.
     * 
     * @return getCustomizedPageStatusResult
     */
    public com.google.enterprise.connector.sharepoint.generated.webs.CustomizedPageStatus getGetCustomizedPageStatusResult() {
        return getCustomizedPageStatusResult;
    }


    /**
     * Sets the getCustomizedPageStatusResult value for this GetCustomizedPageStatusResponse.
     * 
     * @param getCustomizedPageStatusResult
     */
    public void setGetCustomizedPageStatusResult(com.google.enterprise.connector.sharepoint.generated.webs.CustomizedPageStatus getCustomizedPageStatusResult) {
        this.getCustomizedPageStatusResult = getCustomizedPageStatusResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetCustomizedPageStatusResponse)) return false;
        GetCustomizedPageStatusResponse other = (GetCustomizedPageStatusResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getCustomizedPageStatusResult==null && other.getGetCustomizedPageStatusResult()==null) || 
             (this.getCustomizedPageStatusResult!=null &&
              this.getCustomizedPageStatusResult.equals(other.getGetCustomizedPageStatusResult())));
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
        if (getGetCustomizedPageStatusResult() != null) {
            _hashCode += getGetCustomizedPageStatusResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetCustomizedPageStatusResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", ">GetCustomizedPageStatusResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getCustomizedPageStatusResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "GetCustomizedPageStatusResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "CustomizedPageStatus"));
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
