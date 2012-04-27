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

import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.model.ad.ui.Field;

/**
 * Base class of all foreign key/reference ui definitions.
 * 
 * @author mtaal
 */
public class ForeignKeyUIDefinition extends UIDefinition {

  @Override
  public String getParentType() {
    return "text";
  }

  @Override
  public String getFormEditorType() {
    return "OBFKItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBFKFilterTextItem";
  }

  @Override
  public String getGridFieldProperties(Field field) {
    final Property prop = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn());

    Long displaylength = field.getDisplayedLength();
    if (displaylength == null || displaylength == 0) {
      displaylength = field.getColumn().getLength();
    }

    // only output when really needed
    String displayField = "";
    if (getDisplayFieldName(field, prop) != null) {
      displayField = ", displayField: '" + getDisplayFieldName(field, prop) + "'";
    }
    return displayField + ", displaylength:" + displaylength + ",fkField: true"
        + super.getGridFieldProperties(field) + getShowHoverGridFieldSettings(field);
  }

  /**
   * Note: can return null, in that case the default display field name is used
   */
  protected String getDisplayFieldName(Field field, Property prop) {
    return null;
  }

  protected String getSuperGridFieldProperties(Field field) {
    return super.getGridFieldProperties(field);
  }

  protected String getSuperGridFieldName(Field field) {
    return super.getGridFieldName(field);
  }
}
