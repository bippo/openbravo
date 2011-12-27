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

package org.openbravo.service.datasource.test;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.TemplateProcessor;
import org.openbravo.client.kernel.freemarker.FreemarkerTemplateProcessor;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.DataSource;
import org.openbravo.service.datasource.DataSourceComponent;
import org.openbravo.service.datasource.DataSourceComponentProvider;
import org.openbravo.service.datasource.DataSourceService;
import org.openbravo.service.datasource.DataSourceServiceProvider;

/**
 * Tests the correctness of the datasource javascript template.
 * 
 * @author mtaal
 */
public class DataSourceJavaScriptTest extends WeldBaseTest {
  private static final Logger log = Logger.getLogger(DataSourceJavaScriptTest.class);

  @Inject
  private DataSourceServiceProvider dataSourceServiceProvider;

  @Inject
  @ComponentProvider.Qualifier(DataSourceComponentProvider.QUALIFIER)
  private ComponentProvider dataSourceComponentProvider;

  @Inject
  @TemplateProcessor.Qualifier(FreemarkerTemplateProcessor.QUALIFIER)
  private TemplateProcessor templateProcessor;

  /**
   * Tests the generation of javascript for explicit datasources in the system.
   */
  @Test
  public void testJavaScriptGenerationDataSources() throws Exception {
    setSystemAdministratorContext();

    // final Template template = createSaveTemplate(
    // "/org/openbravo/service/datasource/templates/datasource", new ArrayList<Template>());

    int i = 0;
    for (DataSource dataSource : OBDal.getInstance().createQuery(DataSource.class, "").list()) {
      final DataSourceService dataSourceService = dataSourceServiceProvider
          .getDataSource(dataSource.getId());
      final DataSourceComponent component = (DataSourceComponent) dataSourceComponentProvider
          .getComponent(dataSourceService.getName(), new HashMap<String, Object>());
      component.setDataSourceService(dataSourceService);
      i++;
      if ((i % 2) == 0) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("_onlyGenerateCreateStatement", "true");
        component.setParameters(params);
      }
      final Map<String, Object> data = new HashMap<String, Object>();
      data.put("data", component);
      final String result = templateProcessor.process(dataSourceService.getTemplate(), data);
      System.err.println(result);
      log.debug(result);
    }
    // prevent actual save of the template
    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Tests the generation of javascript for a datasources based on a table. All entities are
   */
  @Test
  public void testJavaScriptGenerationAllEntities() throws Exception {
    setSystemAdministratorContext();

    // final Template template = createSaveTemplate(
    // "/org/openbravo/service/datasource/templates/datasource", new ArrayList<Template>());

    int i = 0;
    for (Entity entity : ModelProvider.getInstance().getModel()) {
      final DataSourceService dataSourceService = dataSourceServiceProvider.getDataSource(entity
          .getName());
      final DataSourceComponent component = (DataSourceComponent) dataSourceComponentProvider
          .getComponent(dataSourceService.getName(), new HashMap<String, Object>());
      component.setDataSourceService(dataSourceService);
      i++;
      if ((i % 2) == 0) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("_onlyGenerateCreateStatement", "true");
        component.setParameters(params);
      }
      final Map<String, Object> data = new HashMap<String, Object>();
      data.put("data", component);
      final String result = templateProcessor.process(dataSourceService.getTemplate(), data);
      log.debug(result);
    }
    // prevent actual save of the template
    OBDal.getInstance().rollbackAndClose();
  }

  // not used anymore as the template def is present in the database
  // private Template createSaveTemplate(String name, List<Template> dependencies) throws Exception
  // {
  //
  // // read the datasource module and set in development
  // final Module module = OBDal.getInstance().get(Module.class,
  // "A44B9BA75C354D8FB2E3F7D6EB6BFDC4");
  // module.setInDevelopment(true);
  //
  // final Template template = OBProvider.getInstance().get(Template.class);
  // template.setModule(module);
  // template.setName(name);
  // template.setActive(true);
  // template.setTemplateLanguage(TEMPLATE_LANGUAGE);
  // template.setComponentType(COMPONENT_TYPE);
  // template.setTemplate(readTemplateSource(name));
  //
  // for (Template dependency : dependencies) {
  // final TemplateDependency templateDependency = OBProvider.getInstance().get(
  // TemplateDependency.class);
  // templateDependency.setObclkerTemplate(template);
  // templateDependency.setDependsOnTemplate(dependency);
  // template.getOBCLKERTemplateDependencyList().add(templateDependency);
  // }
  // OBDal.getInstance().save(template);
  // return template;
  // }

  // private String readTemplateSource(String fileName) throws Exception {
  // final URL url = this.getClass().getResource(fileName + ".ftl");
  // final File file = new File(url.toURI());
  // return readFileAsString(file);
  // }
  //
  // private static String readFileAsString(File file) throws java.io.IOException {
  // byte[] buffer = new byte[(int) file.length()];
  // FileInputStream f = new FileInputStream(file);
  // f.read(buffer);
  // return new String(buffer);
  // }

}