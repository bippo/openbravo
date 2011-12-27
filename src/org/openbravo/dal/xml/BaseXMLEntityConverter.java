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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.util.Check;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Contains logic and attributes which are common for both the Dom and Stax based converters
 * implemented in the subclasses.
 * 
 * @author mtaal
 */

public class BaseXMLEntityConverter implements OBNotSingleton {
  // This class should translate the

  private static final Logger log = Logger.getLogger(EntityXMLConverter.class);

  public static BaseXMLEntityConverter newInstance() {
    return OBProvider.getInstance().get(BaseXMLEntityConverter.class);
  }

  private EntityResolver entityResolver;

  private EntityXMLProcessor importProcessor;

  // keeps track which instances are part of the xml because they were
  // referenced
  private Set<BaseOBObject> referenced = new HashSet<BaseOBObject>();

  // keeps track which instances changed during the import and need to
  // be updated
  private List<BaseOBObject> toUpdate = new ArrayList<BaseOBObject>();
  private Set<BaseOBObject> checkUpdate = new HashSet<BaseOBObject>();

  // keeps track which instances need to be inserted
  private List<BaseOBObject> toInsert = new ArrayList<BaseOBObject>();
  private Set<BaseOBObject> checkInsert = new HashSet<BaseOBObject>();

  // the client and organization for which the import is done
  private Client client;
  private Organization organization;

  // some error and log messages
  private StringBuilder errorMessages = new StringBuilder();
  private StringBuilder logMessages = new StringBuilder();
  private StringBuilder warningMessages = new StringBuilder();

  private Map<BaseOBObject, BaseOBObject> replacedObjects = new HashMap<BaseOBObject, BaseOBObject>();

  // signals that this is an overall client data import
  // in this case the client/organization property is updated through the
  // xml, note that this assumes that the client/organization of the object
  // are present in xml! Also if this option is set then the client and
  // organization in this object are null
  private boolean optionClientImport = false;

  private boolean optionImportAuditInfo = false;

  // process stops at 20 errors
  private int noOfErrors = 0;

  protected void clear() {
    toUpdate.clear();
    checkUpdate.clear();
    toInsert.clear();
    checkInsert.clear();
    errorMessages = new StringBuilder();
    logMessages = new StringBuilder();
    noOfErrors = 0;
    referenced.clear();
    entityResolver.clear();
  }

  protected void addToInsertOrUpdateLists(String id, BaseOBObject bob, boolean writable,
      boolean updated, boolean hasReferenceAttribute, boolean preventRealUpdate) {
    final String originalId = getEntityResolver().getOriginalId(bob);
    String originalIdStr = "";
    if (originalId != null) {
      originalIdStr = ", with import id: " + originalId;
    }

    if (!writable && updated) {
      if (bob.isNewOBObject()) {
        warn("Not allowed to create entity: " + bob.getEntityName() + " (import id: " + id + ") "
            + " because it is not writable");
        return;
      } else {
        warn("Not updating entity: " + bob.getIdentifier() + " because it is not writable");
        return;
      }
    } else if (preventRealUpdate) {
      Check.isTrue(!writable || (hasReferenceAttribute && !bob.isNewOBObject()),
          "This case may only occur for referenced objects which are not new");
      // if the object is referenced then it can not be updated
      if (hasReferenceAttribute && !bob.isNewOBObject()) {
        log.debug("Entity " + bob + " (" + bob.getEntity().getTableName() + ") "
            + " has not been updated because it already exists and "
            + "it is imported as a reference from another object");
      }
      if (!writable && !bob.isNewOBObject() && !hasReferenceAttribute) {
        warn("Not updating entity: " + bob.getIdentifier() + "(" + bob.getEntityName() + ") "
            + " because it is not writable for this user");
      }
    } else if (bob.isNewOBObject()) {
      if (!checkInsert.contains(bob)) {
        warnDifferentClientOrg(bob, "Creating");
        log("Inserted entity " + bob.getIdentifier() + originalIdStr);
        toInsert.add(bob);
        checkInsert.add(bob);
      }
    } else if (updated && !checkUpdate.contains(bob)) {
      Check.isFalse(bob.isNewOBObject(), "May only be here for not-new objects");
      // never update an object which was exported as referenced
      Check.isFalse(hasReferenceAttribute, "Referenced objects may not be updated");

      // warn in case of different organization/client
      warnDifferentClientOrg(bob, "Updating");

      log("Updated entity " + bob.getIdentifier() + originalIdStr);

      toUpdate.add(bob);
      checkUpdate.add(bob);
    }

  }

  // if there is a matching object in the db then that one should be
  // used
  protected BaseOBObject replaceByUniqueObject(BaseOBObject bob) {
    BaseOBObject otherUniqueObject = null;
    if (bob.isNewOBObject() && bob.getEntity().getUniqueConstraints().size() > 0) {
      if (bob.getEntity().getTableName().toLowerCase().endsWith("_trl")
          || ("".equals(bob.getId()) || bob.getId() == null)) {
        // We want to match by unique constraint only in the case the bob doesn't have id, or
        // belongs to a _trl table (which have ids which are randomly generated by the
        // verify_language process)
        otherUniqueObject = entityResolver.findUniqueConstrainedObject(bob);
      }
      if (otherUniqueObject != null && otherUniqueObject != bob) {
        // now copy the imported values from the bob to
        // otherUniqueObject
        for (final Property p : bob.getEntity().getProperties()) {
          final boolean isNotImportableProperty = p.isTransient(bob) || p.isAuditInfo()
              || p.isInactive() || p.isId();
          if (isNotImportableProperty) {
            continue;
          }
          // do not change the client or organization of an
          // existing object
          if (p.isClientOrOrganization()) {
            continue;
          }
          // do not replace one to manies
          if (!p.isOneToMany()) {
            otherUniqueObject.set(p.getName(), bob.get(p.getName()));
          }
        }
        // and replace the bob, because the object from the db
        // should be used
        getEntityResolver().exchangeObjects(bob, otherUniqueObject);
        replacedObjects.put(bob, otherUniqueObject);
        return otherUniqueObject;
      }
    }
    return bob;
  }

  protected void repairReferences() {
    for (BaseOBObject bob : replacedObjects.keySet()) {
      final BaseOBObject newObject = replacedObjects.get(bob);
      repairReferences(bob, newObject);
    }
  }

  // if an object is replaced by its counterpart then repair all references
  // to the old one
  private void repairReferences(BaseOBObject prevObject, BaseOBObject newObject) {
    repairReferences(prevObject, newObject, getToInsert());
    repairReferences(prevObject, newObject, getToUpdate());
  }

  private void repairReferences(BaseOBObject prevObject, BaseOBObject newObject,
      List<BaseOBObject> list) {
    for (BaseOBObject bob : list) {
      for (Property p : bob.getEntity().getProperties()) {
        if (p.isPrimitive()) {
          continue;
        }
        if (p.isOneToMany() && p.isChild()) {
          @SuppressWarnings("unchecked")
          final List<Object> childList = (List<Object>) bob.get(p.getName());
          int index = -1;
          while ((index = childList.indexOf(prevObject)) != -1) {
            Check.isTrue(childList.get(index) == prevObject, "Object" + prevObject
                + " not present on index " + index + " of property " + p + " of object " + bob);
            childList.set(index, newObject);
          }
        } else if (bob.get(p.getName()) == prevObject) {
          bob.set(p.getName(), newObject);
        }
      }
    }
  }

  protected Object replaceValue(BaseOBObject owner, Property property, Object newValue) {
    if (importProcessor == null) {
      return newValue;
    } else {
      return importProcessor.replaceValue(owner, property, newValue);
    }
  }

  protected void checkClientOrganizationSet(BaseOBObject bob) {
    if (bob.getEntity().isClientEnabled()) {
      final ClientEnabled ce = (ClientEnabled) bob;
      if (ce.getClient() == null) {
        error("The client of entity " + bob.getIdentifier()
            + " is not set. For a client data import the client needs"
            + " to be set. Check that the xml was created "
            + "with client/organization property export to true");
      }
    }
    if (bob.getEntity().isOrganizationEnabled()) {
      final OrganizationEnabled oe = (OrganizationEnabled) bob;
      if (oe.getOrganization() == null) {
        error("The organization of entity " + bob.getIdentifier()
            + " is not set. For a client data import the organization needs"
            + " to be set. Check that the xml was created "
            + "with client/organization property export to true");
      }
    }
  }

  protected void warnDifferentClientOrg(BaseOBObject bob, String prefix) {

    // don't need to check as the object retains his client/organization
    if (isOptionClientImport()) {
      return;
    }

    if (bob.getEntity().isClientEnabled()) {
      final ClientEnabled ce = (ClientEnabled) bob;
      if (!ce.getClient().getId().equals(getClient().getId())) {
        warn(prefix + " entity " + bob.getIdentifier()
            + " eventhough it does not belong to the target client " + getClient().getIdentifier()
            + " but to client " + ce.getClient().getIdentifier());
      }
    }
    if (bob.getEntity().isOrganizationEnabled()) {
      final OrganizationEnabled oe = (OrganizationEnabled) bob;
      if (!oe.getOrganization().getId().equals(getOrganization().getId())) {
        warn(prefix + " entity " + bob.getIdentifier()
            + " eventhough it does not belong to the target organization "
            + getOrganization().getIdentifier() + " but to organization "
            + oe.getOrganization().getIdentifier());
      }
    }
  }

  /**
   * Checks for objects which were not found in the database but are neither defined in the imported
   * xml. This is not allowed and can result in strange errors.
   * 
   * If a dangling object is found then the error message is added to the list of error messages.
   */
  protected void checkDanglingObjects() {

    // clone the resolved entities
    final List<BaseOBObject> resolvedValues = new ArrayList<BaseOBObject>(getEntityResolver()
        .getData().values());

    // remove the to-insert objects from the resolvedEntities
    resolvedValues.removeAll(getToInsert());

    // now at this point there should not be any new objects anymore
    // in the resolvedEntities
    final StringBuilder sb = new StringBuilder();
    for (BaseOBObject bob : resolvedValues) {
      if (bob.isNewOBObject()) {
        if (sb.length() > 0) {
          sb.append("\n");
        }

        Object idObject = null;
        for (Object key : getEntityResolver().getData().keySet()) {
          final Object value = getEntityResolver().getData().get(key);
          if (value == bob) {
            idObject = key;
            break;
          }
        }

        sb.append("Referenced object " + bob.getEntityName() + " (id: " + bob.getId() + " / "
            + idObject + ") not present in the xml or in the database.");
      }
    }

    if (sb.length() > 0) {
      error(sb.toString());
    }

  }

  protected void warn(String msg) {
    if (warningMessages.length() > 0) {
      warningMessages.append("\n");
    }
    warningMessages.append(msg);
  }

  protected void log(String msg) {
    if (logMessages.length() > 0) {
      logMessages.append("\n");
    }
    logMessages.append(msg);
  }

  protected boolean hasErrorOccured() {
    return errorMessages.length() > 0;
  }

  protected void error(String msg) {
    if (errorMessages.length() > 0) {
      errorMessages.append("\n");
    }
    errorMessages.append(msg);
    if (noOfErrors++ > 20) {
      throw new EntityXMLException("Too many errors, exiting import, error messages:\n"
          + errorMessages);
    }
  }

  protected BaseOBObject resolve(String entityName, String id, boolean reference) {
    return entityResolver.resolve(entityName, id, reference);
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  /**
   * Returns the objects which exist in the database and will be updated.
   * 
   * @return list of objects which will be updated in the database.
   */
  public List<BaseOBObject> getToUpdate() {
    return toUpdate;
  }

  /**
   * Returns the list of objects which should be inserted in the database
   * 
   * @return the list of new BaseOBObjects which should be inserted in the database
   */
  public List<BaseOBObject> getToInsert() {
    return toInsert;
  }

  /**
   * The error messages logged during the import process. If no error message exist then null is
   * returned. If error messages exist then the user of this class should not update the database
   * and do a rollback.
   * 
   * @return the logged error messages, null if no error messages are present
   */
  public String getErrorMessages() {
    if (errorMessages.length() == 0) {
      return null;
    }
    return errorMessages.toString();
  }

  /**
   * The warning messages logged during the import process. Warning messages are non-failing
   * messages. The database can be updated if there are warning messages. If no warning message
   * exist then null is returned.
   * 
   * @return the logged warning messages, null if no warning messages are present
   */
  public String getWarningMessages() {
    if (warningMessages.length() == 0) {
      return null;
    }
    return warningMessages.toString();
  }

  /**
   * The standard log messages logged during the import process. If no log message exist then null
   * is returned.
   * 
   * @return the logged messages, null if no messages are present
   */
  public String getLogMessages() {
    if (logMessages.length() == 0) {
      return null;
    }
    return logMessages.toString();
  }

  /**
   * @return the EntityResolver used by this Converter.
   */
  public EntityResolver getEntityResolver() {

    if (entityResolver == null) {
      entityResolver = EntityResolver.getInstance();
    }
    return entityResolver;
  }

  /**
   * Determines if this a client import. A client import differs from a standard import because it
   * is assumed that all Client/Organization level information is present in the xml and only System
   * objects should be retrieved from the database.
   * 
   * @return the value of the client import option (default is false)
   */
  public boolean isOptionClientImport() {
    return optionClientImport;
  }

  /**
   * Determines if this a client import. A client import differs from a standard import because it
   * is assumed that all Client/Organization level information is present in the xml and only System
   * objects should be retrieved from the database.
   * 
   * @param optionClientImport
   *          sets the value of the client import option (default is false)
   */
  public void setOptionClientImport(boolean optionClientImport) {
    this.optionClientImport = optionClientImport;
  }

  public EntityXMLProcessor getImportProcessor() {
    return importProcessor;
  }

  public void setImportProcessor(EntityXMLProcessor importProcessor) {
    this.importProcessor = importProcessor;
  }

  public void setEntityResolver(EntityResolver entityResolver) {
    this.entityResolver = entityResolver;
  }

  public boolean isOptionImportAuditInfo() {
    return optionImportAuditInfo;
  }

  public void setOptionImportAuditInfo(boolean optionImportAuditInfo) {
    this.optionImportAuditInfo = optionImportAuditInfo;
  }
}