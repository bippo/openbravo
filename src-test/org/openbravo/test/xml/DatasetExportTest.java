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

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetColumn;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.service.db.DataExportService;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the {@link DataExportService} specifically for exports driven by a {@link DataSet}. The
 * data set defines which objects to export and which properties of an object are exported.
 * 
 * See also the following mantis issues:
 * 
 * 8517: Export reference data ignores definition of exportable columns
 * 
 * 8516: Export of dataset, ignores 'exclude audit info' on data set table level
 * 
 * @author mtaal
 */

public class DatasetExportTest extends BaseTest {

  /**
   * Tests include all columns except for the audit info columns.
   * 
   * @see DataSetTable#setExcludeAuditInfo(Boolean)
   * @see DataSetTable#setIncludeAllColumns(Boolean)
   */
  public void testIncludeAllExcludeAudit() {
    setSystemAdministratorContext();
    final DataSet ds = createDataSet();
    for (DataSetTable dst : ds.getDataSetTableList()) {
      dst.setExcludeAuditInfo(true);
      dst.setIncludeAllColumns(true);
    }
    final String xml = DataExportService.getInstance().exportDataSetToXML(ds);
    checkPropsPresent(ds, xml, false, false, new String[] {});
  }

  /**
   * Tests include all columns
   * 
   * @see DataSetTable#setExcludeAuditInfo(Boolean)
   * @see DataSetTable#setIncludeAllColumns(Boolean)
   */
  public void testIncludeAll() {
    setSystemAdministratorContext();
    final DataSet ds = createDataSet();
    for (DataSetTable dst : ds.getDataSetTableList()) {
      dst.setExcludeAuditInfo(false);
      dst.setIncludeAllColumns(true);
    }
    final String xml = DataExportService.getInstance().exportDataSetToXML(ds);
    checkPropsPresent(ds, xml, true, false, new String[] {});
  }

  /**
   * Tests include all columns, including children
   * 
   * @see DataSetTable#setExcludeAuditInfo(Boolean)
   * @see DataSetTable#setIncludeAllColumns(Boolean)
   * @see DataSetTable#isBusinessObject()
   */
  public void testIncludeAllPlusChildren() {
    setSystemAdministratorContext();
    final DataSet ds = createDataSet();
    for (DataSetTable dst : ds.getDataSetTableList()) {
      dst.setExcludeAuditInfo(false);
      dst.setIncludeAllColumns(true);
      dst.setBusinessObject(true);
    }
    final String xml = DataExportService.getInstance().exportDataSetToXML(ds);
    checkPropsPresent(ds, xml, true, true, new String[] {});
    assertTrue(xml.indexOf("<" + Order.PROPERTY_ORDERLINELIST) != -1);
    assertTrue(xml.indexOf("<" + Organization.PROPERTY_ORGANIZATIONINFORMATIONLIST) != -1);
    assertTrue(xml.indexOf("<" + OrderLine.ENTITY_NAME + " id") != -1);
    assertTrue(xml.indexOf("<" + OrganizationInformation.ENTITY_NAME + " id") != -1);
  }

  /**
   * Tests include all columns, except for a few excluded in the {@link DataSetColumn}.
   * 
   * @see DataSetTable#setExcludeAuditInfo(Boolean)
   * @see DataSetTable#setIncludeAllColumns(Boolean)
   * @see DataSetTable#getDataSetColumnList()
   * @see DataSetColumn#isExcluded()
   */
  public void testIncludeAllExcludeSpecificColumn() {
    setSystemAdministratorContext();
    final DataSet ds = createDataSet();
    for (DataSetTable dst : ds.getDataSetTableList()) {
      dst.setExcludeAuditInfo(true);
      dst.setIncludeAllColumns(true);
      dst.setBusinessObject(false);
    }

    // exclude the Order.project and Order.selfService
    final DataSetTable dst = ds.getDataSetTableList().get(0);
    assertTrue(dst.getTable().getDBTableName().toLowerCase().equals("c_order"));
    dst.getDataSetColumnList().add(
        createDataSetColumn(dst, getProperty(Order.ENTITY_NAME, Order.PROPERTY_PROJECT), true));
    dst.getDataSetColumnList().add(
        createDataSetColumn(dst, getProperty(Order.ENTITY_NAME, Order.PROPERTY_SELFSERVICE), true));

    String xml = DataExportService.getInstance().exportDataSetToXML(ds);
    checkPropsPresent(ds, xml, false, false, new String[] { Order.PROPERTY_PROJECT,
        Order.PROPERTY_SELFSERVICE });
  }

  /**
   * Tests excludes all columns, except for a few included in the {@link DataSetColumn}.
   * 
   * @see DataSetTable#setExcludeAuditInfo(Boolean)
   * @see DataSetTable#setIncludeAllColumns(Boolean)
   * @see DataSetTable#getDataSetColumnList()
   * @see DataSetColumn#isExcluded()
   */
  public void testExcludeAllIncludeSpecificColumn() {
    setSystemAdministratorContext();
    final DataSet ds = createDataSet();
    for (DataSetTable dst : ds.getDataSetTableList()) {
      dst.setExcludeAuditInfo(true);
      dst.setIncludeAllColumns(false);
      dst.setBusinessObject(false);
    }

    // exclude the Order.project and Order.selfService
    final DataSetTable dst = ds.getDataSetTableList().get(0);
    assertTrue(dst.getTable().getDBTableName().toLowerCase().equals("c_order"));
    dst.getDataSetColumnList().add(
        createDataSetColumn(dst, getProperty(Order.ENTITY_NAME, Order.PROPERTY_PROJECT), false));
    dst.getDataSetColumnList()
        .add(
            createDataSetColumn(dst, getProperty(Order.ENTITY_NAME, Order.PROPERTY_SELFSERVICE),
                false));

    final List<String> excludeList = new ArrayList<String>();
    addAllPropertiesExcept(Order.ENTITY_NAME, excludeList, new String[] { Order.PROPERTY_PROJECT,
        Order.PROPERTY_SELFSERVICE });
    addAllPropertiesExcept(Organization.ENTITY_NAME, excludeList, new String[] {
        Order.PROPERTY_PROJECT, Order.PROPERTY_SELFSERVICE });

    String xml = DataExportService.getInstance().exportDataSetToXML(ds);
    checkPropsPresent(ds, xml, false, false, excludeList.toArray(new String[excludeList.size()]));
  }

  private void checkPropsPresent(DataSet ds, String xml, boolean auditInfo,
      boolean includeChildren, String[] excluded) {
    final List<String> excludedList = Arrays.asList(excluded);
    for (DataSetTable dst : ds.getDataSetTableList()) {
      final Entity e = ModelProvider.getInstance().getEntityByTableName(
          dst.getTable().getDBTableName());
      for (Property p : e.getProperties()) {
        if (p.isClientOrOrganization()) {
          continue;
        }
        if (p.isId()) { // can not really be excluded
          continue;
        }
        if (p.getName().equals("orderLineTaxList")) {
          continue;
        }
        // add the < in front to prevent accidental matches
        final String xmlCheckName = "<" + p.getName();
        if (excludedList.contains(p.getName())) {
          assertFalse("Fail: property " + p.getName() + " present in xml",
              xml.indexOf(xmlCheckName) != -1);
        } else if (!auditInfo && p.isAuditInfo()) {
          assertFalse("Fail: property " + p.getName() + " present in xml",
              xml.indexOf(xmlCheckName) != -1);
        } else if ((!includeChildren && p.isOneToMany() && p.isChild())) {
          assertFalse("Fail: property " + p.getName() + " present in xml",
              xml.indexOf(xmlCheckName) != -1);
        } else if (p.isOneToMany() && !p.isChild()) {
          // OneToMany columns which are not child should never be in the xml file
          assertFalse("Fail: property " + p.getName() + " present in xml",
              xml.indexOf(xmlCheckName) != -1);
        } else {
          assertTrue("Fail: property " + p.getName() + " NOT present in xml",
              xml.indexOf(xmlCheckName) != -1);
        }
      }
    }
  }

  private void addAllPropertiesExcept(String entityName, List<String> propNames, String[] except) {
    final List<String> exceptList = Arrays.asList(except);
    final Entity e = ModelProvider.getInstance().getEntity(entityName);
    for (Property p : e.getProperties()) {
      if (!exceptList.contains(p.getName())) {
        propNames.add(p.getName());
      }
    }
  }

  private DataSet createDataSet() {
    final DataSet ds = OBProvider.getInstance().get(DataSet.class);
    ds.setId("" + System.currentTimeMillis());
    ds.setClient(OBContext.getOBContext().getCurrentClient());
    ds.setOrganization(OBContext.getOBContext().getCurrentOrganization());
    ds.setDescription("test");
    ds.setExport(true);
    ds.setExportAllowed(true);
    ds.setActive(true);
    ds.getDataSetTableList().add(createDataSetTable(ds, Order.TABLE_NAME));
    ds.getDataSetTableList().add(createDataSetTable(ds, Organization.TABLE_NAME));
    return ds;
  }

  private Property getProperty(String entityName, String propertyName) {
    final Entity e = ModelProvider.getInstance().getEntity(entityName);
    return e.getProperty(propertyName);
  }

  private DataSetTable createDataSetTable(DataSet ds, String tableName) {
    final OBCriteria<Table> obcTable = OBDal.getInstance().createCriteria(Table.class);
    obcTable.add(Restrictions.eq(Table.PROPERTY_DBTABLENAME, tableName));
    assertTrue(obcTable.list().size() == 1);
    final Table table = obcTable.list().get(0);

    final DataSetTable dst = OBProvider.getInstance().get(DataSetTable.class);
    dst.setId("" + System.currentTimeMillis());
    dst.setClient(OBContext.getOBContext().getCurrentClient());
    dst.setOrganization(OBContext.getOBContext().getCurrentOrganization());
    dst.setActive(true);
    dst.setBusinessObject(false);
    dst.setDataset(ds);
    dst.setExcludeAuditInfo(true);
    dst.setIncludeAllColumns(true);
    dst.setTable(table);
    return dst;
  }

  private DataSetColumn createDataSetColumn(DataSetTable dst, Property property, boolean excluded) {
    final DataSetColumn dsc = OBProvider.getInstance().get(DataSetColumn.class);
    dsc.setId("" + System.currentTimeMillis());
    dsc.setClient(OBContext.getOBContext().getCurrentClient());
    dsc.setOrganization(OBContext.getOBContext().getCurrentOrganization());
    dsc.setActive(true);
    dsc.setDatasetTable(dst);
    dsc.setExcluded(excluded);
    dsc.setColumn(getColumn(dst, property));
    return dsc;
  }

  private Column getColumn(DataSetTable dst, Property p) {
    final OBCriteria<Column> obcColumn = OBDal.getInstance().createCriteria(Column.class);
    obcColumn.add(Restrictions.and(
        Restrictions.eq(Column.PROPERTY_DBCOLUMNNAME, p.getColumnName()),
        Restrictions.eq(Column.PROPERTY_TABLE, dst.getTable())));
    assertTrue(obcColumn.list().size() == 1);
    return obcColumn.list().get(0);
  }
}
