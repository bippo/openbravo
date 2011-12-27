/**
 * SimpleModule1.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

@SuppressWarnings({ "rawtypes", "serial" })
public class SimpleModule1  implements java.io.Serializable {
    private java.lang.String author;

    private java.lang.String description;

    private java.lang.String help;

    private java.lang.String licenseAgreement;

    private java.lang.String licenseType;

    private java.lang.String moduleID;

    private java.lang.String moduleVersionID;

    private java.lang.String name;

    private java.lang.String type;

    private java.lang.String updateDescription;

    private java.lang.String url;

    private java.lang.String versionNo;

    public SimpleModule1() {
    }

    public SimpleModule1(
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
           java.lang.String versionNo) {
           this.author = author;
           this.description = description;
           this.help = help;
           this.licenseAgreement = licenseAgreement;
           this.licenseType = licenseType;
           this.moduleID = moduleID;
           this.moduleVersionID = moduleVersionID;
           this.name = name;
           this.type = type;
           this.updateDescription = updateDescription;
           this.url = url;
           this.versionNo = versionNo;
    }


    /**
     * Gets the author value for this SimpleModule1.
     * 
     * @return author
     */
    public java.lang.String getAuthor() {
        return author;
    }


    /**
     * Sets the author value for this SimpleModule1.
     * 
     * @param author
     */
    public void setAuthor(java.lang.String author) {
        this.author = author;
    }


    /**
     * Gets the description value for this SimpleModule1.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this SimpleModule1.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the help value for this SimpleModule1.
     * 
     * @return help
     */
    public java.lang.String getHelp() {
        return help;
    }


    /**
     * Sets the help value for this SimpleModule1.
     * 
     * @param help
     */
    public void setHelp(java.lang.String help) {
        this.help = help;
    }


    /**
     * Gets the licenseAgreement value for this SimpleModule1.
     * 
     * @return licenseAgreement
     */
    public java.lang.String getLicenseAgreement() {
        return licenseAgreement;
    }


    /**
     * Sets the licenseAgreement value for this SimpleModule1.
     * 
     * @param licenseAgreement
     */
    public void setLicenseAgreement(java.lang.String licenseAgreement) {
        this.licenseAgreement = licenseAgreement;
    }


    /**
     * Gets the licenseType value for this SimpleModule1.
     * 
     * @return licenseType
     */
    public java.lang.String getLicenseType() {
        return licenseType;
    }


    /**
     * Sets the licenseType value for this SimpleModule1.
     * 
     * @param licenseType
     */
    public void setLicenseType(java.lang.String licenseType) {
        this.licenseType = licenseType;
    }


    /**
     * Gets the moduleID value for this SimpleModule1.
     * 
     * @return moduleID
     */
    public java.lang.String getModuleID() {
        return moduleID;
    }


    /**
     * Sets the moduleID value for this SimpleModule1.
     * 
     * @param moduleID
     */
    public void setModuleID(java.lang.String moduleID) {
        this.moduleID = moduleID;
    }


    /**
     * Gets the moduleVersionID value for this SimpleModule1.
     * 
     * @return moduleVersionID
     */
    public java.lang.String getModuleVersionID() {
        return moduleVersionID;
    }


    /**
     * Sets the moduleVersionID value for this SimpleModule1.
     * 
     * @param moduleVersionID
     */
    public void setModuleVersionID(java.lang.String moduleVersionID) {
        this.moduleVersionID = moduleVersionID;
    }


    /**
     * Gets the name value for this SimpleModule1.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SimpleModule1.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the type value for this SimpleModule1.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this SimpleModule1.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the updateDescription value for this SimpleModule1.
     * 
     * @return updateDescription
     */
    public java.lang.String getUpdateDescription() {
        return updateDescription;
    }


    /**
     * Sets the updateDescription value for this SimpleModule1.
     * 
     * @param updateDescription
     */
    public void setUpdateDescription(java.lang.String updateDescription) {
        this.updateDescription = updateDescription;
    }


    /**
     * Gets the url value for this SimpleModule1.
     * 
     * @return url
     */
    public java.lang.String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this SimpleModule1.
     * 
     * @param url
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }


    /**
     * Gets the versionNo value for this SimpleModule1.
     * 
     * @return versionNo
     */
    public java.lang.String getVersionNo() {
        return versionNo;
    }


    /**
     * Sets the versionNo value for this SimpleModule1.
     * 
     * @param versionNo
     */
    public void setVersionNo(java.lang.String versionNo) {
        this.versionNo = versionNo;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SimpleModule1)) return false;
        SimpleModule1 other = (SimpleModule1) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.author==null && other.getAuthor()==null) || 
             (this.author!=null &&
              this.author.equals(other.getAuthor()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.help==null && other.getHelp()==null) || 
             (this.help!=null &&
              this.help.equals(other.getHelp()))) &&
            ((this.licenseAgreement==null && other.getLicenseAgreement()==null) || 
             (this.licenseAgreement!=null &&
              this.licenseAgreement.equals(other.getLicenseAgreement()))) &&
            ((this.licenseType==null && other.getLicenseType()==null) || 
             (this.licenseType!=null &&
              this.licenseType.equals(other.getLicenseType()))) &&
            ((this.moduleID==null && other.getModuleID()==null) || 
             (this.moduleID!=null &&
              this.moduleID.equals(other.getModuleID()))) &&
            ((this.moduleVersionID==null && other.getModuleVersionID()==null) || 
             (this.moduleVersionID!=null &&
              this.moduleVersionID.equals(other.getModuleVersionID()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.updateDescription==null && other.getUpdateDescription()==null) || 
             (this.updateDescription!=null &&
              this.updateDescription.equals(other.getUpdateDescription()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            ((this.versionNo==null && other.getVersionNo()==null) || 
             (this.versionNo!=null &&
              this.versionNo.equals(other.getVersionNo())));
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
        if (getAuthor() != null) {
            _hashCode += getAuthor().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getHelp() != null) {
            _hashCode += getHelp().hashCode();
        }
        if (getLicenseAgreement() != null) {
            _hashCode += getLicenseAgreement().hashCode();
        }
        if (getLicenseType() != null) {
            _hashCode += getLicenseType().hashCode();
        }
        if (getModuleID() != null) {
            _hashCode += getModuleID().hashCode();
        }
        if (getModuleVersionID() != null) {
            _hashCode += getModuleVersionID().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getUpdateDescription() != null) {
            _hashCode += getUpdateDescription().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        if (getVersionNo() != null) {
            _hashCode += getVersionNo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SimpleModule1.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://centralrepository.openbravo.com/openbravo/services/WebService3", "SimpleModule1"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("author");
        elemField.setXmlName(new javax.xml.namespace.QName("", "author"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("help");
        elemField.setXmlName(new javax.xml.namespace.QName("", "help"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("licenseAgreement");
        elemField.setXmlName(new javax.xml.namespace.QName("", "licenseAgreement"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("licenseType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "licenseType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moduleID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moduleID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moduleVersionID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moduleVersionID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("", "updateDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versionNo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "versionNo"));
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
