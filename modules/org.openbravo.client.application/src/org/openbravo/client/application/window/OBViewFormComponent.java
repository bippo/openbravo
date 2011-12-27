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
package org.openbravo.client.application.window;

import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;

/**
 * The backing bean for generating the OBViewForm client-side representation.
 * 
 * @author mtaal
 * @author iperdomo
 */

public class OBViewFormComponent extends BaseTemplateComponent {

  private static final String TEMPLATE_ID = "C1D176407A354A40815DC46D24D70EB8";

  private String parentProperty;

  private String templateId = TEMPLATE_ID;

  private OBViewFieldHandler fieldHandler;

  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, templateId);
  }

  public String getParentProperty() {
    return parentProperty;
  }

  public void setParentProperty(String parentProperty) {
    this.parentProperty = parentProperty;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public OBViewFieldHandler getFieldHandler() {
    return fieldHandler;
  }

  public void setFieldHandler(OBViewFieldHandler fieldHandler) {
    this.fieldHandler = fieldHandler;
  }
}
