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

package org.openbravo.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

/**
 * 
 * @author dbaz
 */
public class UpgradeToPocEmail extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(UpdateEmailPasswords.class);

  @Override
  public void execute() {

    // Updating email passwords required to fix issue 13688
    // Nomenclature: "old implementation": email implementation done in the "AD_CLIENT" table
    // ("Client" window)
    // Nomenclature: "new implementation": email implementation done in the "C_POC_CONFIGURATION"
    // table ("Email Configuration" tab child of "Client" window)
    // This script moves the configuration of the old implementation (if it is valid) to the new
    // implementation (if there is not a different configuration already there)

    try {
      ConnectionProvider cp = getConnectionProvider();

      UpgradeToPocEmailData[] oldConfigurationData = UpgradeToPocEmailData.oldConfigurationData(cp);
      if (oldConfigurationData.length == 0) {
        return;
      }
      UpgradeToPocEmailData[] newConfigurationData = UpgradeToPocEmailData.newConfigurationData(cp);

      boolean isFirstExecution = true; // For log4j purposes
      boolean hasToCreateNew = true;
      boolean hasToDeleteOld = true;

      for (UpgradeToPocEmailData oldConfiguration : oldConfigurationData) {
        // Try the upgrade process only if there is an email configuration in the old implementation
        // (if not we are done, there is no functional email configuration running in the old
        // implementation so nothing to upgrade)
        if (oldConfiguration.server != null && !"".equals(oldConfiguration.server)) {
          if (isFirstExecution) { // For log4j purposes
            isFirstExecution = false;
            log4j
                .debug("Migrating old implementation email configurations to the new implementation");
          }
          hasToCreateNew = true;
          hasToDeleteOld = true;
          for (UpgradeToPocEmailData newConfiguration : newConfigurationData) {
            // If there is already a configuration introduced in the new implementation, don't
            // create a new one in the new implementation
            if (oldConfiguration.adClientId.equals(newConfiguration.adClientId)) {
              hasToCreateNew = false;
              // ...and if the existing configuration of the new implementation is the same than the
              // old implementation one and is active, delete the configuration of the old
              // implementation
              if (!hasToCreateNew && "Y".equals(newConfiguration.isactive)
                  && oldConfiguration.server.equals(newConfiguration.server)
                  && oldConfiguration.senderaddress.equals(newConfiguration.senderaddress)
                  && oldConfiguration.accountname.equals(newConfiguration.accountname)) {
                hasToDeleteOld = true;
                // ...and ensure/force this existing configuration of the new implementation has
                // "AD_ORG_ID" = "0" to avoid problems with organizations without access to the
                // existing matched one.
                UpgradeToPocEmailData.changeNewConfigurationDataOrg(cp,
                    newConfiguration.cPocConfigurationId);
                break; // If we already know that the existing old implementation and new
                       // implementation configuration match, no need to continue iterating.
              } else {
                hasToDeleteOld = false;
              }
            }
          }
          if (hasToCreateNew) {
            UpgradeToPocEmailData.insertNewConfigurationData(
                cp.getConnection(),
                cp,
                oldConfiguration.adClientId,
                oldConfiguration.server,
                (oldConfiguration.senderaddress != null
                    && !"".equals(oldConfiguration.senderaddress) ? oldConfiguration.senderaddress
                    : "my@email.com"), oldConfiguration.auth, oldConfiguration.accountname,
                oldConfiguration.accountpass);
            log4j.debug("Server: \"" + oldConfiguration.server + "\" of adClientId: \""
                + oldConfiguration.adClientId + "\" has been ported succesfully");
          } else {
            log4j.debug("Server: \"" + oldConfiguration.server + "\" of adClientId: \""
                + oldConfiguration.adClientId + "\" has not been ported because "
                + (hasToDeleteOld ? "the same" : "a different")
                + " email configuration already exists in the new implementation");
          }
          if (hasToDeleteOld) {
            UpgradeToPocEmailData.deleteOldConfigurationData(cp, oldConfiguration.adClientId);
            log4j.debug("Deleted old implementation configuration of server: \""
                + oldConfiguration.server + "\" of adClientId: \"" + oldConfiguration.adClientId
                + "\" because the server "
                + (hasToCreateNew ? "already has been ported to" : "already exists in")
                + " the new implementation");
          }
        }
      }

    } catch (Exception e) {
      handleError(e);
    }
  }
}
