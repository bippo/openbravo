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
package org.openbravo.client.myob;

import javax.inject.Inject;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

/**
 * Creates the properties list which is initially loaded in the client.
 * 
 * @author mtaal
 */
public class MyOpenbravoWidgetComponent extends BaseComponent {

  static final String COMPONENT_ID = "MyOpenbravoWidgetComponent";
  public static final String CLASSNAMEPARAMETER = "widgetClassName";

  @Inject
  private MyOBUtils myOBUtils;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#getId()
   */
  public String getId() {
    return COMPONENT_ID;
  }

  public String generate() {
    OBContext.setAdminMode();
    try {
      final JSONObject jsonObject = new JSONObject(RequestContext.get().getRequestContent());
      getParameters().put("widgetProperties", jsonObject);

      final String widgetId = jsonObject.getString(CLASSNAMEPARAMETER);
      if (widgetId == null) {
        throw new OBException("No widgetid defined for request " + getParameters());
      }

      final WidgetClass widgetClass = OBDal.getInstance().get(WidgetClass.class,
          (widgetId.startsWith(KernelConstants.ID_PREFIX) ? widgetId.substring(1) : widgetId));
      if (widgetClass == null) {
        throw new OBException("No widgetclass found using id " + widgetId);
      }

      try {
        final WidgetProvider widgetProvider = myOBUtils.getWidgetProvider(widgetClass);
        widgetProvider.setParameters(getParameters());
        return widgetProvider.generate();
      } catch (Exception e) {
        String strJavaClass = (widgetClass.getWidgetSuperclass() != null) ? widgetClass
            .getWidgetSuperclass().getJavaClass() : widgetClass.getJavaClass();
        throw new OBException("Not able to create widgetprovider " + strJavaClass, e);
      }
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public Object getData() {
    return this;
  }
}
