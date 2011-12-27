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
import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class BusinessPartner extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "value", "name", "so_creditavailable",
      "so_creditused", "contact", "phone", "pc", "city", "income", "c_bpartner_id",
      "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey" };
  private static final RequestFilter columnFilter = new ValueListFilter(colNames);
  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      clearSessionValue(vars);

      final String strWindowId = vars.getStringParameter("WindowID");
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "BusinessPartner.name");
      final String strIDValue = vars.getStringParameter("inpIDValue");
      final String strKeyValue = vars.getGlobalVariable("inpKey", "BusinessPartner.key", "");
      vars.setSessionValue("BusinessPartner.adorgid", vars.getStringParameter("inpAD_Org_ID", ""));
      if (!strIDValue.equals("")) {
        final String strNameAux = BusinessPartnerData.existsActual(this, strNameValue, strIDValue);
        if (!strNameAux.equals(""))
          strNameValue = strNameAux;
      }

      vars.removeSessionValue("BusinessPartner.key");

      final String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String strBpartner = strIsSOTrxTab;
      if (strIsSOTrxTab.equals(""))
        strBpartner = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strSelected = "all";
      if (strBpartner.equals("Y"))
        strSelected = "customer";
      else if (strBpartner.equals("N"))
        strSelected = "vendor";
      else
        strSelected = "all";
      vars.setSessionValue("BusinessPartner.bpartner", strSelected);
      if (!strNameValue.equals(""))
        vars.setSessionValue("BusinessPartner.name", strNameValue + "%");
      printPage(response, vars, strKeyValue, strNameValue.concat("%"), strSelected, "paramName");
    } else if (vars.commandIn("KEY")) {
      final String strWindowId = vars.getStringParameter("WindowID");
      final String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "BusinessPartner.key");
      final String strIDValue = vars.getStringParameter("inpIDValue");
      // getGlobalVariable only used to store request value into session, not to read it from there
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "BusinessPartner.adorgid", "");
      if (!strIDValue.equals("")) {
        final String strNameAux = BusinessPartnerData.existsActualValue(this, strKeyValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strKeyValue = strNameAux;
      }
      vars.removeSessionValue("BusinessPartner.name");
      if (!strKeyValue.equals(""))
        vars.setSessionValue("BusinessPartner.key", strKeyValue + "%");
      String strBpartner = strIsSOTrxTab;
      if (strIsSOTrxTab.equals(""))
        strBpartner = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strSelected = "all";
      if (strBpartner.equals("Y"))
        strSelected = "customer";
      else if (strBpartner.equals("N"))
        strSelected = "vendor";
      else
        strSelected = "all";
      vars.setSessionValue("BusinessPartner.bpartner", strSelected);

      // two cases are interesting in the result:
      // - exactly one row (and row content is needed)
      // - zero or more than one row (row content not needed)
      // so limit <= 2 records to get both info from result without needing to fetch all rows
      String rownum = "0", oraLimit1 = null, pgLimit = null;
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        oraLimit1 = "2";
      } else {
        pgLimit = "2";
      }

      final BusinessPartnerData[] data = BusinessPartnerData.selectKey(this, Utility.getContext(
          this, vars, "#User_Client", "BusinessPartner"), Utility.getSelectorOrgs(this, vars,
          strOrg), (strSelected.equals("customer") ? "clients" : ""),
          (strSelected.equals("vendor") ? "vendors" : ""), strKeyValue + "%", pgLimit, oraLimit1);
      if (data != null && data.length == 1) {
        printPageKey(response, vars, data);
      } else
        printPage(response, vars, strKeyValue + "%", "", strSelected, "paramKey");
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      if (vars.getStringParameter("clear").equals("true")) {
        clearSessionValue(vars);
      }
      final String strKey = vars.getGlobalVariable("inpKey", "BusinessPartner.key", "");
      final String strName = vars.getGlobalVariable("inpName", "BusinessPartner.name", "");
      final String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "BusinessPartner.adorgid", "");
      final String strContact = vars.getGlobalVariable("inpContact", "BusinessPartner.contact", "");
      final String strZIP = vars.getGlobalVariable("inpZIP", "BusinessPartner.zip", "");
      final String strProvincia = vars.getGlobalVariable("inpProvincia",
          "BusinessPartner.provincia", "");
      final String strBpartners = vars.getGlobalVariable("inpBpartner", "BusinessPartner.bpartner",
          "all"); // all
      final String strCity = vars.getGlobalVariable("inpCity", "BusinessPartner.city", "");
      final String strNewFilter = vars.getStringParameter("newFilter");
      final String strOffset = vars.getStringParameter("offset");
      final String strPageSize = vars.getStringParameter("page_size");
      final String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      final String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      printGridData(response, vars, strKey, strName, strOrg, strContact, strZIP, strProvincia,
          strBpartners, strCity, strSortCols, strSortDirs, strOffset, strPageSize, strNewFilter);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue,
      String strNameValue, String strBpartners, String focusedId) throws IOException,
      ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of business partners seeker");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/BusinessPartner").createXmlDocument();
    if (strKeyValue.equals("") && strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("name", strNameValue);
    xmlDocument.setParameter("clients", strBpartners);
    xmlDocument.setParameter("vendors", strBpartners);
    xmlDocument.setParameter("all", strBpartners);
    xmlDocument.setParameter("orgs", vars.getStringParameter("inpAD_Org_ID"));

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    xmlDocument.setParameter("jsFocusOnField", Utility.focusFieldJS(focusedId));

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      BusinessPartnerData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: business partners seeker Frame Set");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(BusinessPartnerData[] data) throws IOException, ServletException {
    final StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].cBpartnerId + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
    html.append("var parameter = new Array(\n");
    html.append("new SearchElements(\"_LOC\", true, \"" + data[0].cBpartnerLocationId + "\"),\n");
    html.append("new SearchElements(\"_CON\", true, \"" + data[0].cBpartnerContactId + "\")\n");
    html.append(");\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text, parameter);\n");
    html.append("}\n");
    return html.toString();
  }

  private void printGridStructure(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page structure");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridStructure").createXmlDocument();

    final SQLReturnObject[] data = getHeaders(vars);
    final String type = "Hidden";
    final String title = "";
    final String description = "";

    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setData("structure1", data);
    xmlDocument.setParameter("backendPageSize", String.valueOf(TableSQLData.maxRowsPerGridPage));
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    final PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  private SQLReturnObject[] getHeaders(VariablesSecureApp vars) {
    SQLReturnObject[] data = null;
    final Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();
    final String[] colWidths = { "98", "172", "50", "83", "104", "63", "43", "100", "63", "0", "0",
        "0", "0" };
    for (int i = 0; i < colNames.length; i++) {
      final SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("isvisible",
          (colNames[i].endsWith("_id") || colNames[i].equals("rowkey") ? "false" : "true"));
      final String name = Utility.messageBD(this, "BPS_" + colNames[i].toUpperCase(),
          vars.getLanguage());
      dataAux.setData("name", (name.startsWith("BPS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strName, String strOrg, String strContact, String strZIP, String strProvincia,
      String strBpartners, String strCity, String strOrderCols, String strOrderDirs,
      String strOffset, String strPageSize, String strNewFilter) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page rows");
    int page = 0;
    final SQLReturnObject[] headers = getHeaders(vars);
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
        // New filter or first load
        if (strNewFilter.equals("1") || strNewFilter.equals("")) {
          String rownum = "0", oraLimit1 = null, oraLimit2 = null, pgLimit = null;
          if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
            oraLimit1 = String.valueOf(offset + TableSQLData.maxRowsPerGridPage);
            oraLimit2 = (offset + 1) + " AND " + oraLimit1;
            rownum = "ROWNUM";
          } else {
            pgLimit = TableSQLData.maxRowsPerGridPage + " OFFSET " + offset;
          }
          strNumRows = BusinessPartnerData.countRows(this, rownum,
              Utility.getContext(this, vars, "#User_Client", "BusinessPartner"),
              Utility.getSelectorOrgs(this, vars, strOrg), strKey, strName, strContact, strZIP,
              strProvincia, (strBpartners.equals("customer") ? "clients" : ""),
              (strBpartners.equals("vendor") ? "vendors" : ""), strCity, pgLimit, oraLimit1,
              oraLimit2);
          vars.setSessionValue("BusinessPartnerInfo.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("BusinessPartnerInfo.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit1 = String.valueOf(offset + pageSize);
          String oraLimit2 = (offset + 1) + " AND " + oraLimit1;
          data = BusinessPartnerData.select(this, "ROWNUM",
              Utility.getContext(this, vars, "#User_Client", "BusinessPartner"),
              Utility.getSelectorOrgs(this, vars, strOrg), strKey, strName, strContact, strZIP,
              strProvincia, (strBpartners.equals("customer") ? "clients" : ""),
              (strBpartners.equals("vendor") ? "vendors" : ""), strCity, strOrderBy, "", oraLimit1,
              oraLimit2);
        } else {
          final String pgLimit = pageSize + " OFFSET " + offset;
          data = BusinessPartnerData.select(this, "1",
              Utility.getContext(this, vars, "#User_Client", "BusinessPartner"),
              Utility.getSelectorOrgs(this, vars, strOrg), strKey, strName, strContact, strZIP,
              strProvincia, (strBpartners.equals("customer") ? "clients" : ""),
              (strBpartners.equals("vendor") ? "vendors" : ""), strCity, strOrderBy, pgLimit, "",
              "");
        }
      } catch (final ServletException e) {
        log4j.error("Error in print page data: " + e);
        e.printStackTrace();
        final OBError myError = Utility.translateError(this, vars, vars.getLanguage(),
            e.getMessage());
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
      } catch (final Exception e) {
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

    DecimalFormat df = Utility.getFormat(vars, "priceEdition");

    if (!type.startsWith("<![CDATA["))
      type = "<![CDATA[" + type + "]]>";
    if (!title.startsWith("<![CDATA["))
      title = "<![CDATA[" + title + "]]>";
    if (!description.startsWith("<![CDATA["))
      description = "<![CDATA[" + description + "]]>";
    final StringBuffer strRowsData = new StringBuffer();
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
          final String columnname = headers[k].getField("columnname");

          if (columnname.equalsIgnoreCase("so_creditavailable")
              || columnname.equalsIgnoreCase("so_creditused")
              || columnname.equalsIgnoreCase("income")) {
            final String value = data[j].getField(columnname).equals("") ? "0" : data[j]
                .getField(columnname);
            strRowsData.append(df.format(new BigDecimal(value)));
          } else if ((data[j].getField(columnname)) != null) {
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
    final PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(strRowsData.toString());
    out.print(strRowsData.toString());
    out.close();
  }

  private void clearSessionValue(VariablesSecureApp vars) {
    vars.removeSessionValue("BusinessPartner.key");
    vars.removeSessionValue("BusinessPartner.name");
    vars.removeSessionValue("BusinessPartner.contact");
    vars.removeSessionValue("BusinessPartner.zip");
    vars.removeSessionValue("BusinessPartner.provincia");
    vars.removeSessionValue("BusinessPartner.bpartner");
    vars.removeSessionValue("BusinessPartner.city");

    // remove saved adorgid only when called from DEFAULT,KEY
    // but not when called by clicking search in the selector
    if (!vars.getStringParameter("newFilter").equals("1")) {
      vars.removeSessionValue("BusinessPartner.adorgid");
    }
  }

  @Override
  public String getServletInfo() {
    return "Servlet that presents the business partners seeker";
  } // end of getServletInfo() method
}
