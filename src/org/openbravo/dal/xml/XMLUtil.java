/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * Utility class for XML processing.
 * 
 * @see XMLEntityConverter
 * @see EntityXMLConverter
 * @see org.openbravo.service.rest.DalWebService
 * 
 * @author mtaal
 */

public class XMLUtil implements OBSingleton {

  private static XMLUtil instance;

  public static synchronized XMLUtil getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(XMLUtil.class);
    }
    return instance;
  }

  public static synchronized void setInstance(XMLUtil instance) {
    XMLUtil.instance = instance;
  }

  /** @return a new Dom4j Document */
  public Document createDomDocument() {
    final Document document = DocumentHelper.createDocument();
    return document;
  }

  /**
   * Creates a standard Openbravo root element for a xml document and set ths namespace. Ads the new
   * root element to the Dom4j document.
   * 
   * @param doc
   *          the Dom4j document to set the root element
   * @param elementName
   *          the name of the root element
   * @return the new root element
   */
  public Element addRootElement(Document doc, String elementName) {
    final Namespace ns = new Namespace("ob", "http://www.openbravo.com");
    final QName qName = new QName(elementName, ns);
    final Element root = doc.addElement(qName);
    root.addNamespace("ob", "http://www.openbravo.com");
    return root;
  }

  /**
   * Converts a Dom4j document to a string. A number of specific settings: 1) output encoding is
   * UTF-8, 2) text nodes are not trimmed
   * 
   * @param document
   *          the Dom4j to convert to a XML string
   * @return the XML representation
   */
  public String toString(Document document) {
    try {
      final OutputFormat format = OutputFormat.createPrettyPrint();
      format.setEncoding("UTF-8");
      format.setTrimText(false);
      final StringWriter out = new StringWriter();
      final XMLWriter writer = new XMLWriter(out, format);
      writer.write(document);
      writer.close();
      return out.toString();
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

}