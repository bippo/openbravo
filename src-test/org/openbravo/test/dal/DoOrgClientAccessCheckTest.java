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

package org.openbravo.test.dal;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.test.base.BaseTest;

public class DoOrgClientAccessCheckTest extends BaseTest {

  public void testNormalAdminMode() {

    setTestUserContext();
    OBContext.setAdminMode();
    try {
      insertImage("0");
    } catch (final OBSecurityException e) {
      fail("Security shouldn't fail in normal admin mode");
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void testDoOrgClientAccessCheckWrongClient() {

    setTestUserContext();
    OBContext.setAdminMode(true);
    try {
      // This should fail, because we are using admin mode with Client/Org filtering, and the client
      // is wrong
      insertImage("0");
      fail("No security check");
    } catch (final OBSecurityException e) {
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void testDoOrgClientAccessCheck() {

    setTestUserContext();
    OBContext.setAdminMode(true);
    try {
      // This should work, even if we are filtering, because the client is compatible
      insertImage(TEST_CLIENT_ID);
    } catch (final OBSecurityException e) {
      fail("Security shouldn't fail if client/org is used");
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void testNormalUserMode() {

    setTestUserContext();
    try {
      insertImage("0");
      fail("No security check");
    } catch (final OBSecurityException e) {
      // this is correct, as a normal user shouldn't be able to write the image in client 0
    }
  }

  private void insertImage(String clientId) {
    Client client0 = OBDal.getInstance().get(Client.class, clientId);
    Organization org0 = OBDal.getInstance().get(Organization.class, "0");
    Image image = OBProvider.getInstance().get(Image.class);
    image.setName("ImageTest");
    image.setClient(client0);
    image.setOrganization(org0);
    OBDal.getInstance().save(image);
    OBDal.getInstance().flush();
    OBDal.getInstance().remove(image);
    OBDal.getInstance().flush();
  }

}
