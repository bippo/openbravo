/*
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2008-2012 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package org.openbravo.erpCommon.utility.reporting.printing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.erpCommon.utility.poc.EmailType;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.Report.OutputTypeEnum;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.erpCommon.utility.reporting.TemplateData;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo.EmailDefinition;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

@SuppressWarnings("serial")
public class PrintController extends HttpSecureAppServlet {
  private final Map<String, TemplateData[]> differentDocTypes = new HashMap<String, TemplateData[]>();
  private PocData[] pocData;
  private boolean multiReports = false;
  private boolean archivedReports = false;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    DocumentType documentType = DocumentType.UNKNOWN;
    String sessionValuePrefix = null;
    String strDocumentId = null;

    // Determine which process called the print controller
    if (log4j.isDebugEnabled())
      log4j.debug("Servletpath: " + request.getServletPath());
    if (request.getServletPath().toLowerCase().indexOf("quotations") != -1) {
      documentType = DocumentType.QUOTATION;
      // The prefix PRINTORDERS is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTQUOTATIONS";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId");
    }
    if (request.getServletPath().toLowerCase().indexOf("orders") != -1) {
      documentType = DocumentType.SALESORDER;
      // The prefix PRINTORDERS is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTORDERS";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId");
    }
    if (request.getServletPath().toLowerCase().indexOf("invoices") != -1) {
      documentType = DocumentType.SALESINVOICE;
      // The prefix PRINTINVOICES is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTINVOICES";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcInvoiceId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcInvoiceId");
    }
    if (request.getServletPath().toLowerCase().indexOf("shipments") != -1) {
      documentType = DocumentType.SHIPMENT;
      // The prefix PRINTINVOICES is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTSHIPMENTS";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpmInoutId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpmInoutId");
    }
    if (request.getServletPath().toLowerCase().indexOf("payments") != -1) {
      documentType = DocumentType.PAYMENT;
      // The prefix PRINTPAYMENTS is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTPAYMENT";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpfinPaymentId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpfinPaymentId");
    }

    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId);

  }

  @SuppressWarnings("unchecked")
  void post(HttpServletRequest request, HttpServletResponse response, VariablesSecureApp vars,
      DocumentType documentType, String sessionValuePrefix, String strDocumentId)
      throws IOException, ServletException {
    try {

      Map<String, Report> reports;

      // Checks are maintained in this way for mulithread safety
      HashMap<String, Boolean> checks = new HashMap<String, Boolean>();
      checks.put("moreThanOneCustomer", Boolean.FALSE);
      checks.put("moreThanOnesalesRep", Boolean.FALSE);

      String documentIds[] = null;
      if (log4j.isDebugEnabled())
        log4j.debug("strDocumentId: " + strDocumentId);
      // normalize the string of ids to a comma separated list
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
      if (strDocumentId.length() == 0)
        throw new ServletException(Utility.messageBD(this, "NoDocument", vars.getLanguage()));

      documentIds = strDocumentId.split(",");

      if (log4j.isDebugEnabled())
        log4j.debug("Number of documents selected: " + documentIds.length);

      multiReports = (documentIds.length > 1);

      reports = (Map<String, Report>) vars.getSessionObject(sessionValuePrefix + ".Documents");
      final ReportManager reportManager = new ReportManager(this, globalParameters.strFTPDirectory,
          strReplaceWithFull, globalParameters.strBaseDesignPath,
          globalParameters.strDefaultDesignPath, globalParameters.prefix, multiReports);

      if (vars.commandIn("PRINT")) {
        archivedReports = false;
        // Order documents by Document No.
        if (multiReports)
          documentIds = orderByDocumentNo(documentType, documentIds);

        /*
         * PRINT option will print directly to the UI for a single report. For multiple reports the
         * documents will each be saved individually and the concatenated in the same manner as the
         * saved reports. After concatenating the reports they will be deleted.
         */
        Report report = null;
        JasperPrint jasperPrint = null;
        Collection<JasperPrint> jrPrintReports = new ArrayList<JasperPrint>();
        final Collection<Report> savedReports = new ArrayList<Report>();
        for (int i = 0; i < documentIds.length; i++) {
          String documentId = documentIds[i];
          report = buildReport(response, vars, documentId, reportManager, documentType,
              Report.OutputTypeEnum.PRINT);
          try {
            jasperPrint = reportManager.processReport(report, vars);
            jrPrintReports.add(jasperPrint);
          } catch (final ReportingException e) {
            advisePopUp(request, response, "Report processing failed",
                "Unable to process report selection");
            log4j.error(e.getMessage());
            e.getStackTrace();
          }
          savedReports.add(report);
          if (multiReports) {
            reportManager.saveTempReport(report, vars);
          }
        }
        printReports(response, jrPrintReports, savedReports);
      } else if (vars.commandIn("ARCHIVE")) {
        // Order documents by Document No.
        if (multiReports)
          documentIds = orderByDocumentNo(documentType, documentIds);

        /*
         * ARCHIVE will save each report individually and then print the reports in a single
         * printable (concatenated) format.
         */
        archivedReports = true;
        Report report = null;
        final Collection<Report> savedReports = new ArrayList<Report>();
        for (int index = 0; index < documentIds.length; index++) {
          String documentId = documentIds[index];
          report = buildReport(response, vars, documentId, reportManager, documentType,
              OutputTypeEnum.ARCHIVE);
          buildReport(response, vars, documentId, reports, reportManager);
          try {
            reportManager.processReport(report, vars);
          } catch (final ReportingException e) {
            log4j.error(e);
          }
          reportManager.saveTempReport(report, vars);
          savedReports.add(report);
        }
        printReports(response, null, savedReports);
      } else {
        if (vars.commandIn("DEFAULT")) {

          reports = new HashMap<String, Report>();
          for (int index = 0; index < documentIds.length; index++) {
            final String documentId = documentIds[index];
            if (log4j.isDebugEnabled())
              log4j.debug("Processing document with id: " + documentId);

            try {
              final Report report = new Report(this, documentType, documentId, vars.getLanguage(),
                  "default", multiReports, OutputTypeEnum.DEFAULT);
              reports.put(documentId, report);

              final String senderAddress = EmailData.getSenderAddress(this, vars.getClient(),
                  report.getOrgId());
              boolean moreThanOnesalesRep = checks.get("moreThanOnesalesRep").booleanValue();

              if (request.getServletPath().toLowerCase().indexOf("print.html") == -1) {
                if ("".equals(senderAddress) || senderAddress == null) {
                  final OBError on = new OBError();
                  on.setMessage(Utility.messageBD(this, "NoSender", vars.getLanguage()));
                  on.setTitle(Utility.messageBD(this, "EmailConfigError", vars.getLanguage()));
                  on.setType("Error");
                  final String tabId = vars.getSessionValue("inpTabId");
                  vars.getStringParameter("tab");
                  vars.setMessage(tabId, on);
                  vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
                  printPageClosePopUpAndRefreshParent(response, vars);
                  throw new ServletException("Configuration Error no sender defined");
                }
              }

              // check the different doc typeId's if all the selected
              // doc's
              // has the same doc typeId the template selector should
              // appear
              if (!differentDocTypes.containsKey(report.getDocTypeId())) {
                differentDocTypes.put(report.getDocTypeId(), report.getTemplate());
              }
            } catch (final ReportingException exception) {
              throw new ServletException(exception);
            }

          }

          vars.setSessionObject(sessionValuePrefix + ".Documents", reports);

          if (request.getServletPath().toLowerCase().indexOf("print.html") != -1)
            createPrintOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports);
          else
            createEmailOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports, checks);

        } else if (vars.commandIn("ADD")) {
          if (request.getServletPath().toLowerCase().indexOf("print.html") != -1)
            createPrintOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports);
          else {
            final boolean showList = true;
            createEmailOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports, checks);
          }

        } else if (vars.commandIn("DEL")) {
          final String documentToDelete = vars.getStringParameter("idToDelete");
          final Vector<Object> vector = (Vector<Object>) request.getSession().getAttribute("files");
          request.getSession().setAttribute("files", vector);

          seekAndDestroy(vector, documentToDelete);
          createEmailOptionsPage(request, response, vars, documentType,
              getComaSeparatedString(documentIds), reports, checks);

        } else if (vars.commandIn("EMAIL")) {
          int nrOfEmailsSend = 0;
          for (final PocData documentData : pocData) {
            getEnvironentInformation(pocData, checks);
            final String documentId = documentData.documentId;
            if (log4j.isDebugEnabled())
              log4j.debug("Processing document with id: " + documentId);

            String templateInUse = "default";
            if (differentDocTypes.size() == 1) {
              templateInUse = vars.getRequestGlobalVariable("templates", "templates");
            }

            final Report report = buildReport(response, vars, documentId, reportManager,
                documentType, OutputTypeEnum.EMAIL, templateInUse);

            // if there is only one document type id the user should be
            // able to choose between different templates
            if (differentDocTypes.size() == 1) {
              final String templateId = vars.getRequestGlobalVariable("templates", "templates");
              try {
                final TemplateInfo usedTemplateInfo = new TemplateInfo(this, report.getDocTypeId(),
                    report.getOrgId(), vars.getLanguage(), templateId);
                report.setTemplateInfo(usedTemplateInfo);
              } catch (final ReportingException e) {
                throw new ServletException("Error trying to get template information", e);
              }
            }

            if (report == null)
              throw new ServletException(
                  Utility.messageBD(this, "NoDataReport", vars.getLanguage()) + documentId);
            // Check if the document is not in status 'draft'
            if (!report.isDraft()) {
              // Check if the report is already attached
              if (!report.isAttached()) {
                // get the Id of the entities table, this is used to
                // store the file as an OB attachment
                final String tableId = ToolsData.getTableId(this, report.getDocumentType()
                    .getTableName());

                // If the user wants to archive the document
                if (vars.getStringParameter("inpArchive").equals("Y")) {
                  // Save the report as a attachment because it is
                  // being transferred to the user
                  try {
                    reportManager.createAttachmentForReport(this, report, tableId, vars);
                  } catch (final ReportingException exception) {
                    throw new ServletException(exception);
                  }
                } else {
                  reportManager.saveTempReport(report, vars);
                }
              } else {
                if (log4j.isDebugEnabled())
                  log4j.debug("Document is not attached.");
              }
              final String senderAddress = vars.getStringParameter("fromEmail");
              sendDocumentEmail(report, vars,
                  (Vector<Object>) request.getSession().getAttribute("files"), documentData,
                  senderAddress, checks);
              nrOfEmailsSend++;
            }
          }
          request.getSession().removeAttribute("files");
          createPrintStatusPage(response, vars, nrOfEmailsSend);
        } else if (vars.commandIn("UPDATE_TEMPLATE")) {
          JSONObject o = new JSONObject();
          try {
            final String templateId = vars.getRequestGlobalVariable("templates", "templates");
            final String documentId = pocData[0].documentId;
            for (final PocData documentData : pocData) {
              final Report report = new Report(this, documentType, documentId, vars.getLanguage(),
                  templateId, multiReports, OutputTypeEnum.DEFAULT);
              o.put("templateId", templateId);
              o.put("subject", report.getEmailDefinition().getSubject());
              o.put("body", report.getEmailDefinition().getBody());
              if (!multiReports) {
                o.put("filename", report.getFilename());
              }
              reports = new HashMap<String, Report>();
              reports.put(documentId, report);
            }
            vars.setSessionObject(sessionValuePrefix + ".Documents", reports);

          } catch (Exception e) {
            log4j.error("Error in change template ajax", e);
            o = new JSONObject();
            try {
              o.put("error", true);
            } catch (JSONException e1) {
              log4j.error("Error in change template ajax", e1);
            }
          }

          response.setContentType("application/json");
          final PrintWriter out = response.getWriter();
          out.println(o.toString());
          out.close();
        }

        pageError(response);
      }
    } catch (Exception e) {
      // Catching the exception here instead of throwing it to HSAS because this is used in multi
      // part request making the mechanism to detect popup not to work.
      log4j.error("Error captured: ", e);
      bdErrorGeneralPopUp(request, response, "Error",
          Utility.translateError(this, vars, vars.getLanguage(), e.getMessage()).getMessage());
    }
  }

  private void printReports(HttpServletResponse response, Collection<JasperPrint> jrPrintReports,
      Collection<Report> reports) {
    ServletOutputStream os = null;
    String filename = "";
    try {
      os = response.getOutputStream();
      response.setContentType("application/pdf");

      if (!multiReports && !archivedReports) {
        for (Iterator<Report> iterator = reports.iterator(); iterator.hasNext();) {
          Report report = iterator.next();
          filename = report.getFilename();
        }
        response.setHeader("Content-disposition", "attachment" + "; filename=" + filename);
        for (Iterator<JasperPrint> iterator = jrPrintReports.iterator(); iterator.hasNext();) {
          JasperPrint jasperPrint = (JasperPrint) iterator.next();
          JasperExportManager.exportReportToPdfStream(jasperPrint, os);
        }
      } else {
        response.setContentType("application/pdf");
        concatReport(reports.toArray(new Report[] {}), response);
      }
    } catch (IOException e) {
      log4j.error(e.getMessage());
    } catch (JRException e) {
      e.printStackTrace();
    } finally {
      try {
        os.close();
        response.flushBuffer();
      } catch (IOException e) {
        log4j.error(e.getMessage(), e);
      }
    }
  }

  /*
   * This method is base on code originally created by Mark Thompson (Concatenate.java) and
   * distributed under the following conditions.
   * 
   * $Id: Concatenate.java 3373 2008-05-12 16:21:24Z xlv $
   * 
   * This code is free software. It may only be copied or modified if you include the following
   * copyright notice:
   * 
   * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
   * 
   * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
   * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   */
  private void concatReport(Report[] reports, HttpServletResponse response) {
    try {
      int pageOffset = 0;
      // ArrayList master = new ArrayList();
      int f = 0;
      String filename = "";
      Report outFile = null;
      if (reports.length == 1)
        filename = reports[0].getFilename();
      Document document = null;
      PdfCopy writer = null;
      while (f < reports.length) {
        if (filename == null || filename.equals("")) {
          outFile = reports[f];
          if (multiReports) {
            filename = outFile.getTemplateInfo().getReportFilename();
            filename = filename.replaceAll("@our_ref@", "");
            filename = filename.replaceAll("@cus_ref@", "");
            filename = filename.replaceAll(" ", "_");
            filename = filename.replaceAll("-", "");
            filename = filename + ".pdf";
          } else {
            filename = outFile.getFilename();
          }
        }
        response.setHeader("Content-disposition", "attachment" + "; filename=" + filename);
        // we create a reader for a certain document
        PdfReader reader = new PdfReader(reports[f].getTargetLocation());
        reader.consolidateNamedDestinations();
        // we retrieve the total number of pages
        int n = reader.getNumberOfPages();
        pageOffset += n;

        if (f == 0) {
          // step 1: creation of a document-object
          document = new Document(reader.getPageSizeWithRotation(1));
          // step 2: we create a writer that listens to the document
          writer = new PdfCopy(document, response.getOutputStream());
          // step 3: we open the document
          document.open();
        }
        // step 4: we add content
        PdfImportedPage page;
        for (int i = 0; i < n;) {
          ++i;
          page = writer.getImportedPage(reader, i);
          writer.addPage(page);
        }
        if (reports[f].isDeleteable()) {
          File file = new File(reports[f].getTargetLocation());
          if (file.exists() && !file.isDirectory()) {
            file.delete();
          }
        }
        f++;
      }
      document.close();
    } catch (Exception e) {
      log4j.error(e);
    }
  }

  private Report buildReport(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentId, final ReportManager reportManager, DocumentType documentType,
      OutputTypeEnum outputType) {
    return buildReport(response, vars, strDocumentId, reportManager, documentType, outputType,
        "default");
  }

  private Report buildReport(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentId, final ReportManager reportManager, DocumentType documentType,
      OutputTypeEnum outputType, String templateId) {
    Report report = null;
    if (strDocumentId != null) {
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
    }
    try {
      report = new Report(this, documentType, strDocumentId, vars.getLanguage(), templateId,
          multiReports, outputType);
    } catch (final ReportingException e) {
      log4j.error(e);
    } catch (final ServletException e) {
      log4j.error(e);
    }

    reportManager.setTargetDirectory(report);
    return report;
  }

  private void buildReport(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentId, Map<String, Report> reports, final ReportManager reportManager)
      throws ServletException, IOException {
    final String documentId = vars.getStringParameter("inpDocumentId");
    if (strDocumentId != null)
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
    final Report report = reports.get(strDocumentId);
    if (report == null)
      throw new ServletException(Utility.messageBD(this, "NoDataReport", vars.getLanguage())
          + documentId);
    // Check if the document is not in status 'draft'
    if (!report.isDraft() && !report.isAttached() && vars.commandIn("ARCHIVE")) {
      // TODO: Move the table Id retrieval into the DocumentType
      // getTableId method!
      // get the Id of the entities table, this is used to store the
      // file as an OB attachment
      final String tableId = ToolsData.getTableId(this, report.getDocumentType().getTableName());

      if (log4j.isDebugEnabled())
        log4j.debug("Table " + report.getDocumentType().getTableName() + " has table id: "
            + tableId);
      // Save the report as a attachment because it is being
      // transferred to the user
      try {
        reportManager.createAttachmentForReport(this, report, tableId, vars);
      } catch (final ReportingException exception) {
        throw new ServletException(exception);
      }
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("Document is not attached.");
    }
  }

  /**
   * 
   * @param vector
   * @param documentToDelete
   */
  private void seekAndDestroy(Vector<Object> vector, String documentToDelete) {
    for (int i = 0; i < vector.size(); i++) {
      final AttachContent content = (AttachContent) vector.get(i);
      if (content.id.equals(documentToDelete)) {
        vector.remove(i);
        break;
      }
    }

  }

  PocData[] getContactDetails(DocumentType documentType, String strDocumentId)
      throws ServletException {
    switch (documentType) {
    case QUOTATION:
      return PocData.getContactDetailsForOrders(this, strDocumentId);
    case SALESORDER:
      return PocData.getContactDetailsForOrders(this, strDocumentId);
    case SALESINVOICE:
      return PocData.getContactDetailsForInvoices(this, strDocumentId);
    case SHIPMENT:
      return PocData.getContactDetailsForShipments(this, strDocumentId);
    case PURCHASEORDER:
      return PocData.getContactDetailsForOrders(this, strDocumentId);
    case PAYMENT:
      return PocData.getContactDetailsForPayments(this, strDocumentId);
    }
    return null;
  }

  void sendDocumentEmail(Report report, VariablesSecureApp vars, Vector<Object> object,
      PocData documentData, String senderAddress, HashMap<String, Boolean> checks)
      throws IOException, ServletException {
    final String attachmentFileLocation = report.getTargetLocation();

    final String ourReference = report.getOurReference();
    final String cusReference = report.getCusReference();
    if (log4j.isDebugEnabled())
      log4j.debug("our document ref: " + ourReference);
    if (log4j.isDebugEnabled())
      log4j.debug("cus document ref: " + cusReference);

    final String toName = documentData.contactName;
    String toEmail = null;
    final String replyToName = documentData.salesrepName;
    String replyToEmail = null;

    boolean moreThanOneCustomer = checks.get("moreThanOneCustomer").booleanValue();
    boolean moreThanOnesalesRep = checks.get("moreThanOnesalesRep").booleanValue();
    if (moreThanOneCustomer) {
      toEmail = documentData.contactEmail;
    } else {
      toEmail = vars.getStringParameter("toEmail");
    }

    if (moreThanOnesalesRep) {
      replyToEmail = documentData.salesrepEmail;
    } else {
      replyToEmail = vars.getStringParameter("replyToEmail");
    }
    String emailSubject = vars.getStringParameter("emailSubject");
    String emailBody = vars.getStringParameter("emailBody");

    // TODO: Move this to the beginning of the print handling and do nothing
    // if these conditions fail!!!)

    if ((replyToEmail == null || replyToEmail.length() == 0)) {
      throw new ServletException(Utility.messageBD(this, "NoSalesRepEmail", vars.getLanguage()));
    }

    if ((toEmail == null || toEmail.length() == 0)) {
      throw new ServletException(Utility.messageBD(this, "NoCustomerEmail", vars.getLanguage()));
    }

    // Replace special tags

    emailSubject = emailSubject.replaceAll("@cus_ref@", cusReference);
    emailSubject = emailSubject.replaceAll("@our_ref@", ourReference);
    emailSubject = emailSubject.replaceAll("@cus_nam@", toName);
    emailSubject = emailSubject.replaceAll("@sal_nam@", replyToName);

    emailBody = emailBody.replaceAll("@cus_ref@", cusReference);
    emailBody = emailBody.replaceAll("@our_ref@", ourReference);
    emailBody = emailBody.replaceAll("@cus_nam@", toName);
    emailBody = emailBody.replaceAll("@sal_nam@", replyToName);

    String host = null;
    boolean auth = true;
    String username = null;
    String password = null;
    String connSecurity = null;
    int port = 25;

    OBContext.setAdminMode(true);
    try {
      final EmailServerConfiguration mailConfig = OBDal.getInstance().get(
          EmailServerConfiguration.class, vars.getStringParameter("fromEmailId"));

      host = mailConfig.getSmtpServer();

      if ("N".equals(mailConfig.isSMTPAuthentification())) {
        auth = false;
      }
      username = mailConfig.getSmtpServerAccount();
      password = FormatUtilities.encryptDecrypt(mailConfig.getSmtpServerPassword(), false);
      connSecurity = mailConfig.getSmtpConnectionSecurity();
      port = mailConfig.getSmtpPort().intValue();
    } finally {
      OBContext.restorePreviousMode();
    }

    final String recipientTO = toEmail;
    final String recipientCC = vars.getStringParameter("ccEmail");
    final String recipientBCC = vars.getStringParameter("bccEmail");
    final String replyTo = replyToEmail;
    final String contentType = "text/plain; charset=utf-8";

    if (log4j.isDebugEnabled()) {
      log4j.debug("From: " + senderAddress);
      log4j.debug("Recipient TO (contact email): " + recipientTO);
      log4j.debug("Recipient CC: " + recipientCC);
      log4j.debug("Recipient BCC (user email): " + recipientBCC);
      log4j.debug("Reply-to (sales rep email): " + replyTo);
    }

    List<File> attachments = new ArrayList<File>();
    attachments.add(new File(attachmentFileLocation));

    if (object != null) {
      final Vector<Object> vector = (Vector<Object>) object;
      for (int i = 0; i < vector.size(); i++) {
        final AttachContent objContent = (AttachContent) vector.get(i);
        final File file = prepareFile(objContent, ourReference);
        attachments.add(file);
      }
    }

    try {
      EmailManager.sendEmail(host, auth, username, password, connSecurity, port, senderAddress,
          recipientTO, recipientCC, recipientBCC, replyTo, emailSubject, emailBody, contentType,
          attachments, null, null);
    } catch (Exception exception) {
      log4j.error(exception);
      final String exceptionClass = exception.getClass().toString().replace("class ", "");
      String exceptionString = "Problems while sending the email" + exception;
      exceptionString = exceptionString.replace(exceptionClass, "");
      throw new ServletException(exceptionString);
    }

    // Store the email in the database
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();

      // First store the email message
      final String newEmailId = SequenceIdData.getUUID();
      if (log4j.isDebugEnabled())
        log4j.debug("New email id: " + newEmailId);

      EmailData.insertEmail(conn, this, newEmailId, vars.getClient(), vars.getOrg(),
          vars.getUser(), EmailType.OUTGOING.getStringValue(), replyTo, recipientTO, recipientCC,
          recipientBCC, Utility.formatDate(new Date(), "yyyyMMddHHmmss"), emailSubject, emailBody,
          report.getBPartnerId(),
          ToolsData.getTableId(this, report.getDocumentType().getTableName()),
          documentData.documentId);

      releaseCommitConnection(conn);
    } catch (final NoConnectionAvailableException exception) {
      log4j.error(exception);
      throw new ServletException(exception);
    } catch (final SQLException exception) {
      log4j.error(exception);
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }

      throw new ServletException(exception);
    }

  }

  void createPrintOptionsPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String strDocumentId,
      Map<String, Report> reports) throws IOException, ServletException {
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/reporting/printing/PrintOptions").createXmlDocument();
    xmlDocument.setParameter("strDocumentId", strDocumentId);

    // Get additional document information
    final String draftDocumentIds = "";
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("language", vars.getLanguage());
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", "");
    xmlDocument.setParameter("help", "");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void createEmailOptionsPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String strDocumentId,
      Map<String, Report> reports, HashMap<String, Boolean> checks) throws IOException,
      ServletException {
    XmlDocument xmlDocument = null;
    pocData = getContactDetails(documentType, strDocumentId);
    @SuppressWarnings("unchecked")
    Vector<java.lang.Object> vector = (Vector<java.lang.Object>) request.getSession().getAttribute(
        "files");

    final String[] hiddenTags = getHiddenTags(pocData, vector, vars, checks);
    if (hiddenTags != null) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/utility/reporting/printing/EmailOptions", hiddenTags)
          .createXmlDocument();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/utility/reporting/printing/EmailOptions").createXmlDocument();
    }

    xmlDocument.setParameter("strDocumentId", strDocumentId);

    boolean isTheFirstEntry = false;
    if (vector == null) {
      vector = new Vector<java.lang.Object>(0);
      isTheFirstEntry = new Boolean(true);
    }

    if (vars.getMultiFile("inpFile") != null && !vars.getMultiFile("inpFile").getName().equals("")) {
      final AttachContent content = new AttachContent();
      final FileItem file1 = vars.getMultiFile("inpFile");
      content.setFileName(pocData[0].ourreference.replace('/', '_') + '-'
          + Utility.formatDate(new Date(), "yyyyMMdd-HHmmss") + '.' + file1.getName());
      content.setFileItem(file1);
      content.setId(Utility.formatDate(new Date(), "yyyyMMdd-HHmmss") + '.' + file1.getName());
      content.visible = "hidden";
      if (vars.getStringParameter("inpArchive") == "Y") {
        content.setSelected("true");
      }
      vector.addElement(content);
      request.getSession().setAttribute("files", vector);

    }

    if ("yes".equals(vars.getStringParameter("closed"))) {
      xmlDocument.setParameter("closed", "yes");
      request.getSession().removeAttribute("files");
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("language", vars.getLanguage());
    xmlDocument.setParameter("theme", vars.getTheme());

    EmailDefinition emailDefinition = null;
    try {
      if (moreThanOneLenguageDefined(reports)) {
        emailDefinition = reports.values().iterator().next().getTemplateInfo()
            .get_DefaultEmailDefinition();
        if (emailDefinition == null) {
          throw new OBException("No default lenguage configured");
        }
      } else {
        emailDefinition = reports.values().iterator().next().getEmailDefinition();
      }
    } catch (final OBException exception) {
      final OBError on = new OBError();
      on.setMessage(Utility.messageBD(this, "EmailConfiguration", vars.getLanguage()));
      on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
      on.setType("info");
      final String tabId = vars.getSessionValue("inpTabId");
      vars.getStringParameter("tab");
      vars.setMessage(tabId, on);
      vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
      printPageClosePopUpAndRefreshParent(response, vars);
    } catch (ReportingException e) {
      log4j.error(e);
    }

    String fromEmail = null;
    String fromEmailId = null;

    OBContext.setAdminMode(true);
    try {
      OBCriteria<EmailServerConfiguration> mailConfigCriteria = OBDal.getInstance().createCriteria(
          EmailServerConfiguration.class);
      mailConfigCriteria.addOrderBy("client.id", false);
      final List<EmailServerConfiguration> mailConfigList = mailConfigCriteria.list();

      if (mailConfigList.size() == 0) {
        throw new ServletException("No Poc configuration found for this client.");
      }

      // TODO: There should be a mechanism to select the desired Email server configuration, until
      // then, first search for the current organization (and use the first returned one), then for
      // organization '0' (and use the first returned one) and then for any other of the
      // organization tree where current organization belongs to (and use the first returned one).
      EmailServerConfiguration mailConfig = null;

      for (EmailServerConfiguration currentOrgConfig : mailConfigList) {
        if (vars.getOrg().equals(currentOrgConfig.getOrganization().getId())) {
          mailConfig = currentOrgConfig;
          break;
        }
      }
      if (mailConfig == null) {
        for (EmailServerConfiguration zeroOrgConfig : mailConfigList) {
          if ("0".equals(zeroOrgConfig.getOrganization().getId())) {
            mailConfig = zeroOrgConfig;
            break;
          }
        }
      }
      if (mailConfig == null) {
        mailConfig = mailConfigList.get(0);
      }

      fromEmail = mailConfig.getSmtpServerSenderAddress();
      fromEmailId = mailConfig.getId();
    } finally {
      OBContext.restorePreviousMode();
    }

    // Get additional document information
    String draftDocumentIds = "";
    final AttachContent attachedContent = new AttachContent();
    final boolean onlyOneAttachedDoc = onlyOneAttachedDocs(reports);
    final Map<String, PocData> customerMap = new HashMap<String, PocData>();
    final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
    final Vector<Object> cloneVector = new Vector<Object>();
    boolean allTheDocsCompleted = true;
    for (final PocData documentData : pocData) {
      // Map used to count the different users

      final String customer = documentData.contactName;
      getEnvironentInformation(pocData, checks);
      if (checks.get("moreThanOneDoc")) {
        if (customer == null || customer.length() == 0) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoContact", vars.getLanguage()).replace(
              "@docNum@", documentData.ourreference));

          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        } else if (documentData.contactEmail == null || documentData.contactEmail.equals("")) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoEmail", vars.getLanguage()).replace(
              "@customer@", customer));
          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        }
      }

      if (!customerMap.containsKey(customer)) {
        customerMap.put(customer, documentData);
      }

      final String salesRep = documentData.salesrepName;

      boolean moreThanOnesalesRep = checks.get("moreThanOnesalesRep").booleanValue();
      if (moreThanOnesalesRep) {
        if (salesRep == null || salesRep.length() == 0) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoSenderDocument", vars.getLanguage()));
          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        } else if (documentData.salesrepEmail == null || documentData.salesrepEmail.equals("")) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoEmailSender", vars.getLanguage()).replace(
              "@salesRep@", salesRep));
          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        }
      }

      if (!salesRepMap.containsKey(salesRep)) {
        salesRepMap.put(salesRep, documentData);
      }

      final Report report = reports.get(documentData.documentId);
      // All ids of documents in draft are passed to the web client
      if (report.isDraft()) {
        if (draftDocumentIds.length() > 0)
          draftDocumentIds += ",";
        draftDocumentIds += report.getDocumentId();
        allTheDocsCompleted = false;
      }

      // Fill the report location
      final String reportFilename = report.getContextSubFolder() + report.getFilename();
      documentData.reportLocation = request.getContextPath() + "/" + reportFilename
          + "?documentId=" + documentData.documentId;
      if (log4j.isDebugEnabled())
        log4j.debug(" Filling report location with: " + documentData.reportLocation);

      if (onlyOneAttachedDoc) {
        attachedContent.setDocName(report.getFilename());
        attachedContent.setVisible("checkbox");
        cloneVector.add(attachedContent);
      }

    }
    if (!allTheDocsCompleted) {
      final OBError on = new OBError();
      on.setMessage(Utility.messageBD(this, "ErrorIncompleteDocuments", vars.getLanguage()));
      on.setTitle(Utility.messageBD(this, "ErrorSendingEmail", vars.getLanguage()));
      on.setType("Error");
      final String tabId = vars.getSessionValue("inpTabId");
      vars.getStringParameter("tab");
      vars.setMessage(tabId, on);
      vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
      printPageClosePopUpAndRefreshParent(response, vars);
    }

    final int numberOfCustomers = customerMap.size();
    final int numberOfSalesReps = salesRepMap.size();

    if (!onlyOneAttachedDoc && isTheFirstEntry) {
      if (numberOfCustomers > 1) {
        attachedContent.setDocName(String.valueOf(reports.size() + " Documents to "
            + String.valueOf(numberOfCustomers) + " Customers"));
        attachedContent.setVisible("checkbox");

      } else {
        attachedContent.setDocName(String.valueOf(reports.size() + " Documents"));
        attachedContent.setVisible("checkbox");

      }
      cloneVector.add(attachedContent);
    }

    final AttachContent[] data = new AttachContent[vector.size()];
    final AttachContent[] data2 = new AttachContent[cloneVector.size()];
    if (cloneVector.size() >= 1) { // Has more than 1 element
      vector.copyInto(data);
      cloneVector.copyInto(data2);
      xmlDocument.setData("structure2", data2);
      xmlDocument.setData("structure1", data);
    }
    if (pocData.length >= 1) {
      xmlDocument.setData("reportEmail", "liststructure", reports.get((pocData[0].documentId))
          .getTemplate());
    }

    if (log4j.isDebugEnabled())
      log4j.debug("Documents still in draft: " + draftDocumentIds);
    xmlDocument.setParameter("draftDocumentIds", draftDocumentIds);

    final PocData[] currentUserInfo = PocData.getContactDetailsForUser(this, vars.getUser());
    final String userName = currentUserInfo[0].userName;
    final String userEmail = currentUserInfo[0].userEmail;
    String bccEmail = "";
    String bccName = "";
    if (userEmail != null && userEmail.length() > 0) {
      bccEmail = userEmail;
      bccName = userName;
    }

    if (vars.commandIn("ADD") || vars.commandIn("DEL")) {
      xmlDocument.setParameter("fromEmailId", vars.getStringParameter("fromEmailId"));
      xmlDocument.setParameter("fromEmail", vars.getStringParameter("fromEmail"));
      xmlDocument.setParameter("toEmail", vars.getStringParameter("toEmail"));
      xmlDocument.setParameter("toEmailOrig", vars.getStringParameter("toEmailOrig"));
      xmlDocument.setParameter("ccEmail", vars.getStringParameter("ccEmail"));
      xmlDocument.setParameter("ccEmailOrig", vars.getStringParameter("ccEmailOrig"));
      xmlDocument.setParameter("bccEmail", vars.getStringParameter("bccEmail"));
      xmlDocument.setParameter("bccEmailOrig", vars.getStringParameter("bccEmailOrig"));
      xmlDocument.setParameter("replyToEmail", vars.getStringParameter("replyToEmail"));
      xmlDocument.setParameter("replyToEmailOrig", vars.getStringParameter("replyToEmailOrig"));
      xmlDocument.setParameter("emailSubject", vars.getStringParameter("emailSubject"));
      xmlDocument.setParameter("emailBody", vars.getStringParameter("emailBody"));
    } else {
      xmlDocument.setParameter("fromEmailId", fromEmailId);
      xmlDocument.setParameter("fromEmail", fromEmail);
      xmlDocument.setParameter("toEmail", pocData[0].contactEmail);
      xmlDocument.setParameter("toEmailOrig", pocData[0].contactEmail);
      xmlDocument.setParameter("ccEmail", "");
      xmlDocument.setParameter("ccEmailOrig", "");
      xmlDocument.setParameter("bccEmail", bccEmail);
      xmlDocument.setParameter("bccEmailOrig", bccEmail);
      xmlDocument.setParameter("replyToEmail", pocData[0].salesrepEmail);
      xmlDocument.setParameter("replyToEmailOrig", pocData[0].salesrepEmail);
      xmlDocument.setParameter("emailSubject", emailDefinition.getSubject());
      xmlDocument.setParameter("emailBody", emailDefinition.getBody());
    }
    xmlDocument.setParameter("inpArchive", vars.getStringParameter("inpArchive"));
    xmlDocument.setParameter("fromName", "");
    xmlDocument.setParameter("toName", pocData[0].contactName);
    xmlDocument.setParameter("ccName", "");
    xmlDocument.setParameter("bccName", bccName);
    xmlDocument.setParameter("replyToName", pocData[0].salesrepName);
    xmlDocument.setParameter("inpArchive", vars.getStringParameter("inpArchive"));
    xmlDocument.setParameter("multCusCount", String.valueOf(numberOfCustomers));
    xmlDocument.setParameter("multSalesRepCount", String.valueOf(numberOfSalesReps));
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private boolean moreThanOneLenguageDefined(Map<String, Report> reports) throws ReportingException {
    @SuppressWarnings("rawtypes")
    Iterator itRep = reports.values().iterator();
    HashMap<String, String> lenguages = new HashMap<String, String>();
    while (itRep.hasNext()) {
      Report report = (Report) itRep.next();
      lenguages.put(report.getEmailDefinition().getLanguage(), report.getEmailDefinition()
          .getLanguage());
    }
    return ((lenguages.values().size() > 1) ? true : false);
  }

  private void getEnvironentInformation(PocData[] pocData, HashMap<String, Boolean> checks) {
    final Map<String, PocData> customerMap = new HashMap<String, PocData>();
    final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
    int docCounter = 0;
    checks.put("moreThanOneDoc", false);
    for (final PocData documentData : pocData) {
      // Map used to count the different users
      docCounter++;
      final String customer = documentData.contactName;
      final String salesRep = documentData.salesrepName;
      if (!customerMap.containsKey(customer)) {
        customerMap.put(customer, documentData);
      }
      if (!salesRepMap.containsKey(salesRep)) {
        salesRepMap.put(salesRep, documentData);
      }
    }
    if (docCounter > 1) {
      checks.put("moreThanOneDoc", true);
    }
    boolean moreThanOneCustomer = (customerMap.size() > 1);
    boolean moreThanOnesalesRep = (salesRepMap.size() > 1);
    checks.put("moreThanOneCustomer", new Boolean(moreThanOneCustomer));
    checks.put("moreThanOnesalesRep", new Boolean(moreThanOnesalesRep));
  }

  /**
   * @author gmauleon
   * @param pocData
   * @param vars
   * @param vector
   * @return
   */
  private String[] getHiddenTags(PocData[] pocData, Vector<Object> vector, VariablesSecureApp vars,
      HashMap<String, Boolean> checks) {
    String[] discard;
    final Map<String, PocData> customerMap = new HashMap<String, PocData>();
    final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
    for (final PocData documentData : pocData) {
      // Map used to count the different users

      final String customer = documentData.contactName;
      final String salesRep = documentData.salesrepName;
      if (!customerMap.containsKey(customer)) {
        customerMap.put(customer, documentData);
      }
      if (!salesRepMap.containsKey(salesRep)) {
        salesRepMap.put(salesRep, documentData);
      }
    }
    boolean moreThanOneCustomer = (customerMap.size() > 1);
    boolean moreThanOnesalesRep = (salesRepMap.size() > 1);
    checks.put("moreThanOneCustomer", new Boolean(moreThanOneCustomer));
    checks.put("moreThanOnesalesRep", new Boolean(moreThanOnesalesRep));

    // check the number of customer and the number of
    // sales Rep. to choose one of the 3 possibilities
    // 1.- n customer n sales rep (hide "To" and "Reply-to" inputs)
    // 2.- n customers 1 sales rep (hide "To" input)
    // 3.- Otherwise show both
    if (moreThanOneCustomer && moreThanOnesalesRep) {
      discard = new String[] { "to", "to_bottomMargin", "replyTo", "replyTo_bottomMargin" };
    } else if (moreThanOneCustomer) {
      discard = new String[] { "to", "to_bottomMargin", "multSalesRep", "multSalesRepCount" };
    } else {
      discard = new String[] { "multipleCustomer", "multipleCustomer_bottomMargin" };
    }

    // check the templates
    if (differentDocTypes.size() > 1) { // the templates selector shouldn't
      // appear
      if (discard == null) { // Its the only think to hide
        discard = new String[] { "discardSelect" };
      } else {
        final String[] discardAux = new String[discard.length + 1];
        for (int i = 0; i < discard.length; i++) {
          discardAux[i] = discard[i];
        }
        discardAux[discard.length] = "discardSelect";
        return discardAux;
      }
    }
    if (vector == null && vars.getMultiFile("inpFile") == null) {
      if (discard == null) {
        discard = new String[] { "view" };
      } else {
        final String[] discardAux = new String[discard.length + 1];
        for (int i = 0; i < discard.length; i++) {
          discardAux[i] = discard[i];
        }
        discardAux[discard.length] = "view";
        return discardAux;
      }
    }
    return discard;
  }

  private boolean onlyOneAttachedDocs(Map<String, Report> reports) {
    if (reports.size() == 1) {
      return true;
    } else {
      return false;
    }

  }

  void createPrintStatusPage(HttpServletResponse response, VariablesSecureApp vars,
      int nrOfEmailsSend) throws IOException, ServletException {
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/reporting/printing/PrintStatus").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("language", vars.getLanguage());
    xmlDocument.setParameter("nrOfEmailsSend", "" + nrOfEmailsSend);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * 
   * @param documentIds
   * @return returns a comma separated and quoted string of documents id's. useful to sql querys
   */
  private String getComaSeparatedString(String[] documentIds) {
    String result = new String("(");
    for (int index = 0; index < documentIds.length; index++) {
      final String documentId = documentIds[index];
      if (index + 1 == documentIds.length) {
        result = result + "'" + documentId + "')";
      } else {
        result = result + "'" + documentId + "',";
      }

    }
    return result;
  }

  /**
   * @author gmauleon
   * @param content
   * @return
   * @throws ServletException
   */
  private File prepareFile(AttachContent content, String documentId) throws ServletException {
    try {
      final String attachPath = new OBPropertiesProvider().getOpenbravoProperties().getProperty(
          "attach.path")
          + "/tmp";
      final File f = new File(attachPath, content.getFileName());
      final InputStream inputStream = content.getFileItem().getInputStream();
      final OutputStream out = new FileOutputStream(f);
      final byte buf[] = new byte[1024];
      int len;
      while ((len = inputStream.read(buf)) > 0)
        out.write(buf, 0, len);
      out.close();
      inputStream.close();
      return f;
    } catch (final Exception e) {
      throw new ServletException("Error trying to get the attached file", e);
    }

  }

  /**
   * Returns an array of document's ID ordered by Document No ASC
   * 
   * @param documentType
   * @param documentIds
   *          array of document's ID without order
   * @return List of ordered IDs
   * @throws ServletException
   */
  private String[] orderByDocumentNo(DocumentType documentType, String[] documentIds)
      throws ServletException {
    String strTable = documentType.getTableName();

    StringBuffer strIds = new StringBuffer();
    strIds.append("'");
    for (int i = 0; i < documentIds.length; i++) {
      if (i > 0) {
        strIds.append("', '");
      }
      strIds.append(documentIds[i]);
    }
    strIds.append("'");

    PrintControllerData[] printControllerData;
    String documentIdsOrdered[] = new String[documentIds.length];
    int i = 0;
    if (strTable.equals("C_INVOICE")) {
      printControllerData = PrintControllerData.selectInvoices(this, strIds.toString());
      for (PrintControllerData docID : printControllerData) {
        documentIdsOrdered[i++] = docID.getField("Id");
      }
    } else if (strTable.equals("C_ORDER")) {
      printControllerData = PrintControllerData.selectOrders(this, strIds.toString());
      for (PrintControllerData docID : printControllerData) {
        documentIdsOrdered[i++] = docID.getField("Id");
      }
    } else if (strTable.equals("FIN_PAYMENT")) {
      printControllerData = PrintControllerData.selectPayments(this, strIds.toString());
      for (PrintControllerData docID : printControllerData) {
        documentIdsOrdered[i++] = docID.getField("Id");
      }
    } else
      return documentIds;

    return documentIdsOrdered;
  }

  @Override
  public String getServletInfo() {
    return "Servlet that processes the print action";
  } // End of getServletInfo() method
}
