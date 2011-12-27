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
package org.openbravo.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.database.ConnectionProvider;

/**
 * Validates there are not more than one preference for the same attribute in different
 * organizations with different values. This can cause a different behavior than in <mp16 releases.
 */
public class PreferenceConflict extends BuildValidation {
  private static Logger log4j = Logger.getLogger(PreferenceConflict.class);

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      // checking whether upgrading from <mp16
      if (!PreferenceConflictData.alreadymp16(cp)) {
        PreferenceConflictData[] conflictOrgs = PreferenceConflictData
            .differentOrgDifferentValues(cp);
        for (PreferenceConflictData conflictOrg : conflictOrgs) {
          log4j
              .warn(conflictOrg.attribute
                  + " Preference has different values in different organizations. This can cause a different behavior than in previous release.");
        }
      }

    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }
}
