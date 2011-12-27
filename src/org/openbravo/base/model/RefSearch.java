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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model;

import org.apache.log4j.Logger;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.SearchDomainType;

/**
 * Used by the {@link ModelProvider ModelProvider}, maps the AD_Reference table in the application
 * dictionary.
 * 
 * @author iperdomo
 */

public class RefSearch extends ModelObject {
  private static final Logger log = Logger.getLogger(RefSearch.class);

  private String reference;
  private Reference referenceObject;
  private Column column;

  public Reference getReferenceObject() {
    return referenceObject;
  }

  public void setReferenceObject(Reference referenceObj) {
    this.referenceObject = referenceObj;
    reference = referenceObj.getId();
    final DomainType domainType = referenceObj.getDomainType();
    if (!(domainType instanceof SearchDomainType)) {
      log.error("Domain type of reference " + referenceObj.getId()
          + " is not a TableDomainType but a " + domainType);
    } else {
      ((SearchDomainType) domainType).setRefSearch(this);
    }
  }

  public Column getColumn() {
    return column;
  }

  public void setColumn(Column column) {
    this.column = column;
  }

  /**
   * Deprecated use {@link #getReferenceObject()}
   */
  public String getReference() {
    return reference;
  }

  /**
   * Deprecated use {@link #setReferenceObject(Reference)}
   */
  public void setReference(String reference) {
    this.reference = reference;
  }
}
