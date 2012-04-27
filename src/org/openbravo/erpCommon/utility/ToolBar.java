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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.model.ad.ui.Tab;

public class ToolBar {
  private static Logger log4j = Logger.getLogger(ToolBar.class);
  private ConnectionProvider conn;
  private String language = "en_US";
  private String servlet_action = "";
  private boolean isNew = false;
  private String keyfield = "";
  private String form = "document.forms[0]";
  private String grid_id = "";
  private String pdf = "";
  private boolean isDirectPrint = false;
  private String window_name = "";
  private String base_direction = "";
  private boolean debug = false;
  private boolean isSrcWindow = false;
  private boolean isFrame = false;
  private boolean isRelation = false;
  private boolean isEditable = false;
  private boolean hasAttachments = false;
  private boolean email = false;
  private String tabId;
  private boolean deleteable = true;
  private boolean hasNewButton = true;

  public void setEmail(boolean email) {
    this.email = email;
  }

  /**
   * If the ToolBar is created for a generated window, this functions sets the tabId of that Tab.
   * 
   * Used inside the class to enable/disable functionality based on the concrete tab
   * 
   * @param tabId
   *          tabId of the creator tab, if the ToolBar is for a generated window
   */
  public void setTabId(String tabId) {
    this.tabId = tabId;
  }

  public void setDeleteable(boolean deleteable) {
    this.deleteable = deleteable;
  }

  Hashtable<String, HTMLElement> buttons = new Hashtable<String, HTMLElement>();

  /**
   * Constructor used by the grid view of all generated windows.
   */
  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, false);
  }

  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, _isSrcWindow, false);
  }

  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, _isSrcWindow, _isFrame);
  }

  public ToolBar(ConnectionProvider _conn, String _language, String _action, boolean _isNew,
      String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame, boolean _hasAttachements) {
    this(_conn, true, _language, _action, _isNew, _keyINP, _gridID, _PDFName, _isDirectPrinting,
        _windowName, _baseDirection, _debug, _isSrcWindow, _isFrame, _hasAttachements, true);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, false);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, _isSrcWindow, false);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, _isSrcWindow, false, false, true);
  }

  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame, boolean _hasAttachments) {
    this(_conn, _isEditable, _language, _action, _isNew, _keyINP, _gridID, _PDFName,
        _isDirectPrinting, _windowName, _baseDirection, _debug, _isSrcWindow, _isFrame,
        _hasAttachments, true);
  }

  /**
   * Constructor used by the edit view of all generated windows.
   */
  public ToolBar(ConnectionProvider _conn, boolean _isEditable, String _language, String _action,
      boolean _isNew, String _keyINP, String _gridID, String _PDFName, boolean _isDirectPrinting,
      String _windowName, String _baseDirection, boolean _debug, boolean _isSrcWindow,
      boolean _isFrame, boolean _hasAttachments, boolean _hasNewButton) {
    this.conn = _conn;
    this.language = _language;
    this.servlet_action = _action;
    this.isNew = _isNew;
    this.keyfield = _keyINP;
    if (_gridID != null)
      this.grid_id = _gridID;
    if (_PDFName != null)
      this.pdf = _PDFName;
    this.isDirectPrint = _isDirectPrinting;
    this.window_name = _windowName;
    this.base_direction = _baseDirection;
    this.debug = _debug;
    this.isFrame = _isFrame;
    this.isEditable = _isEditable;
    final int i = this.keyfield.lastIndexOf(".");
    if (i != -1)
      this.form = this.keyfield.substring(0, i);
    this.isSrcWindow = _isSrcWindow;
    this.hasAttachments = _hasAttachments;
    this.hasNewButton = _hasNewButton;
    createAllButtons();
  }

  public void removeElement(String name) {
    try {
      if (buttons != null && !buttons.isEmpty())
        buttons.remove(name);
    } catch (final NullPointerException ignored) {
    }
  }

  private String getButtonScript(String name) {
    if (name.equals("RELATED_INFO")) {
      return (this.isNew ? "logClick(null);" : "")
          + "openServletNewWindow('DEFAULT', true, '../utility/UsedByLink.html', 'LINKS', null, true, 500, 600, true);";
    } else if (name.equals("EDIT")) {
      return "submitCommandForm('" + name + "', true, null, '" + servlet_action
          + (isSrcWindow ? "" : "_Relation") + ".html', '_self', null, "
          + (debug ? "true" : "false") + ");";
    } else if (name.startsWith("SAVE")) {
      return "submitCommandForm('SAVE_" + (isNew ? "NEW" : "EDIT") + name.substring(4)
          + "', true, null, '" + servlet_action + (isSrcWindow ? "" : "_Relation")
          + ".html', '_self', true, false);";
    } else if (name.equals("TREE")) {
      return "openServletNewWindow('DEFAULT', false, '../utility/WindowTree.html', 'TREE', null, null,625, 750, true, false, false);";
    } else if (name.equals("ATTACHMENT")) {
      return ((grid_id == null || grid_id.equals("")) ? ""
          : "if (dijit.byId('"
              + grid_id
              + "').getSelectedRows()=='') {showJSMessage(23);resizeArea(true);calculateMsgBoxWidth();return false;} ")
          + " openServletNewWindow('DEFAULT', false, '../businessUtility/TabAttachments_FS.html?inpKey=' + "
          + ((grid_id == null || grid_id.equals("")) ? keyfield + ".value" : "dijit.byId('"
              + grid_id + "').getSelectedRows()")
          + "+'&inpEditable="
          + (isEditable ? "Y" : "N")
          + "', 'ATTACHMENT', null, true, 600, 700, true);";
    } else if (name.equals("EXCEL")) {
      return "openExcel('" + servlet_action + "_Excel.xls?Command=RELATION_XLS', '_blank');";
    } else if (name.equals("GRIDEXCEL")) {
      return "openServletNewWindow('EXCEL', false, '../utility/ExportGrid.html?inpTabId=' + document.forms[0].inpTabId.value + '&inpWindowId=' + document.forms[0].inpwindowId.value + '&inpAccessLevel=' + document.forms[0].inpAccessLevel.value, 'GRIDEXCEL', null, null, 500, 350, true );";
    } else if (name.equals("GRIDCSV")) {
      return "openServletNewWindow('CSV', false, '../utility/ExportGrid.html?inpTabId=' + document.forms[0].inpTabId.value + '&inpWindowId=' + document.forms[0].inpwindowId.value + '&inpAccessLevel=' + document.forms[0].inpAccessLevel.value, 'GRIDCSV', null, null, 500, 350, true );";
    } else if (name.equals("GRIDPDF")) {
      return "openServletNewWindow('PDF', false, '../utility/ExportGrid.html?inpTabId=' + document.forms[0].inpTabId.value + '&inpWindowId=' + document.forms[0].inpwindowId.value + '&inpAccessLevel=' + document.forms[0].inpAccessLevel.value, 'GRIDPDF', null, null, 500, 350, true );";
    } else if (name.equals("PRINT")) {
      return "openPDFSession('"
          + pdf
          + "', '"
          + (isDirectPrint ? "Printing" : "")
          + "', "
          + keyfield
          + ".name, "
          + ((grid_id == null || grid_id.equals("")) ? "null" : "dijit.byId('" + grid_id
              + "').getSelectedRows()") + ", "
          + ((grid_id == null || grid_id.equals("")) ? "true" : "null") + ");";
    } else if (name.equals("EMAIL")) {
      return "openPDFSession('"
          + pdf.replaceAll("print.html", "send.html")
          + "', '"
          + (isDirectPrint ? "Printing" : "")
          + "', "
          + keyfield
          + ".name, "
          + ((grid_id == null || grid_id.equals("")) ? "null" : "dijit.byId('" + grid_id
              + "').getSelectedRows()") + ", "
          + ((grid_id == null || grid_id.equals("")) ? "true" : "null") + ");";
    } else if (name.equals("UNDO")) {
      return "windowUndo(" + form + ");";
    } else if (name.equals("SEARCH")) {
      return "openSearchWindow('../businessUtility/Buscador.html', 'BUSCADOR', " + form
          + ".inpTabId.value, '" + window_name + "/" + servlet_action
          + (isSrcWindow ? "" : "_Relation") + ".html', " + form + ".inpwindowId.value, "
          + (debug ? "true" : "false") + ");";
    } else if (name.equals("AUDIT_EDITION")) {
      return "changeAuditStatus();";
    } else if (name.equals("AUDIT_RELATION")) {
      return "changeAuditStatusRelation();";
    } else if (name.equals("AUDIT_TRAIL")) {
      // open audit trail popup if exactly one record is selected, otherwise show error message
      return ((grid_id == null || grid_id.equals("")) ? ""
          : "if (dijit.byId('"
              + grid_id
              + "').getSelectedRows().length > 1) {showJSMessage(28);resizeArea(true);calculateMsgBoxWidth();return false;} ")
          + " openServletNewWindow('POPUP_HISTORY', false, '../businessUtility/AuditTrail.html?inpRecordId=' + "
          + ((grid_id == null || grid_id.equals("")) ? keyfield + ".value" : "dijit.byId('"
              + grid_id + "').getSelectedRows()")
          + ", 'AuditTrail', null, true, 600, 900, true, null, null, null, true);";
    } else if (grid_id != null && !grid_id.equals("") && name.equals("PREVIOUS")) {
      return "dijit.byId('grid').goToPreviousRow();";
    } else if (grid_id != null && !grid_id.equals("") && name.equals("NEXT")) {
      return "dijit.byId('grid').goToNextRow();";
    } else if (name.equals("DELETE_RELATION")) {
      return "dijit.byId('grid').deleteRow();";
    } else if (name.equals("FIRST_RELATION")) {
      return "dijit.byId('grid').goToFirstRow();";
    } else if (name.equals("LAST_RELATION")) {
      return "dijit.byId('grid').goToLastRow();";
    } else if (name.equals("GRID_VIEW")) {
      return !isRelation ? "submitCommandForm('RELATION', isUserChanges, null, '" + servlet_action
          + (isSrcWindow ? "" : "_Relation") + ".html', '_self', null, true);" : "";
    } else if (name.equals("FORM_VIEW")) {
      return isRelation ? "submitCommandForm('EDIT', true, null, '" + servlet_action
          + (isSrcWindow ? "" : "_Relation") + ".html', '_self', null, false);" : "";
    } else {
      String action = "";

      if ("NEW".equals(name)) {
        action = "disableToolBarButton('linkButtonNew'); ";
      }
      return action + "submitCommandForm('" + (name.equals("REFRESH") ? "DEFAULT" : name) + "', "
          + (name.equals("NEW") && (this.grid_id.equals("")) ? "true" : "false") + ", null, '"
          + servlet_action + (isSrcWindow ? "" : "_Relation") + ".html', '"
          + (isFrame ? "_parent" : "_self") + "', null, " + (debug ? "true" : "false") + ");";
    }
  }

  private void createAllButtons() {
    if (hasNewButton) {
      buttons.put("NEW",
          new ToolBar_Button(base_direction, "New", Utility.messageBD(conn, "New", language),
              getButtonScript("NEW")));
    }

    buttons.put("EDIT",
        new ToolBar_Button(base_direction, "Edit", Utility.messageBD(conn, "Edit", language),
            getButtonScript("EDIT")));
    buttons.put(
        "RELATION",
        new ToolBar_Button(base_direction, "Relation", Utility
            .messageBD(conn, "Relation", language), getButtonScript("RELATION")));
    buttons.put("FIND",
        new ToolBar_Button(base_direction, "Find", Utility.messageBD(conn, "Find", language),
            getButtonScript("FIND")));
    buttons.put("SEPARATOR2", new ToolBar_Space(base_direction));
    buttons.put(
        "SAVE_RELATION",
        new ToolBar_Button(base_direction, "Save_Relation", Utility.messageBD(conn, "SaveRelation",
            language), getButtonScript("SAVE_RELATION")));
    buttons.put("SAVE_NEW",
        new ToolBar_Button(base_direction, "Save_New",
            Utility.messageBD(conn, "SaveNew", language), getButtonScript("SAVE_NEW")));
    buttons.put("SAVE_EDIT",
        new ToolBar_Button(base_direction, "Save", Utility.messageBD(conn, "SaveEdit", language),
            getButtonScript("SAVE_EDIT")));
    buttons.put(
        "SAVE_NEXT",
        new ToolBar_Button(base_direction, "Save_Next", Utility.messageBD(conn, "SaveNext",
            language), getButtonScript("SAVE_NEXT")));
    buttons.put("SEPARATOR3", new ToolBar_Space(base_direction));
    if (isEditable) {
      buttons.put("DELETE",
          new ToolBar_Button(base_direction, "Delete", Utility.messageBD(conn, "Delete", language),
              getButtonScript("DELETE")));
      buttons.put("DELETE_RELATION",
          new ToolBar_Button(base_direction, "Delete", Utility.messageBD(conn, "Delete", language),
              getButtonScript("DELETE_RELATION")));
    }
    buttons.put("SEPARATOR4", new ToolBar_Space(base_direction));
    buttons.put("UNDO",
        new ToolBar_Button(base_direction, "Undo", Utility.messageBD(conn, "Undo", language),
            getButtonScript("UNDO")));

    if (Utility.isNewUI()) {
      buttons.put(
          "REFRESH",
          new ToolBar_Button(base_direction, "Refresh", Utility
              .messageBD(conn, "Refresh", language), getButtonScript("REFRESH")));
    }

    buttons.put("TREE",
        new ToolBar_Button(base_direction, "Tree", Utility.messageBD(conn, "Tree", language),
            getButtonScript("TREE")));
    buttons.put(
        "ATTACHMENT",
        new ToolBar_Button(base_direction, "Attachment", Utility.messageBD(conn, "Attachment",
            language), getButtonScript("ATTACHMENT"), null, hasAttachments ? "AttachedDocuments"
            : "Attachment"));
    buttons.put(
        "EXCEL",
        new ToolBar_Button(base_direction, "Excel", Utility
            .messageBD(conn, "ExportExcel", language), getButtonScript("EXCEL")));
    buttons.put(
        "GRIDEXCEL",
        new ToolBar_Button(base_direction, "ExportExcel", Utility.messageBD(conn, "ExportExcel",
            language), getButtonScript("GRIDEXCEL")));
    buttons.put(
        "GRIDCSV",
        new ToolBar_Button(base_direction, "ExportCsv", Utility.messageBD(conn, "ExportCsv",
            language), getButtonScript("GRIDCSV")));
    buttons.put(
        "GRIDPDF",
        new ToolBar_Button(base_direction, "ExportPDF", Utility.messageBD(conn, "ExportPDF",
            language), getButtonScript("GRIDPDF")));

    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              getButtonScript("PRINT")));
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              getButtonScript("EMAIL")));
    }
    buttons.put("SEARCH",
        new ToolBar_Button(base_direction, "Search", Utility.messageBD(conn, "Search", language),
            getButtonScript("SEARCH")));
    buttons.put(
        "SEARCH_FILTERED",
        new ToolBar_Button(base_direction, "SearchFiltered", Utility.messageBD(conn, "Search",
            language), getButtonScript("SEARCH")));
    buttons.put("AUDIT_SHOW_EDITION_ENABLED",
        new ToolBar_Button(base_direction, "Audit", Utility.messageBD(conn, "HideAudit", language),
            getButtonScript("AUDIT_EDITION"), true));
    buttons.put("AUDIT_SHOW_EDITION_DISABLED",
        new ToolBar_Button(base_direction, "Audit", Utility.messageBD(conn, "ShowAudit", language),
            getButtonScript("AUDIT_EDITION"), false));
    buttons.put("AUDIT_SHOW_RELATION_ENABLED",
        new ToolBar_Button(base_direction, "Audit", Utility.messageBD(conn, "HideAudit", language),
            getButtonScript("AUDIT_RELATION"), true));
    buttons.put("AUDIT_SHOW_RELATION_DISABLED",
        new ToolBar_Button(base_direction, "Audit", Utility.messageBD(conn, "ShowAudit", language),
            getButtonScript("AUDIT_RELATION"), false));
    buttons.put(
        "AUDIT_TRAIL",
        new ToolBar_Button(base_direction, "AuditTrail", Utility.messageBD(conn, "AuditTrail",
            language), getButtonScript("AUDIT_TRAIL"), false));
    buttons.put("SEPARATOR5", new ToolBar_Space(base_direction));
    buttons.put("FIRST",
        new ToolBar_Button(base_direction, "First", Utility.messageBD(conn, "GotoFirst", language),
            getButtonScript("FIRST")));
    buttons.put("FIRST_RELATION",
        new ToolBar_Button(base_direction, "First", Utility.messageBD(conn, "GotoFirst", language),
            getButtonScript("FIRST_RELATION")));
    buttons.put(
        "PREVIOUS",
        new ToolBar_Button(base_direction, "Previous", Utility.messageBD(conn, "GotoPrevious",
            language), getButtonScript("PREVIOUS")));
    buttons.put("NEXT",
        new ToolBar_Button(base_direction, "Next", Utility.messageBD(conn, "GotoNext", language),
            getButtonScript("NEXT")));
    buttons.put("LAST",
        new ToolBar_Button(base_direction, "Last", Utility.messageBD(conn, "GotoLast", language),
            getButtonScript("LAST")));
    buttons.put("LAST_RELATION",
        new ToolBar_Button(base_direction, "Last", Utility.messageBD(conn, "GotoLast", language),
            getButtonScript("LAST_RELATION")));

    buttons.put("SEPARATOR6", new ToolBar_Space(base_direction));
    // buttons.put("PREVIOUS_RELATION", new ToolBar_Button(base_direction,
    // "PreviousRange", Utility.messageBD(conn, "GotoPreviousRange",
    // language), getButtonScript("PREVIOUS_RELATION")));
    buttons.put(
        "PREVIOUS_RELATION",
        new ToolBar_Button(base_direction, "Previous", Utility.messageBD(conn, "GotoPreviousRange",
            language), getButtonScript("PREVIOUS_RELATION")));
    buttons.put("PREVIOUS_RELATION_DISABLED", new ToolBar_Button(base_direction,
        "PreviousRangeDisabled", Utility.messageBD(conn, "GotoPreviousRange", language), ""));
    // buttons.put("NEXT_RELATION", new ToolBar_Button(base_direction,
    // "NextRange", Utility.messageBD(conn, "GotoNextRange", language),
    // getButtonScript("NEXT_RELATION")));
    buttons.put(
        "NEXT_RELATION",
        new ToolBar_Button(base_direction, "Next", Utility.messageBD(conn, "GotoNextRange",
            language), getButtonScript("NEXT_RELATION")));
    buttons.put("NEXT_RELATION_DISABLED", new ToolBar_Button(base_direction, "NextRangeDisabled",
        Utility.messageBD(conn, "GotoNextRange", language), ""));

    buttons.put("SEPARATOR7", new ToolBar_Space(base_direction));
    buttons.put("HR1", new ToolBar_HR());
    buttons.put(
        "RELATED_INFO",
        new ToolBar_Button(base_direction, "RelatedInfo", Utility.messageBD(conn, "Linked Items",
            language), getButtonScript("RELATED_INFO")));
  }

  /**
   * Utility function to change the visibility of the 'Audit Trail' icon.
   * 
   * community instances: always shown
   * 
   * activated instances: shown if isFullyAudited for the table
   */
  private void changeAuditTrailVisibility() {
    // always hide button in the audit trail window
    if ("3690EB6BA1614375A6F058BBA61B19BC".equals(tabId)) {
      removeElement("AUDIT_TRAIL");
      return;
    }
    if (tabId != null) {
      OBContext.setAdminMode();
      try {
        // ActivationKey already initialized in i.e. HSAS, so just take static info
        if (ActivationKey.isActiveInstance()) {
          // is an obps instance
          Tab tab = OBDal.getInstance().get(Tab.class, tabId);
          if (!tab.getTable().isFullyAudited()) {
            // remove icon
            removeElement("AUDIT_TRAIL");
          }
        }
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  public void prepareInfoTemplate(boolean hasPrevious, boolean hasNext, boolean isTest) {
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("SEPARATOR3");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("SEPARATOR4");
    // removeElement("REFRESH");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("PRINT");
    removeElement("EMAIL");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("SEPARATOR5");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("SEPARATOR6");

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");

    if (!hasPrevious)
      removeElement("PREVIOUS_RELATION");
    else
      removeElement("PREVIOUS_RELATION_DISABLED");
    if (!hasNext)
      removeElement("NEXT_RELATION");
    else
      removeElement("NEXT_RELATION_DISABLED");
    if (!isTest)
      removeAllTests();
  }

  public void prepareEditionTemplate(boolean hasTree, boolean isFiltered, boolean isTest,
      String uiPattern, boolean isAuditEnabled) {
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("DELETE_RELATION");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("FIRST_RELATION");
    removeElement("LAST_RELATION");
    removeElement("FIND");

    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");

    if (isNew) {
      removeElement("AUDIT_SHOW_EDITION_DISABLED");
      removeElement("AUDIT_SHOW_EDITION_ENABLED");
      removeElement("AUDIT_TRAIL");
    } else {
      if (isAuditEnabled)
        removeElement("AUDIT_SHOW_EDITION_DISABLED");
      else
        removeElement("AUDIT_SHOW_EDITION_ENABLED");
    }
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");

    if (!deleteable) {
      removeElement("DELETE");
    }

    // This piece of code used to control the email icon in the manual window. At this point we only
    // use the email functionality
    // only to send order (purchase or sales) and invoices (purchase or sales)
    if (pdf != null && !pdf.contains("orders") && !pdf.contains("invoices")
        && !pdf.contains("payments")) {
      removeElement("EMAIL");
    }
    if (!hasTree)
      removeElement("TREE");
    if (isNew) {
      removeElement("SAVE_NEXT");
      removeElement("DELETE");
      removeElement("ATTACHMENT");
    }
    if (isFiltered)
      removeElement("SEARCH");
    else
      removeElement("SEARCH_FILTERED");
    if (!isTest)
      removeAllTests();
    if (uiPattern.equals("RO")) // read-only
      removeReadOnly();
    if (uiPattern.equals("SR")) // single record
      removeSingleRecord();

    changeAuditTrailVisibility();
  }

  public void prepareEditionTemplateNoSearch(boolean hasTree, boolean isFiltered, boolean isTest,
      String uiPattern, boolean isAuditEnabled) {
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    prepareEditionTemplate(hasTree, isFiltered, isTest, uiPattern, isAuditEnabled);
  }

  /**
   * Deprecated 2.50 use instead
   * {@link ToolBar#prepareEditionTemplateNoSearch(boolean, boolean, boolean, String, boolean)}
   * 
   * @param hasTree
   * @param isFiltered
   * @param isTest
   * @param isReadOnly
   * @param isAuditEnabled
   */
  @Deprecated
  public void prepareEditionTemplateNoSearch(boolean hasTree, boolean isFiltered, boolean isTest,
      boolean isReadOnly, boolean isAuditEnabled) {
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    prepareEditionTemplate(hasTree, isFiltered, isTest, isReadOnly, isAuditEnabled);
  }

  /**
   * Deprecated 2.50 used instead
   * {@link ToolBar#prepareEditionTemplate(boolean hasTree, boolean isFiltered, boolean isTest, String uiPattern, boolean isAuditEnabled)}
   * 
   * @param hasTree
   * @param isFiltered
   * @param isTest
   * @param isReadOnly
   * @param isAuditEnabled
   */
  @Deprecated
  public void prepareEditionTemplate(boolean hasTree, boolean isFiltered, boolean isTest,
      boolean isReadOnly, boolean isAuditEnabled) {
    prepareEditionTemplate(hasTree, isFiltered, isTest, (isReadOnly ? "RO" : "STD"), isAuditEnabled);
  }

  public void prepareRelationTemplateNoSearch(boolean hasTree, boolean isFiltered, boolean isTest,
      boolean isReadOnly, boolean isAuditEnabled) {
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    prepareRelationTemplate(hasTree, isFiltered, isTest, isReadOnly, isAuditEnabled);
  }

  public void prepareRelationTemplate(boolean hasTree, boolean isFiltered, boolean isTest,
      boolean isReadOnly, boolean isAuditEnabled) {
    isRelation = true;
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("DELETE");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("UNDO");
    removeElement("FIRST");
    removeElement("LAST");
    removeElement("FIND");
    removeElement("EXCEL");

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    if (isAuditEnabled)
      removeElement("AUDIT_SHOW_RELATION_DISABLED");
    else
      removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");

    if (!deleteable) {
      removeElement("DELETE_RELATION");
    }

    if (!hasTree)
      removeElement("TREE");
    if (isFiltered)
      removeElement("SEARCH");
    else
      removeElement("SEARCH_FILTERED");
    if (!isTest)
      removeAllTests();
    if (isReadOnly)
      removeReadOnly();

    if (pdf != null && !pdf.contains("orders") && !pdf.contains("invoices")
        && !pdf.contains("payments")) {
      removeElement("EMAIL");
    }

    changeAuditTrailVisibility();
  }

  // AL New toolbars
  public void prepareSimpleToolBarTemplateFrame() {

  }

  public void prepareSimpleToolBarTemplate() {
    removeElement("SEPARATOR1");
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");
    removeElement("EMAIL");
    removeElement("PRINT");
    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              pdf));

    }
    if (email) {
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              pdf));
    }
    removeElement("RELATED_INFO");

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");
  }

  // Simple toolbar with save button
  public void prepareSimpleSaveToolBarTemplate() {
    removeElement("RELATED_INFO");
    removeElement("SEPARATOR1");
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");
    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              pdf));
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              pdf));
    }
    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");
  }

  public void prepareRelationBarTemplate(boolean hasPrevious, boolean hasNext) {
    prepareRelationBarTemplate(hasPrevious, hasNext, "");
  }

  public void prepareRelationBarTemplate(boolean hasPrevious, boolean hasNext, String excelScript) {
    removeElement("SEPARATOR1");
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("EMAIL");
    removeElement("PRINT");

    removeElement(hasPrevious ? "PREVIOUS_RELATION_DISABLED" : "PREVIOUS_RELATION");
    removeElement(hasNext ? "NEXT_RELATION_DISABLED" : "NEXT_RELATION");

    removeElement("RELATED_INFO"); // Modified
    if (pdf != null && !pdf.equals("") && !pdf.equals("..")) {
      buttons.put("PRINT",
          new ToolBar_Button(base_direction, "Print", Utility.messageBD(conn, "Print", language),
              pdf));

    }
    if (email) {
      buttons.put("EMAIL",
          new ToolBar_Button(base_direction, "Email", Utility.messageBD(conn, "Email", language),
              pdf));
    }
    if (!excelScript.equals("") && excelScript != null)
      buttons.put(
          "EXCEL",
          new ToolBar_Button(base_direction, "Excel", Utility.messageBD(conn, "ExportExcel",
              language), excelScript));

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");
  }

  public void prepareSimpleExcelToolBarTemplate(String excelScript) {
    removeElement("SEPARATOR1");
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("ATTACHMENT");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");

    if (!excelScript.equals("") && excelScript != null)
      buttons.put(
          "EXCEL",
          new ToolBar_Button(base_direction, "Excel", Utility.messageBD(conn, "ExportExcel",
              language), excelScript));

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");

  }

  // GD Toolbar with Menu, Refresh and Excel buttons
  public void prepareExcelToolBarTemplate() {
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("FIND");
    removeElement("SEPARATOR2");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("ATTACHMENT");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");
  }

  // AL
  public void prepareSortableTemplate(boolean isTest) {
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("ATTACHMENT");
    removeElement("EXCEL");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("FIRST_RELATION");
    removeElement("PREVIOUS");
    removeElement("NEXT");
    removeElement("LAST");
    removeElement("LAST_RELATION");
    removeElement("PREVIOUS_RELATION");
    removeElement("PREVIOUS_RELATION_DISABLED");
    removeElement("NEXT_RELATION");
    removeElement("NEXT_RELATION_DISABLED");
    removeElement("FIND");
    removeElement("RELATED_INFO");
    if (!isTest)
      removeAllTests();

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");
  }

  public void prepareQueryTemplate(boolean hasPrevious, boolean hasNext, boolean isTest) {
    removeElement("NEW");
    removeElement("EDIT");
    removeElement("RELATION");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEW");
    removeElement("SAVE_EDIT");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
    removeElement("UNDO");
    removeElement("TREE");
    removeElement("GRIDEXCEL");
    removeElement("GRIDCSV");
    removeElement("GRIDPDF");
    removeElement("ATTACHMENT");
    removeElement("SEARCH");
    removeElement("SEARCH_FILTERED");
    removeElement("ORDERBY");
    removeElement("ORDERBY_FILTERED");
    removeElement("FIRST");
    removeElement("LAST");
    if (!hasPrevious)
      removeElement("PREVIOUS_RELATION");
    else
      removeElement("PREVIOUS_RELATION_DISABLED");
    if (!hasNext)
      removeElement("NEXT_RELATION");
    else
      removeElement("NEXT_RELATION_DISABLED");
    if (!isTest)
      removeAllTests();

    removeElement("AUDIT_SHOW_EDITION_DISABLED");
    removeElement("AUDIT_SHOW_EDITION_ENABLED");
    removeElement("AUDIT_SHOW_RELATION_DISABLED");
    removeElement("AUDIT_SHOW_RELATION_ENABLED");
    removeElement("AUDIT_TRAIL");
  }

  private void removeAllTests() {

  }

  private void removeReadOnly() {
    removeSingleRecord();
    removeElement("SAVE_EDIT");
  }

  private void removeSingleRecord() {
    removeElement("NEW");
    removeElement("SAVE_NEW");
    removeElement("SAVE_RELATION");
    removeElement("SAVE_NEXT");
    removeElement("DELETE");
    removeElement("DELETE_RELATION");
  }

  private String transformElementsToString(HTMLElement element, Vector<String> vecLastType,
      boolean isReference) {
    if (element == null)
      return "";
    if (vecLastType == null)
      vecLastType = new Vector<String>(0);
    final StringBuffer sbElement = new StringBuffer();
    String lastType = "";
    if (vecLastType.size() > 0)
      lastType = vecLastType.elementAt(0);
    if (lastType.equals("SPACE") && element.elementType().equals("SPACE"))
      return "";
    if (isReference) {
      sbElement.append("<td width=\"1\">");
      sbElement.append("<img src=\"").append(base_direction)
          .append("/images/blank.gif\" class=\"Main_ToolBar_textlabel_bg_left\" border=\"0\">");
      sbElement.append("</td>\n");
      sbElement.append("<td class=\"Main_ToolBar_textlabel_bg_body\">");
      sbElement
          .append(
              "<a class=\"Main_ToolBar_text_relatedinfo\" href=\"#\" onclick=\""
                  + (this.isNew ? "logClick(null);" : "")
                  + "openServletNewWindow('DEFAULT', true, '../utility/UsedByLink.html', 'LINKS', null, true, 500, 600, true);\">")
          .append(Utility.messageBD(conn, "Linked Items", language)).append("</a></td>\n");
    }
    sbElement.append("<td ");
    if (isReference)
      sbElement.append("class=\"Main_ToolBar_textlabel_bg_right\" ");
    if (element.elementType().equals("SPACE"))
      sbElement.append("class=\"Main_ToolBar_Separator_cell\" ");
    else if (!element.elementType().equals("HR"))
      sbElement.append("width=\"").append(element.getWidth()).append("\" ");
    else
      sbElement.append("class=\"Main_ToolBar_Space\"");
    sbElement.append(">");
    if (!element.elementType().equals("HR"))
      sbElement.append(element);
    sbElement.append("</td>\n");
    vecLastType.clear();
    vecLastType.addElement(element.elementType());
    return sbElement.toString();
  }

  @Override
  public String toString() {
    final StringBuffer toolbar = new StringBuffer();
    toolbar.append("<table class=\"Main_ContentPane_ToolBar Main_ToolBar_bg\" id=\"tdToolBar\">\n");
    toolbar.append("<tr>\n");
    if (buttons != null) {
      final Vector<String> lastType = new Vector<String>(0);

      // In case of using new UI, add in toolbar grid and edition buttons
      if (Utility.isNewUI() && !isSrcWindow) {
        buttons.put(
            "FORM_VIEW",
            new ToolBar_Button(base_direction, "Edition", Utility.messageBD(conn, "Form View",
                language), getButtonScript("FORM_VIEW"), !isRelation, "Edition"
                + (isNew ? "_new" : "")));
        buttons.put(
            "GRID_VIEW",
            new ToolBar_Button(base_direction, "Relation", Utility.messageBD(conn, "Grid View",
                language), getButtonScript("GRID_VIEW"), isRelation));
        buttons.put("SEPARATOR_NEWUI", new ToolBar_Space(base_direction));
        toolbar.append(transformElementsToString(buttons.get("FORM_VIEW"), lastType, false));
        toolbar.append(transformElementsToString(buttons.get("GRID_VIEW"), lastType, false));
        toolbar.append(transformElementsToString(buttons.get("SEPARATOR_NEWUI"), lastType, false));
      }

      toolbar.append(transformElementsToString(buttons.get("NEW"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("EDIT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("FIND"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR2"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SAVE_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SAVE_NEW"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SAVE_EDIT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SAVE_NEXT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR3"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("DELETE"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("DELETE_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR4"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("UNDO"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("REFRESH"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("TREE"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("ATTACHMENT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("EXCEL"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("GRIDEXCEL"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("GRIDCSV"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("GRIDPDF"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PRINT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("EMAIL"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEARCH"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEARCH_FILTERED"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_SHOW_EDITION_ENABLED"), lastType,
          false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_SHOW_RELATION_ENABLED"),
          lastType, false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_SHOW_EDITION_DISABLED"),
          lastType, false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_SHOW_RELATION_DISABLED"),
          lastType, false));
      toolbar.append(transformElementsToString(buttons.get("AUDIT_TRAIL"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("ORDERBY"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("ORDERBY_FILTERED"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR5"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("FIRST"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("FIRST_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PREVIOUS"), lastType, false));
      // toolbar.append("<td class=\"TB_Bookmark\" width=\"5px\"><nobr id=\"bookmark\"></nobr></td>\n"
      // );
      toolbar.append(transformElementsToString(buttons.get("NEXT"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("LAST"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("LAST_RELATION"), lastType, false));
      if (isRelation) {
        toolbar.append("<td width=\"1\"><img src=\"").append(base_direction)
            .append("/images/blank.gif\" style=\"width: 7px;\" border=\"0\">");
        toolbar.append("<td width=\"1\"><img src=\"").append(base_direction)
            .append("/images/blank.gif\" class=\"Main_ToolBar_textlabel_bg_left\" border=\"0\">");
        toolbar.append("</td>\n");
        toolbar.append("<td class=\"Main_ToolBar_textlabel_bg_body\">\n");
        toolbar.append("<div id=\"bookmark\">\n");
        toolbar.append("</div>\n");
        toolbar.append("</td>\n");
        toolbar.append("<td width=\"1\" class=\"Main_ToolBar_textlabel_bg_right\">");
        toolbar.append("<div style=\"padding: 0; margin: 0; border: 0; width: 9px;\" />");
        toolbar.append("</td>\n");
      }
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR6"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PREVIOUS_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("PREVIOUS_RELATION_DISABLED"), lastType,
          false));
      toolbar.append(transformElementsToString(buttons.get("NEXT_RELATION"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("NEXT_RELATION_DISABLED"), lastType,
          false));
      toolbar.append(transformElementsToString(buttons.get("SEPARATOR7"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("HR1"), lastType, false));
      toolbar.append(transformElementsToString(buttons.get("RELATED_INFO"), lastType, true));
    }
    toolbar.append("</tr>\n");
    toolbar.append("</table>\n");
    return toolbar.toString();
  }
}
