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
package org.openbravo.erpCommon.businessUtility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.reference.Reference;
import org.openbravo.reference.ui.UIReference;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class Buscador extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final int MAX_TEXTBOX_DISPLAY = 30;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      vars.setSessionValue("Buscador.inpTabId", vars.getRequiredStringParameter("inpTabId"));
      vars.setSessionValue("Buscador.inpWindow", vars.getRequiredStringParameter("inpWindow"));
      vars.setSessionValue("Buscador.inpWindowId", vars.getStringParameter("inpWindowId"));

      printPageFS(response, vars);
    }

    if (vars.commandIn("FRAME1")) {
      String strTab = vars.getSessionValue("Buscador.inpTabId");
      String strWindow = vars.getSessionValue("Buscador.inpWindow");
      String strWindowId = vars.getSessionValue("Buscador.inpWindowId");
      String strIsSOTrx = vars.getSessionValue(strWindowId + "|issotrxtab");
      String strShowAudit = Utility.getContext(this, vars, "ShowAudit", strWindowId);
      BuscadorData[] data;

      // assumption Buscador servlet is only called from generated windows
      // get url path working on windows in core & modules and use this instead
      // of the incoming window path
      // always use _Relation form url as it matches old behavior in ToolBar.java and
      // both views are mapped to the same servlet
      strWindow = Utility.getTabURL(strTab, "R", true);

      if (!BuscadorData.hasSelectionColumns(this, strTab).equals("0"))
        data = BuscadorData.select(this, vars.getLanguage(), strTab, strShowAudit);
      else
        data = BuscadorData.selectIdentifiers(this, vars.getLanguage(), strTab, strShowAudit);

      if (data == null || data.length == 0) {
        if (log4j.isDebugEnabled())
          log4j.debug("there're no selection columns and no identifiers defined for this table");
        bdError(request, response, "SearchNothing", vars.getLanguage());
      } else {
        data = removeParents(data, strTab);
        if (loadParameters(vars, data, strTab)) {
          for (int i = 0; i < data.length; i++) {
            if (data[i].reference.equals("10") || data[i].reference.equals("14")
                || data[i].reference.equals("34"))
              data[i].value = "%";
            else
              data[i].value = "";
          }
        }
        if (data == null || data.length == 0) {
          if (log4j.isDebugEnabled())
            log4j.debug("The columns defined were parent keys");
          advisePopUp(request, response, "SearchNothing",
              Utility.messageBD(this, "SearchNothing", vars.getLanguage()));
        } else
          printPage(response, vars, strTab, data, strWindow, strWindowId, strIsSOTrx);
      }
    } else
      pageError(response);
  }

  private void printPageFS(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/Buscador_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private BuscadorData[] removeParents(BuscadorData[] data, String strTab) throws ServletException {
    String parentColumn = BuscadorData.parentsColumnName(this, strTab);
    if (data == null || data.length == 0)
      return data;
    if (parentColumn.equals(""))
      return data;
    Vector<Object> vec = new Vector<Object>();
    BuscadorData[] result = null;
    for (int i = 0; i < data.length; i++) {
      if (!parentColumn.equalsIgnoreCase(data[i].columnname)
          || data[i].isselectioncolumn.equals("Y"))
        vec.addElement(data[i]);
    }
    if (vec.size() > 0) {
      result = new BuscadorData[vec.size()];
      vec.copyInto(result);
    }
    return result;
  }

  private boolean loadParameters(VariablesSecureApp vars, BuscadorData[] data, String strTab)
      throws ServletException {
    if (data == null || data.length == 0)
      return false;
    boolean isEmpty = true;
    for (int i = 0; i < data.length; i++) {
      data[i].value = vars.getSessionValue(strTab + "|param"
          + FormatUtilities.replace(data[i].columnname));
      if (!data[i].value.equals(""))
        isEmpty = false;
    }
    return isEmpty;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTab,
      BuscadorData[] data, String strWindow, String strWindowId, String strIsSOTrx)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the attributes seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/Buscador").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    StringBuffer script = new StringBuffer();
    Vector<StringBuffer> vecScript = new Vector<StringBuffer>();
    xmlDocument.setParameter("data",
        generateHtml(vars, data, strTab, strWindowId, script, strIsSOTrx, vecScript));
    xmlDocument.setParameter("scripsJS", vecScript.elementAt(0).toString());
    xmlDocument.setParameter("script",
        (script.toString() + generateScript(data, strWindow, strTab)));
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("window", strWindow);
    {
      OBError myMessage = vars.getMessage(strTab);
      vars.removeMessage(strTab);
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateScript(BuscadorData[] fields, String strWindow, String strTab) {
    StringBuffer strHtml = new StringBuffer();
    strHtml.append("function aceptar() {\n");
    strHtml.append("  var frm = document.forms[0];\n");
    strHtml.append("  var paramsData = new Array();\n");
    strHtml.append("  var count = 0;\n");

    StringBuffer paramsData = new StringBuffer();
    StringBuffer params = new StringBuffer();
    for (BuscadorData field : fields) {
      UIReference reference = Reference.getUIReference(field.reference, field.referencevalue);
      reference.generateFilterAcceptScript(field, params, paramsData);
    }
    strHtml.append("\n").append(paramsData);
    strHtml
        .append("  if (parent.window.opener.selectFilters) parent.window.opener.selectFilters(paramsData);\n");
    strHtml.append("  else parent.window.opener.submitFormGetParams(\"SEARCH\", \"")
        .append(strWindow).append("\"").append(params).append(");\n");
    strHtml.append("  parent.window.close();\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");

    strHtml.append("\nfunction reloadComboReloads(changedField) {\n");
    strHtml.append("  submitCommandForm(changedField, false, null, '../ad_callouts/ComboReloads"
        + strTab + ".html', 'hiddenFrame', null, null, true);\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");

    strHtml.append("function onloadFunctions() {\n");
    strHtml.append("  enableLocalShortcuts();\n");
    strHtml.append("  return true;\n");
    strHtml.append("}\n");
    return strHtml.toString();
  }

  private String generateHtml(VariablesSecureApp vars, BuscadorData[] fields, String strTab,
      String strWindow, StringBuffer script, String strIsSOTrx, Vector<StringBuffer> vecScript)
      throws IOException, ServletException {
    if (fields == null || fields.length == 0)
      return "";
    StringBuffer strHtml = new StringBuffer();
    Vector<Object> vecKeys = new Vector<Object>();
    ArrayList<String> listScript = new ArrayList<String>();

    // store in session all the fields in the pup up, to be used when loading session parameters
    StringBuffer strAllFields = new StringBuffer();
    for (BuscadorData field : fields) {
      strAllFields.append("|").append(field.columnname).append("|");
    }
    vars.setSessionValue("buscador.searchFilds", strAllFields.toString());

    for (BuscadorData field : fields) {
      UIReference reference = Reference.getUIReference(field.reference, field.referencevalue);
      reference.setReplaceWith(strReplaceWith);
      reference.setStrIsSOTrx(strIsSOTrx);

      if (Integer.valueOf(field.displaylength).intValue() > MAX_TEXTBOX_DISPLAY) {
        field.displaylength = Integer.toString(MAX_TEXTBOX_DISPLAY);
      }
      strHtml.append("<tr><td class=\"TitleCell\"> <span class=\"LabelText\">");
      if (reference.hasSecondaryFilter()) {
        strHtml.append(field.name).append(" ")
            .append(Utility.messageBD(this, "From", vars.getLanguage()));
      } else {
        strHtml.append(field.name);
      }
      strHtml.append("</span></td>\n");
      reference.generateFilterHtml(strHtml, vars, field, strTab, strWindow, listScript, vecKeys);
      strHtml.append("</tr>\n");
    }

    script.append("\nfunction enableLocalShortcuts() {\n");
    if (vecKeys.size() > 0) {
      for (int i = 0; i < vecKeys.size(); i++) {
        script.append("  keyArray[keyArray.length] = ").append(vecKeys.elementAt(i).toString())
            .append(";\n");
      }
    } else {
      script.append("\n");
    }
    script.append("}\n");

    StringBuffer scrScr = new StringBuffer();
    for (String js : listScript) {
      scrScr.append("<script language=\"JavaScript\" src=\"").append(js)
          .append("\" type=\"text/javascript\"></script>");
    }
    vecScript.addElement(scrScr);
    return strHtml.toString();
  }

  public String getServletInfo() {
    return "Servlet to render the search popup";
  }
}
