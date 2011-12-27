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
package org.openbravo.erpCommon.utility;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.FormatUtilities;

/**
 * @author Fernando Iriazabal
 * 
 *         Class in charge of the keymap building for each window type.
 */
public class KeyMap {
  static Logger log4j = Logger.getLogger(KeyMap.class);
  private ConnectionProvider conn;
  private String TabID = "";
  private Properties myData = new Properties();

  /**
   * Constructor
   * 
   * @param _conn
   *          Handler for the database connection.
   * @param _tabId
   *          String with the tab's id.
   * @param _windowId
   *          String with the window's id.
   * @throws Exception
   */
  public KeyMap(ConnectionProvider _conn, String _tabId, String _windowId) throws Exception {
    if (_conn == null || _tabId == null || _tabId.equals("") || _windowId == null
        || _windowId.equals(""))
      throw new Exception("Missing parameters");
    this.conn = _conn;
    this.TabID = _tabId;
    generateStructure();
  }

  /**
   * Constructor
   * 
   * @param _conn
   *          Handler for the database connection.
   * @param _action
   *          String with the window type (form, report, process...)
   * @throws Exception
   */
  public KeyMap(ConnectionProvider _conn, String _action) throws Exception {
    if (_conn == null || _action == null || _action.equals(""))
      throw new Exception("Missing parameters");
    this.conn = _conn;
  }

  /**
   * Setter for any internal attribute.
   * 
   * @param name
   *          String with the name of the attribute.
   * @param value
   *          String with the value of the attribute.
   */
  private void setData(String name, String value) {
    if (name == null || name.equals(""))
      return;
    if (this.myData == null)
      this.myData = new Properties();
    this.myData.setProperty(name, value);
  }

  /**
   * Getter for any internal attribute.
   * 
   * @param name
   *          String with the name of the attribute.
   * @return String with the value of the attribute.
   */
  private String getData(String name) {
    if (name == null || name.equals("") || this.myData == null)
      return "";
    String aux = this.myData.getProperty(name);
    if (aux == null)
      return "";
    else
      return aux;
  }

  /**
   * Gets the keymap for the sort tab window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getSortTabKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");
    return script.toString();
  }

  /**
   * Gets the keymap for the Relation window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getRelationKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\"),\n");
    script.append("new keyArrayItem(\"N\", \"submitCommandForm('NEW', false, null, '")
        .append(getData("TabNameUrl")).append("_Edition.html', '_self');\", null, \"ctrlKey\"),\n");
    script.append("new keyArrayItem(\"E\", \"submitCommandForm('EDIT', true, null, '")
        .append(getData("TabNameUrl")).append("_Edition.html', '_self');\", null, \"ctrlKey\"),\n");
    script
        .append(
            "new keyArrayItem(\"B\", \"openSearchWindow('../businessUtility/Buscador.html', 'BUSCADOR', document.frmMain.inpTabId.value, '")
        .append(getData("WindowNameUrl"))
        .append("/")
        .append(getData("TabNameUrl"))
        .append(
            "_Edition.html', document.frmMain.inpwindowId.value, true);\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");
    return script.toString();
  }

  /**
   * Gets the keymap for the Edition window type.
   * 
   * @param isNew
   *          Boolean to indicate if is a new record or not.
   * @return String with the javascript for the keynap.
   */
  public String getEditionKeyMaps(boolean isNew) {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"N\", \"submitCommandForm('NEW', false, null, '")
        .append(getData("TabNameUrl"))
        .append("_Edition.html', '_self', null, true, null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"L\", \"submitCommandForm('RELATION', true, null, '")
        .append(getData("TabNameUrl"))
        .append("_Relation.html', '_self', null, true, null, true);\", null, \"ctrlKey\")\n");
    script
        .append(
            ",new keyArrayItem(\"B\", \"openSearchWindow('../businessUtility/Buscador.html', 'BUSCADOR', document.frmMain.inpTabId.value, '")
        .append(getData("WindowNameUrl"))
        .append("/")
        .append(getData("TabNameUrl"))
        .append(
            "_Edition.html', document.frmMain.inpwindowId.value, true);\", null, \"ctrlKey\")\n");
    if (!getData("IsTabReadOnly").equals("Y")) {
      if (!isNew) {
        script.append(",new keyArrayItem(\"D\", \"submitCommandForm('DELETE', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self');\", null, \"ctrlKey\")\n");
        script
            .append(",new keyArrayItem(\"S\", \"submitCommandForm('SAVE_EDIT_EDIT', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script
            .append(
                ",new keyArrayItem(\"G\", \"submitCommandForm('SAVE_EDIT_RELATION', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script
            .append(",new keyArrayItem(\"H\", \"submitCommandForm('SAVE_EDIT_NEW', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script
            .append(",new keyArrayItem(\"A\", \"submitCommandForm('SAVE_EDIT_NEXT', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
      } else {
        script
            .append(",new keyArrayItem(\"S\", \"submitCommandForm('SAVE_NEW_EDIT', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script
            .append(
                ",new keyArrayItem(\"G\", \"submitCommandForm('SAVE_NEW_RELATION', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
        script.append(",new keyArrayItem(\"H\", \"submitCommandForm('SAVE_NEW_NEW', true, null, '")
            .append(getData("TabNameUrl"))
            .append("_Relation.html', '_self', true, null, null, true);\", null, \"ctrlKey\")\n");
      }
    }
    script.append(",new keyArrayItem(\"REPAGE\", \"submitCommandForm('FIRST', false, null, '")
        .append(getData("TabNameUrl"))
        .append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"AVPAGE\", \"submitCommandForm('LAST', false, null, '")
        .append(getData("TabNameUrl"))
        .append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(",new keyArrayItem(\"RIGHTARROW\", \"submitCommandForm('NEXT', false, null, '")
        .append(getData("TabNameUrl"))
        .append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script
        .append(",new keyArrayItem(\"LEFTARROW\", \"submitCommandForm('PREVIOUS', false, null, '")
        .append(getData("TabNameUrl"))
        .append("_Edition.html', '_self', null, true);\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

  /**
   * Gets the keymap for the Action button window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getActionButtonKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"\", \"\", null, null)\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

  /**
   * Gets the keymap for the Form window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getFormKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

  /**
   * Gets the keymap for the Report window type.
   * 
   * @return String with the javascript for the keynap.
   */
  public String getReportKeyMaps() {
    StringBuffer script = new StringBuffer();
    script.append("\nvar keyArray = new Array(\n");
    script.append("new keyArrayItem(\"M\", \"menuShowHide('buttonMenu');\", null, \"ctrlKey\")\n");
    script.append(");\n");
    script.append("enableShortcuts();");

    return script.toString();
  }

  /**
   * Generates the needed info to build the keymap.
   * 
   * @throws Exception
   */
  private void generateStructure() throws Exception {
    TableSQLQueryData[] data = TableSQLQueryData.selectKeyMapStructure(this.conn, this.TabID);
    if (data == null || data.length == 0)
      throw new Exception("Couldn't get structure for tab " + this.TabID);

    setData("WindowNameUrl", FormatUtilities.replace(data[0].windowName));
    setData("TabNameUrl", FormatUtilities.replace(data[0].tabName));

  }
}
