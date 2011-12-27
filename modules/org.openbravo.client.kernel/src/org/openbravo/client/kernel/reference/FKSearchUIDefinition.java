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
package org.openbravo.client.kernel.reference;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.ModelImplementationMapping;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.Selector;
import org.openbravo.model.ad.domain.SelectorColumn;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the foreign key ui definition which handles the classic search references.
 * 
 * @author mtaal
 */
public class FKSearchUIDefinition extends ForeignKeyUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBSearchItem";
  }

  @Override
  // get the current value for a search item from the database
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    try {
      final JSONObject json = new JSONObject(super.getFieldProperties(field, getValueFromSession));
      if (json.has("value")) {
        final Property prop = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn());
        final BaseOBObject target = OBDal.getInstance().get(prop.getTargetEntity().getName(),
            json.getString("value"));
        if (target != null) {
          json.put("identifier", target.getIdentifier());
        }
      }
      return json.toString();
    } catch (Exception e) {
      throw new OBException("Exception when processing field " + field, e);
    }
  }

  @Override
  public String getFieldProperties(Field field) {
    final String superJsonStr = super.getFieldProperties(field);
    if (field == null) {
      return superJsonStr;
    }
    try {
      final JSONObject json = new JSONObject(
          superJsonStr != null && superJsonStr.startsWith("{") ? superJsonStr : "{}");
      final Property prop = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn());
      final Reference reference = OBDal.getInstance().get(Reference.class,
          prop.getDomainType().getReference().getId());
      ModelImplementation modelImplementation = null;
      for (ModelImplementation localModelImplementation : reference.getADModelImplementationList()) {
        if (localModelImplementation.isActive()) {
          modelImplementation = localModelImplementation;
          break;
        }
      }
      if (modelImplementation == null) {
        return superJsonStr;
      }
      ModelImplementationMapping modelImplementationMapping = null;
      for (ModelImplementationMapping localModelImplementationMapping : modelImplementation
          .getADModelImplementationMappingList()) {
        if (localModelImplementationMapping.isActive()) {
          if (modelImplementationMapping == null) {
            modelImplementationMapping = localModelImplementationMapping;
          } else if (localModelImplementationMapping.isDefault()) {
            modelImplementationMapping = localModelImplementationMapping;
            break;
          }
        }
      }
      if (modelImplementationMapping == null) {
        // TODO: warn
        return superJsonStr;
      }

      json.put("searchUrl", modelImplementationMapping.getMappingName());

      Selector selector = null;
      for (Selector localSelector : reference.getADSelectorList()) {
        if (localSelector.isActive()) {
          selector = localSelector;
          break;
        }
      }
      if (selector == null) {
        // TODO: warn
        return superJsonStr;
      }
      final JSONArray inFields = new JSONArray();
      final List<String> outFields = new ArrayList<String>();
      for (SelectorColumn selectorColumn : selector.getADSelectorColumnList()) {
        if (selectorColumn.isActive()) {
          String columnName = selectorColumn.getDBColumnName()
              + (selectorColumn.getSuffix() != null ? selectorColumn.getSuffix() : "");
          columnName = "inp" + Sqlc.TransformaNombreColumna(columnName);
          if (selectorColumn.getColumnType().equals("I")) {
            JSONObject inField = new JSONObject();
            inField.put("columnName", columnName);
            inField.put("parameterName", "inp" + selectorColumn.getName());
            inFields.put(inField);
          } else {
            outFields.add(columnName);
          }
        }
      }
      json.put("inFields", inFields);
      json.put("outFields", new JSONArray(outFields));

      return json.toString();
    } catch (JSONException e) {
      throw new OBException("Exception when generating field properties for " + field, e);
    }
  }
}
