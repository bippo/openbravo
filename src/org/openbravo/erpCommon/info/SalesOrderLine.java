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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class SalesOrderLine extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "bpartner_name", "dateordered", "documentno",
      "issotrx", "product_name", "qty", "priceactual", "linenetamt", "rowkey" };
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
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "SalesOrderLine.name");
      String windowId = vars.getRequestGlobalVariable("WindowID", "SalesOrderLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals(""))
        strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("SalesOrderLine.isSOTrx", strSOTrx);
      String strProduct = vars.getRequestGlobalVariable("inpmProductId", "SalesOrderLine.product");
      String strBpartnerId = vars.getRequestGlobalVariable("inpcBpartnerId",
          "SalesOrderLine.bpartner");
      String strDocumentNo = "";
      String strDateFrom = "";
      String strDateTo = "";
      String strCal1 = "";
      String strCal2 = "";

      if (!strNameValue.equals("")) {
        int i = 0, count = 1, inicio = 0;
        String search = " - ", token = "";
        do {
          i = strNameValue.indexOf(search, inicio);
          if (i >= 0) {
            token = strNameValue.substring(inicio, i).trim();
            inicio = i + search.length();
          } else {
            token = strNameValue.substring(inicio).trim();
          }

          switch (count) {
          case 1:
            strDocumentNo = token + "%";
            vars.setSessionValue("SalesOrderLine.documentno", strDocumentNo);
            break;
          case 2:
            strDateFrom = token;
            strDateTo = token;
            vars.setSessionValue("SalesOrderLine.datefrom", token);
            vars.setSessionValue("SalesOrderLine.dateto", token);
            break;
          case 3:
            strCal1 = token;
            strCal2 = token;
            vars.setSessionValue("SalesOrderLine.grandtotalfrom", token);
            vars.setSessionValue("SalesOrderLine.grandtotalto", token);
            break;
          case 4:
            vars.setSessionValue("SalesOrderLine.lineno", token);
            break;
          case 5:
            vars.setSessionValue("SalesOrderLine.linenet", token);
            break;
          }
          count++;
        } while (i != -1);
      }
      printPage(response, vars, strBpartnerId, strProduct, strDocumentNo, strDateFrom, strDateTo,
          strCal1, strCal2);
    } else if (vars.commandIn("KEY")) {
      removePageSessionVariables(vars);
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue",
          "SalesOrderLine.documentno");
      String windowId = vars.getRequestGlobalVariable("WindowID", "SalesOrderLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals(""))
        strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("SalesOrderLine.isSOTrx", strSOTrx);
      strKeyValue = strKeyValue + "%";
      vars.setSessionValue("SalesOrderLine.documentno", strKeyValue);
      SalesOrderLineData[] data = null;
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      if (strSOTrx.equals("Y"))
        data = SalesOrderLineData.selectKey(this,
            Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"),
            Utility.getSelectorOrgs(this, vars, strOrg), strKeyValue);
      else
        data = SalesOrderLineData.selectKeySOTrx(this,
            Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"),
            Utility.getContext(this, vars, "#User_Org", "SalesOrderLine"), strKeyValue);
      if (data != null && data.length == 1)
        printPageKey(response, vars, data);
      else
        printPage(response, vars, "", "", strKeyValue, "", "", "", "");
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      String strSOTrx = vars.getGlobalVariable("inpisSOTrx", "SalesOrderLine.isSOTrx", "");
      if (vars.getStringParameter("newFilter").equals("1")) {
        removePageSessionVariables(vars);
      }
      String strBpartnerId = vars.getGlobalVariable("inpBpartnerId", "SalesOrderLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "SalesOrderLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "SalesOrderLine.documentno",
          "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "SalesOrderLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "SalesOrderLine.dateto", "");
      String strDescription = vars.getGlobalVariable("inpDescription",
          "SalesOrderLine.description", "");
      String strCal1 = vars
          .getNumericGlobalVariable("inpCal1", "SalesOrderLine.grandtotalfrom", "");
      String strCal2 = vars.getNumericGlobalVariable("inpCal2", "SalesOrderLine.grandtotalto", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "SalesOrderLine.order", "");
      String strDelivered = vars.getGlobalVariable("inpdelivered", "SalesOrderLine.deliverd", "N");
      String strInvoiced = vars.getGlobalVariable("inpinvoiced", "SalesOrderLine.invoiced", "N");
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "SalesOrderLine.adorgid", "");
      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      printGridData(response, vars, strDocumentNo, strDescription, strOrder, strBpartnerId,
          strDateFrom, strDateTo, strCal1, strCal2, strProduct, strDelivered, strInvoiced,
          strSOTrx, strSortCols, strSortDirs, strOffset, strPageSize, strNewFilter, strOrg);
    } else
      pageError(response);
  }

  private void removePageSessionVariables(VariablesSecureApp vars) {
    vars.removeSessionValue("SalesOrderLine.bpartner");
    vars.removeSessionValue("SalesOrderLine.product");
    vars.removeSessionValue("SalesOrderLine.documentno");
    vars.removeSessionValue("SalesOrderLine.datefrom");
    vars.removeSessionValue("SalesOrderLine.dateto");
    vars.removeSessionValue("SalesOrderLine.description");
    vars.removeSessionValue("SalesOrderLine.grandtotalfrom");
    vars.removeSessionValue("SalesOrderLine.grandtotalto");
    vars.removeSessionValue("SalesOrderLine.order");
    vars.removeSessionValue("SalesOrderLine.deliverd");
    vars.removeSessionValue("SalesOrderLine.invoiced");
    vars.removeSessionValue("SalesOrderLine.adorgid");
    vars.removeSessionValue("SalesOrderLine.currentPage");
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strProduct, String strDocumentNo, String strDateFrom, String strDateTo,
      String strCal1, String strCal2) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of sale-order-lines seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SalesOrderLine").createXmlDocument();

    String strSOTrx = vars.getSessionValue("SalesOrderLine.isSOTrx");

    if (strBPartner.equals("") && strProduct.equals("") && strDocumentNo.equals("")
        && strDateFrom.equals("") && strDateTo.equals("") && strCal1.equals("")
        && strCal2.equals("")) {
      strDocumentNo = "%";
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("documentno", strDocumentNo);
    xmlDocument.setParameter("datefrom", strDateFrom);
    xmlDocument.setParameter("dateto", strDateTo);
    xmlDocument.setParameter("grandtotalfrom", strCal1);
    xmlDocument.setParameter("grandtotalto", strCal2);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("cBpartnerId_DES",
        SalesOrderLineData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("mProductId", strProduct);
    xmlDocument.setParameter("mProductId_DES", SalesOrderLineData.selectProduct(this, strProduct));

    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("isSOTrxCompra", strSOTrx);
    xmlDocument.setParameter("isSOTrxVenta", strSOTrx);

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
      SalesOrderLineData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: sale-order-lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(SalesOrderLineData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].cOrderlineId + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].lineText, "\"", "\\\"") + "\";\n");
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
    xmlDocument.setParameter("backendPageSize", String.valueOf(TableSQLData.maxRowsPerGridPage));
    SQLReturnObject[] data = getHeaders(vars);
    String type = "Hidden";
    String title = "";
    String description = "";

    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setData("structure1", data);
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
    String[] colWidths = { "200", "70", "110", "40", "170", "63", "52", "68", "0" };
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
      String name = Utility
          .messageBD(this, "SOLS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("SOLS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentNo, String strDescription, String strOrder, String strBpartnerId,
      String strDateFrom, String strDateTo, String strCal1, String strCalc2, String strProduct,
      String strDelivered, String strInvoiced, String strSOTrx, String strOrderCols,
      String strOrderDirs, String strOffset, String strPageSize, String strNewFilter, String strOrg)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page rows");

    SQLReturnObject[] headers = getHeaders(vars);
    FieldProvider[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    int page = 0;
    int offset = Integer.valueOf(strOffset).intValue();
    int pageSize = Integer.valueOf(strPageSize).intValue();

    if (headers != null) {
      try {
        // build sql orderBy clause from parameters
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);

        page = TableSQLData.calcAndGetBackendPage(vars, "SalesOrderLine.currentPage");
        if (vars.getStringParameter("movePage", "").length() > 0) {
          // on movePage action force executing countRows again
          strNewFilter = "";
        }
        int oldOffset = offset;
        offset = (page * TableSQLData.maxRowsPerGridPage) + offset;
        log4j.debug("relativeOffset: " + oldOffset + " absoluteOffset: " + offset);

        // New filter or first load
        if (strNewFilter.equals("1") || strNewFilter.equals("")) {
          // calculate params for sql limit/offset or rownum clause
          String rownum = "0", oraLimit1 = null, oraLimit2 = null, pgLimit = null;
          if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
            oraLimit1 = String.valueOf(offset + TableSQLData.maxRowsPerGridPage);
            oraLimit2 = (offset + 1) + " AND " + oraLimit1;
            rownum = "ROWNUM";
          } else {
            pgLimit = TableSQLData.maxRowsPerGridPage + " OFFSET " + offset;
          }

          strNumRows = SalesOrderLineData.countRows(this, rownum,
              Utility.getContext(this, vars, "#User_Client", "SalesOrderLine"),
              Utility.getSelectorOrgs(this, vars, strOrg), strDocumentNo, strDescription, strOrder,
              strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1,
              strCalc2, strProduct, (strDelivered.equals("Y") ? "isdelivered" : ""),
              (strInvoiced.equals("Y") ? "isinvoiced" : ""), ("Y".equals(strSOTrx) ? "Y" : "N"),
              pgLimit, oraLimit1, oraLimit2);

          if (!"Y".equals(strSOTrx)) {
            data = SalesOrderLineData.selectSOTrx(this, "1", Utility.getContext(this, vars,
                "#User_Client", "SalesOrderLine"), Utility.getSelectorOrgs(this, vars, strOrg),
                strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData
                    .nDaysAfter(this, strDateTo, "1"), strCal1, strCalc2, strProduct, (strDelivered
                    .equals("Y") ? "isdelivered" : ""), (strInvoiced.equals("Y") ? "isinvoiced"
                    : ""), strOrderBy, "", "");
          }

          vars.setSessionValue("SalesOrderLine.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("SalesOrderLine.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit = (offset + 1) + " AND " + String.valueOf(offset + pageSize);
          if (strSOTrx.equals("Y")) {
            data = SalesOrderLineData.select(this, "ROWNUM", Utility.getContext(this, vars,
                "#User_Client", "SalesOrderLine"), Utility.getSelectorOrgs(this, vars, strOrg),
                strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData
                    .nDaysAfter(this, strDateTo, "1"), strCal1, strCalc2, strProduct, (strDelivered
                    .equals("Y") ? "isdelivered" : ""), (strInvoiced.equals("Y") ? "isinvoiced"
                    : ""), strOrderBy, oraLimit, "");
          } else {
            data = SalesOrderLineData.selectSOTrx(this, "ROWNUM", Utility.getContext(this, vars,
                "#User_Client", "SalesOrderLine"), Utility.getContext(this, vars, "#User_Org",
                "SalesOrderLine"), strDocumentNo, strDescription, strOrder, strBpartnerId,
                strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1, strCalc2,
                strProduct, (strDelivered.equals("Y") ? "isdelivered" : ""), (strInvoiced
                    .equals("Y") ? "isinvoiced" : ""), strOrderBy, oraLimit, "");
          }
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          if (strSOTrx.equals("Y")) {
            data = SalesOrderLineData.select(this, "1", Utility.getContext(this, vars,
                "#User_Client", "SalesOrderLine"), Utility.getSelectorOrgs(this, vars, strOrg),
                strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData
                    .nDaysAfter(this, strDateTo, "1"), strCal1, strCalc2, strProduct, (strDelivered
                    .equals("Y") ? "isdelivered" : ""), (strInvoiced.equals("Y") ? "isinvoiced"
                    : ""), strOrderBy, "", pgLimit);
          } else {
            data = SalesOrderLineData.selectSOTrx(this, "1", Utility.getContext(this, vars,
                "#User_Client", "SalesOrderLine"), Utility.getSelectorOrgs(this, vars, strOrg),
                strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData
                    .nDaysAfter(this, strDateTo, "1"), strCal1, strCalc2, strProduct, (strDelivered
                    .equals("Y") ? "isdelivered" : ""), (strInvoiced.equals("Y") ? "isinvoiced"
                    : ""), strOrderBy, "", pgLimit);
          }
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

          if (columnname.equalsIgnoreCase("qty") || columnname.equalsIgnoreCase("priceactual")
              || columnname.equalsIgnoreCase("linenetamt")) {
            strRowsData.append(df.format(new BigDecimal(data[j].getField(columnname))));
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

  public String getServletInfo() {
    return "Servlet that presents que sale-orders lines seeker";
  } // end of getServletInfo() method
}
