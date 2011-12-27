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
import org.openbravo.test.dal.DalComplexQueryRequisitionTest;
import org.openbravo.test.dal.DalComplexQueryTestOrderLine;
import org.openbravo.test.dal.DalConnectionProviderTest;
import org.openbravo.test.dal.DalCopyTest;
import org.openbravo.test.dal.DalPerformanceInventoryLineTest;
import org.openbravo.test.dal.DalPerformanceProductTest;
import org.openbravo.test.dal.DalQueryTest;
import org.openbravo.test.dal.DalStoredProcedureTest;
import org.openbravo.test.dal.DalTest;
import org.openbravo.test.dal.DalUtilTest;
import org.openbravo.test.dal.DynamicEntityTest;
import org.openbravo.test.dal.HiddenUpdateTest;
import org.openbravo.test.dal.IssuesTest;
import org.openbravo.test.dal.MappingGenerationTest;
import org.openbravo.test.dal.OBContextTest;
import org.openbravo.test.dal.ReadByNameTest;
import org.openbravo.test.dal.ValidationTest;
import org.openbravo.test.expression.EvaluationTest;
import org.openbravo.test.model.ClassLoaderTest;
import org.openbravo.test.model.OneToManyTest;
import org.openbravo.test.model.RuntimeModelTest;
import org.openbravo.test.model.UniqueConstraintTest;
import org.openbravo.test.modularity.DBPrefixTest;
import org.openbravo.test.modularity.DatasetServiceTest;
import org.openbravo.test.modularity.MergePropertiesTest;
import org.openbravo.test.modularity.TableNameTest;
import org.openbravo.test.security.AccessLevelTest;
import org.openbravo.test.security.AllowedOrganizationsTest;
import org.openbravo.test.security.EntityAccessTest;
import org.openbravo.test.security.WritableReadableOrganizationClientTest;
import org.openbravo.test.system.ErrorTextParserTest;
import org.openbravo.test.system.SystemServiceTest;
import org.openbravo.test.system.SystemValidatorTest;
import org.openbravo.test.xml.DatasetExportTest;
import org.openbravo.test.xml.EntityXMLImportTestBusinessObject;
import org.openbravo.test.xml.EntityXMLImportTestReference;
import org.openbravo.test.xml.EntityXMLImportTestSingle;
import org.openbravo.test.xml.EntityXMLImportTestWarning;
import org.openbravo.test.xml.EntityXMLIssues;
import org.openbravo.test.xml.UniqueConstraintImportTest;

/**
 * This test class is called from the ant task run.tests. It contains all the testcases which are
 * runnable and valid. With the exception of test classes with side effects, i.e. change the
 * database without cleaning up.
 * 
 * NOTE: this suite should not contact test classes which have side-effects (change the database
 * without cleaning up).
 * 
 * @author mtaal
 */
public class AntTaskTests {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Test for org.openbravo.test.dal");
    // $JUnit-BEGIN$
    // suite.addTestSuite(CompositeIdTest.class);

    // dal
    suite.addTestSuite(DalComplexQueryRequisitionTest.class);
    suite.addTestSuite(DalComplexQueryTestOrderLine.class);
    suite.addTestSuite(DalPerformanceInventoryLineTest.class);
    suite.addTestSuite(DalPerformanceProductTest.class);
    suite.addTestSuite(DalQueryTest.class);
    suite.addTestSuite(DalConnectionProviderTest.class);
    suite.addTestSuite(DalTest.class);
    suite.addTestSuite(DalUtilTest.class);
    suite.addTestSuite(DalCopyTest.class);
    suite.addTestSuite(DynamicEntityTest.class);
    suite.addTestSuite(IssuesTest.class);
    suite.addTestSuite(HiddenUpdateTest.class);
    // suite.addTestSuite(HqlTest.class);
    suite.addTestSuite(MappingGenerationTest.class);
    suite.addTestSuite(ValidationTest.class);
    suite.addTestSuite(OBContextTest.class);
    suite.addTestSuite(DalStoredProcedureTest.class);
    suite.addTestSuite(ReadByNameTest.class);

    // expression
    suite.addTestSuite(EvaluationTest.class);

    // model
    suite.addTestSuite(RuntimeModelTest.class);
    suite.addTestSuite(OneToManyTest.class);
    suite.addTestSuite(UniqueConstraintTest.class);
    suite.addTestSuite(ClassLoaderTest.class);

    // modularity
    suite.addTestSuite(DatasetServiceTest.class);
    suite.addTestSuite(DBPrefixTest.class);
    suite.addTestSuite(MergePropertiesTest.class);
    suite.addTestSuite(TableNameTest.class);

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
    // not executing has side effects
    // suite.addTestSuite(ClientExportImportTest.class);
    suite.addTestSuite(EntityXMLImportTestBusinessObject.class);
    suite.addTestSuite(EntityXMLImportTestReference.class);
    suite.addTestSuite(EntityXMLImportTestSingle.class);
    suite.addTestSuite(EntityXMLImportTestWarning.class);
    suite.addTestSuite(EntityXMLIssues.class);
    suite.addTestSuite(UniqueConstraintImportTest.class);
    suite.addTestSuite(DatasetExportTest.class);

    suite.addTestSuite(ClassicSelectorTest.class);

    // $JUnit-END$
    return suite;
  }

}
