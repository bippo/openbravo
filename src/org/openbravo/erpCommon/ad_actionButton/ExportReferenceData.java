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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.service.db.DataExportService;
import org.openbravo.xmlEngine.XmlDocument;

public class ExportReferenceData extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      vars.getGlobalVariable("inpProcessId", "ExportReferenceData|AD_Process_ID");
      vars.getGlobalVariable("inpwindowId", "ExportReferenceData|Window_ID");
      vars.getGlobalVariable("inpTabId", "ExportReferenceData|Tab_ID");
      String strDataSet = vars.getRequiredGlobalVariable("inpadDatasetId",
          "ExportReferenceData|AD_DataSet_ID");
      printPage(response, vars, strDataSet);
    } else if (vars.commandIn("SAVE")) {
      vars.getGlobalVariable("inpProcessId", "ExportReferenceData|AD_Process_ID");
      vars.getGlobalVariable("inpwindowId", "ExportReferenceData|Window_ID");
      String strTab = vars.getGlobalVariable("inpTabId", "ExportReferenceData|Tab_ID");
      String strKey = vars.getRequestGlobalVariable("inpKey", "ExportReferenceData|AD_DataSet_ID");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError = processButton(vars, strKey);
      if (log4j.isDebugEnabled())
        log4j.debug(myError.getMessage());
      vars.setMessage(strTab, myError);
      log4j.warn("********** strWindowPath - " + strWindowPath);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strKey) {
    OBError myError = null;
    try {
      DataSet myDataset = (DataSet) OBDal.getInstance().get(DataSet.class, strKey);
      if (!myDataset.getModule().isInDevelopment()) {
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        myError.setMessage(Utility.messageBD(this, "20532", vars.getLanguage()));
        return myError;
      }
      ExportReferenceDataData[] data = ExportReferenceDataData.selectDataset(this, strKey);
      if (data == null || data.length == 0)
        return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      ExportReferenceDataData[] module = ExportReferenceDataData.selectModule(this,
          data[0].adModuleId);

      final String xml = DataExportService.getInstance().exportDataSetToXML(myDataset,
          data[0].adModuleId, new java.util.HashMap<String, Object>());
      File myFolder = new File(vars.getSessionValue("#sourcePath")
          + (data[0].adModuleId.equals("0") ? "" : "/modules/" + module[0].javapackage)
          + "/referencedata/standard");
      File myFile = new File(vars.getSessionValue("#sourcePath")
          + (data[0].adModuleId.equals("0") ? "" : "/modules/" + module[0].javapackage)
          + "/referencedata/standard/" + Utility.wikifiedName(data[0].name) + ".xml");
      if (!myFolder.exists())
        myFolder.mkdirs();

      FileOutputStream myOutputStream = new FileOutputStream(myFile);
      myOutputStream.write(xml.getBytes("UTF-8"));
      myOutputStream.close();

      MessageDigest cs = MessageDigest.getInstance("MD5");
      cs.update(xml.getBytes("UTF-8"));
      myDataset.setChecksum(new BigInteger(1, cs.digest()).toString());
      OBDal.getInstance().save(myDataset);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      e.printStackTrace();
      myError = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myError;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDataSetID)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Copy from Settlement");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/ExportReferenceData").createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("key", strDataSetID);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  @Override
  public String getServletInfo() {
    return "Servlet Copy from settlement";
  } // end of getServletInfo() method
}
