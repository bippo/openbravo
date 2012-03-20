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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2010-2012 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.secureApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;

/**
 * Utility class to manage user locking and time delays
 * 
 */
public class UserLock {
  private static Logger log4j = Logger.getLogger(UserLock.class);
  private int delay;
  private int lockAfterTrials;

  private String userName;
  private int numberOfFails;
  private User user;

  public UserLock(String userName) {
    // Read Openbravo.properties for locking configuration. If it's properly configured, it tries
    // to read from the properties file in the source directory not to force to deploy the file to
    // change configuration.
    String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path", null);
    Properties obProp;
    if (sourcePath != null && new File(sourcePath + "/config/Openbravo.properties").exists()) {
      try {
        InputStream obPropFile = new FileInputStream(new File(sourcePath
            + "/config/Openbravo.properties"));
        obProp = new Properties();
        obProp.load(obPropFile);
      } catch (Exception e) {
        log4j.error("Error reading properties", e);
        obProp = OBPropertiesProvider.getInstance().getOpenbravoProperties();
      }
    } else {
      obProp = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    }
    String propInc = obProp.getProperty("login.trial.delay.increment", "0");
    String propMax = obProp.getProperty("login.trial.delay.max", "0");
    String propLock = obProp.getProperty("login.trial.user.lock", "0");
    if (propInc.equals("")) {
      propInc = "0";
    }
    if (propMax.equals("")) {
      propMax = "0";
    }
    if (propLock.equals("")) {
      propLock = "0";
    }
    int delayInc;
    int delayMax;
    try {
      delayInc = Integer.parseInt(propInc);
    } catch (NumberFormatException e) {
      log4j.error("Could not set login.trial.delay.increment property " + propInc, e);
      delayInc = 0;
    }
    try {
      delayMax = Integer.parseInt(propMax);
    } catch (NumberFormatException e) {
      log4j.error("Could not set login.trial.delay.max property " + propMax, e);
      delayMax = 0;
    }
    try {
      lockAfterTrials = Integer.parseInt(propLock);
    } catch (NumberFormatException e) {
      log4j.error("Could not set login.trial.user.lock property" + propMax, e);
      lockAfterTrials = 0;
    }

    this.userName = userName;
    setUser();

    if (delayInc == 0) {
      // No need to check number of fails as login security is not enabled
      delay = 0;
      numberOfFails = 0;
      return;
    }

    // Count how many times this user has failed without success
    StringBuilder hql = new StringBuilder();
    hql.append("select count(*)");
    hql.append("  from ADSession s ");
    hql.append(" where s.loginStatus='F'");
    hql.append("   and s.username = :name");
    hql.append("   and s.creationDate > (select coalesce(max(s1.creationDate), s.creationDate-1)");
    hql.append("                           from ADSession s1");
    hql.append("                          where s1.username = s.username");
    hql.append("                            and s1.loginStatus!='F')");
    Query q = OBDal.getInstance().getSession().createQuery(hql.toString());
    q.setParameter("name", userName);

    numberOfFails = ((Long) q.list().get(0)).intValue();
    if (numberOfFails == 0) {
      delay = 0;
      return;
    }

    delay = delayInc * numberOfFails;
    if (delayMax > 0 && delay > delayMax) {
      delay = delayMax;
    }

  }

  private void setUser() {
    OBContext.setAdminMode();
    try {
      OBCriteria<User> obCriteria = OBDal.getInstance().createCriteria(User.class);
      obCriteria.add(Restrictions.eq(User.PROPERTY_USERNAME, userName));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);

      user = (User) obCriteria.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * A new failed login attempt, increments the count of fails and blocks the user if needed
   */
  public void addFail() {
    numberOfFails++;
    boolean lockUser = (lockAfterTrials != 0) && (numberOfFails > lockAfterTrials);
    log4j.debug("lock: " + lockUser + " -lock after:" + lockAfterTrials + "- fails:"
        + numberOfFails + " - user:" + user);
    if (lockUser) {
      // Try to lock the user in database
      delay = 0;
      if (user != null) {
        try {
          OBContext.setAdminMode();

          user.setLocked(true);
          OBDal.getInstance().flush();
          log4j.warn(userName + " is locked after " + numberOfFails + " failed logins.");
          return;
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    }
  }

  public boolean isLockedUser() {
    return user != null && user.isLocked();
  }

  /**
   * Delays the response of checking in case it is configured in Openbravo.properties
   * (login.trial.delay.increment and login.trial.delay.max), and the current username has login
   * attempts failed.
   */
  public void delayResponse() {
    if (delay > 0) {
      log4j.debug("Delaying response " + delay + " seconds because of the previous login failed.");
      try {
        Thread.sleep(delay * 1000);
      } catch (InterruptedException e) {
        log4j.error("Error delaying login response", e);
      }
    }
  }
}
