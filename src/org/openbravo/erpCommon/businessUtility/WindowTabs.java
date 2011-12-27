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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.erpCommon.utility.Utility;

/**
 * @author Fernando
 * 
 *         Class in charge of building the application's tabs for each window type.
 */
public class WindowTabs {
  static Logger log4j = Logger.getLogger(WindowTabs.class);
  private VariablesSecureApp vars;
  private ConnectionProvider conn;
  private String className = "";
  private String TabID = "";
  private String WindowID = "";
  private String Title = "";
  private String ID = "";
  private String action = "";
  private int level = 0;
  boolean editView = true;
  boolean newRecord = false;
  private Hashtable<String, Stack<WindowTabsData>> tabs = new Hashtable<String, Stack<WindowTabsData>>();
  private Stack<WindowTabsData> breadcrumb = new Stack<WindowTabsData>();

  /**
   * Constructor Used by WAD windows.
   * 
   * @param _conn
   *          Object with the database connection methods.
   * @param _vars
   *          Object with the session information.
   * @param _tabId
   *          String with the id of the tab.
   * @param _windowId
   *          String with the id of the window.
   * @throws Exception
   */
  public WindowTabs(ConnectionProvider _conn, VariablesSecureApp _vars, String _tabId,
      String _windowId, boolean _editView) throws Exception {
    this(_conn, _vars, _tabId, _windowId);
    this.editView = _editView;
  }

  public WindowTabs(ConnectionProvider _conn, VariablesSecureApp _vars, String _tabId,
      String _windowId, boolean _editView, boolean _newRecord) throws Exception {
    this(_conn, _vars, _tabId, _windowId);
    this.editView = _editView;
    this.newRecord = _newRecord;
  }

  public WindowTabs(ConnectionProvider _conn, VariablesSecureApp _vars, String _tabId,
      String _windowId) throws Exception {
    if (_conn == null || _vars == null || _tabId == null || _tabId.equals("") || _windowId == null
        || _windowId.equals(""))
      throw new Exception("Missing parameters");
    this.conn = _conn;
    this.vars = _vars;
    this.TabID = _tabId;
    this.WindowID = _windowId;
    this.action = "W";
    this.Title = WindowTabsData.selectWindowInfo(this.conn, this.vars.getLanguage(), this.WindowID);
    getTabs();
  }

  /**
   * Constructor Used by manual windows.
   * 
   * @param _conn
   *          Object with the database connection methods.
   * @param _vars
   *          Object with the session information.
   * @param _className
   *          String with the form's classname.
   * @throws Exception
   */
  public WindowTabs(ConnectionProvider _conn, VariablesSecureApp _vars, String _className)
      throws Exception {
    if (_conn == null || _vars == null || _className == null || _className.equals(""))
      throw new Exception("Missing parameters");
    this.conn = _conn;
    this.vars = _vars;
    this.className = _className;
    getWindowInfo();
    getTabs();
  }

  /**
   * Constructor Used by processes without javaclass.
   * 
   * @param _conn
   *          Object with the database connection methods.
   * @param _vars
   *          Object with the session information.
   * @param adProcessId
   *          the id of the process.
   * @throws Exception
   */
  public WindowTabs(ConnectionProvider _conn, VariablesSecureApp _vars, int adProcessId)
      throws Exception {
    if (_conn == null || _vars == null)
      throw new Exception("Missing parameters");
    this.conn = _conn;
    this.vars = _vars;
    this.className = "";
    getProcessInfo(new Integer(adProcessId).toString());
    getTabs();
  }

  /**
   * Obtains the information for the process (For processes not in menu)
   * 
   * @throws Exception
   */
  private void getProcessInfo(String adProcessId) throws Exception {
    WindowTabsData[] processInfo = WindowTabsData.selectProcessInfo(this.conn, adProcessId);
    if (processInfo == null || processInfo.length == 0) {
      log4j.warn("Error while trying to obtain window info for process: " + adProcessId);
      return;
    }
    this.TabID = processInfo[0].adTabId;
    this.action = processInfo[0].action;
    this.WindowID = processInfo[0].tabname;

    processInfo = WindowTabsData.selectMenuInfo(this.conn, this.vars.getLanguage(), this.action,
        this.TabID);
    if (processInfo == null || processInfo.length == 0) {
      log4j.warn("Error while trying to obtain window info for process: " + adProcessId);
      return;
    }
    this.ID = processInfo[0].id;
    this.Title = processInfo[0].name;
  }

  /**
   * Obtains all the window information from database. (For manual windows)
   * 
   * @throws Exception
   */
  private void getWindowInfo() throws Exception {
    WindowTabsData[] windowInfo = WindowTabsData.selectJavaInfo(this.conn, this.className);
    if (windowInfo == null || windowInfo.length == 0) {
      log4j.warn("Error while trying to obtain window info for class: " + this.className);
      return;
    }
    this.TabID = windowInfo[0].adTabId;
    this.action = windowInfo[0].action;
    this.WindowID = windowInfo[0].tabname;
    windowInfo = WindowTabsData.selectMenuInfo(this.conn, this.vars.getLanguage(), this.action,
        this.TabID);
    if (windowInfo == null || windowInfo.length == 0) {
      log4j.warn("Error while trying to obtain window info for class: " + this.className);
      return;
    }
    this.ID = windowInfo[0].id;
    this.Title = windowInfo[0].name;
  }

  /**
   * Gets the menu root element for the selected window.
   * 
   * @return String with the menu root element.
   * @throws Exception
   */
  private String getMenuInfo() throws Exception {
    if (this.action.equals("W"))
      return WindowTabsData.selectMenu(this.conn, this.vars.getLanguage(), this.WindowID);
    else
      return WindowTabsData.selectMenuManual(this.conn, this.vars.getLanguage(), this.ID);
  }

  /**
   * Build the internal structure of all the tabs defined for the window.
   * 
   * @throws Exception
   */
  private void getTabs() throws Exception {
    WindowTabsData[] tabsAux = null;
    if (this.action.equals("W"))
      tabsAux = WindowTabsData.select(this.conn, this.vars.getLanguage(), this.WindowID);
    else
      tabsAux = WindowTabsData
          .selectManual(this.conn, this.TabID, this.vars.getLanguage(), this.ID);
    int pos = -1;
    if (tabsAux == null || tabsAux.length == 0) {
      log4j.warn("Error while trying to obtain tabs for id: " + this.TabID);
      return;
    }
    for (int i = 0; i < tabsAux.length; i++) {
      if (tabsAux[i].adTabId.equals(this.TabID)) {
        pos = i;
        this.level = Integer.valueOf(tabsAux[i].tablevel).intValue();
        break;
      }
    }
    if (pos == -1)
      throw new Exception("Error while trying to locate the tab: " + this.TabID);
    if (pos < tabsAux.length - 1) {
      if (Integer.valueOf(tabsAux[pos + 1].tablevel).intValue() > this.level)
        getTabsByLevel(tabsAux, pos + 1, false);
      else
        getTabsByLevel(tabsAux, pos, true);
    } else
      getTabsByLevel(tabsAux, pos, true);
  }

  /**
   * Used by the getTabs() method to build the internal structure.
   * 
   * @param tabsAux
   *          Array with the tabs.
   * @param pos
   *          Integer with the actual position in the array.
   * @param register
   *          Boolean to indicates if the actual position must be saved in the breadcrumb.
   * @throws Exception
   */
  private void getTabsByLevel(WindowTabsData[] tabsAux, int pos, boolean register) throws Exception {
    if (register) {
      tabsAux[pos].isbreadcrumb = "Y";
      this.breadcrumb.push(tabsAux[pos]);
    }
    final Stack<WindowTabsData> aux = new Stack<WindowTabsData>();
    aux.push(tabsAux[pos]);
    final String actualLevel = tabsAux[pos].tablevel;
    for (int i = pos + 1; i < tabsAux.length; i++) {
      if (tabsAux[pos].tablevel.equals(tabsAux[i].tablevel))
        aux.push(tabsAux[i]);
      else if (Integer.valueOf(tabsAux[pos].tablevel).intValue() > Integer.valueOf(
          tabsAux[i].tablevel).intValue())
        break;
    }
    int nextPos = -1;
    final Stack<WindowTabsData> result = new Stack<WindowTabsData>();
    while (!aux.empty())
      result.push(aux.pop());
    for (int i = pos - 1; i >= 0; i--) {
      if (tabsAux[pos].tablevel.equals(tabsAux[i].tablevel))
        result.push(tabsAux[i]);
      else if (Integer.valueOf(tabsAux[pos].tablevel).intValue() > Integer.valueOf(
          tabsAux[i].tablevel).intValue()) {
        nextPos = i;
        break;
      }
    }
    if (result.empty())
      throw new Exception("Level missed for tab: " + this.TabID + " in level: "
          + tabsAux[pos].tablevel);
    this.tabs.put(actualLevel, result);
    if (nextPos != -1)
      getTabsByLevel(tabsAux, nextPos, true);
  }

  /**
   * Method to get the parent's tabs of the actual (If exists).
   * 
   * @return String with the HTML text for the tabs.
   */
  public String parentTabs() {
    final StringBuffer text = new StringBuffer();
    if (this.tabs == null)
      return text.toString();
    String strShowAcct = "N";
    String strShowTrl = "N";
    try {
      strShowAcct = Utility.getContext(this.conn, this.vars, "#ShowAcct", this.WindowID);
      strShowTrl = Utility.getContext(this.conn, this.vars, "#ShowTrl", this.WindowID);
    } catch (final Exception ex) {
      ex.printStackTrace();
      log4j.error(ex);
    }
    boolean isFirst = true;
    final boolean hasParent = (this.level > 0);
    if (!hasParent)
      return text.append("<td class=\"tabBackGroundInit\"></td>").toString();
    for (int i = 0; i < this.level; i++) {
      final Stack<WindowTabsData> aux = this.tabs.get(Integer.toString(i));
      if (aux == null)
        continue;
      if (!isFirst)
        text.append("</tr>\n<tr>\n");

      if (isFirst)
        text.append("<td class=\"tabBackGroundInit\">\n");
      if (isFirst)
        text.append("  <div>\n");
      if (isFirst)
        text.append("  <span class=\"tabTitle\">\n");
      if (isFirst)
        text.append("    <div class=\"tabTitle_background\">\n");
      if (isFirst)
        text.append("      <span class=\"tabTitle_elements_container\">\n");
      if (isFirst)
        text.append("        <span class=\"tabTitle_elements_text\" id=\"tabTitle_text\">")
            .append(this.Title).append("</span>\n");
      if (isFirst)
        text.append("        <span class=\"tabTitle_elements_separator\"><div class=\"tabTitle_elements_separator_icon\"></div></span>\n");
      if (isFirst)
        text.append("        <span class=\"tabTitle_elements_image\"><div class=\"tabTitle_elements_image_normal_icon\" id=\"TabStatusIcon\"></div></span>\n");
      if (isFirst)
        text.append("      </span>\n");
      if (isFirst)
        text.append("    </div>\n");
      if (isFirst)
        text.append("  </span>\n");
      if (isFirst)
        text.append("</div>\n");
      if (isFirst)
        text.append("</td></tr><tr><td class=\"tabBackGround\">");

      else
        text.append("<td class=\"tabBackGround\">");
      if (isFirst)
        text.append("  <div class=\"marginLeft\">\n");
      else {
        text.append("  <table class=\"tabTable\"><tr>\n");
        text.append("    <td valign=\"top\"><span class=\"tabLeft\">&nbsp;</span></td>\n");
        text.append("    <td class=\"tabPaneBackGround\">\n");
      }
      boolean isFirstTab = true;
      if (this.action.equals("W")) {
        while (!aux.empty()) {
          final WindowTabsData data = aux.pop();
          if (!data.adTabId.equals(this.TabID) && strShowAcct.equals("N")
              && data.isinfotab.equals("Y"))
            continue;
          else if (!data.adTabId.equals(this.TabID) && strShowTrl.equals("N")
              && data.istranslationtab.equals("Y"))
            continue;
          else if (data.isactive.equals("N")
              || data.enabled.equals("N")
              || ActivationKey.getInstance().hasLicencesTabAccess(data.adTabId) != FeatureRestriction.NO_RESTRICTION)
            continue;
          if (!isFirstTab)
            text.append("<span class=\"tabSeparator\">&nbsp;</span>\n");
          text.append(
              (isFirstTab && !((data.adTabId.equals(this.TabID) || data.isbreadcrumb.equals("Y")))) ? "<div class=\"marginLeft1\">"
                  : "<div>")
              .append("<span class=\"dojoTab")
              .append(
                  ((data.adTabId.equals(this.TabID) || data.isbreadcrumb.equals("Y")) ? (isFirst ? "NULL dojoTabparentfirst"
                      : " dojoTabparent")
                      : "")).append("\">");
          text.append("<div><span><a class=\"dojoTabLink\" href=\"#\" onclick=\"");
          text.append(getUrlCommand(data.adTabId, data.name, Integer.valueOf(data.tablevel)
              .intValue()));
          text.append(
              "\" onMouseOver=\"return true;\" onMouseOut=\"return true;\" id=\"" + data.tabnameid
                  + "\">").append(data.tabname).append("</a></span></div></span>\n");
          isFirstTab = false;
        }
        text.append("  </div>\n");
      }
      if (isFirst)
        text.append("  </div>\n");
      else {
        text.append("    <td valign=\"top\"><span class=\"tabRight\">&nbsp;</span></td>\n");
        text.append("  </tr></table>\n");
      }
      text.append("</td>\n");
      isFirst = false;
    }
    return text.toString();
  }

  /**
   * Method to get the tabs of the same level as the actual.
   * 
   * @return String with the HTML of the tabs.
   */
  public String mainTabs() {
    final StringBuffer text = new StringBuffer();
    if (this.tabs == null)
      return text.toString();
    String strShowAcct = "N";
    String strShowTrl = "N";
    try {
      strShowAcct = Utility.getContext(this.conn, this.vars, "#ShowAcct", this.WindowID);
      strShowTrl = Utility.getContext(this.conn, this.vars, "#ShowTrl", this.WindowID);
    } catch (final Exception ex) {
      ex.printStackTrace();
      log4j.error(ex);
    }
    final boolean hasParent = (this.level > 0);
    final Stack<WindowTabsData> aux = this.tabs.get(Integer.toString(this.level));
    if (aux == null)
      return text.toString();

    if (!hasParent)
      text.append("<td class=\"tabBackGroundInit\">\n");
    if (!hasParent)
      text.append("  <div>\n");
    if (!hasParent)
      text.append("  <span class=\"tabTitle\">\n");
    if (!hasParent)
      text.append("    <div class=\"tabTitle_background\">\n");
    if (!hasParent)
      text.append("      <span class=\"tabTitle_elements_container\">\n");
    if (!hasParent)
      text.append("        <span class=\"tabTitle_elements_text\" id=\"tabTitle_text\">")
          .append(this.Title).append("</span>\n");
    if (!hasParent)
      text.append("        <span class=\"tabTitle_elements_separator\"><div class=\"tabTitle_elements_separator_icon\"></div></span>\n");
    if (!hasParent)
      text.append("        <span class=\"tabTitle_elements_image\"><div class=\"tabTitle_elements_image_normal_icon\" id=\"TabStatusIcon\"></div></span>\n");
    if (!hasParent)
      text.append("      </span>\n");
    if (!hasParent)
      text.append("    </div>\n");
    if (!hasParent)
      text.append("  </span>\n");
    if (!hasParent)
      text.append("</div>\n");
    if (!hasParent)
      text.append("</td></tr><tr>");
    text.append("<td class=\"tabBackGround\">\n");
    if (!hasParent)
      text.append("  <div class=\"marginLeft\">\n");
    else {
      text.append("  <table class=\"tabTable\"><tr>\n");
      text.append("    <td valign=\"top\"><span class=\"tabLeft\">&nbsp;</span></td>\n");
      text.append("    <td class=\"tabPaneBackGround\">\n");
    }
    boolean isFirstTab = true;
    if (this.action.equals("W")) {
      while (!aux.empty()) {
        final WindowTabsData data = aux.pop();
        if (!data.adTabId.equals(this.TabID) && strShowAcct.equals("N")
            && data.isinfotab.equals("Y"))
          continue;
        else if (!data.adTabId.equals(this.TabID) && strShowTrl.equals("N")
            && data.istranslationtab.equals("Y"))
          continue;
        else if (data.isactive.equals("N")
            || data.enabled.equals("N")
            || ActivationKey.getInstance().hasLicencesTabAccess(data.adTabId) != FeatureRestriction.NO_RESTRICTION)
          continue;
        if (!isFirstTab)
          text.append("<span class=\"tabSeparator\">&nbsp;</span>\n");
        text.append(
            (isFirstTab && !((data.adTabId.equals(this.TabID) || data.isbreadcrumb.equals("Y")))) ? "<div class=\"marginLeft1\">"
                : "<div>")
            .append("<span class=\"")
            .append(
                ((data.adTabId.equals(this.TabID) || data.isbreadcrumb.equals("Y")) ? (!hasParent ? "dojoTabcurrentfirst"
                    : "dojoTabcurrent")
                    : "dojoTab")).append("\">");
        text.append("<div><span><a class=\"dojoTabLink\" href=\"#\" onclick=\"");
        text.append(getUrlCommand(data.adTabId, data.name, Integer.valueOf(data.tablevel)
            .intValue()));
        text.append(
            "\" onMouseOver=\"return true;\" onMouseOut=\"return true;\" id=\"" + data.tabnameid
                + "\">").append(data.tabname).append("</a></span></div></span>\n");
        isFirstTab = false;
      }
      text.append("  </div>\n");
    }

    if (!hasParent)
      text.append("  </div>\n");
    else {
      text.append("    <td valign=\"top\"><span class=\"tabRight\">&nbsp;</span></td>\n");
      text.append("  </tr></table>\n");
    }
    text.append("</td>\n");
    return text.toString();
  }

  /**
   * Method to get the child tabs from the actual.
   * 
   * @return String with the HTML of the tabs.
   */
  public String childTabs(boolean readOnly) {
    final StringBuffer text = new StringBuffer();
    if (this.tabs == null)
      return text.append("<td class=\"tabTabbarBackGround\"></td>").toString();
    String strShowAcct = "N";
    String strShowTrl = "N";
    try {
      strShowAcct = Utility.getContext(this.conn, this.vars, "#ShowAcct", this.WindowID);
      strShowTrl = Utility.getContext(this.conn, this.vars, "#ShowTrl", this.WindowID);
    } catch (final Exception ex) {
      ex.printStackTrace();
      log4j.error(ex);
    }
    final Stack<WindowTabsData> aux = this.tabs.get(Integer.toString(this.level + 1));
    if (aux == null)
      return text.append("<td class=\"tabTabbarBackGround\"></td>").toString();
    text.append("<td class=\"tabBackGround\">\n");
    text.append("  <table class=\"tabTable\"><tr>\n");
    text.append("    <td valign=\"top\"><span class=\"tabLeft\">&nbsp;</span></td>\n");
    text.append("    <td class=\"tabPaneBackGround\"><div class=\"marginLeft1\">\n");
    boolean isFirst = true;
    while (!aux.empty()) {
      final WindowTabsData data = aux.pop();
      if (!data.adTabId.equals(this.TabID) && strShowAcct.equals("N") && data.isinfotab.equals("Y"))
        continue;
      else if (!data.adTabId.equals(this.TabID) && strShowTrl.equals("N")
          && data.istranslationtab.equals("Y"))
        continue;
      else if (data.isactive.equals("N")
          || data.enabled.equals("N")
          || ActivationKey.getInstance().hasLicencesTabAccess(data.adTabId) != FeatureRestriction.NO_RESTRICTION)
        continue;
      if (!isFirst)
        text.append("<span class=\"tabSeparator\">&nbsp;</span>\n");
      isFirst = false;
      text.append("<span class=\"dojoTab")
          .append(
              ((data.adTabId.equals(this.TabID) || data.isbreadcrumb.equals("Y")) ? " current" : ""))
          .append("\">");
      text.append("<div><a class=\"dojoTabLink\" href=\"#\" onclick=\"");
      if (readOnly)
        text.append("return false;");
      else
        text.append(getUrlCommand(data.adTabId, data.name, Integer.valueOf(data.tablevel)
            .intValue()));
      text.append(
          "\" onMouseOver=\"return true;\" onMouseOut=\"return true;\"  id=\"" + data.tabnameid
              + "\">").append(data.tabname).append("</a></div></span>\n");
    }
    text.append("    </div></td><td valign=\"top\"><span class=\"tabRight\">&nbsp;</span></td>\n");
    text.append("  </tr></table>\n");
    text.append("</td>\n");
    return text.toString();
  }

  public String childTabs() {
    return childTabs(false);
  }

  /*
   * public String childTabs() { StringBuffer text = new StringBuffer(); if (this.tabs==null) return
   * text.toString(); String strShowAcct = "N"; String strShowTrl = "N"; try { strShowAcct =
   * Utility.getContext(this.conn, this.vars, "#ShowAcct", this.WindowID); strShowTrl =
   * Utility.getContext(this.conn, this.vars, "#ShowTrl", this.WindowID); } catch (Exception ex) {
   * ex.printStackTrace(); log4j.error(ex); } Stack<WindowTabsData> aux = (Stack<WindowTabsData>)
   * this.tabs.get(Integer.toString(this.level+1)); if (aux==null) return
   * text.toString();text.append(
   * "<td class=\"tabPaneBackGround dojoTabContainerChild dojoTabLabels-bottom\">\n" );
   * text.append("<div>\n"); while (!aux.empty()) { WindowTabsData data = (WindowTabsData)
   * aux.pop(); if (!data.adTabId.equals(this.TabID) && strShowAcct.equals("N") &&
   * data.isinfotab.equals("Y")) continue; else if (!data.adTabId.equals(this.TabID) &&
   * strShowTrl.equals("N") && data.istranslationtab.equals("Y")) continue;
   * text.append("<span class=\"dojoTab" ).append(((data.adTabId.equals(this.TabID) ||
   * data.isbreadcrumb.equals("Y"))?" current":"")).append("\">");
   * text.append("<div><a class=\"dojoTabLink\" href=\"#\" onclick=\"");
   * text.append(getUrlCommand(data.adTabId, data.name, Integer.valueOf(data.tablevel).intValue()));
   * text.append("\" onMouseOver=\"return true;\" onMouseOut=\"return true;\">"
   * ).append(data.tabname).append("</a></div></span>\n"); } text.append("</div>\n");
   * text.append("</td>\n"); return text.toString(); }
   */

  /**
   * Method to obtain the breadcrumb for this tab.
   * 
   * @return String with the HTML of the breadcrumb.
   */
  public String breadcrumb() {
    final StringBuffer text = new StringBuffer();
    if (this.breadcrumb == null || this.breadcrumb.empty())
      return text.toString();
    boolean isFirst = true;
    try {
      text.append("<span>").append(getMenuInfo()).append("</span>\n");
      text.append("&nbsp;||&nbsp;\n");
    } catch (final Exception ex) {
      ex.printStackTrace();
      log4j.error("Failed when trying to get parent menu element for breadcrumb");
    }
    while (!this.breadcrumb.empty()) {
      final WindowTabsData data = this.breadcrumb.pop();
      if (!isFirst)
        text.append("&nbsp;&gt;&gt;&nbsp;\n");
      else
        isFirst = false;
      if (!this.breadcrumb.empty()) {
        text.append("<a class=\"Link\" onmouseover=\"return true;\" href=\"#\" onclick=\"");
        text.append(getUrlCommand(data.adTabId, data.name, Integer.valueOf(data.tablevel)
            .intValue()));
        text.append("\" onmouseout=\"return true;\">").append(data.tabname).append("</a>\n");
      } else
        text.append(data.tabname).append("\n");
    }
    return text.toString();
  }

  /**
   * Auxiliar method to get the click command for the elements.
   * 
   * @param _tabId
   *          String with the id of the tab.
   * @param _tabName
   *          String with the tab's name.
   * @param _level
   *          Integer with the tab's level.
   * @return String with the javascript command.
   */
  private String getUrlCommand(String _tabId, String _tabName, int _level) {
    final StringBuffer text = new StringBuffer();
    if (!_tabId.equals(this.TabID) && this.level + 1 >= _level) {
      if (this.newRecord && this.level < _level) {
        // Log user click when is NEW record and clicking a child tab
        text.append("logClick(null);");
      }
      text.append("submitCommandForm('").append(((this.level > _level) ? "DEFAULT" : "TAB"))
          .append("', ");

      if (this.editView) {
        if (this.newRecord) {
          if (this.level >= _level) {
            text.append("isUserChanges"); // Validate when click parent or sibling tab, when user
            // has changed something
          } else {
            text.append("true"); // Always validate when are in NEW record and clicking a child tab
          }
        } else {
          text.append("false"); // Don't validate on previously saved record
        }
      } else { // Grid
        if (this.level >= _level) {
          text.append("isUserChanges"); // validate when user has changed something
        } else {
          text.append("(dijit.byId('grid').metaData.totalRows == 0)"); // Validate on empty grid
        }
      }
      text.append(", null, '");
      text.append(Utility.getTabURL(_tabId, _tabName, true)).append("', '_self', true, true);");
    }
    text.append("return false;");
    return text.toString();
  }
}