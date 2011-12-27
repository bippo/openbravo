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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.openbravo.base.validation.ValidationException;

/**
 * Processes a template with data passed into it. A template processor is used as a singleton so
 * multiple threads may use one template processor at the same time.
 * 
 * @author mtaal
 */
public interface TemplateProcessor {

  /**
   * Validates if a certain template object is valid. If not valid then a
   * {@link ValidationException} is thrown.
   * 
   * @param template
   *          the template to validate
   */
  public void validate(Template template) throws ValidationException;

  /**
   * Processes a template and returns the result as a string.
   * 
   * @param template
   *          the template to process.
   * @param data
   *          the data to use in the template
   * @return the template process result.
   */
  public String process(Template template, Map<String, Object> data);

  /**
   * Is called by the kernel to signal that a template has changed and that TemplateProcessors
   * should clear their cache and reload the templates.
   */
  public void clearCache();

  /**
   * @return the template language supported by this template processor
   */
  public String getTemplateLanguage();

  /**
   * Defines the qualifier for instance selection.
   * 
   * @author mtaal
   */
  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
  public @interface Qualifier {
    String value();
  }

  /**
   * A class used to select the correct implementation.
   * 
   * @author mtaal
   */
  @SuppressWarnings("all")
  public static class Selector extends AnnotationLiteral<TemplateProcessor.Qualifier> implements
      TemplateProcessor.Qualifier {
    private static final long serialVersionUID = 1L;

    final String value;

    public Selector(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
  }

  @ApplicationScoped
  public static class Registry {

    @Inject
    @Any
    private Instance<TemplateProcessor> templateProcessors;

    private static TemplateProcessor lastProcessor = null;

    public TemplateProcessor get(String language) {
      final TemplateProcessor localLastProcessor = lastProcessor;
      if (localLastProcessor != null && localLastProcessor.getTemplateLanguage().equals(language)) {
        return localLastProcessor;
      }
      for (TemplateProcessor processor : templateProcessors) {
        if (processor.getTemplateLanguage().equals(language)) {
          lastProcessor = processor;
          return processor;
        }
      }
      throw new IllegalArgumentException("Template processor for language " + language
          + " not found");
    }
  }

}
