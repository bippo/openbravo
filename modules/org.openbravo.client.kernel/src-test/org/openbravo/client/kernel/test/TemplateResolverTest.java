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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.TemplateDependency;
import org.openbravo.client.kernel.TemplateResolver;
import org.openbravo.dal.service.OBDal;
import org.openbravo.test.base.BaseTest;

/**
 * Test the {@link TemplateResolver}.
 * 
 * Note the testcases assume that at least one template language (
 * {@link Template#getTemplateLanguage()}) is installed and at least one component type (
 * {@link Template#getComponentType()}) is available.
 * 
 * @author mtaal
 */

public class TemplateResolverTest extends BaseTest {
  private static final List<Template> EMPTY_LIST = Collections.emptyList();

  // read some default values used in the test
  private static String TEMPLATE_LANGUAGE;
  private static String COMPONENT_TYPE;

  private static void initializeStatics() {
    final Entity entity = ModelProvider.getInstance().getEntity(Template.ENTITY_NAME);
    final Property templateLanguageProperty = entity
        .getProperty(Template.PROPERTY_TEMPLATELANGUAGE);
    final Property componentTypeProperty = entity.getProperty(Template.PROPERTY_COMPONENTTYPE);

    TEMPLATE_LANGUAGE = templateLanguageProperty.getAllowedValues().iterator().next();
    COMPONENT_TYPE = componentTypeProperty.getAllowedValues().iterator().next();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // after super.setUp, must be done after initializing dal layer in super class
    initializeStatics();
  }

  /**
   * Tests template resolving with dependencies etc.
   * 
   * The following test data is build up: Say there is dependency tree: A depends on B and C, B
   * depends on D and E, C depends on F and G
   * 
   * Then the returned result is computed depth-first: D, E, B, F, G, C, A
   */
  public void testResolveDependencies() throws Exception {
    setSystemAdministratorContext();

    // create the test data
    // build the structure bottom up
    final Template templateF = createSaveTemplate("F");
    final Template templateG = createSaveTemplate("G");
    final Template templateD = createSaveTemplate("D");
    final Template templateE = createSaveTemplate("E");
    final Template templateB = createSaveTemplate("B",
        Arrays.asList(new Template[] { templateD, templateE }));
    final Template templateC = createSaveTemplate("C",
        Arrays.asList(new Template[] { templateF, templateG }));
    final Template templateA = createSaveTemplate("A",
        Arrays.asList(new Template[] { templateB, templateC }));

    OBDal.getInstance().flush();

    // now do resolving
    check(TemplateResolver.getInstance().resolve(templateB), new String[] { "D", "E", "B" });
    check(TemplateResolver.getInstance().resolve(templateC), new String[] { "F", "G", "C" });
    check(TemplateResolver.getInstance().resolve(templateA), new String[] { "D", "E", "B", "F",
        "G", "C", "A" });

    // rollback to not save the new templates
    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Tests dependencies and overrides.
   * 
   * The following test data is build up: Say there is dependency tree: A depends on B and C, B
   * depends on D and E, C depends on F and G
   * 
   * Then the B template is overridden by H which depends on I and J.
   * 
   * Then the returned result is computed depth-first: I, J, H, F, G, C, A
   * 
   * The last test overrides template A with a template K. The resolve should just return K.
   * 
   * @throws Exception
   */
  public void testResolveDependenciesWithOverrides() throws Exception {
    setUserContext("0");

    // create the test data
    // build the structure bottom up
    final Template templateF = createSaveTemplate("F");
    final Template templateG = createSaveTemplate("G");
    final Template templateD = createSaveTemplate("D");
    final Template templateE = createSaveTemplate("E");
    final Template templateB = createSaveTemplate("B",
        Arrays.asList(new Template[] { templateD, templateE }));
    final Template templateC = createSaveTemplate("C",
        Arrays.asList(new Template[] { templateF, templateG }));
    final Template templateA = createSaveTemplate("A",
        Arrays.asList(new Template[] { templateB, templateC }));

    // create the override structur
    final Template templateI = createSaveTemplate("I");
    final Template templateJ = createSaveTemplate("J");
    final Template templateH = createSaveTemplate("H",
        Arrays.asList(new Template[] { templateI, templateJ }));
    templateH.setOverridesTemplate(templateB);
    OBDal.getInstance().save(templateH);

    OBDal.getInstance().flush();

    // now do resolving
    check(TemplateResolver.getInstance().resolve(templateB), new String[] { "I", "J", "H" });
    check(TemplateResolver.getInstance().resolve(templateC), new String[] { "F", "G", "C" });
    check(TemplateResolver.getInstance().resolve(templateA), new String[] { "I", "J", "H", "F",
        "G", "C", "A" });

    // now override the top of the inheritance structure
    final Template templateK = createSaveTemplate("K");
    templateK.setOverridesTemplate(templateA);
    OBDal.getInstance().save(templateK);
    OBDal.getInstance().flush();
    check(TemplateResolver.getInstance().resolve(templateA), new String[] { "K" });

    // rollback to not save the new templates
    OBDal.getInstance().rollbackAndClose();
  }

  private void check(List<Template> templates, String[] names) {
    assertEquals(names.length, templates.size());
    int index = 0;
    for (String name : names) {
      assertEquals(name, templates.get(index++).getName());
    }
  }

  private Template createSaveTemplate(String name) {
    return createSaveTemplate(name, EMPTY_LIST);
  }

  private Template createSaveTemplate(String name, List<Template> dependencies) {
    final Template template = OBProvider.getInstance().get(Template.class);
    template.setName(name);
    template.setActive(true);
    template.setTemplateLanguage(TEMPLATE_LANGUAGE);
    template.setComponentType(COMPONENT_TYPE);

    for (Template dependency : dependencies) {
      final TemplateDependency templateDependency = OBProvider.getInstance().get(
          TemplateDependency.class);
      templateDependency.setObclkerTemplate(template);
      templateDependency.setDependsOnTemplate(dependency);
      template.getOBCLKERTemplateDependencyList().add(templateDependency);
    }
    OBDal.getInstance().save(template);
    return template;
  }
}