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

package org.openbravo.service.json.test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.Type;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalSessionFactoryController;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.bank.BankAccount;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonToDataConverter;
import org.openbravo.test.base.BaseTest;

/**
 * Test the {@link DataToJsonConverter} and the {@link JsonToDataConverter} classes by converting to
 * and from json.
 * 
 * @author mtaal
 */

public class JsonConversionTest extends BaseTest {
  private static final Logger log = Logger.getLogger(JsonConversionTest.class);

  /**
   * Read one BaseOBObject and convert to json and back to the object again.
   */
  public void testJsonConversionOfSingleObject() throws Exception {
    setSystemAdministratorContext();
    final Client client = OBDal.getInstance().get(Client.class, "0");
    final DataToJsonConverter dataJsonConverter = new DataToJsonConverter();
    final JSONObject jsonObject = dataJsonConverter.toJsonObject(client, DataResolvingMode.FULL);
    log.debug(jsonObject.toString());

    // then convert back
    final JsonToDataConverter jsonDataConverter = new JsonToDataConverter();
    final BaseOBObject bob = jsonDataConverter.toBaseOBObject(jsonObject);
    assertSame(client, bob);
  }

  /**
   * Test a json property path consisting of several steps (dots).
   * 
   */
  public void testJsonMultiplePaths() throws Exception {
    setSystemAdministratorContext();
    final User user = OBDal.getInstance().get(User.class, "1000018");
    final BusinessPartner bp = user.getBusinessPartner();
    // nullify it
    bp.setBankAccount(null);

    final DataToJsonConverter dataJsonConverter = new DataToJsonConverter();
    final JSONObject jsonObject = dataJsonConverter.toJsonObject(user, DataResolvingMode.FULL);
    System.err.println(jsonObject.toString());
    log.debug(jsonObject.toString());

    jsonObject.put("businessPartner.bankAccount.iBAN", "test");
    jsonObject.put("businessPartner.bankAccount.id", "1000000");

    // then convert back
    final JsonToDataConverter jsonDataConverter = new JsonToDataConverter();
    final BaseOBObject bob = jsonDataConverter.toBaseOBObject(jsonObject);
    assertEquals(0, jsonDataConverter.getErrors().size());
    final BankAccount ba = OBDal.getInstance().get(BankAccount.class, "1000000");
    assertSame(user.getBusinessPartner(), bp);
    assertNotNull(bp.getBankAccount());
    assertSame(bp.getBankAccount(), ba);
    assertEquals("test", ba.getIBAN());
    assertSame(user, bob);

    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Read list of ADColumn objects and translate to json and back.
   */
  public void testJsonConversionListOfObjects() throws Exception {
    setSystemAdministratorContext();
    final List<BaseOBObject> columns = OBDal.getInstance()
        .createQuery(Column.ENTITY_NAME, " table.id='100'").list();
    final DataToJsonConverter converter = new DataToJsonConverter();
    final List<JSONObject> jsonObjects = converter.toJsonObjects(columns);
    log.debug(jsonObjects.toString());
    // then convert back
    final JsonToDataConverter jsonDataConverter = new JsonToDataConverter();
    final List<BaseOBObject> list = jsonDataConverter.toBaseOBObjects(jsonObjects);
    assertFalse(jsonDataConverter.hasErrors());
    assertEquals(columns.size(), list.size());
    int i = 0;
    for (BaseOBObject bob : list) {
      assertSame(bob, columns.get(i++));
    }
  }

  /**
   * Read all objects in the database and translate to json and back. Then check that no updates
   * take place.
   */
  public void testJsonConversionAllObjects() throws Exception {
    setSystemAdministratorContext();
    final SessionFactoryController currentSFC = SessionFactoryController.getInstance();
    try {
      final SessionFactoryController newSFC = new LocalSessionFactoryController();
      SessionFactoryController.setInstance(newSFC);
      SessionFactoryController.getInstance().reInitialize();
      SessionHandler.getInstance().commitAndClose();
      for (Entity entity : ModelProvider.getInstance().getModel()) {
        log.debug("Testing json conversion of " + entity.getName());
        List<BaseOBObject> bobs = OBDal.getInstance().createQuery(entity.getName(), "").list();
        // limit the size to prevent memory issues
        if (bobs.size() > 5000) {
          bobs = bobs.subList(0, 5000);
        }
        final DataToJsonConverter converter = new DataToJsonConverter();
        final List<JSONObject> jsonObjects = converter.toJsonObjects(bobs);
        log.debug(jsonObjects.toString());
        // then convert back
        final JsonToDataConverter jsonDataConverter = new JsonToDataConverter();
        final List<BaseOBObject> list = jsonDataConverter.toBaseOBObjects(jsonObjects);
        assertFalse("Entity " + entity.getName() + " has errors when converting to/from json",
            jsonDataConverter.hasErrors());
        assertEquals(bobs.size(), list.size());
        int i = 0;
        for (BaseOBObject bob : list) {
          assertSame(bob, bobs.get(i++));
        }
        SessionHandler.getInstance().getSession().clear();
      }
    } finally {
      SessionFactoryController.setInstance(currentSFC);
    }
  }

  // test issue:
  // https://issues.openbravo.com/view.php?id=14697
  public void testIssue14697CheckDerivedReadableCurrency() {
    setUserContext(getRandomUser().getId());
    final BaseOBObject c = OBDal.getInstance().get(Currency.class, "100");
    final DataToJsonConverter converter = new DataToJsonConverter();

    List<JSONObject> jsonObjects = converter.toJsonObjects(Collections.singletonList(c));
    assertTrue(jsonObjects.size() == 1);
    final String jsonString = jsonObjects.get(0).toString();
    // System.err.println(jsonString);

    // check on all derived readable properties
    boolean checkDone = false;
    for (Property property : c.getEntity().getProperties()) {
      if (!property.allowDerivedRead()) {
        assertTrue(jsonString.indexOf(property.getName()) == -1);
        checkDone = true;
      }
    }
    assertTrue(checkDone);
  }

  private class LocalSessionFactoryController extends DalSessionFactoryController {
    @Override
    protected void setInterceptor(Configuration configuration) {
      configuration.setInterceptor(new LocalInterceptor());
    }
  }

  private class LocalInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      return false;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      fail();
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
        Object[] previousState, String[] propertyNames, Type[] types) {
      fail();
      return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      fail();
      return false;
    }

    @Override
    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
      fail();
    }

    @Override
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
      fail();
    }

    @Override
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
      fail();
    }
  }

}