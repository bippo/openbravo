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

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTemplate extends DefaultHandler implements XmlComponentTemplate, IDComponent {
  String strName;
  TemplateConfiguration configuration;
  Hashtable<Object, Object> hasSubXmlTemplates; // hashtable of
  // SubXmlTemplates
  Vector<Object> hasDataTemplate; // hashtable of DataTemplate (the same that
  // the hashtable DataTemplate in
  // Configuration
  Hashtable<String, LabelTemplate> hasLabelTemplate;
  SectionTemplate activeSection;
  XmlVectorTemplate activeXmlVector; // it may be the vecXmlVector, the
  // vecDetail of a DataTemplate or the
  // head or foot of a SectionTemplate
  XmlVectorTemplate vecXmlVector; // contains XmlComponents before, after and
  // between DataTemplates and the
  // DataTemplate
  Hashtable<String, ParameterTemplate> hasParameterTemplate; // contains the
  // ParameterTemplates
  // that not are
  // for the SQL
  // query
  Stack<Object> stcElement; // Stack of Elements
  String strElement;
  String strPreviousElement;
  String strFunctionOfElement;
  String strFieldOfElement;
  String strFunction;
  XmlEngine xmlEngine;
  StackElement stackElement;
  String fileConfiguration;
  String[] discard;
  String prefix;
  String uri;
  String strCharacters = "";

  static Logger log4jXmlTemplate = Logger.getLogger(XmlTemplate.class);

  public XmlTemplate(String strName, String fileConfiguration, String[] discard, XmlEngine xmlEngine) {
    this.strName = strName;
    this.fileConfiguration = fileConfiguration;
    this.discard = discard;
    this.xmlEngine = xmlEngine;
    hasSubXmlTemplates = new Hashtable<Object, Object>();
    hasDataTemplate = new Vector<Object>();
    hasParameterTemplate = new Hashtable<String, ParameterTemplate>();
    hasLabelTemplate = new Hashtable<String, LabelTemplate>();
    configuration = new TemplateConfiguration(hasDataTemplate, xmlEngine, hasParameterTemplate,
        hasLabelTemplate, this); // XmlEngineNP:
    // unique
    // argument in
    // the
    // cosntructor:
    // this
    for (int i = 0; i < discard.length; i++) {
      configuration.hashtable.put(discard[i], new Discard());
    }
    prefix = null;
  }

  String name() {
    return strName;
  }

  String fileConfiguration() {
    return fileConfiguration;
  }

  public int type() {
    return REPORT;
  }

  // XmlEngineNP: make sure this functions can be addede or deleted from the
  // constructor, because it is necessary to make clear, or also for the
  // subreports
  void clear() {
    /*
     * pass to XmlDocument for (Enumeration e = hasDataTemplate.elements() ; e.hasMoreElements();) {
     * DataTemplate elementDataTemplate = (DataTemplate)e.nextElement();
     * elementDataTemplate.clear(); } // see if this loop necessary or if it is here whre the
     * DataTemplate clear is executed
     */
    activeSection = null;
    vecXmlVector = new XmlVectorTemplate();
    activeXmlVector = vecXmlVector;
    stcElement = new Stack<Object>();
  }

  private void pushElement(StackElement stackElement) {
    stcElement.push(stackElement);
    strElement = stackElement.name();
  }

  private StackElement popElement() {
    if (stcElement.isEmpty())
      return null;
    StackElement stackElement = (StackElement) stcElement.pop();
    if (stcElement.isEmpty()) {
      strElement = null;
    } else {
      strElement = ((StackElement) stcElement.peek()).name();
    }
    return stackElement;
  }

  private StackElement peekElement() {
    StackElement stackElement;
    if (stcElement.isEmpty()) {
      stackElement = null;
    } else {
      stackElement = (StackElement) stcElement.peek();
    }
    return stackElement;
  }

  public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) {
    if (log4jXmlTemplate.isDebugEnabled())
      log4jXmlTemplate.debug("XmlTemplate: startPrefixMapping is called, prefix:" + prefix
          + " uri: " + uri);
    this.prefix = prefix;
  }

  public void startElement(java.lang.String uri, java.lang.String name, java.lang.String qName,
      Attributes amap) { // throws SAXException {
    if (log4jXmlTemplate.isDebugEnabled())
      log4jXmlTemplate.debug("XmlTemplate: startElement is called3:" + name + " strElement: "
          + strElement);

    // characters can be read in multiple chunks, add them to the activeXmlVector at the end of the
    // element
    if (strCharacters.length() > 0) {
      CharacterComponent character = new CharacterComponent(strCharacters);
      activeXmlVector.addElement(character);
      strCharacters = "";
    }
    StackElement previousStackElement = peekElement();
    stackElement = new StackElement(qName);
    pushElement(stackElement);
    if (previousStackElement != null) {
      stackElement.setPrintEnabled(previousStackElement.printEnabled());
    }
    if (log4jXmlTemplate.isDebugEnabled())
      log4jXmlTemplate.debug("Call to CharacterComponent");
    TagTemplate tag = new TagTemplate(qName, amap, xmlEngine.strReplaceWhat,
        xmlEngine.strReplaceWith, prefix, uri);
    prefix = null;

    String id = null;
    String strClass = null;

    for (int i = 0; i < amap.getLength(); i++) {
      if (log4jXmlTemplate.isDebugEnabled())
        log4jXmlTemplate.debug("  XmlTemplate (attribute list): attribute name=" + amap.getQName(i)
            + " value=" + amap.getValue(i));
      if (amap.getQName(i).equals("id")) {
        id = amap.getValue(i);
      } else if (amap.getQName(i).equals("class")) {
        strClass = amap.getValue(i);
      }
    }
    if (id != null || strClass != null) {
      IDComponent iDComponent = null;
      int i = 0;
      boolean tagAdded = false;
      for (String strKey : configuration.hashtable.vecKeys) {
        if (strKey.equals(id) || strKey.equals(strClass)) {
          iDComponent = (IDComponent) configuration.hashtable.vecObjects.elementAt(i);

          if (iDComponent != null) {
            log4jXmlTemplate.debug("id: " + id + " tipo: " + iDComponent.type());
            // first test if print the tag
            switch (iDComponent.type()) {
            case IDComponent.DISCARD:
              log4jXmlTemplate.debug("Case DISCARD");
              stackElement.setPrintEnabled(false);
              break;
            }
            if (stackElement.printEnabled()) {
              if (!tagAdded) {
                activeXmlVector.addElement(tag);
                tagAdded = true;
              }
              // Discard2 }

              switch (iDComponent.type()) {
              case IDComponent.SECTION:
                log4jXmlTemplate.debug("Case SECTION");
                stackElement.setSection(activeSection);
                activeSection = (SectionTemplate) iDComponent;
                activeSection.dataTemplate.vecSectionTemplate.addElement(activeSection);
                if (activeSection.dataTemplate.firstSectionTemplate == null) {
                  activeSection.dataTemplate.firstSectionTemplate = activeSection;
                  vecXmlVector.addElement(activeSection.dataTemplate);
                }
                if (activeSection.breakFieldTemplate == null) {
                  log4jXmlTemplate.debug("Add to detail");
                  activeXmlVector = activeSection.dataTemplate().vecDetailTemplate;
                } else {
                  log4jXmlTemplate.debug("Add to Head");
                  activeXmlVector = activeSection.vecHeadTemplate;
                }
                break;
              case IDComponent.FIELD:
                log4jXmlTemplate.debug("Case FIELD");
                stackElement.setSkipCharacters(); // to remove
                // the
                // characters
                FieldTemplate field = (FieldTemplate) iDComponent;
                activeXmlVector.addElement(field);
                break;
              case IDComponent.PARAMETER:
                log4jXmlTemplate.debug("Case PARAMETER");
                stackElement.setSkipCharacters(); // to remove
                // the
                // characters
                ParameterTemplate parameter = (ParameterTemplate) iDComponent;
                // vecParameter.addElement(parameter);
                activeXmlVector.addElement(parameter);
                break;
              case IDComponent.ATTRIBUTE:
                log4jXmlTemplate.debug("Case ATTRIBUTE");
                // not remove the characters
                AttributeComponentTemplate attributeComponent = (AttributeComponentTemplate) iDComponent;
                FunctionTemplate functionAttributeComponent = attributeComponent.functionTemplate();
                if (functionAttributeComponent != null) {
                  if (activeSection == null) {
                    functionAttributeComponent.dataTemplate.vecFunctionTemplateOutSection
                        .addElement(functionAttributeComponent); // XmlEngineNP
                    // (it
                    // had
                    // problems
                    // compiling,verify
                    // it
                    // is
                    // vecFunctionTemplateOutSection)
                  } else {
                    activeSection.addFunction(functionAttributeComponent);
                  }
                }
                tag.setAttribute(attributeComponent);
                break;
              case IDComponent.FUNCTION:
                log4jXmlTemplate.debug("Case FUNCTION");
                stackElement.setSkipCharacters(); // to remove
                // the
                // characters
                FunctionTemplate function = (FunctionTemplate) iDComponent;
                if (activeSection == null) {
                  function.dataTemplate.vecFunctionTemplateOutSection.addElement(function); // XmlEngineNP
                  // like the
                  // last one
                } else {
                  activeSection.addFunction(function);
                }
                activeXmlVector.addElement(function);
                break;
              case IDComponent.REPORT:
                log4jXmlTemplate.debug("Case REPORT");
                stackElement.setSkipCharacters(); // to remove
                // the
                // characters
                XmlTemplate subDocument = (XmlTemplate) iDComponent;
                activeXmlVector.addElement(subDocument);
                break;
              case IDComponent.LABEL:
                log4jXmlTemplate.debug("Case LABEL");
                stackElement.setSkipCharacters(); // to remove
                // the
                // characters
                LabelTemplate label = (LabelTemplate) iDComponent;
                activeXmlVector.addElement(label);
                break;
              }
            } // Discard2
          }

        } // id = key
        i++;
      } // bucle del hashtable
      if (!tagAdded) {
        if (stackElement.printEnabled()) {
          activeXmlVector.addElement(tag);
        }
      }

      /*
       * else { if(stackElement.printEnabled()) { activeXmlVector.addElement(tag); } }
       */
    } else {
      if (stackElement.printEnabled()) {
        activeXmlVector.addElement(tag);
      }
    }

  }

  public void endElement(java.lang.String uri, java.lang.String name, java.lang.String qName) { // throws
    // SAXException
    // {
    do {
      if (log4jXmlTemplate.isDebugEnabled())
        log4jXmlTemplate.debug("XmlTemplate: endElement is called: " + name + " strElement: "
            + strElement);
      stackElement = popElement();
      if (stackElement == null) {
        log4jXmlTemplate.warn("XmlTemplate: not begin tag for " + name);
        return;
      }
    } while (!qName.trim().equalsIgnoreCase(stackElement.name().trim()));

    // characters can be read in multiple chunks, add them to the activeXmlVector at the end of the
    // element
    activeXmlVector.addElement(new CharacterComponent(strCharacters));
    strCharacters = "";

    if (stackElement.isSection()) {
      activeSection = stackElement.section();
      if (activeSection == null) {
        activeXmlVector = vecXmlVector;
      } else {
        activeXmlVector = activeSection.vecFootTemplate;
      }
      log4jXmlTemplate.debug("Add to Foot");
    }

    log4jXmlTemplate.debug(" strElement (after pop): " + strElement);

    CharacterComponent character;
    if (qName.equals("DIVFO") || qName.endsWith("_TMP")) {
      character = new CharacterComponent("");
    } else {
      character = new CharacterComponent("</" + qName + ">");
    }
    if (stackElement.printEnabled()) {
      activeXmlVector.addElement(character);
    }
    // the stackElement is changed once it is verified if the element could
    // be printed
    if (!stcElement.isEmpty())
      stackElement = (StackElement) stcElement.peek();
  }

  public void characters(char[] ch, int start, int length) { // throws
    // SAXException {
    if (log4jXmlTemplate.isDebugEnabled()) {
      log4jXmlTemplate.debug("XmlTemplate: characters is called: " + new String(ch, start, length)
          + " element:" + strElement + " function of:" + strFunctionOfElement + " previousElement:"
          + strPreviousElement);
    }
    if (strElement == null)
      return;
    if (!stackElement.printEnabled())
      return;
    if (!peekElement().skipCharacters()) {
      String chars = new String(ch, start, length);
      // characters can be read in multiple chunks, concatenate them here
      strCharacters += chars;
    }
  }

  public XmlComponentValue createXmlComponentValue(XmlDocument xmlDocument) {
    if (log4jXmlTemplate.isDebugEnabled())
      log4jXmlTemplate.debug("Creation of XmlDocument: " + strName);
    XmlDocument subXmlDocument = xmlDocument.hasSubXmlDocuments.get(strName);
    if (subXmlDocument == null) {
      if (log4jXmlTemplate.isDebugEnabled())
        log4jXmlTemplate.debug("new subXmlDocument : " + strName
            + " in createXmlComponentValue of " + xmlDocument.xmlTemplate.strName);
      subXmlDocument = new XmlDocument(this, xmlDocument);
    }
    return subXmlDocument;
  }

  public XmlDocument createXmlDocument() {
    return createXmlDocument(null);
  }

  XmlDocument createXmlDocument(XmlDocument parentXmlDocument) {
    XmlDocument subXmlDocument = new XmlDocument(this, parentXmlDocument);
    return subXmlDocument;
  }

}
