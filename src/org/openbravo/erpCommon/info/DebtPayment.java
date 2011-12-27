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
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class DebtPayment extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "BPARTNER", "ORDERNO", "INVOICE", "DATEPLANNED",
      "AMOUNT", "WRITEOFFAMT", "CURRENCY", "PAYMENTRULE", "DEBTCANCEL", "DEBTGENERATE",
      "C_DEBT_PAYMENT_ID", "ROWKEY" };
  private static final RequestFilter columnFilter = new ValueListFilter(colNames);
  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT") || vars.commandIn("KEY")) {
      cleanSessionValue(vars);
      printPage(response, vars);
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {

      if (vars.getStringParameter("newFilter").equals("1")) {
        cleanSessionValue(vars);
      }

      String strBpartnerId = vars.getGlobalVariable("inpBpartnerId", "DebtPayment.inpBpartnerId",
          "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "DebtPayment.inpDateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "DebtPayment.inpDateTo", "");
      String strCal1 = vars.getNumericGlobalVariable("inpCal1", "DebtPayment.inpCal1", "");
      String strCal2 = vars.getNumericGlobalVariable("inpCal2", "DebtPayment.inpCal2", "");
      String strPaymentRule = vars.getGlobalVariable("inpCPaymentRuleId",
          "DebtPayment.inpCPaymentRuleId", "");
      String strIsReceipt = vars.getGlobalVariable("inpIsReceipt", "DebtPayment.inpIsReceipt", "Y");
      String strIsPaid = vars.getGlobalVariable("inpIsPaid", "DebtPayment.inpIsPaid", "N");
      String strIsPending = vars.getGlobalVariable("inpPending", "DebtPayment.inpPending", "P");
      String strInvoice = vars.getGlobalVariable("inpInvoice", "DebtPayment.inpInvoice", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "DebtPayment.inpOrder", "");
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "DebtPayment.adorgid", "");

      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);

      printGridData(response, vars, strBpartnerId, strDateFrom, strDateTo, strCal1, strCal2,
          strPaymentRule, strIsReceipt, strIsPaid, strIsPending, strOrder, strInvoice, strSortCols,
          strSortDirs, strOffset, strPageSize, strNewFilter, strOrg);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the DebtPayments seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/DebtPayment")
        .createXmlDocument();
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "All_Payment Rule", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "DebtPayment"), Utility.getContext(this, vars, "#User_Client", "DebtPayment"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "DebtPayment", "");
      xmlDocument.setData("reportPaymentRule", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    // Set default filter values due to heavy load. From date = To Date =
    // Today
    String strDateFormat = vars.getJavaDateFormat();
    SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    Date today = new Date();
    xmlDocument.setParameter("dateFromValue", dateFormat.format(today));
    xmlDocument.setParameter("dateToValue", dateFormat.format(today));

    vars.setSessionValue("DebtPayment.inpDateFrom", dateFormat.format(today));
    vars.setSessionValue("DebtPayment.inpDateTo", dateFormat.format(today));

    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
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
    boolean[] colSortable = { true, true, true, true, true, true, true, false, false, false, false,
        false };
    String[] colWidths = { "113", "59", "57", "60", "65", "62", "55", "81", "110", "110", "0", "0" };
    for (int i = 0; i < colNames.length; i++) {
      SQLReturnObject dataAux = new SQLReturnObject();
      dataAux.setData("columnname", colNames[i]);
      dataAux.setData("gridcolumnname", colNames[i]);
      dataAux.setData("adReferenceId", "AD_Reference_ID");
      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");
      dataAux.setData("isidentifier", (colNames[i].equals("ROWKEY") ? "true" : "false"));
      dataAux.setData("iskey", (colNames[i].equals("ROWKEY") ? "true" : "false"));
      dataAux.setData("isvisible",
          (colNames[i].endsWith("_ID") || colNames[i].equals("ROWKEY") ? "false" : "true"));
      String name = Utility.messageBD(this, "DPS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("DPS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      dataAux.setData("issortable", colSortable[i] ? "true" : "false");
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars,
      String strBpartnerId, String strDateFrom, String strDateTo, String strCal1, String strCal2,
      String strPaymentRule, String strIsReceipt, String strIsPaid, String strIsPending,
      String strOrder, String strInvoice, String strOrderCols, String strOrderDirs,
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

    // adjust to either pending or any other state then pending
    strIsPending = strIsPending.equals("P") ? "= 'P'" : "<> 'P'";

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
        if (strNewFilter.equals("1") || strNewFilter.equals("")) {
          // New filter or first load
          /*
           * strNumRows = DebtPaymentData.countRows(this, Utility.getContext(this, vars,
           * "#User_Client", "DebtPayment"), Utility.getSelectorOrgs(this, vars, strOrg),
           * strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1,
           * strCal2, strPaymentRule, strIsPaid, strIsReceipt, strInvoice, strOrder, strIsPending);
           */
          String rownum = "0", oraLimit1 = null, oraLimit2 = null, pgLimit = null;
          if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
            oraLimit1 = String.valueOf(offset + TableSQLData.maxRowsPerGridPage);
            oraLimit2 = (offset + 1) + " AND " + oraLimit1;
            rownum = "ROWNUM";
          } else {
            pgLimit = TableSQLData.maxRowsPerGridPage + " OFFSET " + offset;
          }

          strNumRows = DebtPaymentData.countRows(this, rownum,
              Utility.getContext(this, vars, "#User_Client", "DebtPayment"),
              Utility.getSelectorOrgs(this, vars, strOrg), strBpartnerId, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1, strCal2, strPaymentRule,
              strIsPaid, strIsReceipt, strInvoice, strOrder, strIsPending, pgLimit, oraLimit1,
              oraLimit2);
          vars.setSessionValue("DebtPaymentInfo.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("DebtPaymentInfo.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit = (offset + 1) + " AND " + String.valueOf(offset + pageSize);
          data = DebtPaymentData
              .select(this, vars.getLanguage(), "ROWNUM",
                  Utility.getContext(this, vars, "#User_Client", "DebtPayment"),
                  Utility.getSelectorOrgs(this, vars, strOrg), strBpartnerId, strDateFrom,
                  DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1, strCal2, strPaymentRule,
                  strIsPaid, strIsReceipt, strInvoice, strOrder, strIsPending, strOrderBy,
                  oraLimit, "");
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          data = DebtPaymentData.select(this, vars.getLanguage(), "1",
              Utility.getContext(this, vars, "#User_Client", "DebtPayment"),
              Utility.getSelectorOrgs(this, vars, strOrg), strBpartnerId, strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strCal1, strCal2, strPaymentRule,
              strIsPaid, strIsReceipt, strInvoice, strOrder, strIsPending, strOrderBy, "", pgLimit);
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

          if (columnname.equalsIgnoreCase("amount") || columnname.equalsIgnoreCase("writeoffamt")) {
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

  private void cleanSessionValue(VariablesSecureApp vars) {
    vars.removeSessionValue("DebtPayment.inpBpartnerId");
    vars.removeSessionValue("DebtPayment.inpDateFrom");
    vars.removeSessionValue("DebtPayment.inpDateTo");
    vars.removeSessionValue("DebtPayment.inpCal1");
    vars.removeSessionValue("DebtPayment.inpCal2");
    vars.removeSessionValue("DebtPayment.inpCPaymentRuleId");
    vars.removeSessionValue("DebtPayment.inpIsReceipt");
    vars.removeSessionValue("DebtPayment.inpIsPaid");
    vars.removeSessionValue("DebtPayment.inpPending");
    vars.removeSessionValue("DebtPayment.inpInvoice");
    vars.removeSessionValue("DebtPayment.inpOrder");
    vars.removeSessionValue("DebtPaymentInfo.currentPage");
  }

  public String getServletInfo() {
    return "Servlet that presents que DebtPayments seeker";
  } // end of getServletInfo() method
}
