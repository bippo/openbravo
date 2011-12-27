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
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.domain.Preference;

/**
 * Checks there is only one selected preference for the current visibility.
 * 
 */
public class SL_Preference extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  private static final ValueListFilter booleanFilter = new ValueListFilter("Y", "N", "");
  private static final IsIDFilter idFilter = new IsIDFilter();

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    boolean selected = info.getStringParameter("inpselected", booleanFilter).equals("Y");
    if (selected) {
      OBCriteria<Preference> qPref = OBDal.getInstance().createCriteria(Preference.class);

      String prefId = info.getStringParameter("inpadPreferenceId", idFilter);
      if (!prefId.isEmpty()) {
        qPref.add(Restrictions.ne(Preference.PROPERTY_ID, prefId));
      }

      if (info.getStringParameter("inpispropertylist", booleanFilter).equals("Y")) {
        qPref.add(Restrictions.eq(Preference.PROPERTY_PROPERTY,
            info.getStringParameter("inpproperty", null)));
      } else {
        qPref.add(Restrictions.eq(Preference.PROPERTY_ATTRIBUTE,
            info.getStringParameter("inpattribute", null)));
      }

      qPref.add(Restrictions.eq(Preference.PROPERTY_SELECTED, true));

      String client = info.getStringParameter("inpvisibleatClientId", idFilter);
      if (client.isEmpty() || client.equals("0")) {
        qPref.add(Restrictions.or(Restrictions.isNull(Preference.PROPERTY_VISIBLEATCLIENT),
            Restrictions.eq(Preference.PROPERTY_VISIBLEATCLIENT + ".id", "0")));
      } else {
        qPref.add(Restrictions.eq(Preference.PROPERTY_VISIBLEATCLIENT + ".id", client));
      }

      String org = info.getStringParameter("inpvisibleatOrgId", idFilter);
      if (org.isEmpty() || org.equals("0")) {
        qPref.add(Restrictions.or(Restrictions.isNull(Preference.PROPERTY_VISIBLEATORGANIZATION),
            Restrictions.eq(Preference.PROPERTY_VISIBLEATORGANIZATION + ".id", "0")));
      } else {
        qPref.add(Restrictions.eq(Preference.PROPERTY_VISIBLEATORGANIZATION + ".id", org));
      }

      String user = info.getStringParameter("inpadUserId", idFilter);
      if (user.isEmpty()) {
        qPref.add(Restrictions.isNull(Preference.PROPERTY_USERCONTACT));
      } else {
        qPref.add(Restrictions.eq(Preference.PROPERTY_USERCONTACT + ".id", user));
      }

      String role = info.getStringParameter("inpvisibleatRoleId", idFilter);
      if (role.isEmpty()) {
        qPref.add(Restrictions.isNull(Preference.PROPERTY_VISIBLEATROLE));
      } else {
        qPref.add(Restrictions.eq(Preference.PROPERTY_VISIBLEATROLE + ".id", role));
      }

      String window = info.getStringParameter("inpadWindowId", idFilter);
      if (window.isEmpty()) {
        qPref.add(Restrictions.isNull(Preference.PROPERTY_WINDOW));
      } else {
        qPref.add(Restrictions.eq(Preference.PROPERTY_WINDOW + ".id", window));
      }

      if (qPref.count() > 0) {
        info.addResult("inpselected", "N");
        info.addResult(
            "MESSAGE",
            Utility.messageBD(this, "MultipleSelectedPreferences", OBContext.getOBContext()
                .getLanguage().getLanguage()));
      }
    }
  }
}
