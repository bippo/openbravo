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

package org.openbravo.check;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class Check extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    final String command = req.getParameter("Command") == null ? "" : req.getParameter("Command");

    final PrintWriter out = resp.getWriter();

    if (command.equals("memory")) {
      final Runtime runtime = Runtime.getRuntime();
      final long maxMemory = runtime.maxMemory() / (1024 * 1024); // Memory
      // in MB

      out.println(maxMemory);
    } else if (command.equals("jvm-version")) {
      out.println(System.getProperty("java.runtime.version"));
    } else if (command.equals("server")) {
      out.println(req.getSession().getServletContext().getServerInfo());
    } else if (command.equals("ant")) {
      final String result = checkAnt(req.getParameter("file"), req.getParameter("task"));
      out.println(result);
    } else {
      out.println("Non-recognized command: " + command);
    }
    out.close();
  }

  private String checkAnt(String file, String task) {
    final Project project = new Project();
    System.out.println("task: " + task);
    try {
      project.init();
      ProjectHelper.getProjectHelper().parse(project, new File(file));
      project.executeTarget(task);
    } catch (final Exception e) {
      return e.getMessage();
    }
    return "OK";
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    doPost(req, resp);
  }
}
