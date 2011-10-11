/**
 * GssGetAclForUrlsResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssGetAclForUrlsResult  extends com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclBaseResult  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl[] allAcls;

    public GssGetAclForUrlsResult() {
    }

    public GssGetAclForUrlsResult(
           java.lang.String siteCollectionUrl,
           java.lang.String siteCollectionGuid,
           java.lang.String logMessage,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl[] allAcls) {
        super(
            siteCollectionUrl,
            siteCollectionGuid,
            logMessage);
        this.allAcls = allAcls;
    }


    /**
     * Gets the allAcls value for this GssGetAclForUrlsResult.
     *
     * @return allAcls
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl[] getAllAcls() {
        return allAcls;
    }


    /**
     * Sets the allAcls value for this GssGetAclForUrlsResult.
     *
     * @param allAcls
     */
    public void setAllAcls(com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl[] allAcls) {
        this.allAcls = allAcls;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssGetAclForUrlsResult)) return false;
        GssGetAclForUrlsResult other = (GssGetAclForUrlsResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) &&
            ((this.allAcls==null && other.getAllAcls()==null) ||
             (this.allAcls!=null &&
              java.util.Arrays.equals(this.allAcls, other.getAllAcls())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getAllAcls() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAllAcls());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAllAcls(), i);
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
        new org.apache.axis.description.TypeDesc(GssGetAclForUrlsResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetAclForUrlsResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allAcls");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "AllAcls"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAcl"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAcl"));
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
