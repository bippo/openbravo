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

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the translation information for a Build Main Step (in particular, its
 * translated name, and error/warning/success messages)
 * 
 */
public class BuildMainStepTranslation {

  private String code;
  private String originalName;
  private String translatedName;
  private String originalSuccessMessage;
  private String originalWarningMessage;
  private String originalErrorMessage;
  private String translatedSuccessMessage;
  private String translatedWarningMessage;
  private String translatedErrorMessage;
  private List<BuildStepTranslation> stepTranslations;

  public BuildMainStepTranslation() {
    stepTranslations = new ArrayList<BuildStepTranslation>();
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public String getTranslatedName() {
    return translatedName;
  }

  public void setTranslatedName(String translatedName) {
    this.translatedName = translatedName;
  }

  public String getOriginalSuccessMessage() {
    return originalSuccessMessage;
  }

  public void setOriginalSuccessMessage(String originalSuccessMessage) {
    this.originalSuccessMessage = originalSuccessMessage;
  }

  public String getOriginalWarningMessage() {
    return originalWarningMessage;
  }

  public void setOriginalWarningMessage(String originalWarningMessage) {
    this.originalWarningMessage = originalWarningMessage;
  }

  public String getOriginalErrorMessage() {
    return originalErrorMessage;
  }

  public void setOriginalErrorMessage(String originalErrorMessage) {
    this.originalErrorMessage = originalErrorMessage;
  }

  public String getTranslatedSuccessMessage() {
    return translatedSuccessMessage;
  }

  public void setTranslatedSuccessMessage(String translatedSuccessMessage) {
    this.translatedSuccessMessage = translatedSuccessMessage;
  }

  public String getTranslatedWarningMessage() {
    return translatedWarningMessage;
  }

  public void setTranslatedWarningMessage(String translatedWarningMessage) {
    this.translatedWarningMessage = translatedWarningMessage;
  }

  public String getTranslatedErrorMessage() {
    return translatedErrorMessage;
  }

  public void setTranslatedErrorMessage(String translatedErrorMessage) {
    this.translatedErrorMessage = translatedErrorMessage;
  }

  public List<BuildStepTranslation> getStepTranslations() {
    return stepTranslations;
  }

  public void setStepTranslations(List<BuildStepTranslation> stepTranslations) {
    this.stepTranslations = stepTranslations;
  }

  public void addStepTranslation(BuildStepTranslation stepTranslation) {
    stepTranslations.add(stepTranslation);
  }

  public BuildStepTranslation getBuildStepTranslationForCode(String code) {
    for (BuildStepTranslation stepTranslation : stepTranslations) {
      if (stepTranslation.getCode().equals(code)) {
        return stepTranslation;
      }
    }
    return null;
  }

}
