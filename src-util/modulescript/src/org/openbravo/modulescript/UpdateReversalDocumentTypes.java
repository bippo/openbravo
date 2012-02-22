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

public class UpdateReversalDocumentTypes extends ModuleScript {

  @Override
  public void execute() {

    // Untill now a document type is identified as reversal if its document base type is 'ARC' or
    // 'APC'
    // New column has been added to allow a generic way of marking document as reversal
    // This script updates this column to 'Y' for existing document types
    // that belong to 'ARC' and 'APC'

    try {
      ConnectionProvider cp = getConnectionProvider();
      UpdateReversalDocumentTypesData.update(cp);
      UpdateReversalDocumentTypesData.updateIssue19541(cp);

    } catch (Exception e) {
      handleError(e);
    }
  }

}
