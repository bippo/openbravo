/*
 ************************************************************************************
 * Copyright (C) 2001-2010 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.xmlEngine;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.uiTranslation.TranslationHandler;

class XmlVectorValue extends Vector<Object> {

  private static final long serialVersionUID = 1L;
  static Logger log4jXmlVectorValue = Logger.getLogger(XmlVectorValue.class);
  private XmlDocument xmlDocument = new XmlDocument();
  private HashMap<String, String> textMap;
  public TranslationHandler handler;

  public XmlVectorValue(XmlVectorTemplate xmlVectorTemplate, XmlDocument xmlDocument) {
    if (xmlDocument != null)
      this.xmlDocument = xmlDocument;
    for (Enumeration<Object> e = xmlVectorTemplate.elements(); e.hasMoreElements();) {
      XmlComponentTemplate xmlComponentTemplate = (XmlComponentTemplate) e.nextElement();
      log4jXmlVectorValue.debug("Adding XmlComponentTemplate");
      addElement(xmlComponentTemplate.createXmlComponentValue(xmlDocument));
    }

  }

  StringBuffer printStringBuffer() {
    StringBuffer str = new StringBuffer();
    HashMap<String, String> tabLabels = null;
    if (handler != null && handler.getTabId() != null && !handler.getTabId().equals("")) {
      tabLabels = handler.getWindowLabels();
    }
    for (Enumeration<Object> e = elements(); e.hasMoreElements();) {
      XmlComponentValue xmlComponentValue = (XmlComponentValue) e.nextElement();
      String result = "";
      if (textMap != null) {

        if (xmlComponentValue.print() != null && !xmlComponentValue.print().startsWith("<")
            && !xmlComponentValue.print().equals("")) {

          boolean isTranslated = false;
          result = xmlComponentValue.print().trim();
          log4jXmlVectorValue.debug("printStringBuffer(HashMap<String, String> textMap) - result: "
              + result);
          log4jXmlVectorValue.debug("checking for existence of text in textdata: " + result);
          String translation = textMap.get(result);
          if (translation != null && !translation.equals("")) {
            log4jXmlVectorValue.debug("printStringBuffer() appending xmlComponentValue: "
                + xmlComponentValue.print() + ", translation: " + translation);
            result = translation;
          }
        }
      }
      if ((result == null || result.equals("")) && xmlComponentValue.print() != null) {
        result = xmlComponentValue.print();
      }
      if (result != null && !result.equals("")) {
        if (result.contains("alt=") || result.contains("title=")) {
          log4jXmlVectorValue.debug("printStringBuffer(HashMap<String, String> textMap) - result: "
              + result);
          String prefix = "alt=\"";
          String suffix = "\"";
          if (result.contains("alt=\"")) {
            log4jXmlVectorValue.debug("alt found: " + result.substring(result.indexOf(prefix)));
            if (tabLabels != null && !tabLabels.isEmpty()) {
              int index = 0;
              while (index < result.length() && result.indexOf(prefix, index) > 0) {
                String temp = result.substring(index);
                int start = temp.indexOf(prefix);
                String text = temp.substring(start);
                text = text.replace(prefix, "");
                int end = text.indexOf(suffix);
                text = text.substring(0, end);
                String trl = replaceString(text, tabLabels);
                if (trl != null && !trl.equals("")) {
                  result = result.replace(prefix + text + suffix, prefix + trl + suffix);
                }
                index = start + index + 1;
              }
            } else if (textMap != null) {
              result = replaceString(result, prefix, suffix, textMap);
            }
          }
          prefix = "title=\"";
          if (result.contains("title=\"")) {
            log4jXmlVectorValue.debug("title found: " + result.substring(result.indexOf(prefix)));
            if (tabLabels != null && !tabLabels.isEmpty()) {
              int index = 0;
              while (index < result.length() && result.indexOf(prefix, index) > 0) {
                String temp = result.substring(index);
                int start = temp.indexOf(prefix);
                String text = temp.substring(start);
                text = text.replace(prefix, "");
                int end = text.indexOf(suffix);
                text = text.substring(0, end);
                String trl = replaceString(text, tabLabels);
                if (trl != null && !trl.equals("")) {
                  result = result.replace(prefix + text + suffix, prefix + trl + suffix);
                }
                index = start + index + 1;
              }
            } else if (textMap != null) {
              result = replaceString(result, prefix, suffix, textMap);
            }
          }
        }
      }
      if (!result.equals("")) {
        str.append(result);
      } else {
        str.append(xmlComponentValue.print());
      }
    }
    return str;
  }

  StringBuffer printStringBuffer(HashMap<String, String> textMap) {
    StringBuffer buffer = new StringBuffer();
    for (Enumeration<Object> e = elements(); e.hasMoreElements();) {
      XmlComponentValue xmlComponentValue = (XmlComponentValue) e.nextElement();
      String result = "";
      if (xmlComponentValue.print() != null && !xmlComponentValue.print().startsWith("<")
          && !xmlComponentValue.print().equals("")) {
        result = xmlComponentValue.print();
        log4jXmlVectorValue.debug("printStringBuffer(HashMap<String, String> textMap) - result: "
            + result);
        if (textMap != null) {
          log4jXmlVectorValue.debug("checking for existence of text in textdata: " + result);
          String translation = textMap.get(result);
          if (translation != null && !translation.equals("")) {
            log4jXmlVectorValue.debug("printStringBuffer() appending xmlComponentValue: "
                + xmlComponentValue.print() + ", translation: " + translation);
            result = translation;
          }
        }
      }
      if ((result == null || result.equals("")) && xmlComponentValue.print() != null) {
        result = xmlComponentValue.print();
      }
      if (result != null && !result.equals("")) {
        if (result.contains("alt=") || result.contains("title=")) {
          log4jXmlVectorValue.debug("printStringBuffer(HashMap<String, String> textMap) - result: "
              + result);
          if (textMap != null) {
            String prefix = "alt=\"";
            String suffix = "\"";
            if (result.contains("alt=\"")) {
              log4jXmlVectorValue.debug("alt found: " + result.substring(result.indexOf(prefix)));
              result = replaceString(result, prefix, suffix, textMap);
            }
            prefix = "title=\"";
            if (result.contains("title=\"")) {
              log4jXmlVectorValue.debug("title found: " + result.substring(result.indexOf(prefix)));
              result = replaceString(result, prefix, suffix, textMap);
            }
          }
        }
      }
      if (!result.equals("")) {
        buffer.append(result);
      } else {
        buffer.append(xmlComponentValue.print());
      }
      // log4jXmlVectorValue.debug("Añadido XmlComponentValue, longitud actual:"
      // + str.length());
    }
    // str.append("\n");
    return buffer;
  }

  private String replaceString(String original, HashMap<String, String> textMap) {
    String result = "";
    result = textMap.get(original);

    return result;
  }

  private String replaceString(String originalLine, String prefix, String suffix,
      HashMap<String, String> textMap) {
    String prefixStr = prefix;
    String suffixStr = suffix;
    String inputLine = originalLine;
    int start = inputLine.indexOf(prefixStr);
    String text = inputLine.substring(start);
    text = text.replace(prefixStr, "");
    int end = text.indexOf(suffixStr);
    text = text.substring(0, end);
    String trlText = textMap.get(text);
    if (trlText != null && !trlText.equals("")) {
      log4jXmlVectorValue.debug("Found a match - ORIGINAL: " + prefixStr + text + suffixStr
          + ", TRANSLATION: " + prefixStr + trlText + suffixStr);
      inputLine = inputLine.replace(prefixStr + text + suffixStr, prefixStr + trlText + suffixStr);
    }

    return inputLine;
  }

  String print() {
    return printStringBuffer().toString();
  }

  StringBuffer printPreviousStringBuffer() {
    StringBuffer str = new StringBuffer();
    for (Enumeration<Object> e = elements(); e.hasMoreElements();) {
      XmlComponentValue xmlComponentValue = (XmlComponentValue) e.nextElement();
      str.append(xmlComponentValue.printPrevious());
      // log4jXmlVectorValue.debug("Añadido XmlComponentValue, longitud actual:"
      // + str.length());
    }
    // str.append("\n");
    return str;
  }

  String printPrevious() {
    return printStringBuffer().toString();
  }

  public void setTextMap(HashMap<String, String> textMap) {
    this.textMap = textMap;
  }

  Map<String, String> getTextMap() {
    return textMap;
  }
}
