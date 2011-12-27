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

/**
 * This class contains the information related to a Build Step (basically, its code and name)
 * 
 */
public class BuildStep {
  private String code;
  private String name;

  public BuildStep() {

  }

  public BuildStep(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BuildStepTranslation generateBuildStepTranslation() {
    BuildStepTranslation trl = new BuildStepTranslation();
    trl.setCode(code);
    trl.setOriginalName(name);
    trl.setTranslatedName(name);
    return trl;
  }
}
