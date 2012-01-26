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
package org.openbravo.erpCommon.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.AttributeSetInstanceValue;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeUse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class AttributeSetInstance extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT") || vars.commandIn("KEY")) {
      String strNameValue = vars.getRequestGlobalVariable("inpKeyValue",
          "AttributeSetInstance.instance");
      String strProduct = vars.getRequestGlobalVariable("inpProduct",
          "AttributeSetInstance.product");
      vars.getRequestGlobalVariable("inpwindowId", "AttributeSetInstance.windowId");
      vars.getRequestGlobalVariable("inpLocatorId", "AttributeSetInstance.locatorId");
      vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
      String strAttributeSet = "";
      String strProductInstance = "";
      if (strNameValue.equals("") || strNameValue.equals("0")) {
        vars.setSessionValue("AttributeSetInstance.instance", "");
        AttributeSetInstanceData[] data = AttributeSetInstanceData.selectProductAttr(this,
            strProduct);
        if (data != null && data.length > 0 && !data[0].mAttributesetId.equals("")) {
          strAttributeSet = data[0].mAttributesetId;
          strProductInstance = data[0].mAttributesetinstanceId;
        }
      } else {
        strAttributeSet = AttributeSetInstanceData.selectAttributeSet(this, strNameValue);
      }
      vars.setSessionValue("AttributeSetInstance.attribute", strAttributeSet);
      vars.setSessionValue("AttributeSetInstance.productInstance", strProductInstance);
      vars.setSessionValue("AttributeSetInstance.close", "N");
      if (strAttributeSet.equals("") || strAttributeSet.equals("0"))
        advisePopUp(request, response, "INFO", Utility.messageBD(this, "Info", vars.getLanguage()),
            Utility.messageBD(this, "PAttributeNoSelection", vars.getLanguage()));
      else
        response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DISPLAY");
    } else if (vars.commandIn("DISPLAY")) {
      String strNameValue = vars.getGlobalVariable("inpInstance", "AttributeSetInstance.instance",
          "");
      String strAttributeSet = vars.getGlobalVariable("inpAttribute",
          "AttributeSetInstance.attribute");
      String strProductInstance = vars.getGlobalVariable("inpProductInstance",
          "AttributeSetInstance.productInstance", "");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "AttributeSetInstance.windowId",
          "");
      String strTabId = vars.getGlobalVariable("inpTabId", "AttributeSetInstance.tabId", "");
      String strLocator = vars.getGlobalVariable("inpLocatorId", "AttributeSetInstance.locatorId",
          "");
      String strProduct = vars.getGlobalVariable("inpProduct", "AttributeSetInstance.product", "");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      if (strIsSOTrx.equals(""))
        strIsSOTrx = "N";
      if (log4j.isDebugEnabled())
        log4j.debug("strNameValue: " + strNameValue);
      String strAttrSetValueType = "";
      OBContext.setAdminMode();
      try {
        final Product product = OBDal.getInstance().get(Product.class, strProduct);
        if (product != null) {
          strAttrSetValueType = product.getUseAttributeSetValueAs();
        }
      } finally {
        OBContext.restorePreviousMode();
      }
      if (!strAttributeSet.equals("")) {
        if ("F".equals(strAttrSetValueType))
          advisePopUp(request, response, "INFO",
              Utility.messageBD(this, "Info", vars.getLanguage()),
              Utility.messageBD(this, "AttrSetValTypeSpecification", vars.getLanguage()));
        else
          printPage(response, vars, strNameValue, strAttributeSet, strProductInstance, strWindowId,
              strTabId, strLocator, strIsSOTrx, strProduct);
      } else
        advisePopUp(request, response, "INFO", Utility.messageBD(this, "Info", vars.getLanguage()),
            Utility.messageBD(this, "PAttributeNoSelection", vars.getLanguage()));
    } else if (vars.commandIn("SAVE")) {
      String strAttributeSet = vars.getRequiredStringParameter("inpAttribute");
      String strInstance = vars.getStringParameter("inpInstance");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strProduct = vars.getRequestGlobalVariable("inpProduct",
          "AttributeSetInstance.product");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

      // Set Attributes
      AttributeSetInstanceValue attSetValue = new AttributeSetInstanceValue(
          vars.getStringParameter("inplot"), vars.getStringParameter("inpserno"),
          vars.getStringParameter("inpDateFrom"), vars.getStringParameter("inpislocked", "N"),
          vars.getStringParameter("inplockDescription"));
      AttributeSet attSet = OBDal.getInstance().get(AttributeSet.class, strAttributeSet);
      HashMap<String, String> attValues = new HashMap<String, String>();
      for (AttributeUse attrUse : attSet.getAttributeUseList()) {
        final String elementName = attrUse.getAttribute().getName();
        if (attrUse.isActive() && attrUse.getAttribute().isActive()) {
          if (attrUse.getAttribute().isMandatory()) {
            attValues.put(replace(elementName),
                vars.getRequiredStringParameter("inp" + elementName));
          } else {
            attValues.put(replace(elementName),
                vars.getStringParameter("inp" + replace(elementName)));
          }
        }
      }

      OBError myMessage = attSetValue.setAttributeInstance(this, vars, strAttributeSet,
          strInstance, strWindowId, strIsSOTrx, strProduct, attValues);
      vars.setSessionValue("AttributeSetInstance.instance", attSetValue.getAttSetInstanceId());
      vars.setSessionValue("AttributeSetInstance.attribute", strAttributeSet);
      vars.setSessionValue("AttributeSetInstance.close", "Y");
      vars.setMessage(strTabId, myMessage);
      // vars.setSessionValue("AttributeSetInstance.message", strMessage);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=DISPLAY");
    } else
      pageErrorPopUp(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strInstance,
      String strAttributeSet, String strProductInstance, String strWindowId, String strTabId,
      String strLocator, String strIsSOTrx, String strProduct) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the attributes seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/AttributeSetInstance").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("attribute", strAttributeSet);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("locatorId", strLocator);

    {
      OBError myMessage = vars.getMessage("AttributeSetInstance");
      vars.removeMessage("AttributeSetInstance");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    String message = "";
    if (vars.getSessionValue("AttributeSetInstance.close").equals("Y"))
      message = "printMessage('')";
    xmlDocument.setParameter("body", message);
    /*
     * String message = vars.getSessionValue("AttributeSetInstance.message");
     * vars.removeSessionValue("AttributeSetInstance.message"); if (!message.equals("")) { if
     * (!message.equals("OK")) message = "alert('" + message + "');"; else message =
     * "printMessage('');"; } xmlDocument.setParameter("body", message);
     */

    if (strInstance.equals("")
        && AttributeSetInstanceData.isInstanceAttribute(this, strAttributeSet).equals("0"))
      strInstance = strProductInstance;

    xmlDocument.setParameter("instance", strInstance);
    xmlDocument.setParameter("product", strProduct);
    String strName = Utility.messageBD(this, "Description", vars.getLanguage());
    xmlDocument.setParameter("nameDescription", strName.equals("") ? "Description" : strName);
    xmlDocument.setParameter("description", AttributeSetInstanceData.selectDescription(this,
        (strInstance.equals("") ? strProductInstance : strInstance)));
    AttributeSetInstanceData[] data = AttributeSetInstanceData.select(this, strAttributeSet);
    xmlDocument.setParameter(
        "data",
        generateHtml(vars, data, AttributeSetInstanceData.selectInstance(this,
            (strInstance.equals("") ? strProductInstance : strInstance)), strInstance, strIsSOTrx,
            strWindowId));
    xmlDocument.setParameter("script", generateScript(vars, data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String instanceValue(AttributeSetInstanceData[] instanceData, String strAttributeId,
      boolean isList) {
    if (instanceData == null || instanceData.length == 0)
      return "";
    for (int i = 0; i < instanceData.length; i++) {
      if (instanceData[i].mAttributeId.equals(strAttributeId)) {
        if (isList)
          return instanceData[i].mAttributevalueId;
        else
          return instanceData[i].value;
      }
    }
    return "";
  }

  private String generateScript(VariablesSecureApp vars, AttributeSetInstanceData[] fields)
      throws IOException, ServletException {
    if (fields == null || fields.length == 0)
      return "";
    StringBuffer strHtml = new StringBuffer();
    strHtml.append("function onloadFunctions() {\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");
    return strHtml.toString();
  }

  private String generateHtml(VariablesSecureApp vars, AttributeSetInstanceData[] fields,
      AttributeSetInstanceData[] instanceData, String strAttributeInstance, String strIsSOTrx,
      String strWindowId) throws IOException, ServletException {
    if (fields == null || fields.length == 0)
      return "";
    StringBuffer strHtml = new StringBuffer();
    if (!fields[0].elementname.equals("")) {
      for (int i = 0; i < fields.length; i++) {
        strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
        // String strName = Utility.messageBD(this,
        // fields[i].elementname, vars.getLanguage());
        // strHtml.append(strName.equals("")?fields[i].elementname:strName);
        strHtml.append(fields[i].elementname);
        strHtml.append("</span></td>\n");
        strHtml.append("<td class=\"LabelText\">");
        String strValue = instanceValue(instanceData, fields[i].mAttributeId,
            fields[i].islist.equals("Y"));
        if (fields[i].islist.equals("Y")) {
          strHtml.append("<select ");
          strHtml.append("name=\"inp" + replace(fields[i].elementname) + "\" ");
          strHtml.append("class=\"Combo");
          if (fields[i].ismandatory.equals("Y"))
            strHtml.append("Key");
          strHtml.append(" Combo_OneCell_width\" ");
          strHtml.append(" id=\"").append(replace(fields[i].elementname)).append("\">");
          AttributeSetInstanceData[] data = AttributeSetInstanceData.selectList(this,
              fields[i].mAttributeId);
          if (!fields[i].ismandatory.equals("Y"))
            strHtml.append("<option value=\"\"></option>\n");
          for (int j = 0; j < data.length; j++) {
            strHtml.append("<option value=\"");
            strHtml.append(data[j].value);
            strHtml.append("\" ");
            if (data[j].value.equalsIgnoreCase(strValue))
              strHtml.append("selected");
            strHtml.append(">");
            strHtml.append(data[j].name);
            strHtml.append("</option>\n");
          }
          strHtml.append("</select>");
        } else {
          strHtml.append("<textarea ");
          strHtml.append("name=\"inp" + replace(fields[i].elementname) + "\" ");
          strHtml
              .append("class=\"dojoValidateValid TextArea_OneCell_width TextArea_OneCell_height");
          if (fields[i].ismandatory.equals("Y"))
            strHtml.append(" required");
          strHtml.append("\"");
          strHtml.append(">");
          strHtml.append(strValue);
          strHtml.append("</textarea>");
        }
        strHtml.append("</td><td></td><td></td></tr>\n");
      }
    }
    if (fields[0].islot.equals("Y")) {
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "Lot", vars.getLanguage());
      strHtml.append(strName.equals("") ? "Lot" : strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td ");
      strHtml.append("class=\"TextBox_ContentCell\"><input type=\"text\" ");
      strHtml.append("name=\"inplot\" ");
      strHtml.append("maxlength=\"20\" ");
      strHtml.append("class=\"dojoValidateValid TextBox_OneCell_width");
      // strHtml.append("onkeydown=\"autoCompleteNumber(this, true, true, event);return true;\" ");
      if (!fields[0].mLotctlId.equals("") && (strIsSOTrx.equals("N") || strWindowId.equals("191"))) {
        strHtml.append(" readonly\" readonly=true ");
      } else {
        strHtml.append("\" ");
      }
      if (strAttributeInstance.equals("") && (strIsSOTrx.equals("N") || strWindowId.equals("191")))
        strHtml.append("value=\""
            + AttributeSetInstanceData.selectNextLot(this, fields[0].mLotctlId) + "\" ");
      else
        strHtml.append("value=\""
            + ((instanceData != null && instanceData.length > 0) ? instanceData[0].lot : "")
            + "\" ");
      strHtml.append("></td><td></td><td></td></tr>\n");
    }
    if (fields[0].isserno.equals("Y")) {
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "SerNo", vars.getLanguage());
      strHtml.append(strName.equals("") ? "SerNo" : strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td class=\"TextBox_ContentCell\"><input type=\"text\" ");
      strHtml.append("name=\"inpserno\" ");
      strHtml.append("maxlength=\"20\" ");
      strHtml.append("class=\"dojoValidateValid TextBox_OneCell_width");
      // strHtml.append("onkeydown=\"autoCompleteNumber(this, true, true, event);return true;\" ");
      if (!fields[0].mSernoctlId.equals("")
          && (strIsSOTrx.equals("N") || strWindowId.equals("191"))) {
        strHtml.append(" readonly\" readonly=true ");
      } else {
        strHtml.append("\" ");
      }
      if (strAttributeInstance.equals("") && (strIsSOTrx.equals("N") || strWindowId.equals("191")))
        strHtml.append("value=\""
            + AttributeSetInstanceData.selectNextSerNo(this, fields[0].mSernoctlId) + "\" ");
      else
        strHtml.append("value=\""
            + ((instanceData != null && instanceData.length > 0) ? instanceData[0].serno : "")
            + "\" ");
      strHtml.append("></td><td></td><td></td></tr>\n");
    }
    if (fields[0].isguaranteedate.equals("Y")) {
      if (log4j.isDebugEnabled())
        log4j.debug("GuaranteeDate:"
            + ((instanceData != null && instanceData.length > 0) ? instanceData[0].guaranteedate
                : ""));
      String strGuaranteeDate = null;

      if (strAttributeInstance.equals("") && (strIsSOTrx.equals("N") || strWindowId.equals("191")))
        strGuaranteeDate = DateTimeData.nDaysAfter(this, DateTimeData.today(this),
            fields[0].guaranteedays);
      else
        strGuaranteeDate = (instanceData != null && instanceData.length > 0) ? instanceData[0].guaranteedate
            : "";

      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "GuaranteeDate", vars.getLanguage());
      strHtml.append(strName.equals("") ? "GuaranteeDate" : strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td class=\"TextBox_btn_ContentCell\">");
      strHtml
          .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" summary=\"\" style=\"padding-top: 0px;\">");
      strHtml.append("<tr>");
      strHtml.append("<td class=\"TextBox_ContentCell\">");
      strHtml
          .append("<input dojoType=\"openbravo:DateTextbox\" displayFormat=\""
              + vars.getSessionValue("#AD_SqlDateFormat")
              + "\" saveFormat=\""
              + vars.getSessionValue("#AD_SqlDateFormat")
              + "\" class=\"TextBox_btn_OneCell_width\"  type=\"text\" name=\"inpDateFrom\" id=\"paramDateFrom\" size=\"10\" maxlength=\"10\" value=\""
              + strGuaranteeDate
              + "\" onkeyup=\"autoCompleteDate(this.textbox, this.displayFormat);return true;\"></input><script>djConfig.searchIds.push(\"paramDateFrom\");</script>");
      strHtml.append("</td>");
      strHtml.append("<td class=\"FieldButton_ContentCell\">");
      strHtml.append("<a class=\"FieldButtonLink\" href=\"#\"");
      strHtml
          .append("onfocus=\"this.className='FieldButtonLink_hover'; window.status='Show calendar'; return true;\"");
      strHtml.append("onblur=\"this.className='FieldButtonLink'; window.status=''; return true;\"");
      strHtml.append("onkeypress=\"this.className='FieldButtonLink_active'; return true;\"");
      strHtml.append("onkeyup=\"this.className='FieldButtonLink_hover'; return true;\"");
      strHtml.append("onClick=\"showCalendar('frmMain.");
      strHtml.append("inpDateFrom', ");
      strHtml.append("document.frmMain.inpDateFrom.value, false);return false;\">");
      strHtml.append("<table class=\"FieldButton\"");
      strHtml.append("onmousedown=\"this.className='FieldButton_active'; return true;\"");
      strHtml.append("onmouseup=\"this.className='FieldButton'; return true;\"");
      strHtml
          .append("onmouseover=\"this.className='FieldButton_hover'; window.status='Show calendar'; return true;\"");
      strHtml
          .append("onmouseout=\"this.className='FieldButton'; window.status=''; return true;\">");
      strHtml.append("<tr>");
      strHtml.append("<td class=\"FieldButton_bg\">");
      strHtml
          .append(
              "<img alt=\"Calendar\" class=\"FieldButton_Icon FieldButton_Icon_Calendar\" title=\"Calendar\" src=\"")
          .append(strReplaceWith).append("/images/blank.gif\" border=\"0\"\"/>");
      strHtml.append("</td>");
      strHtml.append("</tr>");
      strHtml.append("</table>");
      strHtml.append("</a>");
      strHtml.append("</td>");
      strHtml.append("</tr>");
      strHtml.append("</table>");
      strHtml.append("</td><td></td><td></td>");
      strHtml.append("</tr>");
    }
    if (fields[0].islockable.equals("Y")) {
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      String strName = Utility.messageBD(this, "IsLocked", vars.getLanguage());
      strHtml.append(strName.equals("") ? "IsLocked" : strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td class=\"Radio_Check_ContentCell\"><input type=\"checkbox\" ");
      strHtml.append("name=\"inpislocked\" ");
      strHtml.append("value=\"Y\" ");
      if (instanceData != null && instanceData.length > 0 && instanceData[0].islocked.equals("Y"))
        strHtml.append("checked ");
      strHtml.append("></td></tr>\n");
      strHtml.append("<tr><td class=\"TitleCell\"><span class=\"LabelText\">");
      strName = Utility.messageBD(this, "LockDescription", vars.getLanguage());
      strHtml.append(strName.equals("") ? "LockDescription" : strName);
      strHtml.append("</span></td>\n");
      strHtml.append("<td>");
      strHtml.append("<textarea ");
      strHtml.append("name=\"inplockDescription\" ");
      strHtml.append("class=\"dojoValidateValid TextArea_OneCell_width TextArea_OneCell_height");
      strHtml.append("\">");
      if (instanceData != null && instanceData.length > 0)
        strHtml.append(instanceData[0].lockDescription);
      strHtml.append("</textarea>");
      strHtml.append("</td><td></td><td></td></tr>\n");
    }
    return strHtml.toString();
  }

  private String replace(String strIni) {
    // delete characters: " ","&",","
    return Replace.replace(Replace.replace(Replace.replace(
        Replace.replace(Replace.replace(Replace.replace(strIni, "#", ""), " ", ""), "&", ""), ",",
        ""), "(", ""), ")", "");
  }

  public String getServletInfo() {
    return "Servlet that presents attributes seeker";
  } // end of getServletInfo() method
}
