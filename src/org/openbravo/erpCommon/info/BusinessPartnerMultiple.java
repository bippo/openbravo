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
import org.openbravo.xmlEngine.XmlDocument;

public class BusinessPartnerMultiple extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "Value", "Name", "SO_CreditAvailable",
      "SO_CreditUsed", "SalesRep", "Url", "Email", "ActualLifetimeValue", "C_BPartner_ID", "RowKey" };
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
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue",
          "BusinessPartnerMultiple.name");
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String strBpartner = strIsSOTrxTab;
      String strKeyValue = vars.getGlobalVariable("inpKey", "BusinessPartnerMultiple.key", "");
      String strBpartners = vars.getStringParameter("inpBpartner", "all");

      if (strIsSOTrxTab.equals(""))
        strBpartner = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strSelected = "all";
      if (strBpartner.equals("Y"))
        strSelected = "costumer";
      else if (strBpartner.equals("N"))
        strSelected = "vendor";
      else
        strSelected = "all";
      vars.setSessionValue("BusinessPartnerMultiple.bpartner", strSelected);
      if (!strNameValue.equals(""))
        vars.setSessionValue("BusinessPartnerMultiple.name", strNameValue + "%");
      printPage(response, vars, strKeyValue, strNameValue, strBpartners);
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      String action = vars.getStringParameter("action");
      log4j.debug("command DATA - action: " + action);

      String strKey = vars.getGlobalVariable("inpKey", "BusinessPartnerMultiple.key", "");
      String strName = vars.getGlobalVariable("inpName", "BusinessPartnerMultiple.name", "");
      String strContact = vars.getStringParameter("inpContact");
      String strZIP = vars.getStringParameter("inpZIP");
      String strProvincia = vars.getStringParameter("inpProvincia");
      String strBpartners = vars.getGlobalVariable("inpBpartner",
          "BusinessPartnerMultiple.bpartner", "all");
      String strCity = vars.getStringParameter("inpCity");
      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "Invoice.adorgid", "");

      if (action.equalsIgnoreCase("getRows")) { // Asking for data rows
        printGridData(response, vars, strKey, strName, strContact, strZIP, strProvincia,
            strBpartners, strCity, strSortCols, strSortDirs, strOffset, strPageSize, strNewFilter,
            strOrg);

      } else if (action.equalsIgnoreCase("getIdsInRange")) {
        // asking for selected rows
        printGridDataSelectedRows(response, vars, strKey, strName, strContact, strZIP,
            strProvincia, strBpartners, strCity, strSortCols, strSortDirs);
      } else {
        throw new ServletException("Unimplemented action in DATA request: " + action);
      }

    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue,
      String strNameValue, String strBpartners) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the multiple business partners seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/BusinessPartnerMultiple").createXmlDocument();
    if (strKeyValue.equals("") && strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("alertMsg",
        "ALERT_MSG=\"" + Utility.messageBD(this, "NoBusinessPartnerSelected", vars.getLanguage())
            + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("name", strNameValue);
    xmlDocument.setParameter("clients", strBpartners);
    xmlDocument.setParameter("vendors", strBpartners);
    xmlDocument.setParameter("all", strBpartners);
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
    String[] colWidths = { "87", "165", "73", "77", "117", "100", "110", "51", "0", "0" };
    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("RowKey") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("RowKey") ? "true" : "false"));
      dataAux
          .setData("isvisible",
              (colNames[i].endsWith("_ID") || colNames[i].equalsIgnoreCase("RowKey") ? "false"
                  : "true"));
      String name = Utility
          .messageBD(this, "MBPS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("MBPS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strName, String strContact, String strZIP, String strProvincia, String strBpartners,
      String strCity, String strOrderCols, String strOrderDirs, String strOffset,
      String strPageSize, String strNewFilter, String strOrg) throws IOException, ServletException {
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
        page = TableSQLData.calcAndGetBackendPage(vars, "BusinessPartnerData.currentPage");
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
          strNumRows = BusinessPartnerMultipleData.countRows(this, rownum, Utility.getContext(this,
              vars, "#User_Client", "BusinessPartner"), Utility.getContext(this, vars,
              "#AccessibleOrgTree", "BusinessPartner"), strKey, strName, strContact, strZIP,
              strProvincia, (strBpartners.equals("costumer") ? "clients" : ""), (strBpartners
                  .equals("vendor") ? "vendors" : ""), strCity, pgLimit, oraLimit1, oraLimit2);
          vars.setSessionValue("BusinessPartnerMultipleInfo.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("BusinessPartnerMultipleInfo.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit1 = String.valueOf(offset + pageSize);
          String oraLimit2 = (offset + 1) + " AND " + oraLimit1;
          data = BusinessPartnerMultipleData.select(this, "ROWNUM", Utility.getContext(this, vars,
              "#User_Client", "BusinessPartner"), Utility.getContext(this, vars,
              "#AccessibleOrgTree", "BusinessPartner"), strKey, strName, strContact, strZIP,
              strProvincia, (strBpartners.equals("costumer") ? "clients" : ""), (strBpartners
                  .equals("vendor") ? "vendors" : ""), strCity, strOrderBy, "", oraLimit1,
              oraLimit2);
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          data = BusinessPartnerMultipleData.select(this, "1", Utility.getContext(this, vars,
              "#User_Client", "BusinessPartner"), Utility.getContext(this, vars,
              "#AccessibleOrgTree", "BusinessPartner"), strKey, strName, strContact, strZIP,
              strProvincia, (strBpartners.equals("costumer") ? "clients" : ""), (strBpartners
                  .equals("vendor") ? "vendors" : ""), strCity, strOrderBy, pgLimit, "", "");
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

    DecimalFormat df = Utility.getFormat(vars, "priceEdition");

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

          if (columnname.equalsIgnoreCase("SO_CreditAvailable")
              || columnname.equalsIgnoreCase("SO_CreditUsed")
              || columnname.equalsIgnoreCase("ActualLifetimeValue")) {
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
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(strRowsData.toString());
    out.print(strRowsData.toString());
    out.close();
  }

  /**
   * Prints the response for the getRowsIds action. It returns the rowkey for the identifier column
   * for the list of selected rows [minOffset..maxOffset]
   * 
   */
  private void printGridDataSelectedRows(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strName, String strContact, String strZIP, String strProvincia,
      String strBpartners, String strCity, String strOrderCols, String strOrderDirs)
      throws IOException, ServletException {
    int minOffset = new Integer(vars.getStringParameter("minOffset")).intValue();
    int maxOffset = new Integer(vars.getStringParameter("maxOffset")).intValue();
    log4j.debug("Output: print page ids, minOffset: " + minOffset + ", maxOffset: " + maxOffset);
    String type = "Hidden";
    String title = "";
    String description = "";
    FieldProvider[] data = null;
    FieldProvider[] res = null;
    try {
      // build sql orderBy clause
      String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);
      String strPage = vars.getSessionValue("ProductMultiple|currentPage", "0");
      int page = Integer.valueOf(strPage);

      int oldMinOffset = minOffset;
      int oldMaxOffset = maxOffset;
      minOffset = (page * TableSQLData.maxRowsPerGridPage) + minOffset;
      maxOffset = (page * TableSQLData.maxRowsPerGridPage) + maxOffset;
      log4j.debug("relativeMinOffset: " + oldMinOffset + " absoluteMinOffset: " + minOffset);
      log4j.debug("relativeMaxOffset: " + oldMaxOffset + " absoluteMaxOffset: " + maxOffset);
      // Filtering result
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        String oraLimit1 = String.valueOf(maxOffset);
        String oraLimit2 = (minOffset + 1) + " AND " + oraLimit1;
        data = BusinessPartnerMultipleData.select(this, "ROWNUM", Utility.getContext(this, vars,
            "#User_Client", "BusinessPartner"), Utility.getContext(this, vars,
            "#AccessibleOrgTree", "BusinessPartner"), strKey, strName, strContact, strZIP,
            strProvincia, (strBpartners.equals("costumer") ? "clients" : ""), (strBpartners
                .equals("vendor") ? "vendors" : ""), strCity, strOrderBy, "", oraLimit1, oraLimit2);
      } else {
        // minOffset and maxOffset are zero based so pageSize is difference +1
        int pageSize = maxOffset - minOffset + 1;
        String pgLimit = pageSize + " OFFSET " + minOffset;
        data = BusinessPartnerMultipleData.select(this, "1", Utility.getContext(this, vars,
            "#User_Client", "BusinessPartner"), Utility.getContext(this, vars,
            "#AccessibleOrgTree", "BusinessPartner"), strKey, strName, strContact, strZIP,
            strProvincia, (strBpartners.equals("costumer") ? "clients" : ""), (strBpartners
                .equals("vendor") ? "vendors" : ""), strCity, strOrderBy, pgLimit, "", "");
      }

      // result field has to be named id -> rename by copy the list
      res = new FieldProvider[data.length];
      for (int i = 0; i < data.length; i++) {
        SQLReturnObject sqlReturnObject = new SQLReturnObject();
        String resValue = "<![CDATA[" + data[i].getField("rowkey") + "]]>";
        sqlReturnObject.setData("id", resValue);
        res[i] = sqlReturnObject;
      }
    } catch (Exception e) {
      log4j.error("Error obtaining id-list for getIdsInRange", e);
      type = "Error";
      title = "Error";
      if (!e.getMessage().startsWith("<![CDATA["))
        description = "<![CDATA[" + e.getMessage() + "]]>";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridID").createXmlDocument();
    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setData("structure1", res);
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que multiple business partners seeker";
  } // end of getServletInfo() method
}
