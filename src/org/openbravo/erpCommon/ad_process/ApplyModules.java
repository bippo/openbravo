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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.log4j.PropertyConfigurator;
import org.openbravo.base.AntExecutor;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_process.buildStructure.Build;
import org.openbravo.erpCommon.ad_process.buildStructure.BuildMainStep;
import org.openbravo.erpCommon.ad_process.buildStructure.BuildStep;
import org.openbravo.erpCommon.ad_process.buildStructure.BuildTranslation;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.module.ModuleLog;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.service.system.RestartTomcat;
import org.openbravo.xmlEngine.XmlDocument;
import org.xml.sax.InputSource;

/**
 * Servlet for the Apply Modules method.
 * 
 */
public class ApplyModules extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(request, response, vars);
    } else if (vars.commandIn("STARTAPPLY")) {
      startApply(response, vars);
    } else if (vars.commandIn("RESETREBUILDSTATE")) {
      resetBuild(response, vars);
    } else if (vars.commandIn("TOMCAT")) {
      printPageTomcat(request, response, vars);
    } else if (vars.commandIn("RESTART")) {
      restartApplicationServer(response, vars);
    } else {
      pageError(response);
    }
  }

  /**
   * Prints a page that only allows the user to restart or reload Tomcat
   */
  private void printPageTomcat(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars) throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartTomcat").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Restarts the application server after building the application to apply modules.
   * 
   * @param response
   *          the HttpServletResponse to write to
   * @param vars
   *          the application variables
   * @throws IOException
   * @throws ServletException
   */
  private void restartApplicationServer(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartingContext").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final String message = Utility.messageBD(this, "TOMCAT_RESTART", vars.getLanguage());
    xmlDocument.setParameter("message", Utility.formatMessageBDToHtml(message));

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    response.flushBuffer();

    RestartTomcat.restart();
  }

  /**
   * Prints the default page for the process, showing the process description and a OK and Cancel
   * buttons. First it checks whether the server has write permissions to be able to execute the
   * process.
   */
  private void printPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars) throws IOException, ServletException {
    // Check for permissions to apply modules from application server.
    final File f = new File(vars.getSessionValue("#sourcePath"));
    if (!f.canWrite()) {
      bdErrorGeneralPopUp(request, response, Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.messageBD(this, "NoApplicableModules", vars.getLanguage()));
      return;
    }

    Build build = getBuildFromXMLFile();
    if (build == null) {
      throw new ServletException(
          "Build information couldn't be read (possibly because the file couldn't be found)");
    }
    String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/ApplyModules").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("buttonLog", fileName);
    xmlDocument.setParameter("logfile", fileName);
    {
      final OBError myMessage = vars.getMessage("ApplyModules");
      vars.removeMessage("ApplyModules");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    FieldProvider[] nodeData = getFieldProviderFromBuild(vars, build);
    xmlDocument.setData("structureStepTree", nodeData);

    /*
     * There is an initial Javascript part which is generated here, and injected into the html page
     * Basically, this Javascript part consists in a number of Javascript global variables whose
     * value depends on the Build Structure which is read from the buildStructure.xml file
     */
    // This variable contains all possible state codes
    String arraySteps = " var possible_states=[";
    // This variable contains the error status (error, warning, success, nothing yet) for every
    // possible state
    String errorStatus = "var error_status=[";
    String numofWarns = "var numofwarns=[";
    String numofErrors = "var numoferrs=[";
    // This variable contains information about the structure of the build (mainsteps and substeps
    // codes, in a kind of hierarchical structure, defined as multidimensional arrays)
    String nodeStructure = "var nodestructure=[";
    // This variable contains all possible states in which the process could end
    String endStates = "var end_states=[";
    int i = 0;
    int k = 0;
    int l = 0;
    for (BuildMainStep mstep : build.getMainSteps()) {
      if (mstep.getErrorCode() != null) {
        if (l > 0)
          endStates += ",";
        endStates += mstep.getErrorCode().replace("RB", "");
        l++;
      }
      if (mstep.getStepList().size() > 0) {
        if (k > 0)
          nodeStructure += ",";
        nodeStructure += "[" + mstep.getCode().replace("RB", "") + ",[";
        k++;
      } else {
        if (i > 0) {
          arraySteps += ",";
          errorStatus += ",";
          numofWarns += ",";
          numofErrors += ",";
        }
        arraySteps += mstep.getCode().replace("RB", "");
        errorStatus += "''";
        numofWarns += "0";
        numofErrors += "0";
      }
      i++;
      int j = 0;
      for (BuildStep step : mstep.getStepList()) {
        if (j > 0)
          nodeStructure += ",";
        j++;
        arraySteps += "," + step.getCode().replace("RB", "");
        nodeStructure += step.getCode().replace("RB", "");
        errorStatus += ",''";
        numofWarns += ",0";
        numofErrors += ",0";
      }
      if (mstep.getStepList().size() > 0) {
        nodeStructure += "]]";
      }
    }
    if (build.getMainSteps().get(build.getMainSteps().size() - 1).getWarningCode() != null) {
      if (l > 0)
        endStates += ",";
      endStates += build.getMainSteps().get(build.getMainSteps().size() - 1).getWarningCode()
          .replace("RB", "");
    }
    if (build.getMainSteps().get(build.getMainSteps().size() - 1).getSuccessCode() != null) {
      if (l > 0)
        endStates += ",";
      endStates += build.getMainSteps().get(build.getMainSteps().size() - 1).getSuccessCode()
          .replace("RB", "");
    }
    // We also add the successful final state of the last main step
    arraySteps += ","
        + build.getMainSteps().get(build.getMainSteps().size() - 1).getSuccessCode()
            .replace("RB", "");
    errorStatus += ",''";
    numofWarns += ",0";
    numofErrors += ",0";
    arraySteps += "];";
    errorStatus += "];";
    numofWarns += "];";
    numofErrors += "];";
    nodeStructure += "];";
    endStates += "];";

    String firstState = "var first_state='"
        + build.getMainSteps().get(0).getCode().replace("RB", "") + "';";
    String generatedJS = firstState + "\n" + endStates + "\n" + arraySteps + "\n" + errorStatus
        + "\n" + numofWarns + "\n" + numofErrors + "\n" + nodeStructure + "\n" + "\n";
    xmlDocument.setParameter("jsparam", generatedJS);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  private FieldProvider[] getFieldProviderFromBuild(VariablesSecureApp vars, Build build) {
    try {
      if (vars.getLanguage().equals("en_US")) {
        FieldProvider[] nodeData = build.getFieldProvidersForBuild();
        return nodeData;
      } else {
        BuildTranslation buildTranslation = getBuildTranslationFromFile(vars.getLanguage());
        if (buildTranslation == null) {
          FieldProvider[] nodeData = build.getFieldProvidersForBuild();
          return nodeData;
        }
        buildTranslation.setBuild(build);
        FieldProvider[] nodeData = buildTranslation.getFieldProvidersForBuild();
        return nodeData;
      }
    } catch (Exception e) {
      log4j.error("Error reading build information file", e);
    }
    return null;
  }

  protected static BuildTranslation getBuildTranslationFromFile(String language) throws Exception {

    String source = OBPropertiesProvider.getInstance().getOpenbravoProperties().get("source.path")
        .toString();
    File modulesF = new File(source, "modules");
    File[] modules = modulesF.listFiles();
    File translationFile = null;
    for (int i = 0; i < modules.length && translationFile == null; i++) {
      File provisionalFile = new File(modules[i], "referencedata/translation/" + language
          + "/buildStructureTrl.xml");
      if (provisionalFile.exists())
        translationFile = provisionalFile;
    }
    if (translationFile == null)
      return null;
    FileInputStream fis = new FileInputStream(translationFile);
    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

    BufferedReader xmlReader = new BufferedReader(isr);

    BeanReader beanReader = new BeanReader();

    beanReader.getBindingConfiguration().setMapIDs(false);

    beanReader.getXMLIntrospector().register(
        new InputSource(new FileReader(new File(source,
            "/src/org/openbravo/erpCommon/ad_process/buildStructure/mapping.xml"))));

    beanReader.registerBeanClass("BuildTranslation", BuildTranslation.class);

    BuildTranslation build = (BuildTranslation) beanReader.parse(xmlReader);

    return build;
  }

  protected Build getBuildFromXMLFile() {
    try {
      String source = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .get("source.path").toString();
      return Build.getBuildFromXMLFile(source
          + "/src/org/openbravo/erpCommon/ad_process/buildStructure/buildStructure.xml", new File(
          source, "/src/org/openbravo/erpCommon/ad_process/buildStructure/mapping.xml")
          .getAbsolutePath());

    } catch (Exception e) {
      log4j.error("Error while reading the build information file");
      return null;
    }
  }

  private void resetBuild(HttpServletResponse response, VariablesSecureApp vars) {
    Build build = getBuildFromXMLFile();
    vars.setSessionValue("ApplyModules|Last_Line_Number_Log", "-1");
    vars.setSessionValue("ApplyModules|ProcessFinished", "N");
    PreparedStatement ps = null;
    PreparedStatement ps2 = null;
    PreparedStatement updateSession = null;
    User currentUser = OBContext.getOBContext().getUser();
    try {
      try {
        if (OBScheduler.getInstance().getScheduler().isStarted())
          OBScheduler.getInstance().getScheduler().shutdown(true);
      } catch (Exception e) {
        log4j.warn("Could not shutdown scheduler", e);
        // We will not log an exception if the scheduler complains. The user shouldn't notice this
      }
      OBDal.getInstance().flush();
      OBDal.getInstance().getConnection().commit();

      ps = getPreparedStatement("DELETE FROM AD_ERROR_LOG");
      ps.executeUpdate();
      ps2 = getPreparedStatement("UPDATE AD_SYSTEM_INFO SET SYSTEM_STATUS='"
          + build.getMainSteps().get(0).getCode() + "'");
      ps2.executeUpdate();
      // We also cancel sessions opened for users different from the current one
      updateSession = getPreparedStatement("UPDATE AD_SESSION SET SESSION_ACTIVE='N' WHERE CREATEDBY<>?");
      updateSession.setString(1, currentUser.getId());
      updateSession.executeUpdate();

    } catch (final Exception e) {
      createModuleLog(false, e.getMessage());
    } finally {
      try {
        releasePreparedStatement(ps);
        releasePreparedStatement(ps2);
        releasePreparedStatement(updateSession);
      } catch (SQLException e) {
        // Ignored on purpose
      }
    }
  }

  /**
   * Method to be called via AJAX. Creates a new AntExecutor object, saves it in session and
   * executes the apply modules task on it.
   */
  private void startApply(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (vars.getSessionValue("ApplyModules|BuildRunning").equals("Y")) {
      return;
    }

    OBContext.setAdminMode();
    PreparedStatement ps3 = null;
    PreparedStatement ps4 = null;
    AntExecutor ant = null;
    try {
      // We first shutdown the background process, so that it doesn't interfere
      // with the rebuild process

      Properties props = new Properties();
      props.setProperty("log4j.appender.DB", "org.openbravo.utils.OBRebuildAppender");
      props.setProperty("log4j.appender.DB.Basedir", vars.getSessionValue("#sourcePath"));
      props.setProperty("log4j.rootCategory", "INFO,DB");
      PropertyConfigurator.configure(props);

      String sourcePath = vars.getSessionValue("#sourcePath");
      ant = new AntExecutor(sourcePath);

      response.setContentType("text/html; charset=UTF-8");
      final PrintWriter out = response.getWriter();

      // do not execute translation process (all entries should be already in the module)
      ant.setProperty("tr", "no");
      // We will show special, friendly warnings when they are available
      ant.setProperty("friendlyWarnings", "true");
      ant.setProperty("logFileName", vars.getStringParameter("logfile"));

      final Vector<String> tasks = new Vector<String>();
      tasks.add("UIrebuild");

      vars.setSessionValue("ApplyModules|BuildRunning", "Y");
      ant.runTask(tasks);

      vars.setSessionValue("ApplyModules|ProcessFinished", "Y");
      out.close();
    } catch (final Exception e) {
      // rolback the old transaction and start a new one
      // to store the build log
      OBDal.getInstance().rollbackAndClose();
      createModuleLog(false, e.getMessage());
      OBDal.getInstance().commitAndClose();
    } finally {
      vars.setSessionValue("ApplyModules|BuildRunning", "");
      try {
        Properties props = new Properties();
        props.setProperty("log4j.rootCategory", "INFO,R");
        PropertyConfigurator.configure(props);
        if (ant != null)
          ant.closeLogFile();
        releasePreparedStatement(ps3);
        releasePreparedStatement(ps4);
      } catch (SQLException e) {
      }
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Creates a new module log entry for a build with the action set to B and no module information
   * set.
   * 
   * @param success
   *          if true then the build was successfull, false if not successfull
   * @param msg
   *          optional additional message
   */
  private static void createModuleLog(boolean success, String msg) {
    final ModuleLog ml = OBProvider.getInstance().get(ModuleLog.class);
    ml.setAction("B");
    if (success) {
      ml.setLog("Build successfull");
    } else {
      final int prefixLength = "Build failed, message: ".length();
      final int maxMsgLength = 2000 - prefixLength;
      if (msg == null) {
        ml.setLog("Build failed");
      } else if (msg.length() > maxMsgLength) {
        ml.setLog("Build failed, message: " + msg.substring(0, maxMsgLength));
      } else {
        ml.setLog("Build failed, message: " + msg);
      }
    }
    OBDal.getInstance().save(ml);
  }

}
