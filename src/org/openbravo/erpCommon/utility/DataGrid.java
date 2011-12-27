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

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.reference.Reference;
import org.openbravo.reference.ui.UIReference;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * @author Fernando Iriazabal
 * 
 *         DataGrid handler class
 */
public class DataGrid extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String action = vars.getStringParameter("action");
    String TabId = vars.getStringParameter("inpadTabId");
    String WindowId = vars.getStringParameter("inpadWindowId");
    int accessLevel = new Integer(vars.getStringParameter("inpAccessLevel")).intValue();
    if (log4j.isDebugEnabled())
      log4j.debug("action: " + action);
    if (log4j.isDebugEnabled())
      log4j.debug("TabId: " + TabId);
    if (log4j.isDebugEnabled())
      log4j.debug("WindowId: " + WindowId);
    TableSQLData tableSQL = null;
    try {
      tableSQL = new TableSQLData(vars, this, TabId, Utility.getContext(this, vars,
          "#AccessibleOrgTree", WindowId, accessLevel), Utility.getContext(this, vars,
          "#User_Client", WindowId), Utility.getContext(this, vars, "ShowAudit", WindowId,
          accessLevel).equals("Y"));
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    // Asking for column's structure
    if (vars.commandIn("STRUCTURE")) {
      printPageStructure(response, vars, tableSQL);
    } else if (vars.commandIn("DATA")) { // DataGrid data
      if (log4j.isDebugEnabled())
        log4j.debug(">>DATA");
      if (action.equalsIgnoreCase("getRows")) { // Asking for data rows
        printPageData(response, vars, tableSQL);
      } else if (action.equalsIgnoreCase("getIdsInRange")) { // Asking for
        // selected
        // rows
        if (log4j.isDebugEnabled())
          log4j.debug(">>>>getIdsInRange");
        printPageDataId(response, vars, tableSQL);
      } else if (action.equalsIgnoreCase("getColumnTotals")) { // Asking
        // for
        // total of
        // the
        // selected
        // rows
        if (log4j.isDebugEnabled())
          log4j.debug(">>>>getColumnTotals");
        getColumnTotals(response, vars, tableSQL);
      } else if (action.equalsIgnoreCase("getComboContent")) { // Asking
        // for
        // dynamic
        // combo
        // content
        // (Edition)
        if (log4j.isDebugEnabled())
          log4j.debug(">>>>getComboContent");
        getComboContent(response, vars, TabId);
      } else if (action.equalsIgnoreCase("getDefaultValues")) { // Asking
        // for
        // default
        // values
        // (Edition)
        if (log4j.isDebugEnabled())
          log4j.debug(">>>>getDefaultValues");
        this.getDefaultValues(response, vars);
      }
    } else if (vars.commandIn("UPDATE")) { // Updating rows
      if (log4j.isDebugEnabled())
        log4j.debug(">>UPDATE");
      try {
        if (action.equalsIgnoreCase("deleteRow")) { // Deleting
          if (log4j.isDebugEnabled())
            log4j.debug(">>>>deleteRow");
          delete(response, vars, tableSQL, TabId, WindowId, accessLevel);
        } else { // Inserting or updating
          save(response, vars);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("Command " + action + " not defined");
    }
  }

  /**
   * Returns the column headers.
   * 
   * @param tableSQL
   *          Object hanler of tab's query
   * @return Array with the column's headers.
   * @throws ServletException
   */
  private SQLReturnObject[] getHeaders(TableSQLData tableSQL) throws ServletException {
    return tableSQL.getHeaders();
  }

  /**
   * Gets the total for the selected rows.
   * 
   * @param vars
   *          to get access to session and request parameters
   * @param tableSQL
   *          Object handler of tab's query
   * @return String with the total.
   */
  private String getTotalRows(VariablesSecureApp vars, TableSQLData tableSQL) {
    if (tableSQL == null)
      return "0";
    FieldProvider[] data = null;
    try {
      String currPageKey = tableSQL.getTabID() + "|" + "currentPage";
      String strPage = vars.getSessionValue(currPageKey, "0");
      int page = Integer.valueOf(strPage);

      ExecuteQuery execquery = new ExecuteQuery(this, tableSQL.getTotalSQL(page),
          tableSQL.getParameterValuesTotalSQL());
      data = execquery.select();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (data == null || data.length == 0)
      return "0";
    else
      return data[0].getField("TOTAL");
  }

  /**
   * Prints the response for the structure command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Object hanler of tab's query.
   * @throws IOException
   * @throws ServletException
   */
  private void printPageStructure(HttpServletResponse response, VariablesSecureApp vars,
      TableSQLData tableSQL) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page structure");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridStructure").createXmlDocument();
    SQLReturnObject[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    try {
      data = getHeaders(tableSQL);
    } catch (Exception ex) {
      type = "Error";
      title = "Error";
      description = ex.getMessage();
      ex.printStackTrace();
    }
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

  /**
   * Prints the response for the data rows command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Object handler of tab's query.
   * @throws IOException
   * @throws ServletException
   */
  private void printPageData(HttpServletResponse response, VariablesSecureApp vars,
      TableSQLData tableSQL) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page rows");
    int pageSize = new Integer(vars.getStringParameter("page_size")).intValue();
    int offset = new Integer(vars.getRequestGlobalVariable("offset", tableSQL.getTabID()
        + "|offset")).intValue();
    SQLReturnObject[] headers = getHeaders(tableSQL);
    FieldProvider[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";

    int page = TableSQLData.calcAndGetBackendPage(vars, tableSQL.getTabID() + "|" + "currentPage");
    int absoluteOffset = (page * TableSQLData.maxRowsPerGridPage) + offset;
    log4j.debug("relativeOffset: " + offset + " absoluteOffset: " + absoluteOffset);
    offset = absoluteOffset;

    if (tableSQL != null && headers != null) {
      try {
        // Prepare SQL adding the user filter parameters
        String strSQL = ModelSQLGeneration.generateSQL(this, vars, tableSQL, "",
            new Vector<String>(), new Vector<String>(), offset, pageSize);
        if (log4j.isDebugEnabled())
          log4j.debug("offset: " + offset + " - SQL: " + strSQL);
        vars.removeSessionValue(tableSQL.getTabID() + "|newOrder");

        // Wrap query to fetch only the required rows and execute it
        // passing params
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValues());
        data = execquery.select();
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
    strRowsData.append("  <rows numRows=\"").append(getTotalRows(vars, tableSQL))
        .append("\" backendPage=\"" + page + "\">\n");
    if (data != null && data.length > 0) {
      for (int j = 0; j < data.length; j++) {
        strRowsData.append("    <tr>\n");
        for (int k = 0; k < headers.length; k++) {
          strRowsData.append("      <td><![CDATA[");
          String columnname = headers[k].getField("columnname");

          if (((headers[k].getField("iskey").equals("false") && !headers[k].getField(
              "gridcolumnname").equalsIgnoreCase("keyname")) || !headers[k].getField("iskey")
              .equals("true")) && !tableSQL.getSelectField(columnname + "_R").equals("")) {
            columnname += "_R";
          }

          if ((data[j].getField(columnname)) != null) {
            String adReferenceId = headers[k].getField("adReferenceId");

            UIReference reference = Reference.getUIReference(adReferenceId, adReferenceId);
            String value = reference.formatGridValue(vars, data[j].getField(columnname));

            // Old Image reference, leave it as is
            if (adReferenceId.equals("32"))
              strRowsData.append(strReplaceWith).append("/images/");

            strRowsData.append(value.replaceAll("<b>", "").replaceAll("<B>", "")
                .replaceAll("</b>", "").replaceAll("</B>", "").replaceAll("<i>", "")
                .replaceAll("<I>", "").replaceAll("</i>", "").replaceAll("</I>", "")
                .replaceAll("<p>", "&nbsp;").replaceAll("<P>", "&nbsp;")
                .replaceAll("<br>", "&nbsp;").replaceAll("<BR>", "&nbsp;")
                .replaceAll("<h1>", "&nbsp;").replaceAll("<H1>", "&nbsp;")
                .replaceAll("</h1>", "&nbsp;").replaceAll("</H1>", "").replaceAll("<h2>", "&nbsp;")
                .replaceAll("<H2>", "&nbsp;").replaceAll("</h2>", "&nbsp;").replaceAll("</H2>", "")
                .replaceAll("<h3>", "&nbsp;").replaceAll("<H3>", "&nbsp;")
                .replaceAll("</h3>", "&nbsp;").replaceAll("</H3>", "").replaceAll("<li>", "&nbsp;")
                .replaceAll("<LI>", "&nbsp;").replaceAll("</li>", "&nbsp;").replaceAll("</LI>", "")
                .replaceAll("<ul>", "&nbsp;").replaceAll("<UL>", "&nbsp;")
                .replaceAll("</ul>", "&nbsp;").replaceAll("</UL>", ""));
          } else {
            if (headers[k].getField("adReferenceId").equals("32")
                || headers[k].getField("adReferenceId").equals("4AA6C3BE9D3B4D84A3B80489505A23E5")) {
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
    // if (log4j.isDebugEnabled()) log4j.debug(strRowsData.toString());
    out.print(strRowsData.toString());
    out.close();
  }

  /**
   * Prints the response for the getRowsIds command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Object handler of tab's query.
   * @throws IOException
   * @throws ServletException
   */
  private void printPageDataId(HttpServletResponse response, VariablesSecureApp vars,
      TableSQLData tableSQL) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page ids");
    int minOffset = new Integer(vars.getStringParameter("minOffset")).intValue();
    int maxOffset = new Integer(vars.getStringParameter("maxOffset")).intValue();
    String type = "Hidden";
    String title = "";
    String description = "";
    FieldProvider[] data = null;
    FieldProvider[] res = null;

    // get current page
    String currPageKey = tableSQL.getTabID() + "|" + "currentPage";
    String strPage = vars.getSessionValue(currPageKey, "0");
    int page = Integer.valueOf(strPage);

    int oldMinOffset = minOffset;
    int oldMaxOffset = maxOffset;
    minOffset = (page * TableSQLData.maxRowsPerGridPage) + minOffset;
    maxOffset = (page * TableSQLData.maxRowsPerGridPage) + maxOffset;
    log4j.debug("relativeMinOffset: " + oldMinOffset + " absoluteMinOffset: " + minOffset);
    log4j.debug("relativeMaxOffset: " + oldMaxOffset + " absoluteMaxOffset: " + maxOffset);

    if (tableSQL != null) {
      try {
        // minOffset and maxOffset are zero based so pageSize is difference +1
        int pageSize = maxOffset - minOffset + 1;
        String strSQL = ModelSQLGeneration.generateSQLonlyId(this, vars, tableSQL,
            (tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + "AS ID"),
            new Vector<String>(), new Vector<String>(), minOffset, pageSize);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValuesOnlyId());
        data = execquery.select();
        res = new FieldProvider[data.length];
        for (int i = 0; i < data.length; i++) {
          SQLReturnObject sqlReturnObject = new SQLReturnObject();
          sqlReturnObject.setData("id", data[i].getField(tableSQL.getKeyColumn()));
          res[i] = sqlReturnObject;
        }
      } catch (Exception e) {
        if (log4j.isDebugEnabled())
          log4j.debug("Error obtaining rows data");
        e.printStackTrace();
        type = "Error";
        title = "Error";
        if (!e.getMessage().startsWith("<![CDATA["))
          description = "<![CDATA[" + e.getMessage() + "]]>";
      }
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
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Prints the response for the getColumnsTotal command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Object hanler of tab's query.
   * @throws ServletException
   * @throws IOException
   */
  private void getColumnTotals(HttpServletResponse response, VariablesSecureApp vars,
      TableSQLData tableSQL) throws ServletException, IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print page column total");
    String rows = vars.getInStringParameter("rows", IsIDFilter.instance);
    String columnname = vars.getStringParameter("columnName");
    FieldProvider[] data = null;
    if (tableSQL != null) {
      try {
        Vector<String> filter = new Vector<String>();
        filter.addElement(tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + " IN " + rows);
        String strSQL = ModelSQLGeneration.generateSQL(this, vars, tableSQL,
            "SUM(" + tableSQL.getTableName() + "." + columnname + ") AS TOTAL", filter,
            new Vector<String>(), 0, 0, false, false);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValues());
        data = execquery.select();
      } catch (Exception e) {
        if (log4j.isDebugEnabled())
          log4j.debug("Error obtaining rows data");
        e.printStackTrace();
      }
    }
    String total = "0";
    if (data != null && data.length > 0)
      total = data[0].getField("TOTAL");
    // fallback to default value when query returned null (i.e. with no rows in filter)
    if (total == null) {
      total = "0";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridTotal").createXmlDocument();
    xmlDocument.setParameter("total", total);
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Prints the response for the delete command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @param tableSQL
   *          Object hanler of tab's query.
   * @throws IOException
   * @throws ServletException
   */
  private void delete(HttpServletResponse response, VariablesSecureApp vars, TableSQLData tableSQL,
      String strTab, String WindowId, int accessLevel) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Delete record");

    int result = 1;
    int total = 0;
    String type, title, description = "";

    if (AccessData.isReadOnly(this, strTab).equals("Y")) {
      result = 0;
      type = "Error";
      title = "Error";
      description = Utility.messageBD(this, "AccessCannotDelete", vars.getLanguage());
    } else if (WindowAccessData.hasNotDeleteAccess(this, vars.getRole(), strTab)) {
      result = 0;
      type = "Error";
      title = "Error";
      description = Utility.messageBD(this, "NoWriteAccess", vars.getLanguage());
    } else {
      // Set session info before deleting
      SessionInfo.setProcessType("W");
      SessionInfo.setProcessId(strTab);

      Vector<String> parametersData = null;
      String rows = vars.getInStringParameter("rows", IsIDFilter.instance);
      StringBuffer SqlDataBuffer = new StringBuffer();
      SqlDataBuffer.append("DELETE FROM ").append(tableSQL.getTableName()).append(" \n");
      SqlDataBuffer.append("WHERE ").append(tableSQL.getKeyColumn()).append(" \n");
      SqlDataBuffer.append("IN ").append(rows);
      String parentKey = tableSQL.getParentColumnName();
      if (parentKey != null && !parentKey.equals("")) {
        SqlDataBuffer.append(" AND ").append(tableSQL.getTableName()).append(".").append(parentKey)
            .append(" = ?");
        if (parametersData == null)
          parametersData = new Vector<String>();
        parametersData.addElement(vars.getGlobalVariable("inpParentKey", tableSQL.getWindowID()
            + "|" + parentKey));
      }
      SqlDataBuffer.append(" AND AD_Client_ID IN (")
          .append(Utility.getContext(this, vars, "#User_Client", WindowId, accessLevel))
          .append(")");
      SqlDataBuffer.append(" AND AD_Org_ID IN (")
          .append(Utility.getContext(this, vars, "#User_Org", WindowId, accessLevel)).append(")");
      if (log4j.isDebugEnabled())
        log4j.debug(SqlDataBuffer.toString());

      try {
        ExecuteQuery execquery = new ExecuteQuery(this, SqlDataBuffer.toString(), parametersData);
        total = execquery.executeStatement();
        if (total == 0) {
          result = 0;
          type = "Error";
          title = "Error";
          description = "0 " + Utility.messageBD(this, "RowsDeleted", vars.getLanguage());
        } else {
          result = 1;
          type = "Success";
          title = "Success";
          description = total + " " + Utility.messageBD(this, "RowsDeleted", vars.getLanguage());
        }
      } catch (ServletException e) {
        log4j.error("Error in delete: " + e);
        e.printStackTrace();
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorAjax(response, "Error", "Connection Error", "No database connection");
          return;
        } else {
          result = 0;
          type = myError.getType();
          title = myError.getTitle();
          if (!myError.getMessage().startsWith("<![CDATA["))
            description = "<![CDATA[" + myError.getMessage() + "]]>";
          else
            description = myError.getMessage();
        }
      } catch (Exception e) {
        log4j.error("Error in delete: " + e);
        e.printStackTrace();
        result = 0;
        type = "ERROR";
        title = "Error";
        if (!e.getMessage().startsWith("<![CDATA["))
          description = "<![CDATA[" + e.getMessage() + "]]>";
        else
          description = e.getMessage();
      }
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/DataGridDelete").createXmlDocument();
    xmlDocument.setParameter("result", Integer.toString(result));
    xmlDocument.setParameter("type", type);
    xmlDocument.setParameter("title", title);
    xmlDocument.setParameter("description", description);
    xmlDocument.setParameter("total", Integer.toString(total));
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled())
      log4j.debug(xmlDocument.print());
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Prints the response for the getComboContent command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @param TabId
   *          Id of the tab
   * @throws ServletException
   * @throws IOException
   */
  private void getComboContent(HttpServletResponse response, VariablesSecureApp vars, String TabId)
      throws ServletException, IOException {
    /*
     * String columnname = vars.getStringParameter("subordinatedColumn"); if
     * (log4j.isDebugEnabled()) log4j.debug("--------"); if (log4j.isDebugEnabled())
     * log4j.debug(""); if (log4j.isDebugEnabled()) log4j.debug(columnname.toString()); if
     * (log4j.isDebugEnabled()) log4j.debug(""); if (log4j.isDebugEnabled())
     * log4j.debug("--------");
     * 
     * //Get columname to get reference id (17, 18 or 19) DataGridReferenceIdData[] refId =
     * DataGridReferenceIdData.select(this, vars.getLanguage(), TabId, columnname.toString());
     * 
     * if (log4j.isDebugEnabled()) log4j.debug(refId[0].adReferenceId);
     * 
     * if (refId[0].adReferenceId.equals("17")) { if (log4j.isDebugEnabled()) log4j.debug("true");
     * 
     * StringBuffer SelectClause = new StringBuffer(); StringBuffer FromClause = new StringBuffer();
     * StringBuffer WhereClause = new StringBuffer(); SelectClause.append("SELECT ");
     * FromClause.append("FROM "); WhereClause.append("WHERE "); SelectClause.append("Name ");
     * FromClause.append("AD_Ref_List_V "); WhereClause
     * .append("Ad_Reference_Id = ").append(refId[0].adReferenceValueId);
     * WhereClause.append(" AND "); WhereClause.append("ad_language = '").append
     * (vars.getLanguage()).append("' \n");
     * 
     * //String SqlData; StringBuffer SqlData = new StringBuffer();
     * SqlData.append(SelectClause).append(FromClause).append(WhereClause); if
     * (log4j.isDebugEnabled()) log4j.debug("SQL del Combo: " + SqlData.toString());
     * 
     * try{ ExecuteQuery execquery = new ExecuteQuery(this, SqlData.toString(), null);
     * FieldProvider[] data = execquery.select(0,0);
     * 
     * StringBuffer strRowsData = new StringBuffer(); if (log4j.isDebugEnabled())
     * log4j.debug("\n Ha tragado la query del SQL Combo\n"); strRowsData.append("    <row>\n");
     * 
     * for (int k=0;k<data.length;k++) { if ((data[1].getField("Name")) != null) {
     * strRowsData.append("          <option>").append(data[k].getField
     * ("Name")).append("</option>\n"); } }
     * 
     * strRowsData.append("    </row>\n"); response.setContentType("text/xml; charset=UTF-8");
     * PrintWriter out = response.getWriter();
     * //out.print("<response type=\"object\" id=\"structureParser\">");
     * out.print(strRowsData.toString()); //out.print("</response>"); out.close();
     * 
     * } catch (Exception e) { if (log4j.isDebugEnabled()) log4j.debug("Error in printPageData");
     * e.printStackTrace(); } } else if (refId[0].adReferenceId.equals("19")) { if
     * (log4j.isDebugEnabled()) log4j.debug("true");
     * 
     * StringBuffer SelectClause = new StringBuffer(); StringBuffer FromClause = new StringBuffer();
     * SelectClause.append("SELECT "); FromClause.append("FROM ");
     * 
     * SelectClause.append(columnname).append(", Name "); FromClause.append(columnname.substring(0,
     * columnname.length()-3));
     * 
     * //String SqlData; StringBuffer SqlData = new StringBuffer();
     * SqlData.append(SelectClause).append(FromClause); if (log4j.isDebugEnabled())
     * log4j.debug("SQL del Combo: " + SqlData.toString());
     * 
     * try{ ExecuteQuery execquery = new ExecuteQuery(this, SqlData.toString(), null);
     * FieldProvider[] data = execquery.select(0,0);
     * 
     * StringBuffer strRowsData = new StringBuffer(); if (log4j.isDebugEnabled())
     * log4j.debug("\n Ha tragado la query del SQL Combo\n"); strRowsData.append("    <row>\n");
     * 
     * for (int k=0;k<data.length;k++) { if ((data[1].getField("Name")) != null) {
     * strRowsData.append("          <option>").append(data[k].getField
     * ("Name")).append("</option>\n"); } }
     * 
     * strRowsData.append("    </row>\n"); response.setContentType("text/xml; charset=UTF-8");
     * PrintWriter out = response.getWriter();
     * //out.print("<response type=\"object\" id=\"structureParser\">");
     * out.print(strRowsData.toString()); //out.print("</response>"); out.close();
     * 
     * } catch (Exception e) { if (log4j.isDebugEnabled()) log4j.debug("Error in printPageData");
     * e.printStackTrace(); } }
     */
  }

  /**
   * Prints the response for the getDefaultValues command.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @throws ServletException
   * @throws IOException
   */
  private void getDefaultValues(HttpServletResponse response, VariablesSecureApp vars)
      throws ServletException, IOException {
  }

  /**
   * Prints the response for the Insert or Update commands.
   * 
   * @param response
   *          Handler for the response Object.
   * @param vars
   *          Handler for the session info.
   * @throws ServletException
   * @throws IOException
   */
  private void save(HttpServletResponse response, VariablesSecureApp vars) throws ServletException,
      IOException {
  }
}
