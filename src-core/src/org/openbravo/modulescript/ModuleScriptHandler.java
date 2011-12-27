/*
 ************************************************************************************
 * Copyright (C) 2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.modulescript;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.openbravo.buildvalidation.BuildValidationHandler;

public class ModuleScriptHandler extends Task {
  private static final Logger log4j = Logger.getLogger(ModuleScriptHandler.class);

  private File basedir;
  private String moduleJavaPackage;

  @Override
  public void execute() {
    List<String> classes = new ArrayList<String>();
    if (moduleJavaPackage != null) {
      // We will only be executing the ModuleScripts of a specific module
      File moduleDir = new File(basedir, "modules/" + moduleJavaPackage + "/build/classes");
      BuildValidationHandler.readClassFiles(classes, moduleDir);
    } else {
      File coreBuildFolder = new File(basedir, "src-util/modulescript/build/classes");
      BuildValidationHandler.readClassFiles(classes, coreBuildFolder);
      File moduleFolder = new File(basedir, "modules");
      File modFoldersA[] = moduleFolder.listFiles();
      ArrayList<File> modFolders = new ArrayList<File>();
      for (File f : modFoldersA) {
        modFolders.add(f);
      }
      Collections.sort(modFolders);
      for (File modFolder : modFolders) {
        if (modFolder.isDirectory()) {
          File validationFolder = new File(modFolder, "build/classes");
          if (validationFolder.exists()) {
            BuildValidationHandler.readClassFiles(classes, validationFolder);
          }
        }
      }
    }
    for (String s : classes) {
      try {
        Class<?> myClass = Class.forName(s);
        if (myClass.getGenericSuperclass().equals(
            Class.forName("org.openbravo.modulescript.ModuleScript"))) {
          Object instance = myClass.newInstance();
          log4j.info("Executing moduleScript: " + s);
          callExecute(myClass, instance);
        }
      } catch (Exception e) {
        log4j.error("Error executing moduleScript: " + s);
        throw new BuildException("Execution of moduleScript " + s + "failed.");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private ArrayList<String> callExecute(Class<?> myClass, Object instance)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    return (ArrayList<String>) myClass.getMethod("execute", new Class[0]).invoke(instance,
        new Object[0]);
  }

  public File getBasedir() {
    return basedir;
  }

  public void setBasedir(File basedir) {
    this.basedir = basedir;
  }

  public String getModuleJavaPackage() {
    return moduleJavaPackage;
  }

  public void setModuleJavaPackage(String moduleJavaPackage) {
    this.moduleJavaPackage = moduleJavaPackage;
  }

}
