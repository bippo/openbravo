/**
 * ModuleDependency.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

@SuppressWarnings({ "rawtypes", "serial" })
public class ModuleDependency  implements java.io.Serializable {
    private java.lang.String moduleID;

    private java.lang.String moduleName;

    private java.lang.String moduleVersionDependencyID;

    private java.lang.String versionEnd;

    private java.lang.String versionStart;

    public ModuleDependency() {
    }

    public ModuleDependency(
           java.lang.String moduleID,
           java.lang.String moduleName,
           java.lang.String moduleVersionDependencyID,
           java.lang.String versionEnd,
           java.lang.String versionStart) {
           this.moduleID = moduleID;
           this.moduleName = moduleName;
           this.moduleVersionDependencyID = moduleVersionDependencyID;
           this.versionEnd = versionEnd;
           this.versionStart = versionStart;
    }


    /**
     * Gets the moduleID value for this ModuleDependency.
     * 
     * @return moduleID
     */
    public java.lang.String getModuleID() {
        return moduleID;
    }


    /**
     * Sets the moduleID value for this ModuleDependency.
     * 
     * @param moduleID
     */
    public void setModuleID(java.lang.String moduleID) {
        this.moduleID = moduleID;
    }


    /**
     * Gets the moduleName value for this ModuleDependency.
     * 
     * @return moduleName
     */
    public java.lang.String getModuleName() {
        return moduleName;
    }


    /**
     * Sets the moduleName value for this ModuleDependency.
     * 
     * @param moduleName
     */
    public void setModuleName(java.lang.String moduleName) {
        this.moduleName = moduleName;
    }


    /**
     * Gets the moduleVersionDependencyID value for this ModuleDependency.
     * 
     * @return moduleVersionDependencyID
     */
    public java.lang.String getModuleVersionDependencyID() {
        return moduleVersionDependencyID;
    }


    /**
     * Sets the moduleVersionDependencyID value for this ModuleDependency.
     * 
     * @param moduleVersionDependencyID
     */
    public void setModuleVersionDependencyID(java.lang.String moduleVersionDependencyID) {
        this.moduleVersionDependencyID = moduleVersionDependencyID;
    }


    /**
     * Gets the versionEnd value for this ModuleDependency.
     * 
     * @return versionEnd
     */
    public java.lang.String getVersionEnd() {
        return versionEnd;
    }


    /**
     * Sets the versionEnd value for this ModuleDependency.
     * 
     * @param versionEnd
     */
    public void setVersionEnd(java.lang.String versionEnd) {
        this.versionEnd = versionEnd;
    }


    /**
     * Gets the versionStart value for this ModuleDependency.
     * 
     * @return versionStart
     */
    public java.lang.String getVersionStart() {
        return versionStart;
    }


    /**
     * Sets the versionStart value for this ModuleDependency.
     * 
     * @param versionStart
     */
    public void setVersionStart(java.lang.String versionStart) {
        this.versionStart = versionStart;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ModuleDependency)) return false;
        ModuleDependency other = (ModuleDependency) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.moduleID==null && other.getModuleID()==null) || 
             (this.moduleID!=null &&
              this.moduleID.equals(other.getModuleID()))) &&
            ((this.moduleName==null && other.getModuleName()==null) || 
             (this.moduleName!=null &&
              this.moduleName.equals(other.getModuleName()))) &&
            ((this.moduleVersionDependencyID==null && other.getModuleVersionDependencyID()==null) || 
             (this.moduleVersionDependencyID!=null &&
              this.moduleVersionDependencyID.equals(other.getModuleVersionDependencyID()))) &&
            ((this.versionEnd==null && other.getVersionEnd()==null) || 
             (this.versionEnd!=null &&
              this.versionEnd.equals(other.getVersionEnd()))) &&
            ((this.versionStart==null && other.getVersionStart()==null) || 
             (this.versionStart!=null &&
              this.versionStart.equals(other.getVersionStart())));
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
        if (getModuleID() != null) {
            _hashCode += getModuleID().hashCode();
        }
        if (getModuleName() != null) {
            _hashCode += getModuleName().hashCode();
        }
        if (getModuleVersionDependencyID() != null) {
            _hashCode += getModuleVersionDependencyID().hashCode();
        }
        if (getVersionEnd() != null) {
            _hashCode += getVersionEnd().hashCode();
        }
        if (getVersionStart() != null) {
            _hashCode += getVersionStart().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ModuleDependency.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("/services/WebService3", "ModuleDependency"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moduleID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moduleID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moduleName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moduleName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moduleVersionDependencyID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moduleVersionDependencyID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versionEnd");
        elemField.setXmlName(new javax.xml.namespace.QName("", "versionEnd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versionStart");
        elemField.setXmlName(new javax.xml.namespace.QName("", "versionStart"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
