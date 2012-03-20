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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.event;

import org.hibernate.Transaction;
import org.openbravo.dal.core.OBInterceptor;

/**
 * The event object send out when a transaction has been completed.
 * 
 * To receive this event, create a class with a method which has this signature:
 * 
 * public void onTransactionComplete(@Observes TransactionCompletedEvent event) {
 * 
 * Note, the method name is unimportant, the @Observes EntityNewEvent specifies that this method
 * will be called before persisting a new instance.
 * 
 * @see OBInterceptor#afterTransactionCompletion(Transaction)
 * 
 * @author mtaal
 */
public class TransactionCompletedEvent {
  private Transaction transaction;

  public Transaction getTransaction() {
    return transaction;
  }

  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
  }

}
