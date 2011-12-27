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
package org.openbravo.erpCommon.utility;

import org.openbravo.erpCommon.businessUtility.Preferences;

/**
 * Exception thrown when a preference is not defined for the current visibility level.
 * 
 * @see Preferences#getPreferenceValue(String, boolean, org.openbravo.model.ad.system.Client,
 *      org.openbravo.model.common.enterprise.Organization, org.openbravo.model.ad.access.User,
 *      org.openbravo.model.ad.access.Role, org.openbravo.model.ad.ui.Window)
 *      Preferences.getPreferenceValue
 * 
 */
public class PropertyNotFoundException extends PropertyException {

  private static final long serialVersionUID = 1L;

}
