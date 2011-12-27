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

package org.openbravo.service.dataset;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetColumn;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Offers services around datasets. The main function is to retrieve DataSets and to determine which
 * Properties of an Entity can be exported and which objects can be exported.
 * 
 * @author Martin Taal
 */
public class DataSetService implements OBSingleton {
  private static final Logger log = Logger.getLogger(DataSetService.class);

  private static DataSetService instance;

  public static synchronized DataSetService getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DataSetService.class);
    }
    return instance;
  }

  public static synchronized void setInstance(DataSetService instance) {
    DataSetService.instance = instance;
  }

  /**
   * Returns true if the {@link DataSet} has data. Note that the client/organization of the current
   * user are used for querying
   * 
   * @param dataSet
   *          the data set to check for content
   * @return true if there are objects in the data set, false other wise
   * @see DataSet#getDataSetTableList()
   * @see DataSetTable
   */
  public boolean hasData(DataSet dataSet) {
    long totalCnt = 0;
    for (DataSetTable dataSetTable : dataSet.getDataSetTableList()) {
      final Entity entity = ModelProvider.getInstance().getEntityByTableName(
          dataSetTable.getTable().getDBTableName());
      final OBCriteria<BaseOBObject> obc = OBDal.getInstance().createCriteria(entity.getName());
      totalCnt += obc.count();
      if (totalCnt > 0) {
        return true;
      }
    }
    return totalCnt > 0;
  }

  /**
   * Checks if objects of a {@link DataSetTable} of the {@link DataSet} have changed since a
   * specific date. Note that this method does not use whereclauses or other filters defined in the
   * dataSetTable. It checks all instances of the table of the DataSetTable.
   * 
   * @param dataSet
   *          the DataSetTables of this dataSet are checked.
   * @param afterDate
   *          the time limit
   * @return true if there is at least one object which has changed since afterDate, false
   *         afterwards
   */
  public <T extends BaseOBObject> boolean hasChanged(DataSet dataSet, Date afterDate) {
    for (DataSetTable dataSetTable : dataSet.getDataSetTableList()) {
      final Entity entity = ModelProvider.getInstance().getEntityByTableName(
          dataSetTable.getTable().getDBTableName());
      final OBCriteria<T> obc = OBDal.getInstance().createCriteria(entity.getName());
      obc.add(Restrictions.gt(Organization.PROPERTY_UPDATED, afterDate));
      // todo: count is slower than exists, is exists possible?
      List<?> list = obc.list();
      if (obc.count() < 20 && obc.count() > 0) {
        log.warn("The following rows were changed after your last update.database or export.database:");
        for (Object obj : list) {
          log.warn("     -" + obj);
        }
      } else if (obc.count() > 20) {
        log.warn("Rows inside the table " + ((BaseOBObject) list.get(0)).getEntity().getTableName()
            + " were changed after your last update.database or export.database:");
      }
      if (obc.count() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieves a dataset using the value and module of the dataset
   * 
   * @param value
   *          the value used to find the dataset in the database
   * @param moduleId
   *          the id of the module used to find the dataset in the database
   * @return the found DataSet
   */
  public DataSet getDataSetByValueModule(String value, String moduleId) {
    final Module module = OBDal.getInstance().get(Module.class, moduleId);
    final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
    obc.add(Restrictions.eq(DataSet.PROPERTY_MODULE, module));
    obc.add(Restrictions.eq(DataSet.PROPERTY_SEARCHKEY, value));
    final List<?> list = obc.list();
    Check.isTrue(list.size() <= 1,
        "There is more than one dataset available when searching using the name/id " + value + "/"
            + moduleId);
    if (list.size() == 0) {
      return null;
    }
    return (DataSet) list.get(0);
  }

  /**
   * Finds datasets belonging to the Module with a specific moduleId.
   * 
   * @param moduleId
   *          the moduleId of the module to use for searching datasets
   * @return the list of found datasets
   */
  public List<DataSet> getDataSetsByModuleID(String moduleId) {
    final Module module = OBDal.getInstance().get(Module.class, moduleId);
    final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
    obc.add(Restrictions.eq(DataSet.PROPERTY_MODULE, module));
    return obc.list();
  }

  /**
   * Finds a dataset solely on the basis of its value
   * 
   * @param value
   *          the value to search for
   * @return the found DataSet
   */
  public DataSet getDataSetByValue(String value) {
    final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
    obc.add(Restrictions.eq(DataSet.PROPERTY_SEARCHKEY, value));
    final List<DataSet> ds = obc.list();
    // Check.isTrue(ds.size() > 0, "There is no DataSet with name " + value);
    if (ds.size() == 0) {
      // TODO: throw an exception?
      return null;
    }
    Check.isTrue(ds.size() == 1, "There is more than one DataSet with the name " + value
        + ". The number of found DataSets is " + ds.size());
    return ds.get(0);
  }

  /**
   * Returns a list of DataSet tables instances on the basis of the DataSet
   * 
   * @param dataSet
   *          the DataSet for which the list of tables is required
   * @return the DataSetTables of the DataSet
   * @deprecated use dataSet.getDataSetTableList()
   */
  @Deprecated
  public List<DataSetTable> getDataSetTables(DataSet dataSet) {
    return dataSet.getDataSetTableList();
  }

  /**
   * Return the list of DataSet columns for a table
   * 
   * @param dataSetTable
   *          the dataSetTable for which the columns need to be found
   * @return the list of DataSetColumns of the dataSetTable
   * @deprecated use dataSetTable.getDataSetColumnList()
   */
  @Deprecated
  public List<DataSetColumn> getDataSetColumns(DataSetTable dataSetTable) {
    return dataSetTable.getDataSetColumnList();
  }

  /**
   * Determines which objects are exportable using the DataSetTable whereClause.
   * 
   * @param dataSetTable
   *          the dataSetTable defines the Entity and the whereClause to use
   * @param moduleId
   *          the moduleId is a parameter in the whereClause
   * @return the list of exportable business objects
   */
  public List<BaseOBObject> getExportableObjects(DataSetTable dataSetTable, String moduleId) {
    return getExportableObjects(dataSetTable, moduleId, new HashMap<String, Object>());
  }

  /**
   * Determines which objects are exportable using the DataSetTable whereClause.
   * 
   * @param dataSetTable
   *          the dataSetTable defines the Entity and the whereClause to use
   * @param moduleId
   *          the moduleId is a parameter in the whereClause
   * @param parameters
   *          a collection of named parameters which are used in the whereClause of the dataSetTable
   * @return the list of exportable business objects
   */
  @SuppressWarnings("unchecked")
  public List<BaseOBObject> getExportableObjects(DataSetTable dataSetTable, String moduleId,
      Map<String, Object> parameters) {

    // do the part which can be done as super user separately from the
    // actual read of the db

    OBContext.setAdminMode();
    try {
      final String entityName = dataSetTable.getTable().getName();
      final Entity entity = ModelProvider.getInstance().getEntity(entityName);

      if (entity == null) {
        log.error("Entity not found using table name " + entityName);
        return new ArrayList<BaseOBObject>();
      }

      String whereClause = dataSetTable.getSQLWhereClause();

      final Map<String, Object> existingParams = new HashMap<String, Object>();
      if (whereClause != null) {
        if (parameters != null) {
          for (final String name : parameters.keySet()) {
            if (whereClause.indexOf(":" + name) != -1) {
              existingParams.put(name, parameters.get(name));
            }
          }
        }
      }

      if (moduleId != null && whereClause != null) {
        while (whereClause.indexOf("@moduleid@") != -1) {
          whereClause = whereClause.replace("@moduleid@", "'" + moduleId + "'");
        }
        if (whereClause.indexOf(":moduleid") != -1 && parameters.get("moduleid") == null) {
          existingParams.put("moduleid", moduleId);
        }
      }

      final OBQuery<BaseOBObject> oq = OBDal.getInstance().createQuery(entity.getName(),
          whereClause);
      oq.setFilterOnActive(false);
      oq.setNamedParameters(existingParams);

      if (OBContext.getOBContext().getRole().getId().equals("0")
          && OBContext.getOBContext().getCurrentClient().getId().equals("0")) {
        oq.setFilterOnReadableOrganization(false);
        oq.setFilterOnReadableClients(false);
      }

      final List<?> list = oq.list();
      Collections.sort(list, new BaseOBIDHexComparator());
      return (List<BaseOBObject>) list;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Determines which objects are exportable using the DataSetTable whereClause. Returns an iterator
   * over these objects. The returned objects are sorted by id.
   * 
   * @param dataSetTable
   *          the dataSetTable defines the Entity and the whereClause to use
   * @param moduleId
   *          the moduleId is a parameter in the whereClause
   * @param parameters
   *          a collection of named parameters which are used in the whereClause of the dataSetTable
   * @return iterator over the exportable objects
   */
  public Iterator<BaseOBObject> getExportableObjectsIterator(DataSetTable dataSetTable,
      String moduleId, Map<String, Object> parameters) {

    final String entityName = dataSetTable.getTable().getName();
    final Entity entity = ModelProvider.getInstance().getEntity(entityName);

    if (entity == null) {
      log.error("Entity not found using table name " + entityName);
      return new ArrayList<BaseOBObject>().iterator();
    }

    String whereClause = dataSetTable.getSQLWhereClause();
    final Map<String, Object> existingParams = new HashMap<String, Object>();
    for (final String name : parameters.keySet()) {
      if (whereClause.indexOf(":" + name) != -1) {
        existingParams.put(name, parameters.get(name));
      }
    }
    if (moduleId != null && whereClause != null) {
      while (whereClause.indexOf("@moduleid@") != -1) {
        whereClause = whereClause.replace("@moduleid@", "'" + moduleId + "'");
      }
      if (whereClause.indexOf(":moduleid") != -1 && parameters.get("moduleid") == null) {
        existingParams.put("moduleid", moduleId);
      }
    }

    // set the order by, first detect if there is an alias
    String alias = "";
    // this is a space on purpose
    if (whereClause != null && whereClause.toLowerCase().trim().startsWith("as")) {
      // strip the as
      final String strippedWhereClause = whereClause.toLowerCase().trim().substring(2).trim();
      // get the next space
      final int index = strippedWhereClause.indexOf(" ");
      alias = strippedWhereClause.substring(0, index);
      alias += ".";
    }

    final OBQuery<BaseOBObject> oq = OBDal.getInstance().createQuery(entity.getName(),
        (whereClause != null ? whereClause : "") + " order by " + alias + "id");
    oq.setFilterOnActive(false);
    oq.setNamedParameters(existingParams);

    if (OBContext.getOBContext().getRole().getId().equals("0")
        && OBContext.getOBContext().getCurrentClient().getId().equals("0")) {
      oq.setFilterOnReadableOrganization(false);
      oq.setFilterOnReadableClients(false);
    }
    try {
      return oq.iterate();
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * This method will return the properties as defined by the DataSetcolumns definition. It will
   * return transient properties but not the audit-info properties if so excluded by the DataSet
   * definition.
   * 
   * @param bob
   *          the business object to export
   * @param dataSetTable
   *          the dataSetTable to export
   * @param dataSetColumns
   *          the list of potential columns to export
   * @return the list of properties which are exportable
   */
  public List<Property> getEntityProperties(BaseOBObject bob, DataSetTable dataSetTable,
      List<DataSetColumn> dataSetColumns) {
    return getExportableProperties(bob, dataSetTable, dataSetColumns, true);
  }

  /**
   * This method will return the properties as defined by the DataSetcolumns definition. It will
   * <b>not</b> return transient properties and neither the audit-info properties if so excluded by
   * the DataSet definition.
   * 
   * @param bob
   *          the business object to export
   * @param dataSetTable
   *          the dataSetTable to export
   * @param dataSetColumns
   *          the list of potential columns to export
   * @return the list of properties which are exportable
   */
  public List<Property> getExportableProperties(BaseOBObject bob, DataSetTable dataSetTable,
      List<DataSetColumn> dataSetColumns) {
    return getExportableProperties(bob, dataSetTable, dataSetColumns, false);
  }

  /**
   * This method will return the properties which are exportable as defined by the DataSetcolumns
   * definition. It will include transient properties depending on the parameter. Audit-info
   * properties are never exported
   * 
   * @param bob
   *          the business object to export
   * @param dataSetTable
   *          the dataSetTable to export
   * @param dataSetColumns
   *          the list of potential columns to export
   * @param exportTransients
   *          if true then transient properties are also exportable
   * @return the list of properties which are exportable
   */
  public List<Property> getExportableProperties(BaseOBObject bob, DataSetTable dataSetTable,
      List<DataSetColumn> dataSetColumns, boolean exportTransients) {

    final Entity entity = bob.getEntity();
    final List<Property> exportables;
    // check if all are included, except the excluded
    if (dataSetTable.isIncludeAllColumns()) {
      exportables = new ArrayList<Property>(entity.getProperties());
      // now remove the excluded
      for (final DataSetColumn dsc : dataSetColumns) {
        if (dsc.isExcluded()) {
          exportables.remove(entity.getPropertyByColumnName(dsc.getColumn().getDBColumnName()));
        }
      }
    } else {
      // not all included, go through the DataSetcolumns
      // and add the not excluded
      exportables = new ArrayList<Property>();
      for (final DataSetColumn dsc : dataSetColumns) {
        if (!dsc.isExcluded()) {
          exportables.add(entity.getPropertyByColumnName(dsc.getColumn().getDBColumnName()));
        }
      }
    }
    // remove the transients
    if (!exportTransients) {
      final List<Property> toRemove = new ArrayList<Property>();
      for (final Property p : exportables) {
        if (p.isTransient(bob)) {
          toRemove.add(p);
        }
      }
      exportables.removeAll(toRemove);
    }

    // Remove the auditinfo
    if (dataSetTable.isExcludeAuditInfo()) {
      final List<Property> toRemove = new ArrayList<Property>();
      for (final Property p : exportables) {
        if (p.isAuditInfo()) {
          toRemove.add(p);
        }
      }
      exportables.removeAll(toRemove);
    }

    return exportables;
  }

  // compares the content of a list by converting the id to a hex
  public static class BaseOBIDHexComparator implements Comparator<Object> {

    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof BaseOBObject) || !(o2 instanceof BaseOBObject)) {
        return 0;
      }
      final BaseOBObject bob1 = (BaseOBObject) o1;
      final BaseOBObject bob2 = (BaseOBObject) o2;
      if (!(bob1.getId() instanceof String) || !(bob2.getId() instanceof String)) {
        return 0;
      }
      try {
        final BigInteger bd1 = new BigInteger(bob1.getId().toString(), 32);
        final BigInteger bd2 = new BigInteger(bob2.getId().toString(), 32);
        return bd1.compareTo(bd2);
      } catch (final NumberFormatException n) {
        // ignoring exception on purpose, some id's can't be compared numerically
        return 0;
      }
    }
  }

  public static class BaseStringComparator implements Comparator<Object> {

    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof BaseOBObject) || !(o2 instanceof BaseOBObject)) {
        return 0;
      }
      final BaseOBObject bob1 = (BaseOBObject) o1;
      final BaseOBObject bob2 = (BaseOBObject) o2;
      final String bd1 = bob1.getId().toString();
      final String bd2 = bob2.getId().toString();
      return bd1.compareTo(bd2);
    }
  }

}
