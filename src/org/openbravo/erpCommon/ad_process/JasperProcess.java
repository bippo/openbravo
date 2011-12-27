package org.openbravo.erpCommon.ad_process;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;

import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.PrintJRData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.utils.Replace;

public class JasperProcess implements Process {

  static Logger log4j = Logger.getLogger(JasperProcess.class);

  private ProcessLogger logger;

  private ConnectionProvider connection;

  public void initialize(ProcessBundle bundle) {
    logger = bundle.getLogger();
    connection = bundle.getConnection();
  }

  @SuppressWarnings("unchecked")
  public void execute(ProcessBundle bundle) throws Exception {

    HashMap<String, Object> designParameters = null;
    HashMap<Object, Object> exportParameters = null;

    ConfigParameters config = bundle.getConfig();
    VariablesSecureApp vars = bundle.getContext().toVars();
    FieldProvider[] data = (FieldProvider[]) bundle.getParams().get("data");

    String classInfoId = (String) bundle.getParams().get("classInfoId");

    String strReportName = bundle.getImpl();
    String strOutputType = (String) bundle.getParams().get("strOutputType");

    designParameters = (HashMap<String, Object>) bundle.getParams().get("designParameters");
    exportParameters = (HashMap<Object, Object>) bundle.getParams().get("exportParameters");

    if (strReportName == null || strReportName.equals("")) {
      strReportName = PrintJRData.getReportName(connection, classInfoId);
    }

    String strAttach = config.strFTPDirectory + "/284-" + classInfoId;

    String strLanguage = bundle.getContext().getLanguage();
    Locale locLocale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));

    String strBaseDesign = getBaseDesignPath(config);

    strReportName = Replace.replace(Replace.replace(strReportName, "@basedesign@", strBaseDesign),
        "@attach@", strAttach);
    String strFileName = strReportName.substring(strReportName.lastIndexOf("/") + 1);

    // FIXME: os is never assigned, but used leading to an NPE
    ServletOutputStream os = null;
    try {
      JasperReport jasperReport = Utility.getTranslatedJasperReport(connection, strReportName,
          strLanguage, strBaseDesign);

      if (designParameters == null)
        designParameters = new HashMap<String, Object>();

      Boolean pagination = true;
      if (strOutputType.equals("pdf"))
        pagination = false;

      designParameters.put("IS_IGNORE_PAGINATION", pagination);
      // designParameters.put("BASE_WEB", strReplaceWithFull);
      designParameters.put("BASE_DESIGN", strBaseDesign);
      designParameters.put("ATTACH", strAttach);
      designParameters.put("USER_CLIENT", Utility.getContext(connection, vars, "#User_Client", ""));
      designParameters.put("USER_ORG", Utility.getContext(connection, vars, "#User_Org", ""));
      designParameters.put("LANGUAGE", strLanguage);
      designParameters.put("LOCALE", locLocale);

      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator(vars.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
      dfs.setGroupingSeparator(vars.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
      DecimalFormat numberFormat = new DecimalFormat(
          vars.getSessionValue("#AD_ReportNumberFormat"), dfs);
      designParameters.put("NUMBERFORMAT", numberFormat);

      if (log4j.isDebugEnabled())
        log4j.debug("creating the format factory: " + vars.getJavaDateFormat());
      JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(vars.getJavaDateFormat());
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);

      JasperPrint jasperPrint;
      Connection conn = null;
      try {
        conn = connection.getTransactionConnection();
        if (data != null) {
          designParameters.put("REPORT_CONNECTION", conn);
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters,
              new JRFieldProviderDataSource(data, vars.getJavaDateFormat()));
        } else {
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, conn);
        }
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      } finally {
        connection.releaseRollbackConnection(conn);
      }

      if (exportParameters == null)
        exportParameters = new HashMap<Object, Object>();
      if (strOutputType == null || strOutputType.equals(""))
        strOutputType = "html";
      if (strOutputType.equals("html")) {
        if (log4j.isDebugEnabled())
          log4j.debug("JR: Print HTML");
        // response.setHeader( "Content-disposition", "inline" +
        // "; filename=" + strFileName + "." +strOutputType);
        JRHtmlExporter exporter = new JRHtmlExporter();
        exportParameters.put(JRHtmlExporterParameter.JASPER_PRINT, jasperPrint);
        exportParameters.put(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
        exportParameters.put(JRHtmlExporterParameter.SIZE_UNIT,
            JRHtmlExporterParameter.SIZE_UNIT_POINT);
        exportParameters.put(JRHtmlExporterParameter.OUTPUT_STREAM, os);
        exporter.setParameters(exportParameters);
        exporter.exportReport();

      } else if (strOutputType.equals("pdf")) {
        // response.setContentType("application/pdf");
        // response.setHeader( "Content-disposition", "attachment" +
        // "; filename=" + strFileName + "." +strOutputType);
        JasperExportManager.exportReportToPdfStream(jasperPrint, os);

      } else if (strOutputType.equals("xls")) {
        // response.setContentType("application/vnd.ms-excel");
        // response.setHeader( "Content-disposition", "attachment" +
        // "; filename=" + strFileName + "." +strOutputType);
        JExcelApiExporter exporter = new JExcelApiExporter();
        exportParameters.put(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exportParameters.put(JRExporterParameter.OUTPUT_STREAM, os);
        exportParameters.put(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
        exportParameters.put(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
            Boolean.TRUE);

        exporter.setParameters(exportParameters);
        exporter.exportReport();

      } else {
        throw new ServletException("Output format no supported");
      }
    } catch (JRException e) {
      if (log4j.isDebugEnabled())
        log4j.debug("JR: Error: " + e);
      e.printStackTrace();
      throw new ServletException(e.getMessage());

    } catch (Exception e) {
      throw new ServletException(e.getMessage());

    } finally {
      try {
        os.close();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Returns the absolute path to the correct language subfolder within the context's src-loc
   * folder.
   * 
   * @return String with the absolute path on the local drive.
   */
  protected String getBaseDesignPath(ConfigParameters config) {
    if (log4j.isDebugEnabled()) {
      log4j.debug("*********************Base path: " + config.strBaseDesignPath);
    }
    String strNewAddBase = config.strDefaultDesignPath;
    String strFinal = config.strBaseDesignPath;
    if (!strFinal.endsWith("/" + strNewAddBase)) {
      strFinal += "/" + strNewAddBase;
    }
    if (log4j.isDebugEnabled()) {
      log4j.debug("*********************Base path: " + strFinal);
    }
    return config.prefix + strFinal;
  }

}
