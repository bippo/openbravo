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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.xml;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.model.ad.alert.Alert;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.utility.Tree;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.payment.DebtPayment;

/**
 * Handles primitive reference values. These are references which are not modeled as foreign keys to
 * a specific table but as string/table combinations stored in different fields. Sometimes the table
 * is stored in a separate field, sometimes it needs to be computed (see
 * {@link Tree#PROPERTY_TYPEAREA}).
 * 
 * 
 * This class provides utility methods to find these references and resolve them.
 * 
 * @see TreeNode#PROPERTY_NODE
 * @see TreeNode#PROPERTY_REPORTSET
 * @see AccountingFact#PROPERTY_RECORDID
 * @see AccountingFact#PROPERTY_RECORDID2
 * @see Alert#PROPERTY_RECORDID
 * 
 * @see EntityXMLConverter
 * 
 * @author mtaal
 */

public class PrimitiveReferenceHandler implements OBSingleton {

  private static PrimitiveReferenceHandler instance;

  public static synchronized PrimitiveReferenceHandler getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(PrimitiveReferenceHandler.class);
    }
    return instance;
  }

  public static synchronized void setInstance(PrimitiveReferenceHandler instance) {
    PrimitiveReferenceHandler.instance = instance;
  }

  private List<Property> treeNodePrimitiveReferences;
  private List<Property> alertPrimitiveReferences;
  private List<Property> attachmentPrimitiveReferences;
  private List<Property> accountingFactPrimitiveReferences;

  /**
   * @param entity
   *          the Entity for which to get the primitive reference properties
   * @return list of properties of the entity which are primitive references
   *         {@link #isPrimitiveReference(Property)}).
   */
  public List<Property> getPrimitiveReferences(Entity entity) {
    if (entity.getName().equals(TreeNode.ENTITY_NAME)) {
      if (treeNodePrimitiveReferences != null) {
        return treeNodePrimitiveReferences;
      }
      final List<Property> result = new ArrayList<Property>();
      result.add(entity.getProperty(TreeNode.PROPERTY_REPORTSET));
      result.add(entity.getProperty(TreeNode.PROPERTY_NODE));
      treeNodePrimitiveReferences = result;
      return treeNodePrimitiveReferences;
    }
    if (entity.getName().equals(Alert.ENTITY_NAME)) {
      if (alertPrimitiveReferences != null) {
        return alertPrimitiveReferences;
      }
      final List<Property> result = new ArrayList<Property>();
      result.add(entity.getProperty(Alert.PROPERTY_REFERENCESEARCHKEY));
      alertPrimitiveReferences = result;
      return alertPrimitiveReferences;
    }
    if (entity.getName().equals(Attachment.ENTITY_NAME)) {
      if (attachmentPrimitiveReferences != null) {
        return attachmentPrimitiveReferences;
      }
      final List<Property> result = new ArrayList<Property>();
      result.add(entity.getProperty(Attachment.PROPERTY_RECORD));
      attachmentPrimitiveReferences = result;
      return attachmentPrimitiveReferences;
    }
    if (entity.getName().equals(AccountingFact.ENTITY_NAME)) {
      if (accountingFactPrimitiveReferences != null) {
        return accountingFactPrimitiveReferences;
      }
      final List<Property> result = new ArrayList<Property>();
      result.add(entity.getProperty(AccountingFact.PROPERTY_RECORDID));
      result.add(entity.getProperty(AccountingFact.PROPERTY_RECORDID2));
      accountingFactPrimitiveReferences = result;
      return accountingFactPrimitiveReferences;
    }
    throw new OBException("Entity " + entity + " does not have primitive references");
  }

  /**
   * Code which handles so-called primitive references, These are references which are modeled as
   * primitive types and which do not have a foreign key constraint defined in the database. The
   * reference is defined using 2 columns: one holding the id, the other holding the table
   * 
   * @param property
   *          the property to check if it is a primitive reference
   * @return true if this property is a primitive reference, false otherwise
   */
  public boolean isPrimitiveReference(Property property) {
    if (property.getEntity().getName().equals(TreeNode.ENTITY_NAME)
        && (property.getName().equals(TreeNode.PROPERTY_REPORTSET) || property.getName().equals(
            TreeNode.PROPERTY_NODE))) {
      return true;
    }
    if (property.getEntity().getName().equals(Alert.ENTITY_NAME)
        && property.getName().equals(Alert.PROPERTY_REFERENCESEARCHKEY)) {
      return true;
    }
    if (property.getEntity().getName().equals(Attachment.ENTITY_NAME)
        && property.getName().equals(Attachment.PROPERTY_RECORD)) {
      return true;
    }
    if (property.getEntity().getName().equals(AccountingFact.ENTITY_NAME)
        && (property.getName().equals(AccountingFact.PROPERTY_RECORDID) || property.getName()
            .equals(AccountingFact.PROPERTY_RECORDID2))) {
      return true;
    }
    return false;
  }

  /**
   * Returns true if the passed object has so-called primitive references (
   * {@link #isPrimitiveReference(Property)}).
   * 
   * @param obObject
   *          the object having a primitive reference property (or not)
   * @return true if this entity holds a primitive reference
   */
  // TODO; store this concept in the entity itself, is faster
  // Discussion: the methods here however make use of a lot of string comparison
  // when this would be done in the Entity class then it is not allowed to
  // refer to generated classes and then property name/entity name comparisons
  // are not compile-time-checked
  public boolean hasObjectPrimitiveReference(BaseOBObject obObject) {
    final Entity entity = obObject.getEntity();
    if (entity.getName().equals(TreeNode.ENTITY_NAME)) {
      return true;
    }
    if (entity.getName().equals(Alert.ENTITY_NAME)) {
      return true;
    }
    if (entity.getName().equals(Attachment.ENTITY_NAME)) {
      return true;
    }
    if (entity.getName().equals(AccountingFact.ENTITY_NAME)) {
      return true;
    }
    return false;
  }

  /**
   * In case of a primitive reference ({@link #isPrimitiveReference(Property)}) then this method
   * returns the Entity to which the property value refers.
   * 
   * This method may only be called if the primitive reference is unequal to null.
   * 
   * @param obObject
   *          the object refering to an instance of an Entity
   * @param property
   *          the primitive reference property
   * @return the Entity to which the property value refers, can not be null
   * @throws OBException
   *           if no Entity can be found
   */
  public Entity getPrimitiveReferencedEntity(BaseOBObject obObject, Property property) {
    if (property.getEntity().getName().equals(TreeNode.ENTITY_NAME)) {
      final TreeNode treeNode = (TreeNode) obObject;
      final Entity entity = ModelProvider.getInstance().getEntityFromTreeType(
          treeNode.getTree().getTypeArea());
      if (entity == null) {
        throw new OBException("No entity found for treetype " + treeNode.getTree().getTypeArea());
      }
      return entity;
    }
    if (property.getEntity().getName().equals(Alert.ENTITY_NAME)) {
      final Alert alert = (Alert) obObject;
      final Table table = alert.getAlertRule().getTab().getTable();
      final Entity entity = ModelProvider.getInstance().getEntity(table.getName());
      if (entity == null) {
        throw new OBException("No entity for table with name " + table.getName());
      }
      return entity;
    }
    if (property.getEntity().getName().equals(Attachment.ENTITY_NAME)) {
      final Attachment attachment = (Attachment) obObject;
      final Entity entity = ModelProvider.getInstance().getEntity(attachment.getTable().getName());
      if (entity == null) {
        throw new OBException("No entity for table with name " + attachment.getTable().getName());
      }
      return entity;
    }
    if (property.getEntity().getName().equals(AccountingFact.ENTITY_NAME)) {
      if (property.getName().equals(AccountingFact.PROPERTY_RECORDID2)) {
        return ModelProvider.getInstance().getEntity(DebtPayment.ENTITY_NAME);
      }
      final AccountingFact accountingFact = (AccountingFact) obObject;
      final Entity entity = ModelProvider.getInstance().getEntity(
          accountingFact.getTable().getName());
      if (entity == null) {
        throw new OBException("No entity for table with name "
            + accountingFact.getTable().getName());
      }
      return entity;
    }
    throw new OBException("Property " + property + " is not a primitive reference");
  }

}