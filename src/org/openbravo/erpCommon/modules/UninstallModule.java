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
package org.openbravo.erpCommon.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.CPStandAlone;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;

/**
 * UninstallModule class is able to uninstall a list of modules, uninstalling a module consists on
 * deleting all its contents directory and setting it in ad_module table as uninstalled and
 * inactive, afterwards a rebuild process is required, this process will delete the uninstalled
 * modules from database.
 * 
 * 
 */
public class UninstallModule {
  private static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(UninstallModule.class);
  private String modulesBaseDir;

  private StringBuffer log = new StringBuffer();
  private int logLevel = 0;
  private VariablesSecureApp vars;

  public static final int MSG_SUCCESS = 0;
  public static final int MSG_WARN = 1;
  public static final int MSG_ERROR = 2;

  /**
   * Creates a new {@link UninstallModule} instance
   * 
   * @param conn
   * @param obDir
   * @param _vars
   */
  public UninstallModule(ConnectionProvider conn, String obDir, VariablesSecureApp _vars) {
    pool = conn;
    modulesBaseDir = obDir + "/modules";
    vars = _vars;
  }

  /**
   * Creates a new {@link UninstallModule} instance
   * 
   * @param xmlPoolFile
   * @param obDir
   * @param _vars
   */
  public UninstallModule(String xmlPoolFile, String obDir, VariablesSecureApp _vars) {
    pool = new CPStandAlone(xmlPoolFile);
    modulesBaseDir = obDir + "/modules";
    vars = _vars;
  }

  /**
   * Executes the uninstall process for a comma separated list of modules, all these modules will be
   * uninstalled as well as all their contained modules.
   * 
   * @param moduleIdList
   *          Comma separated list of module ids
   */
  public void execute(String moduleIdList) {
    if (moduleIdList == null || moduleIdList.equals("")) {
      log4j.error("No module selected to uninstall");
      addLog("@NoModuleSelectedToUninstall@", MSG_ERROR);
      return;
    }
    try {
      moduleIdList = moduleIdList.replace("(", "").replace(")", "");
      if (moduleIdList.contains("'0'")) {
        log4j.error("Cannot uninstall core");
        addLog("@CannotUninstallCore@", MSG_ERROR);
        return;
      }
      final UninstallModuleData[] dependencies = UninstallModuleData.selectDependencies(pool,
          moduleIdList);
      if (dependencies != null && dependencies.length > 0) {
        for (int i = 0; i < dependencies.length; i++) {
          log4j.error(dependencies[i].origname
              + " cannot uninstall module because it is part of a dependency for "
              + dependencies[i].origname);
          addLog(dependencies[i].origname + " @IsDependencyOf@ " + dependencies[i].name
              + ". @CannotUninstallDependency@", MSG_ERROR);
          try {
            ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
                "cannot uninstall module because it is part of a dependency "
                    + dependencies[i].name, "E");
          } catch (final ServletException ex) {
            ex.printStackTrace();
          }
        }
        return;
      }

      // loop all the modules to be uninstalled
      final StringTokenizer st = new StringTokenizer(moduleIdList, ",", false);
      while (st.hasMoreTokens()) {
        final String module = st.nextToken().trim();

        final String status = UninstallModuleData.selectStatus(pool, module);
        if (status != null && "U".equals(status)) {
          continue;
        }

        // obtain all the modules to uninstall: selected modules + inclussions of them
        String modsToUninstall = getContentList(module.replace("'", ""));

        ArrayList<String> uninstallList = new ArrayList<String>(Arrays.asList(modsToUninstall
            .replace(" ", "").replace("'", "").split(",")));
        for (UninstallModuleData dep : UninstallModuleData
            .selectDependencies(pool, modsToUninstall)) {
          // Do not uninstall inclussions that are dependencies for other modules
          uninstallList.remove(dep.adDependentModuleId);
          addLog(dep.origname + " @IsDependencyOf@ " + dep.name + ". @ModuleWillNotBeUninstalled@",
              MSG_WARN);
        }

        modsToUninstall = "";
        for (String mod : uninstallList) {
          modsToUninstall += "'" + mod + "',";
        }
        if (modsToUninstall.isEmpty()) {
          return;
        }
        modsToUninstall = modsToUninstall.substring(0, modsToUninstall.length() - 1);

        final UninstallModuleData data[] = UninstallModuleData.selectDirectories(pool,
            modsToUninstall); // delete
        // directories
        if (data != null && data.length > 0) {
          for (int i = 0; i < data.length; i++) {
            UninstallModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()),
                data[i].adModuleId, data[i].version, data[i].name, "Uninstalled module "
                    + data[i].name + " - " + data[i].version, "D");
            final File f = new File(modulesBaseDir, data[i].javapackage);
            if (f.exists()) {
              if (!Utility.deleteDir(f)) {
                addLog("@CannotRemoveModule@ " + data[i].name, MSG_ERROR);
                log4j.error("Cannot remove module contents " + data[i].name);
              }
              if (f.exists()) {
                addLog("@CannotRemoveModule@ " + data[i].name, MSG_ERROR);
                log4j.error("Cannot remove module contents " + data[i].name);
              } else {
                addLog("@RemovedModule@ " + data[i].name, MSG_SUCCESS);
                log4j.info("Removed contents for module " + data[i].name);
              }
            } else {
              addLog("@ModuleHasNotDirectory@ " + data[i].name, MSG_WARN);
              log4j.info(data[i].name + " has no contents directory");
            }
          }
        }

        if (status.equals("I")) {
          // the module has not been already installed, delete it from temporary installation tables
          UninstallModuleData.deleteTmpModule(pool, modsToUninstall);
          UninstallModuleData.deleteTmpDependency(pool, modsToUninstall);
          UninstallModuleData.deleteTmpDBPrefix(pool, modsToUninstall);
        } else {
          // set as uninstalled in DB
          UninstallModuleData.updateUninstall(pool, modsToUninstall);
        }
      }
    } catch (final Exception e) {
      addLog(e.toString(), MSG_ERROR);
      e.printStackTrace();
    }
  }

  /**
   * Returns a list of comma separated ids of the modules contained in the package or template. The
   * passed module/package/template is also returned as part of the contents.
   * 
   * 
   * @param module
   *          Pakage or template to check
   * @return a comma separated list of ids
   * @throws Exception
   *           in case the query failed
   */
  private String getContentList(String module) throws Exception {
    String rt = "'" + module + "'";
    final UninstallModuleData data[] = UninstallModuleData.selectContent(pool, module);
    if (data != null && data.length > 0) {
      for (int i = 0; i < data.length; i++) {
        rt += ", " + getContentList(data[i].adModuleId);
      }
    }
    return rt;
  }

  /**
   * Add a log with a level
   * 
   * @param m
   *          message
   * @param level
   *          log level
   */
  private void addLog(String m, int level) {
    log4j.info(m);
    if (level > logLevel) {
      logLevel = level;
      log = new StringBuffer(m);
    } else if (level == logLevel)
      log.append(m + "\n");
  }

  /**
   * Returns a OBError instance obtained from the current log
   * 
   * @return a new instance of OBError if the log has messages, null otherwise
   */
  public OBError getOBError() {
    if (log.length() != 0) {
      final String lang = vars.getLanguage();
      final OBError rt = new OBError();
      switch (logLevel) {
      case MSG_ERROR:
        rt.setType("Error");
        break;
      case MSG_WARN:
        rt.setType("Warning");
        break;
      default:
        rt.setType("Success");
        break;
      }
      rt.setMessage(Utility.parseTranslation(pool, vars, lang, log.toString()));

      rt.setTitle(Utility.messageBD(pool, rt.getType(), lang));
      return rt;
    } else
      return null;
  }
}
