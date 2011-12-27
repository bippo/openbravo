/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.test.draft;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.geography.Location;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.test.base.BaseTest;

public class FinancialAccountTest extends BaseTest {

  private static final String BANK = "B";
  private static final String CASH = "C";
  private static final String STANDARD_DESCRIPTION = "JUnit Test";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestUtility.setTestContext();
  }

  public void testAddFinancialAccountValid1() {
    TestUtility.insertFinancialAccount("APRM_FINANCIAL_ACCOUNT_1", STANDARD_DESCRIPTION,
        getOneInstance(Currency.class), CASH, false, getOneInstance(Location.class),
        getOneInstance(BusinessPartner.class), null, null, null, null, null, null, null, null,
        null, BigDecimal.ZERO, BigDecimal.ZERO, null, true, false);
  }

  // Pre-requisite: at least one Matching Algorithm created
  public void testAddFinancialAccountValid2() {
    TestUtility.insertFinancialAccount("APRM_FINANCIAL_ACCOUNT_2", STANDARD_DESCRIPTION,
        getOneInstance(Currency.class), BANK, false, getOneInstance(Location.class),
        getOneInstance(BusinessPartner.class), "2054", "4321", "1", null, null, null, "123456789",
        null, null, BigDecimal.ZERO, BigDecimal.ZERO,
        null/* getOneInstance(MatchingAlgorithm.class) */, true, false);
  }

  public void testAddFinancialAccountValid3() {
    TestUtility.insertFinancialAccount("APRM_FINANCIAL_ACCOUNT_3", STANDARD_DESCRIPTION,
        getOneInstance(Currency.class), BANK, false, getOneInstance(Location.class),
        getOneInstance(BusinessPartner.class), "2054", null, null, null, null, null, null, null,
        null, BigDecimal.ZERO, BigDecimal.ZERO, null, true, false);
  }

  /**
   * Currency is mandatory
   */
  public void testAddFinancialAccountNotValid1() {
    TestUtility.insertFinancialAccount("APRM_FINANCIAL_ACCOUNT_4", STANDARD_DESCRIPTION, null,
        BANK, false, getOneInstance(Location.class), getOneInstance(BusinessPartner.class), "2054",
        null, null, null, null, null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, null,
        false, false);
  }

  /**
   * Deletes all the Payment Methods created for testing
   */
  public void testDeleteFinancialAccounts() {
    final OBCriteria<FIN_FinancialAccount> obCriteria = OBDal.getInstance().createCriteria(
        FIN_FinancialAccount.class);
    obCriteria
        .add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_DESCRIPTION, STANDARD_DESCRIPTION));
    final List<FIN_FinancialAccount> finAccs = obCriteria.list();
    for (FIN_FinancialAccount fa : finAccs) {
      OBDal.getInstance().remove(fa);
    }
  }

}
