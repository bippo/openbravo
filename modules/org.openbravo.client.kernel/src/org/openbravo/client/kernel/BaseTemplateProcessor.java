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
package org.openbravo.client.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains generic code for {@link TemplateProcessor} instances.
 * 
 * The generics parameter T is the class of the template as it exists in the specific templating
 * language.
 * 
 * @author mtaal
 */
public abstract class BaseTemplateProcessor<T extends Object> implements TemplateProcessor {

  private Map<String, T> templateCache = new ConcurrentHashMap<String, T>();

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.client.kernel.TemplateProcessor#process(org.openbravo.client.kernel.ComponentTemplate
   * , java.util.Map)
   */
  public String process(Template template, Map<String, Object> data) {

    T templateImplementation = getTemplateImplementation(template);
    if (templateImplementation == null) {
      final String source = createTemplateSource(template);
      templateImplementation = createSetFreeMarkerTemplateInCache(template, source);
    }
    return processTemplate(templateImplementation, data);
  }

  /**
   * Run a template implementation for specific data set.
   * 
   * @param templateImplementation
   *          the template to process
   * @param data
   *          the data which should be passed to the template
   * @return the template output
   */
  protected abstract String processTemplate(T templateImplementation, Map<String, Object> data);

  /**
   * Return the template language specific implementation of the template.
   * 
   * @param template
   *          the template stored in the DB
   * @return the template implementation in the template language
   */
  protected synchronized T getTemplateImplementation(Template template) {
    // can not be cached
    if (template.getId() == null) {
      return null;
    }
    final Map<String, T> localTemplateCache = templateCache;
    T templateImplementation = localTemplateCache.get(template.getId());
    if (templateImplementation != null) {
      return templateImplementation;
    }
    return null;
  }

  /**
   * Creates the template source taking into account overriding templates and pre-prending
   * depends-on templates.
   * 
   * @param template
   *          the template to create the source for
   * @return a complete template source
   * @see TemplateResolver#resolve(Template)
   */
  protected String createTemplateSource(Template template) {
    final List<Template> resolvedTemplates = TemplateResolver.getInstance().resolve(template);
    final StringBuilder source = new StringBuilder();
    for (Template resolvedTemplate : resolvedTemplates) {
      if (resolvedTemplate.getTemplateClasspathLocation() != null) {
        source.append(readTemplateSourceFromClasspath(resolvedTemplate
            .getTemplateClasspathLocation()) + "\n");
      } else {
        source.append(resolvedTemplate.getTemplate() + "\n");
      }
    }
    return source.toString();
  }

  /**
   * Checks the cache if there is already a template implementation for a certain template. If so
   * that one is returned. If not then a new implementation is created.
   * 
   * @param template
   * @param source
   * @return
   */
  protected synchronized T createSetFreeMarkerTemplateInCache(Template template, String source) {
    final T specificTemplate = createTemplateImplementation(template, source);

    // do not cache if the module is in development
    if (template.getModule() != null && template.getModule().isInDevelopment() != null
        && template.getModule().isInDevelopment()) {
      return specificTemplate;
    }

    if (template.getId() != null) {
      final Map<String, T> localTemplateCache = templateCache;
      localTemplateCache.put(template.getId(), specificTemplate);
    }

    return specificTemplate;
  }

  /**
   * To be implemented by the subclass. Based on the template from the DB and the complete template
   * source, create a language specific template implementation instance.
   * 
   * @param template
   *          the template object from the DB
   * @param source
   *          the complete source (after resolving and including all dependencies)
   * @return the template implementation
   */
  protected abstract T createTemplateImplementation(Template template, String source);

  /**
   * Reads the template source from the classpath
   * 
   * @param path
   *          the path to the template file
   * @return the read template
   */
  protected String readTemplateSourceFromClasspath(String path) {
    try {
      final URL url = this.getClass().getResource(path.trim().replace(" ", "+"));
      final File file = new File(url.toURI());
      return readFileAsString(file);
    } catch (Exception e) {
      throw new IllegalArgumentException("Exception for path " + path, e);
    }
  }

  private String readFileAsString(File file) throws java.io.IOException {
    byte[] buffer = new byte[(int) file.length()];
    FileInputStream f = new FileInputStream(file);
    f.read(buffer);
    return new String(buffer);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.TemplateProcessor#clearCache()
   */
  public void clearCache() {
    templateCache = new ConcurrentHashMap<String, T>();
  }
}
