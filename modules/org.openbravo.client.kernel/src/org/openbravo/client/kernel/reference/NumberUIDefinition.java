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

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.reference.UIDefinitionController.FormatDefinition;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the ui definition for numbers.
 * 
 * @author mtaal
 */
public abstract class NumberUIDefinition extends UIDefinition {

  @Override
  public String getFormEditorType() {
    return "OBNumberItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBNumberFilterItem";
  }

  @Override
  public String getFilterEditorProperties(Field field) {
    return ", filterOnKeypress: false" + super.getFilterEditorProperties(field);
  }

  @Override
  public String getTypeProperties() {
    final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) getDomainType();
    if (primitiveDomainType.getFormatId() != null) {
      final String formatId = primitiveDomainType.getFormatId();
      final FormatDefinition shortFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.SHORTFORMAT_QUALIFIER);
      final FormatDefinition normalFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.NORMALFORMAT_QUALIFIER);
      final FormatDefinition inputFormat = UIDefinitionController.getInstance()
          .getFormatDefinition(formatId, UIDefinitionController.INPUTFORMAT_QUALIFIER);
      final StringBuilder sb = new StringBuilder();
      if (inputFormat != null) {
        sb.append(getInputFormatters(inputFormat));
      } else if (shortFormat != null) {
        sb.append(getInputFormatters(shortFormat));
      }
      if (shortFormat != null) {
        // the edit formatter is actually used for creating the display
        // value in the input
        sb.append("editFormatter: " + getFormatter(shortFormat) + ",");
        sb.append("shortDisplayFormatter: " + getFormatter(shortFormat) + ",");
      }
      if (normalFormat != null) {
        sb.append("normalDisplayFormatter: " + getFormatter(normalFormat) + ",");
      }
      if (shortFormat != null) {
        sb.append("createClassicString: " + getFormatter(shortFormat) + ",");
      } else if (normalFormat != null) {
        sb.append("createClassicString: " + getFormatter(normalFormat) + ",");
      }

      return sb.toString();
    }
    return "";
  }

  private String getInputFormatters(FormatDefinition inputFormat) {
    final StringBuilder sb = new StringBuilder();
    sb.append("parseInput: function(value, field, component, record) {"
        + " if (OB.Utilities.Number.IsValidValueString(this, value)) {\n"
        + "return OB.Utilities.Number.OBMaskedToJS(value,'" + inputFormat.getDecimalSymbol()
        + "','" + inputFormat.getGroupingSymbol() + "');" + "\n} else {" + "\nreturn value;"
        + "\n}},");
    sb.append("'maskNumeric': '" + inputFormat.getFormat() + "', " + "'decSeparator': '"
        + inputFormat.getDecimalSymbol() + "'," + "'groupSeparator': '"
        + inputFormat.getGroupingSymbol() + "',");
    return sb.toString();
  }

  private String getFormatter(FormatDefinition formatDefinition) {
    return "function(value, field, component, record) {"
        + "return OB.Utilities.Number.JSToOBMasked(value," + "'" + formatDefinition.getFormat()
        + "'," + "'" + formatDefinition.getDecimalSymbol() + "'," + "'"
        + formatDefinition.getGroupingSymbol() + "', OB.Format.defaultGroupingSize);}";
  }

  @Override
  public String getFieldProperties(Field field) {
    String fieldProperties = super.getFieldProperties(field);
    try {
      JSONObject o = new JSONObject(fieldProperties);
      if (field.isDisplayed() != null && field.isDisplayed()) {
        o.put("width", "50%");
      }
      return o.toString();
    } catch (Exception e) { // ignore
      return fieldProperties;
    }
  }

  @Override
  public String getFieldProperties(Field field, boolean getValueFromSession) {
    try {
      JSONObject o = new JSONObject(super.getFieldProperties(field, getValueFromSession));
      if (field.isDisplayed() != null && field.isDisplayed()) {
        o.put("width", "50%");
      }
      // If a column has a numeric reference, and is required, and doesn't have a default, then
      // the default '0' is set
      if (!getValueFromSession && field.getColumn().isMandatory()
          && field.getColumn().getDefaultValue() == null
          && (!o.has("value") || o.get("value").equals(""))) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", 0);
        jsonObject.put("classicValue", 0);
        if (field.isDisplayed() != null && field.isDisplayed()) {
          o.put("width", "50%");
        }
        return jsonObject.toString();
      }
      return o.toString();
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  public String getFormat() {
    return "generalQtyEdition";
  }

  @Override
  public Object createFromClassicString(String value) {
    if (value == null || value.length() == 0) {
      return null;
    }
    String valueStr = value.toString();
    VariablesSecureApp variables = RequestContext.get().getVariablesSecureApp();
    valueStr = valueStr.replace(
        variables.getSessionValue("#GroupSeparator|" + getFormat()).substring(0, 1), "").replace(
        variables.getSessionValue("#DecimalSeparator|" + getFormat()).substring(0, 1), ".");
    return new BigDecimal(valueStr);
  }

  @Override
  public String convertToClassicString(Object value) {
    if (value == null) {
      return "";
    }
    final String valueStr = value.toString();
    VariablesSecureApp variables = RequestContext.get().getVariablesSecureApp();
    // only replace the decimal symbol
    return valueStr.replace(".", variables.getSessionValue("#DecimalSeparator|" + getFormat())
        .substring(0, 1));
  }

  public static class DecimalUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "float";
    }

    public String getFormat() {
      return "euroEdition";
    }
  }

  public static class BigDecimalUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "float";
    }

    public String getFormat() {
      return "generalQtyEdition";
    }
  }

  public static class IntegerUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "integer";
    }

    public String getFormat() {
      return "integerEdition";
    }

    @Override
    public Object createFromClassicString(String value) {
      if (value == null || value.length() == 0) {
        return null;
      }

      final BigDecimal superValue = (BigDecimal) super.createFromClassicString(value);
      return superValue.longValue();
    }
  }

  public static class AmountUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "float";
    }

    public String getFormat() {
      return "euroEdition";
    }
  }

  public static class QuantityUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "float";
    }

    public String getFormat() {
      return "qtyEdition";
    }
  }

  public static class PriceUIDefinition extends NumberUIDefinition {
    public String getParentType() {
      return "float";
    }

    public String getFormat() {
      return "priceEdition";
    }
  }
}
