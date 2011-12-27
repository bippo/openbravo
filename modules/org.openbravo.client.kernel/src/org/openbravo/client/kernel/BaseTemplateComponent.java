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
package org.openbravo.client.kernel;

import javax.inject.Inject;

import org.openbravo.dal.core.OBContext;

/**
 * This component implementation uses a template to generate its client representation
 * (java-script). The template is provided by the abstract method {@link #getComponentTemplate()}.
 * 
 * @see Template
 * @see TemplateProcessor
 * @see TemplateProcessor.Registry
 * @author mtaal
 */
public class BaseTemplateComponent extends BaseComponent {
  public static final String BASE_QUALIFIER = "Base";

  public static final String DATA_PARAMETER = "data";

  private Template componentTemplate;

  @Inject
  private TemplateProcessor.Registry templateProcessRegistry;

  @Override
  public String generate() {
    OBContext.setAdminMode();
    try {
      if (getData() != null) {
        getParameters().put(DATA_PARAMETER, getData());
      }

      final Template template = getComponentTemplate();
      final TemplateProcessor templateProcessor = templateProcessRegistry.get(template
          .getTemplateLanguage());
      return templateProcessor.process(template, getParameters());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected Template getComponentTemplate() {
    return componentTemplate;
  }

  public void setComponentTemplate(Template componentTemplate) {
    this.componentTemplate = componentTemplate;
  }

  /**
   * @return returns this instance
   * @see org.openbravo.client.kernel.BaseComponent#getData()
   */
  public Object getData() {
    return this;
  }

}
