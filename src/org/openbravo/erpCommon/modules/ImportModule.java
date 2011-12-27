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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.modules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.AxisFault;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToArraySink;
import org.apache.ddlutils.io.DatabaseDataIO;
import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.ddlutils.task.DatabaseUtils;
import org.openbravo.erpCommon.ad_forms.MaturityLevel;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.Zip;
import org.openbravo.model.ad.module.ModuleInstall;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.service.web.ResourceNotFoundException;
import org.openbravo.services.webservice.Module;
import org.openbravo.services.webservice.ModuleDependency;
import org.openbravo.services.webservice.ModuleInstallDetail;
import org.openbravo.services.webservice.SimpleModule;
import org.openbravo.services.webservice.WebService3Impl;
import org.openbravo.services.webservice.WebService3ImplServiceLocator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * ImportModule is able to install modules.
 * 
 * This is done in two steps: -First check if it is possible to install and receive the latest
 * installable modules -Second install them
 * 
 * These two processes are callable independently in order to do it from UI and show messages and
 * wait for confirmation after first one.
 * 
 * It is possible to do the whole installation remotely pulling for the modules to install from the
 * central repository or locally, installing everything from the obx file (which can be passed as an
 * InputStream or as a String file name)
 * 
 */
public class ImportModule {
  static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(ImportModule.class);
  private String obDir;
  private Database db;
  private boolean installLocally = true;
  private boolean antInstall = false;
  private boolean force = false; // force installation though dependencies are
  // not satisfied
  private Module[] modulesToInstall = null;
  private Module[] modulesToUpdate = null;
  private Module[] modulesToMerge = null;
  private StringBuffer log = new StringBuffer();
  private int logLevel = 0;
  VariablesSecureApp vars;

  public static final int MSG_SUCCESS = 0;
  public static final int MSG_WARN = 1;
  public static final int MSG_ERROR = 2;

  OBError errors = null;
  static StringBuilder scanError = null;
  Vector<DynaBean> dynModulesToInstall = new Vector<DynaBean>();
  Vector<DynaBean> dynModulesToUpdate = new Vector<DynaBean>();
  Vector<DynaBean> dependencies = new Vector<DynaBean>();
  Vector<DynaBean> dbprefix = new Vector<DynaBean>();

  boolean checked;
  private String[] dependencyErrors;
  private boolean upgradePrecheckFail;

  private final static String V3_TEMPLATE_ID = "0138E7A89B5E4DC3932462252801FFBC";

  /**
   * Initializes a new ImportModule object, it reads from obdir directory the database model to be
   * able to read the xml information within the obx file.
   * 
   * @param obdir
   *          base directory for the application
   * @param _vars
   *          VariablesSecureApp that will be used to parse messages, if null they will not be
   *          parsed.
   */
  public ImportModule(ConnectionProvider conn, String obdir, VariablesSecureApp _vars) {
    vars = _vars;
    obDir = obdir;
    pool = conn;
    final File[] files = new File[4];
    files[0] = new File(obDir + "/src-db/database/model/tables/AD_MODULE.xml");
    files[1] = new File(obDir + "/src-db/database/model/tables/AD_MODULE_DEPENDENCY.xml");
    files[2] = new File(obDir + "/src-db/database/model/tables/AD_MODULE_DBPREFIX.xml");
    files[3] = new File(obDir + "/src-db/database/model/tables/AD_MODULE_MERGE.xml");

    verifyFilesExist(files);

    db = DatabaseUtils.readDatabaseNoInit(files);
  }

  /**
   * Verifies that the provided files actually exist.
   */
  private void verifyFilesExist(File[] files) {
    for (File file : files) {
      if (!file.exists()) {
        throw new ResourceNotFoundException(String.format(
            Utility.messageBD(pool, "SourceFileNotFound", vars.getLanguage()), file.getPath()));
      }
    }
  }

  /**
   * Check the dependencies for a file name. See
   * {@link #checkDependenciesId(String[], String[], HashMap)}.
   * 
   * @deprecated not used
   */
  public boolean checkDependenciesFileName(String fileName) throws Exception {
    final File file = new File(fileName);
    if (!file.exists())
      throw new Exception("File " + fileName + " do not exist!");
    return checkDependenciesFile(new FileInputStream(file));
  }

  /**
   * Checks whether the given .obx InputStream contains an update to an already installed version.
   * 
   * @param is
   *          an InputStream to the module .obx file
   * @return true if the .obx represents an update to the module
   * @throws Exception
   *           if an error occurs performing the comparison
   */
  public boolean isModuleUpdate(InputStream is) throws Exception {

    boolean isUpdate = false;
    final Vector<DynaBean> modulesInObx = new Vector<DynaBean>();
    getModulesFromObx(modulesInObx, dependencies, dbprefix, is, new HashMap<String, String>());
    // don't care about merges at this stage

    for (final DynaBean module : modulesInObx) {

      String moduleId = (String) module.get("AD_MODULE_ID");
      String moduleName = (String) module.get("NAME");
      String version = (String) module.get("VERSION");

      if (ImportModuleData.moduleInstalled(pool, moduleId)) {
        String installedVersion = ImportModuleData.selectVersion(pool, moduleId);
        VersionUtility.VersionComparator comparator = new VersionUtility.VersionComparator();
        if (comparator.compare(version, installedVersion) > 0) {
          isUpdate = true;
        } else {
          addLog(moduleName + " " + version + " is not an update to "
              + " already installed version " + installedVersion, MSG_WARN);
        }
      } else {
        return true;
      }
    }
    return isUpdate;
  }

  /**
   * Check the dependencies for a file. Used only for local installation from obx file.
   * 
   * @see #checkDependenciesId(String[], String[], HashMap)
   */
  public boolean checkDependenciesFile(InputStream file) throws Exception {

    final Vector<DynaBean> modulesInObx = new Vector<DynaBean>();
    Map<String, String> merges = new HashMap<String, String>();

    getModulesFromObx(modulesInObx, dependencies, dbprefix, file, merges);

    for (final DynaBean module : modulesInObx) {

      String moduleId = (String) module.get("AD_MODULE_ID");
      String version = (String) module.get("VERSION");

      if (ImportModuleData.moduleInstalled(pool, moduleId)) {
        String installedVersion = ImportModuleData.selectVersion(pool, moduleId);
        VersionUtility.VersionComparator comparator = new VersionUtility.VersionComparator();
        if (comparator.compare(version, installedVersion) > 0) {
          dynModulesToUpdate.add(module);
        }
      } else {
        dynModulesToInstall.add(module);
      }
    }

    modulesToInstall = dyanaBeanToModules(dynModulesToInstall, dependencies);
    modulesToUpdate = dyanaBeanToModules(dynModulesToUpdate, dependencies);

    // Check merges
    List<Module> mergesList = new ArrayList<Module>();
    for (Entry<String, String> merge : merges.entrySet()) {
      org.openbravo.model.ad.module.Module mergedDALModule = OBDal.getInstance().get(
          org.openbravo.model.ad.module.Module.class, merge.getKey());
      if (mergedDALModule != null) {
        // Merged module is installed locally, add it as merge to uninstall. In case it is not
        // installed, it does not make sense to show any message to user.
        Module mergedModule = getWsModuleFromDalModule(mergedDALModule);
        HashMap<String, String> additionalInfo = new HashMap<String, String>();
        additionalInfo.put("remove", "true");
        additionalInfo.put("mergedWith", getMergedWith(merge.getValue()));
        mergedModule.setAdditionalInfo(additionalInfo);
        mergesList.add(mergedModule);
      }
    }

    modulesToMerge = mergesList.toArray(new Module[0]);

    errors = new OBError();
    checked = VersionUtility.checkLocal(vars, modulesToInstall, modulesToUpdate, modulesToMerge,
        errors);

    if (antInstall) {
      printAntDependenciesLog();
    }
    if (!checked) {
      try {
        ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Cannot perform installation correctly: " + errors.getMessage()
                + (force ? ". Forced anyway" : ""), "E");
      } catch (final ServletException ex) {
        log4j.error("Error inserting log", ex);
      }
    }
    return checked;
  }

  /**
   * Check the dependencies for a id. After checking dependencies modulesToInstall and
   * modulesToUpdate arrays of modules are completed, thus it is possible to know which are the
   * modules that are needed to install and/or update in order to complete the installation.
   * 
   */
  public boolean checkDependenciesId(String[] installableModules, String[] updateableModules,
      HashMap<String, String> maturityLevels) throws Exception {
    // just for remote usage
    errors = new OBError();
    VersionUtility.setPool(pool);
    final ModuleInstallDetail mid = VersionUtility.checkRemote(vars, installableModules,
        updateableModules, errors, maturityLevels);
    modulesToInstall = mid.getModulesToInstall();
    dependencyErrors = mid.getDependencyErrors();

    // In case core is in the list of modules to update, put in at the last module to update, so it
    // will be updated only in case the rest of modules were successfully downloaded and updated.
    List<Module> updates = new ArrayList<Module>();
    List<Module> merges = new ArrayList<Module>();

    boolean updatingCore = false;
    Module core = null;
    for (Module module : mid.getModulesToUpdate()) {
      if (!module.getModuleID().equals("0")) {
        if ("true".equals(module.getAdditionalInfo().get("remove"))) {
          merges.add(module);
        } else {
          updates.add(module);
        }
      } else {
        updatingCore = true;
        core = module;
      }
    }
    if (updatingCore) {
      updates.add(core);
    }

    modulesToUpdate = updates.toArray(new Module[0]);
    modulesToMerge = merges.toArray(new Module[0]);

    checked = mid.isValidConfiguration();

    upgradePrecheckFail = !checked && mid.getDependencyErrors() != null
        && mid.getDependencyErrors().length > 0
        && "upgrade-precheck".equals(mid.getDependencyErrors()[0]);

    return checked;
  }

  /**
   * Obtains a new {@link Module} from a {@link org.openbravo.model.ad.module.Module DAL Module}
   * 
   * @param dalModule
   *          Original DAL module to convert
   * @return new Module based on DAL module
   */
  private Module getWsModuleFromDalModule(org.openbravo.model.ad.module.Module dalModule) {
    Module rt = new Module();
    rt.setModuleID(dalModule.getId());
    rt.setName(dalModule.getName());
    rt.setVersionNo(dalModule.getVersion());
    return rt;
  }

  /**
   * Obtains the text to show for merged modules about the module that is merged within.
   * 
   * @param moduleId
   * @return
   */
  private String getMergedWith(String moduleId) {
    boolean found = false;
    Module mergedWith = null;

    for (Module module : modulesToInstall) {
      if (moduleId.equals(module.getModuleID())) {
        found = true;
        mergedWith = module;
        break;
      }
    }

    if (!found) {
      for (Module module : modulesToUpdate) {
        if (moduleId.equals(module.getModuleID())) {
          found = true;
          mergedWith = module;
          break;
        }
      }
    }

    if (found) {
      return mergedWith.getName() + " " + mergedWith.getVersionNo();
    } else {
      // This shouldn't happen, the merging module should be found
      return "??";
    }
  }

  /**
   * @deprecated Use {@link ImportModule#checkDependenciesId(String[], String[], HashMap)} instead
   */
  public boolean checkDependenciesId(String[] installableModules, String[] updateableModules)
      throws Exception {
    HashMap<String, String> maturityLevels = new HashMap<String, String>();
    maturityLevels.put("update.level", "500");
    maturityLevels.put("install.level", "500");
    return checkDependenciesId(installableModules, updateableModules, maturityLevels);
  }

  /**
   * Executes the modules installation, first one of the checkDependencies method should have been
   * called in order to set the installable and updateable modules.
   * 
   * This method receives a filename
   * 
   */
  public void execute(String fileName) throws Exception {
    final File file = new File(fileName);
    if (!file.exists())
      throw new Exception("File " + fileName + " do not exist!");
    execute(new FileInputStream(fileName));
  }

  /**
   * Deprecated, use instead ImportModule.execute(InputStream file)
   * 
   * @param file
   * @param file2
   */
  @Deprecated
  public void execute(InputStream file, InputStream file2) {
    execute(file);
  }

  /**
   * Executes the modules installation, first one of the checkDependencies method should have been
   * called in order to set the installable and updateable modules.
   * 
   * This method receives a InputStream of the obx file
   * 
   */
  public void execute(InputStream file) {
    try {
      if (checked || force) {
        if (installLocally) {
          for (Module module : modulesToUpdate) {
            if (!prepareUpdate(module)) {
              return;
            }
          }
          for (Module module : modulesToMerge) {
            if (!prepareUpdate(module)) {
              return;
            }
          }

          File tmpInstall = new File(obDir + "/tmp/localInstall/modules");
          if (!tmpInstall.exists()) {
            tmpInstall.mkdirs();
          }

          // Just pick the first module, to install/update as the rest of them are inside the obx
          // file
          Module module = (modulesToInstall != null && modulesToInstall.length > 0) ? modulesToInstall[0]
              : modulesToUpdate[0];
          installLocalModule(module, file,
              (modulesToInstall != null && modulesToInstall.length > 0));

          // All obx contents (the original module and all its inclusions) has been now unzipped in
          // a temporary directory. modulesToInstall and modulesToUpdate contains the list of
          // modules to install or update (taking into account proper dependencies), so now let's
          // move only the modules in these list to the acutal module directory.
          for (Module m : modulesToInstall) {
            if (!finishLocalInstallation(m)) {
              return;
            }
          }

          for (Module m : modulesToUpdate) {
            if (!finishLocalInstallation(m)) {
              return;
            }
          }

          FileUtils.deleteDirectory(new File(obDir + "/tmp/localInstall"));

          uninstallMerges();
        } else { // install remotely
          execute();
        }
      }
    } catch (final Exception e) {
      log4j.error("Error installing module", e);
      addLog(e.toString(), MSG_ERROR);
      try {
        ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
            e.toString(), "E");
      } catch (final ServletException ex) {
        log4j.error("Error inserting log", ex);
      }
      rollback();
    }
  }

  /**
   * Finishes local installation moving temporary module directories to actual ones.
   */
  private boolean finishLocalInstallation(Module m) throws IOException {
    if ("0".equals(m.getModuleID())) {
      // Core is a special case, it is installed directly in its directory
      return true;
    }

    File tmpInstall = new File(obDir + "/tmp/localInstall/modules");
    File f = new File(tmpInstall, m.getPackageName());
    if (!f.exists()) {
      addLog("@ErrorInstallingLocallyFileNotFound@ " + f, MSG_ERROR);
      FileUtils.deleteDirectory(tmpInstall);
      rollback();
      return false;
    } else {
      File dest = new File(obDir + "/modules/" + m.getPackageName());
      log4j.debug("Moving " + f + " to " + dest);
      FileUtils.moveDirectory(f, dest);
    }
    return true;
  }

  /**
   * Executes the modules installation, first one of the checkDependencies method should have been
   * called in order to set the installable and updateable modules.
   * 
   */
  public void execute() {
    // just for remote installation, modules to install and update must be
    // initialized
    if (checked || force) {
      if ((modulesToInstall == null || modulesToInstall.length == 0)
          && (modulesToUpdate == null || modulesToUpdate.length == 0)) {
        addLog("@ErrorNoModulesToInstall@", MSG_ERROR);
        return;
      }
      if (downloadAllModules()) {
        // if failed downloading, exit installation, no rollback is needed since no actual sources
        // were changed
        installAllModules();
      }
      cleanTmp();
    }
  }

  /**
   * Removes tmp directory where downloaded obx files are temporary stored
   */
  private void cleanTmp() {
    File tmp = new File(obDir + "/tmp");
    if (tmp.exists()) {
      log4j.info("Cleaning " + tmp);
      Utility.deleteDir(tmp);
    }
  }

  /**
   * Downloads all the modules to install/update in the tmp directory. If any error occurs during
   * this process, the process is aborted.
   */
  private boolean downloadAllModules() {
    final File dir = new File(obDir + "/tmp");
    if (!dir.exists())
      dir.mkdirs();

    for (Module module : modulesToInstall) {
      if (!downloadRemoteModule(module)) {
        return false;
      }
    }
    for (Module module : modulesToUpdate) {
      if (!downloadRemoteModule(module)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Downloads a remote module in the tmp directory.
   */
  private boolean downloadRemoteModule(Module module) {
    log4j.info("Downloading " + module.getPackageName() + " " + module.getVersionNo());
    RemoteModule remoteModule = getRemoteModule(module.getModuleVersionID());

    if (remoteModule.isError()) {
      addLog(module.getName(), MSG_ERROR);
      log4j.error("Error downloading module");
      return false;
    }

    InputStream obx = remoteModule.getObx();
    File file = new File(obDir + "/tmp/" + module.getPackageName() + "-" + module.getVersionNo()
        + ".obx");
    log4j.info("File size " + remoteModule.getSize() + " B. Temporary saving in " + file);
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(file);
    } catch (FileNotFoundException e1) {
      log4j.error("Error downloading obx, couldn't create file", e1);
      addLog(module.getName() + "(" + module.getPackageName() + ") @CannotDownloadModule@ " + file,
          MSG_ERROR);
      return false;
    }
    final byte[] buf = new byte[1024];
    int len;
    long size = 0;
    int i = 0;
    DecimalFormat formatter = new DecimalFormat("###.##");
    try {
      // log download, each 10% for big files, each 10K for smaller ones
      int loopsToLog = 10;
      if (remoteModule.getSize() != null) {
        try {
          loopsToLog = (remoteModule.getSize() / 1024) / 10;
          if (loopsToLog < 10) {
            loopsToLog = 10;
          }
        } catch (Exception e) {
          loopsToLog = 10;
        }
      }
      while ((len = obx.read(buf)) > 0) {

        size += len;
        fout.write(buf, 0, len);
        if (remoteModule.getSize() != null) {
          // Print download status log
          i++;
          if (i % loopsToLog == 0) {
            // Do not print for each loop: do it each 30 times (just for big enough modules)
            Double percentage = new Double(size) / new Double(remoteModule.getSize()) * 100;
            String per = formatter.format(percentage);
            log4j.info("  ...downloaded " + size + " " + per + "%");
          }
        }
      }
      fout.close();
      obx.close();

      // Check the obx file has been fully downloaded. This is done now by just checking the
      // dowloaded size is the expected one. In future CRC should be done to guarranty the file is
      // not corrupt, this would require a new service in CR.
      if (remoteModule.getSize() == null || remoteModule.getSize() == size) {
        log4j.info("  Downloaded " + size + " 100% -- OK");
        return true;
      } else {
        addLog(module.getName() + "(" + module.getPackageName() + ") @IncompleteModuleDownload@ "
            + remoteModule.getSize() + "/" + size, MSG_ERROR);
        return false;
      }
    } catch (IOException e) {
      addLog(
          "@ErrorGettingModule@ " + module.getName() + "(" + module.getPackageName() + ") :"
              + e.getMessage(), MSG_ERROR);
      return false;
    }

  }

  /**
   * Installs all the modules to update and install. At this point their obx files have been
   * downloaded locally, so it looks in the tmp directory for the obx and calls the
   * {@link ImportModule#installLocalModule(Module, InputStream, boolean)} method.
   */
  private void installAllModules() {
    // Do installation of new modules
    for (Module module : modulesToInstall) {
      InputStream obx = getTemporaryOBX(module);
      if (obx == null || !installLocalModule(module, obx, true)) {
        return;
      }
    }

    // Do installation of updates
    for (Module module : modulesToUpdate) {
      if (!prepareUpdate(module)) {
        return;
      }
      InputStream obx = getTemporaryOBX(module);
      if (obx == null || !installLocalModule(module, obx, false)) {
        return;
      }
    }

    for (Module module : modulesToMerge) {
      // saves copy of files and removes module directory
      if (!prepareUpdate(module)) {
        return;
      }
    }

    uninstallMerges();

    insertDBLog();
  }

  /**
   * All modules marked as merged will be uninsntalled, the module directory should have been
   * already removed, so it is only pending to set the module as uninstalled
   */
  private void uninstallMerges() {
    if (modulesToMerge == null) {
      return;
    }

    // do not update the audit info here, as its a local config change, which should not be
    // treated as 'local changes' by i.e. update.database
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);
      for (Module module : modulesToMerge) {
        // to uninstall, it is only pending to set status in DB
        org.openbravo.model.ad.module.Module mod = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, module.getModuleID());
        if (mod != null) {
          mod.setStatus("U");
          addLog("@MergeUninstalled@ " + mod.getName(), MSG_SUCCESS);
        }
      }
      OBDal.getInstance().flush();
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  /**
   * Reads in the tmp directory the obx file and returns it as an InputStream.
   */
  private InputStream getTemporaryOBX(Module module) {
    File file = new File(obDir + "/tmp/" + module.getPackageName() + "-" + module.getVersionNo()
        + ".obx");
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      addLog(module.getName() + "(" + module.getPackageName()
          + ") @CannotInstalModuleFileNotFound@ " + file, MSG_ERROR);
      return null;
    }
  }

  /**
   * Installs a module that is passed as InputSteam. It unzips the obx in the proper directory,
   * updates database info about the installed module, updates Openbravo.properties file if updating
   * core and adds entries in .classpth file for installation of new modules in environemts with
   * eclipse.
   */
  private boolean installLocalModule(Module module, InputStream obx, boolean newModule) {
    try {

      final Vector<DynaBean> dynMod = new Vector<DynaBean>();
      final Vector<DynaBean> dynDep = new Vector<DynaBean>();
      final Vector<DynaBean> dynDbPrefix = new Vector<DynaBean>();
      installModule(obx, module.getModuleID(), dynMod, dynDep, dynDbPrefix);

      insertDynaModulesInDB(dynMod, dynDep, dynDbPrefix, newModule);
      if (newModule) {
        // Add entries in .classpath for eclipse users
        addDynaClasspathEntries(dynMod);
      }

      if (module.getModuleID().equals("0")) {
        Utility.mergeOpenbravoProperties(obDir + "/config/Openbravo.properties", obDir
            + "/config/Openbravo.properties.template");
      }
      return true;
    } catch (final Exception e) {
      log4j.error(e.getMessage(), e);
      if (!(e instanceof PermissionException)) {
        addLog("@ErrorGettingModule@ " + module.getName() + "(" + module.getPackageName() + ") :"
            + e.getMessage(), MSG_ERROR);
      }
      rollback();
      try {
        ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Error installing module " + module.getName() + " - " + module.getVersionNo(), "E");
      } catch (final ServletException ex) {
        log4j.error("Error saving log", ex);
      }
      return false;
    }
  }

  /**
   * Prepares a backup for rollback of updates and removes the files within the directories to
   * update, making thus the update like an installation from scratch.
   * 
   */
  private boolean prepareUpdate(Module module) {
    final File dir = new File(obDir + "/backup_install");
    if (!dir.exists())
      dir.mkdirs();

    // take the info from module in db instead from modulesToUpdate because it can be
    // different
    ImportModuleData moduleInDB = null;
    try {
      moduleInDB = ImportModuleData.getModule(pool, module.getModuleID());
    } catch (Exception e) {
      log4j.error("Error getting module from db", e);
    }
    if (moduleInDB == null) {
      addLog("@ErrorDoingBackup@ " + module.getName(), MSG_ERROR);
      return false;
    }

    // Do not maintain multiple backups for different old module's version, remove old existent
    // backups
    for (File existentBackup : dir.listFiles()) {
      if (existentBackup.getName().startsWith(moduleInDB.javapackage + "-")
          && existentBackup.getName().endsWith(".zip")) {
        log4j.info("Deleting old backup file " + existentBackup.getName());
        Utility.deleteDir(existentBackup);
      }
    }

    // Prepare backup for updates
    if (module.getModuleID().equals("0")) { // Updating core
      // set directories to zip
      final File core[] = getCore();

      log4j.info("Zipping core...");
      try {
        Zip.zip(core, obDir + "/backup_install/" + moduleInDB.javapackage + "-"
            + moduleInDB.version + ".zip", obDir);
      } catch (final Exception e) {
        log4j.error("Error zipping module " + module.getName(), e);
        addLog("@ErrorDoingBackup@ " + module.getName(), MSG_ERROR);
        return false;
      }
      log4j.info("Removing old core version files...");
      Utility.deleteDir(core);
    } else {
      // updating a module different than core

      String moduleDir = obDir + "/modules/" + moduleInDB.javapackage;
      File moduleDirFile = new File(moduleDir);
      if (!moduleDirFile.exists()) {
        // nothing to backup, do not create empty zip
        return true;
      }
      try {
        Zip.zip(moduleDir, obDir + "/backup_install/" + moduleInDB.javapackage + "-"
            + moduleInDB.version + ".zip");
        // Delete directory to be updated
        log4j.info("Removing old module version files...");
        Utility.deleteDir(new File(obDir + "/modules/" + moduleInDB.javapackage));
      } catch (final Exception e) {
        log4j.error("Error zipping module " + module.getName(), e);
        addLog("@ErrorDoingBackup@ " + module.getName(), MSG_ERROR);
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the list of modules to update. This list is set by one of the checkDependencies
   * methods.
   */
  public Module[] getModulesToUpdate() {
    return modulesToUpdate;
  }

  /**
   * Returns the list of modules to install. This list is set by one of the checkDependencies
   * methods.
   */
  public Module[] getModulesToInstall() {
    return modulesToInstall;
  }

  /**
   * Returns the list of modules to merge. This list is set by one of the checkDependencies methods.
   */
  public Module[] getModulesToMerge() {
    return modulesToMerge;
  }

  /**
   * Returns the list of errors. This list is set by one of the checkDependencies methods. A list of
   * errors is returned in case the selected modules cannot be installed because dependencies are
   * not satisfied.
   * 
   */
  public OBError getCheckError() {
    return errors;
  }

  public static StringBuilder getScanError() {
    return scanError;
  }

  /**
   * Set the install locally variable, install locally means that no pull is going to be done for
   * the contents of the obx, it will be installed directly from the obx file regardless better
   * versions are available.
   * 
   */
  public void setInstallLocal(boolean v) {
    installLocally = v;
  }

  /**
   * Returns an OBError instance based on the log for the current ImportModule instance
   * 
   */
  public OBError getOBError(ConnectionProvider conn) {
    if (log.length() != 0) {

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

      if (vars != null) {
        final String lang = vars.getLanguage();
        rt.setMessage(Utility.parseTranslation(conn, vars, lang, log.toString()));
        rt.setTitle(Utility.messageBD(conn, rt.getType(), lang));
      } else {
        rt.setMessage(log.toString());
        rt.setTitle(rt.getType());
      }
      return rt;
    } else
      return null;
  }

  /**
   * Rolls back current transaction deleting the already installed modules and recovering the backup
   * for the modules to update.
   * 
   */
  private void rollback() {
    // Modules to install
    addLog("@RollbackInstallation@", MSG_ERROR);
    try {
      ImportModuleData.insertLog(pool, (vars == null ? "0" : vars.getUser()), "", "", "",
          "Rollback installation", "E");
    } catch (final ServletException ex) {
      log4j.error("Error in rollback adding log", ex);
    }

    for (Module module : modulesToInstall) {
      try {
        // remove module from db (in case it is already there)
        ImportModuleData.cleanModuleDependencyInstall(pool, module.getModuleID());
        ImportModuleData.cleanModuleDBPrefixInstall(pool, module.getModuleID());
        ImportModuleData.cleanModuleInstall(pool, module.getModuleID());
      } catch (final Exception e) {
        log4j.error("Error deleting module " + module.getName(), e);
        addLog("Error deleting module " + module.getName() + " from db. " + e.getMessage(),
            MSG_ERROR);
      }

      final File f = new File(obDir + "/modules/" + module.getPackageName());
      if (f.exists()) {
        if (Utility.deleteDir(f)) {
          addLog("@DeletedDirectory@ " + f.getAbsolutePath(), MSG_ERROR);
        } else {
          addLog("@CouldntDeleteDirectory@ " + f.getAbsolutePath(), MSG_ERROR);
        }
      }
    }

    for (Module module : modulesToUpdate) {
      // remove module from db (in case it is already there)
      try {
        ImportModuleData.cleanModuleDependencyInstall(pool, module.getModuleID());
        ImportModuleData.cleanModuleDBPrefixInstall(pool, module.getModuleID());
        ImportModuleData.cleanModuleInstall(pool, module.getModuleID());
      } catch (final Exception e) {
        log4j.error("Error deleting module" + module.getName(), e);
        addLog("Error deleting module " + module.getName() + " from db. " + e.getMessage(),
            MSG_ERROR);
      }
      // take the info from module in db instead from modulesToUpdate because it can be
      // different
      ImportModuleData moduleInDB = null;
      try {
        moduleInDB = ImportModuleData.getModule(pool, module.getModuleID());
      } catch (Exception e) {
        log4j.error("Error reading DB", e);
      }

      String backupFileName = obDir + "/backup_install/" + moduleInDB.javapackage + "-"
          + moduleInDB.version + ".zip";
      File backupFile = new File(backupFileName);
      if (!backupFile.exists()) {
        continue;
      }

      if (module.getModuleID().equals("0")) {
        // restore core
        final File core[] = getCore();
        try {
          log4j.info("Deletig core to restore backup...");
          Utility.deleteDir(core);
          log4j.info("Restoring core " + backupFileName);
          Zip.unzip(backupFileName, obDir);
        } catch (final Exception e) {
          log4j.error("Error restoring core", e);
        }
      } else {
        // restore regular modules
        try {
          File moduleDir = new File(obDir + "/modules/" + module.getPackageName());
          if (moduleDir.exists()) {
            log4j.info("Deleting " + module.getPackageName() + " to restore bakcup...");
            Utility.deleteDir(new File(obDir + "/modules/" + module.getPackageName()));
          }
          log4j.info("Restoring " + backupFileName);
          Zip.unzip(backupFileName, obDir + "/modules/" + module.getPackageName());

        } catch (final Exception e) {
          log4j.error("Error restoring " + module.getName(), e);
        }
      }
    }

    for (Module module : modulesToMerge) {
      ImportModuleData moduleInDB = null;
      try {
        moduleInDB = ImportModuleData.getModule(pool, module.getModuleID());
      } catch (Exception e) {
        log4j.error("Error reading DB", e);
      }

      String backupFileName = obDir + "/backup_install/" + moduleInDB.javapackage + "-"
          + moduleInDB.version + ".zip";
      File backupFile = new File(backupFileName);
      if (!backupFile.exists()) {
        continue;
      }
      try {
        File moduleDir = new File(obDir + "/modules/" + module.getPackageName());
        if (moduleDir.exists()) {
          log4j.info("Deleting " + module.getPackageName() + " to restore bakcup...");
          Utility.deleteDir(new File(obDir + "/modules/" + module.getPackageName()));
        }
        log4j.info("Restoring " + backupFileName);
        Zip.unzip(backupFileName, obDir + "/modules/" + module.getPackageName());

      } catch (final Exception e) {
        log4j.error("Error restoring " + module.getName(), e);
      }
    }
  }

  /**
   * Prints an ant log for the dependencies
   */
  private void printAntDependenciesLog() {
    if (!checked) {
      log4j.error("This module does not satisfy dependencies");
      log4j.warn("Needed dependencies:");
      log4j.warn("  " + errors.getMessage());
    } else {
      if (modulesToInstall != null && modulesToInstall.length > 0) {
        log4j.info("Modules to install:");
        for (int i = 0; i < modulesToInstall.length; i++) {
          log4j.info(modulesToInstall[i].getName() + " " + modulesToInstall[i].getVersionNo());
        }
      }
      if (modulesToUpdate != null && modulesToUpdate.length > 0) {
        log4j.info("Modules to update:");
        for (int i = 0; i < modulesToUpdate.length; i++) {
          log4j.info(modulesToUpdate[i].getName() + " " + modulesToUpdate[i].getVersionNo());
        }
      }
    }
  }

  /**
   * Adds a message with a log level to the current instance log.
   * 
   */
  void addLog(String m, int level) {
    if (level == MSG_ERROR) {
      log4j.error(m);
    } else {
      log4j.info(m);
    }
    if (level > logLevel) {
      logLevel = level;
      log = new StringBuffer(m);
    } else if (level == logLevel)
      log.append(m + "<br>\n");
  }

  /**
   * Receives a Vector<DynaBean> and tranforms it to a Module[]
   */
  private Module[] dyanaBeanToModules(Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dynDependencies) {
    final Module[] rt = new Module[dModulesToInstall.size()];
    int i = 0;

    for (final DynaBean dynModule : dModulesToInstall) {
      rt[i] = new Module();
      rt[i].setModuleID((String) dynModule.get("AD_MODULE_ID"));
      rt[i].setVersionNo((String) dynModule.get("VERSION"));
      rt[i].setName((String) dynModule.get("NAME"));
      rt[i].setLicenseAgreement((String) dynModule.get("LICENSE"));
      rt[i].setLicenseType((String) dynModule.get("LICENSETYPE"));
      rt[i].setPackageName((String) dynModule.get("JAVAPACKAGE"));
      rt[i].setType((String) dynModule.get("TYPE"));
      rt[i].setDescription((String) dynModule.get("DESCRIPTION"));
      rt[i].setHelp((String) dynModule.get("HELP"));
      HashMap<String, String> enforcements = new HashMap<String, String>();
      rt[i].setDependencies(dyanaBeanToDependencies(dynDependencies, rt[i].getModuleID(),
          enforcements));
      // old modules don't have iscommercial column

      String commercial = (String) dynModule.get("ISCOMMERCIAL");
      boolean isCommercial = commercial != null && commercial.equals("Y");
      rt[i].setIsCommercial(isCommercial);
      // To show details in local ad_module_id is used
      rt[i].setModuleVersionID((String) dynModule.get("AD_MODULE_ID"));

      // use this for information that is not contained in standard fields
      HashMap<String, Object> additionalInfo = new HashMap<String, Object>();
      if (isCommercial) {
        String tier = (String) dynModule.get("COMMERCIAL_TIER");
        if (tier == null || tier.isEmpty()) {
          // If tier is not set in the obx, assume tier 2 to show a more restrictive error message
          tier = "2";
        }
        additionalInfo.put("tier", tier);
      }
      additionalInfo.put("enforcements", enforcements);
      rt[i].setAdditionalInfo(additionalInfo);
      i++;
    }
    return rt;
  }

  /**
   * Returns the dependencies in Vector<DynaBean> dynDependencies for the ad_module_id module as a
   * ModuleDependency[], used by dyanaBeanToModules method
   * 
   */
  private ModuleDependency[] dyanaBeanToDependencies(Vector<DynaBean> dynDependencies,
      String ad_module_id, HashMap<String, String> enforcements) {
    final ArrayList<ModuleDependency> dep = new ArrayList<ModuleDependency>();
    try {
      OBContext.setAdminMode();
      for (final DynaBean dynModule : dynDependencies) {
        if (((String) dynModule.get("AD_MODULE_ID")).equals(ad_module_id)) {
          final ModuleDependency md = new ModuleDependency();
          String modId = (String) dynModule.get("AD_DEPENDENT_MODULE_ID");
          md.setModuleID(modId);
          md.setVersionStart((String) dynModule.get("STARTVERSION"));
          md.setVersionEnd((String) dynModule.get("ENDVERSION"));
          md.setModuleName((String) dynModule.get("DEPENDANT_MODULE_NAME"));

          // calculate enforcements, set the local one in case is editable and there is one, other
          // case set the defined in the obx
          OBCriteria<org.openbravo.model.ad.module.ModuleDependency> qDependentMod = OBDal
              .getInstance().createCriteria(org.openbravo.model.ad.module.ModuleDependency.class);
          qDependentMod
              .add(Restrictions.eq(org.openbravo.model.ad.module.ModuleDependency.PROPERTY_MODULE
                  + ".id", ad_module_id));
          qDependentMod.add(Restrictions.eq(
              org.openbravo.model.ad.module.ModuleDependency.PROPERTY_DEPENDENTMODULE + ".id",
              modId));
          String enforcement = null;
          if (!qDependentMod.list().isEmpty()
              && qDependentMod.list().get(0).isUserEditableEnforcement()
              && qDependentMod.list().get(0).getInstanceEnforcement() != null) {
            enforcement = qDependentMod.list().get(0).getInstanceEnforcement();
          } else {
            enforcement = (String) dynModule.get("DEPENDENCY_ENFORCEMENT");
          }
          if (enforcement == null || enforcement.isEmpty()) {
            enforcement = "MAJOR";
          }
          enforcements.put(modId, enforcement);

          dep.add(md);
        }
      }
      final ModuleDependency rt[] = new ModuleDependency[dep.size()];
      for (int i = 0; i < rt.length; i++) {
        rt[i] = dep.get(i);
      }
      return rt;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Adds the classpath entries to .classpath file from the modules in the Vector<DynaBean>
   * 
   */
  private void addDynaClasspathEntries(Vector<DynaBean> dModulesToInstall) throws Exception {
    if (!(new File(obDir + "/.classpath").exists())) {
      log4j.info("No " + obDir + "/.classpath file");
      return;
    }
    log4j.info("Adding .claspath entries");
    final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    final Document doc = docBuilder.parse(obDir + "/.classpath");
    for (final DynaBean module : dModulesToInstall) {
      final String dir = "modules/" + (String) module.get("JAVAPACKAGE") + "/src";
      if (new File(obDir + "/" + dir).exists()) {
        addClassPathEntry(doc, dir);
      } else {
        log4j.info(dir + " does not exist, no claspath entry added");
      }
    }

    // Save the modified xml file to .classpath file
    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    final FileOutputStream fout = new FileOutputStream(obDir + "/.classpath");
    final StreamResult result = new StreamResult(fout);
    final DOMSource source = new DOMSource(doc);
    transformer.transform(source, result);
    fout.close();
  }

  /**
   * Adds a single classpath entry to the xml file
   */
  private void addClassPathEntry(Document doc, String dir) throws Exception {
    log4j.info("adding entry for directory" + dir);
    final Node root = doc.getFirstChild();
    final Node classpath = doc.createElement("classpathentry");
    final NamedNodeMap cpAttributes = classpath.getAttributes();

    Attr attr = doc.createAttribute("kind");
    attr.setValue("src");
    cpAttributes.setNamedItem(attr);

    attr = doc.createAttribute("path");
    attr.setValue(dir);
    cpAttributes.setNamedItem(attr);

    root.appendChild(classpath);

  }

  /**
   * Reads an ZipInputStream and returns it as a ByteArrayInputStream
   * 
   * @param obxInputStream
   * @return
   * @throws Exception
   */
  private ByteArrayInputStream getCurrentEntryStream(ZipInputStream obxInputStream)
      throws Exception {
    final ByteArrayOutputStream fout = new ByteArrayOutputStream();
    for (int c = obxInputStream.read(); c != -1; c = obxInputStream.read()) {
      fout.write(c);
    }
    fout.close();
    final ByteArrayInputStream ba = new ByteArrayInputStream(fout.toByteArray());
    return ba;
  }

  private byte[] getBytesCurrentEntryStream(ZipInputStream obxInputStream) throws Exception {
    final ByteArrayOutputStream fout = new ByteArrayOutputStream();
    final byte[] buf = new byte[1024];
    int len;
    while ((len = obxInputStream.read(buf)) > 0) {
      fout.write(buf, 0, len);
    }

    fout.close();
    return fout.toByteArray();
  }

  /**
   * Inserts in database the Vector<DynaBean> with its dependencies
   * 
   * @param dModulesToInstall
   * @param dependencies1
   * @param newModule
   * @throws Exception
   */
  private void insertDynaModulesInDB(Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dependencies1, Vector<DynaBean> dbPrefix, boolean newModule)
      throws Exception {
    final Properties obProperties = new Properties();
    obProperties.load(new FileInputStream(obDir + "/config/Openbravo.properties"));

    final String url = obProperties.getProperty("bbdd.url")
        + (obProperties.getProperty("bbdd.rdbms").equals("POSTGRE") ? "/"
            + obProperties.getProperty("bbdd.sid") : "");

    final BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(obProperties.getProperty("bbdd.driver"));
    ds.setUrl(url);
    ds.setUsername(obProperties.getProperty("bbdd.user"));
    ds.setPassword(obProperties.getProperty("bbdd.password"));

    final Connection conn = ds.getConnection();

    Integer seqNo = new Integer(ImportModuleData.selectSeqNo(pool));

    for (final DynaBean module : dModulesToInstall) {
      seqNo += 10;
      module.set("ISDEFAULT", "N");
      module.set("STATUS", "I");
      module.set("SEQNO", seqNo);
      module.set("UPDATE_AVAILABLE", null);
      module.set("UPGRADE_AVAILABLE", null);
      log4j.info("Inserting in DB info for module: " + module.get("NAME"));

      String moduleId = (String) module.get("AD_MODULE_ID");

      // Clean temporary tables
      ImportModuleData.cleanModuleInstall(pool, moduleId);
      ImportModuleData.cleanModuleDBPrefixInstall(pool, moduleId);
      ImportModuleData.cleanModuleDependencyInstall(pool, moduleId);

      String type = (String) module.get("TYPE");
      String applyConfigScript = "Y";
      if ("T".equals(type)) {
        if (newModule && V3_TEMPLATE_ID.equals(moduleId)) {
          // When installing V3 template do not apply its config script
          applyConfigScript = "N";
        } else {
          org.openbravo.model.ad.module.Module template = OBDal.getInstance().get(
              org.openbravo.model.ad.module.Module.class, moduleId);
          applyConfigScript = template == null ? "Y" : template.isApplyConfigurationScript() ? "Y"
              : "N";
        }
      }

      // Insert data in temporary tables
      ImportModuleData.insertModuleInstall(pool, moduleId, (String) module.get("NAME"),
          (String) module.get("VERSION"), (String) module.get("DESCRIPTION"),
          (String) module.get("HELP"), (String) module.get("URL"), type,
          (String) module.get("LICENSE"), (String) module.get("ISINDEVELOPMENT"),
          (String) module.get("ISDEFAULT"), seqNo.toString(), (String) module.get("JAVAPACKAGE"),
          (String) module.get("LICENSETYPE"), (String) module.get("AUTHOR"),
          (String) module.get("STATUS"), (String) module.get("UPDATE_AVAILABLE"),
          (String) module.get("ISTRANSLATIONREQUIRED"), (String) module.get("AD_LANGUAGE"),
          (String) module.get("HASCHARTOFACCOUNTS"), (String) module.get("ISTRANSLATIONMODULE"),
          (String) module.get("HASREFERENCEDATA"), (String) module.get("ISREGISTERED"),
          (String) module.get("UPDATEINFO"), (String) module.get("UPDATE_VER_ID"),
          (String) module.get("REFERENCEDATAINFO"), applyConfigScript);

      // Set installed for modules being updated
      ImportModuleData.setModuleUpdated(pool, (String) module.get("AD_MODULE_ID"));

      addLog("@ModuleInstalled@ " + module.get("NAME") + " - " + module.get("VERSION"), MSG_SUCCESS);
    }
    for (final DynaBean module : dependencies1) {
      ImportModuleData.insertModuleDependencyInstall(pool,
          (String) module.get("AD_MODULE_DEPENDENCY_ID"), (String) module.get("AD_MODULE_ID"),
          (String) module.get("AD_DEPENDENT_MODULE_ID"), (String) module.get("STARTVERSION"),
          (String) module.get("ENDVERSION"), (String) module.get("ISINCLUDED"),
          (String) module.get("DEPENDANT_MODULE_NAME"));
    }
    for (final DynaBean module : dbPrefix) {
      ImportModuleData.insertModuleDBPrefixInstall(pool,
          (String) module.get("AD_MODULE_DBPREFIX_ID"), (String) module.get("AD_MODULE_ID"),
          (String) module.get("NAME"));
    }

    conn.close();
  }

  /**
   * Returns all the modules and dependencies described within the obx file (as InputStream)
   * 
   * Used to check dependencies in local installation
   * 
   * @param dModulesToInstall
   * @param dDependencies
   * @param obx
   * @param merges
   *          (output param) It contains all the merges defines in the obx as
   *          (MergedModuleId,MergedBy)
   * @throws Exception
   */
  private void getModulesFromObx(Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dDependencies, Vector<DynaBean> dDBprefix, InputStream obx,
      Map<String, String> merges) throws Exception {
    final ZipInputStream obxInputStream = new ZipInputStream(obx);
    ZipEntry entry = null;
    boolean foundAll = false;
    boolean foundModule = false;
    boolean foundDependency = false;
    boolean foundPrefix = false;
    boolean foundMerge = false;
    while (((entry = obxInputStream.getNextEntry()) != null) && !foundAll) {

      if (entry.getName().endsWith(".obx")) {
        // If it is a new module, install it
        final ByteArrayInputStream ba = getCurrentEntryStream(obxInputStream);
        obxInputStream.closeEntry();
        getModulesFromObx(dModulesToInstall, dDependencies, dDBprefix, ba, merges);
      } else if (entry.getName().replace("\\", "/")
          .endsWith("src-db/database/sourcedata/AD_MODULE.xml")) {
        final Vector<DynaBean> module = getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream));
        boolean isPackage = false;
        if (module != null && module.size() > 0) {
          isPackage = !((String) module.get(0).get("TYPE")).equals("M");
        }
        dModulesToInstall.addAll(module);
        obxInputStream.closeEntry();
        foundModule = true && !isPackage;
      } else if (entry.getName().replace("\\", "/")
          .endsWith("src-db/database/sourcedata/AD_MODULE_DEPENDENCY.xml")) {
        dDependencies.addAll(getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream)));
        obxInputStream.closeEntry();
        foundDependency = true;
      } else if (entry.getName().replace("\\", "/")
          .endsWith("src-db/database/sourcedata/AD_MODULE_DBPREFIX.xml")) {
        dDBprefix.addAll(getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream)));
        obxInputStream.closeEntry();
        foundPrefix = true;
      } else if (entry.getName().replace("\\", "/")
          .endsWith("/src-db/database/sourcedata/AD_MODULE_MERGE.xml")) {
        Vector<DynaBean> dynMerges = getEntryDynaBeans(getBytesCurrentEntryStream(obxInputStream));
        for (DynaBean merge : dynMerges) {
          merges.put((String) merge.get("MERGED_MODULE_UUID"), (String) merge.get("AD_MODULE_ID"));
        }
        obxInputStream.closeEntry();
        foundMerge = true;
      } else {
        obxInputStream.closeEntry();
      }
      foundAll = foundModule && foundDependency && foundPrefix && foundMerge;
    }
    obxInputStream.close();
  }

  /**
   * Reads a ZipInputStream and returns a Vector<DynaBean> with its modules
   * 
   * @param obxInputStream
   * @return
   * @throws Exception
   */
  private Vector<DynaBean> getEntryDynaBeans(byte[] obxEntryBytes) throws Exception {
    final ByteArrayInputStream ba = new ByteArrayInputStream(obxEntryBytes);

    final DatabaseDataIO io = new DatabaseDataIO();
    final DataReader dr = io.getConfiguredCompareDataReader(db);
    dr.getSink().start();
    dr.parse(ba);
    return ((DataToArraySink) dr.getSink()).getVector();
  }

  /**
   * Installs or updates the modules in the obx file
   * 
   * @param obx
   * @param moduleID
   *          The ID for the current module to install
   * @throws Exception
   */
  private void installModule(InputStream obx, String moduleID, Vector<DynaBean> dModulesToInstall,
      Vector<DynaBean> dDependencies, Vector<DynaBean> dDBprefix) throws Exception {

    // For local installations modules are temporary unzipped in tmp/localInstall directory, because
    // it is possible this version in obx is not going to be installed, in any case it must be
    // unzipped looking for other obx files inside it.
    String fileDestination = installLocally && !"0".equals(moduleID) ? obDir + "/tmp/localInstall"
        : obDir;

    if (!(new File(fileDestination + "/modules").canWrite())) {
      addLog("@CannotWriteDirectory@ " + fileDestination + "/modules. ", MSG_ERROR);
      throw new PermissionException("Cannot write on directory: " + fileDestination + "/modules");
    }

    final ZipInputStream obxInputStream = new ZipInputStream(obx);
    ZipEntry entry = null;
    while ((entry = obxInputStream.getNextEntry()) != null) {
      if (entry.getName().endsWith(".obx")) { // If it is a new module
        // install it
        if (installLocally) {
          final ByteArrayInputStream ba = new ByteArrayInputStream(
              getBytesCurrentEntryStream(obxInputStream));

          installModule(ba, moduleID, dModulesToInstall, dDependencies, dDBprefix);
        } // If install remotely it is no necessary to install the .obx
        // because it will be get from CR
        obxInputStream.closeEntry();
      } else {
        // Unzip the contents
        final String fileName = fileDestination + (moduleID.equals("0") ? "/" : "/modules/")
            + entry.getName().replace("\\", "/");
        final File entryFile = new File(fileName);
        // Check whether the directory exists, if not create

        File dir = null;
        if (entryFile.getParent() != null)
          dir = new File(entryFile.getParent());
        if (entry.isDirectory())
          dir = entryFile;

        if (entry.isDirectory() || entryFile.getParent() != null) {
          if (!dir.exists()) {
            log4j.info("Created dir: " + dir.getAbsolutePath());
            dir.mkdirs();
          }
        }

        if (!entry.isDirectory()) {
          // It is a file
          byte[] entryBytes = null;
          boolean found = false;
          // Read the xml file to obtain module info
          if (entry.getName().replace("\\", "/")
              .endsWith("src-db/database/sourcedata/AD_MODULE.xml")) {
            entryBytes = getBytesCurrentEntryStream(obxInputStream);
            final Vector<DynaBean> module = getEntryDynaBeans(entryBytes);
            moduleID = (String) module.get(0).get("AD_MODULE_ID");
            if (installingModule(moduleID)) {
              dModulesToInstall.addAll(module);
            }
            obxInputStream.closeEntry();
            found = true;
          } else if (entry.getName().replace("\\", "/")
              .endsWith("src-db/database/sourcedata/AD_MODULE_DEPENDENCY.xml")) {
            entryBytes = getBytesCurrentEntryStream(obxInputStream);
            final Vector<DynaBean> dep = getEntryDynaBeans(entryBytes);
            if (installingModule((String) dep.get(0).get("AD_MODULE_ID"))) {
              dDependencies.addAll(dep);
            }
            obxInputStream.closeEntry();
            found = true;
          } else if (entry.getName().replace("\\", "/")
              .endsWith("src-db/database/sourcedata/AD_MODULE_DBPREFIX.xml")) {
            entryBytes = getBytesCurrentEntryStream(obxInputStream);
            final Vector<DynaBean> dbp = getEntryDynaBeans(entryBytes);
            if (installingModule((String) dbp.get(0).get("AD_MODULE_ID"))) {
              dDBprefix.addAll(dbp);
            }
            obxInputStream.closeEntry();
            found = true;
          }

          // Unzip the file
          log4j.info("Installing " + fileName);

          final FileOutputStream fout = new FileOutputStream(entryFile);

          if (found) {
            // the entry is already read as a byte[]
            fout.write(entryBytes);
          } else {
            final byte[] buf = new byte[4096];
            int len;
            while ((len = obxInputStream.read(buf)) > 0) {
              fout.write(buf, 0, len);
            }
          }

          fout.close();
        }

        obxInputStream.closeEntry();
      }
    }
    obxInputStream.close();
  }

  /**
   * In case of local installation it checks whether a moduleId is in the list of modules to install
   * or update in order not to install versions included within obx but that are lower than the
   * currently installed ones.
   * 
   */
  private boolean installingModule(String moduleID) {
    if (!installLocally) {
      return true;
    }

    for (Module m : modulesToInstall) {
      if (moduleID.equals(m.getModuleID())) {
        return true;
      }
    }
    for (Module m : modulesToUpdate) {
      if (moduleID.equals(m.getModuleID())) {
        return true;
      }
    }
    return false;
  }

  public boolean getIsLocal() {
    return installLocally;
  }

  /**
   * Inserts log in ad_module_log table
   * 
   */
  private void insertDBLog() {
    try {
      final String user = vars == null ? "0" : vars.getUser();
      if (modulesToInstall != null && modulesToInstall.length > 0) {
        for (int i = 0; i < modulesToInstall.length; i++) {
          ImportModuleData.insertLog(
              pool,
              user,
              modulesToInstall[i].getModuleID(),
              modulesToInstall[i].getModuleVersionID(),
              modulesToInstall[i].getName(),
              "Installed module " + modulesToInstall[i].getName() + " - "
                  + modulesToInstall[i].getVersionNo(), "I");
        }
      }
      if (modulesToUpdate != null && modulesToUpdate.length > 0) {
        for (int i = 0; i < modulesToUpdate.length; i++) {
          ImportModuleData.insertLog(pool, user, modulesToUpdate[i].getModuleID(),
              modulesToUpdate[i].getModuleVersionID(), modulesToUpdate[i].getName(),
              "Updated module " + modulesToUpdate[i].getName() + " to version "
                  + modulesToUpdate[i].getVersionNo(), "U");
        }
      }

      for (Module merge : modulesToMerge) {
        ImportModuleData.insertLog(pool, user, merge.getModuleID(), merge.getModuleVersionID(),
            merge.getName(), "Uninstalled module " + merge.getName() + " because of merge.", "D");
      }

    } catch (final ServletException e) {
      log4j.error("Error inserting log", e);
    }
  }

  /**
   * Scans for updates for the existent modules and sets and returs the list of modules that have
   * updates available
   * 
   * @param conn
   * @param vars
   * @return the list of updates keyed by module id
   */
  @SuppressWarnings("unchecked")
  public static HashMap<String, String> scanForUpdates(ConnectionProvider conn,
      VariablesSecureApp vars) {
    scanError = new StringBuilder();
    try {
      final HashMap<String, String> updateModules = new HashMap<String, String>();
      final String user = vars == null ? "0" : vars.getUser();
      ImportModuleData.insertLog(conn, user, "", "", "", "Scanning For Updates", "S");

      if (!HttpsUtils.isInternetAvailable()) {
        // Check Internet availability and set proxy if required
        ImportModuleData.insertLog(conn, user, "", "", "",
            "Scan for updates: Couldn't contact with webservice server", "E");
        log4j.error("Scan for updates, error cound't reach ws server");
        scanError.append("InternetNotAvailable");
        return updateModules;
      }

      WebService3ImplServiceLocator loc;
      WebService3Impl ws = null;
      SimpleModule[] updates;
      try {
        loc = new WebService3ImplServiceLocator();
        ws = loc.getWebService3();

        updates = ws.moduleScanForUpdates(getInstalledModulesAndDeps(),
            ModuleUtiltiy.getSystemMaturityLevels(false));
      } catch (final Exception e) {
        // do nothing just log the error
        log4j.error("Scan for updates coulnd't contact WS", e);
        try {
          ImportModuleData.insertLog(conn, user, "", "", "",
              "Scan for updates: Couldn't contact with webservice server", "E");
        } catch (final ServletException ex) {
          log4j.error("Error inserting log", e);
        }
        scanError.append("WSServerNotReachable");
        return updateModules; // return empty hashmap
      }

      if (updates != null && updates.length > 0) {
        for (int i = 0; i < updates.length; i++) {

          if (!ImportModuleData.existsVersion(conn, updates[i].getVersionNo(),
              updates[i].getModuleVersionID())) {
            ImportModuleData.updateNewVersionAvailable(conn, updates[i].getVersionNo(),
                updates[i].getModuleVersionID(), updates[i].getUpdateDescription(),
                updates[i].getModuleID());
            ImportModuleData.insertLog(conn, user, updates[i].getModuleID(),
                updates[i].getModuleVersionID(), updates[i].getName(), "Found new version "
                    + updates[i].getVersionNo() + " for module " + updates[i].getName(), "S");
            updateModules.put(updates[i].getModuleID(), "U");
          }

          HashMap<String, String> additionalInfo = updates[i].getAdditionalInfo();
          if (additionalInfo != null && additionalInfo.containsKey("upgrade")) {
            log4j.info("Upgrade found:" + additionalInfo.get("upgrade"));
            JSONObject upgrade = new JSONObject((String) additionalInfo.get("upgrade"));
            final String moduleId = upgrade.getString("moduleId");
            org.openbravo.model.ad.module.Module module = OBDal.getInstance().get(
                org.openbravo.model.ad.module.Module.class, moduleId);
            if (module != null) {
              try {
                OBInterceptor.setPreventUpdateInfoChange(true);
                module.setUpgradeAvailable(upgrade.getJSONArray("showVersion").toString());
                OBDal.getInstance().flush();
              } finally {
                OBInterceptor.setPreventUpdateInfoChange(false);
              }
            } else {
              log4j.error("There is an upgrade for module " + moduleId
                  + ", but it is not present in the instance.");
            }
          }
        }
        addParentUpdates(updateModules, conn);
      }
      try {
        ImportModuleData.insertLog(conn, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Total: found " + updateModules.size() + " updates", "S");
      } catch (final ServletException ex) {
        log4j.error("Error inserting log", ex);
      }
      return updateModules;
    } catch (final Exception e) {
      log4j.error("Scan for updates failed", e);
      try {
        ImportModuleData.insertLog(conn, (vars == null ? "0" : vars.getUser()), "", "", "",
            "Scan for updates: Error: " + e.toString(), "E");
      } catch (final ServletException ex) {
        log4j.error("Error inserting log", ex);
      }
      scanError.append("ScanUpdatesFailed");
      return new HashMap<String, String>();
    }
  }

  private static void addParentUpdates(HashMap<String, String> updates, ConnectionProvider conn) {
    @SuppressWarnings("unchecked")
    final HashMap<String, String> iniUpdates = (HashMap<String, String>) updates.clone();
    for (final String node : iniUpdates.keySet()) {
      addParentNode(node, updates, iniUpdates, conn);
    }
  }

  private static void addParentNode(String node, HashMap<String, String> updates,
      HashMap<String, String> iniUpdates, ConnectionProvider conn) {
    String parentId;
    try {
      parentId = ImportModuleData.getParentNode(conn, node);
    } catch (final ServletException e) {
      // do nothing just stop adding elements
      log4j.error("Error adding parent node", e);
      return;
    }
    if (parentId == null || parentId.equals(""))
      return;
    if (updates.get(parentId) == null && iniUpdates.get(parentId) == null)
      updates.put(parentId, "P");
    addParentNode(parentId, updates, iniUpdates, conn);
  }

  /**
   * Returns the current installed modules with its version
   * 
   * @deprecated use {@link ImportModule#getInstalledModulesAndDeps} instead
   * @param conn
   *          ConnectionProvider needed as it is a static method
   * @return HashMap<String,String> -> <ModuleId, VersionNo>
   */
  public static HashMap<String, String> getInstalledModules(ConnectionProvider conn) {
    final HashMap<String, String> rt = new HashMap<String, String>();
    ImportModuleData data[] = null;
    try {
      data = ImportModuleData.selectInstalled(conn);
    } catch (final Exception e) {
      log4j.error("Error getting installed modules", e);
    }
    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        rt.put(data[i].adModuleId, data[i].version);
      }
    }
    return rt;
  }

  /**
   * Returns the current installed modules with its version (with the exception of those which are
   * marked for deinstallation)
   * 
   * @return HashMap<String, String[][]> --> <ModuleId, VersionInfo[]>
   *         <ul>
   *         <li>VersionInfo [x][0] -> Type "M" Module, "D" Dependency</li>
   *         <li>VersionInfo [x][1] -> If type=="M", version number. If type =="D" dep module Id</li>
   *         <li>VersionInfo [x][2] -> If type=="M", module visibility level. If type=="D", from
   *         version</li>
   *         <li>VersionInfo [x][3] -> If type=="D", to version</li>
   *         <li>VersionInfo [x][4] -> If type=="D", "Y"/"N" is included</li>
   *         <li>VersionInfo [x][5] -> If type=="D", Dependent module name</li>
   *         <li>VersionInfo [x][6] -> If type=="D", Dependency enforcement</li>
   *         <li>VersionInfo [x][7] -> If type=="D", Instance dependency enforcement</li>
   *         </ul>
   */
  public static HashMap<String, String[][]> getInstalledModulesAndDeps() {
    HashMap<String, String[][]> rt = new HashMap<String, String[][]>();
    try {
      OBContext.setAdminMode();

      String defaultMaturity = OBDal.getInstance().get(SystemInformation.class, "0")
          .getMaturityUpdate();
      List<String> installingMods = new ArrayList<String>();

      // Taking into account modules in process of intallation. These modules have higher priority
      // over actually installed ones
      OBCriteria<ModuleInstall> qModInstall = OBDal.getInstance().createCriteria(
          ModuleInstall.class);
      for (ModuleInstall mod : qModInstall.list()) {
        org.openbravo.model.ad.module.Module currentDBVersion = OBDal.getInstance().get(
            org.openbravo.model.ad.module.Module.class, mod.getModule());

        String[][] versionInfo = new String[1][0];
        versionInfo[0] = new String[3];
        versionInfo[0][0] = "M";
        versionInfo[0][1] = mod.getVersion();
        versionInfo[0][2] = currentDBVersion == null ? defaultMaturity : currentDBVersion
            .getMaturityUpdate();

        // Dependencies are not needed as it is a version installed from CR

        rt.put(mod.getModule(), versionInfo);
        installingMods.add(mod.getModule());
      }

      OBCriteria<org.openbravo.model.ad.module.Module> obCriteria = OBDal.getInstance()
          .createCriteria(org.openbravo.model.ad.module.Module.class);
      obCriteria.add(Restrictions.not(Restrictions.eq(
          org.openbravo.model.ad.module.Module.PROPERTY_STATUS, "U")));
      if (!installingMods.isEmpty()) {
        obCriteria.add(Restrictions.not(Restrictions.in(
            org.openbravo.model.ad.module.Module.PROPERTY_ID, installingMods)));
      }
      List<org.openbravo.model.ad.module.Module> modules = obCriteria.list();

      boolean activeInstance = ActivationKey.getInstance().isActive();

      for (org.openbravo.model.ad.module.Module mod : modules) {

        List<org.openbravo.model.ad.module.ModuleDependency> dependencies = mod
            .getModuleDependencyList();

        String[][] versionInfo = new String[dependencies.size() + 1][0];
        versionInfo[0] = new String[3];
        versionInfo[0][0] = "M";
        versionInfo[0][1] = mod.getVersion();

        if (!activeInstance && mod.getMaturityUpdate() != null
            && Integer.parseInt(mod.getMaturityUpdate()) >= MaturityLevel.CS_MATURITY) {
          // Community instances are not allowed to use GA maturity, setting it to CR
          versionInfo[0][2] = Integer.toString(MaturityLevel.QA_APPR_MATURITY);
        } else {
          versionInfo[0][2] = mod.getMaturityUpdate();
        }

        int i = 1;
        for (org.openbravo.model.ad.module.ModuleDependency dep : dependencies) {
          versionInfo[i] = new String[8];
          versionInfo[i][0] = "D";
          versionInfo[i][1] = dep.getDependentModule().getId();
          versionInfo[i][2] = dep.getFirstVersion();
          versionInfo[i][3] = dep.getLastVersion();
          versionInfo[i][4] = dep.isIncluded() ? "Y" : "N";
          versionInfo[i][5] = dep.getDependantModuleName();
          versionInfo[i][6] = dep.getDependencyEnforcement();
          versionInfo[i][7] = dep.isUserEditableEnforcement() ? dep.getInstanceEnforcement() : null;
          i++;
        }

        rt.put(mod.getId(), versionInfo);
      }

      return rt;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a File array with the directories that are part of core
   * 
   */
  private File[] getCore() {
    ArrayList<File> core = new ArrayList<File>();
    core.add(new File(obDir + "/build.xml"));
    core.add(new File(obDir + "/legal"));
    core.add(new File(obDir + "/lib"));
    core.add(new File(obDir + "/src-core"));
    core.add(new File(obDir + "/src-db"));
    core.add(new File(obDir + "/src-gen"));
    core.add(new File(obDir + "/src-trl"));
    core.add(new File(obDir + "/src-wad"));
    core.add(new File(obDir + "/src"));
    core.add(new File(obDir + "/web"));
    core.add(new File(obDir + "/src-test"));
    core.add(new File(obDir + "/src-util"));
    File[] module = new File[core.size()];
    return core.toArray(module);
  }

  /**
   * Returns the module with the ID that is in the module to install or update.
   * 
   * @param moduleID
   * @return the module with the moduleID, or null if not found
   */
  public Module getModule(String moduleID) {
    for (int i = 0; i < modulesToInstall.length; i++) {
      if (modulesToInstall[i].getModuleID().equals(moduleID))
        return modulesToInstall[i];
    }
    for (int i = 0; i < modulesToUpdate.length; i++) {
      if (modulesToUpdate[i].getModuleID().equals(moduleID))
        return modulesToUpdate[i];
    }
    return null;
  }

  private class PermissionException extends Exception {
    private static final long serialVersionUID = 1L;

    public PermissionException(String msg) {
      super(msg);
    }
  }

  /**
   * Obtains remotely an obx for the desired moduleVersionID
   * 
   */
  private RemoteModule getRemoteModule(String moduleVersionID) {
    RemoteModule remoteModule = new RemoteModule();
    WebService3ImplServiceLocator loc;
    WebService3Impl ws = null;
    String strUrl = "";
    boolean isCommercial;

    try {
      loc = new WebService3ImplServiceLocator();
      ws = loc.getWebService3();
    } catch (final Exception e) {
      log4j.error(e);
      addLog("@CouldntConnectToWS@", ImportModule.MSG_ERROR);
      try {
        ImportModuleData.insertLog(ImportModule.pool, (vars == null ? "0" : vars.getUser()), "",
            "", "", "Couldn't contact with webservice server", "E");
      } catch (final ServletException ex) {
        log4j.error(ex);
      }
      remoteModule.setError(true);
      return remoteModule;
    }

    try {
      isCommercial = ws.isCommercial(moduleVersionID);
      strUrl = ws.getURLforDownload(moduleVersionID);
    } catch (AxisFault e1) {
      addLog("@" + e1.getFaultCode() + "@", ImportModule.MSG_ERROR);
      remoteModule.setError(true);
      return remoteModule;
    } catch (RemoteException e) {
      addLog(e.getMessage(), ImportModule.MSG_ERROR);
      remoteModule.setError(true);
      return remoteModule;
    }

    if (isCommercial && !ActivationKey.isActiveInstance()) {
      addLog("@NotCommercialModulesAllowed@", ImportModule.MSG_ERROR);
      remoteModule.setError(true);
      return remoteModule;
    }

    try {
      URL url = new URL(strUrl);
      HttpURLConnection conn = null;

      if (strUrl.startsWith("https://")) {
        ActivationKey ak = ActivationKey.getInstance();
        String instanceKey = "obinstance=" + URLEncoder.encode(ak.getPublicKey(), "utf-8");
        conn = HttpsUtils.sendHttpsRequest(url, instanceKey);
      } else {
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Keep-Alive", "300");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
      }

      if (conn.getResponseCode() == HttpServletResponse.SC_OK) {
        // OBX is ready to be used
        remoteModule.setObx(conn.getInputStream());
        String size = conn.getHeaderField("Content-Length");
        if (size != null) {
          remoteModule.setSize(new Integer(size));
        }
        return remoteModule;
      }

      // There is an error, let's check for a parseable message
      String msg = conn.getHeaderField("OB-ErrMessage");
      if (msg != null) {
        addLog(msg, ImportModule.MSG_ERROR);
      } else {
        addLog("@ErrorDownloadingOBX@ " + conn.getResponseCode(), ImportModule.MSG_ERROR);
      }
    } catch (Exception e) {
      addLog("@ErrorDownloadingOBX@ " + e.getMessage(), ImportModule.MSG_ERROR);
    }
    remoteModule.setError(true);
    return remoteModule;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public String[] getDependencyErrors() {
    return dependencyErrors;
  }

  public boolean isUpgradePrecheckFail() {
    return upgradePrecheckFail;
  }

}
