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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

/**
 * Base implementation of a user interface client reference.
 * 
 * @author mtaal
 */
public abstract class UIDefinition {
  private static final String TYPE_NAME_PREFIX = "_id_";

  private Reference reference;
  private DomainType domainType;
  protected static final Logger log = Logger.getLogger(UIDefinition.class);

  /**
   * Unique name used to identify the type.
   * 
   */
  public String getName() {
    return TYPE_NAME_PREFIX + reference.getId();
  }

  /**
   * @return the Smartclient type from which this type inherits.
   */
  public String getParentType() {
    return "text";
  }

  /**
   * @return the form item type used for editing this reference in a form.
   */
  public String getFormEditorType() {
    return "OBTextItem";
  }

  /**
   * @return the form item type used for editing this reference in a grid. As a default will return
   *         {@link #getFormEditorType()}
   */
  public String getGridEditorType() {
    return getFormEditorType();
  }

  /**
   * @return the form item type used for filtering in grids. As a default will return
   *         {@link #getFormEditorType()}
   */
  public String getFilterEditorType() {
    return getFormEditorType();
  }

  /**
   * Computes the properties used to define the type, this includes all the Smartclient SimpleType
   * properties.
   * 
   * @return a javascript string which can be included in the javascript defining the SimpleType.
   *         The default implementation returns an empty string.
   */
  public String getTypeProperties() {
    return "";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient form. This can be the
   * default value or the sets of values in the valuemap.
   * 
   * NOTE: the field parameter may be null, implementors of subclasses should take this into
   * account.
   * 
   * @param field
   *          the field for which the information should be computed. NOTE: the caller is allowed to
   *          pass null for cases where the field properties are needed for a FormItem which is not
   *          backed by an Openbravo field.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getFieldProperties(Field field) {
    if (field != null && field.isDisplayed() != null && !field.isDisplayed()) {
      return ""; // Not displayed fields use HiddenItem
    }
    return "";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient form. This can be the
   * default value or the sets of values in the valuemap.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @param getValueFromSession
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    String columnValue = "";
    RequestContext rq = RequestContext.get();
    if (getValueFromSession) {
      columnValue = rq.getRequestParameter("inp"
          + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()));
    } else {
      if (field.getColumn().getDBColumnName().equalsIgnoreCase("documentno")
          || (field.getColumn().isUseAutomaticSequence() && field.getColumn().getDBColumnName()
              .equals("Value"))) {
        String docTypeTarget = rq.getRequestParameter("inp"
            + Sqlc.TransformaNombreColumna("C_DocTypeTarget_ID"));
        if (docTypeTarget == null)
          docTypeTarget = "";
        String docType = rq.getRequestParameter("inp"
            + Sqlc.TransformaNombreColumna("C_DocType_ID"));
        if (docType == null)
          docType = "";
        columnValue = "<"
            + Utility.getDocumentNo(new DalConnectionProvider(false), rq.getVariablesSecureApp(),
                field.getTab().getWindow().getId(), field.getColumn().getTable().getDBTableName(),
                docTypeTarget, docType, false, false) + ">";
      } else {
        String defaultS = field.getColumn().getDefaultValue();
        if (defaultS == null || defaultS.equals("\"\"")) {
          defaultS = "";
        }
        if (defaultS.equalsIgnoreCase("@#Date@")) {
          return setNOWDefault();
        } else if (!defaultS.startsWith("@SQL=")) {
          columnValue = Utility.getDefault(new DalConnectionProvider(false),
              rq.getVariablesSecureApp(), field.getColumn().getDBColumnName(), defaultS, field
                  .getTab().getWindow().getId(), "");
        } else {
          ArrayList<String> params = new ArrayList<String>();
          String sql = parseSQL(defaultS, params);
          int indP = 1;
          try {
            PreparedStatement ps = OBDal.getInstance().getConnection(false).prepareStatement(sql);
            for (String parameter : params) {
              String value = "";
              if (parameter.substring(0, 1).equals("#")) {
                value = Utility.getContext(new DalConnectionProvider(false), RequestContext.get()
                    .getVariablesSecureApp(), parameter, field.getTab().getWindow().getId());
              } else {
                String fieldId = "inp" + Sqlc.TransformaNombreColumna(parameter);
                if (RequestContext.get().getParameterMap().containsKey(fieldId)) {
                  value = RequestContext.get().getRequestParameter(fieldId);
                }
                if (value == null || value.equals("")) {
                  value = Utility.getContext(new DalConnectionProvider(false), RequestContext.get()
                      .getVariablesSecureApp(), parameter, field.getTab().getWindow().getId());
                }
              }
              ps.setObject(indP++, value);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
              columnValue = rs.getString(1);
            }
          } catch (Exception e) {
            log.error("Error computing default value for field " + field.getName() + " of tab "
                + field.getTab().getName(), e);
          }
        }
      }
    }
    if (columnValue == null || columnValue.equals("null")) {
      columnValue = "";
    }
    JSONObject jsnobject = new JSONObject();
    try {
      jsnobject.put("value", createFromClassicString(columnValue));
      jsnobject.put("classicValue", columnValue);
    } catch (JSONException e) {
      log.error("Couldn't get field property value for column "
          + field.getColumn().getDBColumnName());
    }
    return jsnobject.toString();
  }

  /**
   * It returns the same as getFieldProperties except in the case of combo UIDefinitions. In combo
   * UI definitions, a call to the super will be done, but the combo computation itself will not be
   * done (so only the default value, or the current request value, will be considered).
   * 
   */
  public String getFieldPropertiesWithoutCombo(Field field, boolean getValueFromSession) {
    return getFieldProperties(field, getValueFromSession);
  }

  /**
   * Returns alignment in grid view. In case it returns null, default alignment for actual data type
   * is used.
   * 
   * @return <code>null</code> for default alignment, "left", "center" or "right"
   */
  public String getCellAlign() {
    return null;
  }

  private String setNOWDefault() {
    JSONObject jsnobject = new JSONObject();
    try {
      UIDefinition uiDef = this;
      if (!(this instanceof DateUIDefinition)) {
        for (UIDefinition def : UIDefinitionController.getInstance().getAllUIDefinitions()) {
          if (def instanceof DateUIDefinition) {
            uiDef = def;
            break;
          }
        }
      }
      String columnValue = uiDef.convertToClassicString(new Date());
      jsnobject.put("value", uiDef.createFromClassicString(columnValue));
      jsnobject.put("classicValue", columnValue);
      jsnobject.put("hasDateDefault", true);
    } catch (JSONException e) {
      log.error("Couldn't get field property value");
    }
    return jsnobject.toString();
  }

  /**
   * Convert a string value as used in classic OB to a type safe value.
   * 
   * @see PrimitiveDomainType#createFromString(String)
   */
  public Object createFromClassicString(String value) {
    if (getDomainType() instanceof PrimitiveDomainType) {
      return ((PrimitiveDomainType) getDomainType()).createFromString(value);
    } else {
      return value;
    }
  }

  /**
   * Creates a classic string which is used by callouts from an object value.
   * 
   * @param value
   * @return the classic string
   */
  public String convertToClassicString(Object value) {
    if (value == null) {
      return "";
    }
    if (!(getDomainType() instanceof PrimitiveDomainType)) {
      return value.toString();
    }
    return ((PrimitiveDomainType) getDomainType()).convertToString(value);
  }

  /**
   * Parameters passed in to the datasource, for example the
   * {@link JsonConstants#ADDITIONAL_PROPERTIES_PARAMETER} can be passed in like this.
   * 
   * @return a list of parameters used to drive the datasource generation incorporating this
   *         UIDefinition.
   */
  public Map<String, Object> getDataSourceParameters() {
    return Collections.emptyMap();
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient grid filter. This can be
   * the default value or the sets of values in the valuemap.
   * 
   * Note: the result should be either empty, if not empty then it start with a comma and end
   * without a comma, this to generate correct javascript.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getFilterEditorProperties(Field field) {
    if (getFilterEditorType() == null) {
      return ",canFilter: false";
    }
    return getFilterEditorPropertiesProperty(field);
  }

  /**
   * Returns the filterEditorProperties property set on the gridfield. Note for implementations in
   * the subclass: field maybe null.
   * 
   * @param field
   *          the field to generate the filter editor properties for, note it is allowed to pass
   *          null, implementors should gracefully handle this.
   * @return
   */
  protected String getFilterEditorPropertiesProperty(Field field) {
    return "";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient grid cell. This can be the
   * default value or the sets of values in the valuemap.
   * 
   * Note: the result should be either empty, if not empty then it start with a comma and end
   * without a comma, this to generate correct javascript.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getGridFieldProperties(Field field) {
    if (this.getGridEditorType() != null
        && !this.getGridEditorType().equals(this.getFormEditorType())) {
      return ", editorType: '" + this.getGridEditorType() + "'";
    }
    return "";
  }

  /**
   * Computes properties to initialize and set the field in a Smartclient grid cell when it is being
   * edited. This can be the default value or the sets of values in the valuemap.
   * 
   * Note: the result should be either empty, if not empty then it start with a comma and end
   * without a comma, this to generate correct javascript.
   * 
   * @param field
   *          the field for which the information should be computed.
   * @return a JSONObject string which is used to initialize the formitem.
   */
  public String getGridEditorFieldProperties(Field field) {
    return "";
  }

  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
  }

  public DomainType getDomainType() {
    if (domainType == null) {
      if (reference == null) {
        throw new OBException("Domain type can not be computed, reference is not set");
      }
      domainType = ModelProvider.getInstance().getReference(reference.getId()).getDomainType();
    }
    return domainType;
  }

  // note can make sense to also enable hover of values for enums
  // but then the value should be converted to the translated
  // value of the enum
  protected String getShowHoverGridFieldSettings(Field field) {
    return ", showHover: true";
  }

  protected String getGridFieldName(Field fld) {
    final Property prop = KernelUtils.getInstance().getPropertyFromColumn(fld.getColumn());
    return prop.getName();
  }

  protected String getValueInComboReference(Field field, boolean getValueFromSession,
      String columnValue) {
    try {
      RequestContext rq = RequestContext.get();
      VariablesSecureApp vars = rq.getVariablesSecureApp();
      boolean comboreload = rq.getRequestParameter("donotaddcurrentelement") != null
          && rq.getRequestParameter("donotaddcurrentelement").equals("true");
      String ref = field.getColumn().getReference().getId();
      String objectReference = "";
      if (field.getColumn().getReferenceSearchKey() != null) {
        objectReference = field.getColumn().getReferenceSearchKey().getId();
      }
      String validation = "";
      if (field.getColumn().getValidation() != null) {
        validation = field.getColumn().getValidation().getId();
      }
      String orgList = Utility.getReferenceableOrg(vars, vars.getStringParameter("inpadOrgId"));
      String clientList = Utility.getContext(new DalConnectionProvider(false), vars,
          "#User_Client", field.getTab().getWindow().getId());
      if (field.getColumn().getDBColumnName().equalsIgnoreCase("AD_CLIENT_ID")) {
        clientList = Utility.getContext(new DalConnectionProvider(false), vars, "#User_Client",
            field.getTab().getWindow().getId(),
            Integer.parseInt(field.getTab().getTable().getDataAccessLevel()));
        clientList = vars.getSessionValue("#User_Client");
        orgList = null;
      }
      if (field.getColumn().getDBColumnName().equalsIgnoreCase("AD_ORG_ID")) {
        orgList = Utility.getContext(new DalConnectionProvider(false), vars, "#User_Org", field
            .getTab().getWindow().getId(),
            Integer.parseInt(field.getTab().getTable().getDataAccessLevel()));
      }
      ApplicationDictionaryCachedStructures cachedStructures = WeldUtils
          .getInstanceFromStaticBeanManager(ApplicationDictionaryCachedStructures.class);
      ComboTableData comboTableData = cachedStructures.getComboTableData(vars, ref, field
          .getColumn().getDBColumnName(), objectReference, validation, orgList, clientList);
      FieldProvider tabData = generateTabData(field.getTab().getADFieldList(), field, columnValue);
      Map<String, String> parameters = comboTableData.fillSQLParametersIntoMap(
          new DalConnectionProvider(false), vars, tabData, field.getTab().getWindow().getId(),
          (getValueFromSession && !comboreload) ? columnValue : "");
      FieldProvider[] fps = comboTableData.select(new DalConnectionProvider(false), parameters,
          getValueFromSession && !comboreload);
      ArrayList<FieldProvider> values = new ArrayList<FieldProvider>();
      values.addAll(Arrays.asList(fps));
      ArrayList<JSONObject> comboEntries = new ArrayList<JSONObject>();
      ArrayList<String> possibleIds = new ArrayList<String>();
      // If column is mandatory we add an initial blank value
      if (!field.getColumn().isMandatory()) {
        possibleIds.add("");
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, (String) null);
        entry.put(JsonConstants.IDENTIFIER, (String) null);
        comboEntries.add(entry);
      }
      for (FieldProvider fp : values) {
        possibleIds.add(fp.getField("ID"));
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, fp.getField("ID"));
        entry.put(JsonConstants.IDENTIFIER, fp.getField("NAME"));
        comboEntries.add(entry);
      }
      JSONObject fieldProps = new JSONObject();
      if (getValueFromSession && !comboreload) {
        fieldProps.put("value", columnValue);
        fieldProps.put("classicValue", columnValue);
      } else {
        if (possibleIds.contains(columnValue)) {
          fieldProps.put("value", columnValue);
          fieldProps.put("classicValue", columnValue);
        } else {
          // In case the default value doesn't exist in the combo values, we choose the first one
          if (comboEntries.size() > 0) {
            if (comboEntries.get(0).has(JsonConstants.ID)) {
              fieldProps.put("value", comboEntries.get(0).get(JsonConstants.ID));
              fieldProps.put("classicValue", comboEntries.get(0).get(JsonConstants.ID));
            } else {
              fieldProps.put("value", (String) null);
              fieldProps.put("classicValue", (String) null);
            }
          } else {
            fieldProps.put("value", "");
            fieldProps.put("classicValue", "");
          }
        }
      }
      fieldProps.put("entries", new JSONArray(comboEntries));
      // comboValues.put(fieldIndex, values);
      // columnValues.put(fieldIndex, fixComboValue(columnValues.get(fieldIndex), fps));
      return fieldProps.toString();
    } catch (Exception e) {
      throw new OBException("Error while computing combo data", e);
    }
  }

  private FieldProvider generateTabData(List<Field> fields, Field currentField, String currentValue) {
    HashMap<String, Object> noinpDataMap = new HashMap<String, Object>();
    for (Field field : fields) {
      if (field.getColumn() == null) {
        continue;
      }
      String oldKey = "inp" + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName());
      Object value;
      if (currentField.getId().equals(field.getId())) {
        value = currentValue;
      } else {
        value = RequestContext.get().getRequestParameter(oldKey);
      }
      noinpDataMap.put(field.getColumn().getDBColumnName(),
          value == null || value.equals("") ? null : value.toString());
    }
    return new FieldProviderFactory(noinpDataMap);
  }

  public static String parseSQL(String code, ArrayList<String> colNames) {
    if (code == null || code.trim().equals(""))
      return "";
    String token;
    String strValue = code;
    StringBuffer strOut = new StringBuffer();

    int i = strValue.indexOf("@");
    String strAux, strAux1;
    while (i != -1) {
      if (strValue.length() > (i + 5) && strValue.substring(i + 1, i + 5).equalsIgnoreCase("SQL=")) {
        strValue = strValue.substring(i + 5, strValue.length());
      } else {
        // Delete the chain symbol
        strAux = strValue.substring(0, i).trim();
        if (strAux.substring(strAux.length() - 1).equals("'")) {
          strAux = strAux.substring(0, strAux.length() - 1);
          strOut.append(strAux);
        } else
          strOut.append(strValue.substring(0, i));
        strAux1 = strAux;
        if (strAux.substring(strAux.length() - 1).equals("("))
          strAux = strAux.substring(0, strAux.length() - 1).toUpperCase().trim();
        if (strAux.length() > 3
            && strAux.substring(strAux.length() - 3, strAux.length()).equals(" IN")) {
          strAux = " type=\"replace\" optional=\"true\" after=\"" + strAux1 + "\" text=\"'" + i
              + "'\"";
        } else {
          strAux = "";
        }
        strValue = strValue.substring(i + 1, strValue.length());

        int j = strValue.indexOf("@");
        if (j < 0)
          return "";

        token = strValue.substring(0, j);

        // String modifier = ""; // holds the modifier (# or $) for the session value
        // if (token.substring(0, 1).indexOf("#") > -1 || token.substring(0, 1).indexOf("$") > -1) {
        // modifier = token.substring(0, 1);
        // token = token.substring(1, token.length());
        // }
        if (strAux.equals(""))
          strOut.append("?");
        else
          strOut.append("'" + i + "'");
        // String parameter = "<Parameter name=\"" + token + "\"" + strAux + "/>";
        // String paramElement[] = { parameter, modifier };
        colNames.add(token);// paramElement);
        strValue = strValue.substring(j + 1, strValue.length());
        strAux = strValue.trim();
        if (strAux.length() > 0 && strAux.substring(0, 1).indexOf("'") > -1)
          strValue = strAux.substring(1, strValue.length());
      }
      i = strValue.indexOf("@");
    }
    strOut.append(strValue);
    return strOut.toString();
  }

  /**
   * @deprecated replaced by {@link #createFromClassicString(String)}
   */
  @Deprecated
  public Object createJsonValueFromClassicValueString(java.lang.String value) {
    if (getDomainType() instanceof PrimitiveDomainType) {
      return ((PrimitiveDomainType) getDomainType()).createFromString(value);
    } else {
      return value;
    }
  }

  /**
   * @deprecated replaced by {@link #createFromClassicString(String)}
   */
  @Deprecated
  public String formatValueFromSQL(java.lang.String value) {
    return value;
  }

  /**
   * @deprecated replaced by {@link #convertToClassicString(Object)}
   */
  @Deprecated
  public String formatValueToSQL(java.lang.String value) {
    return value;
  }

  protected boolean getSafeBoolean(Boolean value) {
    return value != null && value;
  }
}
