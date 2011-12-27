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
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The configuration information of a XmlTemplate
 **/
class TemplateConfiguration extends DefaultHandler {
  Stack<Object> stcElement; // Stack of Elements
  String strElement;
  DataTemplate activeDataTemplate = null;
  Vector<Object> hasDataTemplate;
  XmlEngine xmlEngine; // need for add subreports to the xmlEngine
  Hashtable<String, ParameterTemplate> hasParameter;
  Hashtable<String, LabelTemplate> hasLabels;
  XmlTemplate xmlTemplate;
  HashtableMultiple hashtable;
  String id;
  String strAttribute;
  String strReplace;
  String strBooleanAttribute = null;
  XmlComponentTemplate xmlComponentBooleanWithId = null;
  XmlComponentTemplate xmlComponentArg1;
  XmlComponentTemplate xmlComponentArg2;
  String strFunction;
  FormatCouple formatCouple;
  Vector<ReplaceElement> vecReplace;
  String strTemplate;
  String strDriverDefault;
  String strUrlDefault;
  XmlTemplate subreport;
  String sectionSubreport;
  static Logger log4jTemplateConfiguration = Logger.getLogger(TemplateConfiguration.class);
  StringBuffer buffer;

  public TemplateConfiguration(Vector<Object> hasDataTemplate, XmlEngine xmlEngine,
      Hashtable<String, ParameterTemplate> hasParameter,
      Hashtable<String, LabelTemplate> hasLabels, XmlTemplate xmlTemplate) { // XmlEngineNP:
    // unico
    // argumento
    // en
    // el
    // constructor:
    // this,
    // y
    // eliminar
    // las
    // tres
    // variables
    // actuales,
    // guardar
    // solo
    // XmlTemplate
    this.hasDataTemplate = hasDataTemplate;
    this.xmlEngine = xmlEngine;
    this.hasParameter = hasParameter;
    this.hasLabels = hasLabels;
    this.xmlTemplate = xmlTemplate;
    stcElement = new Stack<Object>();
    hashtable = new HashtableMultiple(); // XmlEngineNP: pass
    // XmlTemplatepasarlo to
    // XmlTemplate, in order to keep it
    // after the reading, due
    // to it will be necessary to create the XmlDocument-s, look for the
    // element with its Id
  }

  private void pushElement(String name) {
    stcElement.push(name);
    strElement = name;
  }

  private void popElement() {
    strElement = (String) stcElement.pop();
    if (!stcElement.isEmpty())
      strElement = (String) stcElement.peek();
  }

  public void processingInstruction(String name, String remainder) {
    if (log4jTemplateConfiguration.isDebugEnabled())
      log4jTemplateConfiguration.debug("TemplateConfiguration: processingInstruction " + name);
  }

  public void startElement(java.lang.String uri, java.lang.String name, java.lang.String qName,
      Attributes amap) { // throws SAXException {
    if (log4jTemplateConfiguration.isDebugEnabled())
      log4jTemplateConfiguration.debug("TemplateConfiguration: startElement is called: " + name);
    readBuffer();

    if (name.trim().equalsIgnoreCase("FIELD")) {

      pushElement(name);
      id = null;
      strAttribute = null;
      strReplace = null;
      strBooleanAttribute = null;
      String strBooleanWithId = null;
      formatCouple = new FormatCouple();
      vecReplace = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        } else if (amap.getQName(i).equals("attribute")) {
          strAttribute = amap.getValue(i);
        } else if (amap.getQName(i).equals("replace")) {
          strReplace = amap.getValue(i);
        } else if (amap.getQName(i).equals("boolean")) {
          strBooleanAttribute = amap.getValue(i);
        } else if (amap.getQName(i).equals("withId")) {
          strBooleanWithId = amap.getValue(i);
          xmlComponentBooleanWithId = (XmlComponentTemplate) hashtable.get(strBooleanWithId);
        } else if (amap.getQName(i).equals("format")) {
          formatCouple = xmlEngine.formatHashtable.get(amap.getValue(i));
          if (formatCouple == null) {
            log4jTemplateConfiguration.warn("  format " + amap.getValue(i) + " not found");
          }
        } else if (amap.getQName(i).equalsIgnoreCase("replaceCharacters")) {
          vecReplace = xmlEngine.replaceHashtable.get(amap.getValue(i));
          if (vecReplace == null) {
            log4jTemplateConfiguration.warn("  replaceCharacters " + amap.getValue(i)
                + " not found");
          }
        }
      }
    } else if (name.trim().equalsIgnoreCase("LABEL")) {
      if (log4jTemplateConfiguration.isDebugEnabled())
        log4jTemplateConfiguration.debug("LABEL name found.");
      LabelTemplate label = new LabelTemplate();
      pushElement(name);
      id = null;
      strReplace = null;
      vecReplace = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  LABEL: attribute name=" + amap.getQName(i)
              + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        } else if (amap.getQName(i).equals("name")) {
          label.strName = amap.getValue(i);
        } else if (amap.getQName(i).equals("replace")) {
          strReplace = amap.getValue(i);
        }
      }
      hasLabels.put(label.strName, label);
      if (id != null) {
        log4jTemplateConfiguration.debug("putting label template in hashtable: " + id);
        hashtable.put(id, label);
      }
      log4jTemplateConfiguration.debug("hashtable size checking if Id added to hashtable: "
          + hashtable.vecKeys.size());
      log4jTemplateConfiguration.debug("hasLabels size: " + hasLabels.size());

    } else if (name.trim().equalsIgnoreCase("FUNCTION")) {
      pushElement(name);
      id = null;
      strAttribute = null;
      strReplace = null;
      strBooleanAttribute = null;
      formatCouple = new FormatCouple();
      String strBooleanWithId = null;
      xmlComponentArg1 = null;
      xmlComponentArg2 = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  FUNCTION: attribute name=" + amap.getQName(i)
              + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        } else if (amap.getQName(i).equals("attribute")) {
          strAttribute = amap.getValue(i);
        } else if (amap.getQName(i).equals("replace")) {
          strReplace = amap.getValue(i);
        } else if (amap.getQName(i).equals("boolean")) {
          strBooleanAttribute = amap.getValue(i);
        } else if (amap.getQName(i).equals("withId")) {
          strBooleanWithId = amap.getValue(i);
          xmlComponentBooleanWithId = (XmlComponentTemplate) hashtable.get(strBooleanWithId);
        } else if (amap.getQName(i).equals("name")) {
          // strFunctionOfElement = new String(strElement);
          strFunction = amap.getValue(i);
        } else if (amap.getQName(i).equals("format")) {
          formatCouple = xmlEngine.formatHashtable.get(amap.getValue(i));
          if (formatCouple == null) {
            log4jTemplateConfiguration.warn("  format " + amap.getValue(i) + " not found");
          }
        } else if (amap.getQName(i).equals("arg1")) {
          String strArg1 = amap.getValue(i);
          xmlComponentArg1 = (XmlComponentTemplate) hashtable.get(strArg1);
        } else if (amap.getQName(i).equals("arg2")) {
          String strArg2 = amap.getValue(i);
          xmlComponentArg2 = (XmlComponentTemplate) hashtable.get(strArg2);
        }
      }
    } else if (name.trim().equalsIgnoreCase("SUBREPORT")) {
      pushElement(name);
      String strSubreportName = null;
      String strSubreportFile = null;
      id = null;
      sectionSubreport = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  SUBREPORT: attribute name=" + amap.getQName(i)
              + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        } else if (amap.getQName(i).equals("name")) {
          strSubreportName = amap.getValue(i);
        } else if (amap.getQName(i).equals("report")) {
          strSubreportFile = amap.getValue(i);
        } else if (amap.getQName(i).equals("section")) {
          sectionSubreport = amap.getValue(i);
        }
      }
      subreport = xmlEngine.readAllXmlTemplates(xmlTemplate.strName + "&" + strSubreportName,
          strSubreportFile, new String[0]);
      xmlTemplate.hasSubXmlTemplates.put(strSubreportName, subreport);
      if (id != null) {
        hashtable.put(id, subreport);
      }
    } else if (name.trim().equalsIgnoreCase("ARGUMENT")) {
      String withId = null;
      String strArgumentName = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  ARGUMENT: attribute name=" + amap.getQName(i)
              + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("withId")) {
          withId = amap.getValue(i);
        } else if (amap.getQName(i).equals("name")) {
          strArgumentName = amap.getValue(i);
        }
      }
      XmlComponentTemplate xmlComponent = (XmlComponentTemplate) hashtable.get(withId);
      if (log4jTemplateConfiguration.isDebugEnabled())
        log4jTemplateConfiguration.debug("XmlComponent of id: " + withId + " xmlComponent: "
            + xmlComponent);
      // Search in the parameterSQL of the structuers of the subreport
      for (Enumeration<Object> e1 = subreport.hasDataTemplate.elements(); e1.hasMoreElements();) {
        DataTemplate elementDataTemplate = (DataTemplate) e1.nextElement();
        for (Enumeration<Object> e2 = elementDataTemplate.vecParameterTemplate.elements(); e2
            .hasMoreElements();) {
          ParameterTemplate parameterTemplate = (ParameterTemplate) e2.nextElement();
          if (parameterTemplate.strName.equals(strArgumentName)) {
            parameterTemplate.xmlComponentTemplate = xmlComponent; // XmlEngineNP:
            // the
            // value
            // should
            // be
            // saved
            // in
            // parameter
            parameterTemplate.section = sectionSubreport;
            if (log4jTemplateConfiguration.isDebugEnabled())
              log4jTemplateConfiguration.debug("Argument: asigned to parametro_SQL: "
                  + parameterTemplate.strName);
          }
        }
      }
      // Search in the parameters of the subreport
      ParameterTemplate parameterTemplate = subreport.hasParameterTemplate.get(strArgumentName);
      if (parameterTemplate != null) {
        parameterTemplate.xmlComponentTemplate = xmlComponent;
        parameterTemplate.section = sectionSubreport;
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("Argument: asigned to parameter: "
              + parameterTemplate.strName);
      }
    } else if (name.trim().equalsIgnoreCase("SECTION")) {
      id = null;
      String strField = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        } else if (amap.getQName(i).equals("field")) {
          strField = amap.getValue(i);
        }
      }
      SectionTemplate section = new SectionTemplate(id, activeDataTemplate);
      if (strField != null) {
        section.breakFieldTemplate = activeDataTemplate.addField(strField);
      }
      if (id != null) {
        hashtable.put(id, section);
      }
    } else if (name.trim().equalsIgnoreCase("DISCARD")) {
      id = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        }
      }
      Discard discard = new Discard();
      if (id != null) {
        hashtable.put(id, discard);
      }
    } else if (name.trim().equalsIgnoreCase("CONNECTION")) {
      pushElement(name);
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  TemplateConfiguration (CONNECTION): attribute name="
              + amap.getQName(i) + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("driver")) {
          if (log4jTemplateConfiguration.isDebugEnabled())
            log4jTemplateConfiguration.debug("    adding driver");
          activeDataTemplate.strDriver = amap.getValue(i);
          if (log4jTemplateConfiguration.isDebugEnabled())
            log4jTemplateConfiguration.debug("    driver added");
        } else if (amap.getQName(i).equals("URL")) {
          activeDataTemplate.strURL = amap.getValue(i);
          // } else if (amap.getQName(i).equals("SQL")) {
          // activeDataTemplate.strSQL = amap.getValue(i);
        }
      }
    } else if (name.trim().equalsIgnoreCase("STRUCTURE")) {
      pushElement(name);
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  TemplateConfiguration (STRUCTURE): attribute name="
              + amap.getQName(i) + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("name")) {
          activeDataTemplate = new DataTemplate();
          // activeDataTemplate.clear(); // XmlEngineNP: hay que
          // pasarlo a XmlDocument, si es necesario
          activeDataTemplate.strDriver = strDriverDefault;
          activeDataTemplate.strURL = strUrlDefault;
          activeDataTemplate.strName = amap.getValue(i);
          // hasDataVector hasDataTemplate.put(amap.getValue(i),
          // activeDataTemplate);
          hasDataTemplate.addElement(activeDataTemplate);
        }
      }
    } else if (name.trim().equalsIgnoreCase("SQL")) {
      pushElement(name);
    } else if (name.trim().equalsIgnoreCase("TEMPLATE")) {
      pushElement(name);
      for (int i = 0; i < amap.getLength(); i++) {
        if (log4jTemplateConfiguration.isDebugEnabled())
          log4jTemplateConfiguration.debug("  TemplateConfiguration (TEMPLATE): attribute name="
              + amap.getQName(i) + " value=" + amap.getValue(i));
        if (amap.getQName(i).equals("file")) {
          strTemplate = amap.getValue(i);
        }
      }
    } else if (name.equals("PARAMETER_SQL") || name.equals("PARAMETER")) {
      ParameterTemplate parameter = new ParameterTemplate();
      pushElement(name);
      id = null;
      strAttribute = null;
      strReplace = null;
      strBooleanAttribute = null;
      String strBooleanWithId = null;
      for (int i = 0; i < amap.getLength(); i++) {
        if (amap.getQName(i).equals("name")) {
          parameter.strName = amap.getValue(i);
        } else if (amap.getQName(i).equals("id")) {
          id = amap.getValue(i);
        } else if (amap.getQName(i).equals("attribute")) {
          strAttribute = amap.getValue(i);
        } else if (amap.getQName(i).equals("replace")) {
          strReplace = amap.getValue(i);
        } else if (amap.getQName(i).equals("boolean")) {
          strBooleanAttribute = amap.getValue(i);
        } else if (amap.getQName(i).equals("withId")) {
          strBooleanWithId = amap.getValue(i);
          xmlComponentBooleanWithId = (XmlComponentTemplate) hashtable.get(strBooleanWithId);
        } else if (amap.getQName(i).equals("default")) {
          parameter.strDefault = amap.getValue(i);
        } else if (amap.getQName(i).equals("type")) {
          if (amap.getValue(i).equals("integer")) {
            parameter.type = java.sql.Types.INTEGER;
          } else if (amap.getValue(i).equals("string")) {
            parameter.type = java.sql.Types.VARCHAR;
          }
        } else if (amap.getQName(i).equals("format")) {
          FormatCouple parameterFormat = xmlEngine.formatHashtable.get(amap.getValue(i));
          if (parameterFormat == null) {
            log4jTemplateConfiguration.warn("  format " + amap.getValue(i) + " not found");
          }
          parameter.formatOutput = parameterFormat.formatOutput;
          parameter.formatSimple = parameterFormat.formatSimple;
        } else if (amap.getQName(i).equals("replaceCharacters")) {
          parameter.vecReplace = xmlEngine.replaceHashtable.get(amap.getValue(i));
          if (parameter.vecReplace == null) {
            log4jTemplateConfiguration.warn("  replaceCharacters " + amap.getValue(i)
                + " not found");
          }
        }
      }
      if (name.equals("PARAMETER_SQL")) {
        activeDataTemplate.vecParameterTemplate.addElement(parameter);
      } else {
        hasParameter.put(parameter.strName, parameter);
      }
      if (id != null) {
        // attribute of parameter
        if (strAttribute != null || strBooleanAttribute != null) {
          log4jTemplateConfiguration.debug("Inside strAttribute in Parameter");
          AttributeComponentTemplate attributeComponentTemplate = new AttributeComponentTemplate(
              parameter, strAttribute, strReplace, strBooleanAttribute, xmlComponentBooleanWithId);
          hashtable.put(id, attributeComponentTemplate);
        } else {
          log4jTemplateConfiguration.debug("Inside id");
          hashtable.put(id, parameter);
        }
      }
    }
  }

  public void endElement(java.lang.String uri, java.lang.String name, java.lang.String qName) {
    if (log4jTemplateConfiguration.isDebugEnabled())
      log4jTemplateConfiguration.debug("TemplateConfiguration: endElement is called: " + name);
    readBuffer();
    if (name.equals("SUBREPORT")) {
      popElement();
    }
  }

  public void characters(char[] ch, int start, int length) { // throws
    // SAXException {
    if (log4jTemplateConfiguration.isDebugEnabled()) {
      log4jTemplateConfiguration.debug("TemplateConfiguration: characters is called: "
          + new String(ch, start, length));
      log4jTemplateConfiguration
          .debug("TemplateConfiguration: characters is called: " + strElement);
    }

    if (buffer == null)
      buffer = new StringBuffer();
    buffer.append(ch, start, length);
  }

  public void readBuffer() {
    if (strElement == null)
      return;
    if (buffer != null) {
      String strBuffer = buffer.toString();
      log4jTemplateConfiguration.debug("Configuration(" + strElement + "): characters is called: "
          + strBuffer);
      if (strElement.trim().equalsIgnoreCase("FIELD")) {
        log4jTemplateConfiguration.debug("characters: (" + strElement + ") antes de addField");
        FieldTemplate fieldTemplate = activeDataTemplate.addField(strBuffer,
            formatCouple.formatOutput, formatCouple.formatSimple, vecReplace);
        log4jTemplateConfiguration.debug("characters: (" + strElement + ") despues de addField");
        if (id != null) {
          if (strAttribute != null || strBooleanAttribute != null) {
            log4jTemplateConfiguration.debug("Inside strAttribute");
            AttributeComponentTemplate attributeComponentTemplate = new AttributeComponentTemplate(
                fieldTemplate, strAttribute, strReplace, strBooleanAttribute,
                xmlComponentBooleanWithId);
            hashtable.put(id, attributeComponentTemplate);
          } else {
            log4jTemplateConfiguration.debug("Inside id");
            hashtable.put(id, fieldTemplate);
          }
        }
        popElement();
      } else if (strElement.trim().equalsIgnoreCase("FUNCTION")) {
        log4jTemplateConfiguration.debug("characters: (" + strElement + ") before addFunction");
        FunctionTemplate functionTemplate = activeDataTemplate.addFunction(strFunction, strBuffer,
            formatCouple.formatOutput, formatCouple.formatSimple, xmlComponentArg1,
            xmlComponentArg2, id);
        log4jTemplateConfiguration.debug("characters: (" + strElement + ") after de addFunction");
        if (id != null) {
          if (strAttribute != null || strBooleanAttribute != null) {
            log4jTemplateConfiguration.debug("Inside strAttribute");
            AttributeComponentTemplate attributeComponentTemplate = new AttributeComponentTemplate(
                functionTemplate, strAttribute, strReplace, strBooleanAttribute,
                xmlComponentBooleanWithId);
            hashtable.put(id, attributeComponentTemplate);
          } else {
            log4jTemplateConfiguration.debug("Inside id");
            hashtable.put(id, functionTemplate);
          }
        }
        popElement();
      } else if (strElement.trim().equalsIgnoreCase("SQL")) {
        activeDataTemplate.strSQL = strBuffer;
        popElement();
      }
      buffer = null;
    }
  }

}
