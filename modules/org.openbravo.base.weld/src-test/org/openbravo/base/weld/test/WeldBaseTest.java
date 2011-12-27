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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.base.weld.test;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.test.base.BaseTest;

/**
 * Base test for weld, provides access to the weld container.
 * 
 * @author mtaal
 */
@RunWith(Arquillian.class)
public class WeldBaseTest extends BaseTest {

  @Deployment
  public static JavaArchive createTestArchive() {
    final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar");
    archive.as(ExplodedImporter.class).importDirectory(sourcePath + "/build/classes");
    archive.addDirectory(sourcePath + "/WebContent/WEB-INF/lib");
    return archive;
  }

  @SuppressWarnings("serial")
  private static final AnnotationLiteral<Any> ANY = new AnnotationLiteral<Any>() {
  };

  @Inject
  private BeanManager beanManager;

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @SuppressWarnings("unchecked")
  protected <U extends Object> U getWeldComponent(Class<U> clz) {

    final Bean<?> bean = beanManager.getBeans(clz, ANY).iterator().next();

    return (U) beanManager.getReference(bean, clz, beanManager.createCreationalContext(bean));
  }
}
