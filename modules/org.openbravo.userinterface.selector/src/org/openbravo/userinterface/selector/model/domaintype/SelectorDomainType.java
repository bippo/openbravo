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
package org.openbravo.userinterface.selector.model.domaintype;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Column;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Table;
import org.openbravo.base.model.domaintype.BaseForeignKeyDomainType;

/**
 * Implements the domain type for a selector.
 * 
 * @author mtaal
 */
public class SelectorDomainType extends BaseForeignKeyDomainType {
  private static final Logger log = Logger.getLogger(SelectorDomainType.class);

  private Column column;
  private String tableName;

  @Override
  public List<Class<?>> getClasses() {
    List<Class<?>> listOfClasses = new ArrayList<Class<?>>();
    listOfClasses.add(SelectorDefinition.class);
    listOfClasses.add(DatasourceDefinition.class);
    return listOfClasses;
  }

  // Note: implementation should clean-up and close database connections or hibernate sessions. If
  // this is not done then the update.database task may hang when disabling foreign keys.
  public void initialize() {

    Session session = ModelProvider.getInstance().getSession();

    final Criteria criteria = session.createCriteria(SelectorDefinition.class);
    criteria.add(Restrictions.eq("referenceId", getReference().getId()));
    final List<?> list = criteria.list();
    if (list.isEmpty()) {
      // a base reference
      if (getReference().getParentReference() == null) {
        return;
      }
      log.error("No selector definition found for reference " + getReference());
      return;
    } else if (list.size() > 1) {
      log.warn("Reference " + getReference()
          + " has more than one selector definition, only one is really used");
    }
    final SelectorDefinition selectorDefinition = (SelectorDefinition) list.get(0);
    Table table = selectorDefinition.getTable();
    if (table == null && selectorDefinition.getDatasourceDefinition() != null) {
      table = selectorDefinition.getDatasourceDefinition().getTable();
    }
    if (table == null) {
      throw new IllegalStateException("The selector " + selectorDefinition.getIdentifier()
          + " is used in a foreign key reference but no table has been set");
    }
    tableName = table.getTableName();
    if (selectorDefinition.getColumn() == null) {
      final List<Column> columns = readColumns(session, table);
      for (Column col : columns) {
        if (col.isKey()) {
          column = col;
          break;
        }
      }
    } else {
      column = selectorDefinition.getColumn();
    }
  }

  @SuppressWarnings("unchecked")
  private List<Column> readColumns(Session session, Table table) {
    final Criteria c = session.createCriteria(Column.class);
    c.addOrder(Order.asc("position"));
    c.add(Restrictions.eq("table", table));
    return c.list();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.base.model.domaintype.BaseForeignKeyDomainType#getForeignKeyColumn(java.lang.
   * String)
   */
  public Column getForeignKeyColumn(String columnName) {
    return column;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.base.model.domaintype.BaseForeignKeyDomainType#getReferedTableName(java.lang.
   * String)
   */
  protected String getReferedTableName(String columnName) {
    return tableName;
  }
}
