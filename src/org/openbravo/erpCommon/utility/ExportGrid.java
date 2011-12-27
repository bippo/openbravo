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
 * All portions are Copyright (C) 2007-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.utils.FileUtility;

public class ExportGrid extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  // Since we don't use any style in the JRXML template, the column's width is based on the header
  // text length multiplied by a factor (character width)
  private static final int CHAR_WIDTH = 10;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String strTabId = vars.getRequiredStringParameter("inpTabId");
    String strWindowId = vars.getRequiredStringParameter("inpWindowId");
    String strAccessLevel = vars.getRequiredStringParameter("inpAccessLevel");
    if (log4j.isDebugEnabled())
      log4j.debug("Export grid, tabID: " + strTabId);
    ServletOutputStream os = null;
    InputStream is = null;

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);
    String fileName = "";
    if (log4j.isDebugEnabled())
      log4j.debug("*********************Base design path: " + strBaseDesign);

    try {
      GridReportVO gridReportVO = createGridReport(vars, strTabId, strWindowId, strAccessLevel,
          vars.commandIn("EXCEL"));
      os = response.getOutputStream();
      is = getInputStream(strBaseDesign + "/org/openbravo/erpCommon/utility/"
          + gridReportVO.getJrxmlTemplate());

      if (log4j.isDebugEnabled())
        log4j.debug("Create report, type: " + vars.getCommand());
      UUID reportId = UUID.randomUUID();
      String strOutputType = vars.getCommand().toLowerCase();
      if (strOutputType.equals("excel")) {
        strOutputType = "xls";
      }
      fileName = "ExportGrid-" + (reportId) + "." + strOutputType;
      if (vars.commandIn("HTML"))
        GridBO.createHTMLReport(is, gridReportVO, globalParameters.strFTPDirectory, fileName);
      else if (vars.commandIn("PDF")) {
        GridBO.createPDFReport(is, gridReportVO, globalParameters.strFTPDirectory, fileName);
      } else if (vars.commandIn("EXCEL")) {
        GridBO.createXLSReport(is, gridReportVO, globalParameters.strFTPDirectory, fileName);
      } else if (vars.commandIn("CSV")) {
        GridBO.createCSVReport(is, gridReportVO, globalParameters.strFTPDirectory, fileName);
      }
      printPagePopUpDownload(os, fileName);
    } catch (JRException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    } catch (IOException ioe) {
      try {
        FileUtility f = new FileUtility(globalParameters.strFTPDirectory, fileName, false, true);
        if (f.exists())
          f.deleteFile();
      } catch (IOException ioex) {
        log4j.error("Error trying to delete temporary report file " + fileName + " : "
            + ioex.getMessage());
      }
    } finally {
      is.close();
      os.close();
    }
  }

  private GridReportVO createGridReport(VariablesSecureApp vars, String strTabId,
      String strWindowId, String strAccessLevel, boolean useFieldLength) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Create Grid Report, tabID: " + strTabId);
    LinkedList<GridColumnVO> columns = new LinkedList<GridColumnVO>();
    FieldProvider[] data = null;
    TableSQLData tableSQL = null;
    try {
      tableSQL = new TableSQLData(vars, this, strTabId, Utility.getContext(this, vars,
          "#AccessibleOrgTree", strWindowId, Integer.valueOf(strAccessLevel).intValue()),
          Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this,
              vars, "ShowAudit", strWindowId).equals("Y"));
    } catch (Exception ex) {
      ex.printStackTrace();
      log4j.error(ex.getMessage());
      throw new ServletException(ex.getMessage());
    }
    SQLReturnObject[] headers = tableSQL.getHeaders(true, useFieldLength);

    if (tableSQL != null && headers != null) {
      try {
        if (log4j.isDebugEnabled())
          log4j.debug("Geting the grid data.");
        vars.setSessionValue(strTabId + "|newOrder", "1");
        String strSQL = ModelSQLGeneration.generateSQL(this, vars, tableSQL, "",
            new Vector<String>(), new Vector<String>(), 0, 0);
        if (log4j.isDebugEnabled())
          log4j.debug("SQL: " + strSQL);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValues());
        data = execquery.select();
      } catch (Exception e) {
        if (log4j.isDebugEnabled())
          log4j.debug("Error obtaining rows data");
        e.printStackTrace();
        throw new ServletException(e.getMessage());
      }
    }
    int totalWidth = 0;
    for (int i = 0; i < headers.length; i++) {
      if (headers[i].getField("isvisible").equals("true")) {
        String columnname = headers[i].getField("columnname");
        if (!tableSQL.getSelectField(columnname + "_R").equals(""))
          columnname += "_R";
        if (log4j.isDebugEnabled())
          log4j.debug("Add column: " + columnname + " width: " + headers[i].getField("width")
              + " reference: " + headers[i].getField("adReferenceId"));
        int intColumnWidth = Integer.valueOf(headers[i].getField("width"));
        if (headers[i].getField("name").length() * CHAR_WIDTH > intColumnWidth) {
          intColumnWidth = headers[i].getField("name").length() * CHAR_WIDTH;
          if (log4j.isDebugEnabled())
            log4j.debug("New width: " + intColumnWidth);
        }
        totalWidth += intColumnWidth;
        Class<?> fieldClass = String.class;
        if (headers[i].getField("adReferenceId").equals("11"))
          fieldClass = Double.class;
        else if (headers[i].getField("adReferenceId").equals("22")
            || headers[i].getField("adReferenceId").equals("12")
            || headers[i].getField("adReferenceId").equals("800008")
            || headers[i].getField("adReferenceId").equals("800019")
            || headers[i].getField("adReferenceId").equals("29"))
          fieldClass = java.math.BigDecimal.class;
        columns.add(new GridColumnVO(headers[i].getField("name"), columnname, intColumnWidth,
            fieldClass));
      }
    }
    String strTitle = ExportGridData.getTitle(this, strTabId, vars.getLanguage());
    if (log4j.isDebugEnabled())
      log4j.debug("GridReport, totalwidth: " + totalWidth + " title: " + strTitle);
    GridReportVO gridReportVO = new GridReportVO("plantilla.jrxml", data, strTitle, columns,
        strReplaceWithFull, totalWidth, vars.getJavaDateFormat());
    return gridReportVO;
  }

  private InputStream getInputStream(String reportFile) throws IOException {
    if (log4j.isDebugEnabled())
      log4j.debug("Get input stream file: " + reportFile);
    return (new FileInputStream(reportFile));
  }
}
