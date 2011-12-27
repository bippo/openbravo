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

package org.openbravo.service.json.test;

import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.module.DataPackage;
import org.openbravo.model.common.bank.Bank;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.service.json.DataEntityQueryService;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.QueryBuilder;
import org.openbravo.service.json.QueryBuilder.TextMatching;
import org.openbravo.test.base.BaseTest;

/**
 * Test the {@link DataEntityQueryService} and {@link QueryBuilder} classes.
 * 
 * @author mtaal
 */

public class DataQueryServiceTest extends BaseTest {

  /**
   * First simple test, read rows 10-100 from ADColumn
   */
  public void testQueryForColumn() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.setFirstResult(10);
    queryService.setMaxResults(100);

    final int totalCount = queryService.count();
    // rough test
    assertTrue(totalCount > 8000);

    final List<BaseOBObject> list = queryService.list();
    assertTrue(list.size() == 100);
    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
    }
  }

  /**
   * Filter on identifier
   */
  public void testFilterOnIdentifierStartsWith() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(JsonConstants.IDENTIFIER, "Smtp");
    queryService.setTextMatching(TextMatching.startsWith.name());
    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getDBColumnName().toUpperCase().startsWith("SMTP"));
    }
  }

  /**
   * Filter on accessible org
   */
  public void testFilterOnAccessibleOrg() throws Exception {
    OBContext.setOBContext("1000000", "1000000", "1000000", "1000008");
    try {
      addReadWriteAccess(Bank.class);
      final Bank bank = OBProvider.getInstance().get(Bank.class);
      bank.setName("test");
      bank.setActive(true);
      OBDal.getInstance().save(bank);
      OBDal.getInstance().flush();

      // check with org filtering
      {
        OBContext.setOBContext("1000003", "1000000", "1000000", "1000000");
        addReadWriteAccess(Bank.class);
        final DataEntityQueryService queryService = new DataEntityQueryService();
        queryService.setEntityName(Bank.ENTITY_NAME);
        queryService.addFilterParameter(JsonConstants.ORG_PARAMETER, "1000005");
        final List<BaseOBObject> list = queryService.list();

        // there should be two
        assertEquals(2, list.size());

        boolean found1000008 = false;
        for (BaseOBObject bob : list) {
          if (((Bank) bob).getOrganization().getId().equals("1000000")) {
            continue;
          }
          found1000008 = true;
          assertEquals("1000008", ((Bank) bob).getOrganization().getId());
        }
        assertTrue("Bank for org 1000008 not found", found1000008);
      }

      // now check without org filtering
      // temporary remove all roleorgs other than 1000003
      // otherwise the check won't work as all orgs are part
      // of the readable orgs
      {
        setSystemAdministratorContext();
        final OBCriteria<RoleOrganization> criteria = OBDal.getInstance().createCriteria(
            RoleOrganization.class);
        criteria.setFilterOnReadableClients(false);
        criteria.setFilterOnReadableOrganization(false);
        List<RoleOrganization> roleOrgs = criteria.list();
        for (RoleOrganization roleOrg : roleOrgs) {
          if (roleOrg.getRole().getId().equals("1000002")
              && !roleOrg.getOrganization().getId().equals("1000003")) {
            OBDal.getInstance().remove(roleOrg);
          }
        }
        OBDal.getInstance().flush();
      }

      {
        setTestUserContext();
        final String[] orgs = OBContext.getOBContext().getReadableOrganizations();
        // check that 1000008 is not there, so that the test is good
        for (String org : orgs) {
          assertTrue(org.equals("0") || org.equals("1000000") || org.equals("1000002")
              || org.equals("1000003"));
        }

        addReadWriteAccess(Bank.class);
        final DataEntityQueryService queryService = new DataEntityQueryService();
        queryService.setEntityName(Bank.ENTITY_NAME);
        final List<BaseOBObject> list = queryService.list();
        assertEquals(1, list.size());
        assertTrue(((Bank) list.get(0)).getOrganization().getId().equals("1000000"));
      }
    } finally {
      OBDal.getInstance().rollbackAndClose();
    }
  }

  /**
   * Filter on identifier and name with contains
   */
  public void testFilterOnIdentifierSubstring() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(JsonConstants.IDENTIFIER, "Serv");
    queryService.setTextMatching(TextMatching.substring.name());
    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getDBColumnName().toUpperCase().contains("SERV"));
    }
  }

  /**
   * Filter on identifier and name with contains
   */
  public void testFilterOnMultipleValues() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(JsonConstants.IDENTIFIER, "mtpserver");
    queryService.addFilterParameter(Column.PROPERTY_NAME, "Serv");
    queryService.setTextMatching(TextMatching.substring.name());
    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getName().contains("Serv"));
      assertTrue(column.getDBColumnName().contains("mtpserver"));
    }
  }

  /**
   * Filter on property of referenced entity
   */
  public void testFilterOnPropertyOfReference() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(Column.PROPERTY_TABLE + "." + JsonConstants.IDENTIFIER,
        "AD_Column");
    queryService.setTextMatching(TextMatching.startsWith.name());
    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getTable().getName().contains("ADColumn"));
    }
  }

  public void testFilterOnReferenceOfReference() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(Column.PROPERTY_TABLE + "." + Table.PROPERTY_WINDOW,
        "2CC1DC1EDEA2454F987E7F2BBF48A4AE");
    queryService.setTextMatching(TextMatching.startsWith.name());

    // test simple sorting
    queryService.setOrderBy(Column.PROPERTY_TABLE + "._identifier");

    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getTable().getWindow().getId().equals("2CC1DC1EDEA2454F987E7F2BBF48A4AE"));
    }
  }

  public void testFilterOnIdentifierOfReferenceOfReference() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(Column.PROPERTY_TABLE + "." + Table.PROPERTY_WINDOW + "."
        + JsonConstants.IDENTIFIER, "atase");
    queryService.setTextMatching(TextMatching.substring.name());

    // test simple sorting
    queryService.setOrderBy("-" + Column.PROPERTY_NAME);

    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getTable().getWindow().getIdentifier().contains("atase"));
    }
  }

  /**
   * Filter on property of referenced entity
   */
  public void testFilterOnPropertyOfReferenceOuterJoining() throws Exception {
    setSystemAdministratorContext();
    final DataEntityQueryService queryService = new DataEntityQueryService();
    queryService.setEntityName(Column.ENTITY_NAME);
    queryService.addFilterParameter(Column.PROPERTY_TABLE + "." + JsonConstants.IDENTIFIER,
        "AD_Column");
    queryService.addFilterParameter(Column.PROPERTY_TABLE + "." + Table.PROPERTY_DATAPACKAGE + "."
        + DataPackage.PROPERTY_NAME, "org.openbravo.model.ad.datamodel");
    queryService.setDoOrExpression();
    queryService.setOrderBy(Column.PROPERTY_TABLE + "." + Table.PROPERTY_NAME);
    queryService.setTextMatching(TextMatching.startsWith.name());
    final List<BaseOBObject> list = queryService.list();

    // we should test something!
    assertTrue(list.size() > 0);

    for (BaseOBObject bob : list) {
      assertTrue(bob instanceof Column);
      final Column column = (Column) bob;
      assertTrue(column.getTable().getName().contains("ADColumn")
          || column.getTable().getDataPackage().getName()
              .equals("org.openbravo.model.ad.datamodel"));
    }
  }

  /**
   * Orderby on identifier with foreign keys
   */
  public void testOrderByOnIdentifier() throws Exception {
    setTestUserContext();
    OBContext.setAdminMode();
    try {
      final DataEntityQueryService queryService = new DataEntityQueryService();
      queryService.setEntityName(FIN_FinaccTransaction.ENTITY_NAME);
      queryService.setOrderBy(JsonConstants.IDENTIFIER);
      queryService.setDoOrExpression();
      queryService.setTextMatching(TextMatching.startsWith.name());
      final List<BaseOBObject> list = queryService.list();

      // fire the query
      assertTrue(list.size() > 0 || list.size() == 0);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}