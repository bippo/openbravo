/*
 ************************************************************************************
 * Copyright (C) 2008-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.uiTranslation;

class WindowLabel {

  public static int FIELD_LABEL = 0;
  public static int FIELD_GROUP_LABEL = 1;
  public static int BUTTON_LABEL = 2;

  public int labelType = FIELD_LABEL;
  private String originalLabel;
  private String translatedLabel;

  public WindowLabel() {
  }

  public WindowLabel(String original, String translated) {
    setOriginalLabel(original);
    setTranslatedLabel(translated);
  }

  public WindowLabel(String labelId, String original, String translated) {
    setOriginalLabel(labelId);
    setTranslatedLabel(original, translated);
  }

  public WindowLabel(int type, String labelId, String original, String translated) {
    setLabelType(type);
    setOriginalLabel(labelId);
    setTranslatedLabel(original, translated);
  }

  public void setLabelType(int type) {
    labelType = type;
  }

  public void setOriginalLabel(String originalString) {
    if (labelType == FIELD_LABEL) {
      originalLabel = "lbl::" + originalString;
    } else if (labelType == FIELD_GROUP_LABEL) {
      originalLabel = "fldgrp::" + originalString;
    }
  }

  public String getOriginalLabel() {
    return originalLabel;
  }

  public void setTranslatedLabel(String translation) {
    translatedLabel = translation;
  }

  public void setTranslatedLabel(String original, String translation) {
    if (translation != null && !translation.equals("")) {
      translatedLabel = translation;
    } else {
      translatedLabel = original;
    }
  }

  public String getTranslatedLabel() {
    return translatedLabel;
  }
}
