/**
 * GssResolveSPGroupResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class GssResolveSPGroupResult  extends com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclBaseResult  implements java.io.Serializable {
    private com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] prinicpals;

    public GssResolveSPGroupResult() {
    }

    public GssResolveSPGroupResult(
           java.lang.String siteCollectionUrl,
           java.lang.String siteCollectionGuid,
           java.lang.String logMessage,
           com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] prinicpals) {
        super(
            siteCollectionUrl,
            siteCollectionGuid,
            logMessage);
        this.prinicpals = prinicpals;
    }


    /**
     * Gets the prinicpals value for this GssResolveSPGroupResult.
     *
     * @return prinicpals
     */
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] getPrinicpals() {
        return prinicpals;
    }


    /**
     * Sets the prinicpals value for this GssResolveSPGroupResult.
     *
     * @param prinicpals
     */
    public void setPrinicpals(com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal[] prinicpals) {
        this.prinicpals = prinicpals;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GssResolveSPGroupResult)) return false;
        GssResolveSPGroupResult other = (GssResolveSPGroupResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) &&
            ((this.prinicpals==null && other.getPrinicpals()==null) ||
             (this.prinicpals!=null &&
              java.util.Arrays.equals(this.prinicpals, other.getPrinicpals())));
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
        if (getPrinicpals() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPrinicpals());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPrinicpals(), i);
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
        new org.apache.axis.description.TypeDesc(GssResolveSPGroupResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssResolveSPGroupResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prinicpals");
        elemField.setXmlName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "Prinicpals"));
        elemField.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "GssPrincipal"));
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
