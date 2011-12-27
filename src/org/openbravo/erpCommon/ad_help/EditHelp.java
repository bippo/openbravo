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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_help;

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
import org.openbravo.xmlEngine.XmlDocument;

public class EditHelp extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("WINDOW", "TAB", "PROCESS", "FORM")) {
      vars.getRequiredGlobalVariable("inpClave", "Help.clave");
      printPageEdit_FS(response, vars, vars.getCommand());
    } else if (vars.commandIn("FIELD")) {
      vars.getRequiredGlobalVariable("inpClave", "Help.clave");
      printPageEdit_FS(response, vars, vars.getCommand());
    } else if (vars.commandIn("ELEMENT")) {

      vars.getRequiredGlobalVariable("inpClave", "Help.clave");
      printPageEdit_FS(response, vars, vars.getCommand());
    } else if (vars.commandIn("EDIT_Window", "EDIT_Tab", "EDIT_Process", "EDIT_Form")) {
      String strClave = vars.getSessionValue("Help.clave");
      vars.removeSessionValue("Help.clave");
      printPageEdit_F1(response, vars, strClave, vars.getCommand());
    } else if (vars.commandIn("EDIT_Field")) {
      String strClave = vars.getSessionValue("Help.clave");
      vars.removeSessionValue("Help.clave");
      printPageEditField_F1(response, vars, strClave, vars.getCommand());
    } else if (vars.commandIn("EDIT_Element")) {
      String strClave = vars.getSessionValue("Help.clave");
      if (log4j.isDebugEnabled())
        log4j.debug("VALUE OF THE KEY: " + strClave);
      vars.removeSessionValue("Help.clave");
      printPageEditElement_F1(response, vars, strClave, vars.getCommand());
    } else if (vars.commandIn("SAVE_Window")) {
      String strClave = vars.getRequiredStringParameter("inpClave");
      String strName = vars.getStringParameter("inpName");
      String strHelp = vars.getStringParameter("inpHelp");
      OBError myError = null;
      int total = 0;
      try {
        if (vars.getLanguage().equals("en_US")) {
          total = DisplayHelpEditData.update(this, strName, strHelp, strClave);
        } else {
          total = DisplayHelpEditData.updateTrl(this, strName, strHelp, vars.getUser(), strClave,
              vars.getLanguage());
          if (total == 0)
            total = DisplayHelpEditData.insertTrl(this, strClave, vars.getLanguage(),
                vars.getUser(), strName, strHelp);
        }
      } catch (ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage("Help", myError);
      }
      if (myError == null && total == 0) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
        vars.setMessage("Help", myError);
      }
      printPageEdit_F2(response, vars);
    } else if (vars.commandIn("SAVE_Tab")) {
      String strClave = vars.getRequiredStringParameter("inpClave");
      String strName = vars.getStringParameter("inpName");
      String strHelp = vars.getStringParameter("inpHelp");
      OBError myError = null;
      int total = 0;
      try {
        if (vars.getLanguage().equals("en_US")) {
          total = DisplayHelpEditData.updateTab(this, strName, strHelp, strClave);
        } else {
          total = DisplayHelpEditData.updateTabTrl(this, strName, strHelp, vars.getUser(),
              strClave, vars.getLanguage());
          if (total == 0)
            total = DisplayHelpEditData.insertTabTrl(this, strClave, vars.getLanguage(),
                vars.getUser(), strName, strHelp);
        }
      } catch (ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage("Help", myError);
      }
      if (myError == null && total == 0) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
        vars.setMessage("Help", myError);
      }
      printPageEdit_F2(response, vars);
    } else if (vars.commandIn("SAVE_Form")) {
      String strClave = vars.getRequiredStringParameter("inpClave");
      String strName = vars.getStringParameter("inpName");
      String strHelp = vars.getStringParameter("inpHelp");
      OBError myError = null;
      int total = 0;
      try {
        if (vars.getLanguage().equals("en_US")) {
          total = DisplayHelpEditData.updateForm(this, strName, strHelp, strClave);
        } else {
          total = DisplayHelpEditData.updateFormTrl(this, strName, strHelp, vars.getUser(),
              strClave, vars.getLanguage());
          if (total == 0)
            total = DisplayHelpEditData.insertFormTrl(this, strClave, vars.getLanguage(),
                vars.getUser(), strName, strHelp);
        }
      } catch (ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage("Help", myError);
      }
      if (myError == null && total == 0) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
        vars.setMessage("Help", myError);
      }
      printPageEdit_F2(response, vars);
    } else if (vars.commandIn("SAVE_Process")) {
      String strClave = vars.getRequiredStringParameter("inpClave");
      String strName = vars.getStringParameter("inpName");
      String strHelp = vars.getStringParameter("inpHelp");
      OBError myError = null;
      int total = 0;
      try {
        if (vars.getLanguage().equals("en_US")) {
          total = DisplayHelpEditData.updateProcess(this, strName, strHelp, strClave);
        } else {
          total = DisplayHelpEditData.updateProcessTrl(this, strName, strHelp, vars.getUser(),
              strClave, vars.getLanguage());
          if (total == 0)
            total = DisplayHelpEditData.insertProcessTrl(this, strClave, vars.getLanguage(),
                vars.getUser(), strName, strHelp);
        }
      } catch (ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage("Help", myError);
      }
      if (myError == null && total == 0) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
        vars.setMessage("Help", myError);
      }
      printPageEdit_F2(response, vars);
    } else if (vars.commandIn("SAVE_Field")) {
      if (log4j.isDebugEnabled())
        log4j.debug("WE GET IN TO SAVE THE 'FIELD' FIELD\n");
      String strClave = vars.getRequiredStringParameter("inpClave");
      String strName = vars.getStringParameter("inpName");
      String strHelp = vars.getStringParameter("inpHelp");
      String strCentrally = vars.getStringParameter("inpiscentrallymaintained");
      if (log4j.isDebugEnabled())
        log4j.debug("THE FIELDS IT RETURNS ARE: " + strClave + strName + strHelp + strCentrally);
      if (strCentrally.equals("Y")) {
      } else {
        strCentrally = "N";
      }
      OBError myError = null;
      int total = 0;
      try {
        if (vars.getLanguage().equals("en_US")) {
          total = DisplayHelpEditData.updateField(this, strName, strHelp, strCentrally, strClave);
        } else {
          if (log4j.isDebugEnabled())
            log4j.debug("WE MAKE AN UPDATE");
          total = DisplayHelpEditData.updateFieldTrl(this, strName, strHelp, vars.getUser(),
              strClave, vars.getLanguage());
          if (log4j.isDebugEnabled())
            log4j.debug("NUMBER OF MODIFIED COLUMNS: " + total);
          if (total == 0)
            total = DisplayHelpEditData.insertFieldTrl(this, strClave, vars.getLanguage(),
                vars.getUser(), strName, strHelp);
          DisplayHelpEditData.updateFieldTrlIscentrally(this, strCentrally, vars.getUser(),
              strClave);
        }
      } catch (ServletException ex) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else
          vars.setMessage("Help", myError);
      }
      if (myError == null && total == 0) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
        vars.setMessage("Help", myError);
      }
      printPageEdit_F2(response, vars);
    } else if (vars.commandIn("SAVE_Element")) {
      if (log4j.isDebugEnabled())
        log4j.debug("WE SAVE THE ELEMENT\n");
      String strClave = vars.getRequiredStringParameter("inpElement");
      String strName = vars.getStringParameter("inpName");
      String strHelp = vars.getStringParameter("inpHelp");
      String strField = vars.getStringParameter("inpClave");
      String strType = vars.getStringParameter("inpType");
      if (log4j.isDebugEnabled())
        log4j.debug("VALUE OF THE FIELD: " + strType);
      if (vars.getLanguage().equals("en_US")) {
        DisplayHelpEditData.updateElement(this, strName, strHelp, strClave);
      } else {
        if (log4j.isDebugEnabled())
          log4j.debug("WE MAKE THE UPDATE");
        int total = DisplayHelpEditData.updateElementTrl(this, strName, strHelp, vars.getUser(),
            strClave, vars.getLanguage());
        if (total == 0)
          DisplayHelpEditData.insertElementTrl(this, strClave, vars.getLanguage(), vars.getUser(),
              strName, strHelp);
      }
      DisplayHelpEditData[] dataisCentrally = DisplayHelpEditData.selectIsCentrally(this, strField);
      String strisCentrally = dataisCentrally[0].iscentrallymaintained;
      if (strisCentrally.equals("Y")) {
        if (log4j.isDebugEnabled())
          log4j.debug("WE REDRAW THE 'FIELD' FIELD\n");
        printPageEditField_F2(response, vars);
      } else {
        printPageEditElement_F2(response, vars);
      }
    } else {
      pageError(response);
    }
  }

  private void printPageEdit_FS(HttpServletResponse response, VariablesSecureApp vars,
      String strTipo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_FS").createXmlDocument();

    xmlDocument.setParameter("command",
        ((strTipo.indexOf("WINDOW") != -1) ? "Window" : ((strTipo.indexOf("TAB") != -1) ? "Tab"
            : ((strTipo.indexOf("FIELD") != -1) ? "Field"
                : ((strTipo.indexOf("PROCESS") != -1) ? "Process"
                    : ((strTipo.indexOf("FORM") != -1) ? "Form" : "Element"))))));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageEdit_F1(HttpServletResponse response, VariablesSecureApp vars,
      String strClave, String strTipo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    String[] discard = { "" };
    DisplayHelpEditData[] data;
    if (strTipo.equalsIgnoreCase("EDIT_Window")) {
      data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData.select(this, strClave)
          : DisplayHelpEditData.selectTrl(this, vars.getLanguage(), strClave));
      strTipo = "Window";
    } else if (strTipo.equalsIgnoreCase("EDIT_Tab")) {
      data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData.selectTab(this, strClave)
          : DisplayHelpEditData.selectTabTrl(this, vars.getLanguage(), strClave));
      strTipo = "Tab";
    } else if (strTipo.equalsIgnoreCase("EDIT_Form")) {
      data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData.selectForm(this, strClave)
          : DisplayHelpEditData.selectFormTrl(this, vars.getLanguage(), strClave));
      strTipo = "Form";
    } else if (strTipo.equalsIgnoreCase("EDIT_Process")) {
      data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData
          .selectProcess(this, strClave) : DisplayHelpEditData.selectProcessTrl(this,
          vars.getLanguage(), strClave));
      strTipo = "Process";
    } else {
      data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData.selectField(this, strClave)
          : DisplayHelpEditData.selectFieldTrl(this, vars.getLanguage(), strClave));
      strTipo = "Field";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_F1", discard).createXmlDocument();

    OBError myMessage = vars.getMessage("Help");
    vars.removeMessage("Help");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("save", strTipo);
    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageEdit_F2(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    OBError myMessage = vars.getMessage("Help");
    vars.removeMessage("Help");
    StringBuffer message = new StringBuffer();
    String[] discard = { "discardError" };
    if (myMessage != null) {
      message.append("function cerrarHelpEditor(){\n");
      message.append("  parent.frameSuperior.setValues_MessageBox(\"messageBoxID\", \"")
          .append(myMessage.getType()).append("\", \"");
      message.append(myMessage.getTitle()).append("\", \"").append(myMessage.getMessage())
          .append("\");\n");
      message.append("  return true;\n");
      message.append("}");
      discard[0] = "discardClose";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_F2", discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("message", message.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageEditField_F1(HttpServletResponse response, VariablesSecureApp vars,
      String strClave, String strTipo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_Field_F1").createXmlDocument();

    OBError myMessage = vars.getMessage("Help");
    vars.removeMessage("Help");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    DisplayHelpEditData[] data;
    data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData.selectField(this, strClave)
        : DisplayHelpEditData.selectFieldTrl(this, vars.getLanguage(), strClave));
    strTipo = "Field";
    String strEdit = "Element";
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("field", strClave);
    xmlDocument.setParameter("save", strTipo);
    xmlDocument.setParameter("edit", strEdit);
    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageEditField_F2(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    OBError myMessage = vars.getMessage("Help");
    vars.removeMessage("Help");
    StringBuffer message = new StringBuffer();
    String[] discard = { "discardError" };
    if (myMessage != null) {
      message.append("function cerrarHelpEditor(){\n");
      message.append("  parent.frameSuperior.setValues_MessageBox(\"messageBoxID\", \"")
          .append(myMessage.getType()).append("\", \"");
      message.append(myMessage.getTitle()).append("\", \"").append(myMessage.getMessage())
          .append("\");\n");
      message.append("  return true;\n");
      message.append("}");
      discard[0] = "discardClose";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_Field_F2", discard).createXmlDocument();
    if (log4j.isDebugEnabled())
      log4j.debug("WE REDRAW THE FIELD OF THE FUNCTION");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("message", message.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageEditElement_F1(HttpServletResponse response, VariablesSecureApp vars,
      String strClave, String strTipo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_Element_F1").createXmlDocument();

    OBError myMessage = vars.getMessage("Help");
    vars.removeMessage("Help");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    DisplayHelpEditData[] data;
    data = (vars.getLanguage().equals("en_US") ? DisplayHelpEditData.selectElement(this, strClave)
        : DisplayHelpEditData.selectElementTrl(this, vars.getLanguage(), strClave));
    strTipo = "Element";
    String strEdit = "Field";
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("save", strTipo);
    xmlDocument.setParameter("field", strClave);
    xmlDocument.setParameter("edit", strEdit);
    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();

  }

  private void printPageEditElement_F2(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Help Editor");
    OBError myMessage = vars.getMessage("Help");
    vars.removeMessage("Help");
    StringBuffer message = new StringBuffer();
    String[] discard = { "discardError" };
    if (myMessage != null) {
      message.append("function cerrarHelpEditor(){\n");
      message.append("  parent.frameSuperior.setValues_MessageBox(\"messageBoxID\", \"")
          .append(myMessage.getType()).append("\", \"");
      message.append(myMessage.getTitle()).append("\", \"").append(myMessage.getMessage())
          .append("\");\n");
      message.append("  return true;\n");
      message.append("}");
      discard[0] = "discardClose";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_help/DisplayHelp_Edit_Element_F2", discard).createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("message", message.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
