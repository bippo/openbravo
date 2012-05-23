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

package org.openbravo.test.xml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.payment.PaymentTermLine;
import org.openbravo.model.financialmgmt.payment.PaymentTermTrl;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test import of data with a business object ({@link PaymentTerm} and {@link PaymentTermLine}),
 * adding and removing childs.
 * 
 * @author mtaal
 */

public class EntityXMLImportTestBusinessObject extends XMLBaseTest {

  // prefix is used to uniquely identify the payment terms used in this test case
  private static final String PREFIX = "" + System.currentTimeMillis();

  private static final Logger log = Logger.getLogger(EntityXMLImportTestBusinessObject.class);

  private static int NO_OF_PT = 1;
  private static int NO_OF_PT_LINE = 1 + NO_OF_PT * NO_OF_PT;
  // will be counted
  private static int TOTAL_PT_PTL = 0;

  // private static int TOTAL_PT_PTL = NO_OF_PT + NO_OF_PT + NO_OF_PT_LINE;

  /** Sets up the test data, creates a first of Payment Terms. */
  public void testAPaymentTerm() {
    cleanRefDataLoaded();
    setTestUserContext();
    addReadWriteAccess(PaymentTermTrl.class);
    createSavePaymentTerm();
  }

  /**
   * Export the Payment Terms from one client and import into another client.
   */
  public void testBPaymentTerm() {

    setTestUserContext();
    setAccess();

    final List<PaymentTerm> pts = getPaymentTerms();
    String xml = getXML(pts);

    log.debug(xml);

    // there is a unique constraint on name
    xml = xml.replaceAll("</name>", "t</name>");

    // export to client 1000001
    setUserContext(QA_TEST_ADMIN_USER_ID);
    // don't be bothered by access checks...
    setAccess();
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    } else if (ir.getErrorMessages() != null) {
      fail(ir.getErrorMessages());
    }

    assertEquals(TOTAL_PT_PTL, ir.getInsertedObjects().size());
    assertEquals(0, ir.getUpdatedObjects().size());
  }

  /**
   * Execute the same test as in {@link #testBPaymentTerm()}, as it is repeated and no data has
   * changed no updates should take place.
   */
  public void testCPaymentTerm() {

    setTestUserContext();
    setAccess();
    final List<PaymentTerm> pts = getPaymentTerms();
    String xml = getXML(pts);

    // there is a unique constraint on name
    xml = xml.replaceAll("</name>", "t</name>");

    // export to client 1000001
    setUserContext(QA_TEST_ADMIN_USER_ID);
    setAccess();
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    }

    assertEquals(0, ir.getInsertedObjects().size());
    assertEquals(0, ir.getUpdatedObjects().size());
  }

  /**
   * Now do the same as in {@link #testCPaymentTerm()} only now with some small changes in the xml,
   * so that some objects are updated.
   */
  public void testDPaymentTerm() {

    setTestUserContext();
    setAccess();

    // make a copy of the paymentterms and their children so that the
    // original db is not updated
    final List<BaseOBObject> pts = DalUtil.copyAll(new ArrayList<BaseOBObject>(getPaymentTerms()),
        false);

    // change some data and export
    final PaymentTerm pt = (PaymentTerm) pts.get(0);
    pt.setName("testtest");
    pt.getFinancialMgmtPaymentTermLineList().get(0).setOverduePaymentDayRule("2");

    String xml = getXML(pts);
    xml = xml.replaceAll("</name>", "t</name>");

    setUserContext(QA_TEST_ADMIN_USER_ID);
    setAccess();
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    }

    assertEquals(0, ir.getInsertedObjects().size());
    assertEquals(2, ir.getUpdatedObjects().size());
    for (final Object o : ir.getUpdatedObjects()) {
      assertTrue(o instanceof PaymentTerm || o instanceof PaymentTermLine);
      if (o instanceof PaymentTermLine) {
        final PaymentTermLine ptl = (PaymentTermLine) o;
        assertTrue(ir.getUpdatedObjects().contains(ptl.getPaymentTerms()));
      }
    }
  }

  /**
   * Test removal of a PaymentTermLine from a PaymentTerm in the xml, then import. After importing
   * the PaymentTermLine should have gone.
   */
  public void testEPaymentTerm() {

    setTestUserContext();
    setAccess();
    // make a copy of the paymentterms and their children so that the
    // original db is not updated
    final List<BaseOBObject> pts = DalUtil.copyAll(new ArrayList<BaseOBObject>(getPaymentTerms()),
        false);

    for (final BaseOBObject bob : pts) {
      final PaymentTerm pt = (PaymentTerm) bob;
      final PaymentTermLine ptl = pt.getFinancialMgmtPaymentTermLineList().get(1);
      pt.getFinancialMgmtPaymentTermLineList().remove(ptl);
    }

    String xml = getXML(pts);
    // there is a unique constraint on name
    xml = xml.replaceAll("</name>", "t</name>");

    setUserContext(QA_TEST_ADMIN_USER_ID);
    // a payment term line is not deletable, but for this test it should be done anyway
    // force this by being admin
    OBContext.setAdminMode();
    try {
      final ImportResult ir = DataImportService.getInstance().importDataFromXML(
          OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
          OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
      if (ir.getException() != null) {
        ir.getException().printStackTrace(System.err);
        fail(ir.getException().getMessage());
      }

      assertEquals(0, ir.getInsertedObjects().size());
      // name of paymentterm has changed
      // overduepaymentrule of paymenttermline is set back to 1
      assertEquals(2, ir.getUpdatedObjects().size());
      for (final Object o : ir.getUpdatedObjects()) {
        assertTrue(o instanceof PaymentTerm || o instanceof PaymentTermLine);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Tests that the previous test {@link #testEPaymentTerm()} did not really remove a line. See this
   * issue: https://issues.openbravo.com/view.php?id=15690
   */
  public void testFPaymentTerm() {
    setUserContext(QA_TEST_ADMIN_USER_ID);
    final List<PaymentTerm> pts = getPaymentTerms();
    for (final PaymentTerm pt : pts) {
      assertEquals(NO_OF_PT_LINE, pt.getFinancialMgmtPaymentTermLineList().size());
    }
  }

  /**
   * Add a PaymentTermLine in the xml and import it, there should be an extra line then.
   */
  public void testGPaymentTerm() {

    setUserContext(QA_TEST_ADMIN_USER_ID);
    setAccess();
    // make a copy of the paymentterms and their children so that the
    // original db is not updated
    final List<BaseOBObject> pts = DalUtil.copyAll(new ArrayList<BaseOBObject>(getPaymentTerms()),
        true);

    // add one at the back
    for (final BaseOBObject bob : pts) {
      final PaymentTerm pt = (PaymentTerm) bob;
      pt.setId("abc");
      if (pt.getFinancialMgmtPaymentTermLineList().isEmpty()) {
        continue;
      }
      final PaymentTermLine ptl0 = pt.getFinancialMgmtPaymentTermLineList().get(0);
      ptl0.setPaymentTerms(pt);
      final PaymentTermLine ptl = (PaymentTermLine) DalUtil.copy(ptl0);
      ptl.setId(null);
      ptl.setClient(null);
      ptl.setOrganization(null);
      ptl.setLineNo((long) NO_OF_PT_LINE);
      pt.getFinancialMgmtPaymentTermLineList().add(ptl);
      ptl.setPaymentTerms(pt);
    }

    String xml = getXML(pts);
    // log.debug(xml);
    // there is a unique constraint on name
    xml = xml.replaceAll("</name>", "t</name>");

    setUserContext(QA_TEST_ADMIN_USER_ID);
    setAccess();
    final ImportResult ir = DataImportService.getInstance().importDataFromXML(
        OBDal.getInstance().get(Client.class, QA_TEST_CLIENT_ID),
        OBDal.getInstance().get(Organization.class, QA_TEST_ORG_ID), xml);
    if (ir.getException() != null) {
      ir.getException().printStackTrace(System.err);
      fail(ir.getException().getMessage());
    }

    // + 1 for the inserted line
    assertEquals(TOTAL_PT_PTL + 1, ir.getInsertedObjects().size());
    for (final Object o : ir.getInsertedObjects()) {
      assertTrue(o instanceof PaymentTermTrl || o instanceof PaymentTerm
          || o instanceof PaymentTermLine);
    }
  }

  /**
   * Tests that {@link #testGPaymentTerm()} was successfull.
   */
  public void testHPaymentTerm() {
    setUserContext(QA_TEST_ADMIN_USER_ID);
    setAccess();
    final List<PaymentTerm> pts = getPaymentTerms();
    for (final PaymentTerm pt : pts) {
      // one pt has 2 lines, one has 1 line
      final int size = pt.getFinancialMgmtPaymentTermLineList().size();
      System.err.println(size);
      assertTrue(size == 2 || size == 3);
    }
  }

  /**
   * Remove the testdata.
   */
  public void testZPaymentTerm() {
    setTestUserContext();
    setAccess();
    final List<PaymentTerm> pts = getPaymentTerms();
    // financialmanagementpaymenttermline is not deletable, but as we are cleaning up
    // force delete by being the admin
    OBContext.setAdminMode();
    try {
      for (final PaymentTerm pt : pts) {
        OBDal.getInstance().remove(pt);
      }
      commitTransaction();

      setUserContext(QA_TEST_ADMIN_USER_ID);
      final List<PaymentTerm> pts2 = getPaymentTerms();
      // financialmanagementpaymenttermline is not deletable, but as we are cleaning up
      // force delete by being the admin
      for (final PaymentTerm pt : pts2) {
        OBDal.getInstance().remove(pt);
      }
      commitTransaction();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void createSavePaymentTerm() {
    setAccess();
    final List<PaymentTerm> result = new ArrayList<PaymentTerm>();
    for (int i = 0; i < NO_OF_PT; i++) {
      final PaymentTerm source = OBDal.getInstance().get(PaymentTerm.class,
          "66BA1164A7394344BB9CD1A6ECEED05D");
      final PaymentTerm pt = (PaymentTerm) DalUtil.copy(source, false);
      pt.setName(PREFIX + " test " + i);
      pt.setOrganization(OBContext.getOBContext().getCurrentOrganization());

      // force new
      // now add a payment termline
      for (int j = 0; j < NO_OF_PT_LINE; j++) {
        final PaymentTermLine ptl = OBProvider.getInstance().get(PaymentTermLine.class);
        ptl.setExcludeTax(true);
        ptl.setLastDayCutoff(new Long(10));
        ptl.setMaturityDate1(new Long(5));
        ptl.setMaturityDate2(new Long(1));
        ptl.setMaturityDate3(new Long(1));
        ptl.setOffsetMonthDue(new Long(j));
        ptl.setLineNo((long) j);
        ptl.setOverduePaymentDayRule("1");
        ptl.setOverduePaymentDaysRule((long) 10);
        ptl.setNextBusinessDay(true);
        ptl.setRest(true);
        ptl.setPaymentTerms(pt);
        ptl.setPercentageDue(new BigDecimal(1));
        pt.getFinancialMgmtPaymentTermLineList().add(ptl);
      }
      result.add(pt);
    }
    for (final PaymentTerm pt : result) {
      OBDal.getInstance().save(pt);
    }
    OBDal.getInstance().commitAndClose();

    // count the children
    int cnt = 0;
    for (PaymentTerm pt : result) {
      PaymentTerm insertedPaymentTerm = OBDal.getInstance().get(PaymentTerm.class, pt.getId());
      cnt++;
      for (Property p : pt.getEntity().getProperties()) {
        if (p.isChild()) {
          cnt += ((Collection<?>) insertedPaymentTerm.get(p.getName())).size();
        }
      }
    }
    TOTAL_PT_PTL = cnt;
  }

  private List<PaymentTerm> getPaymentTerms() {
    final OBCriteria<PaymentTerm> obc = OBDal.getInstance().createCriteria(PaymentTerm.class);
    obc.add(Restrictions.ilike("name", PREFIX + "%"));
    return obc.list();
  }

  // overridden because also children are exported
  @SuppressWarnings("unchecked")
  protected <T extends BaseOBObject> String getXML(List<T> pts) {
    final EntityXMLConverter exc = EntityXMLConverter.newInstance();
    exc.setOptionIncludeReferenced(true);
    exc.setOptionEmbedChildren(true);
    exc.setOptionIncludeChildren(true);
    exc.setAddSystemAttributes(false);
    return exc.toXML((List<BaseOBObject>) pts);
  }

  // set the access so that the test are not bothered by security checks
  // these are not tested here
  private void setAccess() {
    addReadWriteAccess(PaymentTerm.class);
    addReadWriteAccess(PaymentTermLine.class);
  }
}