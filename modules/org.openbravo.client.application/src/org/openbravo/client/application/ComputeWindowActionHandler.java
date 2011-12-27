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
package org.openbravo.client.application;

import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.WindowTrl;

/**
 * Computes information to open a classic window for a record in the new layout.
 * 
 * @author mtaal
 * @see StaticResourceComponent
 */
@ApplicationScoped
public class ComputeWindowActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    final String tabId = removeFragment((String) parameters.get("tabId"));
    final String recordId = removeFragment((String) parameters.get("recordId"));

    try {
      OBContext.setAdminMode();

      final JSONObject json = new JSONObject();
      final Tab tab = OBDal.getInstance().get(Tab.class, tabId);

      json.put("tabId", tabId);
      json.put("windowId", tab.getWindow().getId());

      final Entity entity = ModelProvider.getInstance().getEntity(tab.getTable().getName());

      // special case, find the real recordId for the language case
      if (entity.getName().equals(Language.ENTITY_NAME)) {
        final OBQuery<Language> languages = OBDal.getInstance().createQuery(Language.class,
            Language.PROPERTY_LANGUAGE + "=?");
        languages.setParameters(Collections.singletonList((Object) recordId));
        json.put("recordId", languages.list().get(0).getId());
      } else {
        json.put("recordId", recordId);
      }

      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      String tabTitle = null;
      for (WindowTrl windowTrl : tab.getWindow().getADWindowTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(windowTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = windowTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = tab.getWindow().getName();
      }

      json.put("keyParameter",
          "inp" + Sqlc.TransformaNombreColumna(entity.getIdProperties().get(0).getColumnName()));
      json.put("tabTitle", tabTitle);

      // find the model tab url
      String mappingName = Utility.getTabURL(tabId, "E", false);

      if (mappingName != null && !mappingName.isEmpty()) {
        json.put("mappingName", mappingName);
      }
      return json;
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  // solve the case that sometimes the fragment is added to the tabId or the record id
  private String removeFragment(String value) {
    if (value == null) {
      return value;
    }
    if (value.contains("#")) {
      return value.substring(0, value.indexOf("#"));
    }
    return value;
  }
}
