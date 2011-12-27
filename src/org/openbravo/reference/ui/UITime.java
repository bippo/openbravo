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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;

public class UITime extends UIDate {
  public UITime(String reference, String subreference) {
    super(reference, subreference);
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {

    UIReferenceUtility.addUniqueElement(vecScript, strReplaceWith + "/js/TimeTextBox.js");
    strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
    strHtml
        .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
    strHtml.append("<tr>\n");
    strHtml.append("<td class=\"TextBox_ContentCell\">\n");
    strHtml.append("<input type=\"text\" class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
    strHtml.append("displayFormat=\"%H:%M:%S\" ");
    strHtml.append("saveFormat=\"%H:%M:%S\" ");
    strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("\" ");
    strHtml.append("maxlength=\"19\" ");
    strHtml.append("value=\"").append(fields.value).append("\" ");
    strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("\" ");

    strHtml
        .append("onkeyup=\"autoCompleteTime(this);\" onchange=\"validateTimeTextBox(this);logChanges(this);return true;\"></input> ");
    strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam")
        .append(FormatUtilities.replace(fields.columnname)).append("\") </script>");
    strHtml.append("</td>\n");
    strHtml.append("</td>\n");
    strHtml.append("</tr>\n");
    strHtml.append("</table>\n");
    strHtml.append("</td>\n");

    // "To" box
    String value = vars.getSessionValue(strTab + "|param"
        + FormatUtilities.replace(fields.columnname) + "_f");
    strHtml.append("<td class=\"TitleCell\"> <span class=\"LabelText\">");
    strHtml.append(Utility.messageBD(conn, "To", vars.getLanguage()));
    strHtml.append("</span></td>\n");

    strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
    strHtml
        .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\"  style=\"padding-top: 0px;\">\n");
    strHtml.append("<tr>\n");
    strHtml.append("<td class=\"TextBox_ContentCell\">\n");
    strHtml.append("<input type=\"text\" class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
    strHtml.append("displayFormat=\"%H:%M:%S\" ");
    strHtml.append("saveFormat=\"%H:%M:%S\" ");
    strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("_f\" ");
    strHtml.append("maxlength=\"19\" ");
    strHtml.append("value=\"").append(value).append("\" ");
    strHtml.append("id=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("_f\" ");

    strHtml
        .append("onkeyup=\"autoCompleteTime(this);\" onchange=\"validateTimeTextBox(this);logChanges(this);return true;\"></input> ");
    strHtml.append("<script>djConfig.searchIds.push(\"").append("inpParam")
        .append(FormatUtilities.replace(fields.columnname)).append("\") </script>");
    strHtml.append("</td>\n");
    strHtml.append("</td>\n");
    strHtml.append("</tr>\n");
    strHtml.append("</table>\n");
    strHtml.append("</td>\n");
  }

}
