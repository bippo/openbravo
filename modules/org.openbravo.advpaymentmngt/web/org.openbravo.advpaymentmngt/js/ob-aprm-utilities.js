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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.APRM = {};
OB.APRM.bankTransitoryAccountCalloutResponse = function(me, confirmMessage, financialAccountId) {
  isc.confirm(confirmMessage, function(value){
    var post;
    if (value) {
      var bankTransitoryAccount = me.getField('fINTransitoryAcct')._value,
          bankTransitoryAccountDesc = me.getField('fINTransitoryAcct').valueMap[bankTransitoryAccount];

      me.getField('clearedPaymentAccount').valueMap[bankTransitoryAccount] = bankTransitoryAccountDesc;
      me.getField('clearedPaymentAccount').setValue(bankTransitoryAccount);
      me.getField('clearedPaymentAccountOUT').valueMap[bankTransitoryAccount] = bankTransitoryAccountDesc;
      me.getField('clearedPaymentAccountOUT').setValue(bankTransitoryAccount);
      
      post = {'eventType': 'bankTransitoryCalloutResponse',
              'financialAccountId': financialAccountId};

      OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.APRMActionHandler', post, {}, {});

    }
  });
};
