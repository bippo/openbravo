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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.Menu;
import org.openbravo.model.ad.ui.MenuTrl;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;

/**
 * Reads the menu from the database and caches it in memory for easy consumption by components.
 * 
 * @author mtaal
 */
@SessionScoped
public class MenuManager implements Serializable {

  private static final long serialVersionUID = 1L;

  public static enum MenuEntryType {
    Window, Process, ProcessManual, Report, Form, External, Summary, View
  };

  private MenuOption cachedMenu;
  private List<MenuOption> selectableMenuOptions;
  private String roleId;
  private List<MenuOption> menuOptions;

  public synchronized MenuOption getMenu() {
    if (cachedMenu == null || roleId == null
        || !roleId.equals(OBContext.getOBContext().getRole().getId())) {

      // set the current RoleId
      roleId = OBContext.getOBContext().getRole().getId();

      OBContext.setAdminMode();
      try {

        createInitialMenuList();

        linkWindows();
        linkProcesses();
        linkForms();

        removeInvisibleNodes();

        // set the globals
        final MenuOption localCachedRoot = new MenuOption();
        localCachedRoot.setDbId("-1"); // just use any value
        selectableMenuOptions = new ArrayList<MenuOption>();
        for (MenuOption menuOption : menuOptions) {
          if (menuOption.getParentMenuOption() == null) {
            localCachedRoot.getChildren().add(menuOption);
          }
          if (menuOption.getType() != MenuEntryType.Summary) {
            selectableMenuOptions.add(menuOption);
          }
        }

        Collections.sort(selectableMenuOptions, new MenuComparator());

        cachedMenu = localCachedRoot;
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return cachedMenu;
  }

  @SuppressWarnings("unchecked")
  private void linkMenus() {
    final String menuHql = "select m from ADMenu m left join fetch m.aDMenuTrlList where m.module.enabled=true";
    final Query menuQry = OBDal.getInstance().getSession().createQuery(menuHql);
    final Map<String, MenuOption> menuOptionsByNodeId = new HashMap<String, MenuOption>();
    for (MenuOption menuOption : menuOptions) {
      menuOptionsByNodeId.put(menuOption.getTreeNode().getNode(), menuOption);
    }
    for (Menu menu : (List<Menu>) menuQry.list()) {
      final MenuOption foundOption = menuOptionsByNodeId.get(menu.getId());
      if (menu.isActive() || menu.isSummaryLevel()) {
        if (foundOption != null) {
          foundOption.setMenu(menu);
          if (menu.getURL() != null) {
            foundOption.setType(MenuEntryType.External);
            foundOption.setId(menu.getURL());
          }
          if (menu.getObuiappView() != null && menu.getObuiappView().isActive()
              && isValidForCurrentUserRole(menu.getObuiappView())) {
            foundOption.setType(MenuEntryType.View);
            foundOption.setId(menu.getObuiappView().getName());
          }
        }
      }
    }
  }

  private boolean isValidForCurrentUserRole(OBUIAPPViewImplementation view) {
    for (ViewRoleAccess access : view.getObuiappViewRoleAccessList()) {
      final String accessRoleId = (String) DalUtil.getId(access.getRole());
      if (access.isActive() && roleId.equals(accessRoleId)) {
        return true;
      }
    }
    return false;
  }

  private void linkForms() {
    final String formsHql = "select f, amim from ADForm f, ADModelImplementation ami, ADModelImplementationMapping amim, ADFormAccess afa "
        + "where afa.role.id=:roleId and f.active = true and afa.specialForm = f and ami.specialForm=f and amim.modelObject=ami and amim.default=true";
    final Query formsQry = OBDal.getInstance().getSession().createQuery(formsHql);
    formsQry.setParameter("roleId", OBContext.getOBContext().getRole().getId());
    // force a load
    final List<?> list = formsQry.list();

    final Map<String, MenuOption> menuOptionsByFormId = new HashMap<String, MenuOption>();
    for (MenuOption menuOption : menuOptions) {
      if (menuOption.getMenu() != null && menuOption.getMenu().getSpecialForm() != null) {
        // allow access if not running in a webcontainer as then the config file can not be checked
        boolean hasAccess = !SessionFactoryController.isRunningInWebContainer()
            || ActivationKey.getInstance().hasLicenseAccess("X",
                menuOption.getMenu().getSpecialForm().getId()) == FeatureRestriction.NO_RESTRICTION;
        if (hasAccess) {
          menuOptionsByFormId.put(menuOption.getMenu().getSpecialForm().getId(), menuOption);
        }
      }
    }

    for (Object object : list) {
      final Object[] values = (Object[]) object;
      final Form form = (Form) values[0];
      final ModelImplementationMapping mim = (ModelImplementationMapping) values[1];
      final MenuOption menuOption = menuOptionsByFormId.get(form.getId());
      if (menuOption != null) {
        menuOption.setType(MenuEntryType.Form);
        menuOption.setId(mim.getMappingName());
        menuOption.setForm(form);
      }
    }
  }

  private void linkProcesses() {

    // collect the valid tabs/windows
    final String allowedProcessHql = "select p from ADProcess p, ADProcessAccess apa "
        + "where apa.role.id=:roleId and p.active = true and apa.process = p";
    final Query allowedProcessQry = OBDal.getInstance().getSession().createQuery(allowedProcessHql);
    allowedProcessQry.setParameter("roleId", OBContext.getOBContext().getRole().getId());
    final Map<String, org.openbravo.model.ad.ui.Process> allowedProcesses = new HashMap<String, org.openbravo.model.ad.ui.Process>();
    for (Object processObj : allowedProcessQry.list()) {
      // allow access if not running in a web container
      boolean hasAccess = !SessionFactoryController.isRunningInWebContainer()
          || ActivationKey.getInstance().hasLicenseAccess("P",
              ((org.openbravo.model.ad.ui.Process) processObj).getId()) == FeatureRestriction.NO_RESTRICTION;
      if (hasAccess) {
        allowedProcesses.put(((org.openbravo.model.ad.ui.Process) processObj).getId(),
            (org.openbravo.model.ad.ui.Process) processObj);
      }
    }

    final String processHql = "select p, amim from ADProcess p, ADModelImplementation ami, ADModelImplementationMapping amim, ADProcessAccess apa "
        + "where apa.role.id=:roleId and p.active = true and apa.process = p and ami.process=p and ami.default=true and amim.modelObject=ami and amim.default=true";
    final Query processQry = OBDal.getInstance().getSession().createQuery(processHql);
    processQry.setParameter("roleId", OBContext.getOBContext().getRole().getId());
    // force a load
    final List<?> list = processQry.list();

    final Map<String, MenuOption> menuOptionsByProcessId = new HashMap<String, MenuOption>();
    for (MenuOption menuOption : menuOptions) {
      if (menuOption.getMenu() != null && menuOption.getMenu().getProcess() != null
          && allowedProcesses.containsKey(menuOption.getMenu().getProcess().getId())) {
        menuOptionsByProcessId.put(menuOption.getMenu().getProcess().getId(), menuOption);
      }
    }

    for (Object object : list) {
      final Object[] values = (Object[]) object;
      final org.openbravo.model.ad.ui.Process process = (org.openbravo.model.ad.ui.Process) values[0];
      final ModelImplementationMapping mim = (ModelImplementationMapping) values[1];
      final MenuOption menuOption = menuOptionsByProcessId.get(process.getId());
      if (menuOption != null) {
        if (process.getUIPattern().equals("Standard")) {
          menuOption.setType(MenuEntryType.Process);
        } else if (process.isReport() || process.isJasperReport()) {
          menuOption.setType(MenuEntryType.Report);
          menuOption.setReport(true);
        } else {
          menuOption.setType(MenuEntryType.ProcessManual);
        }
        menuOption.setId(mim.getMappingName());
      }
    }

    // note this logic is based on the VerticalMenu.getUrlString method:
    // } else if (action.equals("P")) {
    // if (isExternalService.equals("Y") && externalType.equals("PS"))
    // strResultado.append("/utility/OpenPentaho.html?inpadProcessId=").append(adProcessId);
    // else {
    // try {
    // if (MenuData.isGenericJavaProcess(this, adProcessId))
    // strResultado.append(
    // "/ad_actionButton/ActionButtonJava_Responser.html?inpadProcessId=").append(
    // adProcessId);
    // else
    // strResultado.append("/ad_actionButton/ActionButton_Responser.html?inpadProcessId=")
    // .append(adProcessId);
    // } catch (final Exception e) {
    // e.printStackTrace();
    // strResultado.append("/ad_actionButton/ActionButton_Responser.html?inpadProcessId=")
    // .append(adProcessId);
    // }
    // }

    for (String processId : menuOptionsByProcessId.keySet()) {
      final MenuOption menuOption = menuOptionsByProcessId.get(processId);
      final org.openbravo.model.ad.ui.Process process = allowedProcesses.get(processId);

      if (menuOption.getId() == null && menuOption.getMenu() != null
          && menuOption.getMenu().getAction().equals("P")) {
        if (process.isExternalService() != null && process.isExternalService()
            && "PS".equals(process.getServiceType())) {
          menuOption.setType(MenuEntryType.Process);
          menuOption.setId("/utility/OpenPentaho.html?inpadProcessId=" + processId);
        } else if ("S".equals(process.getUIPattern()) && !process.isJasperReport()
            && process.getProcedure() == null) {
          // see the MenuData.isGenericJavaProcess method
          menuOption.setType(MenuEntryType.Process);
          menuOption.setId("/ad_actionButton/ActionButtonJava_Responser.html");
        } else {
          menuOption.setType(MenuEntryType.Process);
          menuOption.setId("/ad_actionButton/ActionButton_Responser.html");
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void linkWindows() {
    // collect the valid tabs/windows
    final String tabsHql = "select t from ADTab t join fetch t.window w join fetch t.table, ADWindowAccess awa "
        + "where awa.role.id=:roleId and t.active = true and w.active = true and w = awa.window and t.tabLevel = 0";
    final Query tabsQry = OBDal.getInstance().getSession().createQuery(tabsHql);
    tabsQry.setParameter("roleId", OBContext.getOBContext().getRole().getId());
    // force a load
    final List<?> list = tabsQry.list();

    final Map<String, List<MenuOption>> menuOptionsByWindowId = new HashMap<String, List<MenuOption>>();
    for (MenuOption menuOption : menuOptions) {
      if (menuOption.getMenu() != null && menuOption.getMenu().getWindow() != null) {
        // allow access if not running in a web container
        boolean hasAccess = !SessionFactoryController.isRunningInWebContainer()
            || ActivationKey.getInstance().hasLicenseAccess("MW",
                menuOption.getMenu().getWindow().getId()) == FeatureRestriction.NO_RESTRICTION;
        if (hasAccess) {
          final String windowId = menuOption.getMenu().getWindow().getId();
          if (menuOptionsByWindowId.containsKey(windowId)) {
            menuOptionsByWindowId.get(windowId).add(menuOption);
          } else {
            List<MenuOption> option = new ArrayList<MenuOption>();
            option.add(menuOption);
            menuOptionsByWindowId.put(windowId, option);
          }
        }
        // make sure that the important parts are read into mem
        for (Tab windowTab : menuOption.getMenu().getWindow().getADTabList()) {
          Hibernate.initialize(windowTab);
          Hibernate.initialize(windowTab.getTable());
        }
      }
    }

    for (Tab tab : (List<Tab>) list) {
      final Window window = tab.getWindow();
      final List<MenuOption> options = menuOptionsByWindowId.get(window.getId());
      if (options != null) {
        for (MenuOption menuOption : options) {
          menuOption.setType(MenuEntryType.Window);
          menuOption.setId(tab.getId());
          menuOption.setTab(tab);
        }
      }
    }
  }

  private void removeInvisibleNodes() {

    final List<MenuOption> toRemove = new ArrayList<MenuOption>();
    for (MenuOption menuOption : menuOptions) {
      if (!menuOption.isVisible()) {
        toRemove.add(menuOption);
      }
    }
    for (MenuOption menuOption : toRemove) {
      if (menuOption.getParentMenuOption() != null) {
        menuOption.getParentMenuOption().getChildren().remove(menuOption);
      }
    }
    menuOptions.removeAll(toRemove);
  }

  private void createInitialMenuList() {
    Role role = OBDal.getInstance().get(Role.class, roleId);
    final Tree tree;
    if (role.getPrimaryTreeMenu() != null) {
      tree = role.getPrimaryTreeMenu();
    } else {
      tree = OBDal.getInstance().get(Tree.class, "10");
    }
    menuOptions = new ArrayList<MenuOption>();
    OBCriteria<TreeNode> treeNodes = OBDal.getInstance().createCriteria(TreeNode.class);
    treeNodes.add(Restrictions.eq(TreeNode.PROPERTY_TREE, tree));
    treeNodes.setFilterOnActive(false);

    final Map<String, MenuOption> menuOptionsByMenuId = new HashMap<String, MenuOption>();
    for (TreeNode treeNode : treeNodes.list()) {
      boolean addOption = treeNode.isActive();
      boolean inactiveSummary = false;

      if (!addOption) {
        Menu menuEntry = OBDal.getInstance().get(Menu.class, treeNode.getNode());
        if (menuEntry != null) {
          addOption = menuEntry.isSummaryLevel();
          inactiveSummary = true;
        }
      }

      if (addOption) {
        final MenuOption menuOption = new MenuOption();
        menuOption.setTreeNode(treeNode);
        menuOption.setDbId(treeNode.getId());
        Menu menuEntry = OBDal.getInstance().get(Menu.class, treeNode.getNode());
        if (menuEntry != null && !menuEntry.isActive()) {
          menuOption.setVisible(false);
        }
        menuOptions.add(menuOption);
      }
    }
    linkMenus();

    for (MenuOption menuOption : menuOptions) {
      if (menuOption.getMenu() != null) {
        menuOptionsByMenuId.put(menuOption.getMenu().getId(), menuOption);
      }
    }

    // sort them by sequencenumber of the treenode
    Collections.sort(menuOptions, new MenuSequenceComparator());

    // now put the menuOptions in a tree structure
    for (MenuOption menuOption : menuOptions) {
      menuOption.setParentMenuOption(menuOptionsByMenuId);
    }
  }

  public static class MenuOption implements Serializable {
    private static final long serialVersionUID = 1L;
    private TreeNode treeNode;
    private String label;
    private MenuEntryType type = MenuEntryType.Summary;
    private String id;
    private String dbId;
    private Menu menu;
    private Tab tab;
    private Form form;
    private boolean isReport;
    private MenuOption parentMenuOption;
    private List<MenuOption> children = new ArrayList<MenuOption>();
    private Boolean visible = null;
    private boolean showInClassicMode = false;

    public boolean isSingleRecord() {
      return getTab() != null && getTab().getUIPattern().equals("SR");
    }

    public String getSingleRecordStringValue() {
      return Boolean.toString(isSingleRecord());
    }

    public boolean isReadOnly() {
      return getTab() != null && getTab().getUIPattern().equals("RO");
    }

    public String getReadOnlyStringValue() {
      return Boolean.toString(isReadOnly());
    }

    public boolean isReport() {
      return isReport;
    }

    public void setReport(boolean isReport) {
      this.isReport = isReport;
    }

    public boolean isVisible() {
      if (visible != null) {
        if (!visible && type == MenuEntryType.Summary) {
          for (MenuOption menuOption : children) {
            menuOption.visible = false;
          }
        }
        return visible;
      }

      if (menu == null) {
        visible = false;
      } else if (!children.isEmpty()) {
        boolean localVisible = false;
        for (MenuOption menuOption : children) {
          localVisible |= menuOption.isVisible();
        }
        visible = localVisible;
      } else if (type == MenuEntryType.Summary) {
        visible = false;
      } else {
        visible = true;
      }
      return visible;
    }

    public void setVisible(Boolean visible) {
      this.visible = visible;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public MenuEntryType getType() {
      return type;
    }

    public void setType(MenuEntryType type) {
      if (id != null && id.toLowerCase().startsWith("http")) {
        this.type = MenuEntryType.External;
      } else {
        this.type = type;
      }
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      if (id.toLowerCase().startsWith("http")) {
        setType(MenuEntryType.External);
      }
      this.id = id;
    }

    public List<MenuOption> getChildren() {
      return children;
    }

    public void setChildren(List<MenuOption> children) {
      this.children = children;
    }

    public TreeNode getTreeNode() {
      return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
      this.treeNode = treeNode;
    }

    public MenuOption getParentMenuOption() {
      return parentMenuOption;
    }

    public String getFormId() {
      return form.getId();
    }

    public void setForm(Form form) {
      this.form = form;
    }

    public void setParentMenuOption(Map<String, MenuOption> menuOptionsByMenuId) {
      if (treeNode.getReportSet() != null) {
        parentMenuOption = menuOptionsByMenuId.get(treeNode.getReportSet());
        if (parentMenuOption != null) {
          parentMenuOption.getChildren().add(this);
        }
      }
    }

    public Menu getMenu() {
      return menu;
    }

    public void setMenu(Menu menu) {
      this.menu = menu;
      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      for (MenuTrl menuTrl : menu.getADMenuTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(menuTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          setLabel(menuTrl.getName());
        }
      }
      if (getLabel() == null) {
        setLabel(menu.getName());
      }

      // initialize some collections
      Hibernate.initialize(menu.getADMenuTrlList());
      Hibernate.initialize(menu.getOBUIAPPMenuParametersList());
    }

    public boolean isWindow() {
      return getType().equals(MenuEntryType.Window);
    }

    public boolean isProcess() {
      return getType().equals(MenuEntryType.Process);
    }

    public boolean isModal() {
      if (isProcess()) {
        // done via isModelProcess(String) as is called from different request and getProcess() is
        // not initialized
        String processId = (String) DalUtil.getId(getMenu().getProcess());
        return Utility.isModalProcess(processId);
      }
      return true;
    }

    public boolean isProcessManual() {
      return getType().equals(MenuEntryType.ProcessManual);
    }

    public boolean isView() {
      return getType().equals(MenuEntryType.View);
    }

    public boolean isForm() {
      return getType().equals(MenuEntryType.Form);
    }

    public boolean isExternal() {
      return getType().equals(MenuEntryType.External);
    }

    public Tab getTab() {
      return tab;
    }

    public void setTab(Tab tab) {
      this.tab = tab;
      showInClassicMode = ApplicationUtils.showWindowInClassicMode(tab.getWindow());
    }

    public List<MenuParameter> getParameters() {
      final List<MenuParameter> parameters = new ArrayList<MenuParameter>();
      for (MenuParameter menuParameter : getMenu().getOBUIAPPMenuParametersList()) {
        if (menuParameter.isActive() && menuParameter.getParameterValue() != null) {
          parameters.add(menuParameter);
        }
      }
      return parameters;
    }

    public boolean isShowInClassicMode() {
      return showInClassicMode;
    }

    public String getDbId() {
      return dbId;
    }

    public void setDbId(String dbId) {
      this.dbId = dbId;
    }
  }

  private static class MenuComparator implements Comparator<MenuOption> {

    @Override
    public int compare(MenuOption o1, MenuOption o2) {
      return o1.getLabel().compareTo(o2.getLabel());
    }

  }

  private static class MenuSequenceComparator implements Comparator<MenuOption> {

    @Override
    public int compare(MenuOption o1, MenuOption o2) {
      TreeNode tn1 = o1.getTreeNode();
      TreeNode tn2 = o2.getTreeNode();
      return (int) (tn1.getSequenceNumber() - tn2.getSequenceNumber());
    }

  }

  public List<MenuOption> getSelectableMenuOptions() {
    // initialize
    getMenu();

    return selectableMenuOptions;
  }
}
