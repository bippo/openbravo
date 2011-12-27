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

package org.openbravo.base.model;

import org.apache.log4j.Logger;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.TableDomainType;

/**
 * Used by the {@link ModelProvider ModelProvider}, maps the AD_Ref_Table table in the application
 * dictionary.
 * 
 * @author iperdomo
 */

public class RefTable extends ModelObject {
  private static final Logger log = Logger.getLogger(RefTable.class);

  private Reference reference;

  private Column column;

  private Column displayColumn;

  public Column getColumn() {
    return column;
  }

  public void setColumn(Column column) {
    this.column = column;
  }

  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
    final DomainType domainType = reference.getDomainType();
    if (!(domainType instanceof TableDomainType)) {
      log.error("Domain type of reference " + reference.getId()
          + " is not a TableDomainType but a " + domainType);
    } else {
      ((TableDomainType) domainType).setRefTable(this);
    }
  }

  public Column getDisplayColumn() {
    return displayColumn;
  }

  public void setDisplayColumn(Column displayColumn) {
    this.displayColumn = displayColumn;
  }
}
