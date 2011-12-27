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

import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;

/**
 * This class is used to generate FieldProviders from the Build objects. These Field Providers can
 * then be used in the ApplyModules servlet to generate the Build Structure tree in the Window
 */
public class BuildStepWrapper {

  private String node;
  private String name;
  private String level;

  public BuildStepWrapper(BuildMainStep step) {
    name = step.getName();
    level = "0";
    node = generateNode(step.getCode());
  }

  public BuildStepWrapper(BuildStep step) {
    name = step.getName();
    level = "1";
    node = generateNode(step.getCode());
  }

  public BuildStepWrapper(BuildMainStepTranslation step) {
    name = step.getTranslatedName();
    level = "0";
    node = generateNode(step.getCode());
  }

  public BuildStepWrapper(BuildStepTranslation step) {
    name = step.getTranslatedName();
    level = "1";
    node = generateNode(step.getCode());
  }

  public FieldProvider getFieldProvider() {
    return new FieldProviderFactory(this);
  }

  private String generateNode(String code) {
    String numCode = code.replace("RB", "");
    String postfix = "." + numCode.substring(1);
    if (postfix.equals(".0"))
      postfix = "";
    return numCode.substring(0, 1) + postfix;
  }

  public String getTitleLabel() {
    return name;
  }

  public String getPaddingLevel() {
    return level;
  }

  public String getNode() {
    return node;
  }

  public String getPadding() {
    return node;
  }

  public String getIcon() {
    return node;
  }

  public String getTitle() {
    return node;
  }

  public String getProcessing() {
    return node;
  }

  public String getError() {
    return node;
  }

  public String getException() {
    return node;
  }

  public String getWarning() {
    return node;
  }

}
