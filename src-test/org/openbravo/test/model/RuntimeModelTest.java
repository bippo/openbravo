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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.openbravo.base.model.Column;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.ModelSessionFactoryController;
import org.openbravo.base.model.NamingUtil;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Reference;
import org.openbravo.base.model.Table;
import org.openbravo.base.model.domaintype.BasePrimitiveDomainType;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the in-memory runtime model provided by the {@link ModelProvider}.
 * 
 * @see Entity
 * @see Property
 * @author iperdomo
 */

public class RuntimeModelTest extends BaseTest {

  private static final Logger log = Logger.getLogger(RuntimeModelTest.class);

  // cached list of all tables & columns as used by several tests
  private List<Table> allTables;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    allTables = getTables();
  }

  /**
   * Iterates over the model and prints it to the log.
   */
  public void testDumpModel() {
    for (Entity e : ModelProvider.getInstance().getModel()) {
      log.debug(">>>>>>>>>>>>>> " + e.getName() + " (" + e.getTableName() + ") <<<<<<<<<<<<<<<<<");
      for (Property p : e.getProperties()) {
        log.debug(p.getName() + " (" + p.getColumnName() + ")");
      }
    }
  }

  /**
   * Checks if there are tables without a PK in the model.
   */
  public void testPK() {
    final ArrayList<Table> tablesWithoutPK = new ArrayList<Table>();
    for (final Table t : allTables) {
      if (!t.isView() && t.getPrimaryKeyColumns().size() == 0) {
        tablesWithoutPK.add(t);
      }
    }
    if (tablesWithoutPK.size() != 0) {
      log.debug("Tables without primary keys defined:");
      for (final Table t2 : tablesWithoutPK) {
        log.debug(t2);
      }
    }
    assertEquals(0, tablesWithoutPK.size());
  }

  /**
   * Check the AD_Table.name for illegal characters.
   * 
   * See issue https://issues.openbravo.com/view.php?id=10624 Spaces in AD_TABLE.name should be
   * handled better, currently the entity name contains a space resulting in errors in HQL
   */
  public void testTableName() {
    for (final Table t : allTables) {
      final char[] chars = t.getName().toCharArray();
      for (char c : chars) {
        for (char illegalChar : NamingUtil.ILLEGAL_ENTITY_NAME_CHARS) {
          if (c == illegalChar) {
            fail("Name " + t.getName()
                + " has an illegal character (shown here between the > and <): >" + illegalChar
                + "<");
          }
        }
      }
    }
  }

  /**
   * Just checks that there is a {@link ModelProvider} and that it returns a model (a list of
   * {@link Entity} objects).
   */
  public void testModelProvider() {
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      log.debug("tablename: " + e.getTableName() + " -- classname: " + e.getClassName()
          + " -- mappingname: " + e.getName());
      for (final Property p : e.getProperties())
        log.debug("property: " + p.getColumnName() + " -- mapping: " + p.getName());
    }
    assertNotNull(ModelProvider.getInstance().getModel());
  }

  /**
   * Checks that entities have a unique name.
   * 
   * @see Entity#getName()
   */
  public void testUniqueTableMapping() {
    final List<String> mappings = new ArrayList<String>();
    boolean duplicated = false;
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      if (mappings.contains(e.getName())) {
        log.debug("Duplicated table mapping name: " + e.getName());
        duplicated = true;
        break;
      }
      mappings.add(e.getName());
    }
    assertFalse(duplicated);
  }

  /**
   * Checks that all names of properties are unique within an Entity.
   * 
   * @see Property#getName()
   */
  public void testUniqueColumnMapping() {
    boolean duplicated = false;
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      final List<String> propMappings = new ArrayList<String>();
      for (final Property p : e.getProperties()) {
        if (!p.isOneToMany() && propMappings.contains(p.getName())) {
          log.debug("Duplicated column mapping name: " + p.getName() + " -- column: "
              + p.getColumnName() + " -- table: " + e.getTableName());
          duplicated = true;
        }
        propMappings.add(p.getName());
      }
    }
    assertFalse(duplicated);
  }

  /**
   * Tests that each entity/table has only one PK.
   */
  public void testOnePK() {
    int total = 0;
    for (final Table t : allTables) {
      if (!t.isView() && t.getPrimaryKeyColumns().size() > 1) {
        log.debug("Table: " + t.getId() + " - " + t.getTableName());
        log.debug("  Columns : ");
        for (final Column c : t.getColumns())
          log.debug(c.getColumnName() + ", ");
        log.debug("\n");
        log.debug("    Keys: ");
        for (final Column c : t.getPrimaryKeyColumns())
          log.debug(c.getColumnName() + ", ");
        log.debug("\n");
        log.debug("    Identifiers: ");
        for (final Column c : t.getIdentifierColumns())
          log.debug(c.getColumnName() + ", ");
        log.debug("\n");
        total++;
      }
    }
    if (total != 0)
      log.debug(total + " tables with more than one primary key");
    assertEquals(0, total);
  }

  public void testIdentifiers() {
    final ArrayList<String> tables = new ArrayList<String>();
    for (final Table t : allTables) {
      if (!t.isView() && t.isActive() && t.getIdentifierColumns().size() == 0)
        tables.add(t.getTableName());
    }
    if (tables.size() != 0) {
      log.debug(tables.size() + " tables without Identifier columns");
      for (final String tableName : tables)
        log.debug(tableName);
    }
    assertEquals(0, tables.size());
  }

  /**
   * Tests that all non-one-to-many/not-one-to-one/not-composite id columns have a column
   */
  public void testColumnIdSet() {
    for (Entity entity : ModelProvider.getInstance().getModel()) {
      for (Property property : entity.getProperties()) {
        if (property.isOneToMany() || property.isOneToOne() || property.isCompositeId()) {
          continue;
        }
        assertNotNull("Property " + property + " does not have a columnid ", property.getColumnId());
      }
    }
  }

  /**
   * Tests that parent references are only allowed for specific reference types.
   * 
   * @see Column#getReference()
   * @see Reference
   */
  public void testIsParent() {
    final ArrayList<String> columns = new ArrayList<String>();

    for (final Table t : allTables) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && !c.getReference().getId().equals(Reference.TABLE)
            && !c.getReference().getId().equals(Reference.TABLEDIR)
            && !c.getReference().getId().equals(Reference.SEARCH)
            && !c.getReference().getId().equals(Reference.PRODUCT_ATTRIBUTE)) {
          columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0) {
      log.debug(columns.size() + " columns set as *isParent* errors (wrong reference): "
          + columns.toString());
    }
    assertEquals(0, columns.size());
  }

  /**
   * Tests that columns that has {@link Column#isParent()} on true are not of a primitive type.
   */
  public void testIsParent2() {
    final ArrayList<String> columns = new ArrayList<String>();

    for (final Table t : allTables) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && c.isPrimitiveType()) {
          columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0)
      log.debug(columns.size() + " columns set as *isParent* and are *primitive type*: "
          + columns.toString());
    assertEquals(0, columns.size());
  }

  /**
   * Checks that a column that has {@link Column#isParent()} on true has a table defined.
   */
  public void testIsParent3() {
    final ArrayList<String> columns = new ArrayList<String>();
    for (final Table t : allTables) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && c.getReference().getId().equals(Reference.TABLE)
            && c.getReferenceValue() == null) {
          columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0)
      log.debug(columns.size()
          + " columns set as *isParent* with reference *TABLE* and don't have table defined : "
          + columns.toString());
    assertEquals(0, columns.size());
  }

  /**
   * Checks that a column that has {@link Column#isParent()} finishes on _ID.
   */
  public void testIsParent4() {
    final ArrayList<String> columns = new ArrayList<String>();
    for (final Table t : allTables) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && c.getReference().getId().equals(Reference.TABLEDIR)) {
          final String obNamingConvention = c.getColumnName().substring(
              c.getColumnName().length() - 3);
          if (!obNamingConvention.equals("_ID"))
            columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0)
      log.debug(columns.size()
          + " columns set as *isParent* with reference *TABLEDIR* and column name don't finish with _ID: "
          + columns.toString());
    assertEquals(0, columns.size());
  }

  public void testPrimitiveDomainTypeDefaultMethods() {
    final CustomDomainType customDomainType = new CustomDomainType();
    final long testNumber = 121;
    final String strValue = customDomainType.convertToString(testNumber);
    final long result = (Long) customDomainType.createFromString(strValue);
    assertTrue(result == testNumber);
  }

  private static class CustomDomainType extends BasePrimitiveDomainType {

    @Override
    public Class<?> getPrimitiveType() {
      return Long.class;
    }

    @Override
    public String getXMLSchemaType() {
      return "ob:long";
    }
  }

  /**
   * Returns the tables in the database. Difference to ModelProvider.getTables() is that the latter
   * does not return tables which cannot be handled by DAL, i.e. tables which do not have any
   * primary columns.
   * 
   */
  private List<Table> getTables() {
    final SessionFactoryController sessionFactoryController = new ModelSessionFactoryController();
    final Session session = sessionFactoryController.getSessionFactory().openSession();
    final Transaction tx = session.beginTransaction();
    try {
      final List<Table> tables = ModelProvider.getInstance().list(session, Table.class);
      // read the columns in one query and assign them to the table
      final Criteria c = session.createCriteria(Column.class);
      c.addOrder(Order.asc("position"));
      @SuppressWarnings("unchecked")
      final List<Column> cols = c.list();
      for (final Column column : cols) {
        final Table table = column.getTable();
        table.getColumns().add(column);
      }
      return tables;
    } finally {
      log.debug("Closing session and sessionfactory used during model read");
      tx.commit();
      session.close();
      sessionFactoryController.getSessionFactory().close();
    }
  }

}
