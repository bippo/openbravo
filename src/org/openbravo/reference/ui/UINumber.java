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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;

public class UINumber extends UIReference {
  protected String relationFormat;

  public UINumber(String reference, String subreference) {
    super(reference, subreference);
    addSecondaryFilter = true;
    relationFormat = "euroRelation";
    numeric = true;
  }

  public String getGridType() {
    return "float";
  }

  public String formatGridValue(VariablesSecureApp vars, String value) {
    String rt = value;
    try {
      DecimalFormat numberFormatDecimal = Utility.getFormat(vars, relationFormat);
      if (numberFormatDecimal != null) {
        rt = numberFormatDecimal.format(new BigDecimal(value));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return rt;
  }

  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars,
      BuscadorData fields, String strTab, String strWindow, ArrayList<String> vecScript,
      Vector<Object> vecKeys) throws IOException, ServletException {

    Random rnd = new Random();
    int randomId4Num2 = rnd.nextInt(10000);
    int randomId4Num3 = rnd.nextInt(10000);

    strHtml.append("<td class=\"TextBox_ContentCell\">");
    strHtml.append("<input type=\"text\" class=\"dojoValidateValid TextBox_OneCell_width\" ");
    strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("\" ");
    strHtml.append("maxlength=\"").append(fields.fieldlength).append("\" ");
    strHtml.append("value=\"").append(fields.value).append("\" ");

    UIReferenceUtility.addUniqueElement(vecScript, strReplaceWith + "/js/calculator.js");
    strHtml
        .append("outputformat=\"qtyEdition\" ")
        .append(
            "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ");
    strHtml.append("id=\"").append(randomId4Num2).append("\" ");
    strHtml.append(">");
    strHtml.append("</td>");

    // "To" box
    String value = vars.getSessionValue(strTab + "|param"
        + FormatUtilities.replace(fields.columnname) + "_f");
    strHtml.append("<td class=\"TitleCell\"> <span class=\"LabelText\">");
    strHtml.append(Utility.messageBD(conn, "To", vars.getLanguage()));
    strHtml.append("</span></td>\n");

    strHtml.append("<td class=\"TextBox_btn_ContentCell\">\n");
    strHtml
        .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"\">\n");
    strHtml.append("<tr>");
    strHtml.append("<td class=\"TextBox_ContentCell\">");
    strHtml.append("<input type=\"text\" ");
    strHtml.append("class=\"dojoValidateValid TextBox_btn_OneCell_width\" ");
    strHtml.append("name=\"inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("_f\" ");
    strHtml.append("maxlength=\"").append(fields.fieldlength).append("\" ");
    strHtml.append("value=\"").append(value).append("\" ");
    strHtml
        .append("outputformat=\"qtyEdition\" ")
        .append(
            "onfocus=\"numberInputEvent('onfocus', this);\" onblur=\"numberInputEvent('onblur', this);\" onkeydown=\"numberInputEvent('onkeydown', this, event);\" onchange=\"numberInputEvent('onchange', this);\" ")
        .append("id=\"").append(randomId4Num3).append("\" ");
    strHtml.append(">");

    strHtml
        .append("<td class=\"FieldButton_ContentCell\">\n<table class=\"FieldButton\" onclick=\"calculator('frmMain.");
    strHtml.append("inpParam").append(FormatUtilities.replace(fields.columnname)).append("_f', ");
    strHtml.append("document.frmMain.inpParam").append(FormatUtilities.replace(fields.columnname))
        .append("_f.value, false);return false;\" ");
    strHtml
        .append("onmouseout=\"this.className='FieldButton';window.status='';return true;\" onmouseover=\"this.className='FieldButton_hover';window.status='Show calculator';return true;\" onmousedown=\"this.className='FieldButton_active';return true;\" onmouseup=\"this.className='FieldButton';return true;\">");
    strHtml.append("<tr>\n<td class=\"FieldButton_bg\">\n");
    strHtml
        .append(
            "<img alt=\"Calculator\" class=\"FieldButton_Icon FieldButton_Icon_Calc\" title=\"Calculator\" src=\"")
        .append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></img>\n");
    strHtml.append("</td>\n</tr>\n</table>\n</td>\n</tr>\n</table>\n");
    strHtml.append("<span class=\"invalid\" style=\"display: none;\" id=\"").append(randomId4Num3)
        .append("invalidSpan\">* The value entered is not valid.</span>");
    strHtml.append("<span class=\"missing\" style=\"display: none;\" id=\"").append(randomId4Num3)
        .append("missingSpan\">* This value is required.</span>");
    strHtml
        .append("<span class=\"range\" style=\"display: none;\">* This value is out of range.</span>");
    strHtml.append("</td>");
  }
}
