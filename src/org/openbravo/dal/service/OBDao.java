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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.dal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.openbravo.base.structure.BaseOBObject;

/**
 * Util class for DAL
 * 
 * @author gorkaion
 * 
 */
public class OBDao {

  /**
   * Generic OBCriteria.
   * 
   * @param clazz
   *          Class (entity).
   * @param constraints
   *          List of hibernate Criterion instances which are used as filters
   * @return An OBCriteria object with the constraints.
   */
  public static <T extends BaseOBObject> OBCriteria<T> getFilteredCriteria(Class<T> clazz,
      Criterion... constraints) {
    OBCriteria<T> obc = OBDal.getInstance().createCriteria(clazz);
    for (Criterion c : constraints) {
      obc.add(c);
    }
    return obc;
  }

  /**
   * Returns a List of BaseOBOBjects of the Property identified by the property from the
   * BaseOBObject obj. This method enables the activeFilter so inactive BaseOBObjects are not
   * included on the returned List.
   * 
   * @param obj
   *          BaseOBObject from which the values are requested
   * @param property
   *          the name of the Property for which the value is requested
   */
  @SuppressWarnings("unchecked")
  public static <T extends BaseOBObject> List<T> getActiveOBObjectList(BaseOBObject obj,
      String property) {
    boolean isActiveFilterEnabled = OBDal.getInstance().isActiveFilterEnabled();
    if (!isActiveFilterEnabled) {
      OBDal.getInstance().enableActiveFilter();
    }
    try {
      return (List<T>) obj.get(property);
    } finally {
      if (!isActiveFilterEnabled) {
        OBDal.getInstance().disableActiveFilter();
      }
    }
  }

  /**
   * Parses the string of comma separated id's to return a List with the BaseOBObjects of the given
   * class. If there is an invalid id a null value is added to the List.
   * 
   * @param t
   *          class of the BaseOBObject the id's belong to
   * @param _IDs
   *          String containing the comma separated list of id's
   * @return a List object containing the parsed OBObjects
   */
  public static <T extends BaseOBObject> List<T> getOBObjectListFromString(Class<T> t, String _IDs) {
    String strBaseOBOBjectIDs = _IDs;
    final List<T> baseOBObjectList = new ArrayList<T>();
    if (strBaseOBOBjectIDs.startsWith("(")) {
      strBaseOBOBjectIDs = strBaseOBOBjectIDs.substring(1, strBaseOBOBjectIDs.length() - 1);
    }
    if (!strBaseOBOBjectIDs.equals("")) {
      strBaseOBOBjectIDs = StringUtils.remove(strBaseOBOBjectIDs, "'");
      StringTokenizer st = new StringTokenizer(strBaseOBOBjectIDs, ",", false);
      while (st.hasMoreTokens()) {
        String strBaseOBObjectID = st.nextToken().trim();
        baseOBObjectList.add((T) OBDal.getInstance().get(t, strBaseOBObjectID));
      }
    }
    return baseOBObjectList;
  }
}
