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
package org.openbravo.client.kernel.reference;

import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the Link UI definition.
 * 
 */
public class LinkUIDefinition extends UIDefinition {

  @Override
  public String getParentType() {
    return "link";
  }

  @Override
  public String getFormEditorType() {
    return "OBLinkItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBTextItem";
  }

  @Override
  public String getGridFieldProperties(Field field) {
    Long length = field.getColumn().getLength();

    Long displaylength = field.getDisplayedLength();
    if (displaylength == null || displaylength == 0) {
      displaylength = length;
    }
    // custom override
    if (field.getColumn().getDBColumnName().compareToIgnoreCase("documentno") == 0) {
      length = new Long(20);
    }
    return getShowHoverGridFieldSettings(field) + ", length: " + length + ", displaylength:"
        + displaylength + super.getGridFieldProperties(field);
  }

  @Override
  public String getFilterEditorProperties(Field field) {
    return super.getFilterEditorProperties(field);
  }

}
