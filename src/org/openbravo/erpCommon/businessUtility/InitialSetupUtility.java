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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.module.ADClientModule;
import org.openbravo.model.ad.module.ADOrgModule;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.ui.ElementTrl;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.ad.ui.MessageTrl;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentTemplate;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.EmailTemplate;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationAcctSchema;
import org.openbravo.model.common.enterprise.OrganizationType;
import org.openbravo.model.common.geography.Location;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaDefault;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaElement;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaGL;
import org.openbravo.model.financialmgmt.accounting.coa.Element;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValueOperand;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.model.financialmgmt.calendar.Year;
import org.openbravo.model.financialmgmt.gl.GLCategory;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * @author David Alsasua
 * 
 *         Initial Client Setup Utility class
 */
public class InitialSetupUtility {
  private static final Logger log4j = Logger.getLogger(InitialSetupUtility.class);

  /**
   * 
   * @param strClient
   *          name of the client
   * @return true if exists client in database with provided name
   * @throws Exception
   */
  public static boolean existsClientName(String strClient) throws Exception {
    final OBCriteria<Client> obcClient = OBDal.getInstance().createCriteria(Client.class);
    obcClient.add(Restrictions.eq(Client.PROPERTY_NAME, strClient));
    return obcClient.count() > 0;
  }

  /**
   * 
   * @param strUser
   *          user name
   * @return true if exists a user with the name provided in database
   * @throws Exception
   */
  public static boolean existsUserName(String strUser) throws Exception {
    OBContext.setAdminMode();
    try {
      final OBCriteria<User> obcUser = OBDal.getInstance().createCriteria(User.class);
      obcUser.setFilterOnReadableClients(false);
      obcUser.setFilterOnReadableOrganization(false);
      obcUser.add(Restrictions.eq(User.PROPERTY_USERNAME, strUser));
      return obcUser.count() > 0;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param strClientName
   *          client name
   * @param strCurrency
   *          currency id
   * @return Client object for the created client
   * @throws Exception
   */
  public static Client insertClient(String strClientName, String strCurrency) throws Exception {
    log4j.debug("InitialSetupUtility - insertClient() - clientName = " + strClientName);
    Currency currency = getCurrency(strCurrency);
    final Client newClient = OBProvider.getInstance().get(Client.class);
    newClient.setCurrency(currency);
    newClient.setSearchKey(strClientName);
    newClient.setName(strClientName);
    newClient.setDescription(strClientName);
    newClient.setNewOBObject(true);
    OBDal.getInstance().save(newClient);
    OBDal.getInstance().flush();
    return newClient;
  }

  /**
   * 
   * @param strCurrencyID
   *          c_currency_id
   * @return Currency object that belongs to provided id
   * @throws Exception
   */
  public static Currency getCurrency(String strCurrencyID) throws Exception {
    return OBDal.getInstance().get(Currency.class, strCurrencyID);
  }

  /**
   * 
   * @param strLanguage
   *          language key (for example en_US)
   * @return Language object corresponding to provided key
   * @throws Exception
   */
  public static Language getLanguage(String strLanguage) throws Exception {
    final OBCriteria<Language> obcLanguage = OBDal.getInstance().createCriteria(Language.class);
    obcLanguage.add(Restrictions.eq(Language.PROPERTY_LANGUAGE, strLanguage));
    if (obcLanguage.list().size() > 0)
      return obcLanguage.list().get(0);
    else
      return null;
  }

  /**
   * Returns the relation of trees defined in the reference list of the application dictionary
   * called AD_TreeType Type
   * 
   * @return java.util.List<org.openbravo.model.ad.domain.List>: the relation of AD list elements
   * @throws Exception
   */
  public static List<org.openbravo.model.ad.domain.List> treeRelation() throws Exception {

    final OBCriteria<org.openbravo.model.ad.domain.Reference> obcReference = OBDal.getInstance()
        .createCriteria(org.openbravo.model.ad.domain.Reference.class);
    obcReference.add(Restrictions.eq(org.openbravo.model.ad.domain.Reference.PROPERTY_NAME,
        "AD_TreeType Type"));
    List<org.openbravo.model.ad.domain.Reference> listReferences = obcReference.list();
    if (listReferences.size() != 1)
      return null;

    org.openbravo.model.ad.domain.Reference referenceTree = listReferences.get(0);
    final OBCriteria<org.openbravo.model.ad.domain.List> obcRefTreeList = OBDal.getInstance()
        .createCriteria(org.openbravo.model.ad.domain.List.class);
    obcRefTreeList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE,
        referenceTree));
    obcRefTreeList.addOrder(Order.asc("name"));
    return obcRefTreeList.list();
  }

  /**
   * Returns the tree of the provided type
   * 
   * @param strTreeTypeMenu
   *          two letters corresponding to the tree type for the menu
   * @return Tree menu element (defined at system level)
   * @throws Exception
   */
  public static Tree getSystemMenuTree(String strTreeTypeMenu) throws Exception {
    final OBCriteria<Tree> obcTree = OBDal.getInstance().createCriteria(Tree.class);
    obcTree.add(Restrictions.eq(Tree.PROPERTY_TYPEAREA, strTreeTypeMenu));
    List<Tree> lTrees = obcTree.list();
    if (lTrees.size() != 1)
      return null;
    return lTrees.get(0);
  }

  /**
   * 
   * @param client
   * @param name
   * @param treeType
   * @param boIsAllNodes
   * @return object Tree for the new tree
   * @throws Exception
   */
  public static Tree insertTree(Client client, String name, String treeType, Boolean boIsAllNodes)
      throws Exception {
    final Tree newTree = OBProvider.getInstance().get(Tree.class);
    newTree.setClient(client);
    newTree.setName(name);
    newTree.setDescription(name);
    newTree.setTypeArea(treeType);
    newTree.setAllNodes(boIsAllNodes);
    OBDal.getInstance().save(newTree);
    OBDal.getInstance().flush();
    return newTree;
  }

  /**
   * @deprecated use new insertClientinfo method where new parameter "campaignTree" is added
   * 
   * @param client
   * @param menuTree
   * @param orgTree
   * @param bpartnerTree
   * @param projectTree
   * @param salesRegionTree
   * @param productTree
   * @param boDiscountCalculatedFromLineAmounts
   * @return ClientInformation object for the new element
   * @throws Exception
   * @deprecated
   */
  @Deprecated
  public static ClientInformation insertClientinfo(Client client, Tree menuTree, Tree orgTree,
      Tree bpartnerTree, Tree projectTree, Tree salesRegionTree, Tree productTree,
      Boolean boDiscountCalculatedFromLineAmounts) throws Exception {

    return insertClientinfo(client, menuTree, orgTree, bpartnerTree, projectTree, salesRegionTree,
        productTree, null, boDiscountCalculatedFromLineAmounts);
  }

  /**
   * 
   * @param client
   * @param menuTree
   * @param orgTree
   * @param bpartnerTree
   * @param projectTree
   * @param salesRegionTree
   * @param productTree
   * @param campaignTree
   * @param boDiscountCalculatedFromLineAmounts
   * @return ClientInformation object for the new element
   * @throws Exception
   */
  public static ClientInformation insertClientinfo(Client client, Tree menuTree, Tree orgTree,
      Tree bpartnerTree, Tree projectTree, Tree salesRegionTree, Tree productTree,
      Tree campaignTree, Boolean boDiscountCalculatedFromLineAmounts) throws Exception {
    final ClientInformation newClientInfo = OBProvider.getInstance().get(ClientInformation.class);
    newClientInfo.setClient(client);
    newClientInfo.setPrimaryTreeMenu(menuTree);
    newClientInfo.setPrimaryTreeOrganization(orgTree);
    newClientInfo.setPrimaryTreeBPartner(bpartnerTree);
    newClientInfo.setPrimaryTreeProject(projectTree);
    newClientInfo.setPrimaryTreeSalesRegion(salesRegionTree);
    newClientInfo.setPrimaryTreeProduct(productTree);
    if (campaignTree != null)
      newClientInfo.setTreeCampaign(campaignTree);
    newClientInfo.setDiscountCalculatedFromLineAmounts(boDiscountCalculatedFromLineAmounts);
    OBDal.getInstance().save(newClientInfo);
    OBDal.getInstance().flush();
    return newClientInfo;
  }

  /**
   * Associates a client info record to a client
   * 
   * @param client
   * @param clientInfo
   * @return true if update was correct
   * @throws Exception
   */
  public static boolean setClientInformation(Client client, ClientInformation clientInfo)
      throws Exception {
    boolean boResult = client.getClientInformationList().add(clientInfo);
    OBDal.getInstance().save(client);
    OBDal.getInstance().flush();
    return boResult;
  }

  /**
   * 
   * @param client
   * @throws Exception
   */
  public static void setOrgImage(Client client, Organization org, byte[] image, String strImageName)
      throws Exception {
    Image yourCompanyDocumentImage = OBProvider.getInstance().get(Image.class);
    yourCompanyDocumentImage.setClient(client);
    yourCompanyDocumentImage.setOrganization(org);
    yourCompanyDocumentImage.setBindaryData(image);
    yourCompanyDocumentImage.setName(strImageName);
    org.getOrganizationInformationList().get(0)
        .setYourCompanyDocumentImage(yourCompanyDocumentImage);
    yourCompanyDocumentImage.setOrganization(org);
    OBDal.getInstance().save(yourCompanyDocumentImage);
    OBDal.getInstance().save(org);
    OBDal.getInstance().flush();
  }

  /**
   * 
   * @param client
   * @throws Exception
   */
  public static void setClientImages(Client client) throws Exception {
    SystemInformation sys = OBDal.getInstance().get(SystemInformation.class, "0");
    setYourCompanyBigImage(sys, client);
    setYourCompanyDocumentImage(sys, client);
    setYourCompanyMenuImage(sys, client);
  }

  /**
   * 
   * @param sys
   * @param client
   */
  public static void setYourCompanyBigImage(SystemInformation sys, Client client) {
    Image yourCompanyBigImage = OBProvider.getInstance().get(Image.class);
    Image systemCompanyBigImage = sys.getYourCompanyBigImage();
    if (systemCompanyBigImage != null) {
      yourCompanyBigImage.setClient(client);
      yourCompanyBigImage.setBindaryData(systemCompanyBigImage.getBindaryData());
      yourCompanyBigImage.setName(systemCompanyBigImage.getName());
      client.getClientInformationList().get(0).setYourCompanyBigImage(yourCompanyBigImage);
      OBDal.getInstance().save(yourCompanyBigImage);
      OBDal.getInstance().flush();
    }
  }

  /**
   * 
   * @param sys
   * @param client
   */
  public static void setYourCompanyDocumentImage(SystemInformation sys, Client client) {
    Image yourCompanyDocumentImage = OBProvider.getInstance().get(Image.class);
    if (sys.getYourCompanyDocumentImage() != null) {
      yourCompanyDocumentImage.setClient(client);
      yourCompanyDocumentImage.setBindaryData(sys.getYourCompanyDocumentImage().getBindaryData());
      yourCompanyDocumentImage.setName(sys.getYourCompanyBigImage().getName());
      client.getClientInformationList().get(0)
          .setYourCompanyDocumentImage(yourCompanyDocumentImage);
      OBDal.getInstance().save(yourCompanyDocumentImage);
      OBDal.getInstance().flush();
    }
  }

  /**
   * 
   * @param sys
   * @param client
   */
  public static void setYourCompanyMenuImage(SystemInformation sys, Client client) {
    Image yourCompanyMenuImage = OBProvider.getInstance().get(Image.class);
    if (sys.getYourCompanyMenuImage() != null) {
      yourCompanyMenuImage.setClient(client);
      yourCompanyMenuImage.setBindaryData(sys.getYourCompanyMenuImage().getBindaryData());
      yourCompanyMenuImage.setName(sys.getYourCompanyMenuImage().getName());
      client.getClientInformationList().get(0).setYourCompanyMenuImage(yourCompanyMenuImage);
      OBDal.getInstance().save(yourCompanyMenuImage);
      OBDal.getInstance().flush();
    }
  }

  /**
   * 
   * @param client
   *          client for which the role will be created
   * @param orgProvided
   *          if null, role inserted for organization with id=0
   * @param name
   *          name of the role
   * @param strUserLevelProvided
   *          if null, user level " CO" will be set to the new role
   * @return Role object for new element
   */
  public static Role insertRole(Client client, Organization orgProvided, String name,
      String strUserLevelProvided) throws Exception {
    return insertRole(client, orgProvided, name, strUserLevelProvided, true);
  }

  /**
   * 
   * @param client
   *          client for which the role will be created
   * @param orgProvided
   *          if null, role inserted for organization with id=0
   * @param name
   *          name of the role
   * @param strUserLevelProvided
   *          if null, user level " CO" will be set to the new role
   * @return Role object for new element
   */
  public static Role insertRole(Client client, Organization orgProvided, String name,
      String strUserLevelProvided, boolean isClientAdmin) throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    String strUserLevel;
    if (strUserLevelProvided == null || strUserLevelProvided.equals(""))
      strUserLevel = " CO";
    else
      strUserLevel = strUserLevelProvided;

    final Role newRole = OBProvider.getInstance().get(Role.class);
    newRole.setClient(client);
    newRole.setOrganization(organization);
    newRole.setName(name);
    newRole.setDescription(name);
    newRole.setClientList(client.getId());
    newRole.setOrganizationList(organization.getId());
    newRole.setUserLevel(strUserLevel);
    newRole.setClientAdmin(isClientAdmin);
    OBDal.getInstance().save(newRole);
    OBDal.getInstance().flush();
    return newRole;
  }

  /**
   * 
   * @param role
   *          role for which the organization access information will be created
   * @param orgProvided
   *          if null, organization with id "0" will be used
   * @return RoleOrganization object for new element
   */
  public static RoleOrganization insertRoleOrganization(Role role, Organization orgProvided)
      throws Exception {
    return insertRoleOrganization(role, orgProvided, false);
  }

  /**
   * 
   * @param role
   *          role for which the organization access information will be created
   * @param orgProvided
   *          if null, organization with id "0" will be used
   * @return RoleOrganization object for new element
   */
  public static RoleOrganization insertRoleOrganization(Role role, Organization orgProvided,
      boolean isOrgAdmin) throws Exception {
    OBContext.setAdminMode();
    try {
      Organization organization = null;
      if (orgProvided == null) {
        if ((organization = getZeroOrg()) == null)
          return null;
      } else
        organization = orgProvided;

      final RoleOrganization newRoleOrganization = OBProvider.getInstance().get(
          RoleOrganization.class);
      newRoleOrganization.setClient(role.getClient());
      newRoleOrganization.setOrganization(organization);
      newRoleOrganization.setRole(role);
      newRoleOrganization.setOrgAdmin(isOrgAdmin);
      OBDal.getInstance().save(newRoleOrganization);
      OBDal.getInstance().flush();
      return newRoleOrganization;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param client
   * @param orgProvided
   * @param name
   * @param password
   * @param role
   * @param defaultLanguage
   * @return User object
   * @throws Exception
   */
  public static User insertUser(Client client, Organization orgProvided, String name,
      String password, Role role, Language defaultLanguage) throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;

    final User newUser = OBProvider.getInstance().get(User.class);
    newUser.setClient(client);
    newUser.setOrganization(organization);
    newUser.setName(name);
    newUser.setDescription(name);
    newUser.setUsername(name);
    newUser.setPassword(password);
    newUser.setDefaultLanguage(defaultLanguage);
    if (role != null)
      newUser.setDefaultRole(role);
    OBDal.getInstance().save(newUser);
    OBDal.getInstance().flush();
    return newUser;
  }

  /**
   * 
   * @param client
   * @param user
   * @param orgProvided
   * @param role
   * @return UserRoles object for new element
   * @throws Exception
   */
  public static UserRoles insertUserRole(Client client, User user, Organization orgProvided,
      Role role) throws Exception {
    return insertUserRole(client, user, orgProvided, role, true);
  }

  /**
   * 
   * @param client
   * @param user
   * @param orgProvided
   * @param role
   * @return UserRoles object for new element
   * @throws Exception
   */
  public static UserRoles insertUserRole(Client client, User user, Organization orgProvided,
      Role role, boolean isRoleAdmin) throws Exception {
    OBContext.setAdminMode();
    try {
      Organization organization = null;

      if (orgProvided == null) {
        if ((organization = getZeroOrg()) == null)
          return null;
      } else
        organization = orgProvided;

      final UserRoles newUserRole = OBProvider.getInstance().get(UserRoles.class);
      newUserRole.setClient(client);
      newUserRole.setOrganization(organization);
      newUserRole.setRole(role);
      newUserRole.setUserContact(user);
      newUserRole.setRoleAdmin(isRoleAdmin);
      OBDal.getInstance().save(newUserRole);
      OBDal.getInstance().flush();
      return newUserRole;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Inserts a new role for the created client and user. Also user Openbravo will have rights to
   * access new client
   * 
   * @param client
   * @param user
   * @param organization
   * @param role
   * @throws Exception
   */
  public static void insertUserRoles(Client client, User user, Organization organization, Role role)
      throws Exception {
    insertUserRole(client, user, organization, role);
    insertUserRole(client, OBDal.getInstance().get(User.class, "100"), organization, role);
  }

  /**
   * 
   * @param client
   * @param orgProvided
   * @param name
   * @return Calendar object for new element
   * @throws Exception
   */
  public static Calendar insertCalendar(Client client, Organization orgProvided, String name)
      throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;

    final Calendar newCalendar = OBProvider.getInstance().get(Calendar.class);
    newCalendar.setClient(client);
    newCalendar.setOrganization(organization);
    newCalendar.setName(name);
    OBDal.getInstance().save(newCalendar);
    OBDal.getInstance().flush();
    return newCalendar;
  }

  /**
   * 
   * @return Organization object for * organization (with id 0)
   */
  private static Organization getZeroOrg() {
    return OBDal.getInstance().get(Organization.class, "0");
  }

  /**
   * 
   * @param client
   * @param orgProvided
   * @param calendar
   * @param strYearName
   * @return Year object for new element
   * @throws Exception
   */
  public static Year insertYear(Client client, Organization orgProvided, Calendar calendar,
      String strYearName) throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    final Year newYear = OBProvider.getInstance().get(Year.class);
    newYear.setClient(client);
    newYear.setOrganization(organization);
    newYear.setCalendar(calendar);
    newYear.setFiscalYear(strYearName);
    OBDal.getInstance().save(newYear);
    OBDal.getInstance().flush();
    return newYear;
  }

  /**
   * 
   * @param client
   * @param orgProvided
   * @param name
   * @param accountTree
   * @param bNaturalAccount
   * @return Element object for new element
   * @throws Exception
   */
  public static Element insertElement(Client client, Organization orgProvided, String name,
      Tree accountTree, Boolean bNaturalAccount) throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    final Element newElement = OBProvider.getInstance().get(Element.class);
    newElement.setClient(client);
    newElement.setOrganization(organization);
    newElement.setName(name);
    newElement.setDescription(name);
    newElement.setTree(accountTree);
    newElement.setNaturalAccount(bNaturalAccount);
    OBDal.getInstance().save(newElement);
    OBDal.getInstance().flush();
    return newElement;
  }

  /**
   * 
   * @param element
   * @param orgProvided
   * @param name
   * @param value
   * @param description
   * @param accountType
   * @param accountSign
   * @param isDocControlled
   * @param isSummary
   * @param elementLevel
   * @param doFlush
   * @return ElementValue object for new element
   * @throws Exception
   */
  public static ElementValue insertElementValue(Element element, Organization orgProvided,
      String name, String value, String description, String accountType, String accountSign,
      boolean isDocControlled, boolean isSummary, String elementLevel, boolean doFlush)
      throws Exception {
    return insertElementValue(element, orgProvided, name, value, description, accountType,
        accountSign, isDocControlled, isSummary, elementLevel, doFlush, null, null);
  }

  public static ElementValue insertElementValue(Element element, Organization orgProvided,
      String name, String value, String description, String accountType, String accountSign,
      boolean isDocControlled, boolean isSummary, String elementLevel, boolean doFlush,
      String showValueCond, String titleNode) throws Exception {
    OBContext.setAdminMode();
    try {
      Organization organization = null;
      if (orgProvided == null) {
        if ((organization = getZeroOrg()) == null)
          return null;
      } else
        organization = orgProvided;

      final ElementValue newElementValue = OBProvider.getInstance().get(ElementValue.class);
      newElementValue.setClient(element.getClient());
      newElementValue.setOrganization(organization);
      newElementValue.setSearchKey(value);
      newElementValue.setName(name);
      newElementValue.setDescription(description);
      newElementValue.setAccountingElement(element);
      newElementValue.setAccountType(accountType);
      newElementValue.setAccountSign(accountSign);
      newElementValue.setDocumentControlled(isDocControlled);
      newElementValue.setSummaryLevel(isSummary);
      newElementValue.setElementLevel(elementLevel);
      if (showValueCond != null && !"".equals(showValueCond))
        newElementValue.setShowValueCondition(showValueCond.substring(0, 1));
      if (titleNode != null && !"".equals(titleNode))
        newElementValue.setTitleNode("Y".equals(titleNode.subSequence(0, 1)));
      OBDal.getInstance().save(newElementValue);
      if (doFlush)
        OBDal.getInstance().flush();
      return newElementValue;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the nodes of a given tree
   * 
   * @param accountTree
   * @param client
   * @param orgProvided
   * @return List<TreeNode> with relation of tree node elements of the provided tree
   * @throws Exception
   */
  public static List<TreeNode> getTreeNode(Tree accountTree, Client client, Organization orgProvided)
      throws Exception {
    Organization organization;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    List<TreeNode> lTreeNodes;
    OBContext.setAdminMode();
    try {
      final OBCriteria<TreeNode> obcTreeNode = OBDal.getInstance().createCriteria(TreeNode.class);
      obcTreeNode.add(Restrictions.eq(TreeNode.PROPERTY_TREE, accountTree));
      obcTreeNode.add(Restrictions.eq(TreeNode.PROPERTY_CLIENT, client));
      obcTreeNode.add(Restrictions.eq(TreeNode.PROPERTY_ORGANIZATION, organization));
      if (OBContext.getOBContext().isInAdministratorMode()) {
        obcTreeNode.setFilterOnReadableClients(false);
        obcTreeNode.setFilterOnReadableOrganization(false);
      }
      lTreeNodes = obcTreeNode.list();
      return lTreeNodes;
    } catch (Exception e) {
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the nodes of a given tree
   * 
   * @param accountTree
   * @param client
   * @return List<TreeNode> with relation of tree node elements of the provided tree
   * @throws Exception
   */
  public static List<TreeNode> getTreeNode(Tree accountTree, Client client) throws Exception {
    return getTreeNode(accountTree, client, null);
  }

  /**
   * Sorts the account tree (stored in ADTreeNode) according to the order provided
   * 
   * @param treeNodes
   *          relation of nodes in ADTreeNode belonging to the accounting tree to sort out
   * @param mapSequence
   *          HashMap<String,Long> where the String belongs to the value of a c_elementvalue, and
   *          Long to the sequence that must be assigned to the node that represents that element
   *          value in ADTreeNode
   * @param mapElementValueValue
   *          each tree node in treeNodes has one entry in mapElementValueId to link it's value with
   *          the c_elementvalue_id of that element in c_elementvalue table
   * @param mapElementValueId
   *          stores the link value <-> c_elementvalue_id
   * @param mapParent
   *          stores the link value <-> value of the parent
   * @param doFlush
   *          if true, each new update performs a flush in DAL
   * @throws Exception
   */
  public static void updateAccountTree(List<TreeNode> treeNodes, HashMap<String, Long> mapSequence,
      HashMap<String, String> mapElementValueValue, HashMap<String, String> mapElementValueId,
      HashMap<String, String> mapParent, boolean doFlush) throws Exception {
    OBContext.setAdminMode();
    try {
      Iterator<TreeNode> iTreeNodes = treeNodes.listIterator();
      while (iTreeNodes.hasNext()) {
        try {
          TreeNode treeNode = iTreeNodes.next();
          String strElementId = treeNode.getNode();
          String strElementValue = "0";
          Long lSequence = 10L;
          if (!strElementId.equals("0")) {
            strElementValue = mapElementValueValue.get(strElementId);
            lSequence = mapSequence.get(strElementValue);
            treeNode.setSequenceNumber(lSequence);
            String strParentValue = mapParent.get(strElementValue);
            if (!strParentValue.equals("0"))
              treeNode.setReportSet(mapElementValueId.get(strParentValue));
            OBDal.getInstance().save(treeNode);
          }
          if (doFlush)
            OBDal.getInstance().flush();
        } catch (Exception ignoredException) {
          log4j.error("updateAccountTree() - Ignored exception while sorting account tree.",
              ignoredException);
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param elementValue
   * @param operand
   * @param sign
   * @param sequence
   * @return ElementValueOperand object for the inserted element
   * @throws Exception
   */
  public static ElementValueOperand insertOperand(ElementValue elementValue, ElementValue operand,
      Long sign, Long sequence) throws Exception {
    final ElementValueOperand newElementValueOperand = OBProvider.getInstance().get(
        ElementValueOperand.class);
    newElementValueOperand.setClient(elementValue.getClient());
    newElementValueOperand.setOrganization(elementValue.getOrganization());
    newElementValueOperand.setSign(sign);
    newElementValueOperand.setSequenceNumber(sequence);
    newElementValueOperand.setAccountElement(operand);
    newElementValueOperand.setAccount(elementValue);
    OBDal.getInstance().save(newElementValueOperand);
    OBDal.getInstance().flush();
    return newElementValueOperand;
  }

  /**
   * 
   * @param element
   * @param value
   * @return ElementValue object for the given value in the provided element
   * @throws Exception
   */
  public static ElementValue getElementValue(Element element, String value) throws Exception {
    OBContext.setAdminMode();
    try {
      final OBCriteria<ElementValue> obcEV = OBDal.getInstance().createCriteria(ElementValue.class);
      if (obcEV == null)
        return null;
      obcEV.setFilterOnReadableClients(false);
      obcEV.setFilterOnReadableOrganization(false);
      obcEV.add(Restrictions.eq(ElementValue.PROPERTY_SEARCHKEY, value));
      obcEV.add(Restrictions.eq(ElementValue.PROPERTY_ACCOUNTINGELEMENT, element));
      List<ElementValue> l = obcEV.list();
      if (l.size() != 1)
        return null;
      return l.get(0);
    } catch (Exception e) {
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param client
   * @param orgProvided
   * @param currency
   * @param name
   * @param gAAP
   * @param costingMethod
   * @param hasAlias
   * @return AcctSchema element that matches provided data
   * @throws Exception
   */
  public static AcctSchema insertAcctSchema(Client client, Organization orgProvided,
      Currency currency, String name, String gAAP, String costingMethod, boolean hasAlias)
      throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    final AcctSchema newAcctSchema = OBProvider.getInstance().get(AcctSchema.class);
    newAcctSchema.setClient(client);
    newAcctSchema.setOrganization(organization);
    newAcctSchema.setCurrency(currency);
    newAcctSchema.setName(name);
    newAcctSchema.setGAAP(gAAP);
    newAcctSchema.setCostingMethod(costingMethod);
    newAcctSchema.setUseAccountAlias(hasAlias);
    OBDal.getInstance().save(newAcctSchema);
    OBDal.getInstance().flush();
    return newAcctSchema;
  }

  /**
   * 
   * @param acctSchema
   * @param orgProvided
   *          optional parameter. If null, organization 0 will be used
   * @param listElement
   *          element of the reference list which is going to be inserted. From it's name, the name
   *          of the acct.schema element will be taken, and from it's value (search key) the type
   * @param sequence
   * @param isMandatory
   * @param isBalanced
   * @param defaultAccount
   * @param accountingElement
   * @return AcctSchemaElement object for the inserted element
   * @throws Exception
   */
  public static AcctSchemaElement insertAcctSchemaElement(AcctSchema acctSchema,
      Organization orgProvided, org.openbravo.model.ad.domain.List listElement, Long sequence,
      boolean isMandatory, boolean isBalanced, ElementValue defaultAccount,
      Element accountingElement) throws Exception {
    Organization trxOrganization = null;
    if (orgProvided == null) {
      if ((trxOrganization = getZeroOrg()) == null)
        return null;
    } else
      trxOrganization = orgProvided;
    final AcctSchemaElement newAcctSchemaElement = OBProvider.getInstance().get(
        AcctSchemaElement.class);
    newAcctSchemaElement.setAccountingSchema(acctSchema);
    newAcctSchemaElement.setClient(acctSchema.getClient());
    newAcctSchemaElement.setOrganization(acctSchema.getOrganization());
    newAcctSchemaElement.setSequenceNumber(sequence);
    newAcctSchemaElement.setName(listElement.getName());
    newAcctSchemaElement.setType(listElement.getSearchKey());
    newAcctSchemaElement.setMandatory(isMandatory);
    newAcctSchemaElement.setBalanced(isBalanced);
    // Default value for mandatory elements: OO and AC
    if (listElement.getSearchKey().equals("OO")) {
      newAcctSchemaElement.setTrxOrganization(trxOrganization);
    } else if (listElement.getSearchKey().equals("AC")) {
      newAcctSchemaElement.setAccountElement(defaultAccount);
      newAcctSchemaElement.setAccountingElement(accountingElement != null ? accountingElement
          : defaultAccount.getAccountingElement());
    }
    OBDal.getInstance().save(newAcctSchemaElement);
    OBDal.getInstance().flush();
    return newAcctSchemaElement;
  }

  /**
   * 
   * @param defaultElementValues
   *          map with DefaultAccount and ElementValue object that will be set
   * @param acctSchema
   * @return AcctSchemaDefault object for the created element
   * @throws Exception
   */
  public static AcctSchemaDefault insertAcctSchemaDefault(
      HashMap<String, ElementValue> defaultElementValues, AcctSchema acctSchema) throws Exception {
    final AcctSchemaDefault newAcctSchemaDefault = OBProvider.getInstance().get(
        AcctSchemaDefault.class);
    newAcctSchemaDefault.setClient(acctSchema.getClient());
    newAcctSchemaDefault.setOrganization(acctSchema.getOrganization());
    newAcctSchemaDefault.setAccountingSchema(acctSchema);
    Set<String> defaultAccts = defaultElementValues.keySet();
    for (String strDefault : defaultAccts) {
      Client client = defaultElementValues.get(strDefault).getClient();
      Organization org = defaultElementValues.get(strDefault).getOrganization();
      if (strDefault.equals("W_INVENTORY_ACCT"))
        newAcctSchemaDefault.setWarehouseInventory(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("W_DIFFERENCES_ACCT"))
        newAcctSchemaDefault.setWarehouseDifferences(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("W_REVALUATION_ACCT"))
        newAcctSchemaDefault.setInventoryRevaluation(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("W_INVACTUALADJUST_ACCT"))
        newAcctSchemaDefault.setInventoryAdjustment(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_REVENUE_ACCT"))
        newAcctSchemaDefault.setProductRevenue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_EXPENSE_ACCT"))
        newAcctSchemaDefault.setProductExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_ASSET_ACCT"))
        newAcctSchemaDefault.setFixedAsset(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_COGS_ACCT"))
        newAcctSchemaDefault.setProductCOGS(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_PURCHASEPRICEVARIANCE_ACCT"))
        newAcctSchemaDefault.setPurchasePriceVariance(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_INVOICEPRICEVARIANCE_ACCT"))
        newAcctSchemaDefault.setInvoicePriceVariance(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_TRADEDISCOUNTREC_ACCT"))
        newAcctSchemaDefault.setTradeDiscountReceived(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("P_TRADEDISCOUNTGRANT_ACCT"))
        newAcctSchemaDefault.setTradeDiscountGranted(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("C_RECEIVABLE_ACCT"))
        newAcctSchemaDefault.setCustomerReceivablesNo(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("C_PREPAYMENT_ACCT"))
        newAcctSchemaDefault.setCustomerPrepayment(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("V_LIABILITY_ACCT"))
        newAcctSchemaDefault.setVendorLiability(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("V_LIABILITY_SERVICES_ACCT"))
        newAcctSchemaDefault.setVendorServiceLiability(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("V_PREPAYMENT_ACCT"))
        newAcctSchemaDefault.setVendorPrepayment(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("PAYDISCOUNT_EXP_ACCT"))
        newAcctSchemaDefault.setPaymentDiscountExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("PAYDISCOUNT_REV_ACCT"))
        newAcctSchemaDefault.setPaymentDiscountRevenue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("WRITEOFF_ACCT"))
        newAcctSchemaDefault.setWriteoff(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("WRITEOFF_REV_ACCT"))
        newAcctSchemaDefault.setWriteoffRevenue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("UNREALIZEDGAIN_ACCT"))
        newAcctSchemaDefault.setUnrealizedGainsAcct(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("UNREALIZEDLOSS_ACCT"))
        newAcctSchemaDefault.setUnrealizedLossesAcct(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("REALIZEDGAIN_ACCT"))
        newAcctSchemaDefault.setRealizedGainAcct(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("REALIZEDLOSS_ACCT"))
        newAcctSchemaDefault.setRealizedLossAcct(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("WITHHOLDING_ACCT"))
        newAcctSchemaDefault.setWithholdingAccount(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("E_PREPAYMENT_ACCT"))
        newAcctSchemaDefault.setEmployeePrepayments(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("E_EXPENSE_ACCT"))
        newAcctSchemaDefault.setEmployeeExpenses(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("PJ_ASSET_ACCT"))
        newAcctSchemaDefault.setProjectAsset(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("PJ_WIP_ACCT"))
        newAcctSchemaDefault.setWorkInProgress(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("T_EXPENSE_ACCT"))
        newAcctSchemaDefault.setTaxExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("T_LIABILITY_ACCT"))
        newAcctSchemaDefault.setTaxLiability(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("T_RECEIVABLES_ACCT"))
        newAcctSchemaDefault.setTaxReceivables(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("T_DUE_ACCT"))
        newAcctSchemaDefault.setTaxDue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("T_CREDIT_ACCT"))
        newAcctSchemaDefault.setTaxCredit(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_INTRANSIT_ACCT"))
        newAcctSchemaDefault.setBankInTransit(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_ASSET_ACCT"))
        newAcctSchemaDefault.setBankAsset(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_EXPENSE_ACCT"))
        newAcctSchemaDefault.setBankExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_INTERESTREV_ACCT"))
        newAcctSchemaDefault.setBankInterestRevenue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_INTERESTEXP_ACCT"))
        newAcctSchemaDefault.setBankInterestExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_UNIDENTIFIED_ACCT"))
        newAcctSchemaDefault.setBankUnidentifiedReceipts(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_SETTLEMENTGAIN_ACCT"))
        newAcctSchemaDefault.setBankSettlementGain(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_SETTLEMENTLOSS_ACCT"))
        newAcctSchemaDefault.setBankSettlementLoss(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_REVALUATIONGAIN_ACCT"))
        newAcctSchemaDefault.setBankRevaluationGain(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_REVALUATIONLOSS_ACCT"))
        newAcctSchemaDefault.setBankRevaluationLoss(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_PAYMENTSELECT_ACCT"))
        newAcctSchemaDefault.setPaymentSelection(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("B_UNALLOCATEDCASH_ACCT"))
        newAcctSchemaDefault.setUnallocatedCash(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("CH_EXPENSE_ACCT"))
        newAcctSchemaDefault.setChargeExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("CH_REVENUE_ACCT"))
        newAcctSchemaDefault.setChargeRevenue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("UNEARNEDREVENUE_ACCT"))
        newAcctSchemaDefault.setUnearnedRevenue(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("NOTINVOICEDRECEIVABLES_ACCT"))
        newAcctSchemaDefault.setNonInvoicedReceivables(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("NOTINVOICEDREVENUE_ACCT"))
        newAcctSchemaDefault.setNonInvoicedRevenues(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("NOTINVOICEDRECEIPTS_ACCT"))
        newAcctSchemaDefault.setNonInvoicedReceipts(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("CB_ASSET_ACCT"))
        newAcctSchemaDefault.setCashBookAsset(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("CB_CASHTRANSFER_ACCT"))
        newAcctSchemaDefault.setCashTransfer(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("CB_DIFFERENCES_ACCT"))
        newAcctSchemaDefault.setCashBookDifferences(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("CB_RECEIPT_ACCT"))
        newAcctSchemaDefault.setCashBookReceipt(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("A_DEPRECIATION_ACCT"))
        newAcctSchemaDefault.setDepreciation(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("A_ACCUMDEPRECIATION_ACCT"))
        newAcctSchemaDefault.setAccumulatedDepreciation(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));

      if (strDefault.equals("A_DISPOSAL_LOSS"))
        newAcctSchemaDefault.setDisposalLoss(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
      if (strDefault.equals("A_DISPOSAL_GAIN"))
        newAcctSchemaDefault.setDisposalGain(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
      if (strDefault.equals("CB_EXPENSE_ACCT"))
        newAcctSchemaDefault.setCashBookExpense(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
    }

    OBDal.getInstance().save(newAcctSchemaDefault);
    OBDal.getInstance().flush();
    return newAcctSchemaDefault;
  }

  /**
   * 
   * @param defaultElementValues
   *          map with DefaultAccount and ElementValue object that will be set
   * @param acctSchema
   * @return AcctSchemaGL object for the created element
   * @throws Exception
   */
  public static AcctSchemaGL insertAcctSchemaGL(HashMap<String, ElementValue> defaultElementValues,
      AcctSchema acctSchema) throws Exception {
    final AcctSchemaGL newAcctSchemaGL = OBProvider.getInstance().get(AcctSchemaGL.class);
    newAcctSchemaGL.setClient(acctSchema.getClient());
    newAcctSchemaGL.setOrganization(acctSchema.getOrganization());
    newAcctSchemaGL.setAccountingSchema(acctSchema);

    Set<String> defaultAccts = defaultElementValues.keySet();
    for (String strDefault : defaultAccts) {
      Client client = defaultElementValues.get(strDefault).getClient();
      Organization org = defaultElementValues.get(strDefault).getOrganization();
      if (strDefault.equals("CURRENCYBALANCING_ACCT")) {
        newAcctSchemaGL.setCurrencyBalancingAcct(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
        newAcctSchemaGL.setCurrencyBalancingUse(true);
      }
      if (strDefault.equals("INTERCOMPANYDUEFROM_ACCT"))
        newAcctSchemaGL.setDueFromIntercompany(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
      if (strDefault.equals("INTERCOMPANYDUETO_ACCT"))
        newAcctSchemaGL.setDueToIntercompany(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
      if (strDefault.equals("INCOMESUMMARY_ACCT"))
        newAcctSchemaGL.setIncomeSummary(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
      if (strDefault.equals("PPVOFFSET_ACCT"))
        newAcctSchemaGL.setPPVOffset(getAcctComb(client, org, defaultElementValues.get(strDefault),
            acctSchema, true));
      if (strDefault.equals("RETAINEDEARNING_ACCT"))
        newAcctSchemaGL.setRetainedEarning(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
      if (strDefault.equals("SUSPENSEBALANCING_ACCT")) {
        newAcctSchemaGL.setSuspenseBalancing(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
        newAcctSchemaGL.setSuspenseBalancingUse(true);
      }
      if (strDefault.equals("SUSPENSEERROR_ACCT"))
        newAcctSchemaGL.setSuspenseError(getAcctComb(client, org,
            defaultElementValues.get(strDefault), acctSchema, true));
    }

    OBDal.getInstance().save(newAcctSchemaGL);
    OBDal.getInstance().flush();
    return newAcctSchemaGL;
  }

  /**
   * Returns an account combination for the provided ElementValue element. If it doesn't exists,
   * creates a new one.
   * 
   * @param client
   * @param orgProvided
   * @param elementValue
   * @param acctSchema
   * @param isFullyQualified
   * @return AccountingCombination object for the created element
   */
  private static AccountingCombination getAcctComb(Client client, Organization orgProvided,
      ElementValue elementValue, AcctSchema acctSchema, Boolean isFullyQualified) {
    Organization organization;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;

    final AccountingCombination newAcctComb = OBProvider.getInstance().get(
        AccountingCombination.class);

    newAcctComb.setClient(client);
    newAcctComb.setOrganization(organization);
    newAcctComb.setAccount(elementValue);
    newAcctComb.setAccountingSchema(acctSchema);
    newAcctComb.setOrganization(elementValue.getOrganization());
    newAcctComb.setFullyQualified(isFullyQualified);

    OBDal.getInstance().save(newAcctComb);
    OBDal.getInstance().flush();
    return newAcctComb;
  }

  /**
   * 
   * @param client
   * @param acctSchema
   * @param orgProvided
   * @return OrganizationAcctSchema object for the new element
   */
  public static OrganizationAcctSchema insertOrgAcctSchema(Client client, AcctSchema acctSchema,
      Organization orgProvided) {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    final OrganizationAcctSchema newOrganizationAcctSchema = OBProvider.getInstance().get(
        OrganizationAcctSchema.class);
    newOrganizationAcctSchema.setClient(client);
    newOrganizationAcctSchema.setOrganization(organization);
    newOrganizationAcctSchema.setAccountingSchema(acctSchema);
    OBDal.getInstance().save(newOrganizationAcctSchema);
    OBDal.getInstance().flush();
    return newOrganizationAcctSchema;
  }

  /**
   * 
   * @param client
   * @param organization
   * @param name
   * @param categoryType
   * @param isDefault
   * @return GLCategory object for the new element
   */
  public static GLCategory insertCategory(Client client, Organization organization, String name,
      String categoryType, boolean isDefault) {
    final GLCategory newGLCategory = OBProvider.getInstance().get(GLCategory.class);
    newGLCategory.setClient(client);
    newGLCategory.setOrganization(organization);
    newGLCategory.setName(name);
    newGLCategory.setCategoryType(categoryType);
    newGLCategory.setDefault(isDefault);
    OBDal.getInstance().save(newGLCategory);
    OBDal.getInstance().flush();
    return newGLCategory;
  }

  /**
   * 
   * @param client
   * @param organization
   * @param name
   * @param startNo
   * @return Sequence object for the new created element
   */
  public static Sequence insertSequence(Client client, Organization organization, String name,
      Long startNo) {
    final Sequence newSequence = OBProvider.getInstance().get(Sequence.class);
    newSequence.setClient(client);
    newSequence.setOrganization(organization);
    newSequence.setName(name);
    newSequence.setStartingNo(startNo);
    OBDal.getInstance().save(newSequence);
    OBDal.getInstance().flush();
    return newSequence;
  }

  /**
   * 
   * @param client
   * @param organization
   * @param name
   * @param printName
   * @param docBaseType
   * @param docSubTypeSO
   * @param shipment
   * @param invoice
   * @param isDocNoControlled
   * @param sequence
   * @param category
   * @param isSOTrx
   * @param table
   * @return DocumentType object for the new element
   */
  public static DocumentType insertDocType(Client client, Organization organization, String name,
      String printName, String docBaseType, String docSubTypeSO, DocumentType shipment,
      DocumentType invoice, boolean isDocNoControlled, Sequence sequence, GLCategory category,
      boolean isSOTrx, Table table) {
    final DocumentType newDocumentType = OBProvider.getInstance().get(DocumentType.class);
    newDocumentType.setClient(client);
    newDocumentType.setOrganization(organization);
    newDocumentType.setName(name);
    newDocumentType.setPrintText(printName);
    newDocumentType.setDocumentCategory(docBaseType);
    newDocumentType.setSOSubType(docSubTypeSO);
    newDocumentType.setDocumentTypeForShipment(shipment);
    newDocumentType.setDocumentTypeForInvoice(invoice);
    newDocumentType.setSequencedDocument(isDocNoControlled);
    newDocumentType.setDocumentSequence(sequence);
    newDocumentType.setGLCategory(category);
    newDocumentType.setSalesTransaction(isSOTrx);
    newDocumentType.setTable(table);
    OBDal.getInstance().save(newDocumentType);
    OBDal.getInstance().flush();
    return newDocumentType;
  }

  /**
   * Given a dataset, inserts the elements in the xml file into database.
   * 
   * @param dataset
   * @param client
   * @param orgProvided
   * @return ImportResult object for the created element. Errors, warnings and log is provided in
   *         this object.
   * @throws Exception
   */
  public static ImportResult insertReferenceData(DataSet dataset, Client client,
      Organization orgProvided) throws Exception {
    Organization organization = null;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;
    ImportResult myResult = null;
    String strSourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    String strPath = "";
    File datasetFile;
    OBContext.setAdminMode();
    try {
      if (dataset.getModule().getJavaPackage().equals("org.openbravo")) {
        strPath = strSourcePath + "/referencedata/standard";
      } else {
        strPath = strSourcePath + "/modules/" + dataset.getModule().getJavaPackage()
            + "/referencedata/standard";
      }
      datasetFile = new File(strPath + "/" + Utility.wikifiedName(dataset.getName()) + ".xml");
      if (!datasetFile.exists()) {
        return myResult;
      }
      DataImportService myData = DataImportService.getInstance();
      String strXml = Utility.fileToString(datasetFile.getPath());
      myResult = myData.importDataFromXML(client, organization, strXml, dataset.getModule());

      if (myResult.getErrorMessages() != null && !myResult.getErrorMessages().equals("")
          && !myResult.getErrorMessages().equals("null")) {
        return myResult;
      }
      if (organization.getId().equals(getZeroOrg().getId())
          && getClientModuleList(client, dataset.getModule()).size() == 0) {
        insertClientModule(client, dataset.getModule());
      } else if (getOrgModuleList(client, organization, dataset.getModule()).size() == 0) {
        insertOrgModule(client, organization, dataset.getModule());
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return myResult;
  }

  private static List<ADClientModule> getClientModuleList(Client client, Module module) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ADClientModule> clientModules = OBDal.getInstance().createCriteria(
          ADClientModule.class);
      clientModules.add(Restrictions.eq(ADClientModule.PROPERTY_CLIENT, client));
      clientModules.add(Restrictions.eq(ADClientModule.PROPERTY_MODULE, module));
      clientModules.setFilterOnReadableOrganization(false);
      clientModules.setFilterOnReadableClients(false);
      return clientModules.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static List<ADOrgModule> getOrgModuleList(Client client, Organization organization,
      Module module) {
    OBContext.setAdminMode();
    try {
      OBCriteria<ADOrgModule> orgModules = OBDal.getInstance().createCriteria(ADOrgModule.class);
      orgModules.add(Restrictions.eq(ADOrgModule.PROPERTY_CLIENT, client));
      orgModules.add(Restrictions.eq(ADOrgModule.PROPERTY_ORGANIZATION, organization));
      orgModules.add(Restrictions.eq(ADOrgModule.PROPERTY_MODULE, module));
      orgModules.setFilterOnReadableOrganization(false);
      orgModules.setFilterOnReadableClients(false);
      return orgModules.list();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param document
   * @param name
   * @param templateLocation
   * @param templateFileName
   * @param reportFileName
   * @return DocumentTemplate object with the new element
   */
  public static DocumentTemplate insertDoctypeTemplate(DocumentType document, String name,
      String templateLocation, String templateFileName, String reportFileName) {
    final DocumentTemplate newDocumentTemplate = OBProvider.getInstance().get(
        DocumentTemplate.class);
    newDocumentTemplate.setClient(document.getClient());
    newDocumentTemplate.setOrganization(document.getOrganization());
    newDocumentTemplate.setName(name);
    newDocumentTemplate.setDocumentType(document);
    newDocumentTemplate.setTemplateLocation(templateLocation);
    newDocumentTemplate.setTemplateFilename(templateFileName);
    newDocumentTemplate.setReportFilename(reportFileName);
    OBDal.getInstance().save(newDocumentTemplate);
    OBDal.getInstance().flush();
    return newDocumentTemplate;
  }

  /**
   * 
   * @param documentTemplate
   * @return EmailTemplate object for the new element
   */
  public static EmailTemplate insertEmailTemplate(DocumentTemplate documentTemplate) {
    final EmailTemplate newEmailTemplate = OBProvider.getInstance().get(EmailTemplate.class);
    newEmailTemplate.setClient(documentTemplate.getClient());
    newEmailTemplate.setPocDocumentType(documentTemplate);
    OBDal.getInstance().save(newEmailTemplate);
    OBDal.getInstance().flush();
    return newEmailTemplate;
  }

  /**
   * Returns the set of Module objects for the given ids
   * 
   * @param strModules
   *          relation of ids (in a format so that can be included in a "in" statement of a "where"
   *          clause
   * @return List<Module> with the relation of modules
   * @throws Exception
   */
  public static List<Module> getCOAModules(String strModules) throws Exception {
    StringBuilder strWhereClause = new StringBuilder();
    strWhereClause.append(" as module where module.id in (" + strModules + ")");
    strWhereClause.append(" and module.hasChartOfAccounts = 'Y'");
    final OBQuery<Module> obqModule = OBDal.getInstance().createQuery(Module.class,
        strWhereClause.toString());
    return obqModule.list();
  }

  /**
   * Returns the set of Module objects for the given ids
   * 
   * @param strModules
   *          relation of ids (in a format so that can be included in a "in" statement of a "where"
   *          clause
   * @throws Exception
   */
  public static List<Module> getRDModules(String strModules) throws Exception {
    StringBuilder strWhereClause = new StringBuilder();
    strWhereClause.append(" as module where module.id in (" + strModules + ")");
    strWhereClause.append(" and module.hasReferenceData = 'Y'");
    strWhereClause.append(" and module.hasChartOfAccounts = 'N'");
    final OBQuery<Module> obqModule = OBDal.getInstance().createQuery(Module.class,
        strWhereClause.toString());
    return obqModule.list();
  }

  /**
   * @deprecated use {@link #getDataSets(Module, List)}
   * 
   * @param module
   * @param accessLevel
   *          3-> client/org; 1-> organization only
   * @return List<DataSet> with the relation of DataSet objects
   * @throws Exception
   */
  @Deprecated
  public static List<DataSet> getDataSets(Module module, String accessLevel) throws Exception {
    ArrayList<String> coAccessLevel = new ArrayList<String>();
    coAccessLevel.add(accessLevel);
    return getDataSets(module, coAccessLevel);
  }

  /**
   * Given a module, and an access level, returns all the datasets contained in that module
   * 
   * @param module
   * @param accessLevel
   *          3-> client/org; 6-> System/client
   * @return List<DataSet> with the relation of DataSet objects
   * @throws Exception
   */
  public static List<DataSet> getDataSets(Module module, List<String> accessLevel) throws Exception {
    OBContext.setAdminMode();
    try {
      final OBCriteria<DataSet> obcDataSets = OBDal.getInstance().createCriteria(DataSet.class);
      obcDataSets.add(Restrictions.eq(DataSet.PROPERTY_MODULE, module));
      obcDataSets.add(Restrictions.in(DataSet.PROPERTY_DATAACCESSLEVEL, accessLevel));
      obcDataSets.addOrder(Order.asc(DataSet.PROPERTY_NAME));
      if (obcDataSets.list().size() > 0) {
        return obcDataSets.list();
      } else {
        return null;
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the relation of ad_ref_list elements for the reference with AD_Reference_id='181'
   * (Acct.schema elements)
   * 
   * @return List<org.openbravo.model.ad.domain.List> with the relation of ad_ref_list elements
   * @throws Exception
   */
  public static List<org.openbravo.model.ad.domain.List> getAcctSchemaElements() throws Exception {
    OBContext.setAdminMode();
    try {
      final OBCriteria<org.openbravo.model.ad.domain.List> obcRefList = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.domain.List.class);
      obcRefList.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_REFERENCE, OBDal
          .getInstance().get(Reference.class, "181")));
      if (obcRefList.list().size() > 0) {
        return obcRefList.list();
      } else {
        return null;
      }

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * 
   * @param client
   * @param orgProvided
   *          optional parameter. If not provided, "*" organization used
   * @param module
   * @return ADClientModule object with the created element
   * @throws Exception
   */
  public static ADOrgModule insertOrgModule(Client client, Organization orgProvided, Module module)
      throws Exception {
    Organization org;
    if (orgProvided == null) {
      if ((org = getZeroOrg()) == null) {
        return null;
      }
    } else {
      org = orgProvided;
    }

    OBContext.setAdminMode();
    try {
      final ADOrgModule newADOrgModule = OBProvider.getInstance().get(ADOrgModule.class);
      newADOrgModule.setClient(client);
      newADOrgModule.setOrganization(org);
      newADOrgModule.setModule(module);
      newADOrgModule.setVersion(module.getVersion());
      newADOrgModule.setChecksum(getModuleDatasetsChechsum(module));

      OBDal.getInstance().save(newADOrgModule);
      OBDal.getInstance().flush();
      return newADOrgModule;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static String getModuleDatasetsChechsum(Module module) {
    String checksum = "";
    OBContext.setAdminMode();
    try {
      OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
      obc.createAlias(DataSet.PROPERTY_MODULE, "m");
      obc.add(Restrictions.eq(DataSet.PROPERTY_MODULE, module));
      String[] organizationAccessLevel = { "3", "1" };
      String[] systemAccessLevel = { "3", "6" };
      obc.add(Restrictions.or(Restrictions.and(
          Restrictions.ne(DataSet.PROPERTY_ORGANIZATION, getZeroOrg()),
          Restrictions.in(DataSet.PROPERTY_DATAACCESSLEVEL, organizationAccessLevel)), Restrictions
          .and(Restrictions.eq(DataSet.PROPERTY_ORGANIZATION, getZeroOrg()),
              Restrictions.in(DataSet.PROPERTY_DATAACCESSLEVEL, systemAccessLevel))));
      obc.addOrder(Order.asc("m." + Module.PROPERTY_ID));
      obc.addOrder(Order.asc(DataSet.PROPERTY_SEQUENCENUMBER));
      obc.addOrder(Order.asc(DataSet.PROPERTY_ID));
      obc.setFilterOnReadableClients(false);
      obc.setFilterOnReadableOrganization(false);
      for (DataSet dataset : obc.list()) {
        if (checksum.length() > 0 && !StringUtils.isEmpty(dataset.getChecksum())) {
          checksum = checksum + ",";
        }
        checksum = checksum + (dataset.getChecksum() == null ? "" : dataset.getChecksum());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return checksum;
  }

  /**
   * 
   * @param client
   * @param module
   * @return ADClientModule object with the created element
   * @throws Exception
   */
  public static ADClientModule insertClientModule(Client client, Module module) throws Exception {
    OBContext.setAdminMode();
    try {
      final ADClientModule newADClientModule = OBProvider.getInstance().get(ADClientModule.class);
      newADClientModule.setClient(client);
      newADClientModule.setOrganization(getZeroOrg());
      newADClientModule.setModule(module);
      newADClientModule.setVersion(module.getVersion());
      OBDal.getInstance().save(newADClientModule);
      OBDal.getInstance().flush();
      return newADClientModule;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static boolean existsOrgName(Client client, String strOrgName) throws Exception {
    OBContext.setAdminMode();
    try {
      final OBCriteria<Organization> obcOrg = OBDal.getInstance()
          .createCriteria(Organization.class);
      obcOrg.setFilterOnReadableOrganization(false);
      obcOrg.add(Restrictions.eq(Organization.PROPERTY_CLIENT, client));
      obcOrg.add(Restrictions.eq(Organization.PROPERTY_NAME, strOrgName));
      return obcOrg.count() > 0;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static Organization insertOrganization(String strOrgName, OrganizationType orgType,
      String strcLocationId, Client client) throws Exception {
    log4j.debug("InitialSetupUtility - insertOrganization() - name = " + strOrgName);
    OBContext.setAdminMode();
    try {
      final Organization newOrg = OBProvider.getInstance().get(Organization.class);
      newOrg.setClient(client);
      newOrg.setName(strOrgName);
      newOrg.setSearchKey(strOrgName);
      newOrg.setOrganizationType(orgType);
      OBDal.getInstance().save(newOrg);
      OBDal.getInstance().flush();
      return newOrg;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public static Tree getOrgTree(Client client) throws Exception {
    OBCriteria<Tree> obcTree = OBDal.getInstance().createCriteria(Tree.class);
    obcTree.add(Restrictions.eq(Tree.PROPERTY_TYPEAREA, "OO"));
    obcTree.add(Restrictions.eq(Tree.PROPERTY_CLIENT, client));
    return obcTree.list().get(0);
  }

  public static TreeNode getTreeNode(Organization org, Tree tree, Client client) throws Exception {
    OBContext.setAdminMode();
    try {
      final OBCriteria<TreeNode> obcTreeNode = OBDal.getInstance().createCriteria(TreeNode.class);
      obcTreeNode.setFilterOnReadableOrganization(false);
      obcTreeNode.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      obcTreeNode.add(Restrictions.eq(TreeNode.PROPERTY_CLIENT, client));
      obcTreeNode.add(Restrictions.eq(TreeNode.PROPERTY_NODE, org.getId()));
      return obcTreeNode.list().get(0);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void updateOrgTree(Tree tree, TreeNode orgNode, Organization parentOrg)
      throws Exception {
    Long lSeqNo = 0L;
    orgNode.setReportSet(parentOrg.getId());
    OBContext.setAdminMode();
    try {
      final OBCriteria<TreeNode> obcTreeNodes = OBDal.getInstance().createCriteria(TreeNode.class);
      obcTreeNodes.setFilterOnReadableClients(false);
      obcTreeNodes.setFilterOnReadableOrganization(false);
      obcTreeNodes.add(Restrictions.eq(TreeNode.PROPERTY_REPORTSET, parentOrg.getId()));
      obcTreeNodes.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
      for (TreeNode treeNode : obcTreeNodes.list())
        if (treeNode.getSequenceNumber() > lSeqNo)
          lSeqNo = treeNode.getSequenceNumber();
      orgNode.setSequenceNumber(lSeqNo + 10L);
      OBDal.getInstance().save(orgNode);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static void updateOrgLocation(Organization org, Location location) throws Exception {
    OBContext.setAdminMode();
    try {
      location.setOrganization(getZeroOrg());
      org.getOrganizationInformationList().get(0).setLocationAddress(location);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static String getTranslatedMessage(Language language, String msgId) {
    OBContext.setAdminMode();
    try {
      Message msg = OBDal.getInstance().get(Message.class, msgId);
      OBCriteria<MessageTrl> obcMsgTrl = OBDal.getInstance().createCriteria(MessageTrl.class);
      obcMsgTrl.setFilterOnReadableClients(false);
      obcMsgTrl.setFilterOnReadableOrganization(false);
      obcMsgTrl.add(Restrictions.eq(MessageTrl.PROPERTY_MESSAGE, msg));
      obcMsgTrl.add(Restrictions.eq(MessageTrl.PROPERTY_LANGUAGE, language));
      MessageTrl trl = (MessageTrl) obcMsgTrl.uniqueResult();
      if (trl == null) {
        return msg.getMessageText();
      }
      return trl.getMessageText();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static String getTranslatedColumnName(Language language, String columnName) {
    OBContext.setAdminMode();
    try {
      OBCriteria<org.openbravo.model.ad.ui.Element> obcElement = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.ui.Element.class);
      obcElement.setFilterOnReadableClients(false);
      obcElement.setFilterOnReadableOrganization(false);
      obcElement.add(Restrictions.eq(org.openbravo.model.ad.ui.Element.PROPERTY_DBCOLUMNNAME,
          columnName));
      org.openbravo.model.ad.ui.Element element = (org.openbravo.model.ad.ui.Element) obcElement
          .uniqueResult();

      OBCriteria<ElementTrl> obcElementTrl = OBDal.getInstance().createCriteria(ElementTrl.class);
      obcElementTrl.add(Restrictions.eq(ElementTrl.PROPERTY_APPLICATIONELEMENT, element));
      obcElementTrl.add(Restrictions.eq(ElementTrl.PROPERTY_LANGUAGE, language));
      ElementTrl trl = (ElementTrl) obcElementTrl.uniqueResult();
      if (trl == null) {
        return element.getName();
      }
      return trl.getName();
    } catch (final Exception err) {
      return "Error!";
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns the tree of the provided type
   * 
   * @param strTreeTypeMenu
   *          two letters corresponding to the tree type for the menu
   * @return Tree menu element (defined at system level)
   * @throws Exception
   */
  public static Tree getTree(String strTreeTypeMenu, Client client, Organization orgProvided)
      throws Exception {
    Organization organization;
    if (orgProvided == null) {
      if ((organization = getZeroOrg()) == null)
        return null;
    } else
      organization = orgProvided;

    final OBCriteria<Tree> obcTree = OBDal.getInstance().createCriteria(Tree.class);
    obcTree.add(Restrictions.eq(Tree.PROPERTY_TYPEAREA, strTreeTypeMenu));
    obcTree.add(Restrictions.eq(Tree.PROPERTY_CLIENT, client));
    obcTree.add(Restrictions.eq(Tree.PROPERTY_ORGANIZATION, organization));
    List<Tree> lTrees = obcTree.list();
    if (lTrees.size() != 1)
      return null;
    return lTrees.get(0);
  }

}
