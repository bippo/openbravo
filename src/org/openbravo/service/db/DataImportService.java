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

package org.openbravo.service.db;

import java.io.Reader;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.util.Check;
import org.openbravo.base.validation.ValidationException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityResolver;
import org.openbravo.dal.xml.EntityXMLProcessor;
import org.openbravo.dal.xml.PrimitiveReferenceHandler;
import org.openbravo.dal.xml.StaxXMLEntityConverter;
import org.openbravo.dal.xml.XMLEntityConverter;
import org.openbravo.dal.xml.EntityResolver.ResolvingMode;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.ReferenceDataStore;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.enterprise.Organization;

/**
 * Imports business objects from XML. The business objects can be imported in a specific Client and
 * Organization or as a complete Client import.
 * 
 * @author Martin Taal
 */
public class DataImportService implements OBSingleton {
  private static final Logger log = Logger.getLogger(DataImportService.class);

  private static DataImportService instance;

  public static synchronized DataImportService getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(DataImportService.class);
    }
    return instance;
  }

  public static synchronized void setInstance(DataImportService instance) {
    DataImportService.instance = instance;
  }

  /**
   * Imports the business objects using the client/organization. If there are no error messages and
   * no Exception occurred then the import method will persist the imported business objects.
   * However, the import method does not do a commit. It is the callers responsibility to call
   * commit (if the ImportResult does not contain error messages).
   * 
   * @param client
   *          the client in which the business objects are created/updated
   * @param organization
   *          the organization in which the business objects are created/updated
   * @param xml
   *          the xml containing the data
   * @return the ImportResult object contains error, log and warning messages and lists with the
   *         inserted and updated business objects
   */
  public ImportResult importDataFromXML(Client client, Organization organization, String xml) {
    return importDataFromXML(client, organization, xml, null);
  }

  /**
   * Imports the business objects using the client/organization. If there are no error messages and
   * no Exception occurred then the import method will persist the imported business objects.
   * However, the import method does not do a commit. It is the callers responsibility to call
   * commit (if the ImportResult does not contain error messages).
   * 
   * @param client
   *          the client in which the import takes place
   * @param organization
   *          the organization in which the business objects are created
   * @param xml
   *          the xml containing the data
   * @param module
   *          the module is used to update the AD_REF_DATA_LOADED table during the import action
   * @return the result of the import (error, log and warning messages, to-be-inserted and
   *         to-be-updated business objects
   */
  public ImportResult importDataFromXML(Client client, Organization organization, String xml,
      Module module) {
    try {
      final Document doc = DocumentHelper.parseText(xml);
      return importDataFromXML(client, organization, doc, true, module, null, false, false);
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  /**
   * Imports a complete client. This import method behaves slightly differently than the other
   * import methods because it does not use the client/organization of the current user but uses the
   * client and organization of the data in the import file itself. In addition no unique-constraint
   * checking is done.
   * 
   * @param importProcessor
   *          the import processor which is called for each object after the import
   * @param importAuditInfo
   *          if true then the auditInfo (updated, updatedBy etc.) is also imported.
   * @param reader
   *          the xml stream
   * 
   * @return ImportResult which contains the updated/inserted objects and log and error messages
   * 
   * @see #importDataFromXML(Client, Organization, String)
   */
  public ImportResult importClientData(EntityXMLProcessor importProcessor, boolean importAuditInfo,
      Reader reader) {
    try {

      final ImportResult ir = new ImportResult();

      boolean rolledBack = false;
      OBContext.setAdminMode();
      try {
        // disable the triggers to prevent unexpected extra db actions
        // during import
        TriggerHandler.getInstance().disable();

        final StaxXMLEntityConverter xec = StaxXMLEntityConverter.newInstance();
        xec.setOptionClientImport(true);
        xec.setOptionImportAuditInfo(importAuditInfo);
        xec.getEntityResolver().setOptionCreateReferencedIfNotFound(true);
        xec.setEntityResolver(ClientImportEntityResolver.getInstance());
        xec.setImportProcessor(importProcessor);
        xec.process(reader);

        ir.setLogMessages(xec.getLogMessages());
        ir.setErrorMessages(xec.getErrorMessages());
        ir.setWarningMessages(xec.getWarningMessages());

        if (ir.hasErrorOccured()) {
          OBDal.getInstance().rollbackAndClose();
          rolledBack = true;
          return ir;
        }

        if (importProcessor != null) {
          try {
            importProcessor.process(xec.getToInsert(), xec.getToUpdate());
          } catch (final Exception e) {
            // note on purpose caught and set in ImportResult
            ir.setException(e);
            ir.setErrorMessages(e.getMessage());
            OBDal.getInstance().rollbackAndClose();
            rolledBack = true;
            return ir;
          }
        }

        // validate the objects
        for (BaseOBObject bob : xec.getToInsert()) {
          validateObject(bob, ir);
        }
        for (BaseOBObject bob : xec.getToUpdate()) {
          validateObject(bob, ir);
        }

        if (ir.hasErrorOccured()) {
          OBDal.getInstance().rollbackAndClose();
          rolledBack = true;
          return ir;
        }

        // now save and update
        // do inserts and updates in opposite order, this is important
        // so that the objects on which other depend are inserted first
        final List<BaseOBObject> toInsert = xec.getToInsert();
        int done = 0;
        final Set<BaseOBObject> inserted = new HashSet<BaseOBObject>();
        for (int i = toInsert.size() - 1; i > -1; i--) {
          final BaseOBObject ins = toInsert.get(i);
          insertObjectGraph(ins, inserted, new HashSet<BaseOBObject>());
          ir.getInsertedObjects().add(ins);
          done++;
        }
        Check.isTrue(done == toInsert.size(),
            "Not all objects have been inserted, check for loop: " + done + "/" + toInsert.size());

        // flush to set the ids in the objects
        OBDal.getInstance().flush();

        // do the updates the other way around also
        done = 0;
        final List<BaseOBObject> toUpdate = xec.getToUpdate();
        for (int i = toUpdate.size() - 1; i > -1; i--) {
          final BaseOBObject upd = toUpdate.get(i);
          OBDal.getInstance().save(upd);
          ir.getUpdatedObjects().add(upd);
          done++;
        }
        Check.isTrue(done == toUpdate.size(),
            "Not all objects have been inserted, check for loop: " + done + "/" + toUpdate.size());

        // flush to set the ids in the objects
        OBDal.getInstance().flush();

        // now walk through the objects to repair primitive reference id's
        // note updates both the ir and changes the entityresolver
        repairPrimitiveReferences(ir, xec.getEntityResolver());

        if (ir.hasErrorOccured()) {
          OBDal.getInstance().rollbackAndClose();
          rolledBack = true;
          return ir;
        }

        OBDal.getInstance().flush();
      } catch (final Throwable t) {
        OBDal.getInstance().rollbackAndClose();
        rolledBack = true;
        final Throwable realThrowable = DbUtility.getUnderlyingSQLException(t);
        log.error(realThrowable.getMessage(), realThrowable);
        ir.setException(realThrowable);
      } finally {
        OBContext.restorePreviousMode();
        if (rolledBack) {
          TriggerHandler.getInstance().clear();
        } else if (TriggerHandler.getInstance().isDisabled()) {
          TriggerHandler.getInstance().enable();

          // do a special thing to update the clientlist and orglist columns
          // in the ad_role table with the newly created id's
          // this is done through a stored procedure
          SessionHandler.getInstance().getSession()
              .createSQLQuery("UPDATE AD_ROLE_ORGACCESS SET AD_ROLE_ID='0' where AD_ROLE_ID='0'")
              .executeUpdate();
        }
      }

      return ir;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  // the id's which are repaired are so-called primitive references
  // (for example AD_Tree_Node.node_id
  // these are references without foreign key which are modeled as a
  // string/varchar. During the import the id of an object may change, thereby
  // invalidating the primitive reference (which still uses the old id). This
  // method repairs those ids.
  // see the methods in XMLUtil related to primitive references
  private void repairPrimitiveReferences(ImportResult ir, EntityResolver entityResolver) {

    // at this point all references must exist
    entityResolver.setResolvingMode(ResolvingMode.MUST_EXIST);
    final List<BaseOBObject> repairReferences = new ArrayList<BaseOBObject>();
    for (BaseOBObject bob : ir.getUpdatedObjects()) {
      if (PrimitiveReferenceHandler.getInstance().hasObjectPrimitiveReference(bob)) {
        repairReferences.add(bob);
      }
    }
    for (BaseOBObject bob : ir.getInsertedObjects()) {
      if (PrimitiveReferenceHandler.getInstance().hasObjectPrimitiveReference(bob)) {
        repairReferences.add(bob);
      }
    }
    for (BaseOBObject objectToRepair : repairReferences) {
      if (objectToRepair instanceof TreeNode) {
        final TreeNode tn = (TreeNode) objectToRepair;
        final Entity entity = ModelProvider.getInstance().getEntityFromTreeType(
            tn.getTree().getTypeArea());
        if (entity == null) {
          if (ir.getWarningMessages() == null) {
            ir.setWarningMessages("Imported tree nodes belong to a tree with a tree type "
                + tn.getTree().getTypeArea() + " which is not related to any entity.");
          } else {
            ir.setWarningMessages(ir.getWarningMessages()
                + "\nImported tree nodes belong to a tree with a tree type "
                + tn.getTree().getTypeArea() + " which is not related to any entity.");
          }
          continue;
        }
      }
      for (Property p : PrimitiveReferenceHandler.getInstance().getPrimitiveReferences(
          objectToRepair.getEntity())) {
        final String value = (String) objectToRepair.get(p.getName());
        // also ignore 0 as there is a business partner tree node with 0
        if (value != null && !value.equals("0")) {
          final Entity entity = PrimitiveReferenceHandler.getInstance()
              .getPrimitiveReferencedEntity(objectToRepair, p);

          final BaseOBObject referencedBob = (BaseOBObject) entityResolver.resolve(
              entity.getName(), value, true);

          if (referencedBob == null || referencedBob.getId() == null) {
            if (ir.getErrorMessages() == null) {
              ir.setErrorMessages("The object " + objectToRepair
                  + " references an object (entity: " + entity + ") with id " + value
                  + " which does not exist in the database or in the import set.");
            } else {
              ir.setErrorMessages(ir.getErrorMessages() + "\nThe object " + objectToRepair
                  + " references an object (entity: " + entity + ") with id " + value
                  + " which does not exist in the database or in the import set.");
            }
          } else if (!referencedBob.getId().equals(value)) {
            objectToRepair.set(p.getName(), referencedBob.getId());
          }
        }
      }
    }

  }

  private void validateObject(BaseOBObject bob, ImportResult ir) {
    int i = 0;
    final StringBuilder msgs = new StringBuilder();
    for (Property p : bob.getEntity().getProperties()) {
      if (p.isOneToMany() || p.isAuditInfo() || p.isClientOrOrganization()) {
        continue;
      }
      final Object value = bob.get(p.getName());
      try {
        p.checkIsValidValue(value);
      } catch (ValidationException ve) {
        i++;
        // stop at 100 errors
        if (i > 100) {
          break;
        }
        if (msgs.length() > 0) {
          msgs.append("\n");
        }
        msgs.append("Object " + bob + " table/column: " + p.getEntity().getTableName() + "."
            + p.getColumnName() + " " + ve.getMessage());
      }
    }
    if (msgs.length() > 0) {
      if (ir.getErrorMessages() == null) {
        ir.setErrorMessages(msgs.toString());
      } else {
        ir.setErrorMessages(ir.getErrorMessages() + msgs);
      }
    }
  }

  private ImportResult importDataFromXML(Client client, Organization organization, Document doc,
      boolean createReferencesIfNotFound, Module module, EntityXMLProcessor importProcessor,
      boolean isClientImport, boolean importAuditInfo) {

    if (!isClientImport) {
      log.debug("Importing data for client " + client.getId()
          + (organization != null ? "/" + organization.getId() : ""));
    }

    final ImportResult ir = new ImportResult();

    boolean rolledBack = false;
    List<BaseOBObject> listNew = new Vector<BaseOBObject>();
    List<BaseOBObject> listChanged = new Vector<BaseOBObject>();
    try {
      // disable the triggers to prevent unexpected extra db actions
      // during import
      TriggerHandler.getInstance().disable();

      final XMLEntityConverter xec = XMLEntityConverter.newInstance();
      xec.setClient(client);
      xec.setOrganization(organization);
      xec.setOptionClientImport(isClientImport);
      xec.setOptionImportAuditInfo(importAuditInfo);
      xec.getEntityResolver().setOptionCreateReferencedIfNotFound(createReferencesIfNotFound);

      listNew = xec.getToInsert();
      listChanged = xec.getToUpdate();
      if (isClientImport) {
        xec.setEntityResolver(ClientImportEntityResolver.getInstance());
      }
      xec.setImportProcessor(importProcessor);
      xec.process(doc);

      ir.setLogMessages(xec.getLogMessages());
      ir.setErrorMessages(xec.getErrorMessages());
      ir.setWarningMessages(xec.getWarningMessages());

      if (ir.hasErrorOccured()) {
        OBDal.getInstance().rollbackAndClose();
        rolledBack = true;
        return ir;
      }

      if (importProcessor != null) {
        try {
          importProcessor.process(xec.getToInsert(), xec.getToUpdate());
        } catch (final Exception e) {
          // note on purpose caught and set in ImportResult
          ir.setException(e);
          ir.setErrorMessages(e.getMessage());
          OBDal.getInstance().rollbackAndClose();
          rolledBack = true;
          return ir;
        }
      }

      // note the ir object is adapted in this call
      saveUpdateConvertedObjects(xec, ir, isClientImport, module);

      // did an error occur during the saveUpdate
      if (ir.hasErrorOccured()) {
        OBDal.getInstance().rollbackAndClose();
        rolledBack = true;
        return ir;
      }
    } catch (final Throwable t) {
      boolean isBatchUpdateException = false;
      // We need to capture nested exception of BatchUpdate exception.
      Throwable cause = t.getCause();
      if (cause instanceof BatchUpdateException) {
        BatchUpdateException batchUpdateException = (BatchUpdateException) cause;
        if (batchUpdateException.getNextException() != null) {
          String errorMessage = batchUpdateException.getNextException().getMessage();
          String messageKey = errorMessage.substring("ERROR:".length()).trim();
          isBatchUpdateException = true;
          ir.setErrorMessages("isBatchUpdateException:" + messageKey);
        }
      }
      OBDal.getInstance().rollbackAndClose();
      rolledBack = true;
      if (!isBatchUpdateException) {
        ir.setException(t);
      }

    } finally {
      if (rolledBack) {
        TriggerHandler.getInstance().clear();
      } else if (TriggerHandler.getInstance().isDisabled()) {
        TriggerHandler.getInstance().enable();
        Vector<BaseOBObject> allObjs = new Vector<BaseOBObject>();
        allObjs.addAll(listNew);
        allObjs.addAll(listChanged);
        boolean containsAdRoleOrOrgAccess = false;
        for (BaseOBObject bob : allObjs) {
          if (bob instanceof Role || bob instanceof RoleOrganization) {
            containsAdRoleOrOrgAccess = true;
            break;
          }
        }
        if (containsAdRoleOrOrgAccess)
          SessionHandler.getInstance().getSession()
              .createSQLQuery("UPDATE AD_ROLE_ORGACCESS SET AD_ROLE_ID='0' where AD_ROLE_ID='0'")
              .executeUpdate();
      }
    }

    if (ir.hasErrorOccured()) {
      SessionHandler.getInstance().setDoRollback(true);
    }

    return ir;
  }

  /**
   * Performs the actual update/insert of objects in the correct order in the database. Also sets
   * the {@link ReferenceDataStore} if required.
   * 
   * @param xec
   *          the converter containing the to-be-inserted and to-be-updated objects
   * @param ir
   *          the ImportResult, warning and error messages are added to this object, also its
   *          {@link ImportResult#getUpdatedObjects()} and {@link ImportResult#getInsertedObjects()}
   *          is set
   * @param isClientImport
   *          is true if the import is for a client, in that case the {@link ReferenceDataStore}
   *          data is not set
   * @param module
   *          is set if the import is for a module, null is allowed
   */
  public void saveUpdateConvertedObjects(XMLEntityConverter xec, ImportResult ir,
      boolean isClientImport, Module module) {
    // now save and update
    // do inserts and updates in opposite order, this is important
    // so that the objects on which other depend are inserted first
    final List<BaseOBObject> toInsert = xec.getToInsert();
    int done = 0;
    final Set<BaseOBObject> inserted = new HashSet<BaseOBObject>();
    for (int i = toInsert.size() - 1; i > -1; i--) {
      final BaseOBObject ins = toInsert.get(i);
      // for (final BaseOBObject ins : toInsert) {
      insertObjectGraph(ins, inserted, new HashSet<BaseOBObject>());
      ir.getInsertedObjects().add(ins);
      done++;
    }
    Check.isTrue(done == toInsert.size(), "Not all objects have been inserted, check for loop: "
        + done + "/" + toInsert.size());

    // flush to set the ids in the objects
    OBDal.getInstance().flush();

    // do the updates the other way around also
    done = 0;
    final List<BaseOBObject> toUpdate = xec.getToUpdate();
    for (int i = toUpdate.size() - 1; i > -1; i--) {
      final BaseOBObject upd = toUpdate.get(i);
      OBDal.getInstance().save(upd);
      ir.getUpdatedObjects().add(upd);
      done++;
    }
    Check.isTrue(done == toUpdate.size(), "Not all objects have been inserted, check for loop: "
        + done + "/" + toUpdate.size());

    // flush to set the ids in the objects
    OBDal.getInstance().flush();

    // now walk through the objects to repair primitive reference id's
    // note updates both the ir and changes the entityresolver
    repairPrimitiveReferences(ir, xec.getEntityResolver());

    OBDal.getInstance().flush();

    // store the ad_ref_data_loaded
    if (!isClientImport) {
      OBContext.setAdminMode();
      try {
        for (final BaseOBObject ins : xec.getToInsert()) {
          final String originalId = xec.getEntityResolver().getOriginalId(ins);
          // completely new object, manually added to the xml
          if (originalId == null) {
            continue;
          }
          final ReferenceDataStore rdl = OBProvider.getInstance().get(ReferenceDataStore.class);
          if (ins instanceof ClientEnabled) {
            rdl.setClient(((ClientEnabled) ins).getClient());
          }
          if (ins instanceof OrganizationEnabled) {
            rdl.setOrganization(((OrganizationEnabled) ins).getOrganization());
          }
          rdl.setGeneric(originalId);
          rdl.setSpecific((String) ins.getId());
          rdl.setTable(OBDal.getInstance().get(Table.class, ins.getEntity().getTableId()));
          if (module != null) {
            rdl.setModule(module);
          }
          OBDal.getInstance().save(rdl);
        }
        OBDal.getInstance().flush();
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  // insert an object and all its many-to-one dependencies
  // which have not been inserted yet
  // this works fine as long the graph has no cycles
  // if there are cycles then Hibernate needs to resolve those
  private void insertObjectGraph(BaseOBObject toInsert, Set<BaseOBObject> inserted,
      Set<BaseOBObject> cycleDetect) {
    // prevent infinite looping and don't do the ones we already inserted
    // in a previous objectgraph
    if (cycleDetect.contains(toInsert)) {
      // just save it for now and hope for the best...
      try {
        OBDal.getInstance().save(toInsert);
      } catch (final Exception e) {
        log.warn("There was a problem inserting data in the database.");
        log.info("The following exception was raised: ", e);
        throw new OBException(e);
      }
      cycleDetect.remove(toInsert);
      return;
    }
    if (inserted.contains(toInsert)) {
      return;
    }
    inserted.add(toInsert);
    cycleDetect.add(toInsert);
    final Entity entity = toInsert.getEntity();
    for (final Property property : entity.getProperties()) {
      if (!property.isPrimitive() && !property.isOneToMany()) {
        final Object value = toInsert.get(property.getName());
        if (value instanceof BaseOBObject && ((BaseOBObject) value).isNewOBObject()) {
          insertObjectGraph((BaseOBObject) value, inserted, cycleDetect);
        }
      }
    }
    cycleDetect.remove(toInsert);
    try {
      OBDal.getInstance().save(toInsert);
    } catch (final Exception e) {
      log.warn("There was a problem inserting data in the database.");
      log.info("The following exception was raised: ", e);
      throw new OBException(e);
    }
  }
}
