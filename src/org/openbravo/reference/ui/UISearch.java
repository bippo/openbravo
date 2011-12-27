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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.reference.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.utils.FormatUtilities;

public class UISearch extends UITableDir {
  public UISearch(String reference, String subreference) {
    super(reference, subreference);
  }

  public String getGridType() {
    return "string";
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {

    if (subReference == null || subReference.equals("")) {
      // If not subreference work as tableDir
      reference = "19";
      super.generateFilterHtml(strHtml, vars, fields, strTab, strWindow, vecScript, vecKeys);
    } else {
      strHtml.append("<td class=\"TextBox_btn_ContentCell\" colspan=\"3\">\n");
      strHtml.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\">\n");
      UIReferenceUtility.addUniqueElement(vecScript, strReplaceWith + "/js/searchs.js");

      strHtml.append("<tr>\n<td>\n");
      strHtml.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(fields.columnname)).append("\" ");
      strHtml.append("value=\"")
          .append((!fields.value.equals("") && !fields.value.equals("%")) ? fields.value : "")
          .append("\">");
      strHtml.append("</td>\n");
      strHtml.append("<td class=\"TextBox_ContentCell\">\n");
      if (Integer.valueOf(fields.fieldlength).intValue() < (UIReferenceUtility.MAX_TEXTBOX_LENGTH / 4)) {
        strHtml
            .append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_OneCell_width\" ");
      } else if (Integer.valueOf(fields.fieldlength).intValue() < (UIReferenceUtility.MAX_TEXTBOX_LENGTH / 2)) {
        strHtml
            .append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_TwoCells_width\" ");
      } else {
        strHtml
            .append("<input dojoType=\"openbravo:ValidationTextBox\" type=\"text\" class=\"TextBox_btn_ThreeCells_width\" ");
      }
      strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields.columnname))
          .append("_DES\" ");
      strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields.columnname))
          .append("_DES\" ");
      strHtml.append("maxlength=\"").append(fields.fieldlength).append("\" ");

      if (!fields.value.equals("") && !fields.value.equals("%")) {
        String strSearchTableName = BuscadorData.selectSearchTableName(conn, fields.referencevalue);
        if (strSearchTableName.equals(""))
          strSearchTableName = fields.columnname.substring(0, fields.columnname.length() - 3);
        String strSearchName = BuscadorData.selectSearchName(conn, strSearchTableName,
            fields.value, vars.getLanguage());
        strHtml.append("value=\"").append(strSearchName).append("\" ");
      }

      strHtml.append((!fields.reference.equals("21") && !fields.reference.equals("35")) ? ""
          : "readonly=\"true\" ");
      strHtml.append("><script>djConfig.searchIds.push(\"").append("inpParam")
          .append(FormatUtilities.replace(fields.columnname)).append("_DES\") </script></td>\n");
      String strMethod = "";
      if (fields.reference.equals("21")) {
        strMethod = locationCommands(fields);
      } else if (fields.reference.equals("31")) {
        strMethod = locatorCommands(fields, false, strWindow);
      } else {
        strMethod = searchsCommand(fields, false, strTab, strWindow);
      }

      strMethod = "new keyArrayItem(\"ENTER\", \"" + strMethod + "\", \"inpParam"
          + FormatUtilities.replace(fields.columnname) + "_DES\", \"null\")";
      vecKeys.addElement(strMethod);

      if (fields.reference.equals("21")) {
        strHtml.append(location(fields));
      } else if (fields.reference.equals("31")) {
        strHtml.append(locator(fields, strWindow));
      } else {
        strHtml.append(searchs(fields, strTab, strWindow));
      }
    }
  }

  public void generateFilterAcceptScript(BuscadorData field, StringBuffer params,
      StringBuffer paramsData) {
    if (subReference == null || subReference.equals("")) {
      // If not subreference work as tableDir
      super.generateFilterAcceptScript(field, params, paramsData);
    } else {
      // Call base one
      UIReference ref = new UIReference(reference, subReference);
      ref.generateFilterAcceptScript(field, params, paramsData);
    }
  }

  private String searchsCommand(BuscadorData efd, boolean fromButton, String tabId, String windowId) {
    StringBuffer params = new StringBuffer();
    StringBuffer html = new StringBuffer();
    String strMethodName = "openSearch";
    if (!fromButton) {
      params.append(", 'Command'");
      params.append(", 'KEY'");
    }
    params.append(", 'WindowID'");
    params.append(", '").append(windowId).append("'");
    if (strIsSOTrx != null && (strIsSOTrx.equals("Y") || strIsSOTrx.equals("N"))) {
      params.append(", 'inpisSOTrxTab'");
      params.append(", '").append(strIsSOTrx).append("'");
    }
    String searchName = (efd.reference.equals("25") ? "/info/Account" : ("/info/" + (efd.reference
        .equals("800011") ? "ProductComplete" : FormatUtilities.replace(efd.searchname.trim()))))
        + ".html";
    BuscadorData[] data = null;
    try {
      data = BuscadorData.selectSearchs(conn, "I", efd.referencevalue);
    } catch (ServletException ex) {
      ex.printStackTrace();
    }
    if (data != null && data.length > 0) {
      searchName = data[0].mappingname;
    }

    if (efd.searchname.toUpperCase().startsWith("ATTRIBUTE")) {
      strMethodName = "openPAttribute";
      params.append(", 'inpKeyValue'");
      params.append(", document.frmMain.inpParam").append(FormatUtilities.replace(efd.columnname))
          .append(".value");
      params.append(", 'inpwindowId'");
      params.append(", '").append(windowId).append("'");
      params.append(", 'inpProduct'");
      params.append(", document.frmMain.inpParam").append(FormatUtilities.replace("M_Product_ID"))
          .append(".value");
      params.append(", 'inpLocatorId'");
      params.append(", ((document.frmMain.inpParam")
          .append(FormatUtilities.replace("M_Locator_ID"));
      params.append("!=null)?document.frmMain.inpParam");
      params.append(FormatUtilities.replace("M_Locator_ID")).append(".value:'')");
    }
    html.append(strMethodName).append("(null, null, '..").append(searchName)
        .append("', null, false, 'frmMain', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("_DES.value")
        .append(params.toString()).append(");");
    return html.toString();
  }

  private String locationCommands(BuscadorData efd) {
    StringBuffer html = new StringBuffer();

    html.append(
        "openLocation(null, null, '../info/Location.html', null, false, 'frmMain', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam")
        .append(FormatUtilities.replace(efd.columnname))
        .append(".value, 'inpwindowId', document.frmMain.inpwindowId.value);");
    return html.toString();
  }

  private String searchs(BuscadorData efd, String tabId, String windowId) {
    StringBuffer html = new StringBuffer();
    if (efd.searchname.toUpperCase().indexOf("BUSINESS") != -1) {
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_LOC\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_CON\">\n");
    } else if (efd.searchname.equalsIgnoreCase("PRODUCT")) {
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_PLIST\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_PSTD\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_UOM\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_PLIM\">\n");
      html.append("<input type=\"hidden\" name=\"inpParam")
          .append(FormatUtilities.replace(efd.columnname)).append("_CURR\">\n");
    }
    html.append("<td class=\"FieldButton_bg\">");
    html.append("<a href=\"#\" class=\"FieldButtonLink\" ");
    html.append("onClick=\"").append(searchsCommand(efd, true, tabId, windowId))
        .append("return false;\" ");
    html.append(
        "onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n<img width=\"16\" height=\"16\" alt=\"")
        .append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim())
        .append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_")
        .append(FormatUtilities.replace(efd.searchname.trim())).append("\" ");
    ;
    html.append("border=\"0\" src=\"").append(strReplaceWith)
        .append("/images/blank.gif\"></a></td></table>");
    return html.toString();
  }

  private String locatorCommands(BuscadorData efd, boolean fromButton, String windowId) {
    StringBuffer params = new StringBuffer();
    StringBuffer html = new StringBuffer();
    if (!fromButton) {
      params.append(", 'Command'");
      params.append(", 'KEY'");
    }
    params.append(", 'WindowID'");
    params.append(", '").append(windowId).append("'");
    html.append("openSearch(null, null, '../info/Locator.html', null, false, 'frmMain', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("', 'inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("_DES', document.frmMain.inpParam")
        .append(FormatUtilities.replace(efd.columnname)).append("_DES.value")
        .append(params.toString()).append(");");
    return html.toString();
  }

  private String locator(BuscadorData efd, String windowId) {
    StringBuffer html = new StringBuffer();
    html.append("<td class=\"FieldButton_bg\">");
    html.append("<a href=\"#\"  class=\"FieldButtonLink\" ");
    html.append("onClick=\"").append(locatorCommands(efd, true, windowId))
        .append("return false;\" ");
    html.append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n");
    ;
    html.append("<img width=\"16\" height=\"16\" alt=\"").append(efd.searchname.trim())
        .append("\" title=\"").append(efd.searchname.trim()).append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_")
        .append(FormatUtilities.replace(efd.searchname.trim())).append("\" ");
    ;
    html.append("border=\"0\" src=\"").append(strReplaceWith)
        .append("/images/blank.gif\"></a></td>");
    return html.toString();
  }

  private String location(BuscadorData efd) {
    StringBuffer html = new StringBuffer();
    html.append("<td class=\"FieldButton_bg\">");
    html.append("<a href=\"#\" class=\"FieldButtonLink\" ");
    html.append("onClick=\"").append(locationCommands(efd)).append("return false;\" ");
    html.append(
        "onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Search';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">\n<img width=\"16\" height=\"16\" alt=\"")
        .append(efd.searchname.trim()).append("\" title=\"").append(efd.searchname.trim())
        .append("\" ");
    html.append("class=\"FieldButton_Icon FieldButton_Icon_")
        .append(FormatUtilities.replace(efd.searchname.trim())).append("\" ");
    ;
    html.append("border=\"0\" src=\"").append(strReplaceWith).append("/images/blank.gif\"></a>");
    return html.toString();
  }
}
