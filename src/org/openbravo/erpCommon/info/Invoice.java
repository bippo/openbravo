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

public class Invoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "bpartnername", "dateinvoiced", "documentno",
      "currency", "grandtotal", "convertedamount", "openamt", "issOtrx", "description",
      "poreference", "rowkey" };
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
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Invoice.name");
      String strWindowId = vars.getRequestGlobalVariable("WindowID", "Invoice.windowId");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      if (!strWindowId.equals("")) {
        vars.setSessionValue("Invoice.isSOTrx", (strSOTrx.equals("") ? "N" : strSOTrx));
      }
      if (!strNameValue.equals(""))
        vars.setSessionValue("Invoice.name", strNameValue + "%");
      printPage(response, vars, strNameValue, strWindowId);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Invoice.name");
      String strWindowId = vars.getRequestGlobalVariable("WindowID", "Invoice.windowId");
      String strSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      if (!strWindowId.equals("")) {
        vars.setSessionValue("Invoice.isSOTrx", (strSOTrx.equals("") ? "N" : strSOTrx));
      }
      vars.setSessionValue("Invoice.name", strKeyValue + "%");
      InvoiceData[] data = InvoiceData.selectKey(this, vars.getSqlDateFormat(),
          Utility.getContext(this, vars, "#User_Client", "Invoice"),
          Utility.getSelectorOrgs(this, vars, strOrg), strSOTrx, strKeyValue + "%");
      if (data != null && data.length == 1) {
        printPageKey(response, vars, data);
      } else
        printPage(response, vars, strKeyValue, strWindowId);
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {

      if (vars.getStringParameter("newFilter").equals("1")
          || vars.getStringParameter("newFilter").equals("")) {
        vars.removeSessionValue("Invoice.key");
        vars.removeSessionValue("Invoice.name");
        vars.removeSessionValue("Invoice.inpBpartnerId");
        vars.removeSessionValue("Invoice.inpDateFrom");
        vars.removeSessionValue("Invoice.inpDateTo");
        vars.removeSessionValue("Invoice.inpDescription");
        vars.removeSessionValue("Invoice.inpCal1");
        vars.removeSessionValue("Invoice.inpCal2");
        vars.removeSessionValue("Invoice.inpOrder");
        vars.removeSessionValue("Invoice.inpisSOTrx");
        vars.removeSessionValue("Invoice.adorgid");
        vars.removeSessionValue("Invoice.currentPage");
      }

      String strName = vars.getGlobalVariable("inpKey", "Invoice.name", "");
      String strBpartnerId = vars.getGlobalVariable("inpBpartnerId", "Invoice.inpBpartnerId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "Invoice.inpDateFrom", "");
      String strFechaTo = vars.getGlobalVariable("inpDateTo", "Invoice.inpDateTo", "");
      String strDescription = vars
          .getGlobalVariable("inpDescription", "Invoice.inpDescription", "");
      String strCal1 = vars.getNumericGlobalVariable("inpCal1", "Invoice.inpCal1", "");
      String strCalc2 = vars.getNumericGlobalVariable("inpCal2", "Invoice.inpCal2", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "Invoice.inpOrder", "");
      String strSOTrx = vars.getGlobalVariable("inpisSOTrx", "Invoice.inpisSOTrx", "");
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "Invoice.adorgid", "");

      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);

      printGridData(response, vars, strName, strBpartnerId, strDateFrom, strFechaTo,
          strDescription, strCal1, strCalc2, strOrder, strSOTrx, strOrg, strSortCols, strSortDirs,
          strOffset, strPageSize, strNewFilter);

    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strNameValue, String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: business partners seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Invoice")
        .createXmlDocument();
    String strSOTrx = vars.getSessionValue("Invoice.isSOTrx");

    if (strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strNameValue);
    }
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("isSOTrxCompra", strSOTrx);
    xmlDocument.setParameter("isSOTrxVenta", strSOTrx);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    StringBuffer total = new StringBuffer();
    total.append("keyArray = new Array(\n");
    total
        .append("new keyArrayItem(\"ENTER\", \"openSearch(null, null, '../Invoice.html', 'SELECTOR_INVOICE', false, 'frmMain', 'inpNewcInvoiceId', 'inpNewcInvoiceId_DES', document.frmMain.inpNewcInvoiceId_DES.value, 'Command', 'KEY', 'WindowID', '");
    total.append(strWindow).append("');\", \"inpNewcInvoiceId_DES\", \"null\")\n");
    total.append(");\n");
    total.append("enableShortcuts();\n");
    xmlDocument.setParameter("WindowIDArray", total.toString());
    xmlDocument.setParameter("WindowID", strWindow);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      InvoiceData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Invoice seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
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
    xmlDocument.setParameter("backendPageSize", String.valueOf(TableSQLData.maxRowsPerGridPage));
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
    boolean[] colSortable = { true, true, true, true, true, false, true, true, true, true, true };
    String[] colWidths = { "160", "58", "65", "65", "70", "60", "55", "65", "90", "40", "0" };

    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("rowkey") ? "true" : "false"));
      dataAux.setData("isvisible", (colNames[i].equals("rowkey") ? "false" : "true"));
      String name = Utility.messageBD(this, "INS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("INS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      dataAux.setData("issortable", colSortable[i] ? "true" : "false");
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private String generateResult(InvoiceData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].cInvoiceId + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text);\n");
    html.append("}\n");
    return html.toString();
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strName,
      String strBpartnerId, String strDateFrom, String strFechaTo, String strDescription,
      String strCal1, String strCalc2, String strOrder, String strSOTrx, String strOrg,
      String strOrderCols, String strOrderDirs, String strOffset, String strPageSize,
      String strNewFilter) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: pint page rows");

    SQLReturnObject[] headers = getHeaders(vars);
    FieldProvider[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    int offset = Integer.valueOf(strOffset).intValue();
    int pageSize = Integer.valueOf(strPageSize).intValue();
    int page = 0;

    if (headers != null) {
      try {
        // build sql orderBy clause
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);

        page = TableSQLData.calcAndGetBackendPage(vars, "Invoice.currentPage");
        if (vars.getStringParameter("movePage", "").length() > 0) {
          // on movePage action force executing countRows again
          strNewFilter = "";
        }
        int oldOffset = offset;
        offset = (page * TableSQLData.maxRowsPerGridPage) + offset;
        log4j.debug("relativeOffset: " + oldOffset + " absoluteOffset: " + offset);

        // New filter of first load
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
          strNumRows = InvoiceData.countRows(this, rownum,
              Utility.getContext(this, vars, "#User_Client", "Invoice"),
              Utility.getSelectorOrgs(this, vars, strOrg), strName, strDescription, strBpartnerId,
              strOrder, strDateFrom, DateTimeData.nDaysAfter(this, strFechaTo, "1"), strCal1,
              strCalc2, strSOTrx, pgLimit, oraLimit1, oraLimit2);
          vars.setSessionValue("Invoice.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("Invoice.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit = (offset + 1) + " AND " + String.valueOf(offset + pageSize);
          data = InvoiceData.select(this, "ROWNUM", vars.getSqlDateFormat(),
              Utility.getContext(this, vars, "#User_Client", "Invoice"),
              Utility.getSelectorOrgs(this, vars, strOrg), strName, strDescription, strBpartnerId,
              strOrder, strDateFrom, DateTimeData.nDaysAfter(this, strFechaTo, "1"), strCal1,
              strCalc2, strSOTrx, strOrderBy, oraLimit, "");
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          data = InvoiceData.select(this, "1", vars.getSqlDateFormat(),
              Utility.getContext(this, vars, "#User_Client", "Invoice"),
              Utility.getSelectorOrgs(this, vars, strOrg), strName, strDescription, strBpartnerId,
              strOrder, strDateFrom, DateTimeData.nDaysAfter(this, strFechaTo, "1"), strCal1,
              strCalc2, strSOTrx, strOrderBy, "", pgLimit);
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

          if (columnname.equalsIgnoreCase("grandtotal")
              || columnname.equalsIgnoreCase("convertedamount")
              || columnname.equalsIgnoreCase("openamt")) {
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
    return "Servlet that presents the business partners seeker";
  } // end of getServletInfo() method
}
