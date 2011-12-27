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

package org.openbravo.service.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.ExcludeFilter;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.ddlutils.task.DatabaseUtils;
import org.openbravo.ddlutils.util.DBSMOBUtil;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.OBScheduler;
import org.openbravo.service.system.SystemValidationResult.SystemValidationType;
import org.quartz.SchedulerException;

/**
 * Provides utility like services.
 * 
 * @author Martin Taal
 */
public class SystemService implements OBSingleton {
  private static SystemService instance;
  private static final Logger log4j = Logger.getLogger(SystemService.class);

  public static synchronized SystemService getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(SystemService.class);
    }
    return instance;
  }

  public static synchronized void setInstance(SystemService instance) {
    SystemService.instance = instance;
  }

  /**
   * Returns true if for a certain class there are objects which have changed.
   * 
   * @param clzs
   *          the type of objects which are checked
   * @param afterDate
   *          the timestamp to check
   * @return true if there is an object in the database which changed since afterDate, false
   *         otherwise
   */
  public boolean hasChanged(Class<?>[] clzs, Date afterDate) {
    for (Class<?> clz : clzs) {
      @SuppressWarnings("unchecked")
      final OBCriteria<?> obc = OBDal.getInstance().createCriteria((Class<BaseOBObject>) clz);
      obc.add(Restrictions.gt(Organization.PROPERTY_UPDATED, afterDate));
      // todo: count is slower than exists, is exists possible?
      if (obc.count() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Validates a specific module, checks the javapackage, dependency on core etc. The database
   * changes of the module are not checked. This is a separate task.
   * 
   * @param module
   *          the module to validate
   * @param database
   *          the database to read the dbschema from
   * @return the validation result
   */
  public SystemValidationResult validateModule(Module module, Database database) {
    final ModuleValidator moduleValidator = new ModuleValidator();
    moduleValidator.setValidateModule(module);
    return moduleValidator.validate();
  }

  /**
   * Validates the database for a specific module.
   * 
   * @param module
   *          the module to validate
   * @param database
   *          the database to read the dbschema from
   * @return the validation result
   */
  public SystemValidationResult validateDatabase(Module module, Database database) {
    final DatabaseValidator databaseValidator = new DatabaseValidator();
    databaseValidator.setValidateModule(module);
    databaseValidator.setDatabase(database);
    databaseValidator.setDbsmExecution(true);
    return databaseValidator.validate();
  }

  /**
   * Prints the validation result grouped by validation type to the log.
   * 
   * @param log
   *          the log to which the validation result is printed
   * @param result
   *          the validation result containing both errors and warning
   * @return the errors are returned as a string
   */
  public String logValidationResult(Logger log, SystemValidationResult result) {
    for (SystemValidationType validationType : result.getWarnings().keySet()) {
      log.warn("\n");
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.warn("Warnings for Validation type: " + validationType);
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> warnings = result.getWarnings().get(validationType);
      for (String warning : warnings) {
        log.warn(warning);
      }
    }

    final StringBuilder sb = new StringBuilder();
    for (SystemValidationType validationType : result.getErrors().keySet()) {
      sb.append("\n");
      sb.append("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      sb.append("\nErrors for Validation type: " + validationType);
      sb.append("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> errors = result.getErrors().get(validationType);
      for (String err : errors) {
        sb.append("\n");
        sb.append(err);
      }
    }
    log.error(sb.toString());
    return sb.toString();
  }

  /**
   * Removes all data of a specific {@link Client}, the client is identified by the clientId
   * parameter.
   * 
   * NOTE: this method does not work yet. It is an initial implementation and not yet complete
   * 
   * @param clientId
   *          the id of the client to delete.
   * @deprecated Do not use, is a work in progress
   */
  public void removeAllClientData(String clientId) {
    // the idea was/is the following:
    // 0) compute the order of all entities based on their reference, something like the
    // low-level code in BOM computations: the entity nobody refers to has number 0, the
    // rule is that if there are two entities A and B and there is a reference path from A
    // to B (directly or through other entities) using only non-mandatory many-to-one references
    // then: A.referenceNumber < B.referenceNumber
    // Then the entities can be sorted ascending on the referenceNumber
    // the procedure is then:
    // 1) nullify all non-mandatory many-to-ones
    // 2) then remove the objects in order of the entity.referenceNumber
    // currently this does not work yet because step 1 fails because there are constraints
    // defined in the database which means that certain fields are conditionally mandatory.

    OBContext.setAdminMode();
    try {
      TriggerHandler.getInstance().disable();
      final Client client = OBDal.getInstance().get(Client.class, clientId);
      for (Entity e : ModelProvider.getInstance().getModel()) {
        if (!e.isClientEnabled()) {
          continue;
        }
        nullifyManyToOnes(e, client);
      }
      OBDal.getInstance().flush();

      for (Entity e : ModelProvider.getInstance().getModel()) {
        if (!e.isClientEnabled()) {
          continue;
        }
        final String hql;
        if (e.getName().equals(ClientInformation.ENTITY_NAME)) {
          hql = "delete " + e.getName() + " where id=:clientId";
        } else {
          hql = "delete " + e.getName() + " where client=:clientId";
        }
        SessionHandler.getInstance().getSession().createQuery(hql).setString("clientId", clientId)
            .executeUpdate();
      }
      OBDal.getInstance().flush();
      TriggerHandler.getInstance().enable();
      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void nullifyManyToOnes(Entity e, Client client) {
    final String updatePart = createNullifyNonMandatoryQuery(e);
    if (updatePart == null) {
      return;
    }
    final String hql;
    if (e.getName().equals(ClientInformation.ENTITY_NAME)) {
      hql = updatePart + " where id=:clientId";
    } else {
      hql = updatePart + " where client=:clientId";
    }
    try {
      SessionHandler.getInstance().getSession().createQuery(hql)
          .setString("clientId", client.getId()).executeUpdate();
    } catch (IllegalArgumentException ex) {
      // handle a special case, that the entity name or a property name
      // is a reserved hql word.
      if (ex.getMessage().indexOf("node to traverse cannot be null") != -1) {
        // in this case use an inefficient method
        nullifyPerObject(e, client);
      } else {
        throw ex;
      }
    }
  }

  private String createNullifyNonMandatoryQuery(Entity e) {
    final StringBuilder sb = new StringBuilder("update " + e.getClassName() + " e set ");
    boolean doNullifyProperty = false;
    for (Property p : e.getProperties()) {
      if (!p.isPrimitive() && !p.isOneToMany() && !p.isMandatory()) {
        if (doNullifyProperty) {
          sb.append(", ");
        }
        sb.append("e." + p.getName() + " = null");
        doNullifyProperty = true;
      }
    }
    // no property found, don't do update
    if (!doNullifyProperty) {
      return null;
    }
    return sb.toString();
  }

  private void nullifyPerObject(Entity e, Client client) {
    final OBCriteria<BaseOBObject> obc = OBDal.getInstance().createCriteria(e.getName());
    obc.setFilterOnActive(false);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Restrictions.eq(Organization.PROPERTY_CLIENT, client));
    for (BaseOBObject bob : obc.list()) {
      for (Property p : e.getProperties()) {
        if (!p.isPrimitive() && !p.isOneToMany() && !p.isMandatory()) {
          bob.set(p.getName(), null);
        }
      }
    }
  }

  /**
   * This process deletes a client from the database. During its execution, the Scheduler is
   * stopped, and all sessions active for other users are cancelled
   * 
   * @param client
   *          The client to be deleted
   */
  public void deleteClient(Client client) {
    try {
      long t1 = System.currentTimeMillis();
      Platform platform = getPlatform();
      Connection con = OBDal.getInstance().getConnection();
      killConnectionsAndSafeMode(con);
      try {
        if (OBScheduler.getInstance() != null && OBScheduler.getInstance().getScheduler() != null
            && OBScheduler.getInstance().getScheduler().isStarted())
          OBScheduler.getInstance().getScheduler().standby();
      } catch (Exception e) {
        throw new RuntimeException("Could not shutdown scheduler", e);
      }
      OBDal.getInstance().getConnection().commit();
      disableConstraints(platform);
      OBContext.setAdminMode(false);
      OBDal.getInstance().flush();
      OBDal.getInstance().getConnection().commit();
      String clientId = (String) DalUtil.getId(client);

      List<String> sqlCommands = new ArrayList<String>();

      List<Entity> entities = ModelProvider.getInstance().getModel();
      for (Entity entity : entities) {
        if ((entity.isClientEnabled() || entity.getName().equals("ADClient")) && !entity.isView()) {
          final String sql = "delete from " + entity.getTableName() + " where ad_client_id=?";
          sqlCommands.add(sql);
        }
      }
      for (String command : sqlCommands) {
        PreparedStatement ps = con.prepareStatement(command);
        ps.setString(1, clientId);
        ps.executeUpdate();
      }
      PreparedStatement stpref = con
          .prepareStatement("DELETE FROM ad_preference p where visibleat_client_id=?");
      stpref.setString(1, clientId);
      stpref.executeUpdate();
      PreparedStatement stpers = con
          .prepareStatement("DELETE FROM obuiapp_uipersonalization p where visibleat_client_id=?");
      stpers.setString(1, clientId);
      stpers.executeUpdate();
      con.commit();
      OBDal.getInstance().commitAndClose();
      enableConstraints(platform);
      Connection con2 = platform.borrowConnection();
      try {
        resetSafeMode(con2);
      } finally {
        platform.returnConnection(con2);
      }
      log4j.info("Deletion of client " + clientId + " took " + (System.currentTimeMillis() - t1)
          + " miliseconds");
    } catch (Exception e) {
      log4j.error("exception when deleting the client: ", e);
    } finally {
      OBContext.restorePreviousMode();
      // We restart the scheduler
      try {
        if (OBScheduler.getInstance() != null && OBScheduler.getInstance().getScheduler() != null) {
          OBScheduler.getInstance().getScheduler().start();
        }
      } catch (SchedulerException e) {
        log4j.error("There was an error while restarting the scheduler", e);
      }
    }
  }

  private void resetSafeMode(Connection con) {

    try {
      PreparedStatement ps2 = con
          .prepareStatement("UPDATE AD_SYSTEM_INFO SET SYSTEM_STATUS='RB70'");
      ps2.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't reset the safe mode", e);
    }
  }

  private void killConnectionsAndSafeMode(Connection con) {
    try {
      PreparedStatement updateSession = con
          .prepareStatement("UPDATE AD_SESSION SET SESSION_ACTIVE='N' WHERE CREATEDBY<>?");
      updateSession.setString(1, OBContext.getOBContext().getUser().getId());
      updateSession.executeUpdate();
      PreparedStatement ps2 = con
          .prepareStatement("UPDATE AD_SYSTEM_INFO SET SYSTEM_STATUS='RB80'");
      ps2.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't destroy concurrent sessions", e);
    }
  }

  /**
   * Returns a dbsourcemanager Platform object
   * 
   * @return A Platform object built following the configuration set in the Openbravo.properties
   *         file
   */
  public Platform getPlatform() {
    Properties obProp = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    // We disable check constraints before inserting reference data
    String driver = obProp.getProperty("bbdd.driver");
    String url = obProp.getProperty("bbdd.rdbms").equals("POSTGRE") ? obProp
        .getProperty("bbdd.url") + "/" + obProp.getProperty("bbdd.sid") : obProp
        .getProperty("bbdd.url");
    String user = obProp.getProperty("bbdd.user");
    String password = obProp.getProperty("bbdd.password");
    BasicDataSource datasource = DBSMOBUtil.getDataSource(driver, url, user, password);
    Platform platform = PlatformFactory.createNewPlatformInstance(datasource);
    return platform;
  }

  private void disableConstraints(Platform platform) throws FileNotFoundException, IOException {
    log4j.info("Disabling constraints...");
    ExcludeFilter excludeFilter = DBSMOBUtil.getInstance().getExcludeFilter(
        new File(OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("source.path")));
    Database xmlModel = platform.loadModelFromDatabase(excludeFilter);
    Connection con = null;
    try {
      con = platform.borrowConnection();
      log4j.info("   Disabling foreign keys");
      platform.disableAllFK(con, xmlModel, false);
      log4j.info("   Disabling triggers");
      platform.disableAllTriggers(con, xmlModel, false);
      log4j.info("   Disabling check constraints");
      platform.disableCheckConstraints(con, xmlModel, null);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (con != null) {
        platform.returnConnection(con);
      }
    }
  }

  private void enableConstraints(Platform platform) {
    Properties obProp = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    String obDir = obProp.getProperty("source.path");

    Vector<File> dirs = new Vector<File>();
    dirs.add(new File(obDir, "/src-db/database/model/"));
    File modules = new File(obDir, "/modules");

    for (int j = 0; j < modules.listFiles().length; j++) {
      final File dirF = new File(modules.listFiles()[j], "/src-db/database/model/");
      if (dirF.exists()) {
        dirs.add(dirF);
      }
    }
    File[] fileArray = new File[dirs.size()];
    for (int i = 0; i < dirs.size(); i++) {
      fileArray[i] = dirs.get(i);
    }
    Database xmlModel = DatabaseUtils.readDatabase(fileArray);
    platform.deleteAllInvalidConstraintRows(xmlModel, false);
    log4j.info("Enabling constraints...");
    Connection con = null;
    try {
      con = platform.borrowConnection();
      log4j.info("   Enabling check constraints");
      platform.enableCheckConstraints(con, xmlModel, null);
      log4j.info("   Enabling triggers");
      platform.enableAllTriggers(con, xmlModel, false);
      log4j.info("   Enabling foreign keys");
      platform.enableAllFK(con, xmlModel, false);
    } finally {
      if (con != null) {
        platform.returnConnection(con);
      }
    }
  }
}
