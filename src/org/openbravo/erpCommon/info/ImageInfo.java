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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ImageInfo extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strKey = vars.getStringParameter("inpNameValue");
      String strNameValue = ImageInfoData.selectName(this, strKey);
      if (!strNameValue.equals(""))
        vars.setSessionValue("ImageInfo.name", strNameValue + "%");
      printPageFrame(response, vars, strNameValue, "");
    } else if (vars.commandIn("FIND")) {
      String strName = vars.getRequestGlobalVariable("inpName", "ImageInfo.name");
      String strURL = vars.getStringParameter("inpURL");

      vars.setSessionValue("ImageInfo.initRecordNumber", "0");

      printPageFrame(response, vars, strName, strURL);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("ImageInfo.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ImageInfo");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("ImageInfo.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("ImageInfo.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("ImageInfo.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ImageInfo");
      int intRecordRange = strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
      if (initRecord == 0)
        initRecord = 1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
      vars.setSessionValue("ImageInfo.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageError(response);
  }

  private void printPageFrame(HttpServletResponse response, VariablesSecureApp vars,
      String strName, String strURL) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Frame 2 of the image seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ImageInfo");
    int intRecordRange = (strRecordRange.equals("") ? 0 : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ImageInfo.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));

    if (strName.equals("") && strURL.equals("")) {
      String[] discard = { "sectionDetail", "hasPrevious", "hasNext" };
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo", discard)
          .createXmlDocument();
      xmlDocument.setData("structure1", ImageInfoData.set());
    } else {
      String[] discard = { "withoutPrevious", "withoutNext" };
      ImageInfoData[] data = ImageInfoData.select(this,
          Utility.getContext(this, vars, "#User_Client", "ImageInfo"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "ImageInfo"), strName, strURL,
          initRecordNumber, intRecordRange);
      if (data == null || data.length == 0 || initRecordNumber <= 1)
        discard[0] = new String("hasPrevious");
      if (data == null || data.length == 0 || data.length < intRecordRange)
        discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo", discard)
          .createXmlDocument();
      xmlDocument.setData("structure1", data);
    }
    if (strName.equals("")) {
      xmlDocument.setParameter("name", "%");
    } else {
      xmlDocument.setParameter("name", strName);
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que image seeker";
  } // end of getServletInfo() method
}
