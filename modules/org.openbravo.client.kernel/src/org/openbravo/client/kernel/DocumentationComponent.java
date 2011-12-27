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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;

/**
 * @author iperdomo
 * 
 */
public class DocumentationComponent extends BaseTemplateComponent {

  private static final String SOURCE_PATH_PROPERTY = "source.path";
  private static final String JS_DIRECTORY_PATH = "@javapackage@" + File.separator + "web"
      + File.separator + "@javapackage@" + File.separator + "js";

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, KernelConstants.DOCUMENTATION_TEMPLATE_ID);
  }

  public List<String> getDocumentationResources() {

    final Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    String modulesPath = obProps.getProperty(SOURCE_PATH_PROPERTY);

    if (modulesPath == null) {
      return Collections.emptyList();
    }

    File modulesFolder = new File(modulesPath + File.separator + "modules");

    if (!modulesFolder.exists()) {
      return Collections.emptyList();
    }

    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    final List<String> result = new ArrayList<String>();

    final FilenameFilter jsFilter = new FilenameFilter() {
      public boolean accept(File dir, String fileName) {
        return fileName.endsWith(".js");
      }
    };

    for (Module mod : modules) {
      if (mod.getId().equals("0")) { // Skip core
        continue;
      }

      final File jsModFolder = new File(modulesFolder, JS_DIRECTORY_PATH.replace("@javapackage@",
          mod.getJavaPackage()));

      if (!jsModFolder.exists()) {
        continue;
      }

      File[] files = jsModFolder.listFiles(jsFilter);
      for (int i = 0; i < files.length; i++) {
        result.add("#" + getContextUrl() + "web/" + mod.getJavaPackage() + "/js/"
            + files[i].getName());
      }
    }
    return result;
  }

}
