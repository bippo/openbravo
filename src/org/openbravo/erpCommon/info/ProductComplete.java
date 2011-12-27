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
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class ProductComplete extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "value", "name", "locator", "qty", "c_uom1",
      "attribute", "qtyorder", "c_uom2", "qty_ref", "quantityorder_ref", "rowkey" };
  private static final RequestFilter columnFilter = new ValueListFilter(colNames);
  private static final RequestFilter directionFilter = new ValueListFilter("asc", "desc");
  private static final String ROWKEY_SEPARATOR = "@_##_@";

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      removePageSessionVariables(vars);
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ProductComplete.name");
      String strIDValue = vars.getStringParameter("inpIDValue");
      // This if allows correctly filling the key and name fields and the
      // products selector when we open it from the adecuadamente cuando
      // lo abrimos desde la línea de albarán.
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActual(this, vars.getLanguage(), strNameValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strNameValue = strNameAux;
      }
      String windowId = vars.getRequestGlobalVariable("WindowID", "ProductComplete.windowId");
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse",
          "ProductComplete.warehouse");
      if (strWarehouse.equals(""))
        strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("ProductComplete.warehouse", strWarehouse);
      String strBpartner = vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      vars.removeSessionValue("ProductComplete.key");
      if (!strNameValue.equals(""))
        strNameValue = strNameValue + "%";
      vars.setSessionValue("ProductComplete.name", strNameValue);
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String isSOTrx = strIsSOTrxTab;
      if (strIsSOTrxTab.equals(""))
        isSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ProductComplete.isSOTrx", isSOTrx);
      String strStore = vars.getStringParameter("inpWithStoreLines", isSOTrx);
      vars.setSessionValue("ProductComplete.withstorelines", strStore);

      printPage(response, vars, "", strNameValue, strWarehouse, strStore, strBpartner, "", "",
          "paramName");
    } else if (vars.commandIn("KEY")) {
      removePageSessionVariables(vars);
      String windowId = vars.getRequestGlobalVariable("WindowID", "ProductComplete.windowId");
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ProductComplete.key");
      String strIDValue = vars.getStringParameter("inpIDValue");
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActualValue(this, vars.getLanguage(), strKeyValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strKeyValue = strNameAux;
      }
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse",
          "ProductComplete.warehouse");
      if (strWarehouse.equals(""))
        strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("ProductComplete.warehouse", strWarehouse);
      String strBpartner = vars.getRequestGlobalVariable("inpBPartner", "ProductComplete.bpartner");
      String strIsSOTrxTab = vars.getStringParameter("inpisSOTrxTab");
      String isSOTrx = strIsSOTrxTab;
      if (strIsSOTrxTab.equals(""))
        isSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ProductComplete.isSOTrx", isSOTrx);
      String strStore = vars.getStringParameter("inpWithStoreLines", isSOTrx);
      vars.removeSessionValue("ProductComplete.name");
      if (!strKeyValue.equals(""))
        strKeyValue = strKeyValue + "%";
      vars.setSessionValue("ProductComplete.key", strKeyValue);
      vars.setSessionValue("ProductComplete.withstorelines", strStore);

      ProductCompleteData[] data = null;
      String strClients = Utility.getContext(this, vars, "#User_Client", "ProductComplete");
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      String strOrgs = Utility.getSelectorOrgs(this, vars, strOrg);

      String isCalledFromProduction = "";
      if (windowId.equals("800051") || (windowId.equals("800052"))) {
        log4j
            .debug("selector called from process plan||work requirement using production=y filter");
        isCalledFromProduction = "production";
      }

      // two cases are interesting in the result:
      // - exactly one row (and row content is needed)
      // - zero or more than one row (row content not needed)
      // so limit <= 2 records to get both info from result without needing to fetch all rows
      String rownum = "0", oraLimit1 = null, oraLimit2 = null, pgLimit = null;
      if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
        oraLimit1 = "2";
        oraLimit2 = "1 AND 2";
        rownum = "ROWNUM";
      } else {
        pgLimit = "2";
      }

      if (strStore.equals("Y")) {
        if (vars.getLanguage().equals("en_US"))
          data = ProductCompleteData.select(this, rownum, strKeyValue, "", strWarehouse,
              isCalledFromProduction, vars.getRole(), strBpartner, strClients, "1", pgLimit,
              oraLimit1, oraLimit2);
        else
          data = ProductCompleteData.selecttrl(this, vars.getLanguage(), rownum, strKeyValue, "",
              strWarehouse, isCalledFromProduction, vars.getRole(), strBpartner, strClients, "1",
              pgLimit, oraLimit1, oraLimit2);
      } else {
        if (vars.getLanguage().equals("en_US"))
          data = ProductCompleteData.selectNotStored(this, rownum, strKeyValue, "", strBpartner,
              strClients, strOrgs, isCalledFromProduction, "1", pgLimit, oraLimit1, oraLimit2);
        else
          data = ProductCompleteData.selectNotStoredtrl(this, rownum, vars.getLanguage(),
              strKeyValue, "", strBpartner, strClients, strOrgs, isCalledFromProduction, "1",
              pgLimit, oraLimit1, oraLimit2);
      }
      if (data != null && data.length == 1)
        printPageKey(response, vars, data, strWarehouse);
      else
        printPage(response, vars, strKeyValue, "", strWarehouse, strStore, strBpartner, strClients,
            strOrgs, "paramKey");
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      if (vars.getStringParameter("newFilter").equals("1")) {
        removePageSessionVariables(vars);
      }
      String strKey = vars.getGlobalVariable("inpKey", "ProductComplete.key", "");
      String strName = vars.getGlobalVariable("inpName", "ProductComplete.name", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "ProductComplete.warehouse", "");
      String strBpartner = vars.getGlobalVariable("inpBPartner", "ProductComplete.bpartner", "");
      String strStore = vars.getGlobalVariable("inpWithStoreLines",
          "ProductComplete.withstorelines", "");

      String windowId = vars.getGlobalVariable("WindowID", "ProductComplete.windowId", "");
      String isCalledFromProduction = "";
      if (windowId.equals("800051") || (windowId.equals("800052"))) {
        log4j
            .debug("selector called from process plan||work requirement using production=y filter");
        isCalledFromProduction = "production";
      }

      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      String strClients = Utility.getContext(this, vars, "#User_Client", "ProductComplete");
      String strOrg = vars.getStringParameter("inpAD_Org_ID");
      String strOrgs = Utility.getSelectorOrgs(this, vars, strOrg);

      printGridData(response, vars, strKey, strName, strWarehouse, strBpartner, strStore,
          isCalledFromProduction, strOrgs, strClients, strSortCols, strSortDirs, strOffset,
          strPageSize, strNewFilter);
    } else
      pageError(response);
  }

  private void removePageSessionVariables(VariablesSecureApp vars) {
    vars.removeSessionValue("ProductComplete.key");
    vars.removeSessionValue("ProductComplete.name");
    vars.removeSessionValue("ProductComplete.warehouse");
    vars.removeSessionValue("ProductComplete.bpartner");
    vars.removeSessionValue("ProductComplete.withstorelines");
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue,
      String strNameValue, String strWarehouse, String strStore, String strBpartner,
      String strClients, String strOrgs, String focusedId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the product seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/ProductComplete").createXmlDocument();

    if (strKeyValue.equals("") && strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("name", strNameValue);
    xmlDocument.setParameter("warehouse", strWarehouse);
    xmlDocument.setParameter("store", strStore);
    xmlDocument.setParameter("bpartner", strBpartner);

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    xmlDocument.setParameter("jsFocusOnField", Utility.focusFieldJS(focusedId));

    xmlDocument.setData("structure1",
        WarehouseComboData.select(this, vars.getRole(), vars.getClient()));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      ProductCompleteData[] data, String strWarehouse) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    DecimalFormat df = Utility.getFormat(vars, "qtyEdition");

    xmlDocument.setParameter("script", generateResult(data, strWarehouse, df));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(ProductCompleteData[] data, String strWarehouse, DecimalFormat df)
      throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].mProductId + "\";\n");
    html.append("var text = \"" + FormatUtilities.replaceJS(data[0].name) + "\";\n");
    html.append("var parameter = new Array(\n");
    html.append("new SearchElements(\"_LOC\", true, \"" + data[0].mLocatorId + "\"),\n");
    html.append("new SearchElements(\"_ATR\", true, \"" + data[0].mAttributesetinstanceId
        + "\"),\n");
    html.append("new SearchElements(\"_PQTY\", true, \""
        + (data[0].qtyorder.equals("0") ? "" : df.format(new BigDecimal(data[0].qtyorder)))
        + "\"),\n");
    html.append("new SearchElements(\"_PUOM\", true, \"" + data[0].cUom2Id + "\"),\n");
    html.append("new SearchElements(\"_QTY\", true, \"" + df.format(new BigDecimal(data[0].qty))
        + "\"),\n");
    html.append("new SearchElements(\"_UOM\", true, \"" + data[0].cUom1Id + "\")\n");
    html.append(");\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text, parameter);\n");
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
    boolean[] colSortable = { true, true, false, true, false, true, true, false, false, false,
        false };
    // String[] gridNames = {"Key", "Name","Disp. Credit","Credit used",
    // "Contact", "Phone no.", "Zip", "City", "Income", "c_bpartner_id",
    // "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey"};
    String[] colWidths = { "73", "86", "166", "91", "32", "145", "128", "67", "130", "167", "0" };
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
      String name = Utility.messageBD(this, "PCS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("PCS_") ? colNames[i] : name));
      dataAux.setData("type", "string");
      dataAux.setData("width", colWidths[i]);
      dataAux.setData("issortable", colSortable[i] ? "true" : "false");
      vAux.addElement(dataAux);
    }
    data = new SQLReturnObject[vAux.size()];
    vAux.copyInto(data);
    return data;
  }

  private void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strName, String strWarehouse, String strBpartner, String strStore,
      String strIsCalledFromProduction, String strOrgs, String strClients, String strOrderCols,
      String strOrderDirs, String strOffset, String strPageSize, String strNewFilter)
      throws IOException, ServletException {
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
        // build sql orderBy clause from parameters
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);
        page = TableSQLData.calcAndGetBackendPage(vars, "ProjectData.currentPage");
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
          if (strStore.equals("Y")) {
            // countRows is the same in en_US and +trl case, so a
            // single countRows method is used
            strNumRows = ProductCompleteData.countRows(this, rownum, strKey, strName, strWarehouse,
                strIsCalledFromProduction, vars.getRole(), strBpartner, strClients, pgLimit,
                oraLimit1, oraLimit2);
          } else {
            // countRowsNotStored is the same in en_US and +trl case, so a
            // single countRows method is used
            strNumRows = ProductCompleteData.countRowsNotStored(this, rownum, strKey, strName,
                strBpartner, strClients, strOrgs, strIsCalledFromProduction, pgLimit, oraLimit1,
                oraLimit2);
          }

          vars.setSessionValue("ProductComplete.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("ProductComplete.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit1 = String.valueOf(offset + pageSize);
          String oraLimit2 = (offset + 1) + " AND " + oraLimit1;

          if (strStore.equals("Y")) {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.select(this, "ROWNUM", strKey, strName, strWarehouse,
                  strIsCalledFromProduction, vars.getRole(), strBpartner, strClients, strOrderBy,
                  "", oraLimit1, oraLimit2);
            else
              data = ProductCompleteData.selecttrl(this, vars.getLanguage(), "ROWNUM", strKey,
                  strName, strWarehouse, strIsCalledFromProduction, vars.getRole(), strBpartner,
                  strClients, strOrderBy, "", oraLimit1, oraLimit2);
          } else {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.selectNotStored(this, "ROWNUM", strKey, strName,
                  strBpartner, strClients, strOrgs, strIsCalledFromProduction, strOrderBy, "",
                  oraLimit1, oraLimit2);
            else
              data = ProductCompleteData.selectNotStoredtrl(this, "ROWNUM", vars.getLanguage(),
                  strKey, strName, strBpartner, strClients, strOrgs, strIsCalledFromProduction,
                  strOrderBy, "", oraLimit1, oraLimit2);
          }
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;

          if (strStore.equals("Y")) {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.select(this, "1", strKey, strName, strWarehouse,
                  strIsCalledFromProduction, vars.getRole(), strBpartner, strClients, strOrderBy,
                  pgLimit, "", "");
            else
              data = ProductCompleteData.selecttrl(this, vars.getLanguage(), "1", strKey, strName,
                  strWarehouse, strIsCalledFromProduction, vars.getRole(), strBpartner, strClients,
                  strOrderBy, pgLimit, "", "");
          } else {
            if (vars.getLanguage().equals("en_US"))
              data = ProductCompleteData.selectNotStored(this, "1", strKey, strName, strBpartner,
                  strClients, strOrgs, strIsCalledFromProduction, strOrderBy, pgLimit, "", "");
            else
              data = ProductCompleteData.selectNotStoredtrl(this, "1", vars.getLanguage(), strKey,
                  strName, strBpartner, strClients, strOrgs, strIsCalledFromProduction, strOrderBy,
                  pgLimit, "", "");
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

    DecimalFormat df = Utility.getFormat(vars, "qtyEdition");

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

          if (columnname.equalsIgnoreCase("rowkey")) {
            final StringBuffer rowKey = new StringBuffer();
            rowKey.append(data[j].getField("mProductId")).append(ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("name")).append(ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("mLocatorId")).append(ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("mAttributesetinstanceId")).append(ROWKEY_SEPARATOR);
            rowKey.append(df.format(new BigDecimal(data[j].getField("qtyorder")))).append(
                ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("cUom2Id")).append(ROWKEY_SEPARATOR);
            final String qty = data[j].getField("qty").equals("") ? "0" : data[j].getField("qty");
            rowKey.append(df.format(new BigDecimal(qty))).append(ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("cUom1Id"));
            strRowsData.append(rowKey);
          } else if (columnname.equalsIgnoreCase("qty") || columnname.equalsIgnoreCase("qtyorder")
              || columnname.equalsIgnoreCase("qty_ref")
              || columnname.equalsIgnoreCase("quantityorder_ref")) {
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
    return "Servlet that presents the products seeker";
  } // end of getServletInfo() method
}
