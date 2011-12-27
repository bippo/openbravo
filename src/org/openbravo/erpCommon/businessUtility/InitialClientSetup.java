/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.modules.ModuleUtiltiy;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.module.ADClientModule;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.service.db.ImportResult;
import org.openbravo.utils.FormatUtilities;

/**
 * @author David Alsasua
 * 
 *         Initial Client Setup class
 */

public class InitialClientSetup {
  private static final Logger log4j = Logger.getLogger(InitialClientSetup.class);
  private static final long serialVersionUID = 1L;
  private static final String NEW_LINE = "<br />\n";
  private static final String STRMESSAGEOK = "Success";
  private static final String STRMESSAGEERROR = "Error";
  private static final String STRTREETYPEMENU = "MM";
  private static final String STRTREETYPEORG = "OO";
  private static final String STRTREETYPEBP = "BP";
  private static final String STRTREETYPEPROJECT = "PJ";
  private static final String STRTREETYPESALESREGION = "SR";
  private static final String STRTREETYPEPRODUCT = "PR";
  private static final String STRTREETYPEACCOUNT = "EV";
  private static final String STRTREETYPECAMPAIGN = "MC";
  private static final String STRSEPARATOR = "*****************************************************";
  private static final String STRCLIENTNAMESUFFIX = " Admin";
  private boolean bAccountingCreated = false;
  private Tree treeOrg, treeBPartner, treeProject, treeSalesRegion, treeProduct, treeAccount,
      treeMenu, treeCampaign;
  private Client client;
  private Role role;
  private Currency currency;
  private StringBuffer strHeaderLog;
  private StringBuffer strLog;

  public InitialClientSetup() {
    strHeaderLog = new StringBuffer();
    strLog = new StringBuffer();
  }

  public String getLog() {
    return strHeaderLog.append(strLog).toString();
  }

  public OBError createClient(VariablesSecureApp vars, String strCurrencyID, String strClientName,
      String strClientUser, String strPassword, String strModules, String strAccountText,
      String strCalendarText, boolean bCreateAccounting, FileItem fileCoAFilePath,
      Boolean bBPartner, Boolean bProduct, Boolean bProject, Boolean bCampaign, Boolean bSalesRegion) {
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    String strLanguage = vars.getLanguage();
    strHeaderLog.append("@ReportSummary@").append(NEW_LINE);
    try {
      currency = InitialSetupUtility.getCurrency(strCurrencyID);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@", "process() - Cannot determine currency.",
          e);
    }

    log4j.debug("process() - Creating client.");
    obeResult = insertClient(vars, strClientName, strClientUser, strCurrencyID);
    if (!obeResult.getType().equals(STRMESSAGEOK))
      return obeResult;
    log4j.debug("process() - Client correctly created.");

    log4j.debug("process() - Creating trees.");
    obeResult = insertTrees(vars);
    if (!obeResult.getType().equals(STRMESSAGEOK))
      return obeResult;
    log4j.debug("process() - Trees correcly created.");

    log4j.debug("process() - Creating client information.");
    obeResult = insertClientInfo();
    if (!obeResult.getType().equals(STRMESSAGEOK))
      return obeResult;
    log4j.debug("process() - Client information correcly created.");

    log4j.debug("process() - Inserting images.");
    obeResult = insertImages(vars);
    if (!obeResult.getType().equals(STRMESSAGEOK))
      return obeResult;
    log4j.debug("process() - Images correctly inserted.");

    log4j.debug("process() - Inserting roles.");
    obeResult = insertRoles();
    if (!obeResult.getType().equals(STRMESSAGEOK))
      return obeResult;
    log4j.debug("process() - Roles correctly inserted.");

    log4j.debug("process() - Inserting client user.");
    obeResult = insertUser(strClientUser, strClientName, strPassword, strLanguage);
    if (!obeResult.getType().equals(STRMESSAGEOK))
      return obeResult;
    log4j.debug("process() - Client user correctly inserted. CLIENT CREATION COMPLETED CORRECTLY!");

    strHeaderLog.append(NEW_LINE).append("@CreateClientSuccess@").append(NEW_LINE);
    logEvent(NEW_LINE + "@CreateClientSuccess@");
    logEvent(NEW_LINE + STRSEPARATOR);

    if (bCreateAccounting == false) {
      log4j.debug("process() - No accounting will be created.");
      logEvent(NEW_LINE + "@SkippingAccounting@");
    } else if (fileCoAFilePath != null && fileCoAFilePath.getSize() > 0) {
      log4j.debug("process() - Accounting creation for the new client.");
      obeResult = createAccounting(vars, fileCoAFilePath, bBPartner, bProduct, bProject, bCampaign,
          bSalesRegion, strAccountText, strCalendarText);
      if (!obeResult.getType().equals(STRMESSAGEOK))
        return obeResult;
      bAccountingCreated = true;
      log4j.debug("process() - Accounting creation finished correctly.");
      strHeaderLog.append(NEW_LINE + "@CreateAccountingSuccess@" + NEW_LINE);
    } else {
      logEvent("@SkippingAccounting@." + NEW_LINE + "@ModuleMustBeProvided@");
      log4j.debug("process() - Accounting not inserted through a file. "
          + "It must be provided through a module, then");
    }
    logEvent(NEW_LINE + "*****************************************************");

    if (strModules.equals("")) {
      log4j.debug("process() - No modules to apply. Skipping creation of reference data");
      logEvent(NEW_LINE + "@SkippingReferenceData@");
    } else {
      logEvent(NEW_LINE + "@StartingReferenceData@");
      log4j.debug("process() - Starting creation of reference data");
      obeResult = createReferenceData(vars, strModules, strAccountText, bProduct, bBPartner,
          bProject, bCampaign, bSalesRegion, (bAccountingCreated) ? false : bCreateAccounting,
          strCalendarText);
      if (!obeResult.getType().equals(STRMESSAGEOK))
        return obeResult;
      logEvent(NEW_LINE + "@CreateReferenceDataSuccess@");
      strHeaderLog.append(NEW_LINE + "@CreateReferenceDataSuccess@" + NEW_LINE);
    }

    try {
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      logErrorAndRollback(
          "@ExceptionInCommit@",
          "createClient() - Exception occured while performing commit in database. Your data may have NOT been saved in database.",
          e);
    }
    obeResult.setType(STRMESSAGEOK);
    obeResult.setMessage("@" + STRMESSAGEOK + "@");

    return obeResult;
  }

  OBError insertClient(VariablesSecureApp vars, String strClientName, String strClientUser,
      String strCurrency) {
    log4j.debug("insertClient() - Starting client creation.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    log4j.debug("insertClient() - Checking if name chosen for the client is already in use.");
    try {
      if (InitialSetupUtility.existsClientName(strClientName)) {
        return logErrorAndRollback("@DuplicateClient@",
            "insertClient() - ERROR - Client Name already existed in database: " + strClientName);
      }
    } catch (Exception e) {
      return logErrorAndRollback("@DuplicateClient@",
          "insertClient() - ERROR - Exception checking existency in database of client "
              + strClientName, e);
    }
    log4j.debug("insertClient() - Client did not exist in database. Can be created.");

    log4j.debug("insertClient() - Checking if user name chosen for the client is already in use.");
    try {
      if (InitialSetupUtility.existsUserName(strClientUser)) {
        return logErrorAndRollback("@DuplicateClientUser@",
            "insertClient() - ERROR - User Name already existed in database: " + strClientUser);
      }
    } catch (Exception e) {
      return logErrorAndRollback("@DuplicateClientUser@",
          "insertClient() - ERROR - Exception checking existency in database of user "
              + strClientUser, e);
    }
    log4j.debug("insertClient() - User name not existed in database. Can be created.");

    logEvent(NEW_LINE + "*****************************************************");
    logEvent(NEW_LINE + "@StartingClient@");

    log4j.debug("insertClient() - Creating Client");
    try {
      client = InitialSetupUtility.insertClient(strClientName, strCurrency);
      if (client == null) {
        return logErrorAndRollback("@CreateClientFailed@",
            "insertClient() - ERROR - Failed creating user " + strClientUser);
      }
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertClient() - ERROR - Exception creating user " + strClientUser, e);
    }
    vars.setSessionValue("AD_Client_ID", client.getId());
    log4j.debug("insertClient() - Correctly created client " + strClientName);

    return obeResult;
  }

  OBError insertTrees(VariablesSecureApp vars) {
    if (client == null)
      return logErrorAndRollback("@CreateClientFailed@",
          "insertTrees() - ERROR - No client in class attribute client! Cannot create trees.");

    log4j.debug("insertTrees() - Inserting trees for client " + client.getName());
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    logEvent(NEW_LINE + "@Client@=" + client.getName());

    log4j.debug("insertTrees() - Obtaining tree relation");
    List<org.openbravo.model.ad.domain.List> treeList;
    try {
      treeList = InitialSetupUtility.treeRelation();
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertTrees() - ERROR - Not able to retrieve trees", e);
    }
    if (treeList == null) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertTrees() - ERROR - Not able to retrieve trees");
    } else {
      log4j.debug("insertTrees() - Retrieved " + treeList.size() + " trees.");
    }

    log4j.debug("insertTrees() - Creating trees");
    try {
      for (Iterator<org.openbravo.model.ad.domain.List> listElements = treeList.iterator(); listElements
          .hasNext();) {
        org.openbravo.model.ad.domain.List listElement = listElements.next();
        log4j.debug("insertTrees() - Processing tree " + listElement.getName() + "("
            + listElement.getDescription() + ")");

        if (listElement.getSearchKey().equals(STRTREETYPEMENU)) {
          log4j.debug("insertTrees() - It is a menu tree");
          Tree t = InitialSetupUtility.getSystemMenuTree(STRTREETYPEMENU);
          if (t == null)
            return logErrorAndRollback("@CreateClientFailed@",
                "insertTrees() - ERROR - Unable to obtain system menu tree");
          else {
            saveTree(t, STRTREETYPEMENU);
            log4j.debug("insertTrees() - Saved menu tree.");
          }
        } else {
          String strTreeType = (String) listElement.getSearchKey();
          String strName = client.getName() + " " + (String) listElement.getName();
          log4j.debug("insertTrees() - Tree of type " + strTreeType + ". Inserting new tree named "
              + strName);

          Tree t = InitialSetupUtility.insertTree(client, strName, strTreeType, true);
          if (t == null)
            return logErrorAndRollback("@CreateClientFailed@",
                "insertTrees() - ERROR - Unable to create trees for the client");
          logEvent("@Client@=" + strName);
          log4j.debug("insertTrees() - Tree correctly inserted in database");
          saveTree(t, strTreeType);
        }
      }
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertTrees() - ERROR - Unable to create trees; an exception occured.", e);
    }

    log4j.debug("insertTrees() - All trees correctly created.");
    return obeResult;
  }

  void saveTree(Tree tree, String strTreeType) {

    if (strTreeType == null)
      log4j.debug("saveTree() - strTreeType is null!!");
    log4j.debug("saveTree() - Saving tree " + tree.getName());
    if (strTreeType.equals(STRTREETYPEORG)) {
      treeOrg = tree;
      return;
    } else if (strTreeType.equals(STRTREETYPEBP)) {
      treeBPartner = tree;
      return;
    } else if (strTreeType.equals(STRTREETYPEPROJECT)) {
      treeProject = tree;
      return;
    } else if (strTreeType.equals(STRTREETYPESALESREGION)) {
      treeSalesRegion = tree;
      return;
    } else if (strTreeType.equals(STRTREETYPEPRODUCT)) {
      treeProduct = tree;
      return;
    } else if (strTreeType.endsWith(STRTREETYPEACCOUNT)) {
      treeAccount = tree;
      return;
    } else if (strTreeType.endsWith(STRTREETYPEMENU)) {
      treeMenu = tree;
      return;
    } else if (strTreeType.endsWith(STRTREETYPECAMPAIGN)) {
      treeCampaign = tree;
      return;
    }
  }

  OBError insertClientInfo() {
    log4j.debug("insertClientInfo() - Starting the creation of client information.");
    if (client == null || treeMenu == null || treeOrg == null || treeBPartner == null
        || treeProject == null || treeSalesRegion == null || treeProduct == null
	|| treeCampaign == null) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertClientInfo() - ERROR - Required information is not present. "
              + "Please check that client and trees where correctly created.");
    }
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);
    ClientInformation clientInfo;
    try {
      clientInfo = InitialSetupUtility.insertClientinfo(client, treeMenu, treeOrg, treeBPartner,
          treeProject, treeSalesRegion, treeProduct, treeCampaign, true);
      if (clientInfo == null)
        return logErrorAndRollback("@CreateClientFailed@",
            "insertClientInfo() - ERROR - Unable to create client information");
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertClientInfo() - ERROR - Unable to create client information", e);
    }
    log4j.debug("insertClientInfo() - Client Information correctly saved in database."
        + " Associating to the client.");

    try {
      if (!InitialSetupUtility.setClientInformation(client, clientInfo))
        return logErrorAndRollback("@CreateClientFailed@",
            "insertClientInfo() - ERROR - Unable to create client information");
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertClientInfo() - ERROR - Unable to create client information", e);
    }
    log4j.debug("insertClientInfo() - Client Information correctly associated to the client.");
    return obeResult;
  }

  OBError insertImages(VariablesSecureApp vars) {
    if (client == null)
      return logErrorAndRollback("@CreateClientFailed@",
          "insertImages() - ERROR - No client in class attribute client! Cannot create trees.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);
    log4j.debug("insertImages() - Setting client images");
    try {
      InitialSetupUtility.setClientImages(client);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertImages() - ERROR - Unable to set client images", e);
    }
    log4j.debug("insertImages() - Client images correctly set");

    return obeResult;
  }

  OBError insertRoles() {
    if (client == null)
      return logErrorAndRollback("@CreateClientFailed@",
          "insertRoles() - ERROR - No client in class attribute client! Cannot create trees.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    String strRoleName = client.getName() + STRCLIENTNAMESUFFIX;
    log4j.debug("insertRoles() - Inserting role with name= " + strRoleName);

    try {
      role = InitialSetupUtility.insertRole(client, null, strRoleName, null);
      if (role == null)
        return logErrorAndRollback("@CreateClientFailed@",
            "insertRoles() - ERROR - Not able to insert the role" + strRoleName);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertRoles() - ERROR - Not able to insert the role" + strRoleName, e);
    }
    log4j.debug("insertRoles() - Role inserted correctly");
    logEvent("@AD_Role_ID@=" + strRoleName);

    log4j.debug("insertRoles() - Inserting role org access");
    try {
      RoleOrganization roleOrg = InitialSetupUtility.insertRoleOrganization(role, null);
      if (roleOrg == null)
        return logErrorAndRollback("@CreateClientFailed@",
            "insertRoles() - Not able to insert the role organizations access" + strRoleName);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertRoles() - Not able to insert the role organizations access" + strRoleName, e);
    }
    log4j.debug("insertRoles() - Role organizations access inserted correctly");

    logEvent("@AD_Role_ID@=" + strRoleName);

    return obeResult;
  }

  OBError insertUser(String strUserNameProvided, String strClientName, String strPassword,
      String strLanguage) {
    if (client == null)
      return logErrorAndRollback("@CreateClientFailed@",
          "insertUser() - ERROR - No client in class attribute client! Cannot create trees.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    String strUserName = strUserNameProvided;
    if (strUserName == null || strUserName.length() == 0)
      strUserName = strClientName + "Client";
    log4j.debug("insertUser() - Inserting user named " + strUserName);
    User user;
    try {
      user = InitialSetupUtility.insertUser(client, null, strUserName,
          FormatUtilities.sha1Base64(strPassword), role,
          InitialSetupUtility.getLanguage(strLanguage));
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertUser() - ERROR - Not able to insert the user " + strUserName, e);
    }
    log4j.debug("insertUser() - User correctly inserted. Inserting user roles.");

    logEvent("@AD_User_ID@=" + strUserName + "/" + strUserName);
    try {
      InitialSetupUtility.insertUserRoles(client, user, null, role);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateClientFailed@",
          "insertUser() - Not able to insert the user " + strUserName, e);
    }
    log4j.debug("insertUser() - User roles correctly inserted.");

    return obeResult;
  }

  OBError createAccounting(VariablesSecureApp vars,
      org.apache.commons.fileupload.FileItem fileCoAFilePath, Boolean bBPartner, Boolean bProduct,
      Boolean bProject, Boolean bCampaign, Boolean bSalesRegion, String strAccountText,
      String strCalendarText) {
    if (client == null || treeAccount == null)
      return logErrorAndRollback(
          "@CreateAccountingFailed@",
          "createAccounting() - ERROR - No client or account tree in the class attributes! Cannot create accounting.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    log4j.debug("createAccounting() - Starting the creation of the accounting.");
    logEvent(NEW_LINE + "@StartingAccounting@" + NEW_LINE);
    COAUtility coaUtility = new COAUtility(client, treeAccount);
    InputStream istrFileCoA;
    try {
      istrFileCoA = fileCoAFilePath.getInputStream();
    } catch (IOException e) {
      return logErrorAndRollback(
          "@CreateAccountingFailed@",
          "createAccounting() - Exception occured while reading the file "
              + fileCoAFilePath.getName(), e);
    }
    obeResult = coaUtility.createAccounting(vars, istrFileCoA, bBPartner, bProduct, bProject,
        bCampaign, bSalesRegion, strAccountText, "US", "A", strCalendarText, currency);

    strLog.append(coaUtility.getLog());
    return obeResult;
  }

  OBError insertAccountingModule(VariablesSecureApp vars, String strModules, boolean bBPartner,
      boolean bProduct, boolean bProject, boolean bCampaign, boolean bSalesRegion,
      String strAccountText, String strCalendarText) {
    log4j.debug("insertAccountingModule() - Starting client creation.");
    if (client == null)
      return logErrorAndRollback("@CreateClientFailed@",
          "insertAccountingModule() - ERROR - No client in class attribute client! Cannot create accounting.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);
    List<Module> lCoaModules = null;
    Module modCoA = null;
    try {
      lCoaModules = InitialSetupUtility.getCOAModules(strModules);
      // Modules with CoA are retrieved.
      if (lCoaModules.size() > 1) {
        // If more than one accounting module was provided, throws error
        return logErrorAndRollback("@CreateReferenceDataFailed@. @OneCoAModule@",
            "createReferenceData() - "
                + "Error. More than one chart of accounts module was selected");
      } else if (lCoaModules.size() == 1) {
        // If just one CoA module was selected, accounting is created
        modCoA = lCoaModules.get(0);
        logEvent(NEW_LINE + "@ProcessingAccountingModule@ " + modCoA.getName());
        log4j.debug("createReferenceData() - Processing Chart of Accounts module "
            + modCoA.getName());
        String strPath = vars.getSessionValue("#SOURCEPATH") + "/modules/"
            + modCoA.getJavaPackage() + "/referencedata/accounts/COA.csv";
        COAUtility coaUtility = new COAUtility(client, treeAccount);
        FileInputStream inputStream = new FileInputStream(strPath);
        obeResult = coaUtility.createAccounting(vars, inputStream, bBPartner, bProduct, bProject,
            bCampaign, bSalesRegion, strAccountText, "US", "A", strCalendarText, currency);
        strLog.append(coaUtility.getLog());
      } else
        return logErrorAndRollback(
            "@CreateReferenceDataFailed@. @CreateAccountingButNoCoAProvided@",
            "createReferenceData() - Create accounting option was active, but no file was provided, and no accoutning module was chosen");
    } catch (Exception e) {
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "createReferenceData() - Exception while processing accounting modules", e);
    }
    ADClientModule clientModule = null;
    try {
      clientModule = InitialSetupUtility.insertClientModule(client, modCoA);
    } catch (Exception e) {
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "createReferenceData() - Exception while updating version installed of the accounting module "
              + modCoA.getName(), e);
    }
    if (clientModule == null)
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "createReferenceData() - Exception while updating version installed of the accounting module "
              + modCoA.getName());
    return obeResult;
  }

  OBError createReferenceData(VariablesSecureApp vars, String strModulesProvided,
      String strAccountText, boolean bProduct, boolean bBPartner, boolean bProject,
      boolean bCampaign, boolean bSalesRegion, boolean bCreateAccounting, String strCalendarText) {
    log4j.debug("createReferenceData() - Starting the process to create"
        + " reference data for modules: " + strModulesProvided);
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);

    String strModules = cleanUpStrModules(strModulesProvided);
    log4j.debug("createReferenceData() - Modules to be processed: " + strModules);
    if (!strModules.equals("")) {
      log4j.debug("createReferenceData() - There exists modules to process");
      if (bCreateAccounting) {
        log4j.debug("createReferenceData() - There exists accounting modules to process");
        obeResult = insertAccountingModule(vars, strModules, bBPartner, bProduct, bProject,
            bCampaign, bSalesRegion, strAccountText, strCalendarText);
        if (!obeResult.getType().equals(STRMESSAGEOK))
          return obeResult;
        log4j.debug("createReferenceData() - Accounting module processed. ");
      }
      try {
        List<Module> lRefDataModules = InitialSetupUtility.getRDModules(strModules);
        if (lRefDataModules.size() > 0) {
          log4j.debug("createReferenceData() - " + lRefDataModules.size()
              + " reference data modules to install");
          obeResult = insertReferenceDataModules(lRefDataModules);
          if (!obeResult.getType().equals(STRMESSAGEOK))
            return obeResult;
          log4j.debug("createReferenceData() - Reference data correctly created");
        } else
          log4j.debug("InitialClientSetup - createReferenceData "
              + "- No Reference Data modules to be installed.");
      } catch (Exception e) {
        return logErrorAndRollback("@CreateReferenceDataFailed@",
            "createReferenceData() - Exception ocurred while inserting reference data", e);
      }
    }
    return obeResult;
  }

  OBError insertReferenceDataModules(List<Module> refDataModules) {
    log4j.debug("insertReferenceDataModules() - Starting client creation.");
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEOK);
    ArrayList<String> strModules = new ArrayList<String>();

    for (Module module : refDataModules)
      strModules.add(module.getId());

    try {
      strModules = new ArrayList<String>(ModuleUtiltiy.orderByDependency(strModules));
    } catch (Exception e) {
      return logErrorAndRollback("@CreateReferenceDataFailed@",
          "insertReferenceDataModules() - Exception ocurred while "
              + "sorting reference data modules by dependencies", e);
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
                + strModuleId);

      logEvent(NEW_LINE + "@ProcessingModule@ " + module.getName());
      log4j.debug("Processing module " + module.getName());

      List<DataSet> lDataSets;
      try {
        ArrayList<String> accessLevel = new ArrayList<String>();
        accessLevel.add("3");
        accessLevel.add("6");
        lDataSets = InitialSetupUtility.getDataSets(module, accessLevel);
        if (lDataSets == null)
          return logErrorAndRollback("@CreateReferenceDataFailed@",
              "insertReferenceDataModules() - ERROR ocurred while obtaining datasets for module "
                  + module.getName());
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
          iResult = InitialSetupUtility.insertReferenceData(dataSet, client, null);
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
                  + module.getName() + NEW_LINE + iResult.getErrorMessages());
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

  /**
   * Adds a message to the log to be returned
   * 
   * @param strMessage
   *          Message to be added to the log returned (will be translated)
   */
  private void logEvent(String strMessage) {
    strLog.append(strMessage).append(NEW_LINE);
  }

  /**
   * This functions registers an error occurred in any of the functions of the class
   * 
   * @param strMessage
   *          Message to be shown in the title of the returned web page (will be translated)
   * @param strLogError
   *          Message to be added to the log4j (not translated)
   * @param e
   *          Exception: optional parameter, just in case the error was caused by an exception
   *          raised
   */
  private OBError logErrorAndRollback(String strMessage, String strLogError, Exception e) {
    OBError obeResult = new OBError();
    obeResult.setType(STRMESSAGEERROR);
    obeResult.setTitle(STRMESSAGEERROR);
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

  private OBError logErrorAndRollback(String strMessage, String strLogError) {
    return logErrorAndRollback(strMessage, strLogError, null);
  }

}
