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
package org.openbravo.client.application;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

/**
 * Computes information to open a classic window for a record in the new layout.
 * 
 * @author mtaal, aro
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class ComputeTranslatedNameActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {

    final String processId = removeFragment((String) parameters.get("processId"));

    try {
      OBContext.setAdminMode();

      final org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, processId);

      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

      String title = process.getName();
      for (org.openbravo.model.ad.ui.ProcessTrl processtrl : process.getADProcessTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(processtrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          title = processtrl.getName();
        }
      }

      final JSONObject json = new JSONObject();
      json.put("processId", processId);
      json.put("processTitle", title);
      return json;

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  // solve the case that sometimes the fragment is added to the tabId or the record id
  private String removeFragment(String value) {

    return (value == null || !value.contains("#")) ? value : value.substring(0, value.indexOf("#"));
  }
}
