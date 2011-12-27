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

package org.openbravo.service.db;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.service.dataset.DataSetService;

/**
 * Exports business objects to XML on the basis of Datasets, DataSetTables and DataSetColumns.
 * 
 * @see DataSetService
 * @see EntityXMLConverter
 * 
 * @author Martin Taal
 */
public class DataExportService implements OBSingleton {
  private static final Logger log = Logger.getLogger(DataExportService.class);

  /**
   * Name of the dataset used for client export.
   */
  public static final String CLIENT_DATA_SET_NAME = "Client Definition";

  /**
   * The name of the client id parameter used in the datasets
   */
  public static final String CLIENT_ID_PARAMETER_NAME = "ClientID";

  private static DataExportService instance;

  /**
   * Returns the current singleton instance of the DataExportService.
   * 
   * @return the DataExportService instance
   */
  public static synchronized DataExportService getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DataExportService.class);
    }
    return instance;
  }

  /**
   * Makes it possible to set a specific DataExportService instance which will be used by the rest
   * of the Openbravo system.
   * 
   * @param instance
   *          the DataExportService instance used by the
   */
  public static synchronized void setInstance(DataExportService instance) {
    DataExportService.instance = instance;
  }

  /**
   * Export the data of a specific dataSet to XML. If the dataset is empty then a null value is
   * returned.
   * 
   * @param dataSet
   *          the dataset to export
   * @return the XML string containing the data of the dataset
   */
  public String exportDataSetToXML(DataSet dataSet) {
    return exportDataSetToXML(dataSet, null, new HashMap<String, Object>());
  }

  /**
   * Exports data of a client. The main difference with the standard dataset export is that also
   * references to client and organizations are exported. The dataset with the name Client
   * Definition is used for the export.
   * 
   * @param parameters
   *          the parameters used in the queries, the client id should be passed in this map with
   *          the parameter name ClientID (note is case sensitive)
   * @param out
   *          the xml is written to this writer
   * @see #CLIENT_DATA_SET_NAME
   * @see #CLIENT_ID_PARAMETER_NAME
   */
  public void exportClientToXML(Map<String, Object> parameters, boolean exportAuditInfo, Writer out) {
    DataSet dataSet = null;
    OBContext.setAdminMode();
    try {
      final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
      obc.add(Restrictions.eq("name", CLIENT_DATA_SET_NAME));
      if (obc.list().size() == 0) {
        throw new OBException("No dataset found with name " + CLIENT_DATA_SET_NAME);
      }
      dataSet = obc.list().get(0);

      // read the client
      final Client client = OBDal.getInstance().get(Client.class,
          parameters.get(CLIENT_ID_PARAMETER_NAME));

      // the export part may not be run as superuser
      log.debug("Exporting dataset " + dataSet.getName());
      final EntityXMLConverter exc = EntityXMLConverter.newInstance();
      exc.setOptionExportClientOrganizationReferences(true);
      exc.setOptionMinimizeXMLSize(true);
      exc.setOptionIncludeChildren(false);
      exc.setOptionIncludeReferenced(true);
      exc.setOptionExportTransientInfo(false);
      exc.setOptionExportAuditInfo(exportAuditInfo);
      exc.setAddSystemAttributes(false);
      exc.setOutput(out);
      exc.setClient(client);

      final List<DataSetTable> dts = dataSet.getDataSetTableList();
      Collections.sort(dts, new DatasetTableComparator());

      final Set<BaseOBObject> toExport = new LinkedHashSet<BaseOBObject>();
      for (final DataSetTable dt : dts) {
        final List<BaseOBObject> list = DataSetService.getInstance().getExportableObjects(dt, null,
            parameters);
        toExport.addAll(list);
      }

      if (toExport.size() > 0) {
        exc.process(toExport);
      }

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Export the data of a specific dataSet to XML. If the dataset is empty then a null value is
   * returned.
   * 
   * @param dataSet
   *          the dataset to export
   * @param moduleId
   *          is used as a parameter in where clauses of the DataSetTable and is used to set the
   *          module id in the AD_REF_DATA_LOADED table
   * @return the XML string containing the data of the dataset
   */
  public String exportDataSetToXML(DataSet dataSet, String moduleId, Map<String, Object> parameters) {

    log.debug("Exporting dataset " + dataSet.getName());

    final EntityXMLConverter exc = EntityXMLConverter.newInstance();
    exc.setOptionIncludeReferenced(false);
    exc.setOptionExportTransientInfo(true);
    exc.setOptionExportAuditInfo(true);
    exc.setAddSystemAttributes(true);
    if (dataSet != null) {
      exc.setDataSet(dataSet);
    }
    final StringWriter out = new StringWriter();
    exc.setOutput(out);

    final List<DataSetTable> dts = dataSet.getDataSetTableList();
    Collections.sort(dts, new DatasetTableComparator());

    // set the Client ID if not set
    if (parameters.get(CLIENT_ID_PARAMETER_NAME) == null) {
      parameters.put(CLIENT_ID_PARAMETER_NAME, OBContext.getOBContext().getCurrentClient().getId());
    }

    final Set<BaseOBObject> toExport = new LinkedHashSet<BaseOBObject>();
    for (final DataSetTable dt : dts) {
      final Boolean isbo = dt.isBusinessObject();
      exc.setOptionIncludeChildren(isbo != null && isbo.booleanValue());
      final List<BaseOBObject> list = DataSetService.getInstance().getExportableObjects(dt,
          moduleId, parameters);
      toExport.addAll(list);
    }

    if (toExport.size() > 0) {
      exc.process(toExport);
    }
    return out.toString();
  }

  // sort the datatable by id
  private class DatasetTableComparator implements Comparator<DataSetTable> {

    @Override
    public int compare(DataSetTable o1, DataSetTable o2) {
      return o1.getId().compareTo(o2.getId());
    }

  }
}
