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

package org.openbravo.test.modularity;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.QueryTimeoutException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.GenericJDBCException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.DataPackage;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDBPrefix;
import org.openbravo.test.base.BaseTest;

/**
 * This test case checks that table names are correctly checked when inserting them into DB. It must
 * be checkes the table name starts with the db prefix for the module and it is not possible to add
 * tables with non-allowed names.
 * 
 * @author alostale
 */
public class TableNameTest extends BaseTest {

  private static final Logger log = Logger.getLogger(TableNameTest.class);

  /**
   * Creates a test module to work with it in later tests
   */
  public void testCreateModule() {
    setSystemAdministratorContext();
    Module module = OBProvider.getInstance().get(Module.class);
    module.setName("Test-table-names");
    module.setJavaPackage("org.openbravo.test.tablename");
    module.setVersion("1.0.0");
    module.setDescription("Testing dbprefixes");
    module.setInDevelopment(true);
    OBDal.getInstance().save(module);

    ModuleDBPrefix dbPrefix = OBProvider.getInstance().get(ModuleDBPrefix.class);
    dbPrefix.setName("TEST1");
    dbPrefix.setModule(module);
    OBDal.getInstance().save(dbPrefix);

    DataPackage pack = OBProvider.getInstance().get(DataPackage.class);
    pack.setJavaPackage("org.openbravo.test.tablename.data");
    pack.setName("org.openbravo.test.tablename.data");
    pack.setModule(module);
    OBDal.getInstance().save(pack);
    commitTransaction();
  }

  /**
   * Creates a table that is valid, it has a valid name according with the db prefix for the module
   * it is assigned to
   */
  public void testCreateTable1() {
    setSystemAdministratorContext();
    Table table = OBProvider.getInstance().get(Table.class);
    table.setName("TEST1_Table1");
    table.setDBTableName("TEST1_Table1");
    table.setDataAccessLevel("1");
    table.setJavaClassName("Table1");

    table.setDataPackage(getPackage("org.openbravo.test.tablename.data"));
    OBDal.getInstance().save(table);
    commitTransaction();
  }

  /**
   * It tries to insert a table with a non-valid name prefix, it is tested that DB raises an
   * exception and thus the table is not inserted.
   */
  public void testCreateTable2() {
    setSystemAdministratorContext();
    Table table = OBProvider.getInstance().get(Table.class);
    table.setName("TEST_Table2");
    table.setDBTableName("TEST_Table2");
    table.setDataAccessLevel("1");
    table.setJavaClassName("Table2");

    table.setDataPackage(getPackage("org.openbravo.test.tablename.data"));
    OBDal.getInstance().save(table);
    try {
      // force dal commit to throw exception
      commitTransaction();
      fail("Saved table but it shouldn't be");
    } catch (GenericJDBCException e) { // thrown on pgsql
      rollback();
    } catch (QueryTimeoutException e) { // thrown on oracle
      rollback();
    }
  }

  /**
   * Same test as testCreateTable2 but trying to insert the table in a package that for core module,
   * it should not insert anything
   */
  public void testCreateTable3() {
    setSystemAdministratorContext();
    Table table = OBProvider.getInstance().get(Table.class);
    table.setName("TEST1_Table3");
    table.setDBTableName("TEST1_Table3");
    table.setDataAccessLevel("1");
    table.setJavaClassName("Table3");

    table.setDataPackage(getPackage("org.openbravo.model.ad.module"));
    OBDal.getInstance().save(table);
    try {
      // force dal commit to throw exception
      commitTransaction();
      fail("Saved table but it shouldn't be");
    } catch (GenericJDBCException e) { // thrown on pgsql
      rollback();
    } catch (QueryTimeoutException e) { // thrown on oracle
      rollback();
    }
  }

  /**
   * This test tryies to change the package for the table created in testCreateTable1 to a package
   * in core, this should fail because the naming rules are not filled
   */
  public void testChangePackage() {
    setSystemAdministratorContext();
    OBCriteria<Table> obCriteria = OBDal.getInstance().createCriteria(Table.class);
    obCriteria.add(Restrictions.eq(Module.PROPERTY_NAME, "TEST1_Table1"));
    List<Table> tables = obCriteria.list();
    Table table = tables.get(0);
    table.setDataPackage(getPackage("org.openbravo.model.ad.module"));
    OBDal.getInstance().save(table);
    try {
      // force dal commit to throw exception
      commitTransaction();
      fail("Saved table but it shouldn't be");
    } catch (GenericJDBCException e) { // thrown on pgsql
      rollback();
    } catch (QueryTimeoutException e) { // thrown on oracle
      rollback();
    }
  }

  /**
   * Removes all created objects from database.
   */
  public void testCleanUp() {
    setSystemAdministratorContext();
    OBCriteria<Module> obCriteria = OBDal.getInstance().createCriteria(Module.class);
    obCriteria.add(Restrictions.eq(Module.PROPERTY_NAME, "Test-table-names"));
    List<Module> modules = obCriteria.list();

    log.debug("deleting " + modules.size() + " modules");
    for (Module module : modules) {
      log.debug("*deleting module:" + module.toString());

      OBCriteria<ModuleDBPrefix> obCritPrefix = OBDal.getInstance().createCriteria(
          ModuleDBPrefix.class);
      obCritPrefix.add(Restrictions.eq(ModuleDBPrefix.PROPERTY_MODULE, module));
      List<ModuleDBPrefix> dbp = obCritPrefix.list();
      log.debug("  -deleting " + dbp.size() + " prefixes");
      for (ModuleDBPrefix p : dbp) {
        log.debug("     +deleting dbprefix:" + p.toString());
        OBDal.getInstance().remove(p);
      }

      OBCriteria<DataPackage> obCritPack = OBDal.getInstance().createCriteria(DataPackage.class);
      obCritPack.add(Restrictions.eq(ModuleDBPrefix.PROPERTY_MODULE, module));
      List<DataPackage> packs = obCritPack.list();
      log.debug("  -deleting " + packs.size() + " packs");
      for (DataPackage p : packs) {
        log.debug("     +deleting pack:" + p.toString());
        OBCriteria<Table> obCritTable = OBDal.getInstance().createCriteria(Table.class);
        obCritTable.add(Restrictions.eq(Table.PROPERTY_DATAPACKAGE, p));
        List<Table> tables = obCritTable.list();
        log.debug("     -deleting " + tables.size() + " tables");
        for (Table t : tables) {
          log.debug("        +deleting table:" + t.toString());
          OBDal.getInstance().remove(t);
        }

        OBDal.getInstance().remove(p);
      }

      OBDal.getInstance().remove(module);
      commitTransaction();
    }

  }

  private DataPackage getPackage(String name) {
    OBCriteria<DataPackage> obCriteria = OBDal.getInstance().createCriteria(DataPackage.class);
    obCriteria.add(Restrictions.eq(DataPackage.PROPERTY_NAME, name));
    List<DataPackage> pack = obCriteria.list();
    DataPackage p = pack.get(0);
    log.debug("Package: " + p.getName());
    return p;
  }
}
