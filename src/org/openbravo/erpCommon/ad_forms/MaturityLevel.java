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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.HttpsUtils;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.services.webservice.WebService3Impl;
import org.openbravo.services.webservice.WebService3ImplServiceLocator;

/**
 * Maintains a list of possible maturity levels for a module. This list is maintained in central
 * repository.
 * 
 */
public class MaturityLevel {
  private final Logger log4j = Logger.getLogger(MaturityLevel.class);
  private String[][] levels;
  public final static int CS_MATURITY = 500;
  public final static int QA_APPR_MATURITY = 200;
  private boolean error = false;

  /**
   * Calls central repository webservice to obtain the list of possible statuses. In case the
   * service is not available or the request fails, the list is initiallized with 500-Production.
   */
  public MaturityLevel() {
    // Check internet availability and set proxy, to obtain maturity levels
    error = !HttpsUtils.isInternetAvailable();
    log4j.error("Couldn't connect to Internet to obtain maturity levels");
    try {
      // retrieve the module details from the webservice
      final WebService3ImplServiceLocator loc = new WebService3ImplServiceLocator();
      final WebService3Impl ws = loc.getWebService3();
      levels = ws.getMaturityLevels();
    } catch (final Exception e) {
      log4j.error("Error obtaining maturity levels", e);
      error = true;
    }

    if (error) {
      // could not obtain actual levels, setting Confirmed Stable only
      log4j.warn("Setting default Confirmed Stable level");
      levels = new String[1][2];
      if (ActivationKey.getInstance().isActive()) {
        levels[0][0] = Integer.toString(MaturityLevel.CS_MATURITY);
        levels[0][1] = "Confirmed Stable";
      } else {
        levels[0][0] = Integer.toString(MaturityLevel.QA_APPR_MATURITY);
        levels[0][1] = "QA Approved";
      }
    }
  }

  /**
   * Obtains the FieldProvider[][] to populate the statuses drop down list.
   */
  public FieldProvider[] getCombo() {
    FieldProvider rt[] = new FieldProvider[levels.length];
    int i = 0;
    for (String[] level : levels) {
      SQLReturnObject l = new SQLReturnObject();
      l.setData("ID", level[0]);
      l.setData("NAME", level[1]);
      rt[i] = l;
      i++;
    }
    return rt;
  }

  /**
   * Returns the name associated to a level
   */
  public String getLevelName(String maturityLevel) {
    for (String[] level : levels) {
      if (level[0].equals(maturityLevel)) {
        return level[1];
      }
    }
    log4j.warn("Could not find maturity level " + maturityLevel);
    return "--";
  }

  /**
   * Returns the less mature level name. Assumes the list is ordered
   */
  public String getLessMature() {
    return levels[0][1];
  }

  /**
   * Returns the most mature level name. Assumes the list is ordered
   */
  public String getMostMature() {
    return levels[levels.length - 1][1];
  }

  boolean hasInternetError() {
    return error;
  }
}
