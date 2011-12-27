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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

class TranslationUtils {

  private static final Logger log4j = Logger.getLogger(TranslationUtils.class);

  public static final int TAB = 0, FORM = 1, PROCESS = 2;

  public static HashMap<String, String> processFormLabels(ConnectionProvider con, String filename,
      String lang) {

    return retrieveLabelData(con, filename, lang);
  }

  public static InterfaceInfo getModuleLang(ConnectionProvider con, InterfaceInfo info) {
    String lang = "";
    try {
      if (info.getId() != null && !info.getId().equals("")) {
        InterfaceModuleInfoData[] moduleInfo;
        if (info.getInterfaceType() == InterfaceInfo.TAB) {
          moduleInfo = InterfaceModuleInfoData.selectTabModuleLang(con, info.getId());
          for (int i = 0; i < moduleInfo.length; i++) {
            InterfaceModuleInfoData module = moduleInfo[i];
            if (module != null && module.modulelanguage != null) {
              lang = module.modulelanguage;
              defineInterfaceInfo(info, module);
            }
          }
        } else if (info.getInterfaceType() == InterfaceInfo.PROCESS) {
          moduleInfo = InterfaceModuleInfoData.selectProcessModuleLang(con, info.getId());
          for (int i = 0; i < moduleInfo.length; i++) {
            InterfaceModuleInfoData module = moduleInfo[i];
            if (module != null && module.modulelanguage != null) {
              lang = module.modulelanguage;
              defineInterfaceInfo(info, module);
            }
          }
        }
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return info;
  }

  private static void defineInterfaceInfo(InterfaceInfo info, InterfaceModuleInfoData module) {
    info.setModuleLanguage(module.modulelanguage);
    info.setModuleId(module.moduleid);
    info.setTitle(module.name);
    info.setDescription(module.description);
    info.setHelp(module.help);
  }

  public static WindowLabel[] processWindowLabels(ConnectionProvider con, String tabId,
      String lang, String moduleLang) {
    FieldLabelsData[] fieldLabels;
    FieldGroupLabelsData[] fieldGroupLabels;
    try {
      List<WindowLabel> windowLabelsCol = new ArrayList<WindowLabel>();

      fieldLabels = FieldLabelsData.select(con, tabId, lang);
      fieldGroupLabels = FieldGroupLabelsData.selectFieldGroupTrl(con, tabId, lang);
      populateFieldLabels(windowLabelsCol, fieldLabels);
      if (fieldGroupLabels.length > 0) {
        populateFieldGroupLabels(windowLabelsCol, fieldGroupLabels);
      }

      WindowLabel[] windowLabels = new WindowLabel[windowLabelsCol.size()];
      return windowLabelsCol.toArray(windowLabels);
    } catch (ServletException e) {
      log4j.error("Error in processWindowLabels", e);
    }
    return new WindowLabel[0];
  }

  public static WindowLabel[] processProcessLabels(ConnectionProvider con, String lang,
      InterfaceInfo uiInfo) {
    ProcessLabelsData[] processLabels;
    try {
      if (lang.equals(uiInfo.getModuleLanguage()) || lang.equals("")
          || uiInfo.getModuleLanguage().equals("")) {
        processLabels = ProcessLabelsData.selectOriginalParameters(con, uiInfo.getId());
        return populateProcessLabels(processLabels);
      } else {
        processLabels = ProcessLabelsData.selectTranslatedParameters(con, uiInfo.getId(), lang);
        return populateProcessLabels(processLabels);
      }
    } catch (ServletException e) {
      log4j.error("Error in processProcessLabels", e);
    }
    return new WindowLabel[0];
  }

  public static InterfaceInfo getInterfaceHeaderTrlInfo(ConnectionProvider con, InterfaceInfo info,
      String lang) {
    InterfaceInfo uiInfoResult = info;
    if (info.getInterfaceType() == InterfaceInfo.TAB) {
      InterfaceTrlInfoData[] data;
      try {
        data = InterfaceTrlInfoData.selectProcessTrlInfo(con, uiInfoResult.getId(), lang);
        if (data != null && data.length > 0) {
          uiInfoResult.setTitle(data[0].name);
          uiInfoResult.setDescription(data[0].description);
          uiInfoResult.setHelp(data[0].help);
        }
      } catch (ServletException e) {
        e.printStackTrace();
      }
    } else if (info.getInterfaceType() == InterfaceInfo.PROCESS) {
      InterfaceTrlInfoData[] data;
      try {
        data = InterfaceTrlInfoData.selectProcessTrlInfo(con, uiInfoResult.getId(), lang);
        if (data != null && data.length > 0) {
          uiInfoResult.setTitle(data[0].name);
          uiInfoResult.setDescription(data[0].description);
          uiInfoResult.setHelp(data[0].help);
        }
      } catch (ServletException e) {
        e.printStackTrace();
      }
    }
    return uiInfoResult;
  }

  private static void populateFieldLabels(List<WindowLabel> windowLabelsCol,
      FieldLabelsData[] fieldLabels) {
    for (int labelsCount = 0; labelsCount < fieldLabels.length; labelsCount++) {
      FieldLabelsData labelData = fieldLabels[labelsCount];
      WindowLabel label = new WindowLabel(labelData.adColumnId, labelData.fieldName,
          labelData.fieldtrlName);
      windowLabelsCol.add(label);
    }
  }

  private static void populateFieldGroupLabels(List<WindowLabel> windowLabelsCol,
      FieldGroupLabelsData[] fieldGroupLabels) {
    HashMap<String, WindowLabel> uniqueFieldGroups = new HashMap<String, WindowLabel>();
    for (int labelsCount = 0; labelsCount < fieldGroupLabels.length; labelsCount++) {
      FieldGroupLabelsData labelData = fieldGroupLabels[labelsCount];
      WindowLabel label = new WindowLabel(WindowLabel.FIELD_GROUP_LABEL, labelData.fieldgroupid,
          labelData.fieldgroupname, labelData.fieldgrouptrlname);
      uniqueFieldGroups.put(label.getOriginalLabel(), label);
    }
    windowLabelsCol.addAll(uniqueFieldGroups.values());
  }

  private static WindowLabel[] populateProcessLabels(ProcessLabelsData[] fieldLabels) {
    WindowLabel[] res = new WindowLabel[fieldLabels.length];
    for (int labelsCount = 0; labelsCount < fieldLabels.length; labelsCount++) {
      ProcessLabelsData labelData = fieldLabels[labelsCount];
      WindowLabel label = new WindowLabel(labelData.processparacolumnname,
          labelData.processparaname, labelData.processparatrlname);
      res[labelsCount] = label;
    }
    return res;
  }

  private static HashMap<String, String> retrieveLabelData(ConnectionProvider conn,
      String fileName, String language) {
    HashMap<String, String> textmap = new HashMap<String, String>();
    try {
      TextInterfacesData[] textData = TextInterfacesData.selectText(conn, fileName, language);
      for (int i = 0; i < textData.length; i++) {
        // trim values, in some occasions there is a character 160 representing blank spaces
        textmap.put(textData[i].text.replace((char) 160, ' ').trim(), textData[i].trltext);
      }
      return textmap;
    } catch (ServletException e) {
      log4j.error("Error in retrieveLabelData", e);
    }
    return textmap;
  }

}
