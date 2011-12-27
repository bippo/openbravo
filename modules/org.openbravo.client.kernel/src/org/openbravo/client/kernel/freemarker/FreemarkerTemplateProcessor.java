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
package org.openbravo.client.kernel.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.validation.ValidationException;
import org.openbravo.client.kernel.BaseTemplateProcessor;
import org.openbravo.client.kernel.Template;
import org.openbravo.client.kernel.TemplateProcessor;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Implements the {@link TemplateProcessor} for the <a
 * href="http://www.freemarker.org">freemarker</a> templating engine.
 * 
 * @author mtaal
 */
@ApplicationScoped
@TemplateProcessor.Qualifier(FreemarkerTemplateProcessor.QUALIFIER)
public class FreemarkerTemplateProcessor extends
    BaseTemplateProcessor<freemarker.template.Template> {
  public static final String QUALIFIER = "OBCLFRE_Freemarker";

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseTemplateProcessor#processTemplate(java.lang.Object,
   * java.util.Map)
   */
  protected String processTemplate(freemarker.template.Template templateImplementation,
      Map<String, Object> data) {
    try {
      final StringWriter output = new StringWriter();
      templateImplementation.process(data, output);
      return output.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } catch (TemplateException e) {
      throw new IllegalStateException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.client.kernel.BaseTemplateProcessor#createTemplateImplementation(org.openbravo
   * .client.kernel.Template, java.lang.String)
   */
  protected freemarker.template.Template createTemplateImplementation(Template template,
      String source) {
    try {
      return new freemarker.template.Template("template", new StringReader(source),
          getNewConfiguration());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openbravo.client.kernel.TemplateProcessor#validate(org.openbravo.client.kernel.Template)
   */
  public void validate(Template template) throws ValidationException {
    try {
      final String source = createTemplateSource(template);
      new freemarker.template.Template("template", new StringReader(source), getNewConfiguration());
    } catch (Exception e) {
      final Entity entity = ModelProvider.getInstance().getEntity(Template.ENTITY_NAME);
      final Property templateProperty = entity.getProperty(Template.PROPERTY_TEMPLATE);
      final ValidationException ve = new ValidationException();
      ve.addMessage(templateProperty, e.getMessage());
      throw ve;
    }
  }

  private Configuration getNewConfiguration() {
    final Configuration cfg = new Configuration();
    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setTemplateExceptionHandler(new OBExceptionHandler());
    return cfg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.TemplateProcessor#getTemplateLanguage()
   */
  public String getTemplateLanguage() {
    return QUALIFIER;
  }

  /**
   * This exception handler is lenient when values are undefined. In that case nothing is printed or
   * returned.
   */
  private class OBExceptionHandler implements TemplateExceptionHandler {

    public void handleTemplateException(TemplateException te, Environment env, java.io.Writer out)
        throws TemplateException {
      final String msg = te.getMessage().toLowerCase();
      // a fairly rough method to test the exception message...
      boolean isUndefinedException = msg.contains("expression") && msg.contains("undefined");
      if (isUndefinedException) {
        return;
      }
      // in all other cases stop
      throw te;
      // try {
      // out.write("[ERROR: " + te.getMessage() + "]");
      // } catch (IOException e) {
      // throw new TemplateException("Failed to print error message. Cause: " + e, env);
      // }
    }
  }
}
