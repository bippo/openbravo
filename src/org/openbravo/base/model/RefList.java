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
import org.openbravo.base.model.domaintype.StringEnumerateDomainType;

/**
 * A limited mapping of the ref_list to support validation of string types.
 * 
 * @author mtaal
 */
public class RefList extends ModelObject {
  private static final Logger log = Logger.getLogger(RefList.class);

  private Reference reference;

  private String value;

  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
    final DomainType domainType = reference.getDomainType();
    if (!(domainType instanceof StringEnumerateDomainType)) {
      log.error("Domain type of reference " + reference.getId()
          + " is not a TableDomainType but a " + domainType);
    } else {
      ((StringEnumerateDomainType) domainType).addEnumerateValue(value);
    }
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @deprecated functionality is now implemented in {@link #setReference(Reference)}
   */
  public void setAllowedValue() {
  }

}
