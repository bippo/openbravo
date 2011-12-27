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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.betwixt.io.BeanReader;
import org.openbravo.data.FieldProvider;
import org.xml.sax.InputSource;

/**
 * This class contains the full translation information of a Build (specifically, it contains its
 * corresponding BuildMainStep and BuildStep translations)
 * 
 */
public class BuildTranslation {
  private String language;
  private List<BuildMainStepTranslation> mainStepTranslations;
  private Build build;

  public BuildTranslation() {
    mainStepTranslations = new ArrayList<BuildMainStepTranslation>();
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public List<BuildMainStepTranslation> getMainStepTranslations() {
    return mainStepTranslations;
  }

  public void setMainStepTranslations(List<BuildMainStepTranslation> mainStepTranslations) {
    this.mainStepTranslations = mainStepTranslations;
  }

  public void addMainStepTranslation(BuildMainStepTranslation mStepT) {
    mainStepTranslations.add(mStepT);
  }

  public String getTranslatedName(String code) {
    for (BuildMainStepTranslation mainStep : mainStepTranslations) {
      if (mainStep.getCode().equalsIgnoreCase(code)) {
        return mainStep.getTranslatedName();
      }
      for (BuildStepTranslation step : mainStep.getStepTranslations()) {
        if (step.getCode().equalsIgnoreCase(code)) {
          return step.getTranslatedName();
        }
      }
    }
    if (build != null) {
      for (BuildMainStep mainStep : build.getMainSteps()) {
        if (mainStep.getCode().equalsIgnoreCase(code)) {
          return mainStep.getName();
        }
        for (BuildStep step : mainStep.getStepList()) {
          if (step.getCode().equalsIgnoreCase(code)) {
            return step.getName();
          }
        }
      }
    }
    return "";
  }

  public BuildMainStepTranslation getBuildMainStepTranslationForCode(String code) {
    for (BuildMainStepTranslation tr : mainStepTranslations) {
      if (tr.getCode().equals(code))
        return tr;
    }
    return null;
  }

  public FieldProvider[] getFieldProvidersForBuild() {
    if (build == null)
      return null;
    ArrayList<FieldProvider> fieldProviderList = new ArrayList<FieldProvider>();
    for (BuildMainStep mainStep : build.getMainSteps()) {
      BuildMainStepTranslation mainStepTranslation = getBuildMainStepTranslationForCode(mainStep
          .getCode());
      if (mainStepTranslation == null) {
        fieldProviderList.add(new BuildStepWrapper(mainStep).getFieldProvider());
        for (BuildStep step : mainStep.getStepList()) {
          fieldProviderList.add(new BuildStepWrapper(step).getFieldProvider());
        }
      } else {
        fieldProviderList.add(new BuildStepWrapper(mainStepTranslation).getFieldProvider());
        for (BuildStep step : mainStep.getStepList()) {
          BuildStepTranslation stepTranslation = mainStepTranslation
              .getBuildStepTranslationForCode(step.getCode());
          if (stepTranslation == null) {
            fieldProviderList.add(new BuildStepWrapper(step).getFieldProvider());
          } else {
            fieldProviderList.add(new BuildStepWrapper(stepTranslation).getFieldProvider());
          }
        }
      }
    }

    FieldProvider[] fps = new FieldProvider[fieldProviderList.size()];
    int i = 0;
    for (FieldProvider fp : fieldProviderList)
      fps[i++] = fp;
    return fps;
  }

  public static BuildTranslation getTranslationFileFromXML(String translationFilePath,
      String mappingFilePath) throws Exception {

    FileReader xmlReader = new FileReader(translationFilePath);

    BeanReader beanReader = new BeanReader();

    beanReader.getBindingConfiguration().setMapIDs(false);

    beanReader.getXMLIntrospector().register(new InputSource(new FileReader(mappingFilePath)));

    beanReader.registerBeanClass("BuildTranslation", BuildTranslation.class);

    BuildTranslation build = (BuildTranslation) beanReader.parse(xmlReader);

    return build;
  }

  public void setBuild(Build build) {
    this.build = build;
  }
}
