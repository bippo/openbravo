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

package org.openbravo.erpCommon.ad_process.buildStructure;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.betwixt.io.BeanReader;
import org.openbravo.data.FieldProvider;
import org.xml.sax.InputSource;

/**
 * This class contains the information related to the build structure (steps, error, warning and
 * success messages, and the final state of the system if the build fails or succeeds)
 * 
 */
public class Build {
  private List<BuildMainStep> mainSteps;

  public Build() {
    mainSteps = new ArrayList<BuildMainStep>();
  }

  public List<BuildMainStep> getMainSteps() {
    return mainSteps;
  }

  public void addMainStep(BuildMainStep bms) {
    mainSteps.add(bms);
  }

  public BuildTranslation generateBuildTranslation(String language) {
    BuildTranslation trl = new BuildTranslation();
    trl.setLanguage(language);
    for (BuildMainStep mStep : mainSteps) {
      trl.addMainStepTranslation(mStep.generateBuildMainStepTranslation());
    }
    return trl;
  }

  public FieldProvider[] getFieldProvidersForBuild() {
    ArrayList<FieldProvider> fieldProviderList = new ArrayList<FieldProvider>();
    for (BuildMainStep mainStep : mainSteps) {
      fieldProviderList.add(new BuildStepWrapper(mainStep).getFieldProvider());
      for (BuildStep step : mainStep.getStepList()) {
        fieldProviderList.add(new BuildStepWrapper(step).getFieldProvider());
      }
    }

    FieldProvider[] fps = new FieldProvider[fieldProviderList.size()];
    int i = 0;
    for (FieldProvider fp : fieldProviderList)
      fps[i++] = fp;
    return fps;
  }

  public static Build getBuildFromXMLFile(String buildFilePath, String mappingFilePath)
      throws Exception {

    FileReader xmlReader = new FileReader(buildFilePath);

    BeanReader beanReader = new BeanReader();

    beanReader.getBindingConfiguration().setMapIDs(false);

    beanReader.getXMLIntrospector().register(
        new InputSource(new FileReader(new File(mappingFilePath))));

    beanReader.registerBeanClass("Build", Build.class);

    Build build = (Build) beanReader.parse(xmlReader);
    return build;
  }

  public BuildMainStep mainStepOfCode(String state) {

    for (BuildMainStep mstep : getMainSteps()) {
      if (mstep.getCode().equals(state))
        return mstep;
      for (BuildStep step : mstep.getStepList()) {
        if (step.getCode().equals(state)) {
          return mstep;
        }
      }
      if (state.equals(mstep.getSuccessCode()))
        return mstep;
      if (state.equals(mstep.getWarningCode()))
        return mstep;
      if (state.equals(mstep.getErrorCode()))
        return mstep;
    }
    return null;
  }
}
