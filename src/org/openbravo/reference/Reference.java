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
package org.openbravo.reference;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.reference.ui.UIReference;

public class Reference {
  private static final Logger log = Logger.getLogger(Reference.class);

  @SuppressWarnings("unchecked")
  public static UIReference getUIReference(String referenceId, String subreferenceID) {
    String implemenationClass;

    org.openbravo.model.ad.domain.Reference ref = null;
    OBContext.setAdminMode();
    try {
      if (subreferenceID != null && !subreferenceID.equals("")) {
        ref = OBDal.getInstance()
            .get(org.openbravo.model.ad.domain.Reference.class, subreferenceID);
      }

      if (ref != null) {
        implemenationClass = ref.getImpl();
        if (implemenationClass == null) {
          if (ref.getParentReference() != null) {
            implemenationClass = ref.getParentReference().getImpl();
          } else {
            log.error("No reference implementation found for " + ref);
          }
        }
      } else {
        ref = OBDal.getInstance().get(org.openbravo.model.ad.domain.Reference.class, referenceId);
        implemenationClass = ref.getImpl();
      }

      try {
        if (implemenationClass != null) {
          Class<UIReference> c = (Class<UIReference>) Class.forName(implemenationClass);
          @SuppressWarnings("rawtypes")
          Constructor constructor = c.getConstructor(new Class[] { String.class, String.class });
          String params[] = new String[2];
          params[0] = referenceId;
          params[1] = subreferenceID;
          return (UIReference) constructor.newInstance((Object[]) params);
        } else {
          return new UIReference(referenceId, subreferenceID);
        }
      } catch (Exception e) {
        log.error("Error getting class for reference " + referenceId + ", subreference "
            + subreferenceID + ". Getting default UIReference.", e);
        return new UIReference(referenceId, subreferenceID);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
