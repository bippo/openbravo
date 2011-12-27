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

package org.openbravo.test.xml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.ExcludeFilter;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.ddlutils.util.DBSMOBUtil;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.TreeNode;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.payment.DebtPayment;
import org.openbravo.model.project.Project;
import org.openbravo.service.db.ClientImportProcessor;
import org.openbravo.service.db.DataExportService;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;
import org.openbravo.service.system.SystemService;

/**
 * Tests export and import of client dataset.
 * 
 * <b>NOTE: this test has as side effect that new clients are created in the database with all their
 * data. These clients are not removed after the tests.</b>
 * 
 * @author mtaal
 */
public class ClientExportImportTest extends XMLBaseTest {

  // public void _testImportReferenceData() throws Exception {
  // setSystemAdministratorContext();
  //
  // final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
  // .getProperty("source.path");
  // final File importDir = new File(sourcePath, ReferenceDataTask.REFERENCE_DATA_DIRECTORY);
  //
  // for (final File importFile : importDir.listFiles()) {
  // if (importFile.isDirectory()) {
  // continue;
  // }
  // final ClientImportProcessor importProcessor = new ClientImportProcessor();
  // importProcessor.setNewName(null);
  // final ImportResult ir = DataImportService.getInstance().importClientData(importProcessor,
  // false, new FileReader(importFile));
  // if (ir.hasErrorOccured()) {
  // if (ir.getException() != null) {
  // throw new OBException(ir.getException());
  // }
  // if (ir.getErrorMessages() != null) {
  // throw new OBException(ir.getErrorMessages());
  // }
  // }
  // }
  // }

  /**
   * Exports the "F&B International Group" client and then imports as a new client. Has as side
   * effect that a completely new client is added in the database.
   * 
   * Also tests mantis 8509: https://issues.openbravo.com/view.php?id=8509
   * 
   * Also tests mantis 9000: https://issues.openbravo.com/view.php?id=9000
   */
  public void testExportImportClient1000000() {

    // This test has been temporarily disabled until the following issue related to the export
    // client functionality is fixed:
    // https://issues.openbravo.com/view.php?id=14848

    // final String newClientId = exportImport(TEST_CLIENT_ID);
    // testMantis8509(newClientId);
    // testAccountingFactMantis9000(newClientId);
    // testTreeNodesMantis9000(newClientId);
    // SystemService.getInstance().removeAllClientData(newClientId);
  }

  private void testTreeNodesMantis9000(String newClientID) {
    final OBQuery<TreeNode> nodes = OBDal.getInstance().createQuery(TreeNode.class,
        "client.id='" + newClientID + "'");
    nodes.setFilterOnReadableClients(false);
    nodes.setFilterOnReadableOrganization(false);
    assertTrue(nodes.list().size() > 0);
    final Client newClient = OBDal.getInstance().get(Client.class, newClientID);

    boolean testDoneAtLeastOnce = false;
    for (TreeNode node : nodes.list()) {
      assertEquals(newClient, node.getClient());
      // also ignore 0 as there is a business partner/sales region tree node with 0
      if (node.getNode() != null && !node.getNode().equals("0")) {
        final Entity entity = ModelProvider.getInstance().getEntityFromTreeType(
            node.getTree().getTypeArea());
        if (entity.getName().equals(Project.ENTITY_NAME)) {
          // can be removed when this issue:
          // https://issues.openbravo.com/view.php?id=8745
          // is solved
          continue;
        }

        final BaseOBObject bob = OBDal.getInstance().get(entity.getName(), node.getNode());
        assertTrue("Entity instance not found " + entity.getName() + " " + node.getNode(),
            bob != null);
        if (bob instanceof ClientEnabled) {
          assertEquals(newClient, ((ClientEnabled) bob).getClient());
          testDoneAtLeastOnce = true;
        }
      }
      // also ignore 0 as there is a business partner/sales region tree node with 0
      if (node.getReportSet() != null && !node.getReportSet().equals("0")) {
        final Entity entity = ModelProvider.getInstance().getEntityFromTreeType(
            node.getTree().getTypeArea());
        if (entity.getName().equals(Project.ENTITY_NAME)) {
          // can be removed when this issue:
          // https://issues.openbravo.com/view.php?id=8745
          // is solved
          continue;
        }

        final BaseOBObject bob = OBDal.getInstance().get(entity.getName(), node.getReportSet());
        assertTrue("Entity instance not found " + entity.getName() + " " + node.getReportSet(),
            bob != null);
        if (bob instanceof ClientEnabled) {
          assertEquals(newClient, ((ClientEnabled) bob).getClient());
          testDoneAtLeastOnce = true;
        }
      }
    }
    assertTrue(testDoneAtLeastOnce);

  }

  private void testAccountingFactMantis9000(String newClientID) {
    final OBQuery<AccountingFact> facts = OBDal.getInstance().createQuery(AccountingFact.class,
        "client.id='" + newClientID + "'");
    facts.setFilterOnReadableClients(false);
    facts.setFilterOnReadableOrganization(false);
    assertTrue(facts.list().size() > 0);
    final Client newClient = OBDal.getInstance().get(Client.class, newClientID);
    boolean testDoneAtLeastOnce = false;
    for (AccountingFact fact : facts.list()) {
      assertEquals(newClient, fact.getClient());
      if (fact.getRecordID() != null) {
        final BaseOBObject bob = OBDal.getInstance().get(fact.getTable().getName(),
            fact.getRecordID());
        assertTrue(
            "Entity instance not found " + fact.getTable().getName() + " " + fact.getRecordID(),
            bob != null);
        if (bob instanceof ClientEnabled) {
          assertEquals(newClient, ((ClientEnabled) bob).getClient());
          testDoneAtLeastOnce = true;
        }
      }
      if (fact.getRecordID2() != null) {
        final BaseOBObject bob = OBDal.getInstance().get(DebtPayment.ENTITY_NAME,
            fact.getRecordID2());
        assertTrue(
            "Entity instance not found " + DebtPayment.ENTITY_NAME + " " + fact.getRecordID2(),
            bob != null);
        if (bob instanceof ClientEnabled) {
          assertEquals(newClient, ((ClientEnabled) bob).getClient());
          testDoneAtLeastOnce = true;
        }
      }
    }
    assertTrue(testDoneAtLeastOnce);
  }

  /**
   * Exports the "QA Testing" client and then imports as a new client. Has as side effect that a
   * completely new client is added in the database.
   */
  public void _testExportImportClient1000001() {
    exportImport(QA_TEST_CLIENT_ID);
    // SystemService.getInstance().removeAllClientData(newClientId);
  }

  /**
   * Test which copies a client, then deletes it, and then tests that the foreign keys are still
   * activated
   */
  public void testDeleteClient() {
    Platform platform = SystemService.getInstance().getPlatform();
    ExcludeFilter excludeFilter = DBSMOBUtil.getInstance().getExcludeFilter(
        new File(OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("source.path")));
    Database dbBefore = platform.loadModelFromDatabase(excludeFilter);
    String newClientId = exportImport(QA_TEST_CLIENT_ID);
    Client client = OBDal.getInstance().get(Client.class, newClientId);

    SystemService.getInstance().deleteClient(client);
    Database dbAfter = platform.loadModelFromDatabase(excludeFilter);
    for (int i = 0; i < dbBefore.getTableCount(); i++) {
      Table table1 = dbBefore.getTable(i);
      Table table2 = dbAfter.getTable(i);
      for (int j = 0; j < table1.getForeignKeyCount(); j++) {
        assertTrue(table1.getForeignKey(j).equals(table2.getForeignKey(j)));
      }
    }
  }

  // tests mantis issue 8509 related to import of ad tree node as
  // part of client import:
  // 8509: References in the database without using foreign keys can go wrong in import
  // https://issues.openbravo.com/view.php?id=8509
  private void testMantis8509(String clientId) {
    setSystemAdministratorContext();
    final OrganizationStructureProvider osp = new OrganizationStructureProvider();
    osp.setClientId(clientId);
    final Client client = OBDal.getInstance().get(Client.class, clientId);
    final OBCriteria<Organization> os = OBDal.getInstance().createCriteria(Organization.class);
    os.setFilterOnReadableClients(false);
    os.setFilterOnReadableOrganization(false);
    os.setFilterOnActive(false);
    os.add(Restrictions.eq("client", client));
    for (Organization o : os.list()) {
      final Set<String> naturalTree = osp.getNaturalTree(o.getId());
      // all the organizations should at least have a tree of size 2
      if (naturalTree.size() <= 1) {
        fail("Naturaltree computation fails for organization " + o.getId() + " in imported client "
            + clientId);
      }
    }
  }

  private String exportImport(String clientId) {
    setSystemAdministratorContext();
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(DataExportService.CLIENT_ID_PARAMETER_NAME, clientId);

    final StringWriter sw = new StringWriter();
    DataExportService.getInstance().exportClientToXML(parameters, false, sw);
    String xml = sw.toString();
    try {
      final String sourcePath = (String) OBPropertiesProvider.getInstance()
          .getOpenbravoProperties().get("source.path");
      final File dir = new File(sourcePath + File.separator + "temp");
      if (!dir.exists()) {
        dir.mkdir();
      }
      final File f = new File(dir, "export.xml");
      if (f.exists()) {
        f.delete();
      }
      f.createNewFile();
      final FileWriter fw = new FileWriter(f);
      fw.write(xml);
      fw.close();
    } catch (final Exception e) {
      throw new OBException(e);
    }

    final ClientImportProcessor importProcessor = new ClientImportProcessor();
    importProcessor.setNewName("" + System.currentTimeMillis());
    try {
      final ImportResult ir = DataImportService.getInstance().importClientData(importProcessor,
          false, new StringReader(xml));
      xml = null;
      if (ir.getException() != null) {
        throw new OBException(ir.getException());
      }
      if (ir.getErrorMessages() != null) {
        fail(ir.getErrorMessages());
      }
      // none should be updated!
      assertEquals(0, ir.getUpdatedObjects().size());

      String newClientId = null;

      // and never insert anything in client 0
      for (final BaseOBObject bob : ir.getInsertedObjects()) {
        if (bob instanceof ClientEnabled) {
          final ClientEnabled ce = (ClientEnabled) bob;
          assertNotNull(ce.getClient());
          assertTrue(!ce.getClient().getId().equals("0"));
          newClientId = ce.getClient().getId();
        }
      }
      assertTrue(newClientId != null);
      assertTrue(!clientId.equals(newClientId));
      commitTransaction();
      return newClientId;
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

  public void _testImportAccountingTest() {
    doImport("Accounting_Test.xml");
  }

  private void doImport(String fileName) {
    setSystemAdministratorContext();

    final ClientImportProcessor importProcessor = new ClientImportProcessor();
    try {
      // final URL url = this.getClass().getResource("testdata/" + fileName);
      // final File f = new File(new URI(url.toString()));

      final File f = new File(fileName); // "/home/mtaal/mytmp/" +

      final ImportResult ir = DataImportService.getInstance().importClientData(importProcessor,
          false, new FileReader(f));
      if (ir.getException() != null) {
        throw new OBException(ir.getException());
      }
      if (ir.getErrorMessages() != null && ir.getErrorMessages().trim().length() > 0) {
        fail(ir.getErrorMessages());
      }
    } catch (final Exception e) {
      throw new OBException(e);
    }
  }

}