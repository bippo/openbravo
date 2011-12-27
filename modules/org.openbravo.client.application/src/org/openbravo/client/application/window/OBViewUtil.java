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

import java.util.List;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.FieldTrl;

/**
 * Utility methods used in generating Openbravo view representations.
 * 
 * @author mtaal
 */
public class OBViewUtil {

  public static final Element createdElement = OBDal.getInstance().get(Element.class, "245");
  public static final Element createdByElement = OBDal.getInstance().get(Element.class, "246");
  public static final Element updatedElement = OBDal.getInstance().get(Element.class, "607");
  public static final Element updatedByElement = OBDal.getInstance().get(Element.class, "608");

  /**
   * Method for retrieving the label of a field on the basis of the current language of the user.
   * 
   * @see #getLabel(BaseOBObject, List)
   */
  public static String getLabel(Field fld) {
    return getLabel(fld, fld.getADFieldTrlList());
  }

  /**
   * Generic method for computing the translated label/title. It assumes that the trlObjects have a
   * property called language and name and the owner object a property called name.
   * 
   * @param owner
   *          the owner of the trlObjects (for example Field)
   * @param trlObjects
   *          the trl objects (for example FieldTrl)
   * @return a translated name if found or otherwise the name of the owner
   */
  public static String getLabel(BaseOBObject owner, List<?> trlObjects) {
    return getLabel(owner, trlObjects, Field.PROPERTY_NAME);
  }

  public static String getLabel(BaseOBObject owner, List<?> trlObjects, String propertyName) {
    final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
    for (Object o : trlObjects) {
      final BaseOBObject trlObject = (BaseOBObject) o;
      final String trlLanguageId = (String) DalUtil
          .getId(trlObject.get(FieldTrl.PROPERTY_LANGUAGE));
      if (trlLanguageId.equals(userLanguageId)) {
        return (String) trlObject.get(propertyName);
      }
    }
    return (String) owner.get(propertyName);
  }
}
