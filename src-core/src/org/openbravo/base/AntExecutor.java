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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.listener.Log4jListener;

/**
 * The AntExecutor class allows to execute ant tasks in a given build.xml file.
 * 
 * 
 */
public class AntExecutor {
  private static final Logger logger = Logger.getLogger(AntExecutor.class);

  private Project project;
  private String baseDir;
  private FileOutputStream logFile;
  private PrintStream ps;

  /**
   * Initializes a newly created AntExecutor object assigning it the build.xml file to execute tasks
   * from and the base directory where they will be executed.
   * 
   * @param buildFile
   *          - Complete path to the build.xml file
   * @param baseDir
   *          - Complete path to the base directory
   * @throws Exception
   *           - if an error occurs loading the xml file
   */
  public AntExecutor(String buildFile, String baseDir) throws Exception {
    project = new Project();
    this.baseDir = (baseDir == null || baseDir.equals("") ? "." : baseDir);
    try {
      project.init();
      project.setBasedir(this.baseDir);
      ProjectHelper.getProjectHelper().parse(project, new File(buildFile));
    } catch (final BuildException e) {
      throw new Exception("ErrorLoadingBuildXML", e);
    }
  }

  /**
   * Initializes a new AntExecutor object assigning it the build.xml file in the directory passed as
   * parameter, the base directory is the same as the one the build.xml is in.
   * 
   * @param buildDir
   *          - Directory where is the build.xml file and that will be the base directory
   * @throws Exception
   *           - if an error occurs loading the xml file
   */
  public AntExecutor(String buildDir) throws Exception {
    this(buildDir + "/build.xml", buildDir);
  }

  public void setLogFileAndListener(String filename) {
    File logFolder = new File(baseDir, "log");
    if (!logFolder.exists()) {
      logFolder.mkdir();
    }
    File file = new File(baseDir + "/log", filename + "-apply.log");
    final DefaultLogger logger1 = new DefaultLogger();
    try {
      logFile = new FileOutputStream(file);
      ps = new PrintStream(logFile);
      logger1.setOutputPrintStream(ps);
      logger1.setErrorPrintStream(ps);
      logger1.setMessageOutputLevel(Project.MSG_INFO);
      project.addBuildListener(logger1);

      Log4jListener listener = new Log4jListener();
      project.addBuildListener(listener);
    } catch (FileNotFoundException e) {
      logger.error("Error assigning rebuild log file.", e);
    }
  }

  /**
   * Set a value to a property to the project.
   * 
   * @param property
   *          - Property name
   * @param value
   *          - Value to assign
   */
  public void setProperty(String property, String value) {
    project.setProperty(property, value);
  }

  /**
   * Executes an ant task
   * 
   * @param task
   *          - Name of the task to execute
   * @throws Exception
   *           - In case the project is not loaded
   */
  public void runTask(String task) throws Exception {
    if (project == null)
      throw new Exception("NoProjectLoaded");
    if (task == null)
      task = project.getDefaultTarget();
    try {
      project.executeTarget(task);
    } catch (final BuildException e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * Executes a set of ant tasks
   * 
   * @param tasks
   *          - A Vector<String> with the names of the tasks to be executed
   * @throws Exception
   *           - In case the project is not loaded
   */
  public void runTask(Vector<String> tasks) throws Exception {
    if (project == null)
      throw new Exception("NoProjectLoaded");
    try {
      project.executeTargets(tasks);
    } catch (final BuildException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void closeLogFile() {
    try {
      if (logFile != null) {
        ps.flush();
        logFile.close();
      }
    } catch (IOException e) {
    }
  }
}
