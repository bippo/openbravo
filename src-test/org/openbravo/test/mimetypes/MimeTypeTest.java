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
package org.openbravo.test.mimetypes;

import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.MimeTypeUtil;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.test.base.BaseTest;

/**
 * Simple test for MimeTypeUtil class
 * 
 * @author iperdomo
 */
public class MimeTypeTest extends BaseTest {

  public void testGetImageMimeTypes() {
    setSystemAdministratorContext();

    OBCriteria<Image> obc = OBDal.getInstance().createCriteria(Image.class);
    for (Image img : obc.list()) {
      byte[] imageBytes = img.getBindaryData();
      if (imageBytes != null) {
        try {
          String t = MimeTypeUtil.getInstance().getMimeTypeName(imageBytes);
          System.out.println(t);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
