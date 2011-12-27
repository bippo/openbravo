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

public class Product extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final String[] colNames = { "value", "name", "qtyavailable", "pricelist",
      "pricestd", "qtyonhand", "qtyordered", "margin", "pricelimit", "rowkey" };
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
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Product.name");
      String strIDValue = vars.getStringParameter("inpIDValue");
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActual(this, vars.getLanguage(), strNameValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strNameValue = strNameAux;
      }
      String strPriceList = vars.getRequestGlobalVariable("inpPriceList", "Product.priceList");
      String strDate = vars.getRequestGlobalVariable("inpDate", "Product.date");
      String windowId = vars.getRequestGlobalVariable("WindowID", "Product.windowId");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "Product.warehouse",
          Utility.getContext(this, vars, "M_Warehouse_ID", windowId));
      vars.setSessionValue("Product.adorgid", vars.getStringParameter("inpAD_Org_ID", ""));
      vars.removeSessionValue("Product.key");
      strNameValue = strNameValue + "%";
      vars.setSessionValue("Product.name", strNameValue);
      if (strPriceList.equals("")) {
        strPriceList = Utility.getContext(this, vars, "M_Pricelist_ID", windowId);
        if (strPriceList.equals(""))
          strPriceList = ProductData.priceListDefault(this,
              Utility.getContext(this, vars, "#User_Client", "Product"),
              Utility.getContext(this, vars, "#AccessibleOrgTree", "Product"));
        vars.setSessionValue("Product.priceList", strPriceList);
      }
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateOrdered", windowId);
        if (log4j.isDebugEnabled())
          log4j.debug("DateOrdered:" + strDate);
      }
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateInvoiced", windowId);
        if (log4j.isDebugEnabled())
          log4j.debug("DateInvoiced:" + strDate);
      }
      if (strDate.equals(""))
        strDate = DateTimeData.today(this);
      vars.setSessionValue("Product.date", strDate);

      String strPriceListVersion = getPriceListVersion(vars, strPriceList, strDate);
      vars.setSessionValue("Product.priceListVersion", strPriceListVersion);

      printPage(response, vars, "", strNameValue, strWarehouse, strPriceList, strPriceListVersion,
          windowId, "paramName");
    } else if (vars.commandIn("KEY")) {
      removePageSessionVariables(vars);
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Product.key");
      String strIDValue = vars.getStringParameter("inpIDValue");
      if (!strIDValue.equals("")) {
        String strNameAux = ProductData.existsActualValue(this, vars.getLanguage(), strKeyValue,
            strIDValue);
        if (!strNameAux.equals(""))
          strKeyValue = strNameAux;
      }
      String strWarehouse = vars.getRequestGlobalVariable("inpWarehouse", "Product.warehouse");
      String strPriceList = vars.getRequestGlobalVariable("inpPriceList", "Product.priceList");
      String strDate = vars.getRequestGlobalVariable("inpDate", "Product.date");
      String windowId = vars.getRequestGlobalVariable("WindowID", "Product.windowId");
      // getGlobalVariable only used to store request value into session, not to read it from there
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "Product.adorgid", "");
      vars.removeSessionValue("Product.name");
      strKeyValue = strKeyValue + "%";
      vars.setSessionValue("Product.key", strKeyValue);
      if (strWarehouse.equals(""))
        strWarehouse = Utility.getContext(this, vars, "M_Warehouse_ID", windowId);
      vars.setSessionValue("Product.warehouse", strWarehouse);
      if (strPriceList.equals("")) {
        strPriceList = Utility.getContext(this, vars, "M_Pricelist_ID", windowId);
        if (strPriceList.equals(""))
          strPriceList = ProductData.priceListDefault(this,
              Utility.getContext(this, vars, "#User_Client", "Product"),
              Utility.getSelectorOrgs(this, vars, strOrg));
        vars.setSessionValue("Product.priceList", strPriceList);
      }

      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateOrdered", windowId);
        if (log4j.isDebugEnabled())
          log4j.debug("DateOrdered:" + strDate);
      }
      if (strDate.equals("")) {
        strDate = Utility.getContext(this, vars, "DateInvoiced", windowId);
        if (log4j.isDebugEnabled())
          log4j.debug("DateInvoiced:" + strDate);
      }
      if (strDate.equals(""))
        strDate = DateTimeData.today(this);
      vars.setSessionValue("Product.date", strDate);

      String strPriceListVersion = getPriceListVersion(vars, strPriceList, strDate);
      vars.setSessionValue("Product.priceListVersion", strPriceListVersion);

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
      ProductData[] data = ProductData.select(this, strWarehouse, rownum, strKeyValue + "%", "",
          Utility.getContext(this, vars, "#User_Client", "Product"),
          Utility.getContext(this, vars, "#User_Org", "Product"), strPriceListVersion, "1",
          pgLimit, oraLimit1, oraLimit2);
      if (data != null && data.length == 1)
        printPageKey(response, vars, data, strWarehouse, strPriceListVersion);
      else
        printPage(response, vars, strKeyValue, "", strWarehouse, strPriceList, strPriceListVersion,
            windowId, "paramKey");
    } else if (vars.commandIn("STRUCTURE")) {
      printGridStructure(response, vars);
    } else if (vars.commandIn("DATA")) {
      if (vars.getStringParameter("newFilter").equals("1")) {
        removePageSessionVariables(vars);
      }
      String strKey = vars.getGlobalVariable("inpKey", "Product.key", "");
      String strName = vars.getGlobalVariable("inpName", "Product.name", "");
      String strOrg = vars.getGlobalVariable("inpAD_Org_ID", "Product.adorgid", "");
      String strWarehouse = vars.getGlobalVariable("inpWarehouse", "Product.warehouse", "");
      String strPriceList = vars.getGlobalVariable("inpPriceList", "Product.priceList", "");
      String strPriceListVersion = vars.getGlobalVariable("inpPriceListVersion",
          "Product.priceListVersion", "");

      String strNewFilter = vars.getStringParameter("newFilter");
      String strOffset = vars.getStringParameter("offset");
      String strPageSize = vars.getStringParameter("page_size");
      String strSortCols = vars.getInStringParameter("sort_cols", columnFilter);
      String strSortDirs = vars.getInStringParameter("sort_dirs", directionFilter);
      printGridData(response, vars, strKey, strName, strOrg, strWarehouse, strPriceListVersion,
          strSortCols, strSortDirs, strOffset, strPageSize, strNewFilter);
    } else
      pageError(response);
  }

  private void removePageSessionVariables(VariablesSecureApp vars) {
    vars.removeSessionValue("Product.key");
    vars.removeSessionValue("Product.name");
    vars.removeSessionValue("Product.warehouse");
    vars.removeSessionValue("Product.priceList");
    vars.removeSessionValue("Product.priceListVersion");
    vars.removeSessionValue("Product.currentPage");

    // remove saved adorgid only when called from DEFAULT,KEY
    // but not when called by clicking search in the selector
    if (!vars.getStringParameter("newFilter").equals("1")) {
      vars.removeSessionValue("Product.adorgid");
    }
  }

  private String getPriceListVersion(VariablesSecureApp vars, String strPriceList, String strDate)
      throws IOException, ServletException {
    PriceListVersionComboData[] data = PriceListVersionComboData.selectActual(this, strPriceList,
        strDate, Utility.getContext(this, vars, "#User_Client", "Product"));
    if (log4j.isDebugEnabled())
      log4j.debug("Selecting pricelistversion date:" + strDate + " - pricelist:" + strPriceList);
    if (data == null || data.length == 0)
      return "";
    return data[0].mPricelistVersionId;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue,
      String strNameValue, String strWarehouse, String strPriceList, String strPriceListVersion,
      String windowId, String focusedId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 1 of the products seeker");
    String[] discard = new String[1];
    if (windowId.equals("800004")) {
      discard[0] = new String("NotReducedSearch");
    } else {
      discard[0] = new String("ReducedSearch");
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Product",
        discard).createXmlDocument();

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
    xmlDocument.setParameter("priceListVersion", strPriceListVersion);

    xmlDocument.setParameter("jsFocusOnField", Utility.focusFieldJS(focusedId));

    xmlDocument.setParameter("grid", "20");
    xmlDocument.setParameter("grid_Offset", "");
    xmlDocument.setParameter("grid_SortCols", "1");
    xmlDocument.setParameter("grid_SortDirs", "ASC");
    xmlDocument.setParameter("grid_Default", "0");

    xmlDocument.setData("structure1",
        WarehouseComboData.select(this, vars.getRole(), vars.getClient()));

    xmlDocument.setData(
        "structure2",
        PriceListVersionComboData.select(this, strPriceList,
            Utility.getContext(this, vars, "#User_Client", "Product")));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageKey(HttpServletResponse response, VariablesSecureApp vars,
      ProductData[] data, String strWarehouse, String strPriceListVersion) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: product seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    DecimalFormat df = Utility.getFormat(vars, "priceEdition");

    xmlDocument.setParameter("script", generateResult(data, strWarehouse, strPriceListVersion, df));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String generateResult(ProductData[] data, String strWarehouse,
      String strPriceListVersion, DecimalFormat df) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].mProductId + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].name, "\"", "\\\"") + "\";\n");
    html.append("var parameter = new Array(\n");
    html.append("new SearchElements(\"_UOM\", true, \"" + data[0].cUomId + "\"),\n");
    html.append("new SearchElements(\"_PSTD\", true, \""
        + df.format(new BigDecimal(data[0].pricestd)) + "\"),\n");
    html.append("new SearchElements(\"_PLIM\", true, \""
        + df.format(new BigDecimal(data[0].pricelimit)) + "\"),\n");
    html.append("new SearchElements(\"_CURR\", true, \"" + data[0].cCurrencyId + "\"),\n");
    html.append("new SearchElements(\"_PLIST\", true, \""
        + df.format(new BigDecimal(data[0].pricelist)) + "\")\n");
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
    boolean[] colSortable = { true, true, false, false, false, false, false, false, false, false };
    // String[] gridNames = {"Key", "Name","Disp. Credit","Credit used",
    // "Contact", "Phone no.", "Zip", "City", "Income", "c_bpartner_id",
    // "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey"};
    String[] colWidths = { "58", "129", "48", "95", "96", "124", "121", "48", "48", "0" };
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
      String name = Utility.messageBD(this, "PS_" + colNames[i].toUpperCase(), vars.getLanguage());
      dataAux.setData("name", (name.startsWith("PS_") ? colNames[i] : name));
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
      String strName, String strOrg, String strWarehouse, String strPriceListVersion,
      String strOrderCols, String strOrderDirs, String strOffset, String strPageSize,
      String strNewFilter) throws IOException, ServletException {
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
        // build sql orderBy clause
        String strOrderBy = SelectorUtility.buildOrderByClause(strOrderCols, strOrderDirs);

        page = TableSQLData.calcAndGetBackendPage(vars, "Product.currentPage");
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

          strNumRows = ProductData.countRows(this, rownum, strKey, strName, strPriceListVersion,
              Utility.getContext(this, vars, "#User_Client", "Product"),
              Utility.getSelectorOrgs(this, vars, strOrg), pgLimit, oraLimit1, oraLimit2);
          vars.setSessionValue("Product.numrows", strNumRows);
        } else {
          strNumRows = vars.getSessionValue("Product.numrows");
        }

        // Filtering result
        if (this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
          String oraLimit1 = String.valueOf(offset + pageSize);
          String oraLimit2 = (offset + 1) + " AND " + oraLimit1;
          data = ProductData.select(this, strWarehouse, "ROWNUM", strKey, strName,
              Utility.getContext(this, vars, "#User_Client", "Product"),
              Utility.getSelectorOrgs(this, vars, strOrg), strPriceListVersion, strOrderBy, "",
              oraLimit1, oraLimit2);
        } else {
          String pgLimit = pageSize + " OFFSET " + offset;
          data = ProductData.select(this, strWarehouse, "1", strKey, strName,
              Utility.getContext(this, vars, "#User_Client", "Product"),
              Utility.getSelectorOrgs(this, vars, strOrg), strPriceListVersion, strOrderBy,
              pgLimit, "", "");
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

    final DecimalFormat df = Utility.getFormat(vars, "priceEdition");
    final DecimalFormat qdf = Utility.getFormat(vars, "qtyEdition");

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

          // Building rowKey
          if (columnname.equalsIgnoreCase("rowkey")) {
            final StringBuffer rowKey = new StringBuffer();
            rowKey.append(data[j].getField("mProductId")).append(ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("name")).append(ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("cUomId")).append(ROWKEY_SEPARATOR);
            rowKey.append(df.format(new BigDecimal(data[j].getField("pricelist")))).append(
                ROWKEY_SEPARATOR);
            rowKey.append(df.format(new BigDecimal(data[j].getField("pricestd")))).append(
                ROWKEY_SEPARATOR);
            rowKey.append(df.format(new BigDecimal(data[j].getField("pricelimit")))).append(
                ROWKEY_SEPARATOR);
            rowKey.append(data[j].getField("cCurrencyId"));
            strRowsData.append(rowKey);
          } else if (columnname.equalsIgnoreCase("pricelist")
              || columnname.equalsIgnoreCase("pricestd")
              || columnname.equalsIgnoreCase("pricelimit") || columnname.equalsIgnoreCase("margin")) {
            strRowsData.append(df.format(new BigDecimal(data[j].getField(columnname))));
          } else if (columnname.equalsIgnoreCase("qtyonhand")
              || columnname.equalsIgnoreCase("qtyordered")
              || columnname.equalsIgnoreCase("qtyavailable")) {
            strRowsData.append(qdf.format(new BigDecimal(data[j].getField(columnname))));
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
