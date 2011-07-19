/**
 * GssGetAclChangesSinceTokenResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssGetAclChangesSinceTokenResult  extends com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclBaseResult  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection allChanges;

    public GssGetAclChangesSinceTokenResult() {
    }

    public GssGetAclChangesSinceTokenResult(
           java.lang.String siteCollectionUrl,
           java.lang.String siteCollectionGuid,
           java.lang.String logMessage,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection allChanges) {
        super(
            siteCollectionUrl,
            siteCollectionGuid,
            logMessage);
        this.allChanges = allChanges;
    }


    /**
     * Gets the allChanges value for this GssGetAclChangesSinceTokenResult.
     *
     * @return allChanges
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection getAllChanges() {
        return allChanges;
    }


    /**
     * Sets the allChanges value for this GssGetAclChangesSinceTokenResult.
     *
     * @param allChanges
     */
    public void setAllChanges(com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection allChanges) {
        this.allChanges = allChanges;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssGetAclChangesSinceTokenResult)) return false;
        GssGetAclChangesSinceTokenResult other = (GssGetAclChangesSinceTokenResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) &&
            ((this.allChanges==null && other.getAllChanges()==null) ||
             (this.allChanges!=null &&
              this.allChanges.equals(other.getAllChanges())));
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
        if (getAllChanges() != null) {
            _hashCode += getAllChanges().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GssGetAclChangesSinceTokenResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssGetAclChangesSinceTokenResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allChanges");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "AllChanges"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssAclChangeCollection"));
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
