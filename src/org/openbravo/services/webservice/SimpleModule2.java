/**
 * SimpleModule2.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

@SuppressWarnings({ "rawtypes", "serial" })
public class SimpleModule2  extends org.openbravo.services.webservice.SimpleModule1  implements java.io.Serializable {
    private boolean isCommercial;

    public SimpleModule2() {
    }

    public SimpleModule2(
           java.lang.String author,
           java.lang.String description,
           java.lang.String help,
           java.lang.String licenseAgreement,
           java.lang.String licenseType,
           java.lang.String moduleID,
           java.lang.String moduleVersionID,
           java.lang.String name,
           java.lang.String type,
           java.lang.String updateDescription,
           java.lang.String url,
           java.lang.String versionNo,
           boolean isCommercial) {
        super(
            author,
            description,
            help,
            licenseAgreement,
            licenseType,
            moduleID,
            moduleVersionID,
            name,
            type,
            updateDescription,
            url,
            versionNo);
        this.isCommercial = isCommercial;
    }


    /**
     * Gets the isCommercial value for this SimpleModule2.
     * 
     * @return isCommercial
     */
    public boolean isIsCommercial() {
        return isCommercial;
    }


    /**
     * Sets the isCommercial value for this SimpleModule2.
     * 
     * @param isCommercial
     */
    public void setIsCommercial(boolean isCommercial) {
        this.isCommercial = isCommercial;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SimpleModule2)) return false;
        SimpleModule2 other = (SimpleModule2) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.isCommercial == other.isIsCommercial();
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
        _hashCode += (isIsCommercial() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SimpleModule2.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://centralrepository.openbravo.com/openbravo/services/WebService3", "SimpleModule2"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isCommercial");
        elemField.setXmlName(new javax.xml.namespace.QName("", "isCommercial"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
