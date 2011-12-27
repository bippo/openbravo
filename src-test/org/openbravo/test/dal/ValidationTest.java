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

package org.openbravo.test.dal;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.DynamicOBObject;
import org.openbravo.base.util.CheckException;
import org.openbravo.base.validation.NumericPropertyValidator;
import org.openbravo.base.validation.StringPropertyValidator;
import org.openbravo.base.validation.ValidationException;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.alert.AlertRule;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.AcctSchemaTableDocType;
import org.openbravo.model.common.invoice.InvoiceSchedule;
import org.openbravo.model.procurement.Requisition;
import org.openbravo.test.base.BaseTest;

/**
 * Test property validations and validators.
 * 
 * @see Property#getValidator()
 * @see Property#checkIsValidValue(Object)
 * @see NumericPropertyValidator
 * @see StringPropertyValidator
 * @author mtaal
 */

public class ValidationTest extends BaseTest {

  /**
   * Tests the type checking of primitive {@link Property} values.
   */
  public void testTypeChecking() {
    setTestAdminContext();
    final OBCriteria<Currency> obc = OBDal.getInstance().createCriteria(Currency.class);
    obc.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, "USD"));
    final List<Currency> cs = obc.list();
    final Currency c = cs.get(0);

    // now set difference values and get exceptions on each
    setValue(c, Currency.PROPERTY_UPDATEDBY, "test", "only allows reference instances of type");
    setValue(c, Currency.PROPERTY_CLIENT, "test", "only allows reference instances of type");
    setValue(c, Currency.PROPERTY_PRICEPRECISION, new Double(400.0), "only allows instances of");
    setValue(c, Currency.PROPERTY_CREATIONDATE, new BigDecimal(100.0), "only allows instances of");
    try {
      setValue(c, "asdads", null, "does not exist for entity");
      fail("Property does not exist is not checked");
    } catch (final CheckException e) {
      // correct!
    }
  }

  /**
   * Performs type checking of property values on a {@link DynamicOBObject}.
   */
  public void testTypeCheckDynamicObject() {
    final DynamicOBObject bpGroup = new DynamicOBObject();
    bpGroup.setEntityName(Category.ENTITY_NAME);
    setValue(bpGroup, Category.PROPERTY_CLIENT, "test", "only allows reference instances of type");
    setValue(bpGroup, Category.PROPERTY_DESCRIPTION, new Double(400.0), "only allows instances of");
    setValue(bpGroup, Category.PROPERTY_DEFAULT, new BigDecimal(100.0), "only allows instances of");
  }

  private void setValue(BaseOBObject bob, String propName, Object value, String expectedMessage) {
    try {
      bob.set(propName, value);
      if (value == null) {
        fail("Validation not performed on " + bob.getEntityName() + "." + propName
            + " for null value");
      } else {
        fail("Validation not performed on " + bob.getEntityName() + "." + propName
            + " for value of type " + value.getClass().getName());
      }
    } catch (final ValidationException e) {
      assertTrue("Unexpected exception " + e.getMessage(),
          e.getMessage().indexOf(expectedMessage) != -1);
    }
  }

  /**
   * Tests the validation of a list value (an enumeration).
   */
  public void testListValue() {
    setTestAdminContext();
    final OBCriteria<AlertRule> obc = OBDal.getInstance().createCriteria(AlertRule.class);
    for (final AlertRule ar : obc.list()) {
      try {
        ar.setType("A");
        fail("List value check not performed");
      } catch (final ValidationException ve) {
        assertTrue("Illegal exception " + ve.getMessage(),
            ve.getMessage().contains("it should be one of the following values"));
        break;
        // success
      }
    }
  }

  /**
   * Tests the field length validation on a String.
   */
  public void testFieldLength() {
    setSystemAdministratorContext();

    final StringBuffer sb = new StringBuffer();
    final String key = "0123456789";
    for (int i = 0; i < 10; i++) {
      sb.append(key);
    }
    final OBCriteria<Requisition> obc = OBDal.getInstance().createCriteria(Requisition.class);

    for (final Requisition r : obc.list()) {
      try {
        r.setDescription(sb.toString());
        OBDal.getInstance().save(r);
        SessionHandler.getInstance().commitAndClose();
        fail("Minvalue constraint not enforced");
      } catch (final ValidationException ve) {
        // success
      }
    }
  }

  /**
   * Test the validation of a max value of a numeric property.
   * 
   * @see Property#getMaxValue()
   */
  public void testMaxValue() {
    setTestUserContext();
    addReadWriteAccess(InvoiceSchedule.class);
    final OBCriteria<InvoiceSchedule> obc = OBDal.getInstance().createCriteria(
        InvoiceSchedule.class);
    for (final InvoiceSchedule is : obc.list()) {
      try {
        is.setInvoiceCutOffDay((long) 40);
        fail("Maxvalue constraint not enforced");
      } catch (final ValidationException ve) {
        // success
        break;
      }
    }
  }

  /**
   * Test the validation of a min value of a numeric property.
   * 
   * @see Property#getMinValue()
   */
  public void testMinValue() {
    setTestUserContext();
    addReadWriteAccess(InvoiceSchedule.class);
    final OBCriteria<InvoiceSchedule> obc = OBDal.getInstance().createCriteria(
        InvoiceSchedule.class);
    for (final InvoiceSchedule is : obc.list()) {
      try {
        is.setInvoiceCutOffDay((long) 0);
        fail();
      } catch (final ValidationException ve) {
        // success
        break;
      }
    }
  }

  /**
   * Tests Mantis issue: 8624: Validation should not check field length of id/foreign key columns
   */
  public void testMantis8624() {
    final String id = "012345678901234567890123456789";
    final AcctSchemaTableDocType oi = OBProvider.getInstance().get(AcctSchemaTableDocType.class);
    // this failed before:
    oi.setId(id);
  }
}