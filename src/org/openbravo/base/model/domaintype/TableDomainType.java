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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model.domaintype;

import org.openbravo.base.model.Column;
import org.openbravo.base.model.RefTable;

/**
 * The type of columns which have a table reference.
 * 
 * @author mtaal
 */

public class TableDomainType extends BaseForeignKeyDomainType {
  private RefTable refTable;

  /**
   * @return the column based on the RefTable ({@link #setRefTable(RefTable)}).
   */
  public Column getForeignKeyColumn(String columnName) {
    // handles a special case that reference value is not set in a column
    // in that case the reference is the table reference directly
    if (getRefTable() == null) {
      return super.getForeignKeyColumn(columnName);
    }
    return getRefTable().getColumn();
  }

  public RefTable getRefTable() {
    return refTable;
  }

  public void setRefTable(RefTable refTable) {
    this.refTable = refTable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.base.model.domaintype.BaseForeignKeyDomainType#getReferedTableName(java.lang.
   * String)
   */
  protected String getReferedTableName(String columnName) {
    if (getRefTable() == null) {
      return super.getReferedTableName(columnName);
    }
    return getRefTable().getColumn().getTable().getName();
  }
}
