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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.ad.ui.MessageTrl;

/**
 * Collects all labels of the modules which have registered a component provider and generates the
 * client side javascript holding the labels.
 * 
 * @author mtaal
 */
public class I18NComponent extends BaseTemplateComponent {

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, KernelConstants.I18N_TEMPLATE_ID);
  }

  /**
   * Read the labels of all modules except core, first read the default labels from the AD_Message
   * table and then reads the language specific labels.
   * 
   * @return a collection of labels.
   */
  public Collection<Label> getLabels() {
    final Map<String, Label> labels = new HashMap<String, Label>();
    OBContext.setAdminMode();
    try {
      final OBQuery<Module> moduleQuery = OBDal.getInstance()
          .createQuery(Module.class, "id != '0'");
      final List<String> modules = new ArrayList<String>();
      for (Module module : moduleQuery.list()) {
        modules.add(module.getId());
      }

      if (modules.isEmpty()) {
        return Collections.emptyList();
      }

      // first read the labels from the base table
      final OBQuery<Message> messages = OBDal.getInstance().createQuery(Message.class,
          "module.id in (:modules)");
      messages.setNamedParameter("modules", modules);
      for (Message message : messages.list()) {
        final Label label = new Label();
        label.setKey(message.getSearchKey());
        label.setValue(message.getMessageText());
        labels.put(message.getSearchKey(), label);
      }
      final OBQuery<MessageTrl> messagesTrl = OBDal.getInstance().createQuery(MessageTrl.class,
          "message.module.id in (:modules) and language.id=:languageId");
      messagesTrl.setNamedParameter("modules", modules);
      messagesTrl.setNamedParameter("languageId", OBContext.getOBContext().getLanguage().getId());
      for (MessageTrl message : messagesTrl.list()) {
        final Label label = new Label();
        label.setKey(message.getMessage().getSearchKey());
        label.setValue(message.getMessageText());
        labels.put(message.getMessage().getSearchKey(), label);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return labels.values();
  }

  @Inject
  @Any
  private Instance<ComponentProvider> componentProviders;

  public String getETag() {

    if (getModule().isInDevelopment()) {
      return OBContext.getOBContext().getLanguage().getId() + "_" + getLastModified().getTime();
    } else {
      final StringBuilder version = new StringBuilder();
      final List<String> modules = new ArrayList<String>();
      // TODO: it is possible that the modules need to be deterministically sorted
      // to ensure that the version string won't change
      for (ComponentProvider componentProvider : componentProviders) {
        if (!modules.contains(componentProvider.getModule().getId())) {
          version.append(componentProvider.getModule().getVersion());
        }
      }
      return OBContext.getOBContext().getLanguage().getId() + "_" + version;
    }
  }

  public static class Label {
    private String key;
    private String value;

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
