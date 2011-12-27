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
package org.openbravo.userinterface.selector.reference;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.reference.ForeignKeyUIDefinition;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.userinterface.selector.Selector;
import org.openbravo.userinterface.selector.SelectorComponent;
import org.openbravo.userinterface.selector.SelectorConstants;
import org.openbravo.userinterface.selector.SelectorField;

/**
 * Implementation of the foreign key ui definition which uses a selector for its input/filter types.
 * 
 * @author mtaal
 */
public class FKSelectorUIDefinition extends ForeignKeyUIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBSelectorItem";
  }

  @Override
  // get the current value for a selector item from the database
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    try {
      final JSONObject json = new JSONObject(super.getFieldProperties(field, getValueFromSession));
      if (json.has("value")) {
        final Property prop = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn());
        if (prop.isPrimitive()) {
          json.put("identifier", json.getString("value"));
        } else {
          final BaseOBObject target = OBDal.getInstance().get(prop.getTargetEntity().getName(),
              json.getString("value"));
          if (target != null) {
            final Selector selector = getSelector(field);
            final SelectorField displayField = selector.getDisplayfield();
            if (displayField == null) {
              json.put("identifier", target.getIdentifier());
            } else if (displayField.getProperty() != null) {
              json.put("identifier", DalUtil.getValueFromPath(target, displayField.getProperty()));
            } else {
              json.put("identifier", target.getIdentifier());
            }
          }
        }
      }
      return json.toString();
    } catch (Exception e) {
      throw new OBException("Exception when processing field " + field, e);
    }
  }

  public Map<String, Object> getDataSourceParameters() {
    final Map<String, Object> params = new HashMap<String, Object>();
    final Reference reference = OBDal.getInstance().get(Reference.class, getReference().getId());
    for (Selector selector : reference.getOBUISELSelectorList()) {
      if (selector.isActive() && selector.getTable() != null) {
        final String extraProperties = SelectorComponent.getAdditionalProperties(selector, true);
        if (extraProperties.length() > 0) {
          params.put(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER, extraProperties);
        }
        return params;
      }
    }
    return params;
  }

  @Override
  protected String getDisplayFieldName(Field field, Property prop) {
    final Selector selector = getSelector(field);
    final SelectorField displayField = selector.getDisplayfield();
    String displayFieldName = JsonConstants.IDENTIFIER;
    if (displayField != null && displayField.getProperty() != null) {
      displayFieldName = displayField.getProperty();
    } else {
      // fallback to the default
      return null;
    }
    return prop.getName() + "." + displayFieldName;
  }

  public String getFieldProperties(Field field) {
    if (field == null) {
      return super.getFieldProperties(field);
    }
    final Selector selector = getSelector(field);
    final String tableName = field.getColumn().getTable().getDBTableName();
    final String columnName = field.getColumn().getDBColumnName();

    final Property property = DalUtil.getProperty(tableName, columnName);

    final SelectorComponent selectorComponent = WeldUtils
        .getInstanceFromStaticBeanManager(SelectorComponent.class);
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(SelectorConstants.PARAM_TAB_ID, field.getTab().getId());
    parameters.put(SelectorConstants.PARAM_COLUMN_NAME, field.getColumn().getDBColumnName());
    parameters.put(SelectorComponent.SELECTOR_ITEM_PARAMETER, "true");
    parameters.put(SelectorConstants.PARAM_TARGET_PROPERTY_NAME, property.getName());
    selectorComponent.setId(selector.getId());
    selectorComponent.setParameters(parameters);

    // append the super fields
    final String selectorFields = selectorComponent.generate();
    final String superJsonStr = super.getFieldProperties(field);
    if (superJsonStr.trim().startsWith("{")) {
      return selectorFields + ","
          + superJsonStr.trim().substring(1, superJsonStr.trim().length() - 1);
    }
    return selectorFields;
  }

  private Selector getSelector(Field field) {
    final Reference reference = field.getColumn().getReferenceSearchKey();
    Check.isNotNull(reference, "Field " + field + " does not have a reference value set");
    for (Selector selector : reference.getOBUISELSelectorList()) {
      if (selector.isActive()) {
        return selector;
      }
    }
    Check.fail("No valid selector for field " + field);
    return null;
  }
}
