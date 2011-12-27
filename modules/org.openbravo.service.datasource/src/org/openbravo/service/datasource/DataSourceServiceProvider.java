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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * Provides {@link DataSourceService} instances and caches them in a global cache.
 * 
 * @author mtaal
 */
@ApplicationScoped
public class DataSourceServiceProvider {

  private static final long serialVersionUID = 1L;

  private Map<String, DataSourceService> dataSources = new ConcurrentHashMap<String, DataSourceService>();

  @Inject
  private WeldUtils weldUtils;

  /**
   * Checks the internal cache for a datasource with the requested name and returns it if found. If
   * not found a new one is created, which is cached and then returned.
   * 
   * @param name
   *          the name by which to search and identify the data source.
   * @return a {@link DataSourceService} object
   */
  @SuppressWarnings("unchecked")
  public DataSourceService getDataSource(String name) {
    DataSourceService ds = dataSources.get(name);
    if (ds == null) {
      OBContext.setAdminMode();
      try {
        DataSource dataSource = OBDal.getInstance().get(DataSource.class, name);
        if (dataSource == null) {

          final OBCriteria<DataSource> obCriteria = OBDal.getInstance().createCriteria(
              DataSource.class);
          obCriteria.add(Restrictions.eq(DataSource.PROPERTY_NAME, name));
          if (!obCriteria.list().isEmpty()) {
            dataSource = obCriteria.list().get(0);
          }
        }
        if (dataSource == null) {
          ds = weldUtils.getInstance(DefaultDataSourceService.class);
          ds.setName(name);
          ds.setEntity(ModelProvider.getInstance().getEntity(name));
          dataSources.put(name, ds);
        } else {
          if (dataSource.getJavaClassName() != null) {
            final Class<DataSourceService> clz = (Class<DataSourceService>) OBClassLoader
                .getInstance().loadClass(dataSource.getJavaClassName());
            ds = weldUtils.getInstance(clz);
          } else {
            ds = new DefaultDataSourceService();
          }
          ds.setDataSource(dataSource);
          dataSources.put(name, ds);
        }
      } catch (Exception e) {
        throw new OBException(e);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return ds;
  }
}
