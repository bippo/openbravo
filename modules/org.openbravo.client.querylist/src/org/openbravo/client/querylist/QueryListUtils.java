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
package org.openbravo.client.querylist;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.client.myob.WidgetClass;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Tab;

class QueryListUtils {
  private static final Logger log = Logger.getLogger(QueryListUtils.class);

  public static String getWidgetClassFields(WidgetClass widgetClass, IncludeIn includeIn) {
    try {
      final List<JSONObject> jsonFields = new ArrayList<JSONObject>();
      if (!widgetClass.getOBCQLWidgetQueryList().isEmpty()) {
        for (OBCQL_QueryColumn column : QueryListUtils.getColumns(widgetClass
            .getOBCQLWidgetQueryList().get(0))) {
          final JSONObject field = new JSONObject();
          final Reference reference;
          if (column.getReferenceSearchKey() != null) {
            reference = column.getReferenceSearchKey();
          } else {
            reference = column.getReference();
          }
          final UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
              reference);
          field.put("name", column.getDisplayExpression());
          field.put("type", uiDefinition.getName());
          if (uiDefinition.getName().equals("_id_10")) {
            field.put("filterOnKeypress", true);
          }
          field.put("filterEditorType", uiDefinition.getFilterEditorType());
          field.put("canFilter", column.isCanBeFiltered());
          if (column.isHasLink()) {
            field.put("isLink", true);
            field.put("OB_LinkExpression", column.getLinkExpression());
            final Tab tab = column.getTab();
            final Entity entity = ModelProvider.getInstance().getEntity(tab.getTable().getName());

            field.put("OB_TabId", tab.getId());
            field.put("OB_WindowId", tab.getWindow().getId());
            field.put("OB_keyColumnName", entity.getIdProperties().get(0).getColumnName());
            field.put("OB_entityName", entity.getName());
          }

          // Summarize option:
          if (column.getSummarizeType() != null) {
            field.put("showGridSummary", true);
            field.put("summaryFunction", column.getSummarizeType());
          } else {
            field.put("showGridSummary", false);
          }

          field.put("canExport", true);
          if ("E".equals(column.getIncludeIn())
              || ("M".equals(column.getIncludeIn()) && includeIn.equals(IncludeIn.WidgetView))) {
            field.put("hidden", true);
            field.put("showIf", "false");
          }

          try {
            final String fieldProperties = uiDefinition.getFieldProperties(null);
            if (fieldProperties != null && fieldProperties.trim().length() > 0) {
              final JSONObject fieldPropertiesObject = new JSONObject(fieldProperties);
              field.put("fieldProperties", fieldPropertiesObject);
            }
          } catch (NullPointerException e) {
            // handle non-careful implementors of ui definitions
            log.error("Error when processing column: " + column, e);
            // ignore this field properties for now
          }

          field.put("title", getColumnLabel(column));

          field.put("width", column.getWidth().toString() + "%");
          jsonFields.add(field);
        }
      }
      final JSONArray fields = new JSONArray(jsonFields);
      return fields.toString();
    } catch (Exception e) {
      throw new OBException(e);
    }

  }

  public static List<OBCQL_QueryColumn> getColumns(OBCQL_WidgetQuery query) {
    OBCriteria<OBCQL_QueryColumn> obcColumns = OBDal.getInstance().createCriteria(
        OBCQL_QueryColumn.class);
    obcColumns.add(Restrictions.eq(OBCQL_QueryColumn.PROPERTY_WIDGETQUERY, query));
    obcColumns.addOrderBy(OBCQL_QueryColumn.PROPERTY_SEQUENCENUMBER, true);
    return obcColumns.list();
  }

  enum IncludeIn {
    WidgetView(new String[] { "W" }), MaximizedView(new String[] { "W", "M" }), ExportedFile(
        new String[] { "W", "M", "E" });

    private String[] includedValues;

    private IncludeIn(String[] includedValues) {
      this.includedValues = includedValues;
    }

    public String[] getIncludedValues() {
      return includedValues;
    }

    public static IncludeIn getIncludeIn(String strViewMode) {
      if ("widget".equals(strViewMode)) {
        return WidgetView;
      } else if ("maximized".equals(strViewMode)) {
        return MaximizedView;
      } else if ("exported".equals(strViewMode)) {
        return ExportedFile;
      }
      return null;
    }
  }

  public static String getColumnLabel(OBCQL_QueryColumn column) {

    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
    for (QueryColumnTrl trl : column.getOBCQLQueryColumnTrlList()) {
      if (DalUtil.getId(trl.getLanguage()).equals(userLanguageId)) {
        return trl.getName();
      }
    }

    return column.getName();
  }
}
