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

import java.util.Set;

/**
 * The ModelReference implements the reference extensions used for the Data Access Layer. See <a
 * href
 * ="http://wiki.openbravo.com/wiki/Projects/Reference_Extension/Technical_Documentation#DAL">here
 * </a> for more information.
 * 
 * @author mtaal
 */

public interface EnumerateDomainType extends PrimitiveDomainType {

  /**
   * The set of enumerate values in case this reference is an Enumerate.
   * 
   * @return the set of allowed values for a property with this type.
   */
  Set<? extends Object> getEnumerateValues();
}
