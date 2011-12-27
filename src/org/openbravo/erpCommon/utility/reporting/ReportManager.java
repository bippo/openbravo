/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo Public License
 * Version 1.1 (the "License"), being the Mozilla Public License
 * Version 1.1 with a permitted attribution clause; you may not use this
 * file except in compliance with the License. You may obtain a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Business Momentum b.v.
 * All portions are Copyright (C) 2007-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  Business Momentum b.v. (http://www.businessmomentum.eu).
 *************************************************************************
 */
package org.openbravo.erpCommon.utility.reporting;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachmentsData;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;

public class ReportManager {
  private static Logger log4j = Logger.getLogger(ReportManager.class);
  private static final String TEMP_REPORT_DIR = "tmp";

  private ConnectionProvider _connectionProvider;
  private String _strBaseDesignPath;
  private String _strDefaultDesignPath;
  private String language;
  private String _strBaseWeb; // BASE WEB!!!!!!
  private String _prefix;
  private String _strAttachmentPath;
  private boolean multiReports = false;

  public ReportManager(ConnectionProvider connectionProvider, String ftpDirectory,
      String replaceWithFull, String baseDesignPath, String defaultDesignPath, String prefix,
      boolean multiReport) {
    _connectionProvider = connectionProvider;
    _strBaseWeb = replaceWithFull;
    _strBaseDesignPath = baseDesignPath;
    _strDefaultDesignPath = defaultDesignPath;
    _strAttachmentPath = ftpDirectory;
    _prefix = prefix;
    multiReports = multiReport;

    // Strip of ending slash character
    if (_strBaseDesignPath.endsWith("/"))
      _strBaseDesignPath = _strBaseDesignPath.substring(0, _strBaseDesignPath.length() - 1);
    if (_strDefaultDesignPath.endsWith("/"))
      _strDefaultDesignPath = _strDefaultDesignPath
          .substring(0, _strDefaultDesignPath.length() - 1);
  }

  public JasperPrint processReport(Report report, VariablesSecureApp variables)
      throws ReportingException {

    setTargetDirectory(report);
    language = variables.getLanguage();
    final String baseDesignPath = _prefix + "/" + _strBaseDesignPath + "/" + _strDefaultDesignPath;
    final Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));

    String templateLocation = report.getTemplateInfo().getTemplateLocation();
    templateLocation = Replace.replace(
        Replace.replace(templateLocation, "@basedesign@", baseDesignPath), "@baseattach@",
        _strAttachmentPath);
    templateLocation = Replace.replace(templateLocation, "//", "/");
    final String templateFile = templateLocation + report.getTemplateInfo().getTemplateFilename();

    final HashMap<String, Object> designParameters = populateDesignParameters(variables, report);
    designParameters.put("TEMPLATE_LOCATION", templateLocation);
    JasperPrint jasperPrint = null;

    try {
      JasperDesign jasperDesign = JRXmlLoader.load(templateFile);

      Object[] parameters = jasperDesign.getParametersList().toArray();
      String parameterName = "";
      String subReportName = "";
      Collection<String> subreportList = new ArrayList<String>();

      /*
       * TODO: At present this process assumes the subreport is a .jrxml file. Need to handle the
       * possibility that this subreport file could be a .jasper file.
       */
      for (int i = 0; i < parameters.length; i++) {
        final JRDesignParameter parameter = (JRDesignParameter) parameters[i];
        if (parameter.getName().startsWith("SUBREP_")) {
          parameterName = parameter.getName();
          subreportList.add(parameterName);
          subReportName = Replace.replace(parameterName, "SUBREP_", "") + ".jrxml";
          JasperReport jasperReportLines = createSubReport(templateLocation, subReportName,
              baseDesignPath);
          designParameters.put(parameterName, jasperReportLines);
        }
      }

      JasperReport jasperReport = Utility.getTranslatedJasperReport(_connectionProvider,
          templateFile, language, baseDesignPath);

      if (log4j.isDebugEnabled())
        log4j.debug("creating the format factory: " + variables.getJavaDateFormat());
      JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(variables.getJavaDateFormat());
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);
      String salesOrder = report.getCheckSalesOrder();
      if (salesOrder != null && salesOrder.equals("Y")) {
        designParameters.put(
            "DOCUMENT_NAME",
            Utility.messageBD(_connectionProvider, "Sales", language) + " "
                + Utility.messageBD(_connectionProvider, "Invoice", language));
      } else {
        designParameters.put(
            "DOCUMENT_NAME",
            Utility.messageBD(_connectionProvider, "Purchase", language) + " "
                + Utility.messageBD(_connectionProvider, "Invoice", language));
      }
      jasperPrint = fillReport(designParameters, jasperReport);

    } catch (final JRException exception) {
      log4j.error(exception.getMessage());
      exception.printStackTrace();
      throw new ReportingException(exception);
    } catch (final Exception exception) {
      log4j.error(exception.getMessage());
      exception.getStackTrace();
      throw new ReportingException(exception);
    }

    return jasperPrint;
  }

  private String getAttachmentPath() {
    return _strAttachmentPath;
  }

  private String getTempReportDir() {
    return TEMP_REPORT_DIR;
  }

  public void setTargetDirectory(Report report) {
    final File targetDirectory = new File(getAttachmentPath() + "/" + getTempReportDir());
    if (!targetDirectory.exists())
      targetDirectory.mkdirs();
    report.setTargetDirectory(targetDirectory);
  }

  public void saveTempReport(Report report, VariablesSecureApp vars) {
    JasperPrint jasperPrint = null;
    try {
      jasperPrint = processReport(report, vars);
      saveReport(report, jasperPrint);
    } catch (final ReportingException e) {
      log4j.error(e.getMessage());
      e.printStackTrace();
    }
  }

  private void saveReport(Report report, JasperPrint jasperPrint) {
    String separator = "";
    if (!report.getTargetDirectory().toString().endsWith("/")) {
      separator = "/";
    }
    final String target = report.getTargetDirectory() + separator + report.getFilename();
    try {
      JasperExportManager.exportReportToPdfFile(jasperPrint, target);
    } catch (final JRException e) {
      e.printStackTrace();
    }
  }

  private JasperPrint fillReport(HashMap<String, Object> designParameters, JasperReport jasperReport)
      throws ReportingException, SQLException {
    JasperPrint jasperPrint = null;

    Connection con = null;
    try {
      con = _connectionProvider.getTransactionConnection();
      jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
    } catch (final Exception e) {
      log4j.error(e.getMessage());
      e.printStackTrace();
      throw new ReportingException(e.getMessage());
    } finally {
      _connectionProvider.releaseRollbackConnection(con);
    }
    return jasperPrint;
  }

  private JasperReport createSubReport(String templateLocation, String subReportFileName,
      String baseDesignPath) {
    JasperReport jasperReportLines = null;
    try {
      jasperReportLines = Utility.getTranslatedJasperReport(_connectionProvider, templateLocation
          + subReportFileName, language, baseDesignPath);
    } catch (final JRException e1) {
      log4j.error(e1.getMessage());
      e1.printStackTrace();
    }
    return jasperReportLines;
  }

  public File createAttachmentForReport(ConnectionProvider connectionProvider, Report report,
      String tableId, VariablesSecureApp vars) throws ReportingException, IOException {
    if (report.isAttached())
      throw new ReportingException(Utility.messageBD(connectionProvider, "AttachmentExists",
          vars.getLanguage()));

    final String destination = tableId + "-" + report.getDocumentId();

    // First move the file to the correct destination
    final File destinationFolder = new File(_strAttachmentPath + "/" + destination);
    if (!destinationFolder.exists()) {
      destinationFolder.mkdirs();
    }
    report.setTargetDirectory(destinationFolder);

    final JasperPrint jasperPrint = processReport(report, vars);
    saveReport(report, jasperPrint);

    final File sourceFile = new File(report.getTargetLocation());
    final File destinationFile = new File(destinationFolder, sourceFile.getName());
    log4j.debug("Destination file before renaming: " + destinationFile);
    if (!sourceFile.renameTo(destinationFile))
      throw new ReportingException(Utility.messageBD(connectionProvider, "UnreachableDestination",
          vars.getLanguage()) + destinationFolder);

    report.setTargetDirectory(destinationFolder);
    // Attach them to the order in OB
    Connection conn = null;
    try {
      conn = _connectionProvider.getTransactionConnection();

      final String newFileId = SequenceIdData.getUUID();
      log4j.debug("New file id: " + newFileId);
      // The 103 in the following insert specifies the document type: in
      // this case PDF
      TabAttachmentsData.insert(conn, _connectionProvider, newFileId, vars.getClient(),
          vars.getOrg(), vars.getUser(), tableId, report.getDocumentId(), "103",
          "Generated by printing ", destinationFile.getName());

      _connectionProvider.releaseCommitConnection(conn);
    } catch (final Exception exception) {
      try {
        _connectionProvider.releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }

      throw new ReportingException(exception);
    }

    report.setAttached(true);

    return destinationFile;
  }

  private HashMap<String, Object> populateDesignParameters(VariablesSecureApp variables,
      Report report) {
    final String baseDesignPath = _prefix + "/" + _strBaseDesignPath + "/" + _strDefaultDesignPath;
    final HashMap<String, Object> designParameters = new HashMap<String, Object>();

    designParameters.put("DOCUMENT_ID", report.getDocumentId());

    designParameters.put("BASE_ATTACH", _strAttachmentPath);
    designParameters.put("BASE_WEB", _strBaseWeb);
    designParameters.put("BASE_DESIGN", baseDesignPath);
    designParameters.put("IS_IGNORE_PAGINATION", false);
    designParameters.put("USER_CLIENT",
        Utility.getContext(_connectionProvider, variables, "#User_Client", ""));
    designParameters.put("USER_ORG",
        Utility.getContext(_connectionProvider, variables, "#User_Org", ""));

    final String language = variables.getLanguage();
    designParameters.put("LANGUAGE", language);

    final Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));
    designParameters.put("LOCALE", locale);

    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
    dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
    final DecimalFormat NumberFormat = new DecimalFormat(
        variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
    designParameters.put("NUMBERFORMAT", NumberFormat);

    if (report.getTemplateInfo() != null) {
      designParameters.put("SHOW_LOGO", report.getTemplateInfo().getShowLogo());
      designParameters.put("SHOW_COMPANYDATA", report.getTemplateInfo().getShowCompanyData());
      designParameters.put("HEADER_MARGIN", report.getTemplateInfo().getHeaderMargin());
    }

    return designParameters;
  }

}
