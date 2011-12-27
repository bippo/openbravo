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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class ShipmentReceipt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "bpartner_name", "movementdate", "documentno",
      "description", "order_reference", "sales_transaction", "rowkey" };
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
      removePageSessionVariables(vars);
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceipt.name");
      if (!strNameValue.equals(""))
        vars.setSessionValue("ShipmentReceipt.name", strNameValue + "%");
      String windowId = vars.getStringParameter("WindowID");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      if (strSOTrx.equals(""))
        vars.setSessionValue("ShipmentReceipt.isSOTrx", "N");
      else
        vars.setSessionValue("ShipmentReceipt.isSOTrx", strSOTrx);
      printPage(response, vars, strNameValue, strSOTrx);
    } else if (vars.commandIn("KEY")) {
      removePageSessionVariables(vars);
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceipt.name");
      vars.setSessionValue("ShipmentReceipt.name", strKeyValue + "%");
      String windowId = vars.getStringParameter("WindowID");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      if (strSOTrx.equals(""))
        strSOTrx = "N";
      vars.setSessionValue("ShipmentReceipt.isSOTrx", strSOTrx);
      ShipmentReceiptData[] data = ShipmentReceiptData.selectKey(this,
          Utility.getContext(this, vars, "#User_Client", "ShipmentReceipt"),
          Utility.getSelectorOrgs(this, vars, strOrg), strSOTrx, strKeyValue + "%");
      if (data != null && data.length == 1) {
        printPageKey(response, vars, data);
      } else
        printPage(response, vars, strKeyValue + "%", strSOTrx);
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      if (vars.getStringParameter("newFilter").equals("1")) {
        removePageSessionVariables(vars);
      }
      String strName = vars.getGlobalVariable("inpKey", "ShipmentReceipt.name", "");
      String strSalesTransaction = vars.getGlobalVariable("inpSalesTransaction",
          "ShipmentReceipt.salestransaction", "N");
      String strBpartnerId = vars.getGlobalVariable("inpBpartnerId", "ShipmentReceipt.bpartnerid",
          "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ShipmentReceipt.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ShipmentReceipt.dateto", "");
      String strDescription = vars.getGlobalVariable("inpDescription",
          "ShipmentReceipt.description", "");
      String strOrderReference = vars.getGlobalVariable("inpOrderReference",
          "ShipmentReceipt.orderreferrence", "");
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "ShipmentReceipt.adorgid", "");
      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);

      printGridData(response, vars, strName, strDescription, strBpartnerId, strOrderReference,
          strDateFrom, strDateTo, strSalesTransaction, strSortCols, strSortDirs, strOffset,
          strPageSize, strNewFilter, strOrg);
    } else
      pageError(response);
  }

  private void removePageSessionVariables(VariablesSecureApp vars) {
    vars.removeSessionValue("ShipmentReceipt.name");
    vars.removeSessionValue("ShipmentReceipt.salestransaction");
    vars.removeSessionValue("ShipmentReceipt.bpartnerid");
    vars.removeSessionValue("ShipmentReceipt.datefrom");
    vars.removeSessionValue("ShipmentReceipt.dateto");
    vars.removeSessionValue("ShipmentReceipt.description");
    vars.removeSessionValue("ShipmentReceipt.orderreferrence");
    vars.removeSessionValue("ShipmentReceipt.adorgid");
    vars.removeSessionValue("ShipmentReceipt.currentPage");
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strNameValue, String strSOTrx) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the invoice seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/ShipmentReceipt").createXmlDocument();
    if (!strNameValue.endsWith("%"))
      strNameValue = strNameValue + "%";

    xmlDocument.setParameter("key", strNameValue);
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("salesTransaction", strSOTrx);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      ShipmentReceiptData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: invoices seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(ShipmentReceiptData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].clave + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].documentno, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text);\n");
    html.append("}\n");
    return html.toString();
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
    // String[] gridNames = {"Key", "Name","Disp. Credit","Credit used",
    // "Contact", "Phone no.", "Zip", "City", "Income", "c_bpartner_id",
    // "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey"};
    String[] colWidths = { "120", "120", "100", "130", "140", "130", "0" };
    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("isvisible",
          (colNames[i].endsWith("_id") || colNames[i].equals("rowkey") ? "false" : "true"));
      String name = Utility.messageBD(this, "SRS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("SRS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strName,
      String strDescription, String strBpartnerId, String strOrderReference, String strDateFrom,
      String strDateTo, String strSalesTransaction, String strOrderCols, String strOrderDirs,
      String strOffset, String strPageSize, String strNewFilter, String strOrg) throws IOException,
      ServletException {
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
        page = TableSQLData.calcAndGetBackendPage(vars, "ShipmentReceipt.currentPage");
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
          strNumRows = ShipmentReceiptData.countRows(this, rownum,
              Utility.getContext(this, vars, "#User_Client", "ShipmentReceipt"),
              Utility.getSelectorOrgs(this, vars, strOrg), strName, strDescription, strBpartnerId,
              strOrderReference, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
              strSalesTransaction, pgLimit, oraLimit1, oraLimit2);
          // strNumRows = String.valueOf(data.length);
          vars.setSessionValue("ShipmentReceipt.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("ShipmentReceipt.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit = (offset + 1) + " AND " + String.valueOf(offset + pageSize);
          data = ShipmentReceiptData.select(this, "ROWNUM",
              Utility.getContext(this, vars, "#User_Client", "ShipmentReceipt"),
              Utility.getSelectorOrgs(this, vars, strOrg), strName, strDescription, strBpartnerId,
              strOrderReference, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
              strSalesTransaction, strOrderBy, oraLimit, "");
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          data = ShipmentReceiptData.select(this, "1",
              Utility.getContext(this, vars, "#User_Client", "ShipmentReceipt"),
              Utility.getSelectorOrgs(this, vars, strOrg), strName, strDescription, strBpartnerId,
              strOrderReference, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"),
              strSalesTransaction, strOrderBy, "", pgLimit);
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

          /*
           * if (( (headers[k].getField("iskey").equals("false") && !headers
           * [k].getField("gridcolumnname").equalsIgnoreCase("keyname" )) ||
           * !headers[k].getField("iskey").equals("true")) && !tableSQL.getSelectField(columnname +
           * "_R").equals("")) { columnname += "_R"; }
           */

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

  public String getServletInfo() {
    return "Servlet that presents the business partners seeker";
  } // end of getServletInfo() method
}
