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
package org.openbravo.test.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.test.base.BaseTest;

/**
 * Tests registered classes in Application Dictionary
 * 
 * @author iperdomo
 */
public class ClassLoaderTest extends BaseTest {

  private static final Logger log = Logger.getLogger(ClassLoaderTest.class);

  /**
   * Test if all registered classes in Application Dictionary can be loaded. Consistency test to
   * have a clean web.xml
   */
  public void testModelObject() {

    final List<String> notFoundClasses = new ArrayList<String>();

    setSystemAdministratorContext();

    // "S" - "Servlet"
    // "C" - "ContextParam"
    // "L" - "Listener"
    // "ST" - "Session timeout"
    // "F" - "Filter"
    // "R" - "Resource"

    final String[] in = { "L", "F" };

    // Checking listener and filters classes
    OBCriteria<ModelImplementation> obc = OBDal.getInstance().createCriteria(
        ModelImplementation.class);
    obc.add(Restrictions.in(ModelImplementation.PROPERTY_OBJECTTYPE, in));

    for (ModelImplementation mi : obc.list()) {
      try {
        Class.forName(mi.getJavaClassName());
      } catch (ClassNotFoundException e) {
        notFoundClasses.add(mi.getId() + " : " + mi.getJavaClassName());
      }
    }

    // Checking manual servlets
    obc = OBDal.getInstance().createCriteria(ModelImplementation.class);
    obc.add(Restrictions.eq(ModelImplementation.PROPERTY_OBJECTTYPE, "S"));
    obc.add(Restrictions.isNull(ModelImplementation.PROPERTY_TAB));
    obc.add(Restrictions.isNull(ModelImplementation.PROPERTY_SPECIALFORM));
    obc.add(Restrictions.isNull(ModelImplementation.PROPERTY_PROCESS));

    for (ModelImplementation mi : obc.list()) {
      try {
        if (mi.getId().equals("801180")) {
          // Ugly hack! check issue https://issues.openbravo.com/view.php?id=12429
          continue;
        }
        Class.forName(mi.getJavaClassName());
      } catch (ClassNotFoundException e) {
        notFoundClasses.add(mi.getId() + " : " + mi.getJavaClassName());
      }
    }

    // Checking servlets associated to forms
    OBQuery<ModelImplementation> obq = OBDal.getInstance().createQuery(ModelImplementation.class,
        "objectType = 'S' and specialForm is not null and specialForm.active = true");

    for (ModelImplementation mi : obq.list()) {
      try {
        Class.forName(mi.getJavaClassName());
      } catch (ClassNotFoundException e) {
        notFoundClasses.add(mi.getId() + " : " + mi.getJavaClassName());
      }
    }

    // Check servlets associated to processes/reports
    obq = OBDal.getInstance().createQuery(ModelImplementation.class,
        "objectType = 'S' and process is not null and process.active = true");

    for (ModelImplementation mi : obq.list()) {
      try {
        Class.forName(mi.getJavaClassName());
      } catch (ClassNotFoundException e) {
        notFoundClasses.add(mi.getId() + " : " + mi.getJavaClassName());
      }
    }

    // Checking servlets associated to tabs
    obq = OBDal.getInstance().createQuery(ModelImplementation.class,
        "objectType = 'S' and tab is not null and tab.active = true and tab.window.active = true");

    for (ModelImplementation mi : obq.list()) {
      try {
        Class.forName(mi.getJavaClassName());
      } catch (ClassNotFoundException e) {
        notFoundClasses.add(mi.getId() + " : " + mi.getJavaClassName());
      }
    }

    if (notFoundClasses.size() > 0) {
      for (String nf : notFoundClasses) {
        log.error(nf);
      }
    }
    assertEquals(0, notFoundClasses.size());
  }
}
