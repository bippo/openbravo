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

import org.openbravo.base.model.Column;
import org.openbravo.base.model.ModelObject;
import org.openbravo.base.model.Table;

/**
 * The selector read from the database. Note the Column/Table and other types from the
 * org.openbravo.base.model package should be used, not the generated ones!
 * 
 * @author mtaal
 */
public class SelectorDefinition extends ModelObject {

  private Table table;
  private Column column;
  private String referenceId;
  private DatasourceDefinition datasourceDefinition;

  public Column getColumn() {
    return column;
  }

  public void setColumn(Column column) {
    this.column = column;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public Table getTable() {
    return table;
  }

  public void setTable(Table table) {
    this.table = table;
  }

  public DatasourceDefinition getDatasourceDefinition() {
    return datasourceDefinition;
  }

  public void setDatasourceDefinition(DatasourceDefinition datasourceDefinition) {
    this.datasourceDefinition = datasourceDefinition;
  }
}
