/**
 * SPChangeType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public class SPChangeType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected SPChangeType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _Add = "Add";
    public static final java.lang.String _Update = "Update";
    public static final java.lang.String _Delete = "Delete";
    public static final java.lang.String _Rename = "Rename";
    public static final java.lang.String _MoveAway = "MoveAway";
    public static final java.lang.String _MoveInto = "MoveInto";
    public static final java.lang.String _Restore = "Restore";
    public static final java.lang.String _RoleAdd = "RoleAdd";
    public static final java.lang.String _RoleDelete = "RoleDelete";
    public static final java.lang.String _RoleUpdate = "RoleUpdate";
    public static final java.lang.String _AssignmentAdd = "AssignmentAdd";
    public static final java.lang.String _AssignmentDelete = "AssignmentDelete";
    public static final java.lang.String _MemberAdd = "MemberAdd";
    public static final java.lang.String _MemberDelete = "MemberDelete";
    public static final java.lang.String _SystemUpdate = "SystemUpdate";
    public static final java.lang.String _Navigation = "Navigation";
    public static final SPChangeType Add = new SPChangeType(_Add);
    public static final SPChangeType Update = new SPChangeType(_Update);
    public static final SPChangeType Delete = new SPChangeType(_Delete);
    public static final SPChangeType Rename = new SPChangeType(_Rename);
    public static final SPChangeType MoveAway = new SPChangeType(_MoveAway);
    public static final SPChangeType MoveInto = new SPChangeType(_MoveInto);
    public static final SPChangeType Restore = new SPChangeType(_Restore);
    public static final SPChangeType RoleAdd = new SPChangeType(_RoleAdd);
    public static final SPChangeType RoleDelete = new SPChangeType(_RoleDelete);
    public static final SPChangeType RoleUpdate = new SPChangeType(_RoleUpdate);
    public static final SPChangeType AssignmentAdd = new SPChangeType(_AssignmentAdd);
    public static final SPChangeType AssignmentDelete = new SPChangeType(_AssignmentDelete);
    public static final SPChangeType MemberAdd = new SPChangeType(_MemberAdd);
    public static final SPChangeType MemberDelete = new SPChangeType(_MemberDelete);
    public static final SPChangeType SystemUpdate = new SPChangeType(_SystemUpdate);
    public static final SPChangeType Navigation = new SPChangeType(_Navigation);
    public java.lang.String getValue() { return _value_;}
    public static SPChangeType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        SPChangeType enumeration = (SPChangeType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static SPChangeType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(SPChangeType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("gssAcl.generated.sharepoint.connector.enterprise.google.com", "SPChangeType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
