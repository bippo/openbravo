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

package org.openbravo.client.kernel.freemarker.test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.validation.ValidationException;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.TemplateDependency;
import org.openbravo.client.kernel.TemplateProcessor;
import org.openbravo.client.kernel.freemarker.FreemarkerTemplateProcessor;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;

/**
 * Test the {@link FreemarkerTemplateProcessor}, tests both template validation as well as template
 * processing.
 * 
 * The test cases make use of the test templates present in the same package.
 * 
 * @author mtaal
 */

public class FreemarkerTemplateProcessorTest extends WeldBaseTest {
  private static final List<Template> EMPTY_LIST = Collections.emptyList();

  private static String TEMPLATE_LANGUAGE;
  private static String COMPONENT_TYPE;

  @Inject
  @TemplateProcessor.Qualifier(FreemarkerTemplateProcessor.QUALIFIER)
  private TemplateProcessor templateProcessor;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    // after super.setUp, must be done after initializing dal layer in super class
    final Entity entity = ModelProvider.getInstance().getEntity(Template.ENTITY_NAME);
    final Property componentTypeProperty = entity.getProperty(Template.PROPERTY_COMPONENTTYPE);

    TEMPLATE_LANGUAGE = templateProcessor.getTemplateLanguage();
    COMPONENT_TYPE = componentTypeProperty.getAllowedValues().iterator().next();
  }

  /**
   * Tests the test1, test2 and test3 templates. The template files are converted into template
   * objects, and then validated and processed using the {@link TemplateProcessor}.
   */
  @Test
  public void testTemplateReadCombineAndProcess() throws Exception {
    setSystemAdministratorContext();
    final Template template = createSaveTemplates("");
    templateProcessor.validate(template);
    final Map<String, Object> data = new HashMap<String, Object>();
    data.put("name", "openbravo");
    data.put("obContext", OBContext.getOBContext());
    final String result = templateProcessor.process(template, data);
    assertTrue(result.contains("15"));
    assertTrue(result.contains("This is a test"));
    assertTrue(result.contains("name: openbravo"));
    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Tests the {@link TemplateProcessor#validate(Template)} by passing in an invalid test cases:
   * invalid_test. This template has an invalid structure.
   */
  @Test
  public void testTemplateValidation() throws Exception {
    setSystemAdministratorContext();
    final Template template = createSaveTemplate("invalid_test");
    try {
      templateProcessor.validate(template);
      fail("Template validation not working");
    } catch (ValidationException e) {
      // success
    } catch (Throwable t) {
      fail(t.getMessage());
    }
    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Tests if the exception handler indeed works correctly and is lenient about undefined values.
   * See {@link TemplateProcessor}.
   */
  @Test
  public void testLenientTemplateProcessing() throws Exception {
    setSystemAdministratorContext();
    final Template template = createSaveTemplate("lenient_test");
    final String result = templateProcessor.process(template, new HashMap<String, Object>());
    assertTrue(result.contains("15"));
    OBDal.getInstance().rollbackAndClose();
  }

  private Template createSaveTemplates(String prefix) throws Exception {
    final Template templateOne = createSaveTemplate(prefix + "test1");
    final Template templateTwo = createSaveTemplate(prefix + "test2");
    final Template templateThree = createSaveTemplate(prefix + "test3",
        Arrays.asList(new Template[] { templateOne, templateTwo }));
    return templateThree;
  }

  private Template createSaveTemplate(String name) throws Exception {
    return createSaveTemplate(name, EMPTY_LIST);
  }

  private Template createSaveTemplate(String name, List<Template> dependencies) throws Exception {
    final Template template = OBProvider.getInstance().get(Template.class);
    template.setName(name);
    template.setActive(true);
    template.setTemplateLanguage(TEMPLATE_LANGUAGE);
    template.setComponentType(COMPONENT_TYPE);
    template.setTemplate(readTemplateSource(name));
    template.setModule(getModule());

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

  private String readTemplateSource(String fileName) throws Exception {
    final URL url = this.getClass().getResource(fileName + ".ftl");
    final File file = new File(url.toURI());
    return readFileAsString(file);
  }

  private static String readFileAsString(File file) throws java.io.IOException {
    byte[] buffer = new byte[(int) file.length()];
    FileInputStream f = new FileInputStream(file);
    f.read(buffer);
    return new String(buffer);
  }

  private Module getModule() {
    final Module module = OBDal.getInstance().get(Module.class, "4B828F4D03264080AA1D2057B13F613C");
    module.setInDevelopment(true);
    return module;
  }
}