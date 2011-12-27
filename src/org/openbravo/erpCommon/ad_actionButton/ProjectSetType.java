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
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class ProjectSetType extends HttpSecureAppServlet {
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
      String strTab = vars.getStringParameter("inpTabId");
      String strProjectType = vars.getStringParameter("inpcProjecttypeId", "");
      String strKey = vars.getGlobalVariable("inpcProjectId", strWindow + "|C_Project_ID");
      if (!ProjectSetTypeData.hasProjectType(this, strKey))
        printPage(response, vars, strKey, strProjectType, strWindow, strTab, strProcessId);
      else
        bdError(request, response, "ProjectSetTypeError", vars.getLanguage());
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strProjectType = vars.getStringParameter("inpcProjecttypeId");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strKey = vars.getRequestGlobalVariable("inpcProjectId", strWindow + "|C_Project_ID");
      String strTab = vars.getStringParameter("inpTabId");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myMessage = processButton(vars, strKey, strProjectType, strDateFrom, strWindow);
      vars.setMessage(strTab, myMessage);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strKey, String strProjectType,
      String strDateFrom, String windowId) {
    Connection conn = null;
    OBError myMessage = new OBError();
    if (strProjectType == null || strProjectType.equals("")) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      log4j.warn("Rollback in transaction");
      myMessage.setType("Error");
      myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      myMessage.setMessage(Utility.messageBD(this, "NoProjectTypeSelected", vars.getLanguage()));
    } else {
      try {
        conn = this.getTransactionConnection();
        ProjectSetTypeData[] data = ProjectSetTypeData.select(this, strProjectType);
        ProjectSetTypeData[] dataProject = ProjectSetTypeData.selectProject(this, strKey);
        String strProjectPhase = "";
        String strProjectTask = "";
        // Variables used for Project Scheduling purposes
        DateFormat DateFormatter = Utility.getDateFormatter(vars);
        int firstProjectPhase = 0;
        String strPhaseStartDate = "";
        String strPhaseContractDate = "";
        String strTaskStartDate = "";
        String strTaskContractDate = "";
        String strLastContractDate = "";

        for (int i = 0; data != null && i < data.length; i++) {
          strProjectPhase = SequenceIdData.getUUID();

          // Calculates the Starting Date of the Phase
          if (firstProjectPhase == 0) {
            strPhaseStartDate = strDateFrom;
          } else {
            strPhaseStartDate = calculateStartDate(strLastContractDate, DateFormatter);
          }
          // Calculates the Contract Date of the Phase
          strPhaseContractDate = calculateContractDate(strPhaseStartDate, data[i].stdduration,
              DateFormatter);

          try {
            if (ProjectSetTypeData.insertProjectPhase(conn, this, strKey,
                dataProject[0].adClientId, dataProject[0].adOrgId, vars.getUser(),
                data[i].description, data[i].mProductId, data[i].cPhaseId, strProjectPhase,
                data[i].help, data[i].name, data[i].standardqty, strPhaseStartDate,
                strPhaseContractDate, data[i].seqno) == 1) {
              strLastContractDate = strPhaseContractDate;
              ProjectSetTypeData[] data1 = ProjectSetTypeData.selectTask(this, data[i].cPhaseId);
              int firstProjectTask = 0;
              for (int j = 0; data1 != null && j < data1.length; j++) {
                strProjectTask = SequenceIdData.getUUID();

                // Calculates the Starting Date of the Task
                if (firstProjectTask == 0) {
                  strTaskStartDate = strPhaseStartDate;
                } else {
                  strTaskStartDate = calculateStartDate(strLastContractDate, DateFormatter);
                }
                // Calculates the Contract Date of the Task
                strTaskContractDate = calculateContractDate(strTaskStartDate, data1[j].stdduration,
                    DateFormatter);

                try {
                  ProjectSetTypeData.insertProjectTask(conn, this, strProjectTask,
                      data1[j].cTaskId, dataProject[0].adClientId, dataProject[0].adOrgId,
                      vars.getUser(), data1[j].seqno, data1[j].name, data1[j].description,
                      data1[j].help, data1[j].mProductId, strProjectPhase, data1[j].standardqty,
                      strTaskStartDate, strTaskContractDate);
                } catch (ServletException ex) {
                  myMessage = Utility.translateError(this, vars, vars.getLanguage(),
                      ex.getMessage());
                  releaseRollbackConnection(conn);
                  return myMessage;
                }

                strLastContractDate = strTaskContractDate;
                firstProjectTask++;
              }
              firstProjectPhase++;
            }
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }

        // Updates project's Type and category
        String strProjectCategory = ProjectSetTypeData.selectProjectCategory(this, strProjectType);
        try {
          ProjectSetTypeData.update(conn, this, vars.getUser(), strProjectType, strProjectCategory,
              strKey);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }

        // Updates project's Starting and Contract Dates
        ProjectSetTypeData[] dataDates = ProjectSetTypeData.selectDates(this, strKey);
        String strStartDate = strDateFrom.equals("") ? dataDates[0].startdate : strDateFrom;
        String strContractDate = strLastContractDate.equals("") ? dataDates[0].datecontract
            : strLastContractDate;
        try {
          ProjectSetTypeData.updateDates(conn, this, vars.getUser(), strStartDate, strContractDate,
              strKey);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }

        releaseCommitConnection(conn);
        myMessage.setType("Success");
        myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      } catch (Exception e) {
        try {
          releaseRollbackConnection(conn);
        } catch (Exception ignored) {
        }
        e.printStackTrace();
        log4j.warn("Rollback in transaction");
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      }
    }
    return myMessage;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String strProjectType, String windowId, String strTab, String strProcessId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Project set Type");

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
        "org/openbravo/erpCommon/ad_actionButton/ProjectSetType", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "C_ProjectType_ID", "", "Project type service", Utility.getContext(this, vars,
              "#AccessibleOrgTree", ""), Utility.getContext(this, vars, "#User_Client", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "", strProjectType);
      xmlDocument.setData("reportcProjecttypeId", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    ProjectSetTypeData[] dataDates = ProjectSetTypeData.selectDates(this, strKey);
    String strDateFrom = dataDates[0].startdate.equals("") ? DateTimeData.today(this)
        : dataDates[0].startdate;
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("cProjecttypeId", strProjectType);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  // Determines the Starting Date of a Phase or a Task
  private String calculateStartDate(String strLastContractDate, DateFormat DateFormatter)
      throws ParseException {
    String strStartDate = "";
    if (strLastContractDate != null && !strLastContractDate.equals("")) {
      strStartDate = strLastContractDate; // Start Date equals to Last
      // Contract Date
      do {
        strStartDate = Utility.addDaysToDate(strStartDate, "1", DateFormatter); // Start Date equals
        // to Last Contract
        // Date plus one day
      } while (Utility.isWeekendDay(strStartDate, DateFormatter)); // If
      // strStartDate
      // is a
      // weekend
      // day,
      // looks
      // for
      // the
      // next
      // labor
      // day
    }
    return strStartDate;
  }

  // Determines the Contract Date of a Phase or a Task, based on the Standard
  // Duration in Days of the Standard Phase or the Standard Task
  private String calculateContractDate(String strStartDate, String strStdDuration,
      DateFormat DateFormatter) throws ParseException {
    String strContractDate = "";
    if (strStartDate != null && !strStartDate.equals("") && strStdDuration != null
        && !strStdDuration.equals("")) {
      strContractDate = strStartDate; // Contract Date equals to Starting
      // Date
      Integer StdDuration = Integer.parseInt(strStdDuration) - 1;
      int DaysLeft = StdDuration;
      while (DaysLeft > 0) {
        strContractDate = Utility.addDaysToDate(strContractDate, "1", DateFormatter); // Contract
        // Date equals
        // to Starting
        // Date
        // plus one day until the standard
        // duration in days is reached
        if (!Utility.isWeekendDay(strContractDate, DateFormatter))
          DaysLeft--; // If strContractDate is a weekend day, looks
        // for the next labor day
      }
    }
    return strContractDate;
  }

  public String getServletInfo() {
    return "Servlet Project set Type";
  } // end of getServletInfo() method
}
