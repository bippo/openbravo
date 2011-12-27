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

package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.client.application.event.ModuleHandler;
import org.openbravo.client.kernel.BaseComponent;

/**
 * This callout is executed when AD_System_Info.Customization_Allowed column value changes. In case
 * this value is set to true, it nullifies module cache in other to detect the newly created in
 * development template for the ETag.
 * 
 * This is needed because currently System Info window is opened in 2.50 style, so
 * {@link ModuleHandler}, which performs the same action, is not executed in this case.
 * 
 * @see ModuleHandler
 * @see BaseComponent#getETag()
 * 
 * @author alostale
 * 
 */
public class SysInfoCustomAllowed extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    boolean customAllowed = info.getStringParameter("inpcustomizationAllowed",
        new ValueListFilter("Y", "N", "")).equals("Y");
    if (customAllowed) {
      BaseComponent.nullifyModuleCache();
    }
    // No callout return needed
  }
}
