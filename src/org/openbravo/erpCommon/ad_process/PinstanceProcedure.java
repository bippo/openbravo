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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import org.apache.log4j.Logger;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessContext;

/**
 * A pInstance procedure process.
 * 
 * @author awolski
 * 
 */
public class PinstanceProcedure extends ProcedureProcess {

  static Logger log = Logger.getLogger(PinstanceProcedure.class);

  private String pinstanceId;

  /**
   * Initilize the sql and parameters for the procedure.
   */
  @Override
  protected void init(final ProcessBundle bundle) {
    String sql = "CALL " + bundle.getImpl() + "(?)";
    pinstanceId = bundle.getPinstanceId();

    setSQL(sql);
    setParams(new String[] { pinstanceId }, new String[] { "in" });
  }

  @Override
  protected void log(String message, ProcessContext context) {
    OBError msg;
    try {
      final PInstanceProcessData[] data = PInstanceProcessData.select(connection, pinstanceId);
      msg = Utility.getProcessInstanceMessage(connection, context.toVars(), data);
      logger.log(msg.getType() + " " + msg.getTitle() + " " + msg.getMessage());

    } catch (final Exception e) {
      e.printStackTrace();
      msg = Utility.translateError(connection, context.toVars(), context.getLanguage(),
          e.getMessage());
      logger.log(msg.getType() + " " + msg.getTitle() + " " + msg.getMessage());
    }
  }

}
