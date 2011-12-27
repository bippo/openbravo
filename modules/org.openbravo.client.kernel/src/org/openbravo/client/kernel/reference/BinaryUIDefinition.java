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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the image ui definition.
 * 
 * @author mtaal
 */
public class BinaryUIDefinition extends UIDefinition {

  // don't support sorting on binary fields and don't allow editing binary fields
  // in a grid
  @Override
  public String getGridFieldProperties(Field field) {
    return super.getGridFieldProperties(field) + ", canSort: false, canEdit: false";
  }

  @Override
  public String getParentType() {
    return "image";
  }
}
