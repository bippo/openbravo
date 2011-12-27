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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.modulescript;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;

public class CreateDoctypeTemplate extends ModuleScript {

  @Override
  // Inserting Doctype template for shipment and receipt.Related to the issue
  // https://issues.openbravo.com/view.php?id=11996
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      CreateDoctypeTemplateData[] data = CreateDoctypeTemplateData.select(cp);
      for (int i = 0; i < data.length; i++) {

        if (data[i].vCount.equals("0")) {
          String strReportFileName = "Goods Shipment-@our_ref@";
          if (data[i].docbasetype.equals("MMR")) {
            strReportFileName = "Goods Receipt-@our_ref@";
          }
          String strDoctypeTemplate_id = UUID.randomUUID().toString().replace("-", "")
              .toUpperCase();
          CreateDoctypeTemplateData.insertDoctypeTemplate(cp.getConnection(), cp,
              strDoctypeTemplate_id, data[i].adClientId, data[i].cDoctypeId, data[i].name,
              "@basedesign@/org/openbravo/erpReports", strReportFileName, "RptM_InOut.jrxml");
          CreateDoctypeTemplateData.insertEmailDefinition(cp.getConnection(), cp,
              data[i].adClientId, strDoctypeTemplate_id);
        }
      }
    } catch (Exception e) {
      handleError(e);
    }

  }

}