/**
 * ObjectType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class ObjectType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected ObjectType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _NA = "NA";
    public static final java.lang.String _SECURITY_POLICY = "SECURITY_POLICY";
    public static final java.lang.String _ADMINISTRATORS = "ADMINISTRATORS";
    public static final java.lang.String _GROUP = "GROUP";
    public static final java.lang.String _USER = "USER";
    public static final java.lang.String _WEB = "WEB";
    public static final java.lang.String _LIST = "LIST";
    public static final java.lang.String _ITEM = "ITEM";
    public static final ObjectType NA = new ObjectType(_NA);
    public static final ObjectType SECURITY_POLICY = new ObjectType(_SECURITY_POLICY);
    public static final ObjectType ADMINISTRATORS = new ObjectType(_ADMINISTRATORS);
    public static final ObjectType GROUP = new ObjectType(_GROUP);
    public static final ObjectType USER = new ObjectType(_USER);
    public static final ObjectType WEB = new ObjectType(_WEB);
    public static final ObjectType LIST = new ObjectType(_LIST);
    public static final ObjectType ITEM = new ObjectType(_ITEM);
    public java.lang.String getValue() { return _value_;}
    public static ObjectType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        ObjectType enumeration = (ObjectType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static ObjectType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType,
           java.lang.Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ObjectType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "ObjectType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
