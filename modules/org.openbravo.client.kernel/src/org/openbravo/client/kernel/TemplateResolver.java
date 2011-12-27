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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * Resolves templates, meaning that it reads all dependencies of a template (and the dependencies of
 * the dependencies etc.) and also takes care of using overriding templates. So each template is
 * replaced by its overriding template in the result list. Templates are returned in dependency
 * order. This means the following.
 * <p/>
 * Say there is dependency tree: A depends on B and C, B depends on D and E, C depends on F and G
 * 
 * Then the returned result is computed depth-first: D, E, B, F, G, C, A
 * 
 * During the resolving process the overriding takes precedence over dependency. This means that if
 * a template is overridden then the dependencies of the overriding template are used and not the
 * dependencies of the overridden template.
 * 
 * Note: the resolving process accesses the database several times. It makes sense to cache the
 * result.
 * 
 * @author mtaal
 * @see Template#getOBCLKERTemplateDependencyList()
 * @see Template#getOverridesTemplate()
 */
public class TemplateResolver {
  private static final Logger log = Logger.getLogger(TemplateResolver.class);

  private static TemplateResolver instance = new TemplateResolver();

  public static TemplateResolver getInstance() {
    return instance;
  }

  public static void setInstance(TemplateResolver instance) {
    TemplateResolver.instance = instance;
  }

  /**
   * See the description in the class header.
   * 
   * @param template
   *          the template to resolve.
   * @return the list of all templates: dependencies and overriding templates
   */
  public List<Template> resolve(Template template) {
    final List<Template> result = new ArrayList<Template>();
    final Template realTemplate = getOverridingTemplate(template);
    addDependsOnTemplates(realTemplate, result);
    result.add(realTemplate);
    return result;
  }

  private void addDependsOnTemplates(Template template, List<Template> result) {
    if (result.contains(template)) {
      return;
    }
    for (TemplateDependency dependency : template.getOBCLKERTemplateDependencyList()) {
      final Template realDependency = getOverridingTemplate(dependency.getDependsOnTemplate());
      if (result.contains(realDependency)) {
        continue;
      }
      addDependsOnTemplates(realDependency, result);
      result.add(realDependency);
    }
  }

  private Template getOverridingTemplate(Template template) {
    final List<Template> derivePath = new ArrayList<Template>();
    Template currentTemplate = template;
    int cnt = 0;
    while (true) {
      final OBCriteria<Template> templateCriteria = OBDal.getInstance().createCriteria(
          Template.class);
      templateCriteria.add(Restrictions.eq(Template.PROPERTY_OVERRIDESTEMPLATE, currentTemplate));
      final List<Template> overridingTemplates = templateCriteria.list();
      if (overridingTemplates.size() == 0) {
        return currentTemplate;
      }
      if (overridingTemplates.size() > 1) {
        log.error("Template " + currentTemplate + " has more than one ("
            + overridingTemplates.size() + ") overriding template ");
      }
      derivePath.add(currentTemplate);
      final Template newTemplate = overridingTemplates.get(0);
      if (derivePath.contains(newTemplate)) {
        log.error("There is a cycle in the overriding of templates, one template is "
            + currentTemplate);
        return currentTemplate;
      }
      currentTemplate = newTemplate;
      cnt++;
      if (cnt > 10000) {
        throw new IllegalStateException("Infinite cycle, reading overriding templates " + template);
      }
    }
  }
}
