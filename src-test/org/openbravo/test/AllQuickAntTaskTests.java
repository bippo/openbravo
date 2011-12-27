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

package org.openbravo.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openbravo.erpCommon.info.ClassicSelectorTest;
import org.openbravo.test.dal.AdminContextTest;
import org.openbravo.test.dal.DalConnectionProviderTest;
import org.openbravo.test.dal.DalFilterTest;
import org.openbravo.test.dal.DalStoredProcedureTest;
import org.openbravo.test.dal.DalTest;
import org.openbravo.test.dal.DalUtilTest;
import org.openbravo.test.dal.OBContextTest;
import org.openbravo.test.dal.ValidationTest;
import org.openbravo.test.model.ClassLoaderTest;
import org.openbravo.test.model.UniqueConstraintTest;
import org.openbravo.test.modularity.DBPrefixTest;
import org.openbravo.test.preference.PreferenceTest;
import org.openbravo.test.security.AccessLevelTest;
import org.openbravo.test.security.AllowedOrganizationsTest;
import org.openbravo.test.security.EntityAccessTest;
import org.openbravo.test.security.WritableReadableOrganizationClientTest;
import org.openbravo.test.system.ErrorTextParserTest;
import org.openbravo.test.system.SystemServiceTest;
import org.openbravo.test.system.SystemValidatorTest;
import org.openbravo.test.xml.EntityXMLImportTestBusinessObject;
import org.openbravo.test.xml.EntityXMLImportTestReference;
import org.openbravo.test.xml.EntityXMLImportTestSingle;
import org.openbravo.test.xml.EntityXMLImportTestWarning;
import org.openbravo.test.xml.EntityXMLIssues;
import org.openbravo.test.xml.UniqueConstraintImportTest;

/**
 * This test suite should only contain test cases which are quick to run. This makes it possible as
 * a developer to run tests in between without waiting to long for results. Testcases which should
 * not be here is for example the import of a complete client.
 * 
 * NOTE: this suite should not contact test classes which have side-effects (change the database
 * without cleaning up).
 * 
 * @author mtaal
 */
public class AllQuickAntTaskTests {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Test for org.openbravo.test.dal");
    // $JUnit-BEGIN$
    // suite.addTestSuite(CompositeIdTest.class);

    // dal
    // suite.addTestSuite(DalComplexQueryRequisitionTest.class);
    // suite.addTestSuite(DalComplexQueryTestOrderLine.class);
    // suite.addTestSuite(DalPerformanceInventoryLineTest.class);
    // suite.addTestSuite(DalPerformanceProductTest.class);
    // suite.addTestSuite(DalQueryTest.class);
    suite.addTestSuite(DalTest.class);
    suite.addTestSuite(DalFilterTest.class);
    suite.addTestSuite(DalUtilTest.class);
    suite.addTestSuite(DalConnectionProviderTest.class);
    // suite.addTestSuite(DynamicEntityTest.class);
    // suite.addTestSuite(HiddenUpdateTest.class);
    // suite.addTestSuite(HqlTest.class);
    // suite.addTestSuite(MappingGenerationTest.class);
    suite.addTestSuite(ValidationTest.class);
    suite.addTestSuite(OBContextTest.class);
    suite.addTestSuite(DalStoredProcedureTest.class);
    suite.addTestSuite(AdminContextTest.class);

    // expression
    // suite.addTestSuite(EvaluationTest.class);

    // model
    // suite.addTestSuite(RuntimeModelTest.class);
    // suite.addTestSuite(OneToManyTest.class);
    suite.addTestSuite(UniqueConstraintTest.class);
    suite.addTestSuite(ClassLoaderTest.class);

    // modularity
    // suite.addTestSuite(DatasetServiceTest.class);
    suite.addTestSuite(DBPrefixTest.class);

    // security
    suite.addTestSuite(AccessLevelTest.class);
    suite.addTestSuite(AllowedOrganizationsTest.class);
    suite.addTestSuite(EntityAccessTest.class);
    suite.addTestSuite(WritableReadableOrganizationClientTest.class);

    // system
    suite.addTestSuite(SystemServiceTest.class);
    suite.addTestSuite(SystemValidatorTest.class);
    suite.addTestSuite(ErrorTextParserTest.class);

    // xml
    // suite.addTestSuite(ClientExportImportTest.class);
    suite.addTestSuite(EntityXMLImportTestBusinessObject.class);
    suite.addTestSuite(EntityXMLImportTestReference.class);
    suite.addTestSuite(EntityXMLImportTestSingle.class);
    suite.addTestSuite(EntityXMLImportTestWarning.class);
    suite.addTestSuite(EntityXMLIssues.class);
    suite.addTestSuite(UniqueConstraintImportTest.class);

    // preferences
    suite.addTestSuite(PreferenceTest.class);

    suite.addTestSuite(ClassicSelectorTest.class);

    // $JUnit-END$
    return suite;
  }
}
