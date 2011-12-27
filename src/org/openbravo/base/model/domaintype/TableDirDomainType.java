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

/**
 * The type of column in the following cases:
 * <ul>
 * <li>TABLEDIR</li>
 * <li>SEARCH and the reference value is not set</li>
 * <li>IMAGE</li>
 * <li>PRODUCT_ATTRIBUTE</li>
 * <li>RESOURCE_ASSIGNMENT</li>
 * <li>IMAGE_BLOB</li>
 * </ul>
 * 
 * @author mtaal
 */

public class TableDirDomainType extends BaseForeignKeyDomainType {

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.base.model.domaintype.BaseForeignKeyDomainType#getReferedTableName()
   */
  protected String getReferedTableName() {
    return null;
  }
}
