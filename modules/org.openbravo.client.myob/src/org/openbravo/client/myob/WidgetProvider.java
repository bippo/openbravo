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
package org.openbravo.client.myob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.EnumerateDomainType;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.ParameterTrl;
import org.openbravo.client.application.ParameterUtils;
import org.openbravo.client.application.ParameterValue;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.reference.EnumUIDefinition;
import org.openbravo.client.kernel.reference.FKComboUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.ReferencedTable;
import org.openbravo.model.ad.module.ModuleDBPrefix;
import org.openbravo.service.json.JsonConstants;

/**
 * Responsible for creating a widget definition.
 * 
 * NOTE: must be instantiated through the {@link MyOBUtils#getWidgetProvider(WidgetClass)} method.
 * 
 * @author mtaal
 */
public abstract class WidgetProvider {
  private static final Logger log = Logger.getLogger(WidgetProvider.class);

  public static final String WIDGETCLASSID = "widgetClassId";
  public static final String TITLE = "title";
  private static final String COLNUM = "colNum";
  private static final String ROWNUM = "rowNum";
  private static final String HEIGHT = "height";
  private static final String PRIORITY = "priority";
  private static final String DESCRIPTION = "description";
  private static final String SUPERCLASSTITLE = "superclassTitle";
  private static final String DATAACCESSLEVEL = "dataAccessLevel";
  private static final String ENABLEDALLUSERS = "enabledAllUsers";
  private static final String AUTHORMSG = "authorMsg";
  private static final String AUTHORURL = "authorUrl";
  private static final String MODULENAME = "moduleName";
  private static final String MODULEVERSION = "moduleVersion";
  private static final String MODULESTATUS = "moduleStatus";
  private static final String MODULEJPACKAGE = "moduleJavaPackage";
  private static final String MODULETYPE = "moduleType";
  private static final String MODULEDBPREFIX = "moduleDBPrefix";
  private static final String MODULELICENSETYPE = "moduleLicenseType";
  private static final String MODULEUPDATEINFO = "moduleUpdateInfo";
  private static final String MODULELICENSETEXT = "moduleLicenseText";
  private static final String MODULEAUTHOR = "moduleAuthor";
  private static final String ABOUTFIELDDEFINITIONS = "aboutFieldDefinitions";
  protected static final String PARAMETERS = "parameters";
  protected static final String FIELDDEFINITIONS = "fieldDefinitions";
  private static final String ITEMDATA = "itemData";
  private static final String PARAMETERID = "parameterId";
  private static final String PARAMETERNAME = "name";
  private static final String PARAMETERTITLE = "title";
  private static final String PARAMETERTYPE = "type";
  private static final String PARAMETERWIDTH = "width";
  private static final String PARAMETERCOLSPAN = "colSpan";
  private static final String PARAMETERFIELDPROPERTIES = "fieldProperties";
  private static final String PARAMETERREQUIRED = "required";
  private static final String DBINSTANCEID = "dbInstanceId";
  private static final String CAN_MAXIMIZE = "showMaximizeButton";
  private static final String MENU_ITEMS = "menuItems";
  private static final Long WIDGET_HEADER_HEIGHT = 35L;

  private Map<String, Object> parameters = new HashMap<String, Object>();

  // note is only set if the widgetprovider is created
  // through the MyOBUtils class.
  private WidgetClass widgetClass;

  @Inject
  private MyOBUtils myOBUtils;

  // prevent anyone else from creating a widgetprovider directly
  protected WidgetProvider() {
  }

  public JSONObject getWidgetClassDefinition() {
    try {
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put(MyOpenbravoWidgetComponent.CLASSNAMEPARAMETER,
          this.getClientSideWidgetClassName());
      jsonObject.put(WIDGETCLASSID, widgetClass.getId());
      jsonObject.put(TITLE, MyOBUtils.getWidgetTitle(widgetClass));
      jsonObject.put(HEIGHT, widgetClass.getHeight() + WIDGET_HEADER_HEIGHT);
      jsonObject.put(MENU_ITEMS, MyOBUtils.getWidgetMenuItems(widgetClass));
      if (widgetClass.getWidgetSuperclass() != null) {
        jsonObject.put(CAN_MAXIMIZE, widgetClass.getWidgetSuperclass().isCanMaximize());
      } else {
        jsonObject.put(CAN_MAXIMIZE, widgetClass.isCanMaximize());
      }

      final JSONObject aboutFieldDefinitions = new JSONObject();
      aboutFieldDefinitions.put(MODULENAME, widgetClass.getModule().getName());
      aboutFieldDefinitions.put(MODULEVERSION, widgetClass.getModule().getVersion());
      aboutFieldDefinitions.put(MODULESTATUS, widgetClass.getModule().getStatus());
      aboutFieldDefinitions.put(MODULEJPACKAGE, widgetClass.getModule().getJavaPackage());
      aboutFieldDefinitions.put(MODULETYPE, widgetClass.getModule().getType());

      String moduleDBPrefixList = "";
      for (ModuleDBPrefix moduleDBPrefix : widgetClass.getModule().getModuleDBPrefixList()) {
        moduleDBPrefixList += moduleDBPrefix.getName() + " ";
      }
      aboutFieldDefinitions.put(MODULEDBPREFIX, moduleDBPrefixList);
      aboutFieldDefinitions.put(MODULELICENSETYPE, widgetClass.getModule().getLicenseType());
      aboutFieldDefinitions.put(MODULEUPDATEINFO,
          widgetClass.getModule().getUpdateInformation() == null ? "" : widgetClass.getModule()
              .getUpdateInformation());
      aboutFieldDefinitions.put(MODULELICENSETEXT,
          widgetClass.getModule().getLicenseText() == null ? "" : widgetClass.getModule()
              .getLicenseText());
      aboutFieldDefinitions.put(MODULEAUTHOR, widgetClass.getModule().getAuthor() == null ? ""
          : widgetClass.getModule().getAuthor());
      aboutFieldDefinitions.put(TITLE, MyOBUtils.getWidgetTitle(widgetClass));
      aboutFieldDefinitions.put(DESCRIPTION, widgetClass.getDescription() == null ? ""
          : widgetClass.getDescription());
      aboutFieldDefinitions.put(SUPERCLASSTITLE, widgetClass.getWidgetSuperclass() == null ? ""
          : MyOBUtils.getWidgetTitle(widgetClass.getWidgetSuperclass()));
      aboutFieldDefinitions.put(DATAACCESSLEVEL, widgetClass.getDataAccessLevel());
      aboutFieldDefinitions.put(ENABLEDALLUSERS, widgetClass.isAllowAnonymousAccess());
      aboutFieldDefinitions.put(AUTHORMSG,
          widgetClass.getAuthorMsg() == null ? "" : widgetClass.getAuthorMsg());
      aboutFieldDefinitions.put(AUTHORURL,
          widgetClass.getAuthorUrl() == null ? "" : widgetClass.getAuthorUrl());

      final JSONObject defaultParameters = new JSONObject();
      final List<JSONObject> fieldDefinitions = new ArrayList<JSONObject>();
      for (Parameter parameter : widgetClass.getOBUIAPPParameterEMObkmoWidgetClassIDList()) {
        // fixed parameters are not part of the fielddefinitions
        if (parameter.isFixed()) {
          defaultParameters.put(parameter.getDBColumnName(), parameter.getFixedValue());
          continue;
        }
        if (parameter.getDefaultValue() != null) {
          defaultParameters.put(parameter.getDBColumnName(), parameter.getDefaultValue());
        }
        final JSONObject fieldDefinition = new JSONObject();
        fieldDefinition.put(PARAMETERID, parameter.getId());
        fieldDefinition.put(PARAMETERNAME, parameter.getDBColumnName());
        fieldDefinition.put(PARAMETERREQUIRED, parameter.isMandatory());
        fieldDefinition.put(PARAMETERWIDTH, "*");

        final Reference reference;
        if (parameter.getReferenceSearchKey() != null) {
          reference = parameter.getReferenceSearchKey();
        } else {
          reference = parameter.getReference();
        }
        if (reference.getName().equals("Text") || reference.getName().equals("Memo"))
          fieldDefinition.put(PARAMETERCOLSPAN, 2);

        final UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
            reference);
        fieldDefinition.put(PARAMETERTYPE, uiDefinition.getName());

        try {
          final String fieldProperties = uiDefinition.getFieldProperties(null);
          if (fieldProperties != null && fieldProperties.trim().length() > 0) {
            final JSONObject fieldPropertiesObject = new JSONObject(fieldProperties);
            fieldDefinition.put(PARAMETERFIELDPROPERTIES, fieldPropertiesObject);
          }
        } catch (NullPointerException e) {
          // handle non-carefull implementors of ui definitions
          log.error("Error when processing parameter: " + parameter, e);
          // ignore this field properties for now
        }

        final Object valueMap = getComboBoxData(reference);
        if (valueMap != null) {
          if (valueMap instanceof Collection<?>) {
            fieldDefinition.put(ITEMDATA, (Collection<?>) valueMap);
          } else {
            fieldDefinition.put(ITEMDATA, valueMap);
          }
        }
        fieldDefinition.put(PARAMETERTITLE, getParameterLabel(parameter));
        fieldDefinitions.add(fieldDefinition);
      }
      jsonObject.put(PARAMETERS, defaultParameters);
      jsonObject.put(FIELDDEFINITIONS, fieldDefinitions);
      jsonObject.put(ABOUTFIELDDEFINITIONS, aboutFieldDefinitions);
      return jsonObject;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  protected void addDefaultWidgetProperties(JSONObject jsonObject, WidgetInstance widgetInstance)
      throws JSONException {
    jsonObject.put(WIDGETCLASSID, widgetInstance.getWidgetClass().getId());
    jsonObject.put(MyOpenbravoWidgetComponent.CLASSNAMEPARAMETER,
        myOBUtils.getWidgetProvider(widgetClass).getClientSideWidgetClassName());
    jsonObject.put(DBINSTANCEID, widgetInstance.getId());
    jsonObject.put(TITLE, MyOBUtils.getWidgetTitle(widgetInstance));
    jsonObject.put(COLNUM, widgetInstance.getColumnPosition());
    jsonObject.put(ROWNUM, widgetInstance.getSequenceInColumn());
    jsonObject.put(HEIGHT, widgetClass.getHeight() + WIDGET_HEADER_HEIGHT);
    jsonObject.put(PRIORITY, widgetInstance.getRelativePriority());

    final JSONObject widgetParameters = new JSONObject();
    for (ParameterValue parameterValue : widgetInstance
        .getOBUIAPPParameterValueEMObkmoWidgetInstanceIDList()) {
      widgetParameters.put(parameterValue.getParameter().getDBColumnName(),
          ParameterUtils.getParameterValue(parameterValue));
    }

    // Include fixed parameters in the definition.
    for (Parameter parameter : widgetInstance.getWidgetClass()
        .getOBUIAPPParameterEMObkmoWidgetClassIDList()) {
      if (!widgetParameters.has(parameter.getDBColumnName()) && parameter.isFixed()) {
        widgetParameters.put(parameter.getDBColumnName(),
            ParameterUtils.getParameterFixedValue(getStringParameters(getParameters()), parameter));

      }
    }
    jsonObject.put(PARAMETERS, widgetParameters);
  }

  public String getClientSideWidgetClassName() {
    return KernelConstants.ID_PREFIX + getWidgetClass().getId();
  }

  /**
   * As a default will generate javascript which extends the OBShowParameterWidget widget.
   * 
   */
  public String generate() {
    return "isc.defineClass('" + KernelConstants.ID_PREFIX + getWidgetClass().getId()
        + "', isc.OBShowParameterWidget);";
  }

  public JSONObject getWidgetInstanceDefinition(WidgetInstance widgetInstance) {
    try {
      final JSONObject jsonObject = new JSONObject();
      addDefaultWidgetProperties(jsonObject, widgetInstance);
      return jsonObject;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  private Map<String, String> getStringParameters(Map<String, Object> _parameters) {
    Map<String, String> stringParameters = new HashMap<String, String>();
    final Iterator<String> keys = _parameters.keySet().iterator();
    while (keys.hasNext()) {
      final String keyName = keys.next();
      if (_parameters.get(keyName) instanceof String) {
        stringParameters.put(keyName, (String) _parameters.get(keyName));
      }
    }
    return stringParameters;
  }

  private String getParameterLabel(Parameter parameter) {
    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    for (ParameterTrl trl : parameter.getOBUIAPPParameterTrlList()) {
      if (DalUtil.getId(trl.getLanguage()).equals(userLanguageId)) {
        return trl.getName();
      }
    }
    return parameter.getName();
  }

  // ++++++++++ Code below should be moved to the UIDefinition classes +++++++

  private static Object getComboBoxData(Reference reference) {
    OBContext.setAdminMode();
    try {
      final UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
          reference);

      if (uiDefinition instanceof EnumUIDefinition) {
        return getComboBoxData((EnumUIDefinition) uiDefinition);
      } else if (uiDefinition instanceof FKComboUIDefinition) {
        return getComboBoxData((FKComboUIDefinition) uiDefinition);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static Object getComboBoxData(FKComboUIDefinition fkComboUIDefinition) throws Exception {
    // FIXME: Revisit this method later. Not all foreign keys have a ADReferenceTable.
    final ReferencedTable refTable = fkComboUIDefinition.getReference().getADReferencedTableList()
        .get(0);
    final Entity entity = ModelProvider.getInstance().getEntity(refTable.getTable().getName());

    Property displayProperty = null;
    // for now always display the identifier
    // if (false && refTable.getDisplayedColumn() != null) {
    // final String displayColId = (String) DalUtil.getId(refTable.getDisplayedColumn());
    // for (Property prop : entity.getProperties()) {
    // if (prop.getColumnId().equals(displayColId)) {
    // displayProperty = prop;
    // break;
    // }
    // }
    // }

    final String orderBy;
    if (refTable.getHqlorderbyclause() != null) {
      orderBy = refTable.getHqlorderbyclause();
    } else {
      final StringBuilder sb = new StringBuilder();
      for (Property prop : entity.getIdentifierProperties()) {
        if (sb.length() > 0) {
          sb.append(",");
        }
        sb.append(prop.getName());
      }
      orderBy = sb.toString();
    }
    final String whereOrderByClause = (refTable.getHqlwhereclause() != null ? refTable
        .getHqlwhereclause() : "") + " order by " + orderBy;
    final OBQuery<BaseOBObject> obQuery = OBDal.getInstance().createQuery(entity.getName(),
        whereOrderByClause);
    final List<JSONObject> values = new ArrayList<JSONObject>();
    for (BaseOBObject bob : obQuery.list()) {
      final JSONObject dataJSONObject = new JSONObject();
      dataJSONObject.put(JsonConstants.ID, bob.getId());
      dataJSONObject
          .put(
              JsonConstants.IDENTIFIER,
              (displayProperty != null ? bob.getValue(displayProperty.getName()) : bob
                  .getIdentifier()));
      values.add(dataJSONObject);
    }
    return values;
  }

  private static JSONObject getComboBoxData(EnumUIDefinition enumUIDefinition) throws Exception {
    final EnumerateDomainType enumDomainType = (EnumerateDomainType) enumUIDefinition
        .getDomainType();
    @SuppressWarnings("unchecked")
    final Map<String, String> valueMap = createValueMap(
        (Set<String>) enumDomainType.getEnumerateValues(), enumUIDefinition.getReference().getId());
    final JSONObject valueMapJSONObject = new JSONObject();
    for (String key : valueMap.keySet()) {
      valueMapJSONObject.put(key, valueMap.get(key));
    }
    return valueMapJSONObject;
  }

  public static Map<String, String> createValueMap(Set<String> allowedValues, String referenceId) {
    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();

    final Map<String, String> translatedValues = new LinkedHashMap<String, String>();

    for (String allowedValue : allowedValues) {
      translatedValues.put(allowedValue, allowedValue);
    }

    final String readReferenceHql = "select searchKey, name from ADList where reference.id=?";
    final Query readReferenceQry = OBDal.getInstance().getSession().createQuery(readReferenceHql);
    readReferenceQry.setString(0, referenceId);
    for (Object o : readReferenceQry.list()) {
      final Object[] row = (Object[]) o;
      final String value = (String) row[0];
      final String name = (String) row[1];
      if (allowedValues.contains(value)) {
        translatedValues.put(value, name);
      }
    }

    // set the default if no translation found
    final String hql = "select al.searchKey, trl.name from ADList al, ADListTrl trl where "
        + " al.reference.id=? and trl.listReference=al and trl.language.id=?"
        + " and al.active=true and trl.active=true";
    final Query qry = OBDal.getInstance().getSession().createQuery(hql);
    qry.setString(0, referenceId);
    qry.setString(1, userLanguageId);
    for (Object o : qry.list()) {
      final Object[] row = (Object[]) o;
      translatedValues.put((String) row[0], (String) row[1]);
    }
    return translatedValues;
  }

  public WidgetClass getWidgetClass() {
    return widgetClass;
  }

  public void setWidgetClass(WidgetClass widgetClass) {
    this.widgetClass = widgetClass;
  }

  /**
   * Override this method to make validations on widget classes. If this method returns false the
   * widget class won't be available for users to add new instances.
   * 
   * @return true if the widget class definition is valid.
   */
  public boolean validate() {
    return true;
  }
}
