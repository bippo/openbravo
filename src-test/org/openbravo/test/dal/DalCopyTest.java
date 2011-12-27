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

package org.openbravo.test.dal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.payment.PaymentTermLine;
import org.openbravo.model.financialmgmt.payment.PaymentTermTrl;
import org.openbravo.test.base.BaseTest;

/**
 * Test the {@link DalUtil} class and then specifically the copy methods.
 * 
 * @author mtaal
 */

public class DalCopyTest extends BaseTest {

  /**
   * Test copy of a structure, parent and childs should point to eachother
   */
  public void testHiddenUpdates() {
    setTestUserContext();
    addReadWriteAccess(PaymentTerm.class);
    addReadWriteAccess(PaymentTermTrl.class);
    final List<PaymentTerm> pts = OBDal.getInstance().createCriteria(PaymentTerm.class).list();
    final List<PaymentTermLine> ptls = new ArrayList<PaymentTermLine>();
    for (PaymentTerm pt : pts) {
      ptls.addAll(pt.getFinancialMgmtPaymentTermLineList());
    }
    final List<BaseOBObject> copiedPts = DalUtil.copyAll(new ArrayList<BaseOBObject>(pts));
    for (BaseOBObject bob : copiedPts) {
      final PaymentTerm pt = (PaymentTerm) bob;
      assertFalse(pts.contains(pt));
      for (PaymentTermLine ptl : pt.getFinancialMgmtPaymentTermLineList()) {
        assertSame(pt, ptl.getPaymentTerms());
        assertFalse(ptls.contains(ptl));
      }
      for (PaymentTermTrl ptt : pt.getFinancialMgmtPaymentTermTrlList()) {
        assertSame(pt, ptt.getPaymentTerms());
      }
    }
  }
}