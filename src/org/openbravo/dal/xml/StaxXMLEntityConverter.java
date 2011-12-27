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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Converts a XML string to an objectgraph with objects using a Stax approach. It can handle very
 * large XML Documents (> 500mb). It can not handle OneToMany properties, for this the
 * {@link XMLEntityConverter} should be used. The StaxXMLEntityConverter is mainly used for client
 * import and export which has larger datasets. During the XML parse phase this converter will match
 * XML tags with new or existing (in the database) business objects. The matching logic is
 * implemented in the {@link EntityResolver}.
 * <p/>
 * The XMLEntityConverter keeps track of which objects are new, which exist but do not need to be
 * updated or which objects exist but need to be updated.
 * <p/>
 * This converter does not update the database directly. However, it changes the properties of
 * existing objects. This means that a commit after calling the process method on the converter can
 * result in database updates by Hibernate.
 * 
 * @see Entity
 * 
 * @author mtaal
 */

public class StaxXMLEntityConverter extends BaseXMLEntityConverter implements OBNotSingleton {
  // This class should translate the

  private static final Logger log = Logger.getLogger(EntityXMLConverter.class);

  public static StaxXMLEntityConverter newInstance() {
    return OBProvider.getInstance().get(StaxXMLEntityConverter.class);
  }

  /**
   * The main entry point. This method creates a XMLStreamReader and then calls
   * process(XMLStreamReader).
   * 
   * @param reader
   *          the xml
   * @return the list of BaseOBObject present in the root of the xml. This list contains the
   *         to-be-updated, to-be-inserted as well as the unchanged business objects
   */
  public List<BaseOBObject> process(Reader reader) {
    try {
      final XMLInputFactory factory = XMLInputFactory.newInstance();
      final XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);

      int event = xmlReader.getEventType();
      if (event != XMLStreamConstants.START_DOCUMENT) {
        error("XML document is invalid, can not be passed");
        return new ArrayList<BaseOBObject>();
      }
      return process(xmlReader);
    } catch (final EntityXMLException xe) {
      throw xe;
    } catch (final Exception e) {
      throw new EntityXMLException(e);
    }
  }

  /**
   * The main entry point. This method walks through the elements in the root and parses them. The
   * children of a business object (in the xml) are also parsed. Referenced objects are resolved
   * through the {@link EntityResolver}.
   * <p/>
   * After a call to this method the to-be-inserted objects can be retrieved through the
   * {@link #getToInsert()} method and the to-be-updated objects through the {@link #getToUpdate()}
   * method.
   * 
   * @param xmlReader
   *          the xml to parse
   * @return the list of BaseOBObject present in the root of the xml. This list contains the
   *         to-be-updated, to-be-inserted as well as the unchanged business objects
   */
  public List<BaseOBObject> process(XMLStreamReader xmlReader) {
    clear();
    getEntityResolver().setClient(getClient());
    getEntityResolver().setOrganization(getOrganization());

    try {
      // check that the rootelement is the openbravo one
      xmlReader.nextTag();
      final LocalElement rootElement = getElement(xmlReader);
      if (!rootElement.getName().equals(XMLConstants.OB_ROOT_ELEMENT)) {
        throw new OBException("Root tag of the xml document should be: "
            + XMLConstants.OB_ROOT_ELEMENT + ", but it is " + rootElement.getName());
      }

      // walk through the elements
      final Set<BaseOBObject> checkDuplicates = new HashSet<BaseOBObject>();
      final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
      while (true) {
        xmlReader.nextTag();

        // we are the end
        if (isAtEndElement(xmlReader, XMLConstants.OB_ROOT_ELEMENT)) {
          break;
        }

        final LocalElement element = getElement(xmlReader);

        final BaseOBObject bob = processEntityElement(element, xmlReader, false);

        if (hasErrorOccured()) {
          return result;
        }

        // only add it if okay
        if (bob != null && !checkDuplicates.contains(bob)) {
          result.add(bob);
          checkDuplicates.add(bob);
        }
      }
      repairReferences();
      checkDanglingObjects();
      return result;
    } catch (XMLStreamException e) {
      throw new EntityXMLException(e);
    }
  }

  // processes a xml tag which denotes an instance of a business object
  private BaseOBObject processEntityElement(LocalElement obElement, XMLStreamReader xmlReader,
      boolean theReferenced) {
    // note: referenced is true for both childs and many-to-one references
    // it is passed to the entityresolver to allow searches in other
    // organization
    final String entityName = obElement.getName();

    // note id maybe null for new objects
    final String id = obElement.getAttributes().get(XMLConstants.ID_ATTRIBUTE);
    if (entityName == null) {
      error("Element " + obElement.getName() + " has no entityname attribute, not processing it");
      return null;
    }
    try {
      log.debug("Converting entity " + entityName);
      final boolean hasReferenceAttribute = obElement.getAttributes().get(
          XMLConstants.REFERENCE_ATTRIBUTE) != null;

      // resolve the entity, using the id, note that
      // resolve will create a new object if none is found
      BaseOBObject bob = resolve(entityName, id, false);

      // should never be null at this point
      Check.isNotNull(bob, "The business object " + entityName + " (" + id
          + ") can not be resolved");

      // warn/error is logged below if the entity is updated
      // update is prevented below
      final boolean writable = OBContext.getOBContext().isInAdministratorMode()
          || SecurityChecker.getInstance().isWritable(bob);

      // do some checks to determine if this one should be updated
      // a referenced instance should not be updated if it is not new
      // note that embedded children are updated but non-embedded children
      // are not updated!
      final boolean preventRealUpdate = !writable
          || (hasReferenceAttribute && !bob.isNewOBObject());

      final Entity entity = ModelProvider.getInstance().getEntity(obElement.getName());
      boolean updated = false;

      // now parse the property elements
      while (true) {
        xmlReader.nextTag();

        if (isAtEndElement(xmlReader, entityName)) {
          break;
        }

        final LocalElement childElement = getElement(xmlReader);
        final Property p = entity.getProperty(childElement.getName());
        log.debug(">>> Importing property " + p.getName());

        // TODO: make this option controlled
        final boolean isNotImportableProperty = p.isTransient(bob)
            || (p.isAuditInfo() && !isOptionImportAuditInfo()) || p.isInactive();
        if (isNotImportableProperty) {
          log.debug("Property " + p + " is inactive, transient or auditinfo, ignoring it");
          skipElement(xmlReader);
          continue;
        }

        // ignore the id properties as they are already set, or should
        // not be set
        if (p.isId()) {
          skipElement(xmlReader);
          continue;
        }

        if (p.isOneToMany()) {
          throw new EntityXMLException("This XML converter can not handle one-to-many properties");
        }

        final Object currentValue = bob.get(p.getName());

        // do the primitive values
        if (p.isPrimitive()) {

          // NOTE: I noticed a difference between running from Eclipse and from the commandline
          // after some searching, there is a jar file wstx-asl-3.0.2.jar used in
          // src-db/database/lib
          // which provides the woodstox XMLStreamReader. From the commandline the xerces
          // XMLStreamReader
          // is used. They differ in the handling of the getText method in handling entities. For
          // example the following text: <text>Tom&amp;Jerry</text> now assume that the pointer is
          // at the content of the text tag, and you do getText, then woodstox will return Tom&Jerry
          // while xerces will return Tom, this because the &amp; is an entity which is a separate
          // event.
          // below we use the getElementText which works correctly in both cases apparently

          // element value, note that getElementText means that it is not required anymore
          // to go to element end, that is done by the xmlReader
          final String elementText = xmlReader.getElementText();
          Object newValue = ((PrimitiveDomainType) p.getDomainType()).createFromString(elementText);

          // correct the value
          newValue = replaceValue(bob, p, newValue);

          log.debug("Primitive property with value " + newValue);

          // only update if changed
          if ((currentValue != null && newValue == null)
              || (currentValue == null && newValue != null)
              || (currentValue != null && newValue != null && !currentValue.equals(newValue))) {
            log.debug("Value changed setting it");
            if (!preventRealUpdate) {
              bob.set(p.getName(), newValue);
              updated = true;
            }
          }
        } else {
          Check.isTrue(!p.isOneToMany(), "One to many property not allowed here");
          // never update the org or client through xml!
          final boolean clientUpdate = bob instanceof ClientEnabled
              && p.getName().equals(Organization.PROPERTY_CLIENT);
          final boolean orgUpdate = bob instanceof OrganizationEnabled
              && p.getName().equals(Client.PROPERTY_ORGANIZATION);
          if (!isOptionClientImport() && currentValue != null && (clientUpdate || orgUpdate)) {
            skipElement(xmlReader);
            continue;
          }

          // determine the referenced entity
          Object newValue;

          // handle null value
          if (childElement.getAttributes().get(XMLConstants.ID_ATTRIBUTE) == null) {
            newValue = null;
          } else {
            // get the info and resolve the reference
            final String refId = childElement.getAttributes().get(XMLConstants.ID_ATTRIBUTE);
            final String refEntityName = p.getTargetEntity().getName();
            newValue = resolve(refEntityName, refId, true);
          }
          newValue = replaceValue(bob, p, newValue);

          final boolean hasChanged = (currentValue == null && newValue != null)
              || (currentValue != null && newValue != null && !currentValue.equals(newValue));
          if (hasChanged) {
            log.debug("Setting value " + newValue);
            if (!preventRealUpdate) {
              bob.set(p.getName(), newValue);
              updated = true;
            }
          }
          checkEndElement(xmlReader, childElement.getName());
        }
      }

      // do the unique constraint matching here
      // this check can not be done earlier because
      // earlier no properties are set for a new object
      bob = replaceByUniqueObject(bob);

      // add to the correct list on the basis of different characteristics
      addToInsertOrUpdateLists(id, bob, writable, updated, hasReferenceAttribute, preventRealUpdate);

      // do a check that in case of a client/organization import that the
      // client and organization are indeed set
      if (isOptionClientImport()) {
        checkClientOrganizationSet(bob);
      }
      return bob;
    } catch (final Exception e) {
      error("Exception when parsing entity " + entityName + " (" + id + "):" + e.getMessage());
      return null;
    }
  }

  private void skipElement(XMLStreamReader xmlReader) throws XMLStreamException {
    int skipEndElements = 1;
    while (skipEndElements > 0) {
      xmlReader.next();
      if (xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
        skipEndElements--;
      }
      if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
        skipEndElements++;
      }
    }
  }

  private LocalElement getElement(XMLStreamReader xmlReader) throws XMLStreamException {
    if (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT) {
      throw new EntityXMLException("Not a START_ELEMENT event but a " + xmlReader.getEventType()
          + " with name " + xmlReader.getLocalName());
    }
    final LocalElement element = new LocalElement();
    element.setName(xmlReader.getLocalName());
    final Map<String, String> attrs = new HashMap<String, String>();
    for (int i = 0, n = xmlReader.getAttributeCount(); i < n; ++i) {
      attrs.put(xmlReader.getAttributeLocalName(i), xmlReader.getAttributeValue(i));
    }
    element.setAttributes(attrs);
    return element;
  }

  private void checkEndElement(XMLStreamReader xmlReader, String name) throws XMLStreamException {
    xmlReader.nextTag();
    if (xmlReader.getEventType() != XMLStreamConstants.END_ELEMENT) {
      throw new EntityXMLException("Expected an END_ELEMENT event but it was a "
          + xmlReader.getEventType() + " " + xmlReader.getName());
    }
    if (!xmlReader.getLocalName().equalsIgnoreCase(name)) {
      throw new EntityXMLException("Expected an END_ELEMENT for tag: " + name
          + " but it was an END_ELEMENT for tag: " + xmlReader.getLocalName());
    }
  }

  private boolean isAtEndElement(XMLStreamReader xmlReader, String name) {
    return xmlReader.isEndElement() && xmlReader.getLocalName().compareTo(name) == 0;
  }

  // convenience class
  private class LocalElement {
    private String name;
    private Map<String, String> attributes;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Map<String, String> getAttributes() {
      return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
    }
  }
}