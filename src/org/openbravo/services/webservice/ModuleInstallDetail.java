/**
 * ModuleInstallDetail.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

@SuppressWarnings({ "rawtypes", "serial" })
public class ModuleInstallDetail  implements java.io.Serializable {
    private java.lang.String[] dependencyErrors;

    private org.openbravo.services.webservice.Module[] modulesToInstall;

    private org.openbravo.services.webservice.Module[] modulesToUpdate;

    private boolean validConfiguration;

    public ModuleInstallDetail() {
    }

    public ModuleInstallDetail(
           java.lang.String[] dependencyErrors,
           org.openbravo.services.webservice.Module[] modulesToInstall,
           org.openbravo.services.webservice.Module[] modulesToUpdate,
           boolean validConfiguration) {
           this.dependencyErrors = dependencyErrors;
           this.modulesToInstall = modulesToInstall;
           this.modulesToUpdate = modulesToUpdate;
           this.validConfiguration = validConfiguration;
    }


    /**
     * Gets the dependencyErrors value for this ModuleInstallDetail.
     * 
     * @return dependencyErrors
     */
    public java.lang.String[] getDependencyErrors() {
        return dependencyErrors;
    }


    /**
     * Sets the dependencyErrors value for this ModuleInstallDetail.
     * 
     * @param dependencyErrors
     */
    public void setDependencyErrors(java.lang.String[] dependencyErrors) {
        this.dependencyErrors = dependencyErrors;
    }


    /**
     * Gets the modulesToInstall value for this ModuleInstallDetail.
     * 
     * @return modulesToInstall
     */
    public org.openbravo.services.webservice.Module[] getModulesToInstall() {
        return modulesToInstall;
    }


    /**
     * Sets the modulesToInstall value for this ModuleInstallDetail.
     * 
     * @param modulesToInstall
     */
    public void setModulesToInstall(org.openbravo.services.webservice.Module[] modulesToInstall) {
        this.modulesToInstall = modulesToInstall;
    }


    /**
     * Gets the modulesToUpdate value for this ModuleInstallDetail.
     * 
     * @return modulesToUpdate
     */
    public org.openbravo.services.webservice.Module[] getModulesToUpdate() {
        return modulesToUpdate;
    }


    /**
     * Sets the modulesToUpdate value for this ModuleInstallDetail.
     * 
     * @param modulesToUpdate
     */
    public void setModulesToUpdate(org.openbravo.services.webservice.Module[] modulesToUpdate) {
        this.modulesToUpdate = modulesToUpdate;
    }


    /**
     * Gets the validConfiguration value for this ModuleInstallDetail.
     * 
     * @return validConfiguration
     */
    public boolean isValidConfiguration() {
        return validConfiguration;
    }


    /**
     * Sets the validConfiguration value for this ModuleInstallDetail.
     * 
     * @param validConfiguration
     */
    public void setValidConfiguration(boolean validConfiguration) {
        this.validConfiguration = validConfiguration;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ModuleInstallDetail)) return false;
        ModuleInstallDetail other = (ModuleInstallDetail) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dependencyErrors==null && other.getDependencyErrors()==null) || 
             (this.dependencyErrors!=null &&
              java.util.Arrays.equals(this.dependencyErrors, other.getDependencyErrors()))) &&
            ((this.modulesToInstall==null && other.getModulesToInstall()==null) || 
             (this.modulesToInstall!=null &&
              java.util.Arrays.equals(this.modulesToInstall, other.getModulesToInstall()))) &&
            ((this.modulesToUpdate==null && other.getModulesToUpdate()==null) || 
             (this.modulesToUpdate!=null &&
              java.util.Arrays.equals(this.modulesToUpdate, other.getModulesToUpdate()))) &&
            this.validConfiguration == other.isValidConfiguration();
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
        if (getDependencyErrors() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDependencyErrors());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDependencyErrors(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getModulesToInstall() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getModulesToInstall());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getModulesToInstall(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getModulesToUpdate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getModulesToUpdate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getModulesToUpdate(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += (isValidConfiguration() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ModuleInstallDetail.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("/services/WebService3", "ModuleInstallDetail"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dependencyErrors");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dependencyErrors"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modulesToInstall");
        elemField.setXmlName(new javax.xml.namespace.QName("", "modulesToInstall"));
        elemField.setXmlType(new javax.xml.namespace.QName("/services/WebService3", "Module"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modulesToUpdate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "modulesToUpdate"));
        elemField.setXmlType(new javax.xml.namespace.QName("/services/WebService3", "Module"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validConfiguration");
        elemField.setXmlName(new javax.xml.namespace.QName("", "validConfiguration"));
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
