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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.EnumerateDomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.ListTrl;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.utility.TreeNode;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts one or more business objects to a XML presentation which can easily be read into Excel.
 * 
 * @author mtaal
 */
public class EntityExcelXMLConverter implements OBNotSingleton {
  private static final Logger log = Logger.getLogger(EntityExcelXMLConverter.class);

  public static EntityExcelXMLConverter newInstance() {
    return OBProvider.getInstance().get(EntityExcelXMLConverter.class);
  }

  private final Map<String, String> listRefTranslations = new HashMap<String, String>();

  private TransformerHandler xmlHandler;
  private Writer output;

  private DateFormat dateFormat;
  private DateFormat dateTimeFormat;

  // initialize the sax handlers
  private void initialize() throws Exception {
    listRefTranslations.clear();

    String dateFormatStr = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    dateFormatStr = dateFormatStr.replace("MM", "M");
    dateFormatStr = dateFormatStr.replace("dd", "d");

    String dateTimeFormatStr = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateTimeFormat.java");
    dateTimeFormatStr = dateTimeFormatStr.replace("MM", "M");
    dateTimeFormatStr = dateTimeFormatStr.replace("dd", "d");
    dateTimeFormatStr = dateTimeFormatStr.replace("HH", "H");
    dateTimeFormatStr = dateTimeFormatStr.substring(0, dateTimeFormatStr.indexOf(":ss"));

    dateFormat = new SimpleDateFormat(dateFormatStr);
    dateTimeFormat = new SimpleDateFormat(dateTimeFormatStr);
    final StreamResult streamResult = new StreamResult(output);
    final SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

    xmlHandler = tf.newTransformerHandler();

    // do some form of pretty printing...
    final Transformer serializer = xmlHandler.getTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    serializer.setOutputProperty(OutputKeys.VERSION, "1.0");
    serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    xmlHandler.setResult(streamResult);
  }

  /**
   * Export the collection of business objects as xml suited for excel.
   */
  public void export(Collection<BaseOBObject> toProcess) {
    try {
      // always export using a unix line delimiter:
      System.setProperty("line.separator", "\n");

      initialize();
      xmlHandler.startDocument();

      final AttributesImpl rootAttrs = new AttributesImpl();
      rootAttrs.addAttribute("", "", "xmlns:xsi", "CDATA", XMLConstants.XSI_NAMESPACE);

      xmlHandler.startElement(XMLConstants.OPENBRAVO_NAMESPACE, XMLConstants.OB_ROOT_ELEMENT, "ob:"
          + XMLConstants.OB_ROOT_ELEMENT, rootAttrs);

      for (BaseOBObject bob : toProcess) {
        export(bob);
        getOutput().flush();
      }

      xmlHandler.endElement("http://www.openbravo.com", XMLConstants.OB_ROOT_ELEMENT, "ob:"
          + XMLConstants.OB_ROOT_ELEMENT);
      xmlHandler.endDocument();
    } catch (Exception e) {
      throw new EntityXMLException(e);
    }
  }

  protected void export(BaseOBObject obObject) throws SAXException {
    final String entityName = DalUtil.getEntityName(obObject);

    final AttributesImpl entityAttrs = new AttributesImpl();

    xmlHandler.startElement("", "", entityName, entityAttrs);

    // export each property
    for (final Property p : obObject.getEntity().getProperties()) {

      if (p.isOneToMany()) {
        continue;
      }

      // set the tag
      final AttributesImpl propertyAttrs = new AttributesImpl();

      // get the value
      final Object value = obObject.get(p.getName());

      // will result in an empty tag if null
      if (value == null) {
        propertyAttrs.addAttribute("", "", "xsi:nil", "CDATA", "true");
        xmlHandler.startElement("", "", p.getName(), propertyAttrs);
        xmlHandler.endElement("", "", p.getName());
        continue;
      }

      if (p.isCompositeId()) {
        log.warn("Entity " + obObject.getEntity()
            + " has compositeid, this is not yet supported in the webservice");
        xmlHandler.startElement("", "", p.getName(), propertyAttrs);
        xmlHandler.endElement("", "", p.getName());
        continue;
      }

      xmlHandler.startElement("", "", p.getName(), propertyAttrs);
      String txt;
      // make a difference between a primitive and a reference
      if (p.isPrimitive()) {
        // handle a special case the tree node
        // both the parent and the node should be added to the export list
        if (value != null && obObject instanceof TreeNode) {
          if (PrimitiveReferenceHandler.getInstance().isPrimitiveReference(p) && value != null
              && !value.equals("0")) {
            final String strValue = (String) value;
            final Entity referedEntity = PrimitiveReferenceHandler.getInstance()
                .getPrimitiveReferencedEntity(obObject, p);
            final BaseOBObject obValue = OBDal.getInstance().get(referedEntity.getName(), strValue);
            if (obValue == null) {
              txt = null;
            } else {
              txt = ((BaseOBObject) obValue).getIdentifier();
            }
          } else {
            txt = null;
          }
        } else if (p.isDate()) {
          txt = dateFormat.format(value);
        } else if (p.isDatetime()) {
          txt = dateTimeFormat.format(value);
        } else if (p.getDomainType() instanceof EnumerateDomainType) {
          txt = getTranslation(p.getDomainType().getReference().getId(), (String) value);
        } else {
          txt = ((PrimitiveDomainType) p.getDomainType()).convertToString(value);
        }
      } else {
        txt = ((BaseOBObject) value).getIdentifier();
      }
      if (txt != null) {
        xmlHandler.characters(txt.toCharArray(), 0, txt.length());
      }
      xmlHandler.endElement("", "", p.getName());
    }
    xmlHandler.endElement("", "", entityName);
  }

  public Writer getOutput() {
    return output;
  }

  public void setOutput(Writer output) {
    this.output = output;
  }

  private String getTranslation(String referenceId, String value) {
    if (listRefTranslations.containsKey(referenceId + value)) {
      return listRefTranslations.get(referenceId + value);
    }

    OBContext.setAdminMode();
    try {
      final String languageId = OBContext.getOBContext().getLanguage().getId();

      final Reference reference = OBDal.getInstance().get(Reference.class, referenceId);
      for (org.openbravo.model.ad.domain.List list : reference.getADListList()) {
        String searchKey = list.getSearchKey();
        String name = list.getName();
        for (ListTrl listTrl : list.getADListTrlList()) {
          if (DalUtil.getId(listTrl.getLanguage()).equals(languageId)) {
            name = listTrl.getName();
            break;
          }
        }
        listRefTranslations.put(searchKey, name);
      }
      return listRefTranslations.get(referenceId + value);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}