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
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class CreateAccountingConfiguration extends ModuleScript {
  private static final Logger log4j = Logger.getLogger(CreateAccountingConfiguration.class);

  @Override
  // Inserting:
  // 1) accounting schema tables for existing tables that are missing
  // 2) Period control for newly added DocBaseTypes
  // 3) Table access for Transactions
  // 4) update Table for Document Types (ARR accounts receivables recept, APP accounts payable
  // payments)
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      createAcctSchemaTables(cp);
      createPeriodControl(cp);
      deleteTableAccess(cp);
      updateTableDocType(cp);
    } catch (Exception e) {
      handleError(e);
    }
  }

  void createAcctSchemaTables(ConnectionProvider cp) throws Exception {
    CreateAccountingConfigurationData[] data = CreateAccountingConfigurationData
        .selectAcctSchema(cp);
    // Tables to be added hardcoded due to problem when executing module script. The tables
    // sometimes do not exist yet
    String tables[] = { "4D8C3B3C31D1410DA046140C9F024D17", "B1B7075C46934F0A9FD4C4D0F1457B42",
        "D1A97202E832470285C9B1EB026D54E2", "D4C23A17190649E7B78F55A05AF3438C" };
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < tables.length; j++) {
        boolean exist = CreateAccountingConfigurationData.selectTables(cp, data[i].cAcctschemaId,
            tables[j]);
        if (!exist) {
          CreateAccountingConfigurationData.insertAcctSchemaTable(cp.getConnection(), cp,
              data[i].cAcctschemaId, tables[j], data[i].adClientId);
        }
      }
    }
  }

  void createPeriodControl(ConnectionProvider cp) throws Exception {
    CreateAccountingConfigurationData.insertPeriodControl(cp.getConnection(), cp);
  }

  void deleteTableAccess(ConnectionProvider cp) throws Exception {
    CreateAccountingConfigurationData.deleteTableAccess(cp.getConnection(), cp);
  }

  void updateTableDocType(ConnectionProvider cp) throws Exception {
    CreateAccountingConfigurationData.updateTableDocType(cp.getConnection(), cp);
  }
}
