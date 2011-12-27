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
package org.openbravo.erpCommon.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class Account extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "ALIAS", "COMBINATION", "DESCRIPTION", "AD_ORG_ID_D",
      "ACCOUNT_ID_D", "M_PRODUCT_ID_D", "C_BPARTNER_ID_D", "C_PROJECT_ID_D", "C_CAMPAIGN_ID_D",
      "C_VALIDCOMBINATION_ID", "ROWKEY" };
  private static final RequestFilter columnFilter = new ValueListFilter(colNames);
  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strWindowId = vars.getStringParameter("WindowID");
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Account.combination");
      String strAcctSchema = vars.getSessionValue(strWindowId + "|C_AcctSchema_ID");
      if (strAcctSchema.equals("")) {
        strAcctSchema = vars.getRequestGlobalVariable("inpAcctSchema", "Account.cAcctschemaId");
      }
      if (strAcctSchema.equals("")) {
        strAcctSchema = Utility.getContext(this, vars, "$C_AcctSchema_ID", "Account");
        vars.setSessionValue("Account.cAcctschemaId", strAcctSchema);
      } else
        vars.setSessionValue("$C_AcctSchema_ID", strAcctSchema);

      vars.removeSessionValue("Account.alias");
      if (!strNameValue.equals(""))
        vars.setSessionValue("Account.combination", strNameValue + "%");

      String strAlias = vars.getGlobalVariable("inpAlias", "Account.alias", "");
      String strCombination = vars.getGlobalVariable("inpCombination", "Account.combination", "");
      printPage(response, vars, strAlias, strCombination, "", strAcctSchema, true);
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      if (vars.getStringParameter("newFilter").equals("1"))
        clearSessionValues(vars);
      String strWindowId = vars.getStringParameter("WindowID");
      String strAcctSchema = vars.getSessionValue(strWindowId + "|C_AcctSchema_ID");
      if (strAcctSchema.equals("")) {
        strAcctSchema = vars.getRequestGlobalVariable("inpAcctSchema", "Account.cAcctschemaId");
      }
      if (strAcctSchema.equals("")) {
        strAcctSchema = Utility.getContext(this, vars, "$C_AcctSchema_ID", "Account");
        vars.setSessionValue("Account.cAcctschemaId", strAcctSchema);
      } else
        vars.setSessionValue("$C_AcctSchema_ID", strAcctSchema);
      String strAlias = vars.getGlobalVariable("inpAlias", "Account.alias", "");
      String strCombination = vars.getGlobalVariable("inpCombination", "Account.combination", "");
      String strOrganization = vars.getStringParameter("inpOrganization");
      String strAccount = vars.getStringParameter("inpAccount");
      String strProduct = vars.getStringParameter("inpProduct");
      String strBPartner = vars.getStringParameter("inpBPartner");
      String strProject = vars.getStringParameter("inpProject");
      String strCampaign = vars.getStringParameter("inpCampaign");
      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      printGridData(response, vars, strAlias, strCombination, strOrganization, strAccount,
          strProduct, strBPartner, strProject, strCampaign, strSortCols, strSortDirs, strOffset,
          strPageSize, strNewFilter, strAcctSchema);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Account.alias");
      String strAcctSchema = vars.getSessionValue(vars.getStringParameter("WindowID")
          + "|C_AcctSchema_ID");
      if (strAcctSchema.equals("")) {
        strAcctSchema = vars.getRequestGlobalVariable("inpAcctSchema", "Account.cAcctschemaId");
        vars.setSessionValue("Account.cAcctschemaId", strAcctSchema);
      }
      vars.removeSessionValue("Account.combination");
      vars.setSessionValue("Account.alias", strKeyValue + "%");
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      AccountData[] data = AccountData.selectKey(this, strAcctSchema,
          Utility.getContext(this, vars, "#User_Client", "Account"),
          Utility.getSelectorOrgs(this, vars, strOrg), strKeyValue + "%");
      if (data != null && data.length == 1) {
        printPageKey(response, vars, data);
      } else
        printPage(response, vars, strKeyValue + "%", "", "", strAcctSchema, true);
    } else if (vars.commandIn("SAVE")) {
      String strAcctSchema = vars
          .getRequestGlobalVariable("inpAcctSchema", "Account.cAcctschemaId");
      if (strAcctSchema.equals("")) {
        strAcctSchema = Utility.getContext(this, vars, "$C_AcctSchema_ID", "Account");
        vars.setSessionValue("Account.cAcctschemaId", strAcctSchema);
      }
      String strClave = vars.getStringParameter("inpValidCombination");
      String strAlias = vars.getRequestGlobalVariable("inpAlias", "Account.alias");
      String strOrganization = vars.getRequiredStringParameter("inpOrganization");
      String strAccount = vars.getRequiredStringParameter("inpAccount");
      String strProduct = vars.getStringParameter("inpProduct");
      String strBPartner = vars.getStringParameter("inpBPartner");
      String strProject = vars.getStringParameter("inpProject");
      String strCampaign = vars.getStringParameter("inpCampaign");
      AccountData data = AccountData.insert(this, vars.getClient(), strOrganization, strAcctSchema,
          strAccount, strClave, strAlias, vars.getUser(), strProduct, strBPartner, strProject,
          strCampaign);
      if (data != null)
        strClave = data.cValidcombinationId;
      vars.removeSessionValue("Account.alias");
      vars.setSessionValue("Account.combination", AccountData.combination(this, strClave));
      printPageSave(response, vars, data);
    } else
      pageError(response);
  }

  private void printPageSave(HttpServletResponse response, VariablesSecureApp vars, AccountData data)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Saved");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(data.combination);
    out.close();
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      AccountData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Account seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(AccountData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].cValidcombinationId + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].combination, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text, null);\n");
    html.append("}\n");
    return html.toString();
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strAlias,
      String strCombination, String strValidCombination, String strAcctSchema, boolean isDefault)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the accounts seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Account")
        .createXmlDocument();
    AccountData[] data = null;
    if (isDefault) {
      if (strAlias.equals("") && strCombination.equals(""))
        strAlias = "%";
      data = AccountData.set(strAlias, strCombination);
    } else {
      data = AccountData.select(this, "1", "", "", "", "", "", "", "", "", "", strValidCombination,
          Utility.getContext(this, vars, "#User_Client", "Account"),
          Utility.getContext(this, vars, "#User_Org", "Account"), "1 ASC", "", "");
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure1", data);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_Campaign_ID",
          "C_Campaign", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "Account"),
          Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportC_Campaign_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_Project_ID",
          "C_Project", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "Account"),
          Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportC_Project_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      // Utility.getContext(conn, vars, "#AccessibleOrgTree", windowId,
      // accesslevel)

      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "AD_Org_ID",
          "AD_Org (all)", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "Account"),
          Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "Account_ID",
          "C_ElementValue (Accounts)", "C_Accounts", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "Account"), Utility.getContext(this, vars, "#User_Client",
              "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData(
          "reportAccount_ID",
          "liststructure",
          AccountData.selectElementValues(this, vars.getLanguage(), strAcctSchema,
              Utility.getContext(this, vars, "#User_Client", "Account"),
              Utility.getContext(this, vars, "#AccessibleOrgTree", "Account")));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("orgs", vars.getStringParameter("inpAD_Org_ID"));

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");
    xmlDocument.setParameter("inpAcctSchema", strAcctSchema);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGridStructure(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page structure");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridStructure").createXmlDocument();

    SQLReturnObject[] data = getHeaders(vars);
    String type = "Hidden";
    String title = "";
    String description = "";

    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setData("structure1", data);
    xmlDocument.setParameter("backendPageSize", String.valueOf(TableSQLData.maxRowsPerGridPage));
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  private SQLReturnObject[] getHeaders(VariablesSecureApp vars) {
    SQLReturnObject[] data = null;
    Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();
    String[] colWidths = { "43", "193", "151", "105", "123", "71", "101", "43", "59", "0", "0" };
    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("ROWKEY") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("ROWKEY") ? "true" : "false"));
      dataAux.setData("isvisible", (colNames[i].endsWith("_ID") ? "false" : "true"));
      String name = Utility
          .messageBD(this, "ACCS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("ACCS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars,
      String strAlias, String strCombination, String strOrganization, String strAccount,
      String strProduct, String strBPartner, String strProject, String strCampaign,
      String strOrderCols, String strOrderDirs, String strOffset, String strPageSize,
      String strNewFilter, String strAcctSchema) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page rows");
    int page = 0;
    SQLReturnObject[] headers = getHeaders(vars);
    FieldProvider[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    int offset = Integer.valueOf(strOffset).intValue();
    int pageSize = Integer.valueOf(strPageSize).intValue();

    if (headers != null) {
      try {
        // build sql orderBy clause
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);
        page = TableSQLData.calcAndGetBackendPage(vars, "DebtPaymentInfo.currentPage");
        if (vars.getStringParameter("movePage", "").length() > 0) {
          // on movePage action force executing countRows again
          strNewFilter = "";
        }
        int oldOffset = offset;
        offset = (page * TableSQLData.maxRowsPerGridPage) + offset;
        log4j.debug("relativeOffset: " + oldOffset + " absoluteOffset: " + offset);
        if (strNewFilter.equals("1") || strNewFilter.equals("")) { // New
          // filter
          // or
          // first
          // load
          String rownum = "0", oraLimit1 = null, oraLimit2 = null, pgLimit = null;
          if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
            oraLimit1 = String.valueOf(offset + TableSQLData.maxRowsPerGridPage);
            oraLimit2 = (offset + 1) + " AND " + oraLimit1;
            rownum = "ROWNUM";
          } else {
            pgLimit = TableSQLData.maxRowsPerGridPage + " OFFSET " + offset;
          }
          strNumRows = AccountData
              .countRows(this, rownum, strAcctSchema, strAlias, strCombination, strOrganization,
                  strAccount, strProduct, strBPartner, strProject, strCampaign, "",
                  Utility.getContext(this, vars, "#User_Client", "Account"),
                  Utility.getContext(this, vars, "#User_Org", "Account"), pgLimit, oraLimit1,
                  oraLimit2);
          vars.setSessionValue("AccountInfo.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("AccountInfo.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit = (offset + 1) + " AND " + String.valueOf(offset + pageSize);
          data = AccountData.select(this, "ROWNUM", strAcctSchema, strAlias, strCombination,
              strOrganization, strAccount, strProduct, strBPartner, strProject, strCampaign, "",
              Utility.getContext(this, vars, "#User_Client", "Account"),
              Utility.getContext(this, vars, "#User_Org", "Account"), strOrderBy, oraLimit, "");
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          data = AccountData.select(this, "1", strAcctSchema, strAlias, strCombination,
              strOrganization, strAccount, strProduct, strBPartner, strProject, strCampaign, "",
              Utility.getContext(this, vars, "#User_Client", "Account"),
              Utility.getContext(this, vars, "#User_Org", "Account"), strOrderBy, "", pgLimit);
        }
      } catch (ServletException e) {
        log4j.error("Error in print page data: " + e);
        e.printStackTrace();
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorAjax(response, "Error", "Connection Error", "No database connection");
          return;
        } else {
          type = myError.getType();
          title = myError.getTitle();
          if (!myError.getMessage().startsWith("<![CDATA["))
            description = "<![CDATA[" + myError.getMessage() + "]]>";
          else
            description = myError.getMessage();
        }
      } catch (Exception e) {
        if (log4j.isDebugEnabled())
          log4j.debug("Error obtaining rows data");
        type = "Error";
        title = "Error";
        if (e.getMessage().startsWith("<![CDATA["))
          description = "<![CDATA[" + e.getMessage() + "]]>";
        else
          description = e.getMessage();
        e.printStackTrace();
      }
    }

    if (!type.startsWith("<![CDATA["))
      type = "<![CDATA[" + type + "]]>";
    if (!title.startsWith("<![CDATA["))
      title = "<![CDATA[" + title + "]]>";
    if (!description.startsWith("<![CDATA["))
      description = "<![CDATA[" + description + "]]>";
    StringBuffer strRowsData = new StringBuffer();
    strRowsData.append("<xml-data>\n");
    strRowsData.append("  <status>\n");
    strRowsData.append("    <type>").append(type).append("</type>\n");
    strRowsData.append("    <title>").append(title).append("</title>\n");
    strRowsData.append("    <description>").append(description).append("</description>\n");
    strRowsData.append("  </status>\n");
    strRowsData.append("  <rows numRows=\"").append(strNumRows)
        .append("\" backendPage=\"" + page + "\">\n");
    if (data != null && data.length > 0) {
      for (int j = 0; j < data.length; j++) {
        strRowsData.append("    <tr>\n");
        for (int k = 0; k < headers.length; k++) {
          strRowsData.append("      <td><![CDATA[");
          String columnname = headers[k].getField("columnname");

          if ((data[j].getField(columnname)) != null) {
            if (headers[k].getField("adReferenceId").equals("32"))
              strRowsData.append(strReplaceWith).append("/images/");
            strRowsData.append(data[j].getField(columnname).replaceAll("<b>", "")
                .replaceAll("<B>", "").replaceAll("</b>", "").replaceAll("</B>", "")
                .replaceAll("<i>", "").replaceAll("<I>", "").replaceAll("</i>", "")
                .replaceAll("</I>", "").replaceAll("<p>", "&nbsp;").replaceAll("<P>", "&nbsp;")
                .replaceAll("<br>", "&nbsp;").replaceAll("<BR>", "&nbsp;"));
          } else {
            if (headers[k].getField("adReferenceId").equals("32")) {
              strRowsData.append(strReplaceWith).append("/images/blank.gif");
            } else
              strRowsData.append("&nbsp;");
          }
          strRowsData.append("]]></td>\n");
        }
        strRowsData.append("    </tr>\n");
      }
    }
    strRowsData.append("  </rows>\n");
    strRowsData.append("</xml-data>\n");

    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(strRowsData.toString());
    out.print(strRowsData.toString());
    out.close();
  }

  private void clearSessionValues(VariablesSecureApp vars) {
    vars.removeSessionValue("");
  }

  public String getServletInfo() {
    return "Servlet that presents que accounts seeker";
  } // end of getServletInfo() method
}
