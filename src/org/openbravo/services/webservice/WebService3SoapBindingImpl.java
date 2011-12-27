/**
 * WebService3SoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

@SuppressWarnings("rawtypes")
public class WebService3SoapBindingImpl implements org.openbravo.services.webservice.WebService3Impl{
    public byte[] getModule(java.lang.String moduleVersionID) throws java.rmi.RemoteException {
        return null;
    }

    public boolean isCommercial(java.lang.String moduleVersionID) throws java.rmi.RemoteException {
        return false;
    }

    public org.openbravo.services.webservice.Module moduleDetail(java.lang.String moduleVersionID) throws java.rmi.RemoteException {
        return null;
    }

    public org.openbravo.services.webservice.SimpleModule[] moduleSearch(java.lang.String word, java.lang.String[] exclude, java.util.HashMap additionalInfo) throws java.rmi.RemoteException {
        return null;
    }

    public org.openbravo.services.webservice.Module moduleRegister(org.openbravo.services.webservice.Module module, java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException {
        return null;
    }

    public org.openbravo.services.webservice.SimpleModule[] moduleScanForUpdates(java.util.HashMap moduleIdInstalledModules, java.util.HashMap additionalInfo) throws java.rmi.RemoteException {
        return null;
    }

    public org.openbravo.services.webservice.ModuleInstallDetail checkConsistency(java.util.HashMap versionIdInstalled, java.lang.String[] versionIdToInstall, java.lang.String[] versionIdToUpdate, java.util.HashMap additionalInfo) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String getURLforDownload(java.lang.String moduleVersionID) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String[][] getMaturityLevels() throws java.rmi.RemoteException {
        return null;
    }

}
