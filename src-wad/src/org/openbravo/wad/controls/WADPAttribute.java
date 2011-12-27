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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.wad.controls;

import java.util.Properties;

public class WADPAttribute extends WADSearch {

  public WADPAttribute() {
  }

  public WADPAttribute(Properties prop) {
    setInfo(prop);
    initialize();
  }

  public void initialize() {
    addImport("searchs", "../../../../../web/js/searchs.js");
    generateJSCode();
    this.button = new WADFieldButton(this.imageName, getData("ColumnName"),
        getData("ColumnNameInp"), this.searchName, this.command);
    this.isFieldEditable = false;
  }

  private void generateJSCode() {
    StringBuffer validation = new StringBuffer();
    if (getData("IsMandatory").equals("Y")) {
      validation.append("  if (inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")==null || inputValue(frm.inp").append(getData("ColumnNameInp"))
          .append(")==\"\") {\n");
      if (getData("IsDisplayed").equals("Y"))
        validation.append("    setWindowElementFocus(frm.inp").append(getData("ColumnNameInp"))
            .append("_R);\n");
      validation.append("    showJSMessage(1);\n");
      validation.append("    return false;\n");
      validation.append("  }\n");
    }
    setValidation(validation.toString());
    setCalloutJS();
    {

      String text = "function debugSearch(key, text, keyField) {\n" + "  return true;\n" + "}";
      addJSCode("debugSearch", text);
    }
    if (!getData("IsReadOnly").equals("Y") && !getData("IsReadOnlyTab").equals("Y")) {
      StringBuffer commandScript = new StringBuffer();
      StringBuffer text = new StringBuffer();
      commandScript.append("openPAttribute(null, null, '../info/AttributeSetInstance.html', ");
      commandScript.append("null, true, 'frmMain', 'inp").append(getData("ColumnNameInp"))
          .append("', ");
      commandScript.append("'inp").append(getData("ColumnNameInp")).append("_R', ");
      commandScript.append("inputValue(document.frmMain.inp").append(getData("ColumnNameInp"))
          .append("_R), ");
      commandScript.append("'inpIDValue', inputValue(document.frmMain.inp")
          .append(getData("ColumnNameInp")).append("), ");
      commandScript.append("'WindowID', inputValue(document.frmMain.inpwindowId), ");
      commandScript.append("'inpKeyValue', inputValue(document.frmMain.inp")
          .append(getData("ColumnNameInp")).append("), ");
      commandScript.append("'inpwindowId', inputValue(document.frmMain.inpwindowId), ");
      commandScript.append("'inpProduct', inputValue(document.frmMain.inpmProductId)");
      text.append(commandScript).append(", 'Command', 'KEY'");
      commandScript.append(");");
      text.append(");");
      setOnLoad("keyArray[keyArray.length] = new keyArrayItem(\"ENTER\", \"" + text.toString()
          + "\", \"inp" + getData("ColumnNameInp") + "_R\", \"null\");");
      this.imageName = "AttributeSetInstance";
      this.searchName = getData("Name");
      this.command = commandScript.toString();
    }
  }
}
