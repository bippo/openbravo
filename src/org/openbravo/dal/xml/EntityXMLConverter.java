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
 * All portions are Copyright (C) 2008-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.base.structure.Traceable;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.service.dataset.DataSetService;
import org.openbravo.service.dataset.DataSetService.BaseOBIDHexComparator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts one or more business objects to a XML presentation. There are several options which
 * control the behavior. One option is to include referenced objects (or not). For example Currency
 * references Country, if a Currency instance is exported should then also the Country instance be
 * exported. Another option controls if the children of a business object (e.g. the order lines of
 * an order) are exported within the part as a subnode in the xml result. Or that children are not
 * exported or exported in the root of the xml document.
 * 
 * @author mtaal
 */

public class EntityXMLConverter implements OBNotSingleton {
  private static final Logger log = Logger.getLogger(EntityXMLConverter.class);

  public static EntityXMLConverter newInstance() {
    return OBProvider.getInstance().get(EntityXMLConverter.class);
  }

  // controls if many-toreferences objects are also exported
  private boolean optionIncludeReferenced = false;

  // controls if the children (mostly one-to-many) are also included
  // If the dataset is complete
  // (i.e. it contains all records of a set of records which refer to eachother)
  // then children are also always exported anyway
  private boolean optionIncludeChildren = false;

  // if children are exported then they can be embedded in the parent's
  // property or placed in the root.
  private boolean optionEmbedChildren = true;

  // should transient info also be exported
  private boolean optionExportTransientInfo = true;

  // should audit info also be exported
  private boolean optionExportAuditInfo = true;

  // minimize output size, if set to true then the
  // output will probably be less readable
  private boolean optionMinimizeXMLSize = false;

  // controls if the client and organization property are exported also
  private boolean optionExportClientOrganizationReferences = false;

  // only export references which belong to this client
  private Client client;

  // if the system attributes (version, timestamp, etc.) are added to
  // to the root element, for testcases it makes sense to not have this
  // to compare previous output results with new output results
  private boolean addSystemAttributes = true;

  // keeps track of which objects are to be exported
  // and which ones have been considered already
  private List<BaseOBObject> toProcess = new ArrayList<BaseOBObject>();
  private Set<BaseOBObject> allToProcessObjects = new HashSet<BaseOBObject>();
  // the iterator is used in case of large sets of data
  private ScrollableResults dataScroller = null;

  private TransformerHandler xmlHandler;
  private Writer output;

  // is set if the export is done on the basis of a dataset
  private DataSet dataSet;
  private Map<Entity, DataSetTable> dataSetTablesByEntity;

  /**
   * Clear internal data structures, after this call this converter can be used for a new set of
   * objects which need to be exported to a xml representation.
   */
  public void clear() {
    xmlHandler = null;
    output = null;
    toProcess.clear();
    allToProcessObjects.clear();
  }

  // initialize the sax handlers
  private void initializeSax() throws Exception {
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
   * Converts one business object to xml and returns the resulting xml string.
   * 
   * @param obObject
   *          the object to convert to xml
   * @return the xml representation of obObject
   */
  public String toXML(BaseOBObject obObject) {
    final List<BaseOBObject> bobs = new ArrayList<BaseOBObject>();
    bobs.add(obObject);
    return toXML(bobs);
  }

  /**
   * Converts a collection of business objects to xml.
   * 
   * @param bobs
   *          the collection to convert
   * @return the resulting xml string
   */
  public String toXML(Collection<BaseOBObject> bobs) {
    try {
      final StringWriter sw = new StringWriter();
      clear();
      setOutput(sw);
      process(bobs);
      return sw.toString();
    } catch (Exception e) {
      throw new EntityXMLException(e);
    }
  }

  /**
   * Processes one business object and outputs it to the writer ({@link #setOutput(Writer)}).
   * 
   * @param bob
   *          the business object to convert to xml (dom4j)
   */
  public void process(BaseOBObject bob) {
    // set the export list
    getToProcess().add(bob);
    getAllToProcessObjects().add(bob);

    // and do it
    export();
  }

  /**
   * Processes a collection of business objects and outputs them to the writer (
   * {@link #setOutput(Writer)}).
   * 
   * @param bobs
   *          the business objects to convert to xml (dom4j)
   */
  public void process(Collection<BaseOBObject> bobs) {
    // set the export list
    getToProcess().addAll(bobs);
    getAllToProcessObjects().addAll(bobs);

    // and do it
    export();
  }

  protected void export() {
    try {
      // always export using a unix line delimiter:
      System.setProperty("line.separator", "\n");

      initializeSax();
      xmlHandler.startDocument();

      final AttributesImpl rootAttrs = new AttributesImpl();
      rootAttrs.addAttribute("", "", "xmlns:xsi", "CDATA", XMLConstants.XSI_NAMESPACE);

      xmlHandler.startElement(XMLConstants.OPENBRAVO_NAMESPACE, XMLConstants.OB_ROOT_ELEMENT, "ob:"
          + XMLConstants.OB_ROOT_ELEMENT, rootAttrs);

      boolean exportBecauseReferenced = false;
      // do the data scrollers
      if (dataScroller != null) {
        int cnt = 0;
        while (dataScroller.next()) {
          export((BaseOBObject) dataScroller.get(0), false);
          getOutput().flush();
          if ((cnt++ % 100) == 0) {
            // before clearing the session make sure that all added
            // objects are also processed
            // the extra while loop is needed because the export can
            // recursively add new objects
            exportAllToProcessObjects(true);

            OBDal.getInstance().getSession().clear();
          }
        }
        // the remaining objects are always exported because they have been referenced
        exportBecauseReferenced = true;

        // get rid of the datascroller
        dataScroller.close();
        dataScroller = null;
      }

      // handle the remaining toProcess objects
      exportAllToProcessObjects(exportBecauseReferenced);

      // reset mem
      replaceToProcess();
      getAllToProcessObjects().clear();
      xmlHandler.endElement("http://www.openbravo.com", XMLConstants.OB_ROOT_ELEMENT, "ob:"
          + XMLConstants.OB_ROOT_ELEMENT);
      xmlHandler.endDocument();
    } catch (Exception e) {
      throw new EntityXMLException(e);
    }
  }

  private void exportAllToProcessObjects(boolean exportBecauseReferenced) throws Exception {
    boolean localExportBecauseReferenced = exportBecauseReferenced;
    while (toProcess.size() > 0) {
      final List<BaseOBObject> localToProcess = getToProcess();
      // reset the toProcess so that new objects are added to a new list
      replaceToProcess();
      for (BaseOBObject bob : localToProcess) {
        export(bob, localExportBecauseReferenced);
        getOutput().flush();
      }
      localExportBecauseReferenced = true;
    }
  }

  protected void export(BaseOBObject obObject, boolean isAddedBecauseReferenced)
      throws SAXException {
    export(obObject, isAddedBecauseReferenced, null);
  }

  protected void export(BaseOBObject obObject, boolean isAddedBecauseReferenced,
      Boolean excludeAuditInfo) throws SAXException {
    final String entityName = DalUtil.getEntityName(obObject);

    final AttributesImpl entityAttrs = new AttributesImpl();
    // set the id and identifier attributes
    final Object id = DalUtil.getId(obObject);
    if (id != null) {
      entityAttrs.addAttribute("", "", XMLConstants.ID_ATTRIBUTE, "CDATA", id.toString());
    }
    if (!isOptionMinimizeXMLSize()) {
      entityAttrs.addAttribute("", "", XMLConstants.IDENTIFIER_ATTRIBUTE, "CDATA",
          IdentifierProvider.getInstance().getIdentifier(obObject));
    }

    // if this object has been added as a referenced object
    // set the reference attribute so that we at import can treat this
    // one differently
    if (isAddedBecauseReferenced) {
      entityAttrs.addAttribute("", "", XMLConstants.REFERENCE_ATTRIBUTE, "CDATA", "true");
    }

    xmlHandler.startElement("", "", entityName, entityAttrs);

    // depending on the security only a limited set of
    // properties is exported
    boolean onlyIdentifierProps;
    if (OBContext.getOBContext().isInAdministratorMode()) {
      onlyIdentifierProps = false;
    } else {
      onlyIdentifierProps = OBContext.getOBContext().getEntityAccessChecker()
          .isDerivedReadable(obObject.getEntity());
    }

    final List<Property> exportableProperties;
    // second 'and' is necessary because referenced entities are not part of the dataset
    if (getDataSet() != null && dataSetTablesByEntity.get(obObject.getEntity()) != null) {
      final DataSetTable dst = dataSetTablesByEntity.get(obObject.getEntity());
      exportableProperties = DataSetService.getInstance().getExportableProperties(obObject, dst,
          dst.getDataSetColumnList(), optionExportTransientInfo);
    } else {
      exportableProperties = new ArrayList<Property>(obObject.getEntity().getProperties());
      if (excludeAuditInfo != null && excludeAuditInfo) {
        DataSetService.getInstance().removeAuditInfo(exportableProperties);
      }
    }

    // export each property
    for (final Property p : exportableProperties) {
      if (onlyIdentifierProps && !p.isIdentifier()) {
        continue;
      }

      if (p.isClientOrOrganization() && !isOptionExportClientOrganizationReferences()) {
        continue;
      }

      if (p.isOneToMany() && p.isChild()
          && (!isOptionIncludeChildren() || isAddedBecauseReferenced)) {
        continue;
      }

      // note only not-mandatory transient fields are allowed to be
      // not exported, a mandatory field should always be exported
      // auditinfo is mandatory but can be ignored for export
      // as it is always set
      if (p.isAuditInfo() && !isOptionExportAuditInfo()
          && Traceable.class.isAssignableFrom(obObject.getClass())) {
        continue;
      }
      final boolean isTransientField = p.isTransient(obObject);
      if (!p.isMandatory() && isTransientField && !isOptionExportTransientInfo()) {
        continue;
      }

      // set the tag
      final AttributesImpl propertyAttrs = new AttributesImpl();

      // add transient attribute
      if (p.isTransient(obObject)) {
        propertyAttrs.addAttribute("", "", XMLConstants.TRANSIENT_ATTRIBUTE, "CDATA", "true");
      }
      if (p.isAuditInfo()) {
        propertyAttrs.addAttribute("", "", XMLConstants.TRANSIENT_ATTRIBUTE, "CDATA", "true");
      }
      if (p.isInactive()) {
        propertyAttrs.addAttribute("", "", XMLConstants.INACTIVE_ATTRIBUTE, "CDATA", "true");
      }

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
              log.error("Object (" + obObject.getEntityName() + "(" + obObject.getId()
                  + ")): The value " + strValue + " used in this object is not valid, there is no "
                  + referedEntity.getName() + " with that id");
              // Check.isNotNull(obValue, "The value " + strValue + " used in treeNode "
              // + treeNode.getId() + " is not valid, there is no " + referedEntity.getName()
              // + " with that id");
            } else {
              addToExportList((BaseOBObject) obValue);
            }
          }
        }
        final String txt = ((PrimitiveDomainType) p.getDomainType()).convertToString(value);
        xmlHandler.startElement("", "", p.getName(), propertyAttrs);
        xmlHandler.characters(txt.toCharArray(), 0, txt.length());
        xmlHandler.endElement("", "", p.getName());
      } else if (p.isOneToMany() && p.isChild()) {
        xmlHandler.startElement("", "", p.getName(), propertyAttrs);

        // get all the children and export each child
        @SuppressWarnings("unchecked")
        final Collection<BaseOBObject> c = (Collection<BaseOBObject>) value;
        List<BaseOBObject> childObjects = new ArrayList<BaseOBObject>(c);
        Collections.sort(childObjects, new BaseOBIDHexComparator());
        for (final Object o : childObjects) {
          // embed in the parent
          if (isOptionEmbedChildren()) {
            final DataSetTable dst = (getDataSet() != null && obObject.getEntity() != null) ? dataSetTablesByEntity
                .get(obObject.getEntity()) : null;
            if ((excludeAuditInfo != null && excludeAuditInfo)
                || (dst != null && dst.isExcludeAuditInfo())) {
              export((BaseOBObject) o, false, true);
            } else {
              export((BaseOBObject) o, false);
            }
          } else {
            // add the child as a tag, the child entityname is
            // used as the tagname
            final BaseOBObject child = (BaseOBObject) o;

            // add attributes
            final AttributesImpl childAttrs = new AttributesImpl();
            childAttrs.addAttribute("", "", XMLConstants.TRANSIENT_ATTRIBUTE, "CDATA", "true");
            childAttrs.addAttribute("", "", XMLConstants.ID_ATTRIBUTE, "CDATA", DalUtil
                .getId(child).toString());
            if (!isOptionMinimizeXMLSize()) {
              childAttrs.addAttribute("", "", XMLConstants.IDENTIFIER_ATTRIBUTE, "CDATA",
                  IdentifierProvider.getInstance().getIdentifier(child));
            }
            // and write the element
            final String childEntityName = DalUtil.getEntityName(child);
            xmlHandler.startElement("", "", childEntityName, childAttrs);
            xmlHandler.endElement("", "", childEntityName);
            addToExportList((BaseOBObject) o);
          }
        }

        xmlHandler.endElement("", "", p.getName());
      } else if (!p.isOneToMany()) {

        // add reference attributes
        addReferenceAttributes(propertyAttrs, (BaseOBObject) value);

        // and write the element
        xmlHandler.startElement("", "", p.getName(), propertyAttrs);
        xmlHandler.endElement("", "", p.getName());

        // and also export the object itself if required
        // but do not add auditinfo references
        if (isOptionIncludeReferenced() && !p.isAuditInfo() && !p.isClientOrOrganization()) {
          addToExportList((BaseOBObject) value);
        }
      }
    }
    xmlHandler.endElement("", "", entityName);
  }

  private void addReferenceAttributes(AttributesImpl attrs, BaseOBObject referedObject) {
    if (referedObject == null) {
      return;
    }
    // final Element refElement =
    // currentElement.addElement(REFERENCE_ELEMENT_NAME);
    attrs.addAttribute("", "", XMLConstants.ID_ATTRIBUTE, "CDATA", DalUtil.getId(referedObject)
        .toString());
    if (!isOptionMinimizeXMLSize()) {
      attrs.addAttribute("", "", XMLConstants.ENTITYNAME_ATTRIBUTE, "CDATA",
          DalUtil.getEntityName(referedObject));
      attrs.addAttribute("", "", XMLConstants.IDENTIFIER_ATTRIBUTE, "CDATA", IdentifierProvider
          .getInstance().getIdentifier(referedObject));
    }
  }

  protected void addToExportList(BaseOBObject bob) {
    // only export references if belonging to the current client
    if (getClient() != null && bob instanceof ClientEnabled
        && !((ClientEnabled) bob).getClient().getId().equals(getClient().getId())) {
      return;
    }

    // was already exported
    if (getAllToProcessObjects().contains(bob)) {
      return;
    }
    getToProcess().add(bob);
    allToProcessObjects.add(bob);
  }

  /**
   * Controls if referenced objects (through many-to-one associations) should also be exported (in
   * the root of the xml).
   * 
   * @return true the referenced objects are exported, false (the default) referenced objects are
   *         not exported
   */
  public boolean isOptionIncludeReferenced() {
    return optionIncludeReferenced;
  }

  /**
   * Controls if referenced objects (through many-to-one associations) should also be exported (in
   * the root of the xml).
   * 
   * @param optionIncludeReferenced
   *          set to true the referenced objects are exported, set to false (the default) referenced
   *          objects are not exported
   */
  public void setOptionIncludeReferenced(boolean optionIncludeReferenced) {
    this.optionIncludeReferenced = optionIncludeReferenced;
  }

  /**
   * Controls if children (the one-to-many associations) are exported. If true then the children can
   * be exported embedded in the parent or in the root of the xml. This is controlled by the
   * {@link #isOptionEmbedChildren()} option.
   * 
   * @return true children are exported as well, false (the default) children are not exported
   */
  public boolean isOptionIncludeChildren() {
    return optionIncludeChildren;
  }

  /**
   * Controls if children (the one-to-many associations) are exported. If true then the children can
   * be exported embedded in the parent or in the root of the xml. This is controlled by the
   * {@link #isOptionEmbedChildren()} option.
   * 
   * @param optionIncludeChildren
   *          set to true children are exported as well, set to false (the default) children are not
   *          exported
   */
  public void setOptionIncludeChildren(boolean optionIncludeChildren) {
    this.optionIncludeChildren = optionIncludeChildren;
  }

  /**
   * This option controls if children are exported within the parent or in the root of the xml. The
   * default is embedded (default value is true).
   * 
   * @return true (default) children are embedded in the parent, false children are exported in the
   *         root of the xml
   */
  public boolean isOptionEmbedChildren() {
    return optionEmbedChildren;
  }

  /**
   * This option controls if children are exported within the parent or in the root of the xml. The
   * default is embedded (default value is true).
   */
  public void setOptionEmbedChildren(boolean optionEmbedChildren) {
    this.optionEmbedChildren = optionEmbedChildren;
  }

  private List<BaseOBObject> getToProcess() {
    return toProcess;
  }

  private void replaceToProcess() {
    toProcess = new ArrayList<BaseOBObject>();
  }

  private Set<BaseOBObject> getAllToProcessObjects() {
    return allToProcessObjects;
  }

  protected void addSystemAttributes(AttributesImpl systemAttrs) {
    if (!isAddSystemAttributes()) {
      return;
    }
    try {
      OBContext.setAdminMode();
      final List<SystemInformation> sis = OBDal.getInstance()
          .createCriteria(SystemInformation.class).list();
      Check.isTrue(sis.size() > 0, "There should be at least one SystemInfo record but there are "
          + sis.size());
      systemAttrs.addAttribute("", "", XMLConstants.DATE_TIME_ATTRIBUTE, "CDATA", "" + new Date());
      systemAttrs.addAttribute("", "", XMLConstants.OB_VERSION_ATTRIBUTE, "CDATA", sis.get(0)
          .getOpenbravoVersion() + "");
      systemAttrs.addAttribute("", "", XMLConstants.OB_REVISION_ATTRIBUTE, "CDATA", sis.get(0)
          .getCodeRevision() + "");
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private boolean isAddSystemAttributes() {
    return addSystemAttributes;
  }

  /**
   * If set to true then the system version and revision are exported as attributes of the root tag.
   * 
   * @param addSystemAttributes
   *          if true (the default) then the Openbravo version and revision are exported in the root
   *          tag.
   */
  public void setAddSystemAttributes(boolean addSystemAttributes) {
    this.addSystemAttributes = addSystemAttributes;
  }

  /**
   * Controls if the client and organization properties are also exported. The default is false. If
   * this is set to true then the import program should take into account that the
   * client/organization are present in the import xml.
   * 
   * @return if true then the client/organization properties are exported, if false then not
   */
  public boolean isOptionExportClientOrganizationReferences() {
    return optionExportClientOrganizationReferences;
  }

  /**
   * Controls if the client and organization properties are also exported. The default is false. If
   * this is set to true then the import program should take into account that the
   * client/organization are present in the import xml.
   * 
   * @param optionExportClientOrganizationReferences
   *          if set to true then the client/organization properties are exported, if false then not
   */
  public void setOptionExportClientOrganizationReferences(
      boolean optionExportClientOrganizationReferences) {
    this.optionExportClientOrganizationReferences = optionExportClientOrganizationReferences;
  }

  public boolean isOptionExportTransientInfo() {
    return optionExportTransientInfo;
  }

  public void setOptionExportTransientInfo(boolean optionExportTransientInfo) {
    this.optionExportTransientInfo = optionExportTransientInfo;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public boolean isOptionExportAuditInfo() {
    return optionExportAuditInfo;
  }

  public void setOptionExportAuditInfo(boolean optionExportAuditInfo) {
    this.optionExportAuditInfo = optionExportAuditInfo;
  }

  public boolean isOptionMinimizeXMLSize() {
    return optionMinimizeXMLSize;
  }

  public void setOptionMinimizeXMLSize(boolean optionMinimizeXMLSize) {
    this.optionMinimizeXMLSize = optionMinimizeXMLSize;
  }

  public Writer getOutput() {
    return output;
  }

  public void setOutput(Writer output) {
    this.output = output;
  }

  public DataSet getDataSet() {
    return dataSet;
  }

  public void setDataSet(DataSet dataSet) {
    this.dataSet = dataSet;

    dataSetTablesByEntity = new HashMap<Entity, DataSetTable>();
    for (DataSetTable dst : dataSet.getDataSetTableList()) {
      final Entity entity = ModelProvider.getInstance().getEntityByTableName(
          dst.getTable().getDBTableName());
      dataSetTablesByEntity.put(entity, dst);
    }
  }

  public ScrollableResults getDataScroller() {
    return dataScroller;
  }

  public void setDataScroller(ScrollableResults dataScroller) {
    this.dataScroller = dataScroller;
  }
}