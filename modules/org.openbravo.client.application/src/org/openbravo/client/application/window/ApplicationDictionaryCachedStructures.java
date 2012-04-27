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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.domain.ReferencedTable;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.userinterface.selector.Selector;
import org.openbravo.userinterface.selector.SelectorField;

/**
 * This class caches some AD structures used by the Form Initialization component. Basically, it
 * caches: AD components (fields, columns, auxiliary inputs) and ComboTableData instances. This
 * caching occurs to obtain better performance in FIC computations. For this cache to be used, the
 * system needs to be on 'production' mode, that is, all the modules need to be not in development
 */
@SessionScoped
public class ApplicationDictionaryCachedStructures implements Serializable {
  private static final long serialVersionUID = 1L;

  private Map<String, Tab> tabMap = new HashMap<String, Tab>();
  private Map<String, Table> tableMap = new HashMap<String, Table>();
  private Map<String, List<Field>> fieldMap = new HashMap<String, List<Field>>();
  private Map<String, List<Column>> columnMap = new HashMap<String, List<Column>>();
  private Map<String, List<AuxiliaryInput>> auxInputMap = new HashMap<String, List<AuxiliaryInput>>();
  private Map<String, ComboTableData> comboTableDataMap = new HashMap<String, ComboTableData>();

  private boolean useCache;

  public ApplicationDictionaryCachedStructures() {
    // The cache will only be active when there are no modules in development in the system
    final String query = "select m from ADModule m where m.inDevelopment=true";
    final Query indevelMods = OBDal.getInstance().getSession().createQuery(query);
    useCache = indevelMods.list().size() == 0;
  }

  public Tab getTab(String tabId) {
    if (useCache() && tabMap.containsKey(tabId)) {
      return tabMap.get(tabId);
    }
    Tab tab = OBDal.getInstance().get(Tab.class, tabId);
    Hibernate.initialize(tab);
    Hibernate.initialize(tab.getADAuxiliaryInputList());
    Hibernate.initialize(tab.getADFieldList());
    Hibernate.initialize(tab.getTable());
    Hibernate.initialize(tab.getTable().getADColumnList());
    tabMap.put(tabId, tab);
    return tab;
  }

  public Table getTable(String tableId) {
    if (useCache() && tableMap.containsKey(tableId)) {
      return tableMap.get(tableId);
    }
    Table table = OBDal.getInstance().get(Table.class, tableId);
    Hibernate.initialize(table);
    tableMap.put(tableId, table);
    return table;
  }

  public List<Field> getFieldsOfTab(String tabId) {
    if (useCache() && fieldMap.containsKey(tabId)) {
      return fieldMap.get(tabId);
    }
    Tab tab = getTab(tabId);
    List<Field> fields = tab.getADFieldList();
    for (Field f : fields) {
      if (f.getColumn() == null) {
        continue;
      }
      Hibernate.initialize(f.getColumn());
      initializeColumn(f.getColumn());
    }
    fieldMap.put(tabId, fields);
    return fields;
  }

  public List<Column> getColumnsOfTable(String tableId) {
    if (useCache() && columnMap.get(tableId) != null) {
      return columnMap.get(tableId);
    }
    Table table = getTable(tableId);
    List<Column> columns = table.getADColumnList();
    for (Column c : columns) {
      initializeColumn(c);
    }
    columnMap.put(tableId, columns);
    return columns;
  }

  private void initializeColumn(Column c) {

    Hibernate.initialize(c.getValidation());
    if (c.getValidation() != null) {
      Hibernate.initialize(c.getValidation().getValidationCode());
    }
    if (c.getCallout() != null) {
      Hibernate.initialize(c.getCallout());
      Hibernate.initialize(c.getCallout().getADModelImplementationList());
      for (ModelImplementation imp : c.getCallout().getADModelImplementationList()) {
        Hibernate.initialize(imp);
      }
    }

    if (c.getReference() != null) {
      Hibernate.initialize(c.getReference());
      initializeReference(c.getReference());
    }
    if (c.getReferenceSearchKey() != null) {
      initializeReference(c.getReferenceSearchKey());
    }
  }

  private void initializeReference(Reference reference) {
    for (ReferencedTable t : reference.getADReferencedTableList()) {
      Hibernate.initialize(t);
    }
    for (Selector s : reference.getOBUISELSelectorList()) {
      Hibernate.initialize(s);
      SelectorField displayField = s.getDisplayfield();
      Hibernate.initialize(displayField);
    }

  }

  public List<AuxiliaryInput> getAuxiliarInputList(String tabId) {
    if (useCache() && auxInputMap.get(tabId) != null) {
      return auxInputMap.get(tabId);
    }
    Tab tab = getTab(tabId);
    Hibernate.initialize(tab.getADAuxiliaryInputList());
    List<AuxiliaryInput> auxInputs = new ArrayList<AuxiliaryInput>(tab.getADAuxiliaryInputList());
    for (AuxiliaryInput auxIn : auxInputs) {
      Hibernate.initialize(auxIn);
    }
    auxInputMap.put(tabId, auxInputs);
    return auxInputs;
  }

  public ComboTableData getComboTableData(VariablesSecureApp vars, String ref, String colName,
      String objectReference, String validation, String orgList, String clientList) {
    String comboId = ref + colName + objectReference + validation + orgList + clientList;
    if (useCache() && comboTableDataMap.get(comboId) != null) {
      return comboTableDataMap.get(comboId);
    }
    ComboTableData comboTableData;
    try {
      comboTableData = new ComboTableData(vars, new DalConnectionProvider(false), ref, colName,
          objectReference, validation, orgList, clientList, 0);
    } catch (Exception e) {
      throw new OBException("Error while computing combo table data for column " + colName, e);
    }
    if (comboTableData.canBeCached()) {
      comboTableDataMap.put(comboId, comboTableData);
    }
    return comboTableData;

  }

  private boolean useCache() {
    return useCache;
  }
}
