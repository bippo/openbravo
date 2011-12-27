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
 * This class contains the translation information for a Build Step (in particular, its original and
 * translated name)
 */
public class BuildStepTranslation {
  private String code;
  private String originalName;
  private String translatedName;

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

}
