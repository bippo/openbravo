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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.domain.Preference;

/**
 * Creates the properties list which is initially loaded in the client.
 * 
 * @author mtaal
 */
public class PropertiesComponent extends BaseTemplateComponent {

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateComponent#getComponentTemplate()
   */
  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, ApplicationConstants.PROPERTIES_TEMPLATE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#getId()
   */
  public String getId() {
    return ApplicationConstants.PROPERTIES_COMPONENT_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.Component#getETag()
   */
  public String getETag() {
    // is never cached
    return OBContext.getOBContext().getUser().getId() + "_"
        + OBContext.getOBContext().getLanguage().getId() + "_" + System.currentTimeMillis();
  }

  public Collection<LocalProperty> getProperties() {
    final List<LocalProperty> properties = new ArrayList<LocalProperty>();
    final List<Preference> preferences = Preferences.getAllPreferences(OBContext.getOBContext()
        .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getUser().getId(), OBContext.getOBContext().getRole().getId());
    final List<String> handledIds = new ArrayList<String>();
    for (Preference preference : preferences) {
      final LocalProperty localProperty = new LocalProperty();
      if (preference.getProperty() != null) {
        localProperty.setId(preference.getProperty());
      } else {
        localProperty.setId(preference.getAttribute());
      }

      if (preference.getWindow() != null) {
        localProperty.setId(localProperty.getId() + "_" + DalUtil.getId(preference.getWindow()));
      }

      // prevent duplicates
      if (handledIds.contains(localProperty.getId())) {
        continue;
      }
      handledIds.add(localProperty.getId());

      String value = preference.getSearchKey();
      // validate the data, must be valid json
      if (localProperty.getId() != null && value != null) {
        boolean isValid = false;
        try {
          if (value.trim().startsWith("{")) {
            value = new JSONObject(value).toString();
            isValid = true;
          } else if (value.trim().startsWith("[")) {
            value = new JSONArray(value).toString();
            isValid = true;
          } else {
            localProperty.setStringValue(true);
            localProperty.setValue(value);
            isValid = true;
          }
        } catch (JSONException e) {
          // not a valid JSONObject
        }
        if (isValid) {
          localProperty.setValue(value);
          properties.add(localProperty);
        }
      }
      localProperty.setValue(preference.getSearchKey());
    }
    return properties;
  }

  public static class LocalProperty {
    private String id;
    private String value;
    private boolean stringValue = false;

    public boolean isStringValue() {
      return stringValue;
    }

    public void setStringValue(boolean stringValue) {
      this.stringValue = stringValue;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }
}
