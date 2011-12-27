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
package org.openbravo.erpCommon.utility;

import java.io.File;

import org.apache.axis.client.AdminClient;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Deploys or undeploys the SOAP webservices defined in core and in the rest of modules
 * 
 */
public class WebServiceAdmin extends Task {
  private String axisServlet;
  private File baseDir;
  private String action;

  @Override
  public void execute() throws BuildException {
    String wsddFileName;
    if (action.equalsIgnoreCase("deploy")) {
      wsddFileName = "deploy.wsdd";
    } else if (action.equalsIgnoreCase("undeploy")) {
      wsddFileName = "undeploy.wsdd";
    } else {
      throw new BuildException("Not valid action: " + action + ". It must be deploy/undeploy.");
    }

    // (un)deploy module webservices
    File modulesDir = new File(baseDir + "/modules");
    for (File module : modulesDir.listFiles()) {
      File wssdFile = new File(module.getAbsoluteFile() + "/src/" + wsddFileName);
      if (wssdFile.exists()) {
        deployFile(wssdFile);
      }
    }

  }

  private void deployFile(File wssdFile) {
    AdminClient ac = new AdminClient();
    String[] args = { "-l" + axisServlet, wssdFile.getAbsolutePath() };
    try {
      ac.process(args);
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  public String getAxisServlet() {
    return axisServlet;
  }

  public void setAxisServlet(String axisServlet) {
    this.axisServlet = axisServlet;
  }

  public File getBaseDir() {
    return baseDir;
  }

  public void setBaseDir(File baseDir) {
    this.baseDir = baseDir;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
