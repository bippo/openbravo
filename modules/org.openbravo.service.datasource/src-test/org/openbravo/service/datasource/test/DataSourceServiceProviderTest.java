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

package org.openbravo.service.datasource.test;

import java.util.HashMap;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.service.datasource.DataSource;
import org.openbravo.service.datasource.DataSourceConstants;
import org.openbravo.service.datasource.DataSourceService;
import org.openbravo.service.datasource.DataSourceServiceProvider;
import org.openbravo.service.datasource.DefaultDataSourceService;

/**
 * Tests that the {@link DataSourceServiceProvider} can handle defined data sources in the database
 * as well as 'dynamic' datasources.
 * 
 * @author mtaal
 */

public class DataSourceServiceProviderTest extends WeldBaseTest {

  @Inject
  private DataSourceServiceProvider dataSourceServiceProvider;

  /**
   * Creates a DataSource record and then reads it back through the
   * {@link DataSourceServiceProvider}.
   */
  @Test
  public void testJavaScriptGeneration() throws Exception {
    setSystemAdministratorContext();

    // create a test data source
    DataSource dataSource = new DataSource();
    dataSource.setJavaClassName(ExtendedDataSource.class.getName());
    dataSource.setName(Table.ENTITY_NAME);
    dataSource.setDescription("test");
    dataSource.setTemplate(OBDal.getInstance().get(Template.class,
        DataSourceConstants.DS_TEMPLATE_ID));
    final String whereClause = Table.PROPERTY_HELPCOMMENT + " <> null";
    dataSource.setHQLWhereClause(whereClause);
    OBDal.getInstance().save(dataSource);
    OBDal.getInstance().flush();

    final DataSourceService dataSourceService = dataSourceServiceProvider
        .getDataSource(Table.ENTITY_NAME);
    assertTrue(dataSourceService instanceof ExtendedDataSource);
    assertEquals(whereClause, dataSourceService.getWhereClause());
    // do a big fetch
    assertTrue(null != dataSourceService.fetch(new HashMap<String, String>()));

    final DataSourceService columnDataSourceService = dataSourceServiceProvider
        .getDataSource(Column.ENTITY_NAME);
    assertFalse(columnDataSourceService instanceof ExtendedDataSource);
    assertTrue(columnDataSourceService instanceof DefaultDataSourceService);

    // prevent a save
    OBDal.getInstance().rollbackAndClose();
  }

  public static class ExtendedDataSource extends DefaultDataSourceService {

    public Entity getEntity() {
      return ModelProvider.getInstance().getEntity(Table.ENTITY_NAME);
    }
  }

}