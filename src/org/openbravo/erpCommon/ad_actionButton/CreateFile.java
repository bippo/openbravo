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
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class CreateFile extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strKey = vars.getStringParameter("inpcRemittanceId");
      String strMessage = "";
      printPage(response, vars, strKey, strWindow, strProcessId, strMessage, true);
    } else if (vars.commandIn("GENERATE")) {
      String strKey = vars.getStringParameter("inpcRemittanceId");
      getPrintPage(request, response, vars, strKey);
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strProcessId, String strMessage, boolean isDefault)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button create file msg:" + strMessage);

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CreateFile", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setData(
        "reportTyperemittance",
        "liststructure",
        TyperemittanceComboData.select(this,
            Utility.getContext(this, vars, "#User_Client", "CreateFile"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "CreateFile")));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    if (isDefault) {
      xmlDocument.setParameter("messageType", "");
      xmlDocument.setParameter("messageTitle", "");
      xmlDocument.setParameter("messageMessage", "");
    } else {
      OBError myMessage = new OBError();
      myMessage.setTitle("");
      if (log4j.isDebugEnabled())
        log4j.debug("CreateFile - before setMessage");
      if (strMessage == null || strMessage.equals(""))
        myMessage.setType("Success");
      else
        myMessage.setType("Error");
      if (strMessage != null && !strMessage.equals("")) {
        myMessage.setMessage(strMessage);
      } else
        Utility.translateError(this, vars, vars.getLanguage(), "Success");
      if (log4j.isDebugEnabled())
        log4j.debug("CreateFile - Message Type: " + myMessage.getType());
      vars.setMessage("CreateFile", myMessage);
      if (log4j.isDebugEnabled())
        log4j.debug("CreateFile - after setMessage");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void getPrintPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strKey) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("generate " + strKey);
    String strCuaderno = CreateFileData.selectParam(this, strKey, "CUADERNO");
    String strContract = CreateFileData.selectParam(this, strKey, "CONTRACT");
    if (strCuaderno == null)
      strCuaderno = "";
    if (strContract == null || strContract.equals(""))
      advisePopUp(request, response, "Error", Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.messageBD(this, "RemittanceTypeContractError", vars.getLanguage()));
    if (strCuaderno.equals("58"))
      printPageFind58(response, vars, strKey, strContract);
    else if (strCuaderno.equals("19"))
      printPageFind19(response, vars, strKey, strContract);
    else if (strCuaderno.equals("34"))
      printPageFind34(response, vars, strKey);
    else
      advisePopUp(request, response, "Error", Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.messageBD(this, "RemittanceTypeError", vars.getLanguage()));
  }

  private void printPageFind58(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strContract) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pageFind");
    StringBuffer strBuf = new StringBuffer();
    String strMessage = "";
    CreateFileData[] Principio = CreateFileData.select(this, strKey);
    CreateFileData[] Lineas = CreateFileData.selectLineas(this, strKey);
    CreateFileData[] Total = CreateFileData.selectTotal(this, strKey);
    int comprobacion1 = new Integer(CreateFileData.selectComprobacion1(this, strKey)).intValue();
    int comprobacion2 = new Integer(CreateFileData.selectComprobacion2(this, strKey)).intValue();
    int comprobacion3 = new Integer(CreateFileData.selectComprobacion3(this, strKey)).intValue();
    int comprobacion4 = new Integer(CreateFileData.selectComprobacion4(this, strKey)).intValue();
    CreateFileData[] comprobacion5 = CreateFileData.selectComprobacion5(this, strKey);
    if (log4j.isDebugEnabled())
      log4j.debug(" c1:" + comprobacion1 + " c2:" + comprobacion2 + " c3:" + comprobacion3 + " c4:"
          + comprobacion4 + " c5:" + comprobacion5.length);

    for (int i = 0; i < comprobacion5.length; i++) {
      strMessage = strMessage + comprobacion5[i].bpname + " "
          + Utility.messageBD(this, "CodeBankBPErrorMultiple", vars.getLanguage()) + "<br />";
    }
    if (!strMessage.equals("")) {
      printPage(response, vars, strKey, "", "", strMessage, false);
    }

    if (comprobacion1 != 0 || comprobacion2 == 0 || comprobacion3 != 1 || comprobacion4 == 0) {
      strMessage = Utility.messageBD(this, "CreateFileError", vars.getLanguage());
      printPage(response, vars, strKey, "", "", strMessage, false);
    }
    int contador = 2;
    // dubugging headers
    if (Principio == null || Principio.length == 0) {
      strMessage = Utility.messageBD(this, "DefaultAccountError", vars.getLanguage());
      printPage(response, vars, strKey, "", "", strMessage, false);
      return;
    }
    if (Lineas == null || Total == null)
      return;
    if (Principio[0].nif == null || Principio[0].nif.equals("")) {
      strMessage = Utility.messageBD(this, "NIFError", vars.getLanguage());
    }
    if (Principio[0].codebank == null || Principio[0].codebank.equals("")) {
      strMessage = Utility.messageBD(this, "CodeBankError", vars.getLanguage());
    }
    if (Principio[0].codebranch == null || Principio[0].codebranch.equals("")) {
      strMessage = Utility.messageBD(this, "CodeBranchError", vars.getLanguage());
    }
    if (Principio[0].digitcontrol1 == null || Principio[0].digitcontrol1.equals("")) {
      strMessage = Utility.messageBD(this, "DC1Error", vars.getLanguage());
    }
    if (Principio[0].digitcontrol2 == null || Principio[0].digitcontrol2.equals("")) {
      strMessage = Utility.messageBD(this, "DC2Error", vars.getLanguage());
    }
    if (Principio[0].ine == null || Principio[0].ine.equals("")) {
      strMessage = Utility.messageBD(this, "INEError", vars.getLanguage());
    }
    // presentation header
    strBuf = strBuf.append("5170").append(Principio[0].nif).append(strContract)
        .append(Principio[0].dateplanned);
    strBuf = strBuf.append(Principio[0].entidad);
    strBuf = strBuf.append(Principio[0].entofi).append("\r\n");
    // ordering header
    strBuf = strBuf.append("5370").append(Principio[0].nif).append(strContract)
        .append(Principio[0].dateplanned);
    strBuf = strBuf.append(Principio[0].entidad).append(Principio[0].nCuenta).append("        06");
    strBuf = strBuf.append(Principio[0].ine).append("   \r\n");
    // lines
    for (int i = 0; i < Lineas.length; i++) {
      // debugging lines
      if (Lineas[i].creditcardnumber == null || Lineas[i].creditcardnumber.equals("")) {
        strMessage = Utility.messageBD(this, "CodeBankBPError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].fechaVencimiento == null || Lineas[i].fechaVencimiento.equals("")) {
        strMessage = Utility.messageBD(this, "DatePlannedError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].direccion == null || Lineas[i].direccion.equals("")) {
        strMessage = Utility.messageBD(this, "AddressError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].plaza == null || Lineas[i].plaza.equals("")) {
        strMessage = Utility.messageBD(this, "SquareError", vars.getLanguage()) + Lineas[i].tercero;
      }
      if (Lineas[i].postal == null || Lineas[i].postal.equals("")) {
        strMessage = Utility.messageBD(this, "PostCodeError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].localidad == null || Lineas[i].localidad.equals("")) {
        strMessage = Utility.messageBD(this, "TownError", vars.getLanguage()) + Lineas[i].tercero;
      }
      if (Lineas[i].fechaFactura == null || Lineas[i].fechaFactura.equals("")) {
        strMessage = Utility.messageBD(this, "InvoiceDateError", vars.getLanguage())
            + Lineas[i].tercero;
        ;
      }
      strBuf = strBuf.append("5670").append(Principio[0].nif).append(strContract)
          .append(Lineas[i].nFactura).append(Lineas[i].tercero);
      strBuf = strBuf.append(Lineas[i].creditcardnumber).append(Lineas[i].payamt)
          .append("                ");
      strBuf = strBuf.append(Replace.replace(Lineas[i].concepto, "\n", ""))
          .append(Lineas[i].fechaVencimiento).append("  \r\n");
      contador++;
      strBuf = strBuf.append("5676").append(Principio[0].nif).append(strContract)
          .append(Lineas[i].nFactura).append(Lineas[i].direccion);
      strBuf = strBuf.append(Lineas[i].plaza).append(Lineas[i].postal).append(Lineas[i].localidad);
      strBuf = strBuf.append(Lineas[i].codigoProvincia).append(Lineas[i].fechaFactura)
          .append("\r\n");
      contador++;
    }
    CreateFileData[] NLineas = CreateFileData.selectNLineas(this, String.valueOf(contador));
    // total orderer
    strBuf = strBuf.append("5870").append(Principio[0].nif).append(strContract)
        .append(NLineas[0].hueco);
    strBuf = strBuf.append(Total[0].payamt).append(Total[0].nFactura).append(NLineas[0].lineas)
        .append("\r\n");
    NLineas = CreateFileData.selectNLineas(this, String.valueOf(contador + 2));
    // total
    strBuf = strBuf.append("5970").append(Principio[0].nif).append(strContract);
    strBuf = strBuf.append(NLineas[0].ordenantes).append(Total[0].payamt).append(Total[0].nFactura)
        .append(NLineas[0].lineas);
    if (!strMessage.equals("")) {
      printPage(response, vars, strKey, "", "", strMessage, false);
    } else {
      response.setContentType("application/rtf");
      response.setHeader("Content-Disposition", "attachment; filename=BANK.DAT");
      PrintWriter out = response.getWriter();
      out.println(strBuf.toString());
      out.close();
    }
  }

  private void printPageFind19(HttpServletResponse response, VariablesSecureApp vars,
      String strKey, String strContract) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pageFind");
    StringBuffer strBuf = new StringBuffer();
    String strMessage = "";
    CreateFileData[] Principio = CreateFileData.select(this, strKey);
    CreateFileData[] Lineas = CreateFileData.selectLineas(this, strKey);
    CreateFileData[] Total = CreateFileData.selectTotal(this, strKey);

    int comprobacion1 = new Integer(CreateFileData.selectComprobacion1(this, strKey)).intValue();
    int comprobacion2 = new Integer(CreateFileData.selectComprobacion2(this, strKey)).intValue();
    int comprobacion3 = new Integer(CreateFileData.selectComprobacion3(this, strKey)).intValue();
    int comprobacion4 = new Integer(CreateFileData.selectComprobacion4(this, strKey)).intValue();
    CreateFileData[] comprobacion5 = CreateFileData.selectComprobacion5(this, strKey);
    if (log4j.isDebugEnabled())
      log4j.debug(" c1:" + comprobacion1 + " c2:" + comprobacion2 + " c3:" + comprobacion3 + " c4:"
          + comprobacion4 + " c5:" + comprobacion5.length);

    for (int i = 0; i < comprobacion5.length; i++) {
      strMessage = strMessage + comprobacion5[i].bpname + " "
          + Utility.messageBD(this, "CodeBankBPErrorMultiple", vars.getLanguage()) + "<br />";
    }
    if (!strMessage.equals("")) {
      printPage(response, vars, strKey, "", "", strMessage, false);
    }

    if (comprobacion1 != 0 || comprobacion2 == 0 || comprobacion3 != 1 || comprobacion4 == 0) {
      if (log4j.isDebugEnabled())
        log4j.debug("Error: c1:" + comprobacion1 + " c2:" + comprobacion2 + " c3:" + comprobacion3
            + " c4:" + comprobacion4);
      strMessage = Utility.messageBD(this, "CreateFileError", vars.getLanguage());
      printPage(response, vars, strKey, "", "", strMessage, false);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("check1 ok");
    int contador = 2;
    // debugging headers
    if (Principio == null || Principio.length == 0) {
      strMessage = Utility.messageBD(this, "DefaultAccountError", vars.getLanguage());
      printPage(response, vars, strKey, "", "", strMessage, false);
      return;
    }
    if (Lineas == null || Total == null)
      return;
    if (Principio[0].nif == null || Principio[0].nif.equals("")) {
      strMessage = Utility.messageBD(this, "NIFError", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("NIF");
    }
    if (Principio[0].codebank == null || Principio[0].codebank.equals("")) {
      strMessage = Utility.messageBD(this, "CodeBankError", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("codebank");
    }
    if (Principio[0].codebranch == null || Principio[0].codebranch.equals("")) {
      strMessage = Utility.messageBD(this, "CodeBranchError", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("CodeBranchError");
    }
    if (Principio[0].digitcontrol1 == null || Principio[0].digitcontrol1.equals("")) {
      strMessage = Utility.messageBD(this, "DC1Error", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("DC1Error");
    }
    if (Principio[0].digitcontrol2 == null || Principio[0].digitcontrol2.equals("")) {
      strMessage = Utility.messageBD(this, "DC2Error", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("DC2Error");
    }
    if (log4j.isDebugEnabled())
      log4j.debug("check2 ok");
    // presentation header
    strBuf = strBuf.append("5180").append(Principio[0].nif).append(strContract)
        .append(Principio[0].hoy).append(Principio[0].dateplanned);
    strBuf = strBuf.append(Principio[0].entidad19);
    strBuf = strBuf.append(Principio[0].entofi).append("\r\n");
    // ordering header
    strBuf = strBuf.append("5380").append(Principio[0].nif).append(strContract)
        .append(Principio[0].hoy).append(Principio[0].dateplanned);
    strBuf = strBuf.append(Principio[0].entidad19).append(Principio[0].nCuenta)
        .append("        01                                                             ");
    strBuf = strBuf.append("   \r\n");
    // Lines
    for (int i = 0; i < Lineas.length; i++) {
      // lines debugging
      if (Lineas[i].creditcardnumber == null || Lineas[i].creditcardnumber.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("CodeBankBPError");
        strMessage = Utility.messageBD(this, "CodeBankBPError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].fechaVencimiento == null || Lineas[i].fechaVencimiento.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("DatePlannedError");
        strMessage = Utility.messageBD(this, "DatePlannedError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].direccion == null || Lineas[i].direccion.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("AddressError");
        strMessage = Utility.messageBD(this, "AddressError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].plaza == null || Lineas[i].plaza.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("SquareError");
        strMessage = Utility.messageBD(this, "SquareError", vars.getLanguage()) + Lineas[i].tercero;
      }
      if (Lineas[i].postal == null || Lineas[i].postal.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("PostCodeError");
        strMessage = Utility.messageBD(this, "PostCodeError", vars.getLanguage())
            + Lineas[i].tercero;
      }
      if (Lineas[i].localidad == null || Lineas[i].localidad.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("TownError");
        strMessage = Utility.messageBD(this, "TownError", vars.getLanguage()) + Lineas[i].tercero;
      }
      if (Lineas[i].fechaFactura == null || Lineas[i].fechaFactura.equals("")) {
        if (log4j.isDebugEnabled())
          log4j.debug("InvoiceDateError");
        strMessage = Utility.messageBD(this, "InvoiceDateError", vars.getLanguage())
            + Lineas[i].tercero;
        ;
      }
      strBuf = strBuf.append("5680").append(Principio[0].nif).append(strContract)
          .append(Lineas[i].nFactura19).append(Lineas[i].tercero);
      strBuf = strBuf.append(Lineas[i].creditcardnumber).append(Lineas[i].payamt)
          .append("0000000000000000");
      strBuf = strBuf.append(Replace.replace(Lineas[i].concepto, "\n", ""))
          .append(Lineas[i].fechaVencimiento).append("  \r\n");
      contador++;
      /*
       * strBuf = strBuf.append("5676").append(Principio[0].nif).append("000"
       * ).append(Lineas[i].nFactura).append(Lineas[i].direccion); strBuf =
       * strBuf.append(Lineas[i].plaza).append(Lineas[i].postal).append(Lineas [i].localidad);
       * strBuf = strBuf.append(Lineas[i].codigoProvincia).
       * append(Lineas[i].fechaFactura).append("\r\n"); contador++;
       */
    }
    if (log4j.isDebugEnabled())
      log4j.debug("check3 ok");
    CreateFileData[] NLineas = CreateFileData.selectNLineas(this, String.valueOf(contador));
    // total orderer
    strBuf = strBuf.append("5880").append(Principio[0].nif).append(strContract)
        .append(NLineas[0].hueco);
    strBuf = strBuf.append(Total[0].payamt).append(Total[0].nFactura).append(NLineas[0].lineas)
        .append("\r\n");
    NLineas = CreateFileData.selectNLineas(this, String.valueOf(contador + 2));
    // total
    strBuf = strBuf.append("5980").append(Principio[0].nif).append(strContract);
    strBuf = strBuf.append(NLineas[0].ordenantes).append(Total[0].payamt).append(Total[0].nFactura)
        .append(NLineas[0].lineas);
    if (!strMessage.equals("")) {
      printPage(response, vars, strKey, "", "", strMessage, false);
    } else {
      response.setContentType("application/rtf");
      response.setHeader("Content-Disposition", "attachment; filename=BANK.DAT");
      PrintWriter out = response.getWriter();
      out.println(strBuf.toString());
      out.close();
    }
  }

  private void printPageFind34(HttpServletResponse response, VariablesSecureApp vars, String strKey)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pageFind34");
    StringBuffer strBuf = new StringBuffer();
    String strMessage = "";

    String strConcepto = CreateFileData.selectParam(this, strKey, "CONCEPTO");
    if (log4j.isDebugEnabled())
      log4j.debug("Item" + strConcepto);
    if (strConcepto == null || strConcepto.equals(""))
      strConcepto = "N";
    if (log4j.isDebugEnabled())
      log4j.debug("Item" + strConcepto);
    char cConcepto = strConcepto.charAt(0);

    String strCodigo;

    switch (cConcepto) {
    case 'C': // check
      strCodigo = "57";
      break;
    case 'R': // promissory note
      strCodigo = "58";
      break;
    case 'P': // certified payments
      strCodigo = "59";
      break;
    default: // Salaries y bank transfers
      strCodigo = "56";
    }
    if (log4j.isDebugEnabled())
      log4j.debug("code" + strCodigo);
    /*
     * 1->Orderer (by default) 2->Beneficiary
     */
    String strGastos = CreateFileData.selectParam(this, strKey, "GASTOS");
    if (strGastos == null || !strGastos.equals("2"))
      strGastos = "1";
    if (log4j.isDebugEnabled())
      log4j.debug("strGastos" + strGastos);

    /*
     * 0->Only one journal entry 1->One entry per beneficiary
     */
    String strDetalle = CreateFileData.selectParam(this, strKey, "DETALLE");
    if (strDetalle == null || !strDetalle.equals("1"))
      strDetalle = "0";
    if (log4j.isDebugEnabled())
      log4j.debug("code" + strDetalle);

    CreateFile34Data[] Principio = CreateFile34Data.select(this, strKey);
    CreateFile34Data[] Lineas = CreateFile34Data.selectLineas(this, strKey);
    CreateFile34Data[] Total = CreateFile34Data.selectTotal(this, strKey);
    int comprobacion1 = new Integer(CreateFileData.selectComprobacion1(this, strKey)).intValue();
    int comprobacion2 = new Integer(CreateFileData.selectComprobacion2(this, strKey)).intValue();
    int comprobacion3 = new Integer(CreateFileData.selectComprobacion3(this, strKey)).intValue();
    int comprobacion4 = new Integer(CreateFileData.selectComprobacion4(this, strKey)).intValue();
    CreateFileData[] comprobacion5 = CreateFileData.selectComprobacion5(this, strKey);
    if (log4j.isDebugEnabled())
      log4j.debug(" c1:" + comprobacion1 + " c2:" + comprobacion2 + " c3:" + comprobacion3 + " c4:"
          + comprobacion4 + " c5:" + comprobacion5.length);

    for (int i = 0; i < comprobacion5.length; i++) {
      strMessage = strMessage + comprobacion5[i].bpname + " "
          + Utility.messageBD(this, "CodeBankBPErrorMultiple", vars.getLanguage()) + "<br />";
    }
    if (!strMessage.equals("")) {
      printPage(response, vars, strKey, "", "", strMessage, false);
    }

    if (comprobacion1 != 0 || comprobacion2 == 0 || comprobacion3 != 1 || comprobacion4 == 0) {
      if (log4j.isDebugEnabled())
        log4j.debug("Error: c1:" + comprobacion1 + " c2:" + comprobacion2 + " c3:" + comprobacion3
            + " c4:" + comprobacion4);
      strMessage = Utility.messageBD(this, "CreateFileError", vars.getLanguage());
      printPage(response, vars, strKey, "", "", strMessage, false);
    }

    if (log4j.isDebugEnabled())
      log4j.debug("Principio[0].taxid = " + Principio[0].taxid);
    if (log4j.isDebugEnabled())
      log4j.debug("Principio[0].acct = " + Principio[0].acct);
    if (log4j.isDebugEnabled())
      log4j.debug("check1 ok");

    // debugging headers
    if (Principio == null || Principio.length == 0) {
      strMessage = Utility.messageBD(this, "DefaultAccountError", vars.getLanguage());
      printPage(response, vars, strKey, "", "", strMessage, false);
      return;
    }
    if (Lineas == null || Total == null)
      return;
    if (Principio[0].taxid == null || Principio[0].taxid.length() != 9) {
      strMessage = Utility.messageBD(this, "NIFError", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("VAT number");
      printPage(response, vars, strKey, "", "", strMessage, false);
      return;
    }
    if (Principio[0].acct == null || Principio[0].acct.length() != 18) {
      strMessage = Utility.messageBD(this, "BankAccountError", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("bankaccount");
      printPage(response, vars, strKey, "", "", strMessage, false);
      return;
    }

    /*
     * if (comprobacion341!=0) { strMessage = Utility.messageBD(this, "TodayHigherDueData",
     * vars.getLanguage()); if (log4j.isDebugEnabled()) log4j.debug("TodayHigherDueData");
     * printPage(response, vars, strKey, "", "", strMessage, false); return; }
     */

    if (log4j.isDebugEnabled())
      log4j.debug("check2 ok");
    // header: orderer
    // 001
    strBuf = strBuf.append("03").append(strCodigo).append(Principio[0].nif).append("            "); // A
    // -
    // D
    // (common)
    strBuf = strBuf.append("001").append(Principio[0].hoy).append(Principio[0].duedate); // E-F2
    strBuf = strBuf.append(Principio[0].nCuenta).append(strDetalle).append("   ")
        .append(Principio[0].dc).append(Principio[0].hueco).append("\r\n"); // F3-G

    // 002
    strBuf = strBuf.append("03").append(strCodigo).append(Principio[0].nif).append("            "); // A
    // -
    // D
    // (common)
    strBuf = strBuf.append("002").append(Principio[0].nombre).append(Principio[0].hueco)
        .append("\r\n"); // E-G

    // 003
    strBuf = strBuf.append("03").append(strCodigo).append(Principio[0].nif).append("            "); // A
    // -
    // D
    // (common)
    strBuf = strBuf.append("003").append(Principio[0].domicilio).append(Principio[0].hueco)
        .append("\r\n"); // E-G

    // 004
    strBuf = strBuf.append("03").append(strCodigo).append(Principio[0].nif).append("            "); // A
    // -
    // D
    // (common)
    strBuf = strBuf.append("004").append(Principio[0].plaza).append(Principio[0].hueco)
        .append("\r\n"); // E-G

    int contador = 4;
    // Lines
    for (int i = 0; i < Lineas.length; i++) {
      // debugging lines
      if (log4j.isDebugEnabled())
        log4j.debug("Lineas[i].taxid=" + Lineas[i].taxid);
      if (log4j.isDebugEnabled())
        log4j.debug("Lineas[i].acct=" + Lineas[i].acct);
      if (log4j.isDebugEnabled())
        log4j.debug("Lineas[i].nom=" + Lineas[i].nom);
      if (log4j.isDebugEnabled())
        log4j.debug("Lineas[i].dom=" + Lineas[i].dom);
      if (log4j.isDebugEnabled())
        log4j.debug("Lineas[i].pla=" + Lineas[i].pla);
      if (log4j.isDebugEnabled())
        log4j.debug("Lineas[i].prov=" + Lineas[i].prov);

      if (Lineas[i].nom == null || Lineas[i].nom.length() < 1) {
        if (log4j.isDebugEnabled())
          log4j.debug("NameError");
        strMessage = Utility.messageBD(this, "NameError", vars.getLanguage()) + Lineas[i].nif;
        printPage(response, vars, strKey, "", "", strMessage, false);
        return;
      }
      if (Lineas[i].taxid == null || Lineas[i].taxid.length() != 9) {
        if (log4j.isDebugEnabled())
          log4j.debug("NIFError");
        strMessage = Utility.messageBD(this, "NIFBPartnerError", vars.getLanguage())
            + Lineas[i].nombre;
        printPage(response, vars, strKey, "", "", strMessage, false);
        return;
      }
      if (Lineas[i].acct == null || Lineas[i].acct.length() != 20) {
        if (log4j.isDebugEnabled())
          log4j.debug("CodeBankBPError");
        strMessage = Utility.messageBD(this, "CodeBankBPError", vars.getLanguage())
            + Lineas[i].nombre;
        printPage(response, vars, strKey, "", "", strMessage, false);
        return;
      }
      if (Lineas[i].dom == null || Lineas[i].dom.length() < 1) {
        if (log4j.isDebugEnabled())
          log4j.debug("AddressError");
        strMessage = Utility.messageBD(this, "AddressError", vars.getLanguage()) + Lineas[i].nombre;
        printPage(response, vars, strKey, "", "", strMessage, false);
        return;
      }
      if (Lineas[i].pla == null || Lineas[i].pla.length() < 1) {
        if (log4j.isDebugEnabled())
          log4j.debug("SquareError");
        strMessage = Utility.messageBD(this, "SquareError", vars.getLanguage()) + Lineas[i].nombre;
        printPage(response, vars, strKey, "", "", strMessage, false);
        return;
      }
      if (Lineas[i].prov == null || Lineas[i].prov.length() < 1) {
        if (log4j.isDebugEnabled())
          log4j.debug("AddressError");
        strMessage = Utility.messageBD(this, "AddressError", vars.getLanguage()) + Lineas[i].nombre;
        printPage(response, vars, strKey, "", "", strMessage, false);
        return;
      }
      // 010
      strBuf = strBuf.append("06").append(strCodigo).append(Principio[0].nif).append(Lineas[i].nif); // A
      // -
      // D
      // (common)
      strBuf = strBuf.append("010").append(Lineas[i].payamt).append(Lineas[i].nCuenta); // F1-F4
      strBuf = strBuf.append(strGastos);
      strBuf = strBuf.append(cConcepto == 'N' ? "1" : cConcepto == 'P' ? "8" : "9").append("  ");
      strBuf = strBuf.append(Lineas[i].dc).append(Principio[0].hueco).append("\r\n");

      // 011
      strBuf = strBuf.append("06").append(strCodigo).append(Principio[0].nif).append(Lineas[i].nif); // A
      // -
      // D
      // (common)
      strBuf = strBuf.append("011").append(Lineas[i].nombre).append(Principio[0].hueco)
          .append("\r\n");
      // 012
      strBuf = strBuf.append("06").append(strCodigo).append(Principio[0].nif).append(Lineas[i].nif); // A
      // -
      // D
      // (common)
      strBuf = strBuf.append("012").append(Lineas[i].domicilio).append(Principio[0].hueco)
          .append("\r\n");

      // 014
      strBuf = strBuf.append("06").append(strCodigo).append(Principio[0].nif).append(Lineas[i].nif); // A
      // -
      // D
      // (common)
      strBuf = strBuf.append("014").append(Lineas[i].plaza).append(Principio[0].hueco)
          .append("\r\n");

      // 015
      strBuf = strBuf.append("06").append(strCodigo).append(Principio[0].nif).append(Lineas[i].nif); // A
      // -
      // D
      // (common)
      strBuf = strBuf.append("015").append(Lineas[i].provincia).append(Principio[0].hueco)
          .append("\r\n");

      // 016
      strBuf = strBuf.append("06").append(strCodigo).append(Principio[0].nif).append(Lineas[i].nif); // A
      // -
      // D
      // (common)
      strBuf = strBuf.append("016")
          .append(Lineas[i].concepto.replaceAll("\r", " ").replaceAll("\n", " "))
          .append(Principio[0].hueco).append("\r\n");

      contador += 6;
    }
    // total
    CreateFile34Data[] NLineas = CreateFile34Data.selectNLineas(this, String.valueOf(contador + 1),
        ((Integer) Lineas.length).toString());
    strBuf = strBuf.append("08").append(strCodigo).append(Principio[0].nif).append(Total[0].payamt); // A
    // -
    // E
    strBuf = strBuf.append(NLineas[0].ordenantes).append(NLineas[0].lineas)
        .append(NLineas[0].hueco);

    if (!strMessage.equals("")) {
      printPage(response, vars, strKey, "", "", strMessage, false);
    } else {
      response.setContentType("application/rtf");
      response.setHeader("Content-Disposition", "attachment; filename=BANK.DAT");
      PrintWriter out = response.getWriter();
      out.println(strBuf.toString());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet for the generation of files for banks";
  } // end of getServletInfo() method
}
