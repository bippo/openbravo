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

package org.openbravo.test.base;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalLayerInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.model.ad.access.User;

/**
 * Base test class which can/should be extended by most other test classes which want to make use of
 * the Openbravo test infrastructure.
 * 
 * @author mtaal
 * @author iperdomo
 */

public class BaseTest extends TestCase {

  private static final Logger log = Logger.getLogger(BaseTest.class);

  private boolean errorOccured = false;

  /**
   * Record ID of Client "F&B International Group"
   */
  protected static final String TEST_CLIENT_ID = "23C59575B9CF467C9620760EB255B389";

  /**
   * Record ID of Organization "F&B España - Región Norte"
   */
  protected static final String TEST_ORG_ID = "E443A31992CB4635AFCAEABE7183CE85";

  /**
   * Record ID of Organization "F&B US West Coast"
   */
  protected static final String TEST_US_ORG_ID = "BAE22373FEBE4CCCA24517E23F0C8A48";

  /**
   * Record ID of Warehouse "España Región Norte"
   */
  protected static final String TEST_WAREHOUSE_ID = "B2D40D8A5D644DD89E329DC297309055";

  /**
   * Record ID of User "F&BAdmin"
   */
  protected static final String TEST_USER_ID = "A530AAE22C864702B7E1C22D58E7B17B";

  /**
   * Record ID of User "F&BESRNUser" - Any user with less privileges than {@link #TEST_USER_ID}
   */
  protected static final String TEST2_USER_ID = "75449AFBAE7F46029F26C85C4CCF714B";

  /**
   * Record IDs of available users different than {@link #TEST_USER_ID} Note: Initialized to null,
   * need to call {@link #getRandomUser} at least once
   */
  protected static List<User> userIds = null;

  /**
   * Record ID of Role "F&B International Group Admin"
   */
  protected static final String TEST_ROLE_ID = "42D0EEB1C66F497A90DD526DC597E6F0";

  /**
   * Record ID of a Order in Draft status
   */
  protected static final String TEST_ORDER_ID = "F8492493E92C4EE5B5251AC4574778B7";

  /**
   * Record ID of Product "Zumo de Fresa Bio 0,33L"
   */
  protected static final String TEST_PRODUCT_ID = "61047A6B06B3452B85260C7BCF08E78D";

  /**
   * Map representation of current Organization tree for Client {@link #TEST_CLIENT_ID}
   */
  protected static Map<String, String[]> TEST_ORG_TREE = new HashMap<String, String[]>();

  static {

    // "F&B International Group"
    TEST_ORG_TREE.put("19404EAD144C49A0AF37D54377CF452D", new String[] { "" });

    // "F&B España, S.A."
    TEST_ORG_TREE.put("B843C30461EA4501935CB1D125C9C25A", new String[] { "" });

    // "F&B US, Inc."
    TEST_ORG_TREE.put("B843C30461EA4501935CB1D125C9C25A", new String[] { "" });

  }

  /**
   * Record ID of the QA Test client
   */
  protected static final String QA_TEST_CLIENT_ID = "4028E6C72959682B01295A070852010D";

  /**
   * Record ID of the Main organization of QA Test client
   */
  protected static final String QA_TEST_ORG_ID = "43D590B4814049C6B85C6545E8264E37";

  /**
   * Record ID of the "Admin" user of QA Test client
   */
  protected static final String QA_TEST_ADMIN_USER_ID = "4028E6C72959682B01295A0735CB0120";

  /**
   * Record ID of the "Customer" Business Partner Category
   */
  protected static final String TEST_BP_CATEGORY_ID = "4028E6C72959682B01295F40C38C02EB";

  /**
   * Record ID of the geographical location "c\ de la Costa 54, San Sebastián 12784"
   */
  protected static final String TEST_LOCATION_ID = "A21EF1AB822149BEB65D055CD91F261B";

  /**
   * Overridden to initialize the Dal layer, sets the current user to the the User:
   * {@link #TEST_USER_ID}
   */
  @Override
  protected void setUp() throws Exception {

    if (this.getClass().getResource("/log4j.lcf") != null) {
      PropertyConfigurator.configure(this.getClass().getResource("/log4j.lcf"));
    }

    initializeDalLayer();
    // clear the session otherwise it keeps the old model
    setTestUserContext();
    super.setUp();
    // be negative is set back to false at the end of a successfull test.
    errorOccured = true;
  }

  /**
   * Initializes the DALLayer, can be overridden to add specific initialization behavior.
   * 
   * @throws Exception
   */
  protected void initializeDalLayer() throws Exception {
    if (!DalLayerInitializer.getInstance().isInitialized()) {
      DalLayerInitializer.getInstance().initialize(true);
    }
  }

  protected ConnectionProvider getConnectionProvider() {
    try {
      final String propFile = OBConfigFileProvider.getInstance().getFileLocation();
      final ConnectionProvider conn = new ConnectionProviderImpl(propFile + "/Openbravo.properties");
      return conn;
    } catch (PoolNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Reads the configuration properties from the property files.
   * 
   * @deprecated Behavior has been implemented in the {@link OBPropertiesProvider}. Is now done
   *             automatically when initializing the DAL layer.
   */
  protected void setConfigPropertyFiles() {
  }

  /**
   * Set the current user to the 0 user.
   */
  protected void setSystemAdministratorContext() {
    OBContext.setOBContext("0");
  }

  @Deprecated
  protected void setBigBazaarAdminContext() {
    setTestAdminContext();
  }

  @Deprecated
  protected void setBigBazaarUserContext() {
    setTestUserContext();
  }

  /**
   * Sets the current user to the {@link #TEST_USER_ID} user.
   */
  protected void setTestUserContext() {
    OBContext.setOBContext(TEST_USER_ID, TEST_ROLE_ID, TEST_CLIENT_ID, TEST_ORG_ID);
  }

  /**
   * Sets the current user to the 100 user.
   */
  protected void setTestAdminContext() {
    OBContext.setOBContext("100", "0", TEST_CLIENT_ID, TEST_ORG_ID);
  }

  /**
   * Sets the current user. For the 0, 100 and 1000000 users this method should not be used. For
   * these users one of the other context-set methods should be used: {@link #setTestAdminContext()}
   * , {@link #setTestUserContext()} or {@link #setSystemAdministratorContext()}.
   * 
   * @param userId
   *          the id of the user to use.
   */
  protected void setUserContext(String userId) {
    if (userId.equals("0")) {
      log.warn("Forwarding the call to setSystemAdministratorContext, "
          + "consider using that method directly");
      setSystemAdministratorContext();
    } else if (userId.equals("100")) {
      log.warn("Forwarding the call to setFBGroupAdminContext method, "
          + "consider using that method directly");
      setTestAdminContext();
    } else if (userId.equals("1000000")) {
      log.warn("User id 1000000 is not longer available, please update your test. "
          + "Forwarding call to the setTestUserContext method, "
          + "consider using that method directly");
      setTestUserContext();
    } else {
      OBContext.setOBContext(userId);
    }
  }

  /**
   * Gets a random User (Record ID) from the available ones in the test client. The ID is one
   * different than {@link #TEST_USER_ID}
   * 
   * @return A record ID of a available user
   */
  protected User getRandomUser() {
    if (userIds == null) {
      setTestUserContext();

      String[] excludedUserIds = { "100", TEST_USER_ID };
      OBCriteria<User> obc = OBDal.getInstance().createCriteria(User.class);
      obc.add(Restrictions.not(Restrictions.in(User.PROPERTY_ID, excludedUserIds)));
      obc.add(Restrictions.isNotEmpty(User.PROPERTY_ADUSERROLESLIST));

      if (obc.count() == 0) {
        throw new RuntimeException("Unable to initialize the list of available users");
      }
      userIds = new ArrayList<User>();
      for (User u : obc.list()) {
        userIds.add(u);
      }
    }

    Random r = new Random();
    return userIds.get(r.nextInt(userIds.size()));
  }

  @Override
  public TestResult run() {
    // TODO Auto-generated method stub
    return super.run();
  }

  /**
   * Overridden to keep track if an exception was thrown, if not then errorOccurred is set to false,
   * signaling to tearDown to commit the transaction.
   */
  @Override
  public void runTest() throws Throwable {
    super.runTest();
    errorOccured = false;
  }

  /**
   * Performs rolling back of a transaction (in case setTestCompleted was not called by the
   * subclass), or commits the transaction if the testcase passed without exception.
   */
  @Override
  protected void tearDown() throws Exception {
    // if not an administrator but still admin mode set throw an exception
    if (!OBContext.getOBContext().getUser().getId().equals("0")
        && !OBContext.getOBContext().getRole().getId().equals("0")
        && OBContext.getOBContext().isInAdministratorMode()) {
      throw new IllegalStateException(
          "Test case should take care of reseting admin mode correctly in a finally block, use OBContext.restorePreviousMode");
    }
    try {
      if (SessionHandler.isSessionHandlerPresent()) {
        if (SessionHandler.getInstance().getDoRollback()) {
          SessionHandler.getInstance().rollback();
        } else if (isErrorOccured()) {
          SessionHandler.getInstance().rollback();
        } else if (SessionHandler.getInstance().getSession().getTransaction().isActive()) {
          SessionHandler.getInstance().commitAndClose();
        } else {
          SessionHandler.getInstance().getSession().close();
        }
      }
    } catch (final Exception e) {
      SessionHandler.getInstance().rollback();
      reportException(e);
      throw e;
    } finally {
      SessionHandler.deleteSessionHandler();
      OBContext.setOBContext((OBContext) null);
    }
    super.tearDown();
  }

  /**
   * Prints the stacktrace of the exception to System.err. Handles the case that the exception is a
   * SQLException which has the real causing exception in the
   * {@link SQLException#getNextException()} method.
   * 
   * @param e
   *          the exception to report.
   */
  protected void reportException(Exception e) {
    if (e == null)
      return;
    e.printStackTrace(System.err);
    if (e instanceof SQLException) {
      reportException(((SQLException) e).getNextException());
    }
  }

  public boolean isErrorOccured() {
    return errorOccured;
  }

  /**
   * Does a rollback of the transaction;
   */
  public void rollback() {
    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Commits the transaction to the database.
   */
  public void commitTransaction() {
    OBDal.getInstance().commitAndClose();
  }

  /**
   * Deprecated, no need to call this method explicitly anymore. The BaseTest class overrides the
   * runTest method which sets the internal flag, overriding any value passed in this method.
   * 
   * @param errorOccured
   * @deprecated
   */
  public void setErrorOccured(boolean errorOccured) {
    this.errorOccured = errorOccured;
  }

  /**
   * Convenience method, gets an instance for the passed Class from the database. If there are no
   * records for that class then an exception is thrown. If there is more than one result then an
   * arbitrary instance is returned (the first one in the un-ordered resultset).
   * 
   * @param <T>
   *          the specific class to query for.
   * @param clz
   *          instances
   * @return an instance of clz.
   */
  protected <T extends BaseOBObject> T getOneInstance(Class<T> clz) {
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    if (obc.list().size() == 0) {
      throw new OBException("There are zero instances for class " + clz.getName());
    }
    return obc.list().get(0);
  }

  /**
   * Extends the read and write access of the current user to also include the passed class. This
   * can be used to circumvent restrictive access which is not usefull for the test itself.
   * 
   * @param clz
   *          after this call the current user (in the {@link OBContext}) will have read/write
   *          access to this class.
   */
  protected void addReadWriteAccess(Class<?> clz) {
    final Entity entity = ModelProvider.getInstance().getEntity(clz);
    if (!OBContext.getOBContext().getEntityAccessChecker().getWritableEntities().contains(entity)) {
      OBContext.getOBContext().getEntityAccessChecker().getWritableEntities().add(entity);
    }
    if (!OBContext.getOBContext().getEntityAccessChecker().getReadableEntities().contains(entity)) {
      OBContext.getOBContext().getEntityAccessChecker().getReadableEntities().add(entity);
    }
  }

  /**
   * Counts the total occurences in the database for the passed class. Note that active, client and
   * organization filtering applies.
   * 
   * @param <T>
   *          a class type parameter
   * @param clz
   *          the class to count occurences for
   * @return the number of occurences which are active and belong to the current client/organization
   */
  protected <T extends BaseOBObject> int count(Class<T> clz) {
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    return obc.count();
  }
}
