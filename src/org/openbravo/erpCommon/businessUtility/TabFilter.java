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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class TabFilter extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final RequestFilter columnNameFilter = new RequestFilter() {
    @Override
    public boolean accept(String value) {
      for (int i = 0; i < value.length(); i++) {
        int c = value.codePointAt(i);
        if (Character.isLetter(c) || Character.isDigit(c) || value.charAt(i) == '_') {
          return true;
        }
      }
      return false;
    }
  };

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strTab = vars.getRequiredStringParameter("inpTabId");
      String strWindow = vars.getRequiredStringParameter("inpWindow");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strOrderBy = vars.getSessionValue(strTab + "|orderby");
      printPage(response, vars, strTab, strWindow, strWindowId, strOrderBy);
    } else if (vars.commandIn("ORDERBY", "ORDERBY_CLEAR")) {
      String strOrderBy = vars.getInStringParameter("inpSelectedField", columnNameFilter);
      String strTab = vars.getStringParameter("inpTabId");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath = "";
      if (tab != null && tab.length != 0) {
        if (tab[0].help.equals("Y"))
          strWindowPath = "../utility/WindowTree_FS.html?inpTabId=" + strTab;
        strWindowPath = Utility.getTabURL(strTab, "R", true);
      } else
        strWindowPath = strDefaultServlet;
      if (strOrderBy != null && strOrderBy.length() > 0) {
        strOrderBy = strOrderBy.substring(1, strOrderBy.length() - 1);
        {
          StringTokenizer st = new StringTokenizer(strOrderBy, ",", false);
          strOrderBy = "";
          while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (!strOrderBy.equals(""))
              strOrderBy += ",";
            if (token.startsWith("'"))
              strOrderBy += token.substring(1, token.length() - 1).trim();
          }
        }
      }
      if (vars.commandIn("ORDERBY") && !strOrderBy.equals(""))
        vars.setSessionValue(strTab + "|orderby", strOrderBy);
      else
        vars.removeSessionValue(strTab + "|orderby");
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTab,
      String strWindow, String strWindowId, String strOrderBy) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: 'order by' selector");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/businessUtility/TabFilter").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("windowId", strWindowId);

    TabFilterData[] data = null, dataOrderBy = null;
    if (vars.getLanguage().equals("en_US"))
      data = TabFilterData.select(this, strTab);
    else
      data = TabFilterData.selectTrl(this, vars.getLanguage(), strTab);

    if (data != null && data.length > 0) {
      data = getShownFieldsData(data);
      if (data != null && data.length > 0 && !strOrderBy.equals("")) {
        dataOrderBy = getOrderByFields(data, strOrderBy);
        data = getShownFieldsDataWithoutOrderBy(data, dataOrderBy);
      }
    }
    xmlDocument.setData("structure1", data);
    xmlDocument.setData("structure2", dataOrderBy);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private boolean isHasDescriptionReference(String reference) {
    if ("17".equals(reference) || "18".equals(reference) || "19".equals(reference)
        || "21".equals(reference) || "25".equals(reference) || "28".equals(reference)
        || "30".equals(reference) || "31".equals(reference) || "32".equals(reference)
        || "35".equals(reference) || "800011".equals(reference)) {
      return true;
    }
    return false;
  }

  private TabFilterData[] getShownFieldsData(TabFilterData[] data) {
    int contador = 1;
    Vector<Object> fields = new Vector<Object>();
    for (int i = 0; i < data.length; i++) {
      if (mustBeCount(data[i].columnname)) {
        if (isHasDescriptionReference(data[i].adReferenceId) && data[i].isdisplayed.equals("Y"))
          contador++;
        if (data[i].showinrelation.equals("Y") && data[i].isdisplayed.equals("Y")) {
          TabFilterData dataAux = new TabFilterData();
          dataAux.position = Integer.toString(contador);
          dataAux.name = data[i].name;
          fields.addElement(dataAux);
        }
        contador++;
      }
    }
    TabFilterData[] result = new TabFilterData[fields.size()];
    fields.copyInto(result);
    return result;
  }

  private boolean mustBeCount(String columnname) {
    if (!columnname.equalsIgnoreCase("Created") && !columnname.equalsIgnoreCase("CreatedBy")
        && !columnname.equalsIgnoreCase("Updated") && !columnname.equalsIgnoreCase("UpdatedBy"))
      return true;
    else
      return false;
  }

  private TabFilterData[] getOrderByFields(TabFilterData[] data, String strOrderBy) {
    strOrderBy = strOrderBy.trim();
    if (strOrderBy.startsWith("("))
      strOrderBy = strOrderBy.substring(1, strOrderBy.length() - 1);
    if (log4j.isDebugEnabled())
      log4j.debug("TabFilter - getOrderByFields() - String to parse: " + strOrderBy);
    StringTokenizer orderBy = new StringTokenizer(strOrderBy, ",");
    Vector<Object> fields = new Vector<Object>();
    boolean isnegative = false;
    while (orderBy.hasMoreTokens()) {
      String token = orderBy.nextToken();
      token = token.trim();
      if (token.startsWith("'"))
        token = token.substring(1, token.length() - 1);
      String realToken = token;
      if (token.startsWith("-")) {
        token = token.substring(1);
        isnegative = true;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("TabFilter - getOrderByFields() - token to parse: " + token + " - real token: "
            + realToken);
      for (int i = 0; i < data.length; i++) {
        if (data[i].position.equals(token)) {
          TabFilterData dataAux = new TabFilterData();
          dataAux.position = data[i].position;
          dataAux.completePosition = (isnegative ? "-" : "") + data[i].position;
          dataAux.name = data[i].name;
          dataAux.completeName = (!token.equals(realToken) ? "\\/" : "/\\") + data[i].name;
          fields.addElement(dataAux);
          break;
        }
      }
    }
    TabFilterData[] result = new TabFilterData[fields.size()];
    fields.copyInto(result);
    return result;
  }

  private TabFilterData[] getShownFieldsDataWithoutOrderBy(TabFilterData[] data,
      TabFilterData[] dataOrderBy) {
    Vector<Object> fields = new Vector<Object>();
    boolean exists = false;
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < dataOrderBy.length; j++) {
        exists = false;
        if (data[i].position.equals(dataOrderBy[j].position)) {
          exists = true;
          break;
        }
      }
      if (!exists)
        fields.addElement(data[i]);
    }
    TabFilterData[] result = new TabFilterData[fields.size()];
    fields.copyInto(result);
    return result;
  }

  public String getServletInfo() {
    return "Servlet that presents the 'order by' selector";
  } // end of getServletInfo() method
}
