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
 * All portions are Copyright (C) 2007-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.Sqlc;
import org.openbravo.reference.Reference;
import org.openbravo.reference.ui.UIReference;
import org.openbravo.utils.Replace;

public class PrintJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String strProcessId = vars.getRequiredStringParameter("inpadProcessId");
    String strOutputType = vars.getStringParameter("inpoutputtype", "html");
    if (!hasGeneralAccess(vars, "P", strProcessId)) {
      bdError(request, response, "AccessTableNoView", vars.getLanguage());
      return;
    }
    String strReportName = PrintJRData.getReportName(this, strProcessId);
    HashMap<String, Object> parameters = createParameters(vars, strProcessId);

    renderJR(vars, response, strReportName, strOutputType, parameters, null, null);
  }

  private HashMap<String, Object> createParameters(VariablesSecureApp vars, String strProcessId)
      throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("JR: Get Parameters");
    String strParamname;
    JasperReport jasperReport = null;

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    PrintJRData[] processparams = PrintJRData.getProcessParams(this, strProcessId);
    if (processparams != null && processparams.length > 0) {
      String strReportName = PrintJRData.getReportName(this, strProcessId);
      String strAttach = globalParameters.strFTPDirectory + "/284-" + classInfo.id;
      String strLanguage = vars.getLanguage();
      String strBaseDesign = getBaseDesignPath(strLanguage);

      strReportName = Replace.replace(
          Replace.replace(strReportName, "@basedesign@", strBaseDesign), "@attach@", strAttach);

      try {
        JasperDesign jasperDesign = JRXmlLoader.load(strReportName);
        jasperReport = JasperCompileManager.compileReport(jasperDesign);
      } catch (JRException e) {
        if (log4j.isDebugEnabled())
          log4j.debug("JR: Error: " + e);
        e.printStackTrace();
        throw new ServletException(e.getMessage(), e);
      } catch (Exception e) {
        throw new ServletException(e.getMessage(), e);
      }

    }
    for (int i = 0; i < processparams.length; i++) {
      strParamname = "inp" + Sqlc.TransformaNombreColumna(processparams[i].paramname);
      String paramValue = "";

      UIReference ref = Reference.getUIReference(processparams[i].reference, null);
      if (ref.isNumeric()) {
        paramValue = vars.getNumericParameter(strParamname);
      } else {
        paramValue = vars.getStringParameter(strParamname);
      }

      if (log4j.isDebugEnabled()) {
        log4j.debug("JR: -----parameter: " + strParamname + " " + paramValue);
      }

      if (!paramValue.equals("")) {
        parameters.put(
            processparams[i].paramname,
            formatParameter(vars, processparams[i].paramname, paramValue,
                processparams[i].reference, jasperReport));
      }
    }
    return parameters;
  }

  private Object formatParameter(VariablesSecureApp vars, String strParamName,
      String strParamValue, String reference, JasperReport jasperReport) throws ServletException {
    String strObjectClass = "";
    Object object;
    JRParameter[] jrparams = jasperReport.getParameters();
    for (int i = 0; i < jrparams.length; i++) {
      if (jrparams[i].getName().equals(strParamName))
        strObjectClass = jrparams[i].getValueClassName();
    }
    if (log4j.isDebugEnabled())
      log4j.debug("ClassType: " + strObjectClass);
    if (strObjectClass.equals("java.lang.String")) {
      object = new String(strParamValue);
    } else if (strObjectClass.equals("java.util.Date")) {
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
      try {
        object = dateFormat.parse(strParamValue);
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      }
    } else {
      object = new String(strParamValue);
    }
    return object;
  }

  public String getServletInfo() {
    return "Servlet that generates the output of a JasperReports report.";
  } // end of getServletInfo() method
}
