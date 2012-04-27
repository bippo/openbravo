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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.client.kernel.reference.YesNoUIDefinition;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;

/**
 * Parses a dynamic expressions and extracts information, e.g. The expression is using a field or an
 * auxiliary input, etc. <br/>
 * The transformation of @Expression@ is the following:
 * <ul>
 * <li>@ColumnName@ are transformed into property name, e.g. @DocStatus@ into <b>documentStatus</b></li>
 * <li>@AuxiliarInput@ is transformed just removes the <b>@</b>, e.g. @FinancialManagementDep@ into
 * <b>FinancialManagementDep</b></li>
 * </ul>
 * 
 */
public class DynamicExpressionParser {

  private static final String[][] COMPARATIONS = { { "==", " === " }, { "=", " === " },
      { "!", " !== " }, { "^", " !== " }, { "-", " !== " } };

  private static final String[][] UNIONS = { { "|", " || " }, { "&", " && " } };

  private static final String TOKEN_PREFIX = "context.";
  private static Map<String, String> exprToJSMap;
  static {
    exprToJSMap = new HashMap<String, String>();
    exprToJSMap.put("'Y'", "true");
    exprToJSMap.put("'N'", "false");
  }

  private List<Field> fieldsInExpression = new ArrayList<Field>();
  private List<AuxiliaryInput> auxInputsInExpression = new ArrayList<AuxiliaryInput>();
  private List<String> sessionAttributesInExpression = new ArrayList<String>();

  private String code;
  private Tab tab;
  private StringBuffer jsCode;
  private ApplicationDictionaryCachedStructures cachedStructures;

  public DynamicExpressionParser(String code, Tab tab) {
    this.code = code;
    this.tab = tab;
    parse();
  }

  public DynamicExpressionParser(String code, Tab tab,
      ApplicationDictionaryCachedStructures cachedStructures) {
    this.cachedStructures = cachedStructures;
    this.code = code;
    this.tab = tab;
    parse();
  }

  /*
   * Note: This method was partially copied from WadUtility.
   */
  public void parse() {
    StringTokenizer st = new StringTokenizer(code, "|&", true);
    String token, token2;
    String strAux;
    jsCode = new StringBuffer();
    while (st.hasMoreTokens()) {
      strAux = st.nextToken().trim();
      int i[] = getFirstElement(UNIONS, strAux);
      if (i[0] != -1) {
        strAux = strAux.substring(0, i[0]) + UNIONS[i[1]][1]
            + strAux.substring(i[0] + UNIONS[i[1]][0].length());
      }

      int pos[] = getFirstElement(COMPARATIONS, strAux);
      token = strAux;
      token2 = "";
      if (pos[0] >= 0) {
        token = strAux.substring(0, pos[0]);
        token2 = strAux.substring(pos[0] + COMPARATIONS[pos[1]][0].length(), strAux.length());
        strAux = strAux.substring(0, pos[0]) + COMPARATIONS[pos[1]][1]
            + strAux.substring(pos[0] + COMPARATIONS[pos[1]][0].length(), strAux.length());
      }

      DisplayLogicElement leftPart = getDisplayLogicText(token, false, false);
      jsCode.append(leftPart.text);

      if (pos[0] >= 0) {
        jsCode.append(COMPARATIONS[pos[1]][1]);
      }

      DisplayLogicElement rightPart = getDisplayLogicText(token2,
          leftPart.text.contains("currentValues"), leftPart.isBoolean);
      jsCode.append(rightPart.text);
    }
  }

  /**
   * Gets a JavaScript expression based on the dynamic expression, e.g @SomeColumn@!'Y' results in
   * currentValues.someColumn !== true.<br/>
   * Note: Field comparison with <b>'Y'</b> or <b>'N'</b> are transformed in <b>true</b> or
   * <b>false</b>
   * 
   * @return A JavaScript expression
   */
  public String getJSExpression() {
    return jsCode.toString();
  }

  /**
   * @see DynamicExpressionParser#getJSExpression()
   */
  public String toString() {
    return getJSExpression();
  }

  /**
   * Returns the list of Fields used in the dynamic expression
   * 
   */
  public List<Field> getFields() {
    return fieldsInExpression;
  }

  /**
   * Returns the list of session attribute names used in the dynamic expression
   * 
   */
  public List<String> getSessionAttributes() {
    return sessionAttributesInExpression;
  }

  /**
   * Transform values into JavaScript equivalent, e.g. <b>'Y'</b> into <b>true</b>, based in a
   * defined map. Often used in dynamic expression comparisons
   * 
   * If the value is enclosed between brackets, it is extracted, translated and enclosed again
   * 
   * @see DynamicExpressionParserTest
   * 
   * @param value
   *          A string expression like <b>'Y'</b>
   * @return A equivalent value in JavaScript or the same string if has no mapping value
   */
  private String transformValue(String value) {
    if (value == null) {
      return null;
    }
    String removeBracketsRegExp = "[\\[\\(]*(.*?)[\\)\\]]*";
    Pattern pattern = Pattern.compile(removeBracketsRegExp);
    Matcher matcher = pattern.matcher(value);
    String transformedValueWithBrackets = null;
    // It is always matched: zero or plus opening brackets, followed by any string, follow by zero
    // or plus closing brackets
    if (matcher.matches()) {
      // Extracts the value
      String valueWithoutBrackets = matcher.group(1);
      // Transforms the value
      String transformedValueWithoutBrackets = exprToJSMap.get(valueWithoutBrackets) != null ? exprToJSMap
          .get(valueWithoutBrackets) : valueWithoutBrackets;
      // Re-encloses the value
      transformedValueWithBrackets = value.replace(valueWithoutBrackets,
          transformedValueWithoutBrackets);
    }
    return transformedValueWithBrackets;
  }

  /*
   * This method was partially copied from WadUtility.
   */
  private DisplayLogicElement getDisplayLogicText(String token, boolean transformValue,
      boolean boolLeftToken) {
    StringBuffer strOut = new StringBuffer();
    String localToken = token;
    boolean boolToken = false;
    int i = localToken.indexOf("@");
    while (i != -1) {
      strOut.append(localToken.substring(0, i));
      localToken = localToken.substring(i + 1);
      i = localToken.indexOf("@");
      if (i != -1) {
        String strAux = localToken.substring(0, i);
        localToken = localToken.substring(i + 1);
        DisplayLogicElement displayLogicElement = getDisplayLogicTextTranslate(strAux);
        // It needn't boolean transformation as it is a token like @column@
        strOut.append(displayLogicElement.text);
        boolToken = boolToken || displayLogicElement.isBoolean;
      }
      i = localToken.indexOf("@");
    }
    // Do boolean transformation in case comparison left member is a boolean column
    strOut.append(transformValue && boolLeftToken ? transformValue(localToken) : localToken);
    return new DisplayLogicElement(strOut.toString(), boolToken);
  }

  /*
   * This method is a different reimplementation of an equivalent method in WadUtility
   */
  private DisplayLogicElement getDisplayLogicTextTranslate(String token) {
    if (token == null || token.trim().equals(""))
      return new DisplayLogicElement("", false);
    List<Field> fields;
    List<AuxiliaryInput> auxIns;
    try {
      if (cachedStructures == null) {
        cachedStructures = WeldUtils
            .getInstanceFromStaticBeanManager(ApplicationDictionaryCachedStructures.class);
      }
      fields = cachedStructures.getFieldsOfTab(tab.getId());
      auxIns = cachedStructures.getAuxiliarInputList(tab.getId());
    } catch (NullPointerException e) {
      fields = tab.getADFieldList();
      auxIns = tab.getADAuxiliaryInputList();
    }
    for (Field field : fields) {
      if (field.getColumn() == null) {
        continue;
      }
      if (token.equalsIgnoreCase(field.getColumn().getDBColumnName())) {
        fieldsInExpression.add(field);
        final String fieldName = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn())
            .getName();

        UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(
            field.getColumn().getId());

        return new DisplayLogicElement("OB.Utilities.getValue(currentValues,'" + fieldName + "')",
            uiDef instanceof YesNoUIDefinition);
      }
    }
    for (AuxiliaryInput auxIn : auxIns) {
      if (token.equalsIgnoreCase(auxIn.getName())) {
        auxInputsInExpression.add(auxIn);
        return new DisplayLogicElement(TOKEN_PREFIX + auxIn.getName(), false);
      }
    }
    sessionAttributesInExpression.add(token);
    return new DisplayLogicElement(TOKEN_PREFIX
        + (token.startsWith("#") ? token.replace("#", "_") : token), false);
  }

  /*
   * This method was partially copied from WadUtility.
   */
  private static int[] getFirstElement(String[][] array, String token) {
    int min[] = { -1, -1 }, aux;
    for (int i = 0; i < array.length; i++) {
      aux = token.indexOf(array[i][0]);
      if (aux != -1 && (aux < min[0] || min[0] == -1)) {
        min[0] = aux;
        min[1] = i;
      }
    }
    return min;
  }

  private class DisplayLogicElement {
    boolean isBoolean;
    String text;

    public DisplayLogicElement(String text, boolean isBoolean) {
      this.text = text;
      this.isBoolean = isBoolean;
    }
  }
}
