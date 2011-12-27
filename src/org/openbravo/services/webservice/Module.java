/**
 * Module.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

@SuppressWarnings({ "rawtypes", "serial" })
public class Module  extends org.openbravo.services.webservice.Module2  implements java.io.Serializable {
    private java.util.HashMap additionalInfo;

    public Module() {
    }

    public Module(
           java.lang.String author,
           java.lang.String dbPrefix,
           org.openbravo.services.webservice.ModuleDependency[] dependencies,
           java.lang.String description,
           java.lang.String help,
           org.openbravo.services.webservice.ModuleDependency[] includes,
           java.lang.String licenseAgreement,
           java.lang.String licenseType,
           java.lang.String moduleID,
           java.lang.String moduleVersionID,
           java.lang.String name,
           java.lang.String packageName,
           java.lang.String type,
           java.lang.String updateDescription,
           java.lang.String url,
           java.lang.String versionNo,
           boolean isCommercial,
           java.util.HashMap additionalInfo) {
        super(
            author,
            dbPrefix,
            dependencies,
            description,
            help,
            includes,
            licenseAgreement,
            licenseType,
            moduleID,
            moduleVersionID,
            name,
            packageName,
            type,
            updateDescription,
            url,
            versionNo,
            isCommercial);
        this.additionalInfo = additionalInfo;
    }


    /**
     * Gets the additionalInfo value for this Module.
     * 
     * @return additionalInfo
     */
    public java.util.HashMap getAdditionalInfo() {
        return additionalInfo;
    }


    /**
     * Sets the additionalInfo value for this Module.
     * 
     * @param additionalInfo
     */
    public void setAdditionalInfo(java.util.HashMap additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Module)) return false;
        Module other = (Module) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.additionalInfo==null && other.getAdditionalInfo()==null) || 
             (this.additionalInfo!=null &&
              this.additionalInfo.equals(other.getAdditionalInfo())));
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
        if (getAdditionalInfo() != null) {
            _hashCode += getAdditionalInfo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Module.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("/services/WebService3", "Module"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("additionalInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "additionalInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        elemField.setNillable(true);
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
