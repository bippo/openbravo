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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.modulescript;

import org.openbravo.database.ConnectionProvider;

public class CreateLineForSequenceProduct extends ModuleScript {

  @Override
  // Filling Line in Sequence Products
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      if (CreateLineForSequenceProductData.existsNull(cp)) {
        CreateLineForSequenceProductData[] data = CreateLineForSequenceProductData.select(cp);
        for (int i = 0; i < data.length; i++) {
          CreateLineForSequenceProductData[] dataProducts = CreateLineForSequenceProductData
              .selectSequenceProducts(cp, data[i].maSequenceId);
          Integer lineNumber = 10;
          for (int j = 0; j < dataProducts.length; j++) {
            CreateLineForSequenceProductData.updateline(cp, lineNumber.toString(),
                dataProducts[j].maSequenceproductId);
            lineNumber = lineNumber + 10;
          }
        }
      }
    } catch (Exception e) {
      handleError(e);
    }

  }

}