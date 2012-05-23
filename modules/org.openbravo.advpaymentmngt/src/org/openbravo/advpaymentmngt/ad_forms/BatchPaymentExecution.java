/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtility;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.xmlEngine.XmlDocument;

public class BatchPaymentExecution extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "documentno", "businesspartner", "description",
      "duedate", "amount", "rowkey" };
  private static final RequestFilter columnFilter = new ValueListFilter(colNames);
  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "BatchPaymentExecution|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "BatchPaymentExecution|DateTo", "");
      String strOrganizationId = vars.getGlobalVariable("inpOrgId", "BatchPaymentExecution|OrgId",
          "0");
      String strFinancialAccountId = vars.getGlobalVariable("inpFinancialAccount",
          "BatchPaymentExecution|FinancialAccount", "");
      String strPaymentMethodId = vars.getGlobalVariable("inpPaymentMethod",
          "BatchPaymentExecution|PaymentMethod", "");
      String strIsReceipt = vars.getGlobalVariable("inpIsReceipt",
          "BatchPaymentExecution|IsReceipt", "Y");

      printPageDataSheet(response, vars, strOrganizationId, strDateFrom, strDateTo,
          strFinancialAccountId, strPaymentMethodId, strIsReceipt);

    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);

    } else if (vars.commandIn("DATA")) {
      String strFinancialAccountId = vars.getGlobalVariable("inpFinancialAccount",
          "BatchPaymentExecution|FinancialAccount", "");
      String strPaymentMethodId = vars.getGlobalVariable("inpPaymentMethod",
          "BatchPaymentExecution|PaymentMethod", "");
      String strOrgId = vars.getGlobalVariable("inpOrgId", "BatchPaymentExecution|OrgId", "0");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "BatchPaymentExecution|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "BatchPaymentExecution|DateTo", "");
      boolean isReceipt = "Y".equals(vars.getGlobalVariable("inpIsReceipt",
          "BatchPaymentExecution|IsReceipt", ""));
      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);

      printGridData(response, vars, strSortCols, strSortDirs, strOffset, strPageSize, strNewFilter,
          strOrgId, strFinancialAccountId, strPaymentMethodId, strDateFrom, strDateTo, isReceipt);

    } else if (vars.commandIn("CALLOUTPAYMENTMETHOD")) {
      String strPaymentMethodId = vars.getRequestGlobalVariable("inpPaymentMethod", "");
      String strOrgId = vars.getRequestGlobalVariable("inpOrgId", "");
      boolean isReceipt = "Y".equals(vars.getGlobalVariable("inpIsReceipt",
          "BatchPaymentExecution|IsReceipt", ""));
      reloadPaymentMethodCombo(response, strPaymentMethodId, "", strOrgId, isReceipt);

    } else if (vars.commandIn("CALLOUTFINANCIALACCOUNT")) {
      String strFinancialAccountId = vars.getRequestGlobalVariable("inpFinancialAccount", "");
      String strPaymentMethodId = vars.getRequestGlobalVariable("inpPaymentMethod", "");
      String strOrgId = vars.getRequestGlobalVariable("inpOrgId", "");
      String strCurrencyId = vars.getRequestGlobalVariable("inpCurrencyId", "");
      boolean isReceipt = "Y".equals(vars.getGlobalVariable("inpIsReceipt",
          "BatchPaymentExecution|IsReceipt", ""));
      reloadFinancialAccountCombo(response, strPaymentMethodId, strFinancialAccountId, strOrgId,
          strCurrencyId, isReceipt);

    } else
      pageError(response);

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strOrganizationId, String strDateFrom, String strDateTo, String strFinancialAccountId,
      String strPaymentMethodId, String strIsReceipt) throws IOException, ServletException {
    log4j.debug("Output: BatchPaymentExecution");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/advpaymentmngt/ad_forms/BatchPaymentExecution").createXmlDocument();

    final String formClassName = this.getClass().getName();
    final String adFormId = getADFormInfo(formClassName).getId();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateDisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("KeyName", "");
    xmlDocument.setParameter("windowId", adFormId);
    xmlDocument.setParameter("tabId", adFormId);

    xmlDocument.setParameter("inpChkIsReceipt", strIsReceipt);

    String newOrg = strOrganizationId;
    if (null != strOrganizationId) {
      xmlDocument.setParameter("orgId", strOrganizationId);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "49DC1D6F086945AB82F84C66F5F13F16", Utility.getContext(this, vars, "#User_Org",
              "BatchPaymentExecution"), Utility.getContext(this, vars, "#User_Client",
              "BatchPaymentExecution"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "PrintInvoices", "");
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
      comboTableData = null;

    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateTo", strDateTo);

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "BatchPaymentExecution", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.advpaymentmngt.ad_forms.BatchPaymentExecution");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());

      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "BatchPaymentExecution.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "BatchPaymentExecution.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    // Payment Method combobox
    final boolean isReceipt = "Y".equals(strIsReceipt);
    String paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(strPaymentMethodId, "",
        newOrg, true, true, isReceipt);
    xmlDocument.setParameter("sectionDetailPaymentMethod", paymentMethodComboHtml);

    // Financial Account combobox
    // Currency - not filtered
    String finAccountComboHtml = FIN_Utility.getFinancialAccountList(strPaymentMethodId,
        strFinancialAccountId, newOrg, true, "", isReceipt);
    xmlDocument.setParameter("sectionDetailFinancialAccount", finAccountComboHtml);

    OBError myMessage = vars.getMessage(adFormId);
    vars.removeMessage(adFormId);
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printGridStructure(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
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
    // String[] colNames = { "documentno", "businesspartner", "description", "duedate", "amount",
    // "rowkey" };
    String[] colWidths = { "100", "150", "350", "85", "100", "0" }; // total 780
    String[] colLabels = { "APRM_PAYEXECMNGT_DOCUMENTNO", "APRM_FATS_BPARTNER", "Description",
        "APRM_PAYEXECMNGT_DUEDATE", "Amount", "rowkey" };
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
      dataAux.setData("name", Utility.messageBD(this, colLabels[i], vars.getLanguage()));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars,
      String strOrderCols, String strOrderDirs, String strOffset, String strPageSize,
      String strNewFilter, String strOrgId, String strFinancialAccountId,
      String strPaymentMethodId, String strDateFrom, String strDateTo, boolean isReceipt)
      throws IOException {
    log4j.debug("Output: print page rows");

    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    int page = 0;
    SQLReturnObject[] headers = getHeaders(vars);
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    int offset = Integer.valueOf(strOffset).intValue();
    int pageSize = Integer.valueOf(strPageSize).intValue();
    List<FIN_Payment> gridPayments = null;
    String strNewFilterAux = strNewFilter;

    if (headers != null) {
      try {
        // build sql orderBy clause from parameters
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);
        HashMap<String, String> orderByColsMap = new HashMap<String, String>();
        // String[] colNames = { "documentno", "businesspartner", "description", "duedate",
        // "amount", "rowkey" };

        orderByColsMap.put("documentno", FIN_Payment.PROPERTY_DOCUMENTNO);
        orderByColsMap.put("businesspartner", FIN_Payment.PROPERTY_BUSINESSPARTNER);
        orderByColsMap.put("description", FIN_Payment.PROPERTY_DESCRIPTION);
        orderByColsMap.put("duedate", FIN_Payment.PROPERTY_PAYMENTDATE);
        orderByColsMap.put("amount", FIN_Payment.PROPERTY_AMOUNT);
        orderByColsMap.put("rowkey", FIN_Payment.PROPERTY_ID);
        for (int i = 0; i < colNames.length; i++)
          strOrderBy = strOrderBy.replace(colNames[i], orderByColsMap.get(colNames[i]));

        String[] orderByClause = strOrderBy.split(" ");
        String strOrderByProperty = orderByClause[0];
        String strAscDesc = orderByClause[1];

        page = TableSQLData.calcAndGetBackendPage(vars, "BatchPaymentExecution.currentPage");
        if (vars.getStringParameter("movePage", "").length() > 0) {
          // on movePage action force executing countRows again
          strNewFilterAux = "";
        }
        int oldOffset = offset;
        offset = (page * TableSQLData.maxRowsPerGridPage) + offset;
        log4j.debug("relativeOffset: " + oldOffset + " absoluteOffset: " + offset);
        if (strNewFilterAux.equals("1") || strNewFilterAux.equals("")) { // New filter or first load
          gridPayments = dao.getPayExecRowCount(strOrgId, strPaymentMethodId,
              strFinancialAccountId, FIN_Utility.getDate(strDateFrom),
              FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strDateTo, "1")), offset,
              TableSQLData.maxRowsPerGridPage, null, null, isReceipt);
          strNumRows = Integer.toString(gridPayments.size());

          vars.setSessionValue("BatchPaymentExecution.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("BatchPaymentExecution.numrows");
        }

        gridPayments = dao.getPayExecRowCount(strOrgId, strPaymentMethodId, strFinancialAccountId,
            FIN_Utility.getDate(strDateFrom),
            FIN_Utility.getDate(DateTimeData.nDaysAfter(this, strDateTo, "1")), offset, pageSize,
            strOrderByProperty, strAscDesc, isReceipt);

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
    if (gridPayments != null && gridPayments.size() > 0) {
      for (FIN_Payment pay : gridPayments) {
        strRowsData.append("    <tr>\n");
        for (int k = 0; k < headers.length; k++) {
          strRowsData.append("      <td><![CDATA[");
          // "documentno", "businesspartner", "description", "duedate",
          // "amount", "rowkey"
          String columnData = "";
          switch (k) {
          case 0: // documentno
            columnData = pay.getDocumentNo();
            break;
          case 1: // businesspartner
            if (pay.getBusinessPartner() != null) {
              columnData = pay.getBusinessPartner().getIdentifier();
            }
            break;
          case 2: // description
            if (pay.getDescription() != null)
              columnData = pay.getDescription();
            break;
          case 3: // duedate
            columnData = Utility.formatDate(pay.getPaymentDate(), vars.getJavaDateFormat());
            break;
          case 4: // amount
            columnData = pay.getAmount().toString();
            break;
          case 5: // rowkey
            columnData = pay.getId().toString();
            break;
          default: // invalid
            log4j.error("Invalid column");
            break;
          }

          if (columnData != "") {
            if (headers[k].getField("adReferenceId").equals("32"))
              strRowsData.append(strReplaceWith).append("/images/");
            strRowsData.append(columnData.replaceAll("<b>", "").replaceAll("<B>", "")
                .replaceAll("</b>", "").replaceAll("</B>", "").replaceAll("<i>", "")
                .replaceAll("<I>", "").replaceAll("</i>", "").replaceAll("</I>", "")
                .replaceAll("<p>", "&nbsp;").replaceAll("<P>", "&nbsp;")
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

  private void reloadPaymentMethodCombo(HttpServletResponse response, String srtPaymentMethod,
      String strFinancialAccountId, String strOrgId, boolean isReceipt) throws IOException,
      ServletException {
    log4j.debug("Callout: Financial Account has changed to");

    String paymentMethodComboHtml = FIN_Utility.getPaymentMethodList(srtPaymentMethod,
        strFinancialAccountId, strOrgId, true, true, isReceipt);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(paymentMethodComboHtml.replaceAll("\"", "\\'"));
    out.close();

  }

  private void reloadFinancialAccountCombo(HttpServletResponse response, String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, String strCurrencyId, boolean isReceipt)
      throws IOException, ServletException {
    log4j.debug("Callout: Payment Method has changed to " + strPaymentMethodId);

    String finAccountComboHtml = FIN_Utility.getFinancialAccountList(strPaymentMethodId,
        strFinancialAccountId, strOrgId, true, strCurrencyId, isReceipt);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(finAccountComboHtml.replaceAll("\"", "\\'"));
    out.close();

  }

  private Form getADFormInfo(String formClassName) {
    OBContext.setAdminMode();
    try {
      OBCriteria<Form> obc = OBDal.getInstance().createCriteria(Form.class);
      obc.add(Restrictions.eq(Form.PROPERTY_JAVACLASSNAME, formClassName));
      if (obc.list() == null || obc.list().size() == 0) {
        throw new OBException(formClassName + ": Error on window data");
      }
      return obc.list().get(0);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
