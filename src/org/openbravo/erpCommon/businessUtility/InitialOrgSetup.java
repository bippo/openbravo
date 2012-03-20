/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.businessUtility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.modules.ModuleUtiltiy;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.module.ADOrgModule;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationType;
import org.openbravo.model.common.geography.Location;
import org.openbravo.service.db.ImportResult;
import org.openbravo.utils.FormatUtilities;

public class InitialOrgSetup {
  private static final Logger log4j = Logger.getLogger(InitialOrgSetup.class);
  private static final String NEW_LINE = "<br />\n";
  private static final String STRSEPARATOR = "*****************************************************";
  private static final String ERRORTYPE = "Error";
  private static final String OKTYPE = "Success";

  private StringBuffer strHeaderLog;
  private StringBuffer strLog;
  private Language language;
  private Organization org;
  private Client client;
  private User user;
  private Role role;

  /**
   * 
   * @param clientProvided
   *          the new organization will belong to the provided client
   */
  public InitialOrgSetup(Client clientProvided) {
    strHeaderLog = new StringBuffer();
    strLog = new StringBuffer();
    language = OBContext.getOBContext().getLanguage();
    client = clientProvided;
    org = null;
  }

  /**
   * 
   * @param strOrgName
   *          Name of the new organization
   * @param strOrgUser
   *          Name of the user that will be created for the organization
   * @param strOrgType
   *          Organization Type code, according to: 0-Organization, 1-Legal with accounting,
   *          2-Generic, 3-Legal without accounting.
   * @param strParentOrg
   *          New organization will belong to the provided one, in the organization tree
   * @param strcLocationId
   *          Location (if any)
   * @param strPassword
   *          Password for the user to be created
   * @param strModules
   *          Reference data (datasets) to be applied to the new organization
   * @param boCreateAccounting
   *          If true, a new accounting schema will be created (but not the fiscal calendar and
   *          year)
   * @param fileCoAFilePath
   *          Path to the csv file with the chart of accounts to be used to create the accounting
   *          schema (it can also be provided through an accounting type module)
   * @param strCurrency
   *          Currency for the new accounting schema
   * @param bBPartner
   *          If true, the Business Partner accounting dimension will be added to the new accounting
   *          schema
   * @param bProduct
   *          If true, the Product accounting dimension will be added to the new accounting schema
   * @param bProject
   *          If true, the Project accounting dimension will be added to the new accounting schema
   * @param bCampaign
   *          If true, the Campaign accounting dimension will be added to the new accounting schema
   * @param bSalesRegion
   *          If true, the Sales Region accounting dimension will be added to the new accounting
   *          schema
   * @param strSourcePath
   *          Path of the instance. Needed to build the path to the reference data (dataset) files
   */
  public OBError createOrganization(String strOrgName, String strOrgUser, String strOrgType,
      String strParentOrg, String strcLocationId, String strPassword, String strModules,
      boolean boCreateAccounting, FileItem fileCoAFilePath, String strCurrency, boolean bBPartner,
      boolean bProduct, boolean bProject, boolean bCampaign, boolean bSalesRegion,
      String strSourcePath) {
    OBError obResult = new OBError();
    obResult.setType(ERRORTYPE);
    strHeaderLog.append("@ReportSummary@").append(NEW_LINE).append(NEW_LINE);

    log4j.debug("createOrganization() - Checking if user and org names duplicated.");
    obResult = checkDuplicated(strOrgUser, strOrgName);
    if (!obResult.getType().equals(OKTYPE))
      return obResult;
    logEvent("@StartingOrg@" + NEW_LINE);

    log4j.debug("createOrganization() - Creating organization.");
    obResult = insertOrganization((strOrgName == null || strOrgName.equals("")) ? "newOrg"
        : strOrgName, strOrgType, strParentOrg, strcLocationId);
    if (!obResult.getType().equals(OKTYPE))
      return obResult;
    obResult.setType(ERRORTYPE);
    logEvent(InitialSetupUtility.getTranslatedColumnName(language, "AD_Org_ID") + "="
        + org.getName());

    String strOrgId = org.getId();

    // TODO: REMOVE THESE getWritableOrganizations CALLS AS THEY SHOULD NOT BE NEEDED ONCE ARE FIXED
    OBContext.getOBContext().getWritableOrganizations();
    OBContext.getOBContext().addWritableOrganization(strOrgId);
    OBContext.getOBContext().getWritableOrganizations();
    try {
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
      org = OBDal.getInstance().get(Organization.class, strOrgId);
      client = org.getClient();
      if (strcLocationId != null && !strcLocationId.equals(""))
        try {
          InitialSetupUtility.updateOrgLocation(org,
              OBDal.getInstance().get(Location.class, strcLocationId));
        } catch (final Exception err) {
          return logErrorAndRollback(
              "@CreateOrgFailed@",
              "createOrganization() - ERROR - Organization creation process failed. Couldn't set organization location.",
              err);
        }

    } catch (Exception e) {
      logErrorAndRollback(
          "@ExceptionInCommit@",
          "createClient() - Exception occured while performing commit in database. Your data may have NOT been saved in database.",
          e);
    }

    log4j.debug("createOrganization() - Creating users.");
    obResult = insertUser((strOrgUser == null || strOrgUser.equals("")) ? org.getName() + "Org"
        : strOrgUser, strPassword);
    if (!obResult.getType().equals(OKTYPE))
      return obResult;
    logEvent("@AD_User_ID@ = " + strOrgUser + " / " + strOrgUser + NEW_LINE);
    appendHeader("@CreateOrgSuccess@");
    obResult.setType(ERRORTYPE);

    log4j.debug("createOrganization() - Setting organization image");
    obResult = addImages();
    if (!obResult.getType().equals(OKTYPE))
      logEvent(obResult.getMessage());
    obResult.setType(ERRORTYPE);
    logEvent(STRSEPARATOR);

    boolean bAccountingCreated = false;
    if (boCreateAccounting) {
      if (fileCoAFilePath != null && fileCoAFilePath.getSize() > 0) {
        obResult = createAccounting(fileCoAFilePath, strCurrency, bBPartner, bProduct, bProject,
            bCampaign, bSalesRegion, null);
        if (!obResult.getType().equals(OKTYPE))
          return obResult;
        strHeaderLog.append(NEW_LINE + "@CreateAccountingSuccess@" + NEW_LINE);
        logEvent("@CreateAccountingSuccess@");
        bAccountingCreated = true;
      } else {
        logEvent("@SkippingAccounting@." + NEW_LINE + "@ModuleMustBeProvided@");
        log4j.debug("process() - Accounting not inserted through a file. "
            + "It must be provided through a module, then");
      }
    } else {
      appendHeader(NEW_LINE + "@SkippingAccounting@");
    }
    logEvent(NEW_LINE + STRSEPARATOR);

    if (strModules.equals("")) {
      if (boCreateAccounting && !bAccountingCreated)
        return logErrorAndRollback(
            "@CreateReferenceDataFailed@. @CreateAccountingButNoCoAProvided@",
            "createOrganization() - Create accounting option was active, but no file was provided, and no accounting module was chosen",
            null);
      log4j.debug("process() - No modules to apply. Skipping creation of reference data");
      strHeaderLog.append(NEW_LINE + "@SkippingReferenceData@" + NEW_LINE);
      logEvent(NEW_LINE + "@SkippingReferenceData@");
    } else {
      logEvent(NEW_LINE + "@StartingReferenceData@");
      log4j.debug("process() - Starting creation of reference data");
      obResult = createReferenceData(strSourcePath, strModules, bProduct, bBPartner, bProject,
          bCampaign, bSalesRegion, (bAccountingCreated) ? false : boCreateAccounting, strCurrency);
      if (!obResult.getType().equals(OKTYPE))
        return obResult;
      logEvent(NEW_LINE + "@CreateReferenceDataSuccess@");
      strHeaderLog.append(NEW_LINE + "@CreateReferenceDataSuccess@" + NEW_LINE);
    }
    try {
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      logErrorAndRollback(
          "@ExceptionInCommit@",
          "createClient() - Exception occured while performing commit in database. Your data may have NOT been saved in database.",
          e);
    }

    obResult.setType(OKTYPE);
    obResult.setMessage("@" + OKTYPE + "@");

    return obResult;

  }

  private OBError createReferenceData(String strSourcePath, String strModulesProvided,
      boolean product, boolean partner, boolean project, boolean campaign, boolean salesRegion,
      boolean boCreateAccounting, String strCurrency) {
    log4j.debug("createReferenceData() - Starting the process to create"
        + " reference data for modules: " + strModulesProvided);
    OBError obeResult = new OBError();
    obeResult.setType(ERRORTYPE);

    String strModules = cleanUpStrModules(strModulesProvided);
    log4j.debug("createReferenceData() - Modules to be processed: " + strModules);
    if (!strModules.equals("")) {
      log4j.debug("createReferenceData() - There exists modules to process");
      if (boCreateAccounting) {
        try {
          log4j.debug("createReferenceData() - There exists accounting modules to process");
          obeResult = insertAccountingModule(strSourcePath, strModules, partner, product, project,
              campaign, salesRegion,
              InitialSetupUtility.getTranslatedColumnName(language, "Account_ID"),
              InitialSetupUtility.getTranslatedColumnName(language, "C_Calendar_ID"), strCurrency);
          if (!obeResult.getType().equals(OKTYPE))
            return obeResult;
          log4j.debug("createReferenceData() - Accounting module processed. ");
        } catch (Exception e) {
          return logErrorAndRollback("@CreateReferenceDataFailed@",
              "createReferenceData() - Exception ocurred while inserting reference data", e);
        }
      }
      List<Module> lRefDataModules;
      try {
        lRefDataModules = InitialSetupUtility.getRDModules(strModules);
        if (lRefDataModules.size() > 0) {
          log4j.debug("createReferenceData() - " + lRefDataModules.size()
              + " reference data modules to install");
          obeResult = insertReferenceDataModules(lRefDataModules);
          if (!obeResult.getType().equals(OKTYPE))
            return obeResult;
          log4j.debug("createReferenceData() - Reference data correctly created");
        } else
          log4j.debug("InitialClientSetup - createReferenceData "
              + "- No Reference Data modules to be installed.");
      } catch (final Exception err) {
        return logErrorAndRollback("@CreateReferenceDataFailed@",
            "createReferenceData() - Exception ocurred while inserting reference data", err);
      }
    }
    obeResult.setType(OKTYPE);
    return obeResult;
  }

  private OBError insertReferenceDataModules(List<Module> refDataModules) {
    log4j.debug("insertReferenceDataModules() - Starting client creation.");
    OBError obeResult = new OBError();
    obeResult.setType(OKTYPE);
    ArrayList<String> strModules = new ArrayList<String>();

    for (Module module : refDataModules)
      strModules.add(module.getId());

    try {
      OBContext.setAdminMode();
      strModules = new ArrayList<String>(ModuleUtiltiy.orderByDependency(strModules));
    } catch (Exception e) {
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "insertReferenceDataModules() - Exception ocurred while "
              + "sorting reference data modules by dependencies", e);
    } finally {
      OBContext.restorePreviousMode();
    }

    for (int i = 0; i < strModules.size(); i++) {
      String strModuleId = strModules.get(i);
      Module module = null;
      for (int j = 0; j < refDataModules.size(); j++)
        if (refDataModules.get(j).getId().equals(strModuleId))
          module = refDataModules.get(j);
      if (module == null)
        return logErrorAndRollback("@CreateReferenceDataFailed@",
            "insertReferenceDataModules() - ERROR - Cannot retrieve the module of id "
                + strModuleId, null);

      logEvent(NEW_LINE + "@ProcessingModule@ " + module.getName());
      log4j.debug("Processing module " + module.getName());

      List<DataSet> lDataSets;
      try {
        ArrayList<String> accessLevel = new ArrayList<String>();
        accessLevel.add("3"); // Client/Org
        accessLevel.add("1"); // Organization
        lDataSets = InitialSetupUtility.getDataSets(module, accessLevel);
        if (lDataSets == null)
          return logErrorAndRollback("@CreateReferenceDataFailed@",
              "insertReferenceDataModules() - ERROR ocurred while obtaining datasets for module "
                  + module.getName(), null);
      } catch (Exception e) {
        return logErrorAndRollback("@CreateReferenceDataFailed@",
            "insertReferenceDataModules() - Exception ocurred while obtaining datasets for module "
                + module.getName(), e);
      }

      log4j.debug("insertReferenceDataModules() - Obtained " + lDataSets.size()
          + " datasets for module " + module.getName());

      for (DataSet dataSet : lDataSets) {
        log4j.debug("insertReferenceDataModules() - Inserting dataset " + dataSet.getName());
        logEvent("@ProcessingDataset@ " + dataSet.getName());

        ImportResult iResult;
        try {
          iResult = InitialSetupUtility.insertReferenceData(dataSet, client, org);
        } catch (Exception e) {
          return logErrorAndRollback("@CreateReferenceDataFailed@",
              "insertReferenceDataModules() - Exception ocurred while obtaining datasets for module "
                  + module.getName(), e);
        }
        if (iResult.getErrorMessages() != null && !iResult.getErrorMessages().equals("")
            && !iResult.getErrorMessages().equals("null")) {
          logEvent(iResult.getErrorMessages());
          return logErrorAndRollback("@CreateReferenceDataFailed@",
              "insertReferenceDataModules() - Exception ocurred while obtaining datasets for module "
                  + module.getName() + NEW_LINE + iResult.getErrorMessages(), null);
        }
        if (iResult.getWarningMessages() != null && !iResult.getWarningMessages().equals("")
            && !iResult.getWarningMessages().equals("null")) {
          logEvent(iResult.getWarningMessages());
        }
        if (iResult.getLogMessages() != null && !iResult.getLogMessages().equals("")
            && !iResult.getLogMessages().equals("null")) {
          log4j.debug(iResult.getLogMessages());
        }
        List<BaseOBObject> elements = iResult.getInsertedObjects();
        logEvent(elements.size() + " @RowsInserted@");
        elements = iResult.getUpdatedObjects();
        logEvent(elements.size() + " @RowsUpdated@");
      }
    }
    return obeResult;
  }

  private OBError insertAccountingModule(String strSourcePath, String strModules,
      boolean bBPartner, boolean bProduct, boolean bProject, boolean bCampaign,
      boolean bSalesRegion, String strAccountText, String strCalendarText, String strCurrency) {
    log4j.debug("insertAccountingModule() - Starting client creation.");
    if (client == null)
      return logErrorAndRollback(
          "@CreateClientFailed@",
          "insertAccountingModule() - ERROR - No client in class attribute client! Cannot create accounting.",
          null);
    OBError obeResult = new OBError();
    obeResult.setType(OKTYPE);
    List<Module> lCoaModules = null;
    Module modCoA = null;

    Tree accountTree = null;

    log4j.debug("createAccounting() - Retrieving the account tree");
    try {
      accountTree = InitialSetupUtility.getTree("EV", client, null);
      if (accountTree == null)
        return logErrorAndRollback("@CreateAccountingFailed@",
            "createAccounting() - couldn't retrieve the account tree for the client", null);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateAccountingFailed@",
          "createAccounting() - couldn't retrieve the account tree for the client", err);
    }

    try {
      lCoaModules = InitialSetupUtility.getCOAModules(strModules);
      // Modules with CoA are retrieved.
      if (lCoaModules.size() > 1) {
        // If more than one accounting module was provided, throws error
        return logErrorAndRollback("@CreateReferenceDataFailed@. @OneCoAModule@",
            "createReferenceData() - "
                + "Error. More than one chart of accounts module was selected", null);
      } else if (lCoaModules.size() == 1) {
        // If just one CoA module was selected, accounting is created
        modCoA = lCoaModules.get(0);
        logEvent(NEW_LINE + "@ProcessingAccountingModule@ " + modCoA.getName());
        log4j.debug("createReferenceData() - Processing Chart of Accounts module "
            + modCoA.getName());
        String strPath = "";
        try {
          OBContext.setAdminMode();
          strPath = strSourcePath + "/modules/" + modCoA.getJavaPackage()
              + "/referencedata/accounts/COA.csv";
        } finally {
          OBContext.restorePreviousMode();
        }
        COAUtility coaUtility = new COAUtility(client, org, accountTree);
        FileInputStream inputStream = new FileInputStream(strPath);
        obeResult = coaUtility.createAccounting(null, inputStream, bBPartner, bProduct, bProject,
            bCampaign, bSalesRegion, strAccountText, "US", "A", strCalendarText,
            InitialSetupUtility.getCurrency(strCurrency));
        strLog.append(coaUtility.getLog());
      } else
        return logErrorAndRollback(
            "@CreateReferenceDataFailed@. @CreateAccountingButNoCoAProvided@",
            "createReferenceData() - Create accounting option was active, but no file was provided, and no accoutning module was chosen",
            null);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "createReferenceData() - Exception while processing accounting modules", e);
    }
    ADOrgModule orgModule = null;
    try {
      orgModule = InitialSetupUtility.insertOrgModule(client, org, modCoA);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "createReferenceData() - Exception while updating version installed of the accounting module "
              + modCoA.getName(), e);
    }
    if (orgModule == null)
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "createReferenceData() - Exception while updating version installed of the accounting module "
              + modCoA.getName(), null);
    return obeResult;
  }

  private String cleanUpStrModules(String strModulesProvided) {
    String strModules = "";
    if (strModulesProvided != null && !strModulesProvided.equals("")) {
      // Remove ( ) characters from the In string as it causes a failure
      if (strModulesProvided.charAt(0) == '(')
        strModules = strModulesProvided.substring(1, strModulesProvided.length());
      else
        strModules = strModulesProvided;
      if (strModulesProvided.charAt(strModulesProvided.length() - 1) == ')')
        strModules = strModules.substring(0, strModules.length() - 1);
    }
    return strModules;
  }

  private OBError createAccounting(FileItem fileCoAFilePath, String strCurrency, boolean partner,
      boolean product, boolean project, boolean campaign, boolean salesRegion,
      VariablesSecureApp vars) {
    OBError obResult = new OBError();
    obResult.setType(ERRORTYPE);
    Tree accountTree = null;

    log4j.debug("createAccounting() - Retrieving the account tree");
    try {
      accountTree = InitialSetupUtility.getTree("EV", client, null);
      if (accountTree == null)
        return logErrorAndRollback("@CreateAccountingFailed@",
            "createAccounting() - couldn't retrieve the account tree for the client", null);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateAccountingFailed@",
          "createAccounting() - couldn't retrieve the account tree for the client", err);
    }

    log4j.debug("createAccounting() - Starting the creation of the accounting.");
    logEvent(NEW_LINE + "@StartingAccounting@" + NEW_LINE);
    COAUtility coaUtility = new COAUtility(client, org, accountTree);
    InputStream istrFileCoA;
    try {
      istrFileCoA = fileCoAFilePath.getInputStream();
    } catch (IOException e) {
      return logErrorAndRollback(
          "@CreateAccountingFailed@",
          "createAccounting() - Exception occured while reading the file "
              + fileCoAFilePath.getName(), e);
    }
    try {
      OBContext.setAdminMode(true);
      obResult = coaUtility.createAccounting(vars, istrFileCoA, partner, product, project,
          campaign, salesRegion,
          InitialSetupUtility.getTranslatedColumnName(language, "Account_ID"), "US", "A",
          InitialSetupUtility.getTranslatedColumnName(language, "C_Calendar_ID"),
          InitialSetupUtility.getCurrency(strCurrency));
      if (!obResult.getType().equals(OKTYPE))
        return obResult;
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateAccountingFailed@",
          "createAccounting() - Create Accounting Failed", err);
    } finally {
      OBContext.restorePreviousMode();
    }
    log4j.debug("createAccounting() - Accounting creation finished correctly.");
    strLog.append(coaUtility.getLog());

    obResult.setType(OKTYPE);
    return obResult;

  }

  private OBError addImages() {
    OBError obResult = new OBError();
    obResult.setType(ERRORTYPE);
    if (client.getClientInformationList().get(0).getYourCompanyDocumentImage() != null)
      try {
        OBContext.setAdminMode(true);
        InitialSetupUtility.setOrgImage(client, org, client.getClientInformationList().get(0)
            .getYourCompanyDocumentImage().getBindaryData(),
            client.getClientInformationList().get(0).getYourCompanyDocumentImage().getName());
      } catch (final Exception err) {
        obResult.setMessage(err.getMessage());
        return obResult;
      } finally {
        OBContext.restorePreviousMode();
      }
    obResult.setType(OKTYPE);
    return obResult;
  }

  private OBError checkDuplicated(String strOrgUser, String strOrgName) {
    OBError obResult = new OBError();
    obResult.setType(ERRORTYPE);

    log4j.debug("checkDuplicated() - Checking organization name");
    try {
      if (InitialSetupUtility.existsOrgName(client, strOrgName)) {
        return logErrorAndRollback("@DuplicateOrgName@",
            "createOrganization() - ERROR - Organization name already existed in database: "
                + strOrgName, null);
      }
    } catch (final Exception err) {
      return logErrorAndRollback("@DuplicateOrgUser@?",
          "createOrganization() - ERROR - Checking if organization name already existed in database: "
              + strOrgName, err);
    }

    log4j.debug("checkDuplicated() - Checking user name");
    try {
      if (InitialSetupUtility.existsUserName(strOrgUser)) {
        return logErrorAndRollback("@DuplicateOrgUser@",
            "createOrganization() - ERROR - User name already existed in database: " + strOrgUser,
            null);
      }
    } catch (final Exception err) {
      return logErrorAndRollback("@DuplicateOrgUser@?",
          "createOrganization() - ERROR - Checking if user name already existed in database: "
              + strOrgUser, err);
    }
    obResult.setType(OKTYPE);
    return obResult;

  }

  private OBError insertUser(String strOrgUser, String strPassword) {
    OBError obResult = new OBError();
    obResult.setType(ERRORTYPE);
    log4j.debug("insertUser() - Organization User Name: " + strOrgUser);

    try {
      user = InitialSetupUtility.insertUser(client, null, strOrgUser,
          FormatUtilities.sha1Base64(strPassword), null, language);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateOrgFailed@",
          "insertUser() - ERROR - Not able to insert the user " + strOrgUser, err);
    }
    log4j.debug("insertUser() - User correctly inserted. Inserting user roles.");

    try {
      role = InitialSetupUtility.insertRole(client, null, strOrgUser, "  O", false);
      if (role == null)
        return logErrorAndRollback("@CreateOrgFailed@",
            "insertUser() - ERROR - Not able to insert the role" + strOrgUser, null);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateOrgFailed@",
          "insertUser() - ERROR - Not able to insert the role" + strOrgUser, err);
    }
    log4j.debug("insertRoles() - Role inserted correctly");

    log4j.debug("insertRoles() - Inserting role org access");
    try {
      RoleOrganization roleOrg = InitialSetupUtility.insertRoleOrganization(role, org, true);
      if (roleOrg == null)
        return logErrorAndRollback("@CreateOrgFailed@",
            "insertUser() - Not able to insert the role organizations access" + strOrgUser, null);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateOrgFailed@",
          "insertUser() - Not able to insert the role organizations access" + strOrgUser, err);
    }
    log4j.debug("insertUser() - Role organizations access inserted correctly");

    log4j.debug("insertRoles() - Inserting user role");
    try {
      UserRoles userRoles = InitialSetupUtility.insertUserRole(client, user, null, role, true);
      if (userRoles == null)
        return logErrorAndRollback("@CreateOrgFailed@",
            "insertUser() - Not able to insert the user role", null);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateOrgFailed@",
          "insertUser() - Not able to insert the user role", err);
    }

    obResult.setType(OKTYPE);

    return obResult;
  }

  private OBError insertOrganization(String strOrgName, String strOrgType, String strParentOrg,
      String strcLocationId) {

    OBError obResult = new OBError();
    obResult.setType(ERRORTYPE);

    Tree orgTree = null;
    TreeNode orgNode = null;

    try {
      org = InitialSetupUtility.insertOrganization(strOrgName, getOrgType(strOrgType),
          strcLocationId, client);
      if (org == null)
        return logErrorAndRollback("@CreateOrgFailed@",
            "createOrganization() - ERROR - Organization creation process failed.", null);
    } catch (final Exception err) {
      return logErrorAndRollback("@CreateOrgFailed@",
          "createOrganization() - ERROR - Organization creation process failed.", err);
    }

    try {
      orgTree = InitialSetupUtility.getOrgTree(client);
    } catch (final Exception err) {
      return logErrorAndRollback(
          "@CreateOrgFailed@",
          "createOrganization() - ERROR - Organization creation process failed. Couldn't obtain organization tree object",
          err);
    }

    try {
      orgNode = InitialSetupUtility.getTreeNode(org, orgTree, client);
      if (orgNode == null)
        return logErrorAndRollback(
            "@CreateOrgFailed@",
            "createOrganization() - ERROR - Organization creation process failed while retrieving organization node.",
            null);
    } catch (final Exception err) {
      return logErrorAndRollback(
          "@CreateOrgFailed@",
          "createOrganization() - ERROR - Organization creation process failed. Couldn't obtain organization tree node object",
          err);
    }

    try {
      InitialSetupUtility.updateOrgTree(orgTree, orgNode,
          OBDal.getInstance().get(Organization.class, strParentOrg));
    } catch (final Exception err) {
      return logErrorAndRollback(
          "@CreateOrgFailed@",
          "createOrganization() - ERROR - Organization creation process failed. Couldn't obtain organization tree node object",
          err);
    }

    obResult.setType(OKTYPE);
    return obResult;
  }

  private OrganizationType getOrgType(String strOrgType) {
    return OBDal.getInstance().get(OrganizationType.class, strOrgType);
  }

  public String getLog() {
    return strHeaderLog.append(NEW_LINE).append(STRSEPARATOR).append(NEW_LINE + NEW_LINE)
        .append(strLog).toString();
  }

  /**
   * Adds a message to the log to be returned
   * 
   * @param strMessage
   *          Message to be added to the log returned (will be translated)
   */
  private void logEvent(String strMessage) {
    strLog.append(strMessage).append(NEW_LINE);
  }

  private void appendHeader(String strText) {
    strHeaderLog.append(strText).append(NEW_LINE);
    logEvent(strText + NEW_LINE);
  }

  /**
   * This functions registers an error occurred in any of the functions of the class
   * 
   * @param strMessage
   *          Message to be shown in the title of the returned web page (will be translated)
   * @param strLogError
   *          (Optional) Message to be added to the log4j (not translated)
   * @param e
   *          (Optional) Exception: optional parameter, just in case the error was caused by an
   *          exception
   */
  private OBError logErrorAndRollback(String strMessage, String strLogError, Exception e) {
    OBError obeResult = new OBError();
    obeResult.setType(ERRORTYPE);
    obeResult.setTitle(ERRORTYPE);
    obeResult.setMessage(strMessage);
    logEvent(strMessage);
    strHeaderLog.append(NEW_LINE + strMessage + NEW_LINE);

    if (strLogError != null)
      log4j.error(strLogError);

    if (e != null) {
      log4j.error("Exception ", e);
      logEvent(e.getMessage());
    }
    try {
      OBDal.getInstance().rollbackAndClose();
    } catch (Exception ex) {
      log4j.error("Exception executing rollback ", ex);
      logEvent(ex.getMessage());
    }
    return obeResult;
  }

  public String getOrgId() {
    if (org != null)
      return org.getId();
    else
      return "";
  }

}
