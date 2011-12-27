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
package org.openbravo.userinterface.smartclient;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.service.OBDal;

/**
 * The component responsible for creating Smartclient simple type representations used by other
 * modules.
 * 
 * @author mtaal
 */
public class TypesComponent extends BaseTemplateComponent {

  public static final String SC_TYPES_COMPONENT_ID = "SmartClientTypes";
  public static final String SC_TYPES_TEMPLATE_ID = "83DBBE0D8F5E497CA9BBE2A384A4CA36";

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, SC_TYPES_TEMPLATE_ID);
  }

  public List<UIDefinition> getDefinitions() {
    return new ArrayList<UIDefinition>(UIDefinitionController.getInstance().getAllUIDefinitions());
  }
}
