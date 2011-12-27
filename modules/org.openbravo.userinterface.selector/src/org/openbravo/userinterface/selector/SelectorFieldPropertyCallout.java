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
package org.openbravo.userinterface.selector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * This call out computes the columnid of a Selector Field.
 * 
 * @author mtaal
 */
public class SelectorFieldPropertyCallout extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // printRequest(request);

    printPage(response, vars);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    final String selectorID = vars.getStringParameter("inpobuiselSelectorId", IsIDFilter.instance)
        .trim();
    final String property = vars.getStringParameter("inpproperty").trim();
    final Selector selector = OBDal.getInstance().get(Selector.class, selectorID);
    final Table table;
    if (selector.getTable() != null) {
      table = selector.getTable();
    } else if (selector.getObserdsDatasource() != null
        && selector.getObserdsDatasource().getTable() != null) {
      table = selector.getObserdsDatasource().getTable();
    } else {
      // no table don't do anything
      writeEmptyResult(response);
      return;
    }
    // some cases:
    // name --> name column of current table
    // bankAccount.bank.name --> bank column of bankAccount
    final Entity entity = ModelProvider.getInstance().getEntity(table.getName());

    Property foundProperty = null;
    if (property.equals(JsonConstants.IDENTIFIER)) {
      if (entity.getIdentifierProperties().isEmpty()) {
        writeEmptyResult(response);
        return;
      }
      foundProperty = entity.getIdentifierProperties().get(0);
    } else {
      final String[] parts = property.split("\\.");
      Entity currentEntity = entity;
      Property currentProperty;
      for (String part : parts) {
        if (part.length() == 0) {
          writeEmptyResult(response);
          return;
        }
        if (part.equals(JsonConstants.IDENTIFIER) || part.equals(JsonConstants.ID)) {
          if (foundProperty == null) {
            writeEmptyResult(response);
            return;
          }
          break;
        }
        currentProperty = currentEntity.getProperty(part);
        foundProperty = currentProperty;
        if (currentProperty.isPrimitive()) {
          break;
        }
        currentEntity = foundProperty.getTargetEntity();
      }
    }

    // retrieve the column id
    OBContext.setAdminMode();
    try {
      // get the table
      final Entity propertyEntity = foundProperty.getEntity();
      final Table propertyTable = OBDal
          .getInstance()
          .createQuery(Table.class,
              Table.PROPERTY_DBTABLENAME + "='" + propertyEntity.getTableName() + "'").list()
          .get(0);

      final OBCriteria<Column> columnCriteria = OBDal.getInstance().createCriteria(Column.class);
      columnCriteria.add(Restrictions.and(Restrictions.eq(Column.PROPERTY_TABLE, propertyTable),
          Restrictions.eq(Column.PROPERTY_DBCOLUMNNAME, foundProperty.getColumnName())));
      final List<Column> columnList = columnCriteria.list();
      if (columnList.isEmpty()) {
        writeEmptyResult(response);
        return;
      }

      final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

      final StringBuilder sb = new StringBuilder();
      sb.append("var calloutName='Selector_Field_Property_Callout';\n");
      final StringBuilder array = new StringBuilder();
      array.append("var respuesta = new Array(");
      array.append("new Array('inpadColumnId', \"" + columnList.get(0).getId() + "\")");
      // construct the array, where the first dimension contains the name
      // of the field to be changed and the second one our newly generated
      // value
      array.append(");");
      xmlDocument.setParameter("array", sb.toString() + array.toString());
      xmlDocument.setParameter("frameName", "appFrame");
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void writeEmptyResult(HttpServletResponse response) throws IOException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    final StringBuilder sb = new StringBuilder();
    sb.append("var calloutName='Selector_Field_Property_Callout';\n");
    final StringBuilder array = new StringBuilder();
    array.append("var respuesta = new Array(");
    array.append(");");
    xmlDocument.setParameter("array", sb.toString() + array.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
