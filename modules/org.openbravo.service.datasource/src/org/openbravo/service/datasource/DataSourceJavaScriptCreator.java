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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.service.datasource.DataSourceProperty.RefListEntry;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.web.BaseWebServiceServlet;

/**
 * A utility class to create javascript for the datasource of a certain {@link Entity}. The
 * properties of the entity ({@link Entity#getProperties()}) are translated into datasource fields.
 * 
 * @deprecated class should not be used and is not used
 * @author mtaal
 */
@Deprecated
public class DataSourceJavaScriptCreator extends BaseWebServiceServlet {

  private static final long serialVersionUID = 1L;
  private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");

  /**
   * Create javascript to create a datasource on the client.
   * 
   * @param name
   *          the name of the datasource
   * @param entityProperties
   *          the properties of this entity are used for the data source fields
   * @return the javascript string
   */
  public String doCreateJavaScript(String name, List<Property> entityProperties, String dataUrl) {
    final StringBuilder sb = new StringBuilder();
    sb.append("if (isc.DataSource.get('" + name + "') == null) {");

    // TODO: move the data url to a config parameter
    sb.append("isc.DataSource.registerDataSource(isc.RestDataSource.create({ID:'" + name + "'");
    sb.append(", dataURL:'" + dataUrl + name + "'");
    sb.append(", recordXPath: '/response/data', dataFormat: 'json'");

    sb.append(", operationBindings:  [");
    sb.append("{operationType: 'fetch', dataProtocol: 'getParams'}");
    sb.append(", {operationType: 'add', dataProtocol: 'postMessage'}");
    sb.append(", {operationType: 'remove', dataProtocol: 'getParams', requestProperties:{httpMethod: 'DELETE'}}");
    sb.append(", {operationType: 'update', dataProtocol: 'postMessage', requestProperties:{httpMethod: 'PUT'}}");
    sb.append("]");

    sb.append(", fields:[ ");
    boolean fieldAdded = false;
    for (Property prop : entityProperties) {
      if (prop.isOneToMany()) {
        continue;
      }
      if (fieldAdded) {
        sb.append(", ");
      }
      sb.append(getFieldFromProperty(prop));
      fieldAdded = true;
    }
    sb.append("]");

    sb.append("}));");
    sb.append("}");
    return sb.toString();
  }

  private String getFieldFromProperty(Property property) {
    final StringBuilder sb = new StringBuilder("{");
    sb.append("name: \"" + property.getName() + "\"");
    sb.append(", type: \"" + getTypeFromProperty(property) + "\"");

    if (property.isId()) {
      sb.append(", hidden: true");
      sb.append(", primaryKey: true");
    }

    // note boolean can not be mandatory for SC as it gives a
    // client-side validation error when not set (so only checked
    // booleans are allowed).
    if (property.isMandatory() && property.isUpdatable() && !property.isAuditInfo()
        && !property.isBoolean()) {
      sb.append(", required: true");
    }

    if (!property.isUpdatable() || property.isAuditInfo()) {
      sb.append(", canSave: false");
    }

    if (property.getFieldLength() > 0 && !property.isId()) {
      sb.append(", length: " + property.getFieldLength());
    }

    // note: this one should actually never be used as it is
    // not translated
    sb.append(", title: \"" + property.getName() + "\"");

    // an enum
    if (property.getAllowedValues() != null && property.getAllowedValues().size() > 0) {
      sb.append(", valueMap: {");
      boolean multipleValue = false;
      final List<RefListEntry> allowedValues = DataSourceProperty.createValueMap(
          property.getAllowedValues(), property.getDomainType().getReference().getId());
      for (RefListEntry entry : allowedValues) {
        final String label = entry.getLabel();

        if (multipleValue) {
          sb.append(", ");
        }
        sb.append("\"" + escape(entry.getValue()) + "\": \"" + escape(label) + "\"");
        multipleValue = true;
      }
      sb.append("}");
    }

    if (!property.isPrimitive()) {
      if (!property.isId()) {
        sb.append(", hidden: true");
      }
      sb.append(", foreignKey: \"" + property.getTargetEntity().getName() + ".id\"");
    }
    sb.append("}");

    if (!property.isPrimitive()) {
      sb.append(", " + getReferenceProperty(property, JsonConstants.ID));
      sb.append(", " + getReferenceProperty(property, JsonConstants.IDENTIFIER));
    }

    return sb.toString();
  }

  protected String getReferenceProperty(Property property, String subPropName) {
    final StringBuilder sb = new StringBuilder("{");
    sb.append("name: \"" + property.getName() + "." + subPropName + "\"");
    sb.append(", type: \"text\"");
    sb.append(", hidden: true");
    if (property.isMandatory()) {
      sb.append(", required: true");
    }

    if (!property.isUpdatable() || property.isAuditInfo()) {
      sb.append(", canSave: false");
    }

    if (subPropName.equals(JsonConstants.IDENTIFIER)) {
      // sb.append(", editorType: 'comboBox'");
      // sb.append(", editorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.ID + "'");
      // sb.append(", optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
      // sb.append(", filterEditorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.IDENTIFIER + "', editorType: 'comboBox'");
      // sb.append(", required: false, optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
    } else if (subPropName.equals(JsonConstants.ID)) {
      // sb.append(", editorType: 'comboBox'");
      // sb.append(", editorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.ID + "'");
      // sb.append(", optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
      // sb.append(", filterEditorProperties: {displayField: '" + JsonConstants.IDENTIFIER
      // + "', valueField: '" + JsonConstants.ID + "', editorType: 'comboBox'");
      // sb.append(", required: false, optionDataSource: '" + property.getTargetEntity().getName()
      // + "', fetchDelay: 300, autoFetchData: false, selectOnFocus: true, dataPageSize : 15}");
    } else {
      // sb.append(", filterEditorProperties: {required: false}");
    }

    sb.append(", valueXPath: \"" + property.getName() + "/" + subPropName + "\"");
    sb.append(", title: \"" + property.getName() + "\"");
    sb.append("}");

    return sb.toString();
  }

  protected String getTypeFromProperty(Property property) {
    if (property.isBoolean()) {
      return "boolean";
    }

    if (!property.isPrimitive()) {
      return "text";
    }

    if (property.getPrimitiveObjectType() == String.class) {
      return "text";
    } else if (property.getPrimitiveObjectType() == BigDecimal.class) {
      return "float";
    } else if (property.isNumericType()) {
      return "integer";
    } else if (property.getAllowedValues() != null && property.getAllowedValues().size() > 0) {
      return "enum";
    } else if (property.getPrimitiveObjectType() == Date.class) {
      return "date";
    } else if (property.getPrimitiveObjectType() == Timestamp.class) {
      return "datetime";
    } else if (property.getPrimitiveObjectType() == byte[].class
        || property.getPrimitiveObjectType() == Byte[].class) {
      return "image";
    }
    throw new OBException("Type of property " + property + " not supported, primitive object type "
        + property.getPrimitiveObjectType());
  }

  // private void handleException(Throwable t, HttpServletResponse response) {
  // t.printStackTrace(System.err);
  // }

  private String escape(String value) {
    return QUOTE_PATTERN.matcher(value).replaceAll("\\\"");
  }
}
