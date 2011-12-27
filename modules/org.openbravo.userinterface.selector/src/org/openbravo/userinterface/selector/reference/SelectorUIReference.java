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

package org.openbravo.userinterface.selector.reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BuscadorData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.ComboTableQueryData;
import org.openbravo.erpCommon.utility.TableSQLData;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.reference.ui.UIReference;
import org.openbravo.reference.ui.UIReferenceUtility;
import org.openbravo.reference.ui.UITableDir;
import org.openbravo.userinterface.selector.Selector;
import org.openbravo.utils.FormatUtilities;

/**
 * Implements the User Interface part of the new customizable Reference. This part takes care of the
 * user interface in the grid and in the filter popup.
 * 
 * @author mtaal
 */
public class SelectorUIReference extends UIReference {

  public SelectorUIReference(String reference, String subreference) {
    super(reference, subreference);
  }

  /**
   * Generates the HTML code for the input used to display the reference in the filter popup
   */
  public void generateFilterHtml(StringBuffer strHtml, VariablesSecureApp vars, BuscadorData field,
      String strTab, String strWindow, ArrayList<String> vecScript, Vector<Object> vecKeys)
      throws IOException, ServletException {

    OBContext.setAdminMode();
    try {
      UIReferenceUtility.addUniqueElement(vecScript, strReplaceWith
          + "/../org.openbravo.client.kernel/OBCLKER_Kernel/StaticResources");
      strHtml.append("<td class=\"TextBox_ContentCell\">");
      final String inputName = FormatUtilities.replace(field.columnname);

      strHtml.append("<script>var sc_" + inputName + " = null;</script>");
      strHtml.append("<input type='hidden' name='inpParam" + inputName + "' id='" + inputName
          + "' value='" + field.value + "'");
      strHtml.append(" onreset='sc_" + inputName
          + ".resetSelector();' onchange='OB.Utilities.updateSmartClientComponentValue(this, sc_"
          + inputName + ".selectorField);' ");
      strHtml.append("></input>");
      strHtml.append("<script src='" + generateSelectorLink(field) + "'></script>");
      strHtml.append("</td>");
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String generateSelectorLink(BuscadorData field) {
    final StringBuilder sb = new StringBuilder();
    sb.append("../org.openbravo.client.kernel/OBUISEL_Selector/" + getSelectorID(field));
    sb.append("?columnName=" + field.columnname);
    sb.append("&disabled=false");

    if ((Integer.valueOf(field.fieldlength).intValue() > UIReferenceUtility.MAX_TEXTBOX_LENGTH)) {
      sb.append("&CssSize=TwoCells");
    } else {
      sb.append("&CssSize=OneCell");
    }

    sb.append("&DisplayLength=" + field.displaylength);
    sb.append("&required=false");
    return sb.toString();
  }

  private String getSelectorID(BuscadorData field) {
    final String hqlWhere = "reference.id=:reference or reference.id=:referenceValue";
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("reference", field.reference);
    parameters.put("referenceValue", field.referencevalue);

    final OBQuery<Selector> query = OBDal.getInstance().createQuery(Selector.class, hqlWhere);
    query.setNamedParameter("reference", field.reference);
    query.setNamedParameter("referenceValue", field.referencevalue);
    final List<Selector> selectors = query.list();
    if (selectors.isEmpty()) {
      throw new IllegalArgumentException("No Selectors defined for column " + field.adColumnId
          + " " + field.columnname);
    }
    return selectors.get(0).getId();
  }

  public void generateSQL(TableSQLData tableSql, Properties prop) throws Exception {
    OBContext.setAdminMode();
    try {
      Reference ref = OBDal.getInstance().get(Reference.class, subReference);
      if (!ref.getOBUISELSelectorList().isEmpty()) {
        final Selector selector = ref.getOBUISELSelectorList().get(0);
        final String tableName = getTableName(selector);
        if (tableName != null) {
          UITableDir tableDir = new UITableDir("19", null);
          prop.setProperty("ColumnNameSearch", tableName + "_ID");
          tableDir.identifier(tableSql, tableSql.getTableName(), prop,
              prop.getProperty("ColumnName"),
              tableSql.getTableName() + "." + prop.getProperty("ColumnName") + "_R", false);
        }
      } else {
        super.generateSQL(tableSql, prop);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String getTableName(Selector selector) {
    // TODO: add support for datasource field
    if (selector.getTable() != null) {
      if (selector.getValuefield() != null && !selector.isCustomQuery()) {
        final Entity startEntity = ModelProvider.getInstance().getEntity(
            selector.getTable().getName());
        final Property referedProperty = DalUtil.getPropertyFromPath(startEntity, selector
            .getValuefield().getProperty());
        return referedProperty.getEntity().getTableName();
      } else {
        return selector.getTable().getDBTableName();
      }
    } else if (selector.getObserdsDatasource() != null
        && selector.getObserdsDatasource().getTable() != null) {
      return selector.getObserdsDatasource().getTable().getDBTableName();
    }
    return null;
  }

  public void identifier(TableSQLData tableSql, String parentTableName, Properties field,
      String identifierName, String realName, boolean tableRef) throws Exception {
    if (field == null) {
      return;
    }
    OBContext.setAdminMode();
    try {
      Reference ref = OBDal.getInstance().get(Reference.class, subReference);
      if (!ref.getOBUISELSelectorList().isEmpty()) {
        final Selector selector = ref.getOBUISELSelectorList().get(0);
        final String tableName = getTableName(selector);

        if (tableName != null) {
          UITableDir tableDir = new UITableDir("19", null);
          field.setProperty("ColumnNameSearch", tableName + "_ID");
          tableDir.identifier(tableSql, parentTableName, field, identifierName, realName, tableRef);
        }
      } else {
        super.identifier(tableSql, parentTableName, field, identifierName, realName, tableRef);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void setComboTableDataIdentifier(ComboTableData comboTableData, String tableName,
      FieldProvider field) throws Exception {
    OBContext.setAdminMode();
    try {
      Reference ref = OBDal.getInstance().get(Reference.class, subReference);
      if (!ref.getOBUISELSelectorList().isEmpty()) {
        final Selector selector = ref.getOBUISELSelectorList().get(0);
        final String selectorTableName = getTableName(selector);
        if (selectorTableName != null) {
          String fieldName = field == null ? "" : field.getField("name");
          String parentFieldName = fieldName;
          String name = ((fieldName != null && !fieldName.equals("")) ? fieldName : comboTableData
              .getObjectName());

          String tableDirName;
          if (name.equalsIgnoreCase("createdby") || name.equalsIgnoreCase("updatedby")) {
            tableDirName = "AD_User";
            name = "AD_User_ID";
          } else {
            tableDirName = selectorTableName;
          }

          int myIndex = comboTableData.index++;

          ComboTableQueryData trd[] = ComboTableQueryData.identifierColumns(
              comboTableData.getPool(), tableDirName);
          comboTableData.addSelectField("td" + myIndex + "." + name, "ID");

          String tables = tableDirName + " td" + myIndex;
          if (tableName != null && !tableName.equals("") && parentFieldName != null
              && !parentFieldName.equals("")) {
            tables += " on " + tableName + "." + parentFieldName + " = td" + myIndex + "." + name
                + "\n";
            tables += "AND td" + myIndex + ".AD_Client_ID IN (" + comboTableData.getClientList()
                + ") \n";
            tables += "AND td" + myIndex + ".AD_Org_ID IN (" + comboTableData.getOrgList() + ")";
          } else {
            comboTableData.addWhereField(
                "td" + myIndex + ".AD_Client_ID IN (" + comboTableData.getClientList() + ")",
                "CLIENT_LIST");
            if (comboTableData.getOrgList() != null)
              comboTableData.addWhereField(
                  "td" + myIndex + ".AD_Org_ID IN (" + comboTableData.getOrgList() + ")",
                  "ORG_LIST");
          }
          comboTableData.addFromField(tables, "td" + myIndex);
          if (tableName == null || tableName.equals("")) {
            comboTableData.parseValidation();
            comboTableData.addWhereField("(td" + myIndex + ".isActive = 'Y' OR td" + myIndex + "."
                + name + " = (?) )", "ISACTIVE");
            comboTableData.addWhereParameter("@ACTUAL_VALUE@", "ACTUAL_VALUE", "ISACTIVE");
          }
          for (int i = 0; i < trd.length; i++)
            comboTableData.identifier("td" + myIndex, trd[i]);
          comboTableData.addOrderByField("2");
        }
      } else {
        super.setComboTableDataIdentifier(comboTableData, tableName, field);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
